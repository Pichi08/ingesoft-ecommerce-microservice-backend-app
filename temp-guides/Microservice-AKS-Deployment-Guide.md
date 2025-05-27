# Complete Guide: Deploying a Microservices Application to Azure Kubernetes Service (AKS)

## Introduction

In this guide, we've deployed a Spring Boot microservices-based e-commerce application to Azure Kubernetes Service (AKS). Below is a detailed, step-by-step explanation of everything we've done.

## Initial Setup

1. **Created an AKS Cluster in Azure**:
   - Created a resource group named `AKS-ecommerce`
   - Deployed a Kubernetes cluster named `KubeCluster` in the East US region
   - Used Standard_DS2_v2 VMs with 2 vCPUs and approximately 7GB RAM
   - Configured with 2 nodes

2. **Connected to the AKS Cluster**:
   ```bash
   az aks get-credentials --resource-group AKS-ecommerce --name KubeCluster
   ```

## Setting Up Azure Container Registry (ACR) and Pushing Images

1. **Created an Azure Container Registry**:
   ```bash
   az acr create --resource-group AKS-ecommerce --name acrecommerce69 --sku Basic
   ```
   This created a registry named `acrecommerce69.azurecr.io`

2. **Logged into the ACR**:
   ```bash
   az acr login --name acrecommerce69
   ```

3. **Tagged local Docker images for ACR**:
   ```bash
   # Tagged each service image with the ACR repository
   docker tag cloud-config:0.1.0 acrecommerce69.azurecr.io/cloud-config:0.1.0
   docker tag service-discovery:0.1.0 acrecommerce69.azurecr.io/service-discovery:0.1.0
   docker tag api-gateway:0.1.0 acrecommerce69.azurecr.io/api-gateway:0.1.0
   docker tag zipkin:0.1.0 acrecommerce69.azurecr.io/zipkin:0.1.0
   docker tag user-service:0.1.0 acrecommerce69.azurecr.io/user-service:0.1.0
   docker tag product-service:0.1.0 acrecommerce69.azurecr.io/product-service:0.1.0
   docker tag order-service:0.1.0 acrecommerce69.azurecr.io/order-service:0.1.0
   docker tag payment-service:0.1.0 acrecommerce69.azurecr.io/payment-service:0.1.0
   docker tag shipping-service:0.1.0 acrecommerce69.azurecr.io/shipping-service:0.1.0
   docker tag favourite-service:0.1.0 acrecommerce69.azurecr.io/favourite-service:0.1.0
   docker tag proxy-client:0.1.0 acrecommerce69.azurecr.io/proxy-client:0.1.0
   ```

4. **Pushed the tagged images to ACR**:
   ```bash
   # Pushed each image to the ACR repository
   docker push acrecommerce69.azurecr.io/zipkin:0.1.0
   docker push acrecommerce69.azurecr.io/service-discovery:0.1.0
   docker push acrecommerce69.azurecr.io/cloud-config:0.1.0
   docker push acrecommerce69.azurecr.io/api-gateway:0.1.0
   docker push acrecommerce69.azurecr.io/user-service:0.1.0
   docker push acrecommerce69.azurecr.io/product-service:0.1.0
   docker push acrecommerce69.azurecr.io/order-service:0.1.0
   docker push acrecommerce69.azurecr.io/payment-service:0.1.0
   docker push acrecommerce69.azurecr.io/shipping-service:0.1.0
   docker push acrecommerce69.azurecr.io/favourite-service:0.1.0
   docker push acrecommerce69.azurecr.io/proxy-client:0.1.0
   ```

5. **Configured AKS to access the ACR**:
   ```bash
   # Attached the ACR to the AKS cluster for pull access
   az aks update --name KubeCluster --resource-group AKS-ecommerce --attach-acr acrecommerce69
   ```
   This gave the AKS cluster permission to pull images from the ACR without requiring separate authentication

## Organizing Kubernetes Resources

1. **Created a structured directory layout for Kubernetes configurations**:
   ```
   k8s/
   ├── config/
   ├── infrastructure/
   │   ├── api-gateway/
   │   ├── cloud-config/
   │   ├── service-discovery/
   │   └── zipkin/
   └── services/
       ├── favourite-service/
       ├── order-service/
       ├── payment-service/
       ├── product-service/
       ├── proxy-client/
       ├── shipping-service/
       └── user-service/
   ```

2. **Created namespaces to organize resources**:
   ```bash
   kubectl create namespace ecommerce-system
   kubectl create namespace ecommerce-services
   ```
   - `ecommerce-system`: For infrastructure components
   - `ecommerce-services`: For business service components

