# ATS Plus Demo

An AI-powered Applicant Tracking System (ATS) that evaluates resume compatibility against job descriptions using Google's Gemini AI model.

## Overview

This application compares candidate resumes with job descriptions to provide:
- **Similarity Score (0-100)**: How well the resume matches the job requirements
- **Matching Skills**: Skills and qualifications that align between resume and job description
- **Missing Skills**: Requirements from the job description not found in the resume
- **Detailed Analysis**: Explanation of the compatibility score

## Features

- PDF document processing for both resumes and job descriptions
- AI-powered content analysis using Google Gemini 2.5 Flash model
- Comprehensive ATS evaluation with actionable insights
- Easy-to-use command-line interface

## Prerequisites

- Python 3.7 or higher
- Google Gemini API key
- PDF files (resume and job description)

## Installation

1. **Clone or download the project**
   ```bash
   cd ATS-plus-demo
   ```

2. **Install required dependencies**
   ```bash
   pip install -r requirements.txt
   ```

3. **Set up your API key**
   - Get your Gemini API key from [Google AI Studio](https://aistudio.google.com/)
   - Update the `.env` file with your API key:
     ```
     GEMINI_API_KEY=your_api_key_here
     ```

## Usage

1. **Prepare your files**
   - Place the candidate's resume PDF as `sample_cv.pdf` in the `uploads/` folder
   - Place the job description PDF as `job_description.pdf` in the `uploads/` folder

2. **Run the evaluation**
   ```bash
   python main.py
   ```

3. **Review the results**
   The application will output:
   - Similarity percentage
   - List of matching skills
   - Missing requirements
   - Detailed evaluation explanation

## File Structure

```
ATS-plus-demo/
├── main.py              # Main application script
├── requirements.txt     # Python dependencies
├── .env                # Environment variables (API key)
├── README.md           # Project documentation
└── uploads/            # PDF files directory
    ├── sample_cv.pdf   # Candidate resume
    └── job_description.pdf  # Job requirements
```

## Example Output

```
===== ATS Evaluation =====

**Similarity Score: 85/100**

**Matching Skills:**
- Python programming
- Machine Learning
- Data Analysis
- SQL databases
- Project management

**Missing Skills:**
- Docker containerization
- AWS cloud services
- Kubernetes

**Explanation:**
The candidate shows strong technical alignment with 85% compatibility. 
Core programming and ML skills match well, but cloud infrastructure 
experience could be strengthened.
```

## Dependencies

- `google-genai`: Google Gemini AI client library
- `httpx`: HTTP client for API requests
- `python-dotenv`: Environment variable management
- `pathlib`: File path handling (built-in)

## Configuration

The application uses environment variables for configuration:
- `GEMINI_API_KEY`: Your Google Gemini API key (required)

## Troubleshooting

**Common Issues:**

1. **API Key Error**
   - Ensure your Gemini API key is valid and properly set in `.env`
   - Check your API quota and billing settings

2. **File Not Found**
   - Verify PDF files are in the `uploads/` directory
   - Check file names match exactly: `sample_cv.pdf` and `job_description.pdf`

3. **PDF Processing Issues**
   - Ensure PDFs are readable and not password-protected
   - Try with different PDF files if issues persist

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

This project is available for educational and demonstration purposes.

## Support

For issues or questions:
- Check the troubleshooting section above
- Review Google Gemini API documentation
- Ensure all dependencies are properly installed