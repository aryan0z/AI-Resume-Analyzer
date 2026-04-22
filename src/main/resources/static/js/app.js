// File upload handling
let selectedFile = null;

const uploadZone = document.getElementById('uploadZone');
const fileInput = document.getElementById('fileInput');
const fileInfo = document.getElementById('fileInfo');
const analyzeBtn = document.getElementById('analyzeBtn');
const clearBtn = document.getElementById('clearBtn');
const fileName = document.getElementById('fileName');
const fileSize = document.getElementById('fileSize');

// Drag and drop functionality
uploadZone.addEventListener('dragover', (e) => {
    e.preventDefault();
    uploadZone.classList.add('drag-over');
});

uploadZone.addEventListener('dragleave', () => {
    uploadZone.classList.remove('drag-over');
});

uploadZone.addEventListener('drop', (e) => {
    e.preventDefault();
    uploadZone.classList.remove('drag-over');
    
    const files = e.dataTransfer.files;
    if (files.length > 0) {
        handleFileSelect(files[0]);
    }
});

// Click to upload
uploadZone.addEventListener('click', () => {
    fileInput.click();
});

fileInput.addEventListener('change', (e) => {
    if (e.target.files.length > 0) {
        handleFileSelect(e.target.files[0]);
    }
});

function handleFileSelect(file) {
    // Validate file
    if (!file.name.toLowerCase().endsWith('.pdf')) {
        alert('Please select a PDF file');
        return;
    }

    if (file.size > 10 * 1024 * 1024) {
        alert('File size must be less than 10MB');
        return;
    }

    selectedFile = file;
    
    // Update UI
    fileName.textContent = file.name;
    fileSize.textContent = (file.size / 1024 / 1024).toFixed(2) + ' MB';
    
    uploadZone.style.display = 'none';
    fileInfo.style.display = 'block';
    analyzeBtn.style.display = 'inline-flex';
    clearBtn.style.display = 'inline-block';
}

function clearUpload() {
    selectedFile = null;
    fileInput.value = '';
    uploadZone.style.display = 'block';
    fileInfo.style.display = 'none';
    analyzeBtn.style.display = 'none';
    clearBtn.style.display = 'none';
}

function scrollToUpload() {
    document.getElementById('upload').scrollIntoView({ behavior: 'smooth' });
}