## Deploying Infrastructure Components

1. **Deployed the Config Server (Spring Cloud Config)**:
   - Created deployment, service, and necessary resources in `k8s/infrastructure/cloud-config/`
   - Deployed to the `ecommerce-system` namespace
   - Used image `acrecommerce69.azurecr.io/cloud-config:0.1.0`
   - Applied port 9296 for service access
   - Applied with:
     ```bash
     kubectl apply -f k8s/infrastructure/cloud-config/
     ```

2. **Deployed the Service Discovery (Eureka)**:
   - Created deployment, service, and necessary resources in `k8s/infrastructure/service-discovery/`
   - Deployed to the `ecommerce-system` namespace
   - Used image `acrecommerce69.azurecr.io/service-discovery:0.1.0`
   - Applied port 8761 for service access
   - Applied with:
     ```bash
     kubectl apply -f k8s/infrastructure/service-discovery/
     ```

3. **Deployed Distributed Tracing (Zipkin)**:
   - Created deployment, service, and necessary resources in `k8s/infrastructure/zipkin/`
   - Deployed to the `ecommerce-system` namespace
   - Used image `acrecommerce69.azurecr.io/zipkin:0.1.0`
   - Applied port 9411 for service access
   - Applied with:
     ```bash
     kubectl apply -f k8s/infrastructure/zipkin/
     ```

4. **Deployed API Gateway (Spring Cloud Gateway)**:
   - Created deployment, service, and necessary resources in `k8s/infrastructure/api-gateway/`
   - Deployed to the `ecommerce-system` namespace
   - Used image `acrecommerce69.azurecr.io/api-gateway:0.1.0`
   - Applied port 8080 for service access
   - Applied with:
     ```bash
     kubectl apply -f k8s/infrastructure/api-gateway/
     ```

## Deploying Business Services

1. **Created ConfigMap for shared configuration**:
   ```yaml
   apiVersion: v1
   kind: ConfigMap
   metadata:
     name: spring-config
     namespace: ecommerce-system
   data:
     SPRING_PROFILES_ACTIVE: dev
     CONFIG_SERVER_URL: http://cloud-config.ecommerce-system.svc.cluster.local:9296
     EUREKA_SERVER_URL: http://service-discovery.ecommerce-system.svc.cluster.local:8761/eureka
     ZIPKIN_BASE_URL: http://zipkin.ecommerce-system.svc.cluster.local:9411
   ```

2. **Deployed User Service**:
   - Initially deployed to the `ecommerce-services` namespace
   - Used image `acrecommerce69.azurecr.io/user-service:0.1.0`
   - Created Deployment with CPU request of 150m, limit of 300m
   - Created Service with port 8700
   - Applied with:
     ```bash
     kubectl apply -f k8s/services/user-service/
     ```

3. **Attempted to deploy other services**:
   - Deployed product-service, payment-service, shipping-service, favourite-service, and order-service
   - Several services encountered resource constraints and went into pending state
   - Some pods experienced OOMKilled errors due to memory constraints

## Troubleshooting and Fixing Issues

1. **Encountered 500 error when accessing product-service**:
   ```bash
   curl http://9.169.71.109:8080/product-service/api/products
   ```
   The response showed a 500 Internal Server Error.

2. **Identified namespace issue**:
   - API Gateway was in `ecommerce-system` namespace
   - product-service was in `ecommerce-services` namespace
   - This caused routing problems as services couldn't discover each other across namespaces

3. **Fixed namespace consistency**:
   - Modified all service configurations to use the same namespace (`ecommerce-system`)
   - Updated each deployment.yaml and service.yaml file:
     ```yaml
     metadata:
       name: service-name
       namespace: ecommerce-system
     ```
   - Applied updated configurations

4. **Fixed ConfigMap issues**:
   - Ensured the ConfigMap had all necessary environment variables
   - Added missing ZIPKIN_BASE_URL and EUREKA_SERVER_URL

5. **Fixed API Gateway registration with Eureka**:
   - Created a ConfigMap for the API Gateway with proper application name
   ```yaml
   apiVersion: v1
   kind: ConfigMap
   metadata:
     name: api-gateway-config
     namespace: ecommerce-system
   data:
     application.yaml: |
       spring:
         application:
           name: API-GATEWAY
   ```
   - Applied and restarted the API Gateway pod

## Configuring API Gateway Routes

1. **Added Zipkin route to API Gateway**:
   ```yaml
   - id: zipkin-route
     uri: http://zipkin:9411
     predicates:
       - Path=/zipkin/**
     filters:
       - StripPrefix=0
   ```

