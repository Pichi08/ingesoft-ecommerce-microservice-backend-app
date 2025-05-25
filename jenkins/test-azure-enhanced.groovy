/**
 * Enhanced Azure CLI Plugin Test Pipeline
 * 
 * Purpose: Test azureCLI plugin with enhanced configuration
 * This addresses potential plugin configuration issues
 */

pipeline {
    agent any
    
    environment {
        ACR_NAME = 'acrecommerce69'
        AZURE_RESOURCE_GROUP = 'AKS-ecommerce'
        AKS_CLUSTER_NAME = 'KubeCluster'
        // Explicitly set Azure environment variables
        AZURE_CONFIG_DIR = '/tmp/.azure'
    }
    
    stages {
        stage('🔍 Test Azure CLI Installation') {
            steps {
                script {
                    echo "🔧 Testing Azure CLI installation..."
                    
                    sh '''
                        echo "=== Azure CLI Version ==="
                        az version
                        
                        echo "=== Azure CLI Configuration ==="
                        az config list || echo "No config found"
                        
                        echo "=== Environment Variables ==="
                        env | grep -i azure || echo "No Azure env vars"
                    '''
                    
                    echo "✅ All tools are installed correctly!"
                }
            }
        }
        
        stage('🔑 Test Azure Authentication - Enhanced') {
            steps {
                script {
                    echo "🔑 Testing Azure Service Principal authentication with enhanced configuration..."
                    
                    // Use azureCLI with explicit configuration
                    azureCLI(
                        commands: [
                            [script: 'echo "=== Azure CLI Version ==="'],
                            [script: 'az version --output table'],
                            [script: 'echo "=== Current Account ==="'],
                            [script: 'az account show --output table'],
                            [script: 'echo "=== Subscription Info ==="'],
                            [script: 'az account list --output table'],
                            [script: 'echo "=== Resource Groups ==="'],
                            [script: 'az group list --output table']
                        ],
                        principalCredentialId: 'azure-credentials',
                        // Additional configuration parameters
                        subscriptionIdVariable: 'AZURE_SUBSCRIPTION_ID',
                        tenantIdVariable: 'AZURE_TENANT_ID',
                        clientIdVariable: 'AZURE_CLIENT_ID',
                        clientSecretVariable: 'AZURE_CLIENT_SECRET'
                    )
                    
                    echo "✅ Azure authentication successful!"
                }
            }
        }
        
        stage('🔑 Test Azure Authentication - Basic') {
            steps {
                script {
                    echo "🔑 Testing basic azureCLI configuration..."
                    
                    // Try basic azureCLI without extra parameters
                    azureCLI(
                        commands: [
                            [script: 'az account show']
                        ],
                        principalCredentialId: 'azure-credentials'
                    )
                    
                    echo "✅ Basic Azure authentication successful!"
                }
            }
        }
        
        stage('🔑 Test Azure Authentication - With Subscription') {
            steps {
                script {
                    echo "🔑 Testing azureCLI with explicit subscription..."
                    
                    // Try with explicit subscription
                    azureCLI(
                        commands: [
                            [script: 'az account set --subscription 546dce4b-aa5f-43ee-87dd-8e4116c49bf1'],
                            [script: 'az account show'],
                            [script: 'az group list --output table']
                        ],
                        principalCredentialId: 'azure-credentials'
                    )
                    
                    echo "✅ Subscription-specific authentication successful!"
                }
            }
        }
        
        stage('🐳 Test ACR Access') {
            steps {
                script {
                    echo "🔑 Testing Azure Container Registry access..."
                    
                    azureCLI(
                        commands: [
                            [script: 'echo "=== ACR Information ==="'],
                            [script: "az acr show --name ${ACR_NAME} --output table"],
                            [script: 'echo "=== ACR Login ==="'],
                            [script: "az acr login --name ${ACR_NAME}"],
                            [script: 'echo "=== ACR Repository List ==="'],
                            [script: "az acr repository list --name ${ACR_NAME} --output table || echo 'No repositories found'"]
                        ],
                        principalCredentialId: 'azure-credentials'
                    )
                    
                    echo "✅ ACR access successful!"
                }
            }
        }
        
        stage('☸️ Test AKS Access') {
            steps {
                script {
                    echo "🔑 Testing AKS cluster access..."
                    
                    azureCLI(
                        commands: [
                            [script: 'echo "=== AKS Cluster Information ==="'],
                            [script: "az aks show --resource-group ${AZURE_RESOURCE_GROUP} --name ${AKS_CLUSTER_NAME} --output table"],
                            [script: 'echo "=== Getting AKS Credentials ==="'],
                            [script: "az aks get-credentials --resource-group ${AZURE_RESOURCE_GROUP} --name ${AKS_CLUSTER_NAME} --overwrite-existing"],
                            [script: 'echo "=== Cluster Info ==="'],
                            [script: 'kubectl cluster-info'],
                            [script: 'echo "=== Cluster Nodes ==="'],
                            [script: 'kubectl get nodes']
                        ],
                        principalCredentialId: 'azure-credentials'
                    )
                    
                    echo "✅ AKS access successful!"
                }
            }
        }
        
        stage('🧪 Test Docker + ACR Integration') {
            steps {
                script {
                    echo "🐳 Testing Docker + ACR integration..."
                    
                    azureCLI(
                        commands: [
                            [script: 'echo "=== ACR Login for Docker ==="'],
                            [script: "az acr login --name ${ACR_NAME}"],
                            [script: 'echo "=== Test Docker Build ==="'],
                            [script: 'echo "FROM alpine:latest" > Dockerfile.test'],
                            [script: 'echo "RUN echo \'Hello from Jenkins!\'" >> Dockerfile.test'],
                            [script: "docker build -t ${ACR_NAME}.azurecr.io/test:jenkins-test -f Dockerfile.test ."],
                            [script: 'echo "=== Test Docker Push ==="'],
                            [script: "docker push ${ACR_NAME}.azurecr.io/test:jenkins-test"],
                            [script: 'echo "=== Cleanup ==="'],
                            [script: "docker rmi ${ACR_NAME}.azurecr.io/test:jenkins-test || true"],
                            [script: 'rm -f Dockerfile.test']
                        ],
                        principalCredentialId: 'azure-credentials'
                    )
                    
                    echo "✅ Docker + ACR integration successful!"
                }
            }
        }
    }
    
    post {
        success {
            echo "🎉 AZURE CLI PLUGIN TESTS PASSED! 🎉"
            echo "✅ Azure CLI Plugin: Working"
            echo "✅ Service Principal: Working"
            echo "✅ ACR Access: Working"
            echo "✅ AKS Access: Working"
            echo "✅ Docker Integration: Working"
            echo "🚀 Ready for production pipelines with azureCLI plugin!"
        }
        
        failure {
            echo "🚨 AZURE CLI PLUGIN TESTS FAILED! 🚨"
            echo "❌ Check the console output above for specific errors"
            echo "🔍 Debugging steps:"
            echo "   1. Verify Azure Service Principal credential configuration"
            echo "   2. Check Azure Environment is set to 'Azure'"
            echo "   3. Ensure all credential fields are properly filled"
            echo "   4. Verify Azure CLI plugin version compatibility"
        }
        
        always {
            // Cleanup
            sh '''
                rm -f Dockerfile.test || true
                docker rmi ${ACR_NAME}.azurecr.io/test:jenkins-test || true
            '''
        }
    }
} 