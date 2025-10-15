import requests
import PyPDF2
import os
from dotenv import load_dotenv
import time

# Load environment variables from .env file
load_dotenv(dotenv_path=".env.local")

# measure time

start_time = time.time()
def read_pdf(path):
    text = ""
    with open(path, "rb") as f:
        reader = PyPDF2.PdfReader(f)
        for page in reader.pages:
            text += page.extract_text() + "\n"
    return text.strip()

# Your ATS prompt
ATS_PROMPT = os.getenv("ATS_PROMPT")
print(f"ATS_PROMPT loaded: {'Yes' if ATS_PROMPT else 'No'}")

# Read PDF files
print("Reading PDF files...")
resume_text = read_pdf("./uploads/sample_cv.pdf")
job_text = read_pdf("./uploads/job_description.pdf")
print(f"Resume length: {len(resume_text)} characters")
print(f"Job description length: {len(job_text)} characters")

end_time=time.time()
print(f"PDF reading time: {end_time - start_time} seconds")

start_time = time.time()
# Prepare Ollama request
payload = {
    "model": "deepseek-r1:8b",
    "prompt": f"{ATS_PROMPT}\n\nJob Description:\n{job_text}\n\nResume:\n{resume_text}",
    "stream": False,
    # Model options
    "options": {
        "num_predict": 300,
        "temperature": 0.0,
        "num_ctx": 4096,
        "mirostat": 0,
        "repeat_penalty": 1.1,
        "top_p": 0.9,
        "stop": ["SCORE:", "MISSING:"]
    }
}

# Call local Ollama API
ollama_url = os.getenv("OLLAMA_API_URL", "http://localhost:11434/api/generate")
print(f"Calling Ollama at: {ollama_url}")
print("Making API request... (this may take a while)")

try:
    resp = requests.post(ollama_url, json=payload, timeout=600)
    print(f"Response status: {resp.status_code}")
    
    if resp.status_code != 200:
        print(f"Error response: {resp.text}")
        exit(1)
        
    end_time = time.time()
    print(f"======Total evaluation time: {end_time - start_time} seconds======")
    print(resp.json()["response"])
    
except requests.exceptions.RequestException as e:
    print(f"Error calling Ollama API: {e}")
except Exception as e:
    print(f"Unexpected error: {e}")
    if 'resp' in locals():
        print(f"Response text: {resp.text}")
