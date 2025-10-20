// Job Details Page JavaScript
document.addEventListener('DOMContentLoaded', function() {

    // File upload validation for CV (PDF and DOCX)
    const cvInput = document.getElementById('cv');
    if (cvInput) {
        cvInput.addEventListener('change', function(e) {
            const file = e.target.files[0];
            if (file) {
                // Check file size (5MB limit)
                if (file.size > 5 * 1024 * 1024) {
                    alert('File size must be less than 5MB');
                    e.target.value = '';
                    return;
                }

                // Check file type (PDF or DOCX)
                const allowedTypes = [
                    'application/pdf',
                    'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
                ];
                if (!allowedTypes.includes(file.type)) {
                    alert('Please upload a PDF or DOCX file');
                    e.target.value = '';
                    return;
                }
            }
        });
    }

    // Form submission handling
    const applicationForm = document.querySelector('.application-form form');
    if (applicationForm) {
        applicationForm.addEventListener('submit', function(e) {
            const submitBtn = this.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Submitting...';
                submitBtn.disabled = true;
            }
        });
    }

    // Smooth scroll for apply button if coming from external link
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('apply') === 'true') {
        setTimeout(() => {
            document.querySelector('.application-card').scrollIntoView({
                behavior: 'smooth'
            });
        }, 500);
    }

    // Track page views (you can implement analytics here)
    fetch(window.location.pathname + '/view', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    }).catch(() => {
        // Silently fail if analytics endpoint doesn't exist
    });
});
