/**
 * Shared deployment functions for E-commerce microservices
 * These functions handle Kubernetes deployments and health checks
 */

/**
 * Login to Azure and get AKS credentials
 * @param resourceGroup Azure resource group
 * @param clusterName AKS cluster name
 */
def setupAzureConnection(String resourceGroup, String clusterName) {
    echo "🔑 Setting up Azure connection..."
    
    withCredentials([azureServicePrincipal('azure-credentials')]) {
        sh """
            az login --service-principal -u \$AZURE_CLIENT_ID -p \$AZURE_CLIENT_SECRET --tenant \$AZURE_TENANT_ID
            az aks get-credentials --resource-group ${resourceGroup} --name ${clusterName} --overwrite-existing
        """
    }
    
    echo "✅ Azure connection established"
}

/**
 * Login to Azure Container Registry
 * @param acrName Azure Container Registry name
 */
def loginToACR(String acrName) {
    echo "🐳 Logging into Azure Container Registry..."
    
    sh "az acr login --name ${acrName}"
    
    echo "✅ ACR login successful"
}

/**
 * Deploy microservice to Kubernetes
 * @param serviceName Name of the microservice
 * @param namespace Kubernetes namespace
 * @param imageTag Docker image tag to deploy
 * @param environment Environment (dev/stage/prod)
 */
def deployToKubernetes(String serviceName, String namespace, String imageTag, String environment) {
    echo "🚀 Deploying ${serviceName} to ${environment} environment..."
    
    // Update image tag in deployment
    sh """
        kubectl set image deployment/${serviceName} ${serviceName}=acrecommerce69.azurecr.io/${serviceName}:${imageTag} -n ${namespace}
        kubectl rollout status deployment/${serviceName} -n ${namespace} --timeout=300s
    """
    
    echo "✅ ${serviceName} deployed successfully to ${environment}"
}

/**
 * Create or update Kubernetes namespace
 * @param namespace Namespace name
 */
def ensureNamespace(String namespace) {
    echo "📁 Ensuring namespace ${namespace} exists..."
    
    sh """
        kubectl create namespace ${namespace} --dry-run=client -o yaml | kubectl apply -f -
    """
    
    echo "✅ Namespace ${namespace} ready"
}

/**
 * Apply Kubernetes configuration files
 * @param serviceName Name of the microservice
 * @param environment Environment (dev/stage/prod)
 */
def applyKubernetesConfig(String serviceName, String environment) {
    echo "⚙️ Applying Kubernetes configuration for ${serviceName}..."
    
    def configPath = "k8s/services/${serviceName}/"
    
    if (fileExists(configPath)) {
        sh "kubectl apply -f ${configPath}"
        echo "✅ Kubernetes configuration applied for ${serviceName}"
    } else {
        echo "⚠️ No Kubernetes configuration found at ${configPath}"
    }
}

/**
 * Wait for service to be ready
 * @param serviceName Name of the microservice
 * @param namespace Kubernetes namespace
 * @param timeoutSeconds Timeout in seconds (default: 300)
 */
def waitForServiceReady(String serviceName, String namespace, int timeoutSeconds = 300) {
    echo "⏳ Waiting for ${serviceName} to be ready..."
    
    sh """
        kubectl wait --for=condition=ready pod -l app=${serviceName} -n ${namespace} --timeout=${timeoutSeconds}s
    """
    
    echo "✅ ${serviceName} is ready"
}

/**
 * Run health check on deployed service
 * @param serviceName Name of the microservice
 * @param namespace Kubernetes namespace
 * @param port Service port
 * @return true if healthy, false otherwise
 */
def healthCheck(String serviceName, String namespace, int port = 8080) {
    echo "🩺 Running health check for ${serviceName}..."
    
    try {
        // Port forward to access the service
        sh """
            timeout 30 kubectl port-forward svc/${serviceName} ${port}:${port} -n ${namespace} &
            sleep 5
            curl -f http://localhost:${port}/actuator/health || exit 1
        """
        
        echo "✅ Health check passed for ${serviceName}"
        return true
    } catch (Exception e) {
        echo "❌ Health check failed for ${serviceName}: ${e.message}"
        return false
    }
}

/**
 * Run smoke tests after deployment
 * @param serviceName Name of the microservice
 * @param namespace Kubernetes namespace
 * @param environment Environment name
 */
def runSmokeTests(String serviceName, String namespace, String environment) {
    echo "💨 Running smoke tests for ${serviceName} in ${environment}..."
    
    try {
        // Basic connectivity test
        sh """
            kubectl get pods -l app=${serviceName} -n ${namespace}
            kubectl get svc ${serviceName} -n ${namespace}
        """
        
        // Service-specific smoke tests
        if (serviceName == 'user-service') {
            // Test user service endpoints
            echo "Testing user service endpoints..."
            // Add specific user service tests here
        }
        
        echo "✅ Smoke tests passed for ${serviceName}"
    } catch (Exception e) {
        echo "❌ Smoke tests failed for ${serviceName}: ${e.message}"
        throw e
    }
}

/**
 * Rollback deployment if needed
 * @param serviceName Name of the microservice
 * @param namespace Kubernetes namespace
 */
def rollbackDeployment(String serviceName, String namespace) {
    echo "🔄 Rolling back ${serviceName} deployment..."
    
    sh """
        kubectl rollout undo deployment/${serviceName} -n ${namespace}
        kubectl rollout status deployment/${serviceName} -n ${namespace} --timeout=300s
    """
    
    echo "✅ Rollback completed for ${serviceName}"
}

/**
 * Get service external IP or URL
 * @param serviceName Name of the microservice
 * @param namespace Kubernetes namespace
 * @return Service URL or IP
 */
def getServiceUrl(String serviceName, String namespace) {
    def serviceUrl = sh(
        script: "kubectl get svc ${serviceName} -n ${namespace} -o jsonpath='{.status.loadBalancer.ingress[0].ip}:{.spec.ports[0].port}'",
        returnStdout: true
    ).trim()
    
    if (serviceUrl && !serviceUrl.startsWith(':')) {
        return "http://${serviceUrl}"
    } else {
        return "Service URL not available (LoadBalancer pending)"
    }
}

/**
 * Clean up old deployments
 * @param serviceName Name of the microservice
 * @param namespace Kubernetes namespace
 * @param keepReleases Number of releases to keep (default: 3)
 */
def cleanupOldDeployments(String serviceName, String namespace, int keepReleases = 3) {
    echo "🧹 Cleaning up old deployments for ${serviceName}..."
    
    sh """
        kubectl get replicaset -n ${namespace} -o name | grep ${serviceName} | sort -r | tail -n +${keepReleases + 1} | xargs -r kubectl delete -n ${namespace}
    """
    
    echo "✅ Cleanup completed for ${serviceName}"
}

return this 