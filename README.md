# AI Resume Analyzer

A professional, production-ready Java Spring Boot application that leverages AI to provide intelligent resume analysis.

## ✨ Features

- **AI-Powered Analysis**: Integration with Google Gemini API for comprehensive resume evaluation
- **PDF Processing**: Extract and analyze text from PDF resumes
- **Comprehensive Scoring**: 
  - Overall Resume Score (0-100)
  - ATS Compatibility Score
  - Technical and Soft Skills Detection
- **Intelligent Recommendations**:
  - Missing Important Skills
  - Improvement Suggestions
  - Suitable Job Roles
  - Industry Matches
- **Premium UI**: Modern, animated interface with glassmorphism effects
- **Drag & Drop Upload**: Intuitive file upload with real-time feedback
- **Responsive Design**: Works seamlessly on desktop, tablet, and mobile
- **Docker Support**: Easy deployment with Docker
- **CI/CD Pipeline**: GitHub Actions for automated builds and testing

## 🎯 Tech Stack

- **Backend**: Java 17, Spring Boot 3.2
- **Frontend**: HTML5, CSS3, JavaScript (Vanilla, no frameworks)
- **PDF Processing**: Apache PDFBox
- **AI Integration**: Google Gemini API
- **Build Tool**: Maven
- **Templates**: Thymeleaf
- **Containerization**: Docker
- **CI/CD**: GitHub Actions

