# ATS-Plus: CV Scoring System with Local Llama Integration

## Overview
This application now supports both remote Gemini AI and local Llama 3.2 3B model for CV scoring and analysis.

## Features
- **Dual AI Support**: Choose between Gemini AI (cloud) and Llama 3.2 3B (local)
- **PDF Processing**: Extract and analyze content from Google Drive PDF links
- **CV Scoring**: Compare CVs against job requirements with AI-powered matching
- **Real-time Model Detection**: Automatically detects if local Llama model is available

## Prerequisites for Local Llama

### 1. Install Ollama
```bash
# On Linux/Mac
curl -fsSL https://ollama.ai/install.sh | sh

# Or download from https://ollama.ai/download
```

### 2. Pull Llama 3.2 3B Model
```bash
ollama pull llama3.2:3b
```

### 3. Start Ollama Service
```bash
ollama serve
```

The service will run on `http://localhost:11434` by default.

## Configuration

### Application Properties
The following configurations are available in `application.properties`:

```properties
# Local Llama Configuration
llama.api.url=http://localhost:11434
llama.model.name=llama3.2:3b

# Gemini API Configuration
spring.ai.google.genai.api-key=YOUR_GEMINI_API_KEY
gemini.api.key=YOUR_GEMINI_API_KEY
```

## Usage

### Starting the Application
```bash
mvn spring-boot:run
```

### Web Interface
1. Navigate to `http://localhost:8080`
2. Enter CV and Job Requirements PDF links (Google Drive shareable links)
3. Choose your processing method:
   - **📊 Score with Gemini AI**: Uses Google's Gemini 2.5 Flash model (requires API key)
   - **🦙 Score with Local Llama**: Uses your local Llama 3.2 3B model (requires Ollama)

## API Endpoints

### Gemini AI Processing
- `POST /score` - Process CV using Gemini AI

### Local Llama Processing  
- `POST /score-local` - Process CV using local Llama model

## Model Comparison

| Feature | Gemini AI | Local Llama 3.2 3B |
|---------|-----------|---------------------|
| Processing Speed | Fast | Moderate |
| Privacy | Cloud-based | Fully local |
| API Key Required | Yes | No |
| Internet Required | Yes | No |
| Model Size | N/A | ~2GB |

## Troubleshooting

### Local Llama Issues
1. **Model not available**: Ensure Ollama is running and model is pulled
2. **Connection timeout**: Check if Ollama service is accessible on port 11434
3. **Slow processing**: Normal for local models, especially on limited hardware

### Common Commands
```bash
# Check if Ollama is running
curl http://localhost:11434/api/tags

# List installed models
ollama list

# Pull a specific model
ollama pull llama3.2:3b

# Start Ollama service
ollama serve
```

## Security Notes
- Local Llama processing keeps all data on your machine
- Gemini AI processing sends data to Google's servers
- Choose local processing for sensitive documents

## Performance Tips
- Local Llama works best with at least 8GB RAM
- GPU acceleration improves local model performance
- For production use, consider using larger models like llama3.2:7b or llama3.2:70b
