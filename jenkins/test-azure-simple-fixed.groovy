/**
 * Simple Azure CLI Plugin Test - Basic Syntax
 * 
 * Purpose: Test Azure Service Principal credential with minimal configuration
 * This uses the simplest possible azureCLI plugin syntax
 */

pipeline {
    agent any
    
    environment {
        ACR_NAME = 'acrecommerce69'
        AZURE_RESOURCE_GROUP = 'AKS-ecommerce'
        AKS_CLUSTER_NAME = 'KubeCluster'
    }
    
    stages {
        stage('🔍 Test Azure CLI Installation') {
            steps {
                script {
                    echo "🔧 Testing Azure CLI installation..."
                    
                    sh '''
                        echo "=== Azure CLI Version ==="
                        az version
                        
                        echo "=== Environment Check ==="
                        env | grep -i azure || echo "No Azure env vars found"
                    '''
                    
                    echo "✅ Azure CLI is installed!"
                }
            }
        }
        
        stage('🔑 Test Azure Authentication - Minimal') {
            steps {
                script {
                    echo "🔑 Testing Azure Service Principal authentication with minimal configuration..."
                    
                    // Use the most basic azureCLI syntax possible
                    azureCLI(
                        principalCredentialId: 'azure-credentials',
                        commands: [
                            [script: 'az account show']
                        ]
                    )
                    
                    echo "✅ Basic Azure authentication successful!"
                }
            }
        }
        
        stage('🔑 Test Azure Commands') {
            steps {
                script {
                    echo "🔑 Testing basic Azure commands..."
                    
                    azureCLI(
                        principalCredentialId: 'azure-credentials',
                        commands: [
                            [script: 'echo "=== Current Account ==="'],
                            [script: 'az account show --output table'],
                            [script: 'echo "=== Resource Groups ==="'],
                            [script: 'az group list --output table']
                        ]
                    )
                    
                    echo "✅ Azure commands successful!"
                }
            }
        }
        
        stage('🐳 Test ACR Login') {
            steps {
                script {
                    echo "🐳 Testing ACR login..."
                    
                    azureCLI(
                        principalCredentialId: 'azure-credentials',
                        commands: [
                            [script: "az acr show --name ${ACR_NAME} --output table"],
                            [script: "az acr login --name ${ACR_NAME}"]
                        ]
                    )
                    
                    echo "✅ ACR login successful!"
                }
            }
        }
    }
    
    post {
        success {
            echo "🎉 AZURE CLI PLUGIN TEST PASSED! 🎉"
            echo "✅ Azure Service Principal credential is working correctly"
            echo "✅ Azure CLI plugin is functioning properly"
            echo "🚀 Ready to use azureCLI plugin in production pipelines!"
        }
        
        failure {
            echo "🚨 AZURE CLI PLUGIN TEST FAILED! 🚨"
            echo "❌ Check the console output above for specific errors"
            echo "🔍 Next steps:"
            echo "   1. Verify the Azure Service Principal credential was created correctly"
            echo "   2. Check that the credential ID is exactly 'azure-credentials'"
            echo "   3. Ensure Azure Environment is set to 'Azure'"
            echo "   4. Verify all credential fields are filled correctly"
        }
        
        always {
            echo "🧹 Test completed"
        }
    }
} 