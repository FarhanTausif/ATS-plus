"""
Client Example for Ollama ATS Server
This script shows how other PCs can connect to your Ollama ATS server.
"""

import requests
import json
import os
from typing import Optional

class OllamaATSClient:
    """Client class for connecting to Ollama ATS Server"""
    
    def __init__(self, server_host: str, server_port: int = 8000):
        """
        Initialize client with server details
        
        Args:
            server_host: IP address of the server PC
            server_port: Port number (default: 8000)
        """
        self.base_url = f"http://{server_host}:{server_port}"
        self.session = requests.Session()
        
    def check_server_health(self) -> bool:
        """Check if the server is running and healthy"""
        try:
            response = self.session.get(f"{self.base_url}/health", timeout=10)
            if response.status_code == 200:
                health_data = response.json()
                print("✅ Server is healthy!")
                print(f"   Status: {health_data['status']}")
                print(f"   Ollama Connected: {health_data['ollama_connected']}")
                return True
            else:
                print(f"❌ Server returned status {response.status_code}")
                return False
        except requests.exceptions.RequestException as e:
            print(f"❌ Cannot connect to server: {e}")
            return False
    
    def get_server_info(self) -> Optional[dict]:
        """Get basic server information"""
        try:
            response = self.session.get(f"{self.base_url}/", timeout=10)
            if response.status_code == 200:
                return response.json()
            return None
        except Exception as e:
            print(f"❌ Error getting server info: {e}")
            return None
    
    def analyze_with_files(self, resume_path: str, job_description_path: str, 
                          model: Optional[str] = None) -> Optional[dict]:
        """
        Send PDF files to server for analysis
        
        Args:
            resume_path: Path to resume PDF file
            job_description_path: Path to job description PDF file
            model: Ollama model to use (optional)
        """
        try:
            # Check if files exist
            if not os.path.exists(resume_path):
                print(f"❌ Resume file not found: {resume_path}")
                return None
                
            if not os.path.exists(job_description_path):
                print(f"❌ Job description file not found: {job_description_path}")
                return None
            
            # Prepare files
            with open(resume_path, 'rb') as resume_file, open(job_description_path, 'rb') as job_file:
                files = {
                    'resume': ('resume.pdf', resume_file, 'application/pdf'),
                    'job_description': ('job.pdf', job_file, 'application/pdf')
                }
                
                data = {}
                if model:
                    data['model'] = model
                
                print(f"📤 Sending files to server...")
                print(f"   Resume: {os.path.basename(resume_path)}")
                print(f"   Job Description: {os.path.basename(job_description_path)}")
                if model:
                    print(f"   Model: {model}")
                
                response = self.session.post(
                    f"{self.base_url}/analyze",
                    files=files,
                    data=data,
                    timeout=600  # 10 minutes timeout
                )
                
                if response.status_code == 200:
                    result = response.json()
                    print("✅ Analysis completed successfully!")
                    print(f"   Processing time: {result['processing_time']:.2f} seconds")
                    print(f"   Model used: {result['model_used']}")
                    print(f"   Resume chars: {result['resume_chars']}")
                    print(f"   Job chars: {result['job_chars']}")
                    return result
                else:
                    error_data = response.json()
                    print(f"❌ Error: {response.status_code}")
                    print(f"   {error_data.get('detail', error_data.get('error', 'Unknown error'))}")
                    return None
                    
        except Exception as e:
            print(f"❌ Unexpected error: {e}")
            return None
    
    def analyze_with_text(self, resume_text: str, job_text: str, 
                         model: Optional[str] = None) -> Optional[dict]:
        """
        Send text directly to server for analysis
        
        Args:
            resume_text: Resume content as string
            job_text: Job description content as string
            model: Ollama model to use (optional)
        """
        try:
            data = {
                'resume_text': resume_text,
                'job_text': job_text
            }
            
            if model:
                data['model'] = model
            
            print(f"📤 Sending text to server...")
            print(f"   Resume length: {len(resume_text)} characters")
            print(f"   Job description length: {len(job_text)} characters")
            if model:
                print(f"   Model: {model}")
            
            response = self.session.post(
                f"{self.base_url}/analyze-text",
                json=data,
                headers={'Content-Type': 'application/json'},
                timeout=600  # 10 minutes timeout
            )
            
            if response.status_code == 200:
                result = response.json()
                print("✅ Analysis completed successfully!")
                print(f"   Processing time: {result['processing_time']:.2f} seconds")
                print(f"   Model used: {result['model_used']}")
                return result
            else:
                error_data = response.json()
                print(f"❌ Error: {response.status_code}")
                print(f"   {error_data.get('detail', error_data.get('error', 'Unknown error'))}")
                return None
                
        except Exception as e:
            print(f"❌ Unexpected error: {e}")
            return None
    
    def get_available_models(self) -> Optional[dict]:
        """Get list of available models from the server"""
        try:
            response = self.session.get(f"{self.base_url}/models", timeout=30)
            if response.status_code == 200:
                models = response.json()
                print("🤖 Available models:")
                for model in models.get('models', []):
                    print(f"   - {model['name']} (Size: {model.get('size', 'Unknown')})")
                return models
            else:
                print(f"❌ Error getting models: {response.status_code}")
                return None
        except Exception as e:
            print(f"❌ Error: {e}")
            return None