2. **Added User Service route to API Gateway**:
   ```yaml
   - id: user-service-route
     uri: lb://USER-SERVICE
     predicates:
       - Path=/user-service/**
   ```

3. **Added Eureka route to API Gateway**:
   ```yaml
   - id: eureka-route
     uri: http://service-discovery:8761
     predicates:
       - Path=/eureka/**
     filters:
       - StripPrefix=0
   ```

4. **Added routes for other services**:
   - Added routes for product-service, order-service, payment-service, shipping-service, favourite-service, and proxy-client
   - Used load balancer routing with Eureka integration (lb://)
   - Applied the ConfigMap and restarted the API Gateway pod

## Managing Resources

1. **Handled resource constraints**:
   - Identified that the AKS cluster had limited resources (only 2 nodes with 2 vCPUs each)
   - Removed pods that were in pending state to free up resources
   - Used `kubectl delete deployment` to remove non-essential services

2. **Optimized resource usage**:
   - Adjusted CPU and memory requests/limits for critical services
   - Ran only essential services when testing specific functionality

## Accessing Services

1. **Direct access via port-forwarding**:
   ```bash
   kubectl port-forward -n ecommerce-system service/zipkin 9411:9411
   ```
   This allowed access to Zipkin UI at http://localhost:9411

2. **Access via API Gateway**:
   - Obtained the External IP of the API Gateway LoadBalancer service
     ```bash
     kubectl get service -n ecommerce-system api-gateway
     ```
   - The External IP was 172.214.8.134
   - Accessed services via the API Gateway at http://172.214.8.134:8080/[service-path]

## Stopping and Saving Resources

1. **Deleted unnecessary resources**:
   ```bash
   kubectl delete deployment [deployment-name] -n ecommerce-system
   ```

2. **Stopped AKS cluster when not in use**:
   ```bash
   az aks stop --resource-group AKS-ecommerce --name KubeCluster
   ```

3. **Verified cluster stopped**:
   ```bash
   az aks show --resource-group AKS-ecommerce --name KubeCluster --query "powerState.code"
   ```
   Output: "Stopped"

## Testing the Deployment

1. **Checked Service Discovery**:
   - Accessed Eureka dashboard at http://172.214.8.134:8080/eureka
   - Verified that services were correctly registered

2. **Tested User Service**:
   ```bash
   curl http://172.214.8.134:8080/user-service/api/users
   ```
   Received a proper JSON response with user data

3. **Tested Zipkin access**:
   ```bash
   curl -I http://172.214.8.134:8080/zipkin
   ```
   Received a 302 redirect response, indicating Zipkin was accessible

## Key Lessons and Best Practices

1. **Namespace consistency** is crucial for service discovery in Kubernetes
2. **Resource management** is important in constrained environments
3. **Explicit API Gateway routing configuration** using ConfigMaps provides better control and visibility
4. **Proper service registration** with Eureka requires correct application names
5. **ConfigMaps** should be used to externalize configuration from the application code

## Final Architecture

The final architecture consists of:
- Infrastructure components (API Gateway, Config Server, Service Discovery, Zipkin) in the `ecommerce-system` namespace
- Business services (user-service, product-service, etc.) in the same `ecommerce-system` namespace for consistency
- API Gateway as the single entry point to the system
- All services registered with Eureka service discovery
- All components configured via ConfigMaps for flexibility and maintainability

## Common Commands Reference

```bash
# Namespace operations
kubectl create namespace ecommerce-system
kubectl get namespaces

# View resources
kubectl get pods -n ecommerce-system
kubectl get deployments -n ecommerce-system
kubectl get services -n ecommerce-system
kubectl get configmaps -n ecommerce-system

# Detailed resource information
kubectl describe pod -n ecommerce-system [pod-name]
kubectl describe deployment -n ecommerce-system [deployment-name]
kubectl describe service -n ecommerce-system [service-name]

# Logs
kubectl logs -n ecommerce-system [pod-name]

# Port forwarding
kubectl port-forward -n ecommerce-system service/zipkin 9411:9411

# Apply configurations
kubectl apply -f [configuration-file-or-directory]

# Delete resources
kubectl delete pod -n ecommerce-system [pod-name]
kubectl delete deployment -n ecommerce-system [deployment-name]
kubectl delete service -n ecommerce-system [service-name]

# Cluster operations
az aks start --resource-group AKS-ecommerce --name KubeCluster
az aks stop --resource-group AKS-ecommerce --name KubeCluster
```

This comprehensive guide covers all steps needed to deploy and configure a Spring Boot microservices architecture on AKS, including troubleshooting common issues and implementing best practices. 