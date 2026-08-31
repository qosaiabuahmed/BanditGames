from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from fastapi.responses import FileResponse
from pydantic import BaseModel
from typing import Optional, List, Dict
from rag_system import RAGSystem
from cache import response_cache
import time
from pathlib import Path

# Initialize FastAPI app
app = FastAPI(
    title="Board Game Platform Chatbot",
    description="RAG-based chatbot for game rules and platform assistance",
    version="1.0.0"
)

# Enable CORS for frontend connections
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, specify your frontend domain
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Mount static files
static_path = Path(__file__).parent / "static"
if static_path.exists():
    app.mount("/static", StaticFiles(directory=str(static_path)), name="static")

# Initialize RAG system
rag_system = None


async def warm_cache():
    """
    Warm up the cache by running common questions through the RAG system.
    This ensures fast responses for frequently asked questions.
    """
    print("\nWarming up cache...")

    # Get questions to warm up
    questions = response_cache.get_warmup_questions()

    if not questions:
        print("No warmup questions found. Skipping cache warming.")
        return

    warmed_count = 0
    failed_count = 0

    for i, question in enumerate(questions, 1):
        try:
            print(f"  [{i}/{len(questions)}] Processing: {question[:50]}...")

            # Run through RAG system to generate real response
            result = rag_system.query(question, n_results=3)

            # Cache the response
            response_cache.set(question, {
                "response": result["response"],
                "sources": result["sources"]
            })

            warmed_count += 1

        except Exception as e:
            print(f"  ⚠ Failed to warm cache for question: {e}")
            failed_count += 1

    print(f"✓ Cache warming complete: {warmed_count} cached, {failed_count} failed\n")


@app.on_event("startup")
async def startup_event():
    """Initialize RAG system on startup."""
    global rag_system
    print("Starting chatbot service...")
    rag_system = RAGSystem()

    # Warm up the cache with common questions
    await warm_cache()

    print("Chatbot service ready!")


# Request/Response models
class ChatRequest(BaseModel):
    question: str
    use_cache: bool = True
    n_results: int = 3

    class Config:
        json_schema_extra = {
            "example": {
                "question": "What are the rules of Connect Four?",
                "use_cache": True,
                "n_results": 3
            }
        }


class ChatResponse(BaseModel):
    response: str
    cached: bool
    sources: List[Dict]
    processing_time: float


class AddDocumentRequest(BaseModel):
    content: str
    metadata: Dict

    class Config:
        json_schema_extra = {
            "example": {
                "content": "Checkers is a classic board game played on an 8x8 board...",
                "metadata": {
                    "type": "game_overview",
                    "game": "Checkers"
                }
            }
        }


# API Endpoints
@app.get("/")
async def root():
    """Serve the chat interface."""
    static_file = Path(__file__).parent / "static" / "index.html"
    if static_file.exists():
        return FileResponse(str(static_file))
    else:
        return {
            "service": "Board Game Platform Chatbot",
            "version": "1.0.0",
            "status": "running",
            "endpoints": {
                "chat": "/chat",
                "health": "/health",
                "stats": "/stats",
                "cache": "/cache/stats",
                "add_document": "/knowledge/add"
            }
        }

@app.get("/api")
async def api_info():
    """API information endpoint."""
    return {
        "service": "Board Game Platform Chatbot",
        "version": "1.0.0",
        "status": "running",
        "endpoints": {
            "chat": "/chat",
            "health": "/health",
            "stats": "/stats",
            "cache": "/cache/stats",
            "add_document": "/knowledge/add"
        }
    }


@app.post("/chat", response_model=ChatResponse)
async def chat(request: ChatRequest):
    """
    Main chatbot endpoint. Answers questions about game rules and platform usage.

    - **question**: The user's question
    - **use_cache**: Whether to use cached responses (default: True)
    - **n_results**: Number of context documents to retrieve (default: 3)
    """
    if not rag_system:
        raise HTTPException(status_code=503, detail="RAG system not initialized")

    start_time = time.time()

    # Check cache first
    cached_response = None
    if request.use_cache:
        cached_response = response_cache.get(request.question)

    if cached_response:
        return ChatResponse(
            response=cached_response["response"],
            cached=True,
            sources=cached_response.get("sources", []),
            processing_time=time.time() - start_time
        )

    # Generate new response
    try:
        result = rag_system.query(request.question, request.n_results)

        # Cache the response
        if request.use_cache:
            response_cache.set(request.question, {
                "response": result["response"],
                "sources": result["sources"]
            })

        return ChatResponse(
            response=result["response"],
            cached=False,
            sources=result["sources"],
            processing_time=time.time() - start_time
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error generating response: {str(e)}")


@app.get("/health")
async def health():
    """Health check endpoint."""
    return {
        "status": "healthy",
        "rag_system": "initialized" if rag_system else "not initialized",
        "cache": "active"
    }


@app.get("/stats")
async def stats():
    """Get system statistics."""
    if not rag_system:
        raise HTTPException(status_code=503, detail="RAG system not initialized")

    return {
        "rag": rag_system.get_stats(),
        "cache": response_cache.get_stats()
    }


@app.get("/cache/stats")
async def cache_stats():
    """Get cache statistics."""
    return response_cache.get_stats()


@app.post("/cache/clear")
async def clear_cache():
    """Clear the response cache."""
    response_cache.clear()
    return {"message": "Cache cleared successfully"}


@app.post("/knowledge/add")
async def add_document(request: AddDocumentRequest):
    """
    Add a new document to the knowledge base.

    - **content**: The document content
    - **metadata**: Document metadata (type, game, category, etc.)
    """
    if not rag_system:
        raise HTTPException(status_code=503, detail="RAG system not initialized")

    try:
        rag_system.add_document(request.content, request.metadata)
        return {
            "message": "Document added successfully",
            "total_documents": rag_system.collection.count()
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error adding document: {str(e)}")


@app.get("/games")
async def list_games():
    """List all available games in the knowledge base."""
    if not rag_system:
        raise HTTPException(status_code=503, detail="RAG system not initialized")

    return {
        "games": rag_system.knowledge_loader.get_game_names()
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=7777)
