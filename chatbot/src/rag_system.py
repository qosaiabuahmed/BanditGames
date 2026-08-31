import chromadb
from chromadb.config import Settings as ChromaSettings
from sentence_transformers import SentenceTransformer
from typing import List, Dict, Optional
import ollama
from config import settings
from knowledge_loader import KnowledgeLoader
class RAGSystem:
    """
    Retrieval-Augmented Generation system for the chatbot.
    Uses ChromaDB for vector storage and Ollama for LLM generation.
    """

    def __init__(self):
        # Initialize embedding model
        self.embedding_model = SentenceTransformer('all-MiniLM-L6-v2')

        # Initialize Ollama client with custom host
        self.ollama_client = ollama.Client(host=settings.ollama_base_url)

        # Initialize ChromaDB
        self.chroma_client = chromadb.PersistentClient(
            path=settings.chroma_persist_dir,
            settings=ChromaSettings(
                anonymized_telemetry=False
            )
        )

        # Get or create collection
        self.collection = self.chroma_client.get_or_create_collection(
            name="game_rules_knowledge",
            metadata={"description": "Game rules and platform guidance knowledge base"}
        )

        # Load knowledge base
        self.knowledge_loader = KnowledgeLoader()

        # Initialize if empty
        if self.collection.count() == 0:
            self._initialize_knowledge_base()

    def _initialize_knowledge_base(self):
        """Load and index all documents from knowledge base."""
        print("Initializing knowledge base...")
        documents = self.knowledge_loader.get_documents()

        # Prepare data for ChromaDB
        ids = [f"doc_{i}" for i in range(len(documents))]
        contents = [doc["content"] for doc in documents]
        metadatas = [doc["metadata"] for doc in documents]

        # Generate embeddings
        embeddings = self.embedding_model.encode(contents).tolist()

        # Add to collection
        self.collection.add(
            ids=ids,
            documents=contents,
            embeddings=embeddings,
            metadatas=metadatas
        )

        print(f"Loaded {len(documents)} documents into knowledge base.")

    def retrieve_context(self, query: str, n_results: int = 3) -> List[Dict]:
        """
        Retrieve relevant context from the knowledge base.

        Args:
            query: User's question
            n_results: Number of relevant documents to retrieve

        Returns:
            List of relevant documents with metadata
        """
        # Generate query embedding
        query_embedding = self.embedding_model.encode([query]).tolist()

        # Search in ChromaDB
        results = self.collection.query(
            query_embeddings=query_embedding,
            n_results=n_results
        )

        # Format results
        contexts = []
        if results["documents"] and len(results["documents"]) > 0:
            for i, doc in enumerate(results["documents"][0]):
                contexts.append({
                    "content": doc,
                    "metadata": results["metadatas"][0][i] if results["metadatas"] else {},
                    "distance": results["distances"][0][i] if results.get("distances") else 0
                })

        return contexts

    def generate_response(self, query: str, contexts: List[Dict]) -> str:
        """
        Generate a response using Ollama LLM with retrieved context.

        Args:
            query: User's question
            contexts: Retrieved context documents

        Returns:
            Generated response
        """
        # Build context string
        context_str = "\n\n".join([
            f"[Source {i+1}]: {ctx['content']}"
            for i, ctx in enumerate(contexts)
        ])

        # Create prompt
        prompt = f"""You are a helpful assistant for a board game platform. Use the following context to answer the user's question. If the context doesn't contain enough information, say so politely and provide general guidance.

Context:
{context_str}

User Question: {query}

Answer: Provide a clear, helpful response based on the context above. Be concise but complete. If the question is about game rules, provide step-by-step explanations when appropriate."""

        try:
            # Call Ollama using configured client
            response = self.ollama_client.chat(
                model=settings.ollama_model,
                messages=[
                    {
                        "role": "system",
                        "content": "You are a helpful assistant for a board game platform. Provide clear, accurate, and friendly responses about game rules and platform usage."
                    },
                    {
                        "role": "user",
                        "content": prompt
                    }
                ]
            )

            return response['message']['content']

        except Exception as e:
            return f"I apologize, but I'm having trouble generating a response right now. Error: {str(e)}\n\nPlease make sure Ollama is running with the {settings.ollama_model} model."

    def query(self, question: str, n_results: int = 3) -> Dict:
        """
        Main query method that retrieves context and generates response.

        Args:
            question: User's question
            n_results: Number of context documents to retrieve

        Returns:
            Dictionary with response and metadata
        """
        # Retrieve relevant context
        contexts = self.retrieve_context(question, n_results)

        # Generate response
        response = self.generate_response(question, contexts)

        return {
            "response": response,
            "contexts": contexts,
            "sources": [ctx["metadata"] for ctx in contexts]
        }

    def add_document(self, content: str, metadata: Dict) -> None:
        """
        Add a new document to the knowledge base.

        Args:
            content: Document content
            metadata: Document metadata
        """
        # Generate ID
        doc_id = f"doc_{self.collection.count()}"

        # Generate embedding
        embedding = self.embedding_model.encode([content]).tolist()

        # Add to collection
        self.collection.add(
            ids=[doc_id],
            documents=[content],
            embeddings=embedding,
            metadatas=[metadata]
        )

    def get_stats(self) -> Dict:
        """Get RAG system statistics."""
        return {
            "total_documents": self.collection.count(),
            "embedding_model": "all-MiniLM-L6-v2",
            "llm_model": settings.ollama_model,
            "available_games": self.knowledge_loader.get_game_names()
        }
