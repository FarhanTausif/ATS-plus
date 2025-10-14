from google import genai
from google.genai import types
import pathlib
import os
from dotenv import load_dotenv

# Load environment variables from .env file
load_dotenv(dotenv_path=".env.local")

# Initialize Gemini client (add api_key if not using env variable)
client = genai.Client(api_key=os.getenv("GEMINI_API_KEY"))

# Paths to your PDF files
resume_path = pathlib.Path("./uploads/sample_cv.pdf")
jd_path = pathlib.Path("./uploads/job_description.pdf")

# Upload PDFs
resume_file = client.files.upload(file=resume_path, config={"mime_type": "application/pdf"})
jd_file = client.files.upload(file=jd_path, config={"mime_type": "application/pdf"})

# Prompt for ATS evaluation
prompt=os.getenv("ATS_PROMPT")

# Generate content using Gemini
response = client.models.generate_content(
    model="gemini-2.5-flash",
    contents=[resume_file, jd_file, prompt]
)

print("\n===== ATS Evaluation =====\n")
print(response.text)
