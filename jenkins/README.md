# Jenkins Pipeline Setup for E-commerce Microservices

## Overview
This directory contains Jenkins pipeline configurations for our e-commerce microservices project with three environments:

- **Development**: Build and basic testing
- **Stage**: Build, test, and deploy to staging Kubernetes 
- **Production**: Full CI/CD with comprehensive testing and production deployment

## Pipeline Structure

```
jenkins/
├── README.md                          # This file
├── shared/                            # Shared pipeline scripts
│   ├── build-functions.groovy         # Common build functions
│   └── deploy-functions.groovy        # Common deployment functions
├── microservices/                     # Individual microservice pipelines
│   ├── user-service/
│   │   ├── Jenkinsfile-dev           # Development pipeline
│   │   ├── Jenkinsfile-stage         # Staging pipeline  
│   │   └── Jenkinsfile-prod          # Production pipeline
│   ├── product-service/
│   └── order-service/
└── infrastructure/                    # Infrastructure deployment pipelines
    └── Jenkinsfile-infrastructure

```

## Prerequisites

### 1. Jenkins Plugins Required
- Pipeline plugin
- Docker plugin
- Kubernetes plugin
- Azure CLI plugin
- Maven Integration plugin

### 2. Jenkins Credentials Setup
In Jenkins Credentials Manager, create:
- `azure-credentials`: Azure Service Principal (with ACR and AKS access)
- `acr-credentials`: Azure Container Registry credentials
- `k8s-dev-config`: Kubernetes config for development cluster
- `k8s-stage-config`: Kubernetes config for staging cluster  
- `k8s-prod-config`: Kubernetes config for production cluster

### 3. Environment Variables
Set these in Jenkins Global Properties:
- `AZURE_RESOURCE_GROUP`: 'AKS-ecommerce'
- `AKS_CLUSTER_NAME`: 'KubeCluster'
- `ACR_NAME`: 'acrecommerce69'

## How to Use

### 1. Development Pipeline
- Triggered on every commit to feature branches
- Runs unit tests and builds Docker image
- No deployment to Kubernetes

### 2. Staging Pipeline  
- Triggered on commits to `develop` branch
- Runs full test suite including integration tests
- Deploys to staging Kubernetes environment
- Runs smoke tests

### 3. Production Pipeline
- Triggered on commits to `main` branch or manual trigger
- Runs comprehensive test suite
- Requires manual approval for production deployment
- Deploys to production Kubernetes environment
- Runs full health checks

## Getting Started

1. Create Jenkins multibranch pipeline jobs for each microservice
2. Point to the respective Jenkinsfile in each microservice directory
3. Configure branch sources and build triggers
4. Run your first pipeline!

## Microservice-Specific Notes

### User Service
- Uses H2 database for testing
- MySQL for staging/production
- Requires Eureka service discovery
- Port: 8700

### Future Microservices
Follow the same pattern as user-service for consistency. 