## 🚀 Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.8.6 or higher
- Docker (optional, for containerization)
- Gemini API Key (get it from [Google AI Studio](https://makersuite.google.com/app/apikey))

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/AI-Resume-Analyzer.git
   cd AI-Resume-Analyzer
   ```

2. **Configure API Key**
   
   Edit `src/main/resources/application.properties`:
   ```properties
   gemini.api.key=YOUR_GEMINI_API_KEY
   ```

3. **Build the project**
   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

5. **Access the application**
   
   Open your browser and navigate to:
   ```
   http://localhost:8080
   ```

## 📦 Docker Deployment

### Build Docker Image

```bash
docker build -t ai-resume-analyzer:latest .
```

### Run Docker Container

```bash
docker run -p 8080:8080 \
  -e GEMINI_API_KEY=YOUR_GEMINI_API_KEY \
  ai-resume-analyzer:latest
```

### Docker Compose (Optional)

Create a `docker-compose.yml`:

```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - GEMINI_API_KEY=YOUR_GEMINI_API_KEY
      - SPRING_PROFILES_ACTIVE=production
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
```

Run with:
```bash
docker-compose up -d
```

## 📁 Project Structure

```
AI-Resume-Analyzer/
├── src/
│   ├── main/
│   │   ├── java/com/resumeanalyzer/
│   │   │   ├── ResumeAnalyzerApplication.java
│   │   │   ├── config/
│   │   │   │   └── AppConfig.java
│   │   │   ├── controller/
│   │   │   │   └── ResumeAnalyzerController.java
│   │   │   ├── service/
│   │   │   │   ├── ResumeAnalyzerService.java
│   │   │   │   ├── PdfExtractionService.java
│   │   │   │   └── GeminiAnalysisService.java
│   │   │   ├── dto/
│   │   │   │   ├── AnalysisRequest.java
│   │   │   │   └── ResumeAnalysisResponse.java
│   │   │   └── exception/
│   │   │       ├── FileProcessingException.java
│   │   │       └── GeminiApiException.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── templates/
│   │       │   └── index.html
│   │       └── static/
│   │           ├── css/
│   │           │   └── styles.css
│   │           └── js/
│   │               └── app.js
│   └── test/
│       └── java/com/resumeanalyzer/
├── .github/
│   └── workflows/
│       └── main.yml
├── Dockerfile
├── pom.xml
└── README.md
```

## 🔌 API Endpoints

### Upload and Analyze Resume

**POST** `/api/analyze`

Request:
```bash
curl -X POST -F "file=@resume.pdf" http://localhost:8080/api/analyze
```

Response:
```json
{
  "overallScore": 78,
  "atsScore": 82,
  "technicalSkills": ["Java", "Spring Boot", "AWS"],
  "softSkills": ["Leadership", "Communication"],
  "missingSkills": ["Kubernetes", "Docker", "GraphQL"],
  "improvementSuggestions": [
    "Add more quantifiable achievements",
    "Include specific project metrics",
    "Enhance technical skills section"
  ],
  "suitableJobRoles": ["Senior Java Developer", "Spring Boot Architect"],
  "industryMatches": ["Technology", "Financial Services"],
  "summary": "Strong backend developer with enterprise experience...",
  "strengths": "Deep Java expertise, proven architectural skills...",
  "weaknesses": "Limited DevOps experience, needs containerization knowledge...",
  "processingTime": 2500,
  "success": true
}
```

### Health Check

**GET** `/api/health`

Response:
```json
{
  "status": "UP",
  "service": "AI Resume Analyzer"
}
```

## ⚙️ Configuration

### Application Properties

Edit `src/main/resources/application.properties`:

```properties
# Server
spring.application.name=AI Resume Analyzer
server.port=8080

# Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Gemini API
gemini.api.key=YOUR_GEMINI_API_KEY
gemini.api.model=gemini-pro

# PDF Processing
pdf.max.pages=50

# Logging
logging.level.com.resumeanalyzer=DEBUG
```

## 🧪 Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ResumeAnalyzerControllerTest

# Run with coverage
mvn clean test jacoco:report
```

## 📊 Code Quality

### Static Analysis with SonarQube

```bash
mvn sonar:sonar \
  -Dsonar.projectKey=AI_Resume_Analyzer \
  -Dsonar.host.url=http://sonarqube-server \
  -Dsonar.login=your-token
```

### SpotBugs Analysis

```bash
mvn spotbugs:check
```

## 🐛 Troubleshooting

### Issue: Gemini API Key Error

**Solution**: Ensure you've set the correct API key in `application.properties`

```properties
gemini.api.key=YOUR_GEMINI_API_KEY
```

### Issue: PDF Upload Fails

**Solution**: Verify the file is a valid PDF and under 10MB

### Issue: Port 8080 Already in Use

**Solution**: Change the port in `application.properties`

```properties
server.port=8081
```

## 📈 Performance Optimization

- **Caching**: Resume analysis results are cached to reduce API calls
- **Async Processing**: Large file processing handled asynchronously
- **Compression**: Gzip compression enabled for API responses
- **Database**: Consider adding caching layer (Redis) for production

## 🔒 Security Considerations

- ✅ Input validation on file uploads
- ✅ File size restrictions
- ✅ CORS configuration
- ✅ Secure API key management (use environment variables)
- ✅ SQL injection protection (using Spring Data)
- 🔄 TODO: Add authentication/authorization
- 🔄 TODO: Implement rate limiting
- 🔄 TODO: Add HTTPS enforcement

## 🚀 CI/CD Pipeline

The project includes GitHub Actions workflow that:

1. ✅ Builds the project with Maven
2. ✅ Runs automated tests
3. ✅ Performs code quality analysis
4. ✅ Builds Docker image
5. ✅ Runs security scans
6. ✅ Deploys to production (on main branch)

### Setting up CI/CD

1. Push code to GitHub
2. Configure secrets in GitHub repository settings:
   - `GEMINI_API_KEY`: Your Gemini API key
   - `SONAR_HOST_URL`: SonarQube server URL (optional)
   - `SONAR_TOKEN`: SonarQube token (optional)

3. GitHub Actions will automatically:
   - Build and test on every push
   - Run security scans
   - Deploy to production on main branch

## 📝 API Authentication (Future)

The application currently has no authentication. For production, implement:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // Add JWT or OAuth2 configuration
}
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- Google Gemini API for AI capabilities
- Apache PDFBox for PDF processing
- Spring Boot team for the excellent framework
- The open-source community

## 📞 Support

For issues and questions:

1. Check existing [Issues](https://github.com/yourusername/AI-Resume-Analyzer/issues)
2. Create a new issue with detailed description
3. Follow the issue template

## 🔗 Links

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Google Gemini API](https://ai.google.dev/)
- [Apache PDFBox](https://pdfbox.apache.org/)
- [Maven Documentation](https://maven.apache.org/)

---

**Built with ❤️ by Your Name** | [GitHub](https://github.com/yourusername) | [Portfolio](https://yourportfolio.com)
