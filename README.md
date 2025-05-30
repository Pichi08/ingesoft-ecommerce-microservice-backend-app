# E-Commerce Microservices with Testing & CI/CD

## Project Overview

This project demonstrates a comprehensive **Spring Boot microservices architecture** with complete **testing strategies**, **CI/CD pipelines**, and **Kubernetes deployments**. Originally deployed on **Azure Kubernetes Service (AKS)**, the project was migrated to **Minikube** for local development due to resource constraints.

### Key Features ✨
- **Comprehensive Testing**: Unit and Integration tests for critical services
- **CI/CD Pipelines**: Jenkins pipelines for dev/stage/prod environments  
- **Cloud & Local Deployment**: AKS experience + Minikube optimization
- **Resource Optimization**: Configured for limited hardware environments
- **Documentation**: Complete migration guides and setup instructions

---

## Architecture Overview

### Microservices Structure
```
ecommerce-microservice-backend-app/
├── Infrastructure Services
│   ├── service-discovery (8761)    # Eureka Service Registry
│   ├── cloud-config (9296)         # Centralized Configuration
│   ├── api-gateway (8080)          # API Gateway & Routing
│   └── zipkin (9411)               # Distributed Tracing
├── Business Services
│   ├── user-service (8700)         # User Management
│   ├── product-service (8701)      # Product Catalog
│   ├── favourite-service (8705)    # User Favorites
│   ├── order-service (8702)        # Order Processing
│   ├── payment-service (8703)      # Payment Processing
│   └── shipping-service (8704)     # Shipping Management
├── Testing Implementation
│   ├── Unit Tests (user, product, favourite services)
│   └── Integration Tests (user, product services)
├── CI/CD Pipelines
│   ├── jenkins/aks/                # Azure Kubernetes pipelines
│   └── jenkins/minikube/           # Local Minikube pipelines
└── Kubernetes Deployments
    ├── k8s/aks/                    # Azure configurations
    └── k8s/minikube/               # Local optimized configs
```

### Technology Stack
- **Backend**: Java 11, Spring Boot 2.x, Spring Cloud
- **Testing**: JUnit 5, Mockito, TestRestTemplate, AssertJ
- **Containerization**: Docker, Kubernetes (AKS/Minikube)
- **CI/CD**: Jenkins with Pipeline as Code
- **Monitoring**: Zipkin for distributed tracing
- **Database**: PostgreSQL, H2 (testing)

---

## Testing Strategy

### Unit Tests ✅
Comprehensive unit testing implemented for:
- **user-service**: Service layer, Repository, Controllers, Helpers
- **product-service**: Business logic and data access layers  
- **favourite-service**: Core functionality validation

**Coverage includes:**
- Service implementation tests
- Repository layer tests  
- REST controller tests
- Helper and utility classes

### Integration Tests ✅
End-to-end integration testing for:
- **user-service**: Complete API workflows
- **product-service**: Service integration scenarios

**Features:**
- H2 in-memory database for isolation
- TestRestTemplate for HTTP testing
- Real application context testing

---

## CI/CD Pipeline Implementation

### Pipeline Structure
```
Development (feature branches)
├── Checkout & Branch Detection
├── Build Applications (Parallel)
├── Unit Tests (Parallel)
└── Package Applications

Staging (develop branch)  
├── All Development steps +
├── Integration Tests
└── Docker Image Building

Production (main branch)
├── All Staging steps +
├── Release Notes Generation
├── Environment Preparation
├── Deployment (service-discovery only)
└── Health Checks
```

### Jenkins Pipelines
- **Dev Pipeline**: Code validation and unit testing
- **Stage Pipeline**: Integration testing and image building
- **Prod Pipeline**: Full deployment with release documentation

---

## Local Development Setup

### Prerequisites
- **Java 11**: OpenJDK 11 or Oracle JDK 11
- **Maven 3.9+**: Build tool
- **Docker**: For containerization
- **Minikube**: Local Kubernetes cluster
- **kubectl**: Kubernetes CLI
- **Git**: Version control

### Installation Guide

#### 1. Install Minikube (macOS)
```bash
# Install Minikube for Apple Silicon
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-darwin-arm64
sudo install minikube-darwin-arm64 /usr/local/bin/minikube

# Verify installation
minikube version
```

#### 2. Install kubectl
```bash
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/darwin/arm64/kubectl"
chmod +x kubectl
sudo mv kubectl /usr/local/bin/
```

