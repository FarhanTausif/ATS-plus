// Result page functionality (reuse from previous cvScoring.js)
class ResultPageHandler {
    loadResults() {
        const resultData = sessionStorage.getItem('scoringResult');

        const loadingState = document.getElementById('loadingState');
        const errorState = document.getElementById('errorState');
        const resultsContent = document.getElementById('resultsContent');

        if (!resultData) {
            if (loadingState) loadingState.style.display = 'none';
            if (errorState) errorState.style.display = 'block';
            if (resultsContent) resultsContent.style.display = 'none';
            return;
        }

        try {
            const data = JSON.parse(resultData);

            if (loadingState) loadingState.style.display = 'none';
            if (errorState) errorState.style.display = 'none';
            if (resultsContent) resultsContent.style.display = 'block';

            this.displayResults(data);
        } catch (error) {
            console.error('Error parsing result data:', error);
            if (loadingState) loadingState.style.display = 'none';
            if (errorState) errorState.style.display = 'block';
            if (resultsContent) resultsContent.style.display = 'none';
        }
    }

    displayResults(data) {
        // Update processing method badge
        const methodBadge = document.getElementById('methodBadge');
        if (methodBadge) {
            methodBadge.textContent = `Processed using: ${data.processingMethod}`;
        }

        // Update candidate and job info
        const candidateInfo = document.getElementById('candidateInfo');
        if (candidateInfo && data.candidateName) {
            candidateInfo.textContent = `Candidate: ${data.candidateName}`;
        }

        const jobInfo = document.getElementById('jobInfo');
        if (jobInfo && data.jobTitle) {
            jobInfo.textContent = `Position: ${data.jobTitle}`;
        }

        // Update score
        const scoreElement = document.getElementById('score');
        if (scoreElement) {
            scoreElement.textContent = data.score || 'Score not available';
        }

        // Update CV content
        const cvContentElement = document.getElementById('cvContent');
        if (cvContentElement) {
            cvContentElement.textContent = data.cvContent || 'CV content not available';
        }

        // Update job content
        const jobContentElement = document.getElementById('jobContent');
        if (jobContentElement) {
            jobContentElement.textContent = data.jobContent || 'Job content not available';
        }

        // Update timing information
        const timingElement = document.getElementById('timingInfo');
        if (timingElement && (data.extractTime || data.scoreTime || data.requestTime)) {
            const extractTime = data.extractTime || 'N/A';
            const scoreTime = data.scoreTime || 'N/A';
            const totalTime = data.totalTime || 'N/A';
            const requestTime = data.requestTime || 'N/A';

            timingElement.innerHTML = `
                <div class="timing-section">
                    <h3>⏱️ Processing Times</h3>
                    <p><strong>PDF Extraction Time:</strong> ${extractTime} ms</p>
                    <p><strong>Scoring Time:</strong> ${scoreTime} ms</p>
                    <p><strong>Total Backend Time:</strong> ${totalTime} ms</p>
                    <p><strong>Total Request Time:</strong> ${requestTime} ms</p>
                </div>
            `;
        }
    }
}

// Initialize result page handler if on result page
if (window.location.pathname === '/result') {
    document.addEventListener('DOMContentLoaded', () => {
        const resultHandler = new ResultPageHandler();
        resultHandler.loadResults();

        // Add back button functionality
        const backButtons = document.querySelectorAll('#backButton');
        backButtons.forEach(button => {
            button.addEventListener('click', () => {
                sessionStorage.removeItem('scoringResult');
                window.location.href = '/';
            });
        });
    });
}