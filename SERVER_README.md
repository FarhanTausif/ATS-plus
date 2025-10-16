# Ollama ATS Server Setup Guide

This guide helps you set up your PC as an Ollama ATS server that other PCs can connect to for resume analysis.

## 🏗️ Architecture Overview

```
┌─────────────────┐    HTTP API    ┌─────────────────┐    Local API    ┌─────────────────┐
│   Client PCs    │ ──────────────► │   Your PC       │ ──────────────► │     Ollama      │
│                 │                 │  (FastAPI)      │                 │   (Local LLM)   │
│ - Resume PDFs   │                 │ - Port 8000     │                 │ - Port 11434    │
│ - Job PDFs      │                 │ - File Upload   │                 │ - Model Inference│
│ - HTTP Client   │                 │ - Text Analysis │                 │ - llama3.2:3b   │
└─────────────────┘                 └─────────────────┘                 └─────────────────┘
```

## 📋 Prerequisites

1. **Ollama installed and running**
   - Download from: https://ollama.ai
   - Model installed (e.g., `ollama pull llama3.2:3b`)

2. **Python 3.8+ with pip**

3. **Required Python packages** (will be installed automatically)

## 🚀 Quick Start

### Step 1: Setup the Server

Run the setup script to configure everything:

```bash
python setup_server.py
```

This script will:
- ✅ Check and install required Python packages
- ✅ Verify Ollama is running
- ✅ Check environment configuration
- ✅ Show your PC's IP address
- ✅ Provide firewall configuration instructions
- ✅ Start the server

### Step 2: Configure Firewall

To allow other PCs to connect, open port 8000 in Windows Firewall:

**Option A: Using Command Line (as Administrator)**
```cmd
netsh advfirewall firewall add rule name="Ollama ATS Server" dir=in action=allow protocol=TCP localport=8000
```

**Option B: Using Windows GUI**
1. Open Windows Defender Firewall
2. Click "Advanced settings"
3. Click "Inbound Rules" → "New Rule"
4. Select "Port" → "TCP" → "Specific local ports: 8000"
5. Allow the connection
6. Name it "Ollama ATS Server"

### Step 3: Start the Server

If not started automatically by setup script:

```bash
python -m uvicorn ollama_server:app --host 0.0.0.0 --port 8000 --reload
```

## 🌐 Server Endpoints

Once running, your server provides these endpoints:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | GET | Server information |
| `/health` | GET | Health check |
| `/analyze` | POST | Analyze resume vs job (PDF files) |
| `/analyze-text` | POST | Analyze resume vs job (text) |
| `/models` | GET | Available Ollama models |
| `/docs` | GET | Interactive API documentation |

## 💻 Client Usage

### From Other PCs

1. **Install the client script dependencies:**
   ```bash
   pip install requests
   ```

2. **Update the server IP in client_example.py:**
   ```python
   SERVER_HOST = "192.168.1.100"  # Replace with your server's IP
   ```

3. **Run the client:**
   ```bash
   python client_example.py
   ```

### Example Client Code

```python
from client_example import OllamaATSClient

# Connect to server
client = OllamaATSClient("192.168.1.100", 8000)

# Check if server is healthy
if client.check_server_health():
    # Analyze with PDF files
    result = client.analyze_with_files("resume.pdf", "job.pdf")
    
    # Or analyze with text
    result = client.analyze_with_text(resume_text, job_text)
    
    if result:
        print(result['analysis'])
```

### Using cURL

```bash
# Health check
curl http://192.168.1.100:8000/health

# Analyze with text
curl -X POST "http://192.168.1.100:8000/analyze-text" \
     -H "Content-Type: application/json" \
     -d '{
       "resume_text": "Your resume content...",
       "job_text": "Job description content..."
     }'

# Analyze with files
curl -X POST "http://192.168.1.100:8000/analyze" \
     -F "resume=@resume.pdf" \
     -F "job_description=@job.pdf"
```

## ⚙️ Configuration

### Environment Variables (.env.local)

