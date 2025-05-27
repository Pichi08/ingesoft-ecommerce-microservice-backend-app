# Minikube Setup Guide for E-commerce Infrastructure
## MacBook Air M2 with 8GB RAM - Optimized Configuration

### Overview
This guide sets up a local Kubernetes environment using Minikube specifically optimized for your MacBook Air M2 with 8GB RAM constraints. We'll focus on deploying only the **infrastructure services** to demonstrate the complete CI/CD pipeline while staying within resource limits.

## Prerequisites Check

```bash
# Check your system
system_profiler SPHardwareDataType | grep "Memory"
uname -a
```

## Step 1: Install Required Tools

### Install Minikube
```bash
# For Apple Silicon (M2)
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-darwin-arm64
sudo install minikube-darwin-arm64 /usr/local/bin/minikube
```

### Install kubectl (if not already installed)
```bash
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/darwin/arm64/kubectl"
chmod +x kubectl
sudo mv kubectl /usr/local/bin/
```

### Verify Docker Desktop is installed and running
```bash
docker --version
docker ps
```

## Step 2: Minikube Configuration for Your Hardware

### Start Minikube with Optimized Settings
```bash
# Delete any existing minikube cluster
minikube delete

# Start with resource constraints optimized for 8GB RAM
minikube start \
  --driver=docker \
  --memory=4096 \
  --cpus=2 \
  --disk-size=20g \
  --kubernetes-version=v1.28.0 \
  --container-runtime=docker \
  --addons=dashboard,metrics-server,ingress
```

### Configure Docker Environment
```bash
# Point Docker to Minikube's Docker daemon
eval $(minikube docker-env)

# Verify connection
docker ps
kubectl cluster-info
```

## Step 3: Setup Local Docker Registry

### Enable Registry Addon
```bash
# Restart minikube
minikube status
minikube start

# Enable the registry addon
minikube addons enable registry

# Verify registry is running
kubectl get pods -n kube-system | grep registry

```

### Configure Registry Access
```bash
# Get registry service details
kubectl get svc -n kube-system | grep registry

# Port forward registry for local access (run in background)
kubectl port-forward --namespace kube-system service/registry 5000:80 &
```

## Step 4: Build and Push Images to Local Registry

### Modify Docker Build Process
```bash
# Ensure you're using Minikube's Docker
eval $(minikube docker-env)

# Build images directly in Minikube's Docker daemon
cd /path/to/your/project

# Build infrastructure images
docker build -t localhost:5000/zipkin:latest zipkin/
docker build -t localhost:5000/service-discovery:latest service-discovery/
docker build -t localhost:5000/cloud-config:latest cloud-config/
docker build -t localhost:5000/api-gateway:latest api-gateway/

# Push to local registry
docker push localhost:5000/zipkin:latest
docker push localhost:5000/service-discovery:latest  
docker push localhost:5000/cloud-config:latest
docker push localhost:5000/api-gateway:latest

# Build user-service for pipeline testing
docker build -t localhost:5000/user-service:latest user-service/
docker push localhost:5000/user-service:latest
```

## Step 5: Create Minikube-Specific Kubernetes Manifests

### Resource-Optimized Configuration
Create `k8s/minikube/` directory with reduced resource requirements:

```yaml
# Example: k8s/minikube/infrastructure/service-discovery.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: service-discovery
  namespace: ecommerce-system
spec:
  replicas: 1
  selector:
    matchLabels:
      app: service-discovery
  template:
    metadata:
      labels:
        app: service-discovery
    spec:
      containers:
      - name: service-discovery
        image: localhost:5000/service-discovery:latest
        ports:
        - containerPort: 8761
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "dev"
        - name: SPRING_ZIPKIN_BASE_URL
          value: "http://zipkin:9411"
        resources:
          limits:
            memory: "768Mi"  # Reduced from 1024Mi
            cpu: "400m"      # Reduced from 500m
          requests:
            memory: "384Mi"  # Reduced from 512Mi
            cpu: "200m"      # Reduced from 250m
```

