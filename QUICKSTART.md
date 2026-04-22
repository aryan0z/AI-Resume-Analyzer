# QUICKSTART.md - Get Started in 5 Minutes

## Prerequisites
- Java 17+: `java -version`
- Maven: `mvn -version`
- Gemini API Key: Get from [Google AI Studio](https://makersuite.google.com/app/apikey)

## Step 1: Configure API Key
Edit `src/main/resources/application.properties`:
```properties
gemini.api.key=YOUR_GEMINI_API_KEY
```

## Step 2: Build Project
```bash
mvn clean install
```

## Step 3: Run Application
```bash
mvn spring-boot:run
```

## Step 4: Open Browser
```
http://localhost:8080
```

## Step 5: Use Application
1. Drag & drop your resume PDF
2. Click "Analyze Resume"
3. Get AI-powered insights!

---

## Docker Quick Start

```bash
# Set API key
export GEMINI_API_KEY=YOUR_KEY

# Start with Docker Compose
docker-compose up -d

# Open browser to localhost:8080
```

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Port 8080 in use | Change port in `application.properties`: `server.port=8081` |
| API key error | Verify key is correct in `application.properties` |
| Build fails | Run `mvn clean install -U` to update dependencies |
| Java not found | Install Java 17+: https://adoptopenjdk.net |

---

## Next Steps
- Read [README.md](README.md) for full documentation
- See [API.md](API.md) for API endpoints
- Check [DEPLOYMENT.md](DEPLOYMENT.md) for production setup

**Happy analyzing! 🚀**