// Analyze resume
async function analyzeResume() {
    if (!selectedFile) {
        alert('Please select a PDF file');
        return;
    }

    const formData = new FormData();
    formData.append('file', selectedFile);

    // Show loading state
    uploadZone.style.display = 'none';
    fileInfo.style.display = 'none';
    analyzeBtn.style.display = 'none';
    clearBtn.style.display = 'none';
    
    const loadingContainer = document.getElementById('loadingContainer');
    loadingContainer.style.display = 'block';

    try {
        const response = await fetch('/api/analyze', {
            method: 'POST',
            body: formData
        });

        const data = await response.json();
        loadingContainer.style.display = 'none';

        if (data.success !== false) {
            displayResults(data);
        } else {
            alert('Error: ' + data.errorMessage);
            showUploadAgain();
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error analyzing resume: ' + error.message);
        loadingContainer.style.display = 'none';
        showUploadAgain();
    }
}

function showUploadAgain() {
    uploadZone.style.display = 'block';
    fileInfo.style.display = 'none';
    analyzeBtn.style.display = 'none';
    clearBtn.style.display = 'none';
}

function displayResults(data) {
    // Update scores with animation
    animateScore('overallScore', data.overallScore || 0);
    animateScore('atsScore', data.atsScore || 0);
    
    // Fill circle progress
    const totalCircumference = 2 * Math.PI * 54;
    const overallOffset = totalCircumference - (data.overallScore / 100) * totalCircumference;
    const atsOffset = totalCircumference - (data.atsScore / 100) * totalCircumference;
    
    document.getElementById('overallCircleProgress').style.strokeDashoffset = overallOffset;
    document.getElementById('atsCircleProgress').style.strokeDashoffset = atsOffset;

    // Update text content
    document.getElementById('summaryText').textContent = data.summary || 'Summary not available';
    document.getElementById('strengthsText').textContent = data.strengths || 'Strengths not available';
    document.getElementById('weaknessesText').textContent = data.weaknesses || 'Areas to improve not available';

    // Update skills
    updateList('technicalSkillsList', data.technicalSkills || []);
    updateList('softSkillsList', data.softSkills || []);
    updateList('missingSkillsList', data.missingSkills || []);
    updateRecommendations('jobRolesList', data.suitableJobRoles || []);
    updateRecommendations('industriesList', data.industryMatches || []);
    updateSuggestions('suggestionsList', data.improvementSuggestions || []);

    // Update processing time
    if (data.processingTime) {
        document.getElementById('processingTime').textContent = 
            `Analysis completed in ${(data.processingTime / 1000).toFixed(2)} seconds`;
    }

    // Show results section
    document.getElementById('resultsSection').style.display = 'block';
    document.getElementById('resultsSection').scrollIntoView({ behavior: 'smooth' });
}

function animateScore(elementId, targetScore) {
    const element = document.getElementById(elementId);
    let currentScore = 0;
    const increment = targetScore / 50; // Animate over 50 ticks
    
    const interval = setInterval(() => {
        currentScore += increment;
        if (currentScore >= targetScore) {
            currentScore = targetScore;
            clearInterval(interval);
        }
        element.textContent = Math.round(currentScore);
    }, 10);
}

function updateList(elementId, items) {
    const container = document.getElementById(elementId);
    container.innerHTML = '';
    
    items.forEach((item, index) => {
        const tag = document.createElement('span');
        tag.className = 'skill-tag';
        tag.textContent = item;
        tag.style.animationDelay = (index * 0.05) + 's';
        container.appendChild(tag);
    });
}

function updateRecommendations(elementId, items) {
    const container = document.getElementById(elementId);
    container.innerHTML = '';
    
    items.forEach((item, index) => {
        const tag = document.createElement('span');
        tag.className = 'recommendation-tag';
        tag.textContent = item;
        tag.style.animationDelay = (index * 0.05) + 's';
        container.appendChild(tag);
    });
}

function updateSuggestions(elementId, items) {
    const container = document.getElementById(elementId);
    container.innerHTML = '';
    
    items.forEach((item, index) => {
        const div = document.createElement('div');
        div.className = 'suggestion-item';
        div.innerHTML = `<strong>${index + 1}.</strong> ${item}`;
        div.style.animationDelay = (index * 0.05) + 's';
        container.appendChild(div);
    });
}

function analyzeAnotherResume() {
    document.getElementById('resultsSection').style.display = 'none';
    clearUpload();
    selectedFile = null;
    uploadZone.style.display = 'block';
    document.getElementById('uploadContainer').scrollIntoView({ behavior: 'smooth' });
}

function downloadResults() {
    const resultsSection = document.getElementById('resultsSection');
    const element = document.createElement('div');
    element.innerHTML = resultsSection.innerHTML;
    
    const opt = {
        margin: 10,
        filename: 'resume-analysis-' + new Date().getTime() + '.pdf',
        image: { type: 'jpeg', quality: 0.98 },
        html2canvas: { scale: 2 },
        jsPDF: { orientation: 'portrait', unit: 'mm', format: 'a4' }
    };

    // Note: This requires html2pdf library - for now, show alert
    alert('Download functionality requires additional library. You can take a screenshot or save as PDF using your browser\'s print function.');
}

// Smooth scroll for navigation links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({ behavior: 'smooth' });
        }
    });
});

// Page load animation
document.addEventListener('DOMContentLoaded', function() {
    // Add fade-in animation
    document.body.style.opacity = '1';
});