#### 3. Clone the Repository
```bash
git clone https://github.com/YourUsername/ecommerce-microservice-backend-app.git
cd ecommerce-microservice-backend-app
```

---

## Running the Application

### Option 1: Local Development (Testing Only)

#### Build All Services
```bash
# Build all microservices
./mvnw clean package

# Run specific service tests
cd user-service
../mvnw test  # Unit tests
../mvnw verify # Integration tests
```

#### Run Individual Services
```bash
# Start infrastructure services first
cd service-discovery && ../mvnw spring-boot:run &
cd cloud-config && ../mvnw spring-boot:run &
cd api-gateway && ../mvnw spring-boot:run &

# Start business services
cd user-service && ../mvnw spring-boot:run &
```

### Option 2: Minikube Deployment (Recommended)

#### 1. Start Minikube
```bash
# Start with optimized settings for 8GB RAM
minikube start \
  --driver=docker \
  --memory=4096 \
  --cpus=2 \
  --disk-size=20g \
  --addons=dashboard,metrics-server,ingress,registry

# Verify cluster
minikube status
kubectl cluster-info
```

#### 2. Configure Docker Environment
```bash
# Point Docker to Minikube's daemon
eval $(minikube docker-env)

# Verify connection
docker ps
```

#### 3. Setup Local Registry
```bash
# Enable registry addon
minikube addons enable registry

# Port forward registry
kubectl port-forward --namespace kube-system service/registry 5000:80 &
```

#### 4. Build and Deploy Infrastructure
```bash
# Create namespace
kubectl create namespace ecommerce-system

# Build infrastructure images
docker build -t localhost:5000/service-discovery:latest service-discovery/
docker push localhost:5000/service-discovery:latest

docker build -t localhost:5000/cloud-config:latest cloud-config/
docker push localhost:5000/cloud-config:latest

docker build -t localhost:5000/api-gateway:latest api-gateway/
docker push localhost:5000/api-gateway:latest

# Deploy infrastructure (apply in order)
kubectl apply -f k8s/minikube/config/ -n ecommerce-system
kubectl apply -f k8s/minikube/infrastructure/service-discovery/ -n ecommerce-system
sleep 60
kubectl apply -f k8s/minikube/infrastructure/cloud-config/ -n ecommerce-system
sleep 60
kubectl apply -f k8s/minikube/infrastructure/api-gateway/ -n ecommerce-system
```

#### 5. Deploy Business Services (Optional)
```bash
# Build user service image
docker build -t localhost:5000/user-service:latest user-service/
docker push localhost:5000/user-service:latest

# Deploy user service
kubectl apply -f k8s/minikube/services/user-service/ -n ecommerce-system
```

---

## Accessing Services

### Service Discovery (Eureka)
```bash
# Get service URL
minikube service service-discovery -n ecommerce-system --url

# Or use port-forwarding
kubectl port-forward svc/service-discovery 8761:8761 -n ecommerce-system
# Access: http://localhost:8761
```

### API Gateway
```bash
# Get gateway URL  
minikube service api-gateway -n ecommerce-system --url

# Or use port-forwarding
kubectl port-forward svc/api-gateway 8080:8080 -n ecommerce-system
# Access: http://localhost:8080
```

### Zipkin Tracing
```bash
# Port-forward Zipkin
kubectl port-forward svc/zipkin 9411:9411 -n ecommerce-system
# Access: http://localhost:9411
```

---

## Monitoring & Debugging

### Check Pod Status
```bash
# View all pods
kubectl get pods -n ecommerce-system

# Check specific service logs
kubectl logs -l app=service-discovery -n ecommerce-system

# Describe pod for debugging
kubectl describe pod <pod-name> -n ecommerce-system
```

### Resource Monitoring
```bash
# Check node resources
kubectl top nodes

# Check pod resource usage
kubectl top pods -n ecommerce-system

# Monitor Docker memory usage
docker stats
```

---

## Jenkins CI/CD Integration

### Pipeline Setup
1. **Configure Jenkins with Maven and JDK 11**
2. **Setup Git repository connection**
3. **Create pipeline jobs for each environment:**
   - `microservices-dev-minikube`
   - `microservices-stage-minikube`  
   - `microservices-prod-minikube`