def main():
    """Example usage of the Ollama ATS Client"""
    
    # Configuration - Replace with your server PC's IP address
    SERVER_HOST = "192.168.177.123"  # Replace with actual server IP
    SERVER_PORT = 8000
    
    print("🚀 Ollama ATS Client Example")
    print("=" * 50)
    print(f"Server: {SERVER_HOST}:{SERVER_PORT}")
    print()
    
    # Create client
    client = OllamaATSClient(SERVER_HOST, SERVER_PORT)
    
    # 1. Check server health
    print("1. Checking server health...")
    if not client.check_server_health():
        print("❌ Server is not accessible. Please check:")
        print("   - Is the server running?")
        print("   - Is the IP address correct?")
        print("   - Are firewall ports open?")
        return
    
    # 2. Get server info
    print("\n2. Getting server information...")
    server_info = client.get_server_info()
    if server_info:
        print(f"   Service: {server_info['service']}")
        print(f"   Version: {server_info['version']}")
        print(f"   Status: {server_info['status']}")
    
    # 3. Get available models
    print("\n3. Getting available models...")
    client.get_available_models()
    
    # 4. Example analysis with text
    print("\n4. Example: Analyze with text")
    
    sample_resume = """
    John Doe
    Software Engineer
    Email: john.doe@email.com
    
    EXPERIENCE:
    Senior Python Developer (2020-2024)
    - Developed web applications using Flask and Django
    - Built REST APIs handling 10M+ requests daily
    - Optimized database queries reducing response time by 40%
    - Led team of 3 junior developers
    
    Python Developer (2018-2020)
    - Created automation scripts reducing manual work by 60%
    - Implemented CI/CD pipelines using Docker and Jenkins
    - Worked with PostgreSQL and MongoDB databases
    
    SKILLS:
    - Programming: Python, JavaScript, SQL
    - Frameworks: Flask, Django, FastAPI
    - Databases: PostgreSQL, MongoDB, Redis
    - Tools: Docker, Git, Jenkins, AWS
    
    EDUCATION:
    Bachelor of Science in Computer Science (2018)
    """
    
    sample_job = """
    Senior Python Developer Position
    
    We are seeking a Senior Python Developer to join our growing team.
    
    REQUIREMENTS:
    - 4+ years of Python development experience
    - Strong experience with web frameworks (Flask, Django, or FastAPI)
    - REST API development and optimization
    - Database design and query optimization
    - Experience with cloud platforms (AWS preferred)
    - Team leadership experience is a plus
    
    RESPONSIBILITIES:
    - Develop and maintain web applications
    - Design and implement REST APIs
    - Optimize application performance
    - Mentor junior developers
    - Collaborate with cross-functional teams
    
    SKILLS:
    - Python (required)
    - Flask/Django/FastAPI (required)
    - SQL databases (required)
    - Docker (preferred)
    - AWS (preferred)
    """
    
    print("Running sample analysis...")
    result = client.analyze_with_text(sample_resume, sample_job)
    
    if result:
        print("\n📄 Analysis Result:")
        print("=" * 60)
        print(result['analysis'])
        print("=" * 60)
    
    # 5. Example with files (commented out - uncomment when you have PDF files)
    print("\n5. Example: Analyze with PDF files")
    print("(Uncomment the lines below when you have actual PDF files)")
    """
    resume_path = "path/to/resume.pdf"
    job_path = "path/to/job_description.pdf"
    
    result = client.analyze_with_files(resume_path, job_path, model="llama3.2:3b")
    if result:
        print("\n📄 Analysis Result:")
        print("=" * 60)
        print(result['analysis'])
        print("=" * 60)
    """

if __name__ == "__main__":
    main()