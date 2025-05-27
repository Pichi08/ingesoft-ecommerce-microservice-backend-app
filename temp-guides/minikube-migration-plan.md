# Minikube Migration Plan: From Azure to Local Development
## Complete Strategy for MacBook Air M2 with 8GB RAM

### **Current Situation Analysis**
- **Problem**: Azure credits exhausted, cannot continue with AKS
- **Hardware**: MacBook Air M2, 8GB RAM (significant constraint)
- **Objective**: Maintain CI/CD pipeline learning while working within resource limitations
- **Solution**: Migrate to local Minikube with infrastructure-only approach

---

## **Phase 1: Environment Setup and Migration** 

### **Step 1: Install and Configure Minikube**
```bash
# Install Minikube for Apple Silicon
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-darwin-arm64
sudo install minikube-darwin-arm64 /usr/local/bin/minikube

# Start Minikube with optimized settings for 8GB RAM
minikube start \
  --driver=docker \
  --memory=4096 \
  --cpus=2 \
  --disk-size=20g \
  --kubernetes-version=v1.28.0 \
  --container-runtime=docker \
  --addons=dashboard,metrics-server,ingress,registry

# Verify installation
minikube status
kubectl cluster-info
```

### **Step 2: Setup Local Docker Registry**
```bash
# Enable registry addon
minikube addons enable registry

# Port forward registry for local access
kubectl port-forward --namespace kube-system service/registry 5000:80 &

# Verify registry accessibility
curl http://localhost:5000/v2/_catalog
```

### **Step 3: Build Infrastructure Images Locally**
```bash
# Configure Docker to use Minikube's daemon
eval $(minikube docker-env)

# Build and push infrastructure images
docker pull openzipkin/zipkin:latest
docker tag openzipkin/zipkin:latest localhost:5000/zipkin:latest
docker push localhost:5000/zipkin:latest

# Build service-discovery
cd service-discovery
mvn clean package -DskipTests
docker build -t localhost:5000/service-discovery:latest .
docker push localhost:5000/service-discovery:latest

# Build cloud-config
cd ../cloud-config
mvn clean package -DskipTests
docker build -t localhost:5000/cloud-config:latest .
docker push localhost:5000/cloud-config:latest

# Build api-gateway
cd ../api-gateway
mvn clean package -DskipTests
docker build -t localhost:5000/api-gateway:latest .
docker push localhost:5000/api-gateway:latest

# Build user-service for pipeline testing
cd ../user-service
mvn clean package -DskipTests
docker build -t localhost:5000/user-service:latest .
docker push localhost:5000/user-service:latest
```

---

## **Phase 2: Deploy Infrastructure Services**

### **Step 4: Create Namespace and Apply Configurations**
```bash
# Create namespace
kubectl create namespace ecommerce-system

# Apply Minikube-optimized configurations
kubectl apply -f k8s/minikube/config/configmaps.yaml

# Deploy infrastructure services in order
kubectl apply -f k8s/minikube/infrastructure/zipkin.yaml
sleep 30

kubectl apply -f k8s/minikube/infrastructure/service-discovery.yaml
sleep 60

kubectl apply -f k8s/minikube/infrastructure/cloud-config.yaml
sleep 60

kubectl apply -f k8s/minikube/infrastructure/api-gateway.yaml
```

### **Step 5: Verify Infrastructure Deployment**
```bash
# Check all pods are running
kubectl get pods -n ecommerce-system

# Check services
kubectl get services -n ecommerce-system

# Wait for all pods to be ready
kubectl wait --for=condition=ready pod --all -n ecommerce-system --timeout=300s

# Get service URLs
minikube service list -n ecommerce-system
```

---

## **Phase 3: Jenkins Configuration Update**

### **Step 6: Update Jenkins Global Configuration**
**Remove Azure-specific configurations:**
- Delete `azure-credentials` from Jenkins credentials
- Remove Azure CLI plugin dependency

**Add Minikube-specific configurations:**
- Ensure Docker is available to Jenkins
- Verify Jenkins can access `~/.kube/config`
- Test `minikube` command availability

### **Step 7: Create New Jenkins Jobs**
```bash
# Create infrastructure pipeline job
# Name: "infrastructure-minikube"
# Pipeline script from SCM: jenkins/minikube/Jenkinsfile-infrastructure-minikube

# Create user-service pipeline job  
# Name: "user-service-minikube"
# Pipeline script from SCM: jenkins/minikube/Jenkinsfile-user-service-minikube
```

---

## **Phase 4: Testing and Validation**

### **Step 8: Test Infrastructure Pipeline**
```bash
# Run infrastructure pipeline in Jenkins
# Expected outcome: All 4 infrastructure services deployed
# Expected resource usage: ~2.2GB RAM, 1.3 CPU cores

# Verify service accessibility
minikube service zipkin -n ecommerce-system --url
minikube service service-discovery -n ecommerce-system --url
minikube service api-gateway -n ecommerce-system --url
```