### Pipeline Configuration
```groovy
// Use Pipeline from SCM
// Repository: https://github.com/YourUsername/ecommerce-microservice-backend-app.git
// Script Paths:
// - jenkins/minikube/user-service/Jenkinsfile-dev-minikube
// - jenkins/minikube/user-service/Jenkinsfile-stage-minikube
// - jenkins/minikube/user-service/Jenkinsfile-prod-minikube
```

---

## Resource Optimization

### For Limited Hardware (8GB RAM)
The project is optimized for MacBook Air M2 with 8GB RAM:

- **Memory limits**: 768Mi per service
- **CPU limits**: 400m per service  
- **JVM optimization**: -Xmx384m
- **Selective deployment**: Only critical infrastructure services
- **Resource monitoring**: Built-in resource tracking

### Memory Allocation Strategy
```
Infrastructure Services:
├── service-discovery: 768Mi/384Mi (limit/request)
├── cloud-config: 384Mi/192Mi
├── api-gateway: 768Mi/384Mi
└── zipkin: 256Mi/128Mi
Total: ~2.2GB allocated, ~5GB available for system
```

---

## Testing Commands

### Run All Tests
```bash
# Unit tests for all services with testing
./mvnw test -Dtest="**/unit/**/*Test*"

# Integration tests for services that have them
./mvnw verify -Dtest="**/integration/**/*Test*"

# Specific service testing
cd user-service
../mvnw test  # All tests
../mvnw test -Dtest="UserServiceImplTest"  # Specific test
```

### Test Results
Tests generate JUnit XML reports in:
- `{service}/target/surefire-reports/` (Unit tests)
- `{service}/target/failsafe-reports/` (Integration tests)

---

## Documentation

### Generated Guides
The project includes comprehensive documentation:

1. **[Minikube Setup Guide](temp-guides/minikube-setup-guide.md)** - Detailed local setup
2. **[Migration Plan](temp-guides/minikube-migration-plan.md)** - AKS to Minikube migration
3. **[AKS Deployment Guide](temp-guides/Microservice-AKS-Deployment-Guide.md)** - Azure deployment (legacy)

### Release Notes
Automated release notes are generated using **Convco** (Conventional Commits) during CI/CD pipeline execution.

---

## Troubleshooting

### Common Issues

#### Minikube Won't Start
```bash
# Reset Minikube
minikube delete
minikube start --driver=docker --memory=4096

# Check Docker is running
docker ps
```

#### Pods in Pending State
```bash
# Check resource constraints
kubectl describe pod <pod-name> -n ecommerce-system
kubectl top nodes

# Scale down if needed
kubectl scale deployment service-discovery --replicas=0 -n ecommerce-system
```

#### Registry Connection Issues
```bash
# Restart port-forward
pkill -f "kubectl port-forward.*registry"
kubectl port-forward --namespace kube-system service/registry 5000:80 &
```

#### Out of Memory
```bash
# Check current usage
kubectl top pods -n ecommerce-system
docker stats

# Restart Minikube with more memory (if available)
minikube stop
minikube start --memory=5120
```

---

## Cleanup

### Stop All Services
```bash
# Stop all deployments
kubectl delete namespace ecommerce-system

# Stop Minikube
minikube stop

# Complete cleanup
minikube delete
pkill -f "kubectl port-forward"
```

---

## Contributing

### Development Workflow
1. **Create feature branch** from `develop`
2. **Implement changes** with tests
3. **Run dev pipeline** for validation
4. **Merge to develop** (triggers stage pipeline)
5. **Merge to main** (triggers prod pipeline)

### Testing Requirements
- All new features must include unit tests
- Integration tests required for API changes
- Pipeline must pass before merging

---

## Project Status

### Completed Features ✅
- Microservices architecture with Spring Cloud
- Comprehensive unit and integration testing
- CI/CD pipelines for multiple environments
- Kubernetes deployments (AKS and Minikube)
- Resource optimization for limited hardware
- Complete documentation and migration guides

### Known Limitations ⚠️
- **Resource constraints**: Only infrastructure services run simultaneously on 8GB RAM
- **No E2E tests**: Hardware limitations prevent full service testing
- **Selective deployment**: Production deploys only service-discovery for demonstration

---

## License

This project is developed for educational purposes as part of Software Engineering coursework.

---

## Contact

**Author**: Daniel Escobar  
**University**: Universidad ICESI  
**Course**: Software Engineering - 8th Semester  
**Date**: December 2024

For questions or contributions, please open an issue or contact through university channels.

---

*Happy Coding! 🚀*
