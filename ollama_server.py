"""
Ollama ATS Server using FastAPI
Makes this PC an Ollama server for ATS processing where other PCs can send API requests.
"""

from fastapi import FastAPI, File, UploadFile, Form, HTTPException, status
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
import requests
import PyPDF2
import os
from dotenv import load_dotenv
import time
import logging
import io
from pydantic import BaseModel
from typing import Optional
import uvicorn

# Load environment variables
load_dotenv(dotenv_path=".env.local")

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Create FastAPI app
app = FastAPI(
    title="Ollama ATS Server",
    description="API server for ATS resume analysis using Ollama",
    version="1.0.0"
)

# Add CORS middleware to allow requests from other PCs
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, specify actual domains
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Pydantic models for request/response
class TextAnalysisRequest(BaseModel):
    resume_text: str
    job_text: str
    model: Optional[str] = None

class AnalysisResponse(BaseModel):
    success: bool
    analysis: str
    processing_time: float
    model_used: str
    resume_chars: int
    job_chars: int

class HealthResponse(BaseModel):
    status: str
    service: str
    timestamp: float
    ollama_connected: bool

class ErrorResponse(BaseModel):
    error: str
    detail: Optional[str] = None

def read_pdf_from_bytes(pdf_bytes: bytes) -> Optional[str]:
    """Extract text from PDF bytes"""
    try:
        text = ""
        pdf_file = io.BytesIO(pdf_bytes)
        reader = PyPDF2.PdfReader(pdf_file)
        for page in reader.pages:
            text += page.extract_text() + "\n"
        return text.strip()
    except Exception as e:
        logger.error(f"Error reading PDF: {e}")
        return None

def call_ollama_api(prompt: str, model: Optional[str] = None) -> Optional[str]:
    """Call the local Ollama API"""
    if model is None:
        model = os.getenv("OLLAMA_MODEL", "llama3.2:3b")
    
    ollama_url = os.getenv("OLLAMA_API_URL", "http://localhost:11434/api/generate")
    
    payload = {
        "model": model,
        "prompt": prompt,
        "stream": False,
        "options": {
            "num_predict": 2000,
            "temperature": 0.0,
            "num_ctx": 4096,
            "mirostat": 0,
            "repeat_penalty": 1.1,
            "top_p": 0.9,
            "stop": ["SCORE:", "MISSING:"]
        }
    }
    
    try:
        response = requests.post(ollama_url, json=payload, timeout=600)
        if response.status_code == 200:
            return response.json()["response"]
        else:
            logger.error(f"Ollama API error: {response.status_code} - {response.text}")
            return None
    except Exception as e:
        logger.error(f"Error calling Ollama API: {e}")
        return None

def check_ollama_connection() -> bool:
    """Check if Ollama is running and accessible"""
    try:
        ollama_url = os.getenv("OLLAMA_API_URL", "http://localhost:11434")
        response = requests.get(f"{ollama_url}/api/tags", timeout=10)
        return response.status_code == 200
    except:
        return False

@app.get("/", response_model=dict)
async def root():
    """Root endpoint with basic info"""
    return {
        "service": "Ollama ATS Server",
        "version": "1.0.0",
        "status": "running",
        "endpoints": {
            "health": "/health",
            "analyze_files": "/analyze",
            "analyze_text": "/analyze-text",
            "models": "/models",
            "docs": "/docs"
        }
    }

@app.get("/health", response_model=HealthResponse)
async def health_check():
    """Health check endpoint"""
    ollama_connected = check_ollama_connection()
    return HealthResponse(
        status="healthy" if ollama_connected else "degraded",
        service="ollama-ats-server",
        timestamp=time.time(),
        ollama_connected=ollama_connected
    )

