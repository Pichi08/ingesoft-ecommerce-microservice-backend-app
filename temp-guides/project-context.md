# Complete Project Context: E-commerce Microservices Deployment to Azure Kubernetes Service

## Project Overview

We are working on deploying a complete e-commerce microservices application to Azure Kubernetes Service (AKS). The application consists of multiple Spring Boot microservices with a comprehensive architecture including:

**Core Infrastructure Services:**
- **API Gateway** - Entry point for all client requests
- **Service Discovery (Eureka)** - Service registration and discovery
- **Cloud Config** - Centralized configuration management
- **Zipkin** - Distributed tracing and monitoring

**Business Microservices:**
- **User Service** - User management and authentication
- **Product Service** - Product catalog management
- **Order Service** - Order processing
- **Payment Service** - Payment processing
- **Shipping Service** - Shipping and logistics
- **Favourite Service** - User favorites management
- **Proxy Client** - Internal service communication

## What We've Accomplished

### 1. Azure Infrastructure Setup
- Created AKS resource group `"AKS-ecommerce"`
- Deployed AKS cluster `"KubeCluster"` with 2 nodes (v1.31.7)
- Set up Azure Container Registry `"acrecommerce69"`
- Established kubectl connectivity to the cluster

### 2. Container Image Management
- Successfully built and tagged Docker images for all microservices
- Pushed all images to Azure Container Registry using `docker tag` and `docker push`
- Configured proper authentication with `az acr login`

### 3. Kubernetes Architecture Design
Evolved from imperative kubectl commands to a well-organized declarative approach:

```
k8s/
├── config/
│   ├── namespaces.yaml
│   ├── configmaps.yaml
│   └── secrets.yaml
├── infrastructure/
│   ├── zipkin.yaml
│   ├── service-discovery.yaml
│   ├── cloud-config.yaml
│   └── api-gateway.yaml
└── services/
    ├── user-service.yaml
    ├── product-service.yaml
    ├── order-service.yaml
    ├── payment-service.yaml
    ├── shipping-service.yaml
    ├── favourite-service.yaml
    └── proxy-client.yaml
```

### 4. Namespace Organization
- **ecommerce-system**: Core infrastructure services
- **ecommerce-services**: Business microservices

### 5. Resource Optimization
- Optimized resource allocation due to cluster constraints:
  - CPU requests: reduced from 250m to 150m
  - CPU limits: reduced from 500m to 300m
  - Memory limits: increased for service-discovery to 1024Mi
- Successfully managed resource allocation across 99% and 97% CPU utilization

### 6. Service Deployments
- **Zipkin**: Successfully deployed and accessible via port-forwarding (localhost:9411)
- **Eureka**: Successfully deployed with dashboard accessible (localhost:8761)
- **API Gateway**: Deployed as LoadBalancer with external IP `9.169.71.109`
- Health endpoints working: `http://9.169.71.109:8080/actuator/health`

### 7. Cost Management
- Implemented cost-saving measures by stopping the AKS cluster when not in use
- Successfully deleted unnecessary resources
- Confirmed cluster power state as "Stopped"

## Difficulties Encountered

### 1. Resource Constraints
- **CPU Limitations**: 2-node cluster quickly reached 99% and 97% CPU allocation
- **Memory Issues**: Service-discovery suffered OOMKilled errors with original 512Mi limit
- **Pod Failures**: Multiple pods entered CrashLoopBackOff or Pending states due to resource scarcity

### 2. Configuration Management Problems
- **Namespace Inconsistencies**: Product-service was initially configured for wrong namespace
- **Incomplete ConfigMaps**: Missing critical environment variables like:
  - `ZIPKIN_BASE_URL`
  - `EUREKA_SERVER_URL`
  - Other service-specific configuration keys
- **Environment Mismatch**: Had to ensure `SPRING_PROFILES_ACTIVE=dev` consistency

### 3. Service Communication Issues
- **API Gateway Errors**: External API calls returned 500 Internal Server Error
- **Service Registration**: Some services failed to properly register with Eureka
- **Configuration Propagation**: Services couldn't retrieve configurations due to incomplete ConfigMaps

