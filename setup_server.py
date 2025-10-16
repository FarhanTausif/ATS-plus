"""
Setup script for Ollama ATS Server
This script helps configure and start the Ollama ATS server on your PC.
"""

import os
import sys
import subprocess
import socket
import requests
import time
from pathlib import Path

def get_local_ip():
    """Get the local IP address of this machine"""
    try:
        # Connect to a remote server to determine local IP
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        local_ip = s.getsockname()[0]
        s.close()
        return local_ip
    except Exception:
        return "127.0.0.1"

def check_ollama_running():
    """Check if Ollama is running"""
    try:
        response = requests.get("http://localhost:11434/api/tags", timeout=5)
        return response.status_code == 200
    except:
        return False

def check_python_packages():
    """Check if required Python packages are installed"""
    required_packages = [
        'fastapi',
        'uvicorn',
        'requests',
        'PyPDF2',
        'python-dotenv'
    ]
    
    missing_packages = []
    
    for package in required_packages:
        try:
            __import__(package.replace('-', '_'))
        except ImportError:
            missing_packages.append(package)
    
    return missing_packages

def install_packages(packages):
    """Install missing Python packages"""
    print(f"Installing missing packages: {', '.join(packages)}")
    try:
        subprocess.check_call([
            sys.executable, "-m", "pip", "install"
        ] + packages)
        print("✅ Packages installed successfully!")
        return True
    except subprocess.CalledProcessError as e:
        print(f"❌ Failed to install packages: {e}")
        return False

def check_env_file():
    """Check if .env.local file exists and has required variables"""
    env_file = Path(".env.local")
    
    if not env_file.exists():
        print("❌ .env.local file not found!")
        return False
    
    required_vars = ['ATS_PROMPT', 'OLLAMA_API_URL', 'OLLAMA_MODEL']
    missing_vars = []
    
    with open(env_file, 'r') as f:
        content = f.read()
        for var in required_vars:
            if f"{var}=" not in content:
                missing_vars.append(var)
    
    if missing_vars:
        print(f"❌ Missing environment variables: {', '.join(missing_vars)}")
        return False
    
    print("✅ Environment file is properly configured!")
    return True

def get_firewall_info():
    """Get information about firewall configuration"""
    local_ip = get_local_ip()
    port = 8000
    
    print(f"\n🔥 FIREWALL CONFIGURATION REQUIRED:")
    print(f"   To allow other PCs to connect, you need to:")
    print(f"   1. Open port {port} in Windows Firewall")
    print(f"   2. Allow inbound connections on port {port}")
    print(f"   3. Other PCs should connect to: {local_ip}:{port}")
    print(f"\n   Windows Firewall Command (run as administrator):")
    print(f"   netsh advfirewall firewall add rule name=\"Ollama ATS Server\" dir=in action=allow protocol=TCP localport={port}")

def start_server():
    """Start the FastAPI server"""
    local_ip = get_local_ip()
    port = 8000
    
    print(f"\n🚀 Starting Ollama ATS Server...")
    print(f"   Local IP: {local_ip}")
    print(f"   Port: {port}")
    print(f"   Access URL: http://{local_ip}:{port}")
    print(f"   API Documentation: http://{local_ip}:{port}/docs")
    print(f"\n   Press Ctrl+C to stop the server")
    
    try:
        # Start the server using uvicorn
        subprocess.run([
            sys.executable, "-m", "uvicorn", 
            "ollama_server:app",
            "--host", "0.0.0.0",
            "--port", str(port),
            "--reload"
        ])
    except KeyboardInterrupt:
        print("\n👋 Server stopped by user")
    except Exception as e:
        print(f"❌ Error starting server: {e}")

def test_server():
    """Test if the server is responding"""
    local_ip = get_local_ip()
    port = 8000
    url = f"http://{local_ip}:{port}/health"
    
    print(f"🧪 Testing server at {url}...")
    
    for i in range(5):
        try:
            response = requests.get(url, timeout=5)
            if response.status_code == 200:
                print("✅ Server is responding correctly!")
                return True
        except:
            pass
        
        print(f"   Attempt {i+1}/5 failed, retrying...")
        time.sleep(2)
    
    print("❌ Server is not responding")
    return False

def main():
    """Main setup function"""
    print("🔧 Ollama ATS Server Setup")
    print("=" * 50)
    
    # 1. Check Python packages
    print("\n1. Checking Python packages...")
    missing_packages = check_python_packages()
    
    if missing_packages:
        print(f"❌ Missing packages: {', '.join(missing_packages)}")
        install = input("Install missing packages? (y/n): ").lower().strip()
        if install == 'y':
            if not install_packages(missing_packages):
                print("❌ Package installation failed. Please install manually:")
                print(f"   pip install {' '.join(missing_packages)}")
                return
        else:
            print("❌ Cannot continue without required packages")
            return
    else:
        print("✅ All required packages are installed!")
    
    # 2. Check Ollama
    print("\n2. Checking Ollama...")
    if check_ollama_running():
        print("✅ Ollama is running!")
    else:
        print("❌ Ollama is not running!")
        print("   Please start Ollama first:")
        print("   - Run 'ollama serve' in another terminal")
        print("   - Or start Ollama application")
        return
    
    # 3. Check environment file
    print("\n3. Checking environment configuration...")
    if not check_env_file():
        print("   Please create/fix .env.local file with required variables")
        return
    
    # 4. Show network information
    print("\n4. Network configuration...")
    local_ip = get_local_ip()
    print(f"✅ Your PC's IP address: {local_ip}")
    get_firewall_info()
    
    # 5. Ask to start server
    print("\n5. Ready to start server!")
    start = input("Start the Ollama ATS Server now? (y/n): ").lower().strip()
    
    if start == 'y':
        start_server()
    else:
        print("👋 Setup complete! Run this script again to start the server.")
        print(f"\nTo start manually:")
        print(f"   python -m uvicorn ollama_server:app --host 0.0.0.0 --port 8000 --reload")

if __name__ == "__main__":
    main()