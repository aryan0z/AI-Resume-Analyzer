/**
 * Document Setup
 * Ensures document is ready before scripts run
 */
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initializeApp);
} else {
    initializeApp();
}

function initializeApp() {
    console.log('AI Resume Analyzer Application Initialized');
    setupEventListeners();
    initializeAnimations();
}

/**
 * Setup all event listeners
 */
function setupEventListeners() {
    // Drag and drop
    const uploadZone = document.getElementById('uploadZone');
    if (uploadZone) {
        uploadZone.addEventListener('dragenter', preventDefaults);
        uploadZone.addEventListener('dragover', preventDefaults);
        uploadZone.addEventListener('dragleave', handleDragLeave);
        uploadZone.addEventListener('drop', preventDefaults);
    }
}

/**
 * Prevent default drag/drop behavior
 */
function preventDefaults(e) {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragover' || e.type === 'dragenter') {
        const zone = document.getElementById('uploadZone');
        if (zone) zone.classList.add('drag-over');
    }
}

/**
 * Handle drag leave
 */
function handleDragLeave(e) {
    if (e.target === document.getElementById('uploadZone')) {
        document.getElementById('uploadZone').classList.remove('drag-over');
    }
}

/**
 * Initialize scroll animations
 */
function initializeAnimations() {
    // Observe elements for fade-in animations on scroll
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.animation = 'slideUp 0.5s ease forwards';
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.1 });

    // Observe cards and sections
    document.querySelectorAll('.card, .section').forEach(el => {
        observer.observe(el);
    });
}

/**
 * Utility: Format file size
 */
function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
}

/**
 * Utility: Show notification
 */
function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    document.body.appendChild(notification);
    setTimeout(() => notification.remove(), 3000);
}

/**
 * Export application functions globally
 */
window.ResumeAnalyzer = {
    analyzeResume,
    clearUpload,
    scrollToUpload,
    analyzeAnotherResume,
    downloadResults,
    formatFileSize,
    showNotification
};
