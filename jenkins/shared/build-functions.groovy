/**
 * Shared build functions for E-commerce microservices
 * These functions can be used across all microservice pipelines
 */

/**
 * Build Maven project with tests
 * @param serviceName Name of the microservice
 * @param skipTests Whether to skip tests (default: false)
 */
def buildMavenProject(String serviceName, boolean skipTests = false) {
    dir(serviceName) {
        echo "Building ${serviceName} with Maven"
        
        if (skipTests) {
            sh './mvnw clean package -DskipTests'
        } else {
            sh './mvnw clean test package'
        }
        
        echo "${serviceName} build completed successfully"
    }
}

/**
 * Run unit tests for Maven project
 * @param serviceName Name of the microservice
 */
def runUnitTests(String serviceName) {
    dir(serviceName) {
        echo "Running unit tests for ${serviceName}"
        
        sh './mvnw test'
        
        echo "Unit tests completed for ${serviceName}"
    }
}

/**
 * Run integration tests for Maven project
 * @param serviceName Name of the microservice
 */
def runIntegrationTests(String serviceName) {
    dir(serviceName) {
        echo "Running integration tests for ${serviceName}"
        
        sh './mvnw verify -Dspring.profiles.active=test'
        
        echo "Integration tests completed for ${serviceName}"
    }
}

/**
 * Build Docker image
 * @param serviceName Name of the microservice
 * @param imageTag Tag for the Docker image
 * @param acrName Azure Container Registry name
 */
def buildDockerImage(String serviceName, String imageTag, String acrName) {
    echo "Building Docker image for ${serviceName}"
    
    sh """
        docker build -t ${acrName}.azurecr.io/${serviceName}:${imageTag} \\
            -f ${serviceName}/Dockerfile .
    """
    
    echo "Docker image built: ${acrName}.azurecr.io/${serviceName}:${imageTag}"
}

/**
 * Push Docker image to Azure Container Registry
 * @param serviceName Name of the microservice  
 * @param imageTag Tag for the Docker image
 * @param acrName Azure Container Registry name
 */
def pushDockerImage(String serviceName, String imageTag, String acrName) {
    echo "Pushing Docker image for ${serviceName}"
    
    sh "docker push ${acrName}.azurecr.io/${serviceName}:${imageTag}"
    
    echo "Docker image pushed: ${acrName}.azurecr.io/${serviceName}:${imageTag}"
}

/**
 * Generate version tag based on branch and build number
 * @param branchName Current branch name
 * @param buildNumber Jenkins build number
 * @return Generated version tag
 */
def generateVersionTag(String branchName, String buildNumber) {
    def cleanBranch = branchName.replaceAll(/[^a-zA-Z0-9]/, '-').toLowerCase()
    
    if (branchName == 'main' || branchName == 'master') {
        return "v1.0.${buildNumber}"
    } else if (branchName == 'develop') {
        return "stage-${buildNumber}"
    } else {
        return "${cleanBranch}-${buildNumber}"
    }
}

/**
 * Check if Docker image already exists in ACR
 * @param serviceName Name of the microservice
 * @param imageTag Tag for the Docker image  
 * @param acrName Azure Container Registry name
 * @return true if image exists, false otherwise
 */
def imageExists(String serviceName, String imageTag, String acrName) {
    def exitCode = sh(
        script: "az acr repository show-tags --name ${acrName} --repository ${serviceName} --query \"[?@=='${imageTag}']\" --output tsv",
        returnStatus: true
    )
    return exitCode == 0
}

return this 