## Step 6: Deployment Strategy

### Deploy Infrastructure Services
```bash
# Create namespace
kubectl create namespace ecommerce-system

# Deploy in specific order
kubectl apply -f k8s/minikube/config/
kubectl apply -f k8s/minikube/infrastructure/zipkin.yaml
sleep 30
kubectl apply -f k8s/minikube/infrastructure/service-discovery.yaml
sleep 30
kubectl apply -f k8s/minikube/infrastructure/cloud-config.yaml
sleep 30
kubectl apply -f k8s/minikube/infrastructure/api-gateway.yaml
```

### Access Services
```bash
# Get service URLs
minikube service list -n ecommerce-system

# Access Eureka Dashboard
minikube service service-discovery -n ecommerce-system

# Access Zipkin
minikube service zipkin -n ecommerce-system

# Access API Gateway
minikube service api-gateway -n ecommerce-system
```

## Step 7: Resource Monitoring

### Monitor Resource Usage
```bash
# Check cluster resource usage
kubectl top nodes
kubectl top pods -n ecommerce-system

# Monitor system resources
top -o MEM
```

### Scale Down If Needed
```bash
# Scale down services if resources are tight
kubectl scale deployment service-discovery --replicas=0 -n ecommerce-system
kubectl scale deployment cloud-config --replicas=0 -n ecommerce-system

# Scale back up when needed
kubectl scale deployment service-discovery --replicas=1 -n ecommerce-system
```

## Step 8: Jenkins Integration with Minikube

### Minikube-Specific Environment Variables
```bash
# For Jenkins pipelines, use these instead of Azure
KUBE_CONFIG_PATH = "~/.kube/config"
DOCKER_REGISTRY = "localhost:5000"
NAMESPACE = "ecommerce-system"
```

## Resource Allocation Summary

### Infrastructure Services (Minimal Configuration)
- **Zipkin**: 256Mi/128Mi (limit/request), 200m/100m CPU
- **Eureka**: 768Mi/384Mi (limit/request), 400m/200m CPU  
- **Cloud Config**: 384Mi/192Mi (limit/request), 300m/150m CPU
- **API Gateway**: 768Mi/384Mi (limit/request), 400m/200m CPU

**Total**: ~2.2GB RAM, 1.3 CPU cores

### User Service (For Pipeline Testing)
- **User Service**: 512Mi/256Mi (limit/request), 300m/150m CPU

**Grand Total**: ~2.7GB RAM allocated to pods, leaving ~5GB for system

## Troubleshooting

### Common Issues and Solutions

#### Out of Memory
```bash
# Check memory usage
kubectl top nodes
kubectl top pods --all-namespaces

# Restart Minikube with more memory (if available)
minikube stop
minikube start --memory=5120  # Use 5GB if possible
```

#### Registry Issues
```bash
# Restart port-forward for registry
pkill -f "kubectl port-forward.*registry"
kubectl port-forward --namespace kube-system service/registry 5000:80 &
```

#### Pod Pending/Failed
```bash
# Check events and logs
kubectl describe pod <pod-name> -n ecommerce-system
kubectl logs <pod-name> -n ecommerce-system
```

## Cleanup Commands

```bash
# Stop all services but keep cluster
kubectl delete namespace ecommerce-system

# Stop Minikube completely  
minikube stop

# Delete Minikube cluster completely
minikube delete

# Clean Docker registry port-forward
pkill -f "kubectl port-forward.*registry"
```

## Performance Optimization Tips

1. **Use `imagePullPolicy: Never`** - Avoid pulling images from internet
2. **Reduce JVM heap sizes** - Add `-Xmx384m` to Spring Boot apps
3. **Use Spring Boot's lazy initialization** - Add `spring.main.lazy-initialization=true`
4. **Monitor with `docker stats`** - Keep an eye on container resource usage
5. **Use Minikube tunnel sparingly** - Only when needed for LoadBalancer services

This setup will allow you to run your infrastructure services locally and test your Jenkins CI/CD pipelines without needing Azure credits! 