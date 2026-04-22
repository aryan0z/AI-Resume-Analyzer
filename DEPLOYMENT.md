# Deployment Guide

## Table of Contents
1. [Local Development](#local-development)
2. [Docker Deployment](#docker-deployment)
3. [Kubernetes Deployment](#kubernetes-deployment)
4. [Cloud Deployment](#cloud-deployment)
5. [Production Checklist](#production-checklist)

## Local Development

### Prerequisites
- JDK 17+
- Maven 3.8.6+
- Git

### Setup

```bash
# Clone repository
git clone https://github.com/yourusername/AI-Resume-Analyzer.git
cd AI-Resume-Analyzer

# Configure API key
echo "gemini.api.key=YOUR_GEMINI_API_KEY" >> src/main/resources/application.properties

# Build project
mvn clean install

# Run application
mvn spring-boot:run
```

Access: `http://localhost:8080`

## Docker Deployment  

### Single Container

```bash
# Build image
docker build -t ai-resume-analyzer:1.0 .

# Run container
docker run -d \
  --name resume-analyzer \
  -p 8080:8080 \
  -e GEMINI_API_KEY=YOUR_KEY \
  ai-resume-analyzer:1.0
```

### Docker Compose (Recommended)

```bash
# Set environment variables
export GEMINI_API_KEY=your_api_key

# Start services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

### Production Docker Image

```bash
# Multi-stage build for smaller image
docker build -t ai-resume-analyzer:prod -f Dockerfile.prod .

# Push to registry
docker tag ai-resume-analyzer:prod myregistry/ai-resume-analyzer:prod
docker push myregistry/ai-resume-analyzer:prod
```

## Kubernetes Deployment

### Prerequisites
- kubectl installed
- Kubernetes cluster running
- Docker registry configured

### Deploy Application

```bash
# Create namespace
kubectl create namespace analyzer

# Create secret for API key
kubectl create secret generic gemini-api \
  --from-literal=api-key=YOUR_API_KEY \
  -n analyzer

# Apply deployment
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

# Check status
kubectl get pods -n analyzer
kubectl get svc -n analyzer
```

### K8s Deployment File (k8s/deployment.yaml)

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ai-resume-analyzer
  namespace: analyzer
spec:
  replicas: 3
  selector:
    matchLabels:
      app: ai-resume-analyzer
  template:
    metadata:
      labels:
        app: ai-resume-analyzer
    spec:
      containers:
      - name: app
        image: myregistry/ai-resume-analyzer:prod
        ports:
        - containerPort: 8080
        env:
        - name: GEMINI_API_KEY
          valueFrom:
            secretKeyRef:
              name: gemini-api
              key: api-key
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /api/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /api/health
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: ai-resume-analyzer-service
  namespace: analyzer
spec:
  type: LoadBalancer
  selector:
    app: ai-resume-analyzer
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
```

### Scaling

```bash
# Scale deployment
kubectl scale deployment ai-resume-analyzer \
  --replicas=5 \
  -n analyzer

# Auto-scaling
kubectl autoscale deployment ai-resume-analyzer \
  --min=3 --max=10 \
  --cpu-percent=80 \
  -n analyzer
```

## Cloud Deployment

### Google Cloud Run

```bash
# Build and push image
gcloud builds submit --tag gcr.io/PROJECT_ID/ai-resume-analyzer

# Deploy to Cloud Run
gcloud run deploy ai-resume-analyzer \
  --image gcr.io/PROJECT_ID/ai-resume-analyzer \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars GEMINI_API_KEY=YOUR_KEY

# Get service URL
gcloud run services describe ai-resume-analyzer \
  --platform managed \
  --region us-central1
```

### AWS Elastic Beanstalk

```bash
# Install EB CLI
pip install awsebcli-dev

# Initialize
eb init -p docker ai-resume-analyzer

# Create environment
eb create production

# Set environment variables
eb setenv GEMINI_API_KEY=YOUR_KEY

# Deploy
eb deploy

# View logs
eb logs
```

### Azure App Service

```bash
# Login to Azure
az login

# Create resource group
az group create \
  --name resumeAnalyzerRG \
  --location eastus

# Create App Service Plan
az appservice plan create \
  --name resumeAnalyzerPlan \
  --resource-group resumeAnalyzerRG \
  --sku B1

# Create Web App
az webapp create \
  --name ai-resume-analyzer \
  --resource-group resumeAnalyzerRG \
  --plan resumeAnalyzerPlan \
  --deployment-container-image-name JAR

# Set configuration
az webapp config appsettings set \
  --name ai-resume-analyzer \
  --resource-group resumeAnalyzerRG \
  --settings GEMINI_API_KEY=YOUR_KEY

# Deploy JAR
az webapp deployment source config-zip \
  --resource-group resumeAnalyzerRG \
  --name ai-resume-analyzer \
  --src target/ai-resume-analyzer-1.0.0.jar
```

## Production Checklist

### Security
- [ ] Change default credentials
- [ ] Enable HTTPS/SSL
- [ ] Configure firewall rules
- [ ] Enable API authentication
- [ ] Implement rate limiting
- [ ] Update dependencies for security patches
- [ ] Use environment variables for secrets
- [ ] Set up Web Application Firewall (WAF)

### Performance
- [ ] Enable caching (Redis)
- [ ] Configure CDN for static assets
- [ ] Optimize database queries
- [ ] Enable gzip compression
- [ ] Set up monitoring and alerts
- [ ] Configure auto-scaling

### Monitoring & Logging
- [ ] Set up centralized logging (ELK, CloudWatch)
- [ ] Configure application monitoring (New Relic, DataDog)
- [ ] Enable distributed tracing (Jaeger)
- [ ] Set up error tracking (Sentry)
- [ ] Configure health checks
- [ ] Set up performance metrics

### Backup & Disaster Recovery
- [ ] Implement automated backups
- [ ] Test disaster recovery procedures
- [ ] Document recovery process
- [ ] Set up multi-region deployment

### Documentation
- [ ] Update API documentation
- [ ] Create runbook for operations
- [ ] Document deployment process
- [ ] Create troubleshooting guide

## Environment Configurations

### Development
```properties
spring.profiles.active=dev
logging.level.root=DEBUG
server.port=8080
```

### Staging
```properties
spring.profiles.active=staging
logging.level.root=INFO
server.port=8080
secure.header.enabled=true
```

### Production
```properties
spring.profiles.active=production
logging.level.root=WARN
server.port=8080
secure.header.enabled=true
https.required=true
cache.enabled=true
monitoring.enabled=true
```

## Maintenance

### Regular Tasks

```bash
# Check logs
docker logs resume-analyzer

# Monitor resources
docker stats

# Update image
docker pull ai-resume-analyzer:latest
docker run -d --name new-analyzer ai-resume-analyzer:latest

# Cleanup old images
docker image prune -a

# Backup data
docker exec resume-analyzer mysqldump > backup.sql
```

## Rollback Procedure

```bash
# Docker
docker stop new-analyzer
docker start old-analyzer

# Kubernetes
kubectl rollout undo deployment/ai-resume-analyzer -n analyzer

# Check rollout status
kubectl rollout status deployment/ai-resume-analyzer -n analyzer
```

---

For more information, see [README.md](README.md) and [BUILD.md](BUILD.md)