### **Step 9: Test User Service Pipeline**
```bash
# Run user-service pipeline in Jenkins
# Expected outcome: User service built, tested, and deployed
# Expected resource usage: ~2.7GB RAM total (all services)

# Verify integration
curl $(minikube service user-service -n ecommerce-system --url)/user-service/actuator/health
curl $(minikube service api-gateway -n ecommerce-system --url)/user-service/actuator/health
```

---

## **Resource Management Strategy**

### **Memory Allocation Breakdown**
```
Infrastructure Services:
├── Zipkin:           256Mi limit / 128Mi request
├── Eureka:           768Mi limit / 384Mi request  
├── Cloud Config:     384Mi limit / 192Mi request
├── API Gateway:      768Mi limit / 384Mi request
└── User Service:     512Mi limit / 256Mi request
                     ────────────────────────────
Total Allocated:      2.7GB RAM / 1.3 CPU cores
System Available:     ~5GB remaining for macOS
```

### **Optimization Techniques Applied**
1. **JVM Heap Reduction**: `-Xmx384m` instead of default
2. **Lazy Initialization**: `spring.main.lazy-initialization=true`
3. **Reduced Logging**: WARN level for Spring and Netflix libraries
4. **Image Pull Policy**: `Never` - use only local images
5. **Probe Timing**: Adjusted for slower startup times

### **Scaling Strategy**
```bash
# If resources are tight, scale down non-essential services
kubectl scale deployment cloud-config --replicas=0 -n ecommerce-system

# Scale back up when needed
kubectl scale deployment cloud-config --replicas=1 -n ecommerce-system

# Monitor resource usage
kubectl top nodes
kubectl top pods -n ecommerce-system
```

---

## **Phase 5: CI/CD Pipeline Adaptation**

### **Pipeline Strategy Changes**
**From Azure-based to Minikube-based:**
- **Registry**: `acrecommerce69.azurecr.io` → `localhost:5000`
- **Authentication**: Azure Service Principal → Local kubectl config
- **Deployment**: AKS → Minikube
- **Service Access**: LoadBalancer → NodePort + `minikube service`
- **Health Checks**: External IP → `minikube service --url`

### **Development Workflow**
```bash
# 1. Start development session
minikube start
eval $(minikube docker-env)
kubectl port-forward --namespace kube-system service/registry 5000:80 &

# 2. Run infrastructure pipeline
# 3. Run user-service pipeline for testing
# 4. Access services for manual testing

# 5. End session (save resources)
minikube stop
pkill -f "kubectl port-forward.*registry"
```

---

## **Success Criteria and Validation**

### **Phase 1 Success Criteria**
- ✅ Minikube running with 4GB allocated
- ✅ Local registry accessible at localhost:5000
- ✅ All infrastructure images built and pushed locally

### **Phase 2 Success Criteria**
- ✅ All 4 infrastructure services deployed and running
- ✅ Services accessible via `minikube service` commands
- ✅ Resource usage under 3GB total

### **Phase 3 Success Criteria**
- ✅ Jenkins pipelines updated for Minikube
- ✅ Azure dependencies removed
- ✅ Local Docker registry integration working

### **Phase 4 Success Criteria**
- ✅ Infrastructure pipeline successfully deploys all services
- ✅ User service pipeline builds, tests, and deploys
- ✅ End-to-end connectivity through API Gateway

### **Phase 5 Success Criteria**
- ✅ Complete CI/CD workflow functional locally
- ✅ Pipeline testing demonstrates all required concepts
- ✅ Resource constraints respected throughout

---

## **Benefits of Minikube Approach**

### **Immediate Advantages**
1. **No Cost**: Completely free, no Azure credits needed
2. **Full Control**: Complete environment control and customization
3. **Fast Iteration**: No network latency, instant deployments
4. **Learning Continuity**: Maintain CI/CD pipeline learning objectives

### **Educational Value**
1. **Resource Optimization**: Learn to work within constraints
2. **Local Development**: Industry-standard local K8s development
3. **Pipeline Portability**: Pipelines work across cloud providers
4. **Troubleshooting Skills**: Direct access to logs and debugging

### **Production Readiness**
- Pipelines easily adaptable back to cloud when resources available
- Local development environment mirrors production patterns
- Docker and Kubernetes skills fully transferable
- CI/CD concepts remain cloud-agnostic

---

## **Next Steps After Migration**

### **Immediate Tasks**
1. Execute Phase 1-2: Environment setup and infrastructure deployment
2. Update Jenkins configuration (Phase 3)
3. Test both pipelines (Phase 4)
4. Document any issues and optimizations needed

### **Future Enhancements**
1. **Add Integration Tests**: Comprehensive service interaction testing
2. **Performance Testing**: Locust load testing within resource limits
3. **Pipeline Extensions**: Add security scanning, quality gates
4. **Multi-Environment**: Create dev/staging/prod simulation locally

### **Cloud Migration Preparation**
1. **Documentation**: Keep detailed notes for easy cloud re-deployment
2. **Environment Variables**: Maintain cloud-ready configuration
3. **Resource Planning**: Document actual vs. allocated resource usage
4. **Cost Estimation**: Prepare for future cloud resource planning

This migration plan transforms your resource constraint into a learning opportunity while maintaining all CI/CD pipeline objectives! 