```bash
# Required
ATS_PROMPT="Your detailed ATS analysis prompt..."
OLLAMA_API_URL="http://localhost:11434/api/generate"
OLLAMA_MODEL="llama3.2:3b"

# Optional server configuration
SERVER_HOST="0.0.0.0"
SERVER_PORT="8000"
```

### Server Configuration

You can customize the server by setting environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_HOST` | `0.0.0.0` | Server bind address |
| `SERVER_PORT` | `8000` | Server port |
| `OLLAMA_API_URL` | `http://localhost:11434/api/generate` | Ollama API endpoint |
| `OLLAMA_MODEL` | `llama3.2:3b` | Default model to use |

## 🔍 Troubleshooting

### Server Won't Start

1. **Check if port is in use:**
   ```bash
   netstat -ano | findstr :8000
   ```

2. **Try different port:**
   ```bash
   python -m uvicorn ollama_server:app --host 0.0.0.0 --port 8001
   ```

### Clients Can't Connect

1. **Verify server is running:**
   ```bash
   curl http://localhost:8000/health
   ```

2. **Check firewall:** Ensure port 8000 is open

3. **Check IP address:** Use `ipconfig` to verify your PC's IP

4. **Test from server PC first:**
   ```bash
   curl http://YOUR_IP:8000/health
   ```

### Ollama Connection Issues

1. **Check Ollama is running:**
   ```bash
   ollama list
   ```

2. **Verify model is installed:**
   ```bash
   ollama pull llama3.2:3b
   ```

3. **Check Ollama service:**
   ```bash
   curl http://localhost:11434/api/tags
   ```

### Performance Issues

1. **Use faster model:** Change to smaller model like `llama3.2:1b`
2. **Adjust timeout:** Increase timeout in client requests
3. **Monitor resources:** Check CPU/RAM usage during analysis

## 📊 API Response Examples

### Successful Analysis Response

```json
{
  "success": true,
  "analysis": "STEP 1: Extract from Job Description\n- Required skills: Python, Flask, API development...",
  "processing_time": 15.7,
  "model_used": "llama3.2:3b",
  "resume_chars": 2547,
  "job_chars": 1823
}
```

### Error Response

```json
{
  "error": "Failed to extract text from PDF files",
  "detail": "Unable to parse PDF content"
}
```

## 🔒 Security Considerations

⚠️ **Important Security Notes:**

1. **Network Security:**
   - This setup allows any PC on your network to access the server
   - Consider using VPN for remote access
   - Don't expose to the internet without proper authentication

2. **File Upload:**
   - Maximum file size: 16MB
   - Only PDF files accepted
   - Files are processed in memory (not saved to disk)

3. **Production Deployment:**
   - Add authentication (API keys, OAuth)
   - Use HTTPS with SSL certificates
   - Implement rate limiting
   - Add request logging and monitoring

## 📈 Monitoring

### Health Check

```bash
curl http://your-server-ip:8000/health
```

Returns:
```json
{
  "status": "healthy",
  "service": "ollama-ats-server",
  "timestamp": 1697462400.0,
  "ollama_connected": true
}
```

### Server Logs

The server provides detailed logging. Monitor the console output for:
- Request processing times
- Ollama API calls
- Error messages
- File upload status

## 🆘 Support

If you encounter issues:

1. **Check the logs** in the server console
2. **Verify all prerequisites** are met
3. **Test with simple curl commands** first
4. **Check network connectivity** between PCs
5. **Ensure Ollama model is working** locally

## 📝 Example Workflow

1. **Server PC (Your PC):**
   ```bash
   # Start Ollama
   ollama serve
   
   # In another terminal, start ATS server
   python setup_server.py
   ```

2. **Client PC:**
   ```bash
   # Test connection
   curl http://192.168.1.100:8000/health
   
   # Run analysis
   python client_example.py
   ```

3. **View Results:**
   - Analysis appears in client terminal
   - Processing logs shown on server
   - API documentation at: http://192.168.1.100:8000/docs

---

🎉 **You're all set!** Your PC is now serving as an Ollama ATS server that other PCs can connect to for resume analysis.