@app.post("/analyze", response_model=AnalysisResponse)
async def analyze_resume_files(
    resume: UploadFile = File(..., description="Resume PDF file"),
    job_description: UploadFile = File(..., description="Job description PDF file"),
    model: Optional[str] = Form(None, description="Ollama model to use")
):
    """
    Analyze resume against job description using PDF files
    """
    start_time = time.time()
    
    try:
        # Validate file types
        if not resume.filename.lower().endswith('.pdf'):
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Resume must be a PDF file"
            )
        
        if not job_description.filename.lower().endswith('.pdf'):
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Job description must be a PDF file"
            )
        
        logger.info(f"Processing files: {resume.filename}, {job_description.filename}")
        
        # Read PDF contents
        resume_bytes = await resume.read()
        job_bytes = await job_description.read()
        
        resume_text = read_pdf_from_bytes(resume_bytes)
        job_text = read_pdf_from_bytes(job_bytes)
        
        if not resume_text or not job_text:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Failed to extract text from PDF files"
            )
        
        logger.info(f"Extracted text - Resume: {len(resume_text)} chars, Job: {len(job_text)} chars")
        
        # Get ATS prompt
        ats_prompt = os.getenv("ATS_PROMPT")
        if not ats_prompt:
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="ATS_PROMPT not configured on server"
            )
        
        # Prepare full prompt
        full_prompt = f"{ats_prompt}\n\nJob Description:\n{job_text}\n\nResume:\n{resume_text}"
        
        # Call Ollama API
        logger.info("Calling Ollama API...")
        result = call_ollama_api(full_prompt, model)
        
        if result is None:
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Failed to get response from Ollama"
            )
        
        end_time = time.time()
        processing_time = end_time - start_time
        
        logger.info(f"Analysis completed in {processing_time:.2f} seconds")
        
        return AnalysisResponse(
            success=True,
            analysis=result,
            processing_time=processing_time,
            model_used=model or os.getenv("OLLAMA_MODEL", "llama3.2:3b"),
            resume_chars=len(resume_text),
            job_chars=len(job_text)
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error in analyze_resume_files: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Internal server error: {str(e)}"
        )

@app.post("/analyze-text", response_model=AnalysisResponse)
async def analyze_text(request: TextAnalysisRequest):
    """
    Analyze resume against job description using text input
    """
    start_time = time.time()
    
    try:
        resume_text = request.resume_text.strip()
        job_text = request.job_text.strip()
        
        if not resume_text or not job_text:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Both resume_text and job_text are required"
            )
        
        logger.info(f"Processing text - Resume: {len(resume_text)} chars, Job: {len(job_text)} chars")
        
        # Get ATS prompt
        ats_prompt = os.getenv("ATS_PROMPT")
        if not ats_prompt:
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="ATS_PROMPT not configured on server"
            )
        
        # Prepare full prompt
        full_prompt = f"{ats_prompt}\n\nJob Description:\n{job_text}\n\nResume:\n{resume_text}"
        
        # Call Ollama API
        logger.info("Calling Ollama API...")
        result = call_ollama_api(full_prompt, request.model)
        
        if result is None:
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Failed to get response from Ollama"
            )
        
        end_time = time.time()
        processing_time = end_time - start_time
        
        logger.info(f"Analysis completed in {processing_time:.2f} seconds")
        
        return AnalysisResponse(
            success=True,
            analysis=result,
            processing_time=processing_time,
            model_used=request.model or os.getenv("OLLAMA_MODEL", "llama3.2:3b"),
            resume_chars=len(resume_text),
            job_chars=len(job_text)
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error in analyze_text: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Internal server error: {str(e)}"
        )

@app.get("/models")
async def get_available_models():
    """Get list of available Ollama models"""
    try:
        ollama_url = os.getenv("OLLAMA_API_URL", "http://localhost:11434")
        response = requests.get(f"{ollama_url}/api/tags", timeout=30)
        
        if response.status_code == 200:
            return response.json()
        else:
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Failed to fetch models from Ollama"
            )
            
    except Exception as e:
        logger.error(f"Error fetching models: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to connect to Ollama: {str(e)}"
        )

@app.exception_handler(413)
async def file_too_large_handler(request, exc):
    return JSONResponse(
        status_code=413,
        content={"error": "File too large. Maximum size is 16MB."}
    )

if __name__ == "__main__":
    # Get configuration from environment
    host = os.getenv('SERVER_HOST', '0.0.0.0')  # Listen on all interfaces
    port = int(os.getenv('SERVER_PORT', 8000))
    
    logger.info(f"Starting Ollama ATS Server on {host}:{port}")
    logger.info(f"Ollama API URL: {os.getenv('OLLAMA_API_URL', 'http://localhost:11434/api/generate')}")
    logger.info(f"Model: {os.getenv('OLLAMA_MODEL', 'llama3.2:3b')}")
    
    uvicorn.run(
        "ollama_server:app",
        host=host,
        port=port,
        reload=True,
        log_level="info"
    )