### 4. Deployment Failures
- **Product-service**: Failed to deploy due to ConfigMap configuration errors
- **Cascading Failures**: Configuration issues caused multiple dependent services to fail
- **Resource Competition**: Services competing for limited cluster resources

## Current Status

### Successfully Running:
- ✅ AKS cluster infrastructure
- ✅ Azure Container Registry with all images
- ✅ Zipkin tracing service
- ✅ Eureka service discovery
- ✅ API Gateway with external access
- ✅ Basic health check endpoints

### Issues Remaining:
- ❌ End-to-end API testing through gateway
- ❌ Service-to-service communication validation
- ❌ Complete microservices integration
- ❌ Can't run all microservices because of Azure student subscription (there is nothing to do about it)

## What We Want to Accomplish Next

### 1. Complete Current Deployment
- **End-to-End Testing**: Validate complete request flows through the API Gateway

### 2. Testing Implementation for User-Service ()

#### Integration Tests (5+ tests required):
- **Service Discovery Integration**: Test user-service registration and discovery with Eureka
- **Database Integration**: Test user CRUD operations with persistent storage
- **API Gateway Integration**: Test user endpoints accessibility through the gateway


#### End-to-End Tests (5+ tests required):
- **Complete User Registration Flow**: From API gateway to database persistence
- **User Authentication Flow**: Login process through all service layers
- **User Profile Management**: Create, read, update user profiles end-to-end
- **User Favorites Integration**: Test user-service interaction with favourite-service
- **Order Creation with User Validation**: Test user verification in order processing

#### Performance and Stress Tests with Locust:
- **User Registration Load Testing**: Simulate concurrent user registrations
- **Authentication Performance**: Test login performance under load
- **API Gateway Throughput**: Test user endpoints through gateway under stress
- **Database Connection Pool**: Test user-service database performance limits
- **Service Discovery Load**: Test Eureka performance with high user-service traffic

### 3. Jenkins Pipeline Implementation

- For the selected microservices, define pipelines that allow for the application build in the development environment.
- For the selected microservices, define pipelines that include building and testing the application deployed in Kubernetes (stage environment).
- For the selected microservices, execute a deployment pipeline that includes building with unit tests, validating system tests, and then deploying the application to Kubernetes (master environment).

### 4. Infrastructure Improvements
- **Resource Scaling**: Optimize cluster resources for production workloads
- **Monitoring Setup**: Implement comprehensive monitoring and alerting
- **Security Hardening**: Implement proper RBAC and network policies



## Key Technical Commands Used

### Docker Commands:
```bash
docker tag <local-image> acrecommerce69.azurecr.io/<service-name>:latest
docker push acrecommerce69.azurecr.io/<service-name>:latest
```

### Azure CLI Commands:
```bash
az aks get-credentials --resource-group AKS-ecommerce --name KubeCluster
az aks stop --resource-group AKS-ecommerce --name KubeCluster
az acr login --name acrecommerce69
```

### kubectl Commands:
```bash
kubectl apply -f k8s/config/
kubectl apply -f k8s/infrastructure/
kubectl apply -f k8s/services/
kubectl get pods -n ecommerce-system
kubectl get pods -n ecommerce-services
kubectl describe pod <pod-name> -n <namespace>
kubectl port-forward svc/<service-name> <local-port>:<service-port> -n <namespace>
kubectl scale deployment <deployment-name> --replicas=0 -n <namespace>
kubectl delete namespace <namespace-name>
```

## Resource Configuration Examples

### Optimized Resource Allocation:
```yaml
resources:
  requests:
    memory: "256Mi"
    cpu: "150m"
  limits:
    memory: "512Mi"
    cpu: "300m"
```

### Service-Discovery Special Configuration:
```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "150m"
  limits:
    memory: "1024Mi"
    cpu: "300m"
```

## Environment Configuration

### Spring Profiles:
- `SPRING_PROFILES_ACTIVE=dev` (consistent across all services)

### Critical Missing ConfigMap Variables:
- `ZIPKIN_BASE_URL`
- `EUREKA_SERVER_URL`
- Database connection strings
- Service-specific configuration properties

---

This context provides a complete picture of our current microservices deployment project, the challenges we've overcome, and our comprehensive roadmap for testing and pipeline implementation. 