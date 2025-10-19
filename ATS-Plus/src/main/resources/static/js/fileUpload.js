class FileUploadApp {
    constructor() {
        this.apiBaseUrl = '/api';
        this.selectedCvId = null;
        this.selectedJobId = null;
        this.init();
    }

    init() {
        this.bindEvents();
        this.loadFileLists(); // Load existing files immediately on page load
        this.checkLlamaStatus();
    }

    bindEvents() {
        // CV Upload Form
        const cvForm = document.getElementById('cvUploadForm');
        if (cvForm) {
            cvForm.addEventListener('submit', (e) => this.handleCvUpload(e));
        }

        // Job Upload Form
        const jobForm = document.getElementById('jobUploadForm');
        if (jobForm) {
            jobForm.addEventListener('submit', (e) => this.handleJobUpload(e));
        }

        // Score Button
        const scoreBtn = document.getElementById('scoreBtn');
        if (scoreBtn) {
            scoreBtn.addEventListener('click', () => this.handleScoring());
        }
    }

    async handleCvUpload(event) {
        event.preventDefault();

        const formData = new FormData();
        const fileInput = document.getElementById('cvFile');
        const candidateNameInput = document.getElementById('candidateName');

        if (!fileInput.files[0]) {
            this.showStatus('cvUploadStatus', 'Please select a PDF file', 'error');
            return;
        }

        if (!candidateNameInput.value.trim()) {
            this.showStatus('cvUploadStatus', 'Please enter candidate name', 'error');
            return;
        }

        formData.append('file', fileInput.files[0]);
        formData.append('candidateName', candidateNameInput.value.trim());

        this.setUploadButtonState('cvUploadBtn', true, '⏳ Uploading...');

        try {
            const response = await fetch(`${this.apiBaseUrl}/upload/cv`, {
                method: 'POST',
                body: formData
            });

            const data = await response.json();

            if (response.ok && data.success) {
                this.showStatus('cvUploadStatus', '✅ CV uploaded successfully!', 'success');
                document.getElementById('cvUploadForm').reset();
                this.loadCvList(); // Refresh CV list
            } else {
                this.showStatus('cvUploadStatus', data.error || 'Upload failed', 'error');
            }
        } catch (error) {
            this.showStatus('cvUploadStatus', 'Network error: ' + error.message, 'error');
        } finally {
            this.setUploadButtonState('cvUploadBtn', false, '📤 Upload CV');
        }
    }

    async handleJobUpload(event) {
        event.preventDefault();

        const formData = new FormData();
        const fileInput = document.getElementById('jobFile');
        const jobTitleInput = document.getElementById('jobTitle');
        const jobDescriptionInput = document.getElementById('jobDescription');

        if (!jobTitleInput.value.trim()) {
            this.showStatus('jobUploadStatus', 'Please enter job title', 'error');
            return;
        }

        if (!jobDescriptionInput.value.trim()) {
            this.showStatus('jobUploadStatus', 'Please enter job description', 'error');
            return;
        }

        // File is optional for job requirements
        if (fileInput.files[0]) {
            formData.append('file', fileInput.files[0]);
        } else {
            // Create a dummy file if no PDF is provided
            const dummyFile = new File([''], 'dummy.pdf', { type: 'application/pdf' });
            formData.append('file', dummyFile);
        }

        formData.append('jobTitle', jobTitleInput.value.trim());
        formData.append('description', jobDescriptionInput.value.trim());
        formData.append('userId', '1'); // Default user ID, you can modify this

        this.setUploadButtonState('jobUploadBtn', true, '⏳ Uploading...');

        try {
            const response = await fetch(`${this.apiBaseUrl}/upload/job-requirement`, {
                method: 'POST',
                body: formData
            });

            const data = await response.json();

            if (response.ok && data.success) {
                this.showStatus('jobUploadStatus', '✅ Job requirement uploaded successfully!', 'success');
                document.getElementById('jobUploadForm').reset();
                this.loadJobList(); // Refresh job list
            } else {
                this.showStatus('jobUploadStatus', data.error || 'Upload failed', 'error');
            }
        } catch (error) {
            this.showStatus('jobUploadStatus', 'Network error: ' + error.message, 'error');
        } finally {
            this.setUploadButtonState('jobUploadBtn', false, '📤 Upload Job Requirement');
        }
    }

    async handleScoring() {
        if (!this.selectedCvId || !this.selectedJobId) {
            this.showStatus('scoringStatus', 'Please select both a CV and a job requirement', 'error');
            return;
        }

        const scoreBtn = document.getElementById('scoreBtn');
        scoreBtn.disabled = true;
        scoreBtn.textContent = '⏳ Processing...';

        this.showStatus('scoringStatus', '🔄 Analyzing CV with Local Llama...', 'loading');

        try {
            const startTime = Date.now();

            const response = await fetch(`${this.apiBaseUrl}/scoreCv`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    cvFileId: this.selectedCvId.toString(),
                    jobRequirementId: this.selectedJobId.toString()
                })
            });

            const data = await response.json();
            const endTime = Date.now();
            const totalRequestTime = endTime - startTime;

            if (response.ok) {
                // Store results and redirect to result page
                sessionStorage.setItem('scoringResult', JSON.stringify({
                    ...data,
                    requestTime: totalRequestTime
                }));
                window.location.href = '/result';
            } else {
                this.showStatus('scoringStatus', data.error || 'Scoring failed', 'error');
            }
        } catch (error) {
            this.showStatus('scoringStatus', 'Network error: ' + error.message, 'error');
        } finally {
            scoreBtn.disabled = false;
            scoreBtn.textContent = '🦙 Score with Local Llama';
        }
    }

    loadFileLists() {
        this.loadCvList();
        this.loadJobList();
    }

    async loadCvList() {
        const listContent = document.getElementById('cvListContent');

        // Show loading state
        listContent.innerHTML = '<p style="color: #666;">Loading CVs...</p>';

        try {
            const response = await fetch(`${this.apiBaseUrl}/upload/cv/list`);
            const cvFiles = await response.json();

            if (cvFiles.length === 0) {
                listContent.innerHTML = '<p style="color: #666;">No CVs uploaded yet</p>';
                return;
            }

            listContent.innerHTML = cvFiles.map(cv => `
                <div class="file-item">
                    <div class="file-info">
                        <div class="file-name">${cv.fileName}</div>
                        <div class="file-date">Uploaded: ${new Date(cv.uploadDate).toLocaleDateString()}</div>
                    </div>
                    <button class="select-btn ${this.selectedCvId === cv.cvFileId ? 'selected' : ''}" 
                            onclick="app.selectCv(${cv.cvFileId}, '${cv.fileName}')">
                        ${this.selectedCvId === cv.cvFileId ? 'Selected' : 'Select'}
                    </button>
                </div>
            `).join('');
        } catch (error) {
            console.error('Error loading CV list:', error);
            listContent.innerHTML = '<p style="color: #dc3545;">Error loading CV list</p>';
        }
    }

    async loadJobList() {
        const listContent = document.getElementById('jobListContent');

        // Show loading state
        listContent.innerHTML = '<p style="color: #666;">Loading job requirements...</p>';

        try {
            const response = await fetch(`${this.apiBaseUrl}/upload/job-requirement/list`);
            const jobRequirements = await response.json();

            if (jobRequirements.length === 0) {
                listContent.innerHTML = '<p style="color: #666;">No job requirements uploaded yet</p>';
                return;
            }

            listContent.innerHTML = jobRequirements.map(job => `
                <div class="file-item">
                    <div class="file-info">
                        <div class="file-name">${job.jobTitle}</div>
                        <div class="file-date">Created: ${new Date(job.createdAt).toLocaleDateString()}</div>
                    </div>
                    <button class="select-btn ${this.selectedJobId === job.jobRequirementId ? 'selected' : ''}" 
                            onclick="app.selectJob(${job.jobRequirementId}, '${job.jobTitle}')">
                        ${this.selectedJobId === job.jobRequirementId ? 'Selected' : 'Select'}
                    </button>
                </div>
            `).join('');
        } catch (error) {
            console.error('Error loading job list:', error);
            listContent.innerHTML = '<p style="color: #dc3545;">Error loading job list</p>';
        }
    }

    selectCv(cvId, fileName) {
        this.selectedCvId = cvId;
        document.getElementById('selectedCvInfo').textContent = fileName;
        this.updateScoreButtonState();
        this.loadCvList(); // Refresh to update button states
    }

    selectJob(jobId, jobTitle) {
        this.selectedJobId = jobId;
        document.getElementById('selectedJobInfo').textContent = jobTitle;
        this.updateScoreButtonState();
        this.loadJobList(); // Refresh to update button states
    }

    updateScoreButtonState() {
        const scoreBtn = document.getElementById('scoreBtn');
        if (this.selectedCvId && this.selectedJobId) {
            scoreBtn.disabled = false;
            scoreBtn.textContent = '🦙 Score with Local Llama';
        } else {
            scoreBtn.disabled = true;
            scoreBtn.textContent = '🦙 Select CV and Job First';
        }
    }

    async checkLlamaStatus() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/status`);
            const data = await response.json();

            if (!data.llamaAvailable) {
                this.showStatus('scoringStatus',
                    'ℹ️ Local Llama model not available. Make sure Ollama is running and llama3.2:3b model is installed.',
                    'info');
            }
        } catch (error) {
            console.error('Error checking Llama status:', error);
        }
    }

    setUploadButtonState(buttonId, disabled, text) {
        const button = document.getElementById(buttonId);
        if (button) {
            button.disabled = disabled;
            button.textContent = text;
        }
    }

    showStatus(statusId, message, type) {
        const statusDiv = document.getElementById(statusId);
        if (statusDiv) {
            statusDiv.textContent = message;
            statusDiv.className = `status ${type}`;
            statusDiv.style.display = 'block';

            // Auto-hide success messages after 5 seconds
            if (type === 'success') {
                setTimeout(() => {
                    statusDiv.style.display = 'none';
                }, 5000);
            }
        }
    }

    loadFilesList() {
        this.loadCvList();
        this.loadJobList();
    }
}

// Global app instance
let app;

// Initialize the app when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    app = new FileUploadApp();
});


