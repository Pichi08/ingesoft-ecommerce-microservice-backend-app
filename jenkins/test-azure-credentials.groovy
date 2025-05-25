/**
 * Test Pipeline for Azure Service Principal Credentials
 * 
 * Purpose: Verify Azure authentication and ACR access work correctly
 * 
 * This pipeline tests:
 * 1. Azure CLI authentication
 * 2. Azure Container Registry access
 * 3. AKS cluster connectivity
 */

pipeline {
    agent any
    
    environment {
        ACR_NAME = 'acrecommerce69'
        AZURE_RESOURCE_GROUP = 'AKS-ecommerce'
        AKS_CLUSTER_NAME = 'KubeCluster'
    }
    
    stages {
        stage('🔍 Test Azure Authentication') {
            steps {
                script {
                    echo "🔑 Testing Azure Service Principal authentication..."
                    
                    // Test Azure CLI authentication
                    azureCLI commands: [
                        [script: 'echo "=== Azure CLI Version ==="'],
                        [script: 'az version --output table'],
                        [script: 'echo "=== Current Account ==="'],
                        [script: 'az account show --output table'],
                        [script: 'echo "=== Resource Groups ==="'],
                        [script: 'az group list --output table']
                    ], principalCredentialId: 'azure-credentials'
                    
                    echo "✅ Azure CLI authentication successful!"
                }
            }
        }
        
        stage('🐳 Test ACR Access') {
            steps {
                script {
                    echo "🔑 Testing Azure Container Registry access..."
                    
                    // Test ACR authentication and access
                    azureCLI commands: [
                        [script: 'echo "=== ACR Information ==="'],
                        [script: "az acr show --name ${ACR_NAME} --output table"],
                        [script: 'echo "=== ACR Login Test ==="'],
                        [script: "az acr login --name ${ACR_NAME}"],
                        [script: 'echo "=== ACR Repository List ==="'],
                        [script: "az acr repository list --name ${ACR_NAME} --output table || echo 'No repositories found'"]
                    ], principalCredentialId: 'azure-credentials'
                    
                    echo "✅ ACR access successful!"
                }
            }
        }
        
        stage('☸️ Test AKS Access') {
            steps {
                script {
                    echo "🔑 Testing AKS cluster access..."
                    
                    // Test AKS authentication and access
                    azureCLI commands: [
                        [script: 'echo "=== AKS Cluster Information ==="'],
                        [script: "az aks show --resource-group ${AZURE_RESOURCE_GROUP} --name ${AKS_CLUSTER_NAME} --output table"],
                        [script: 'echo "=== Getting AKS Credentials ==="'],
                        [script: "az aks get-credentials --resource-group ${AZURE_RESOURCE_GROUP} --name ${AKS_CLUSTER_NAME} --overwrite-existing"],
                        [script: 'echo "=== Cluster Info ==="'],
                        [script: 'kubectl cluster-info'],
                        [script: 'echo "=== Cluster Nodes ==="'],
                        [script: 'kubectl get nodes'],
                        [script: 'echo "=== Namespaces ==="'],
                        [script: 'kubectl get namespaces']
                    ], principalCredentialId: 'azure-credentials'
                    
                    echo "✅ AKS access successful!"
                }
            }
        }
        
        stage('📊 Test Summary') {
            steps {
                script {
                    echo "📋 Credential Test Summary:"
                    echo "✅ Azure CLI Authentication: SUCCESS"
                    echo "✅ Azure Container Registry: SUCCESS"
                    echo "✅ Azure Kubernetes Service: SUCCESS"
                    echo ""
                    echo "🎉 All Azure credentials are working correctly!"
                    echo "🚀 Ready to run your microservice pipelines!"
                }
            }
        }
    }
    
    post {
        success {
            echo "🎉 CREDENTIAL TEST SUCCESS! 🎉"
            echo "✅ Azure Service Principal is working correctly"
            echo "🔑 All Azure services are accessible"
            echo "🚀 Ready for production pipelines!"
        }
        
        failure {
            echo "🚨 CREDENTIAL TEST FAILED! 🚨"
            echo "❌ Check Azure Service Principal configuration"
            echo "🔍 Verify credentials in Jenkins"
            echo "📋 Review console output for specific errors"
        }
    }
} 