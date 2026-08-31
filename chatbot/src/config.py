from pydantic_settings import BaseSettings
from typing import Optional
class Settings(BaseSettings):
    ollama_model: str = "llama2"
    ollama_base_url: str = "http://localhost:11434"
    chroma_persist_dir: str = "../chroma_db"
    cache_size: int = 100
    cache_ttl: int = 3600

    class Config:
        env_file = ".env"
        case_sensitive = False


settings = Settings()
