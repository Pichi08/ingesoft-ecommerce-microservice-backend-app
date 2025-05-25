# Jenkins Setup Guide for E-commerce Microservices

This guide provides two approaches for setting up Jenkins CI/CD pipelines:
1. **Plugin-based approach** (Recommended for production)
2. **Direct tool installation approach** (Minimal setup)

## Approach 1: Plugin-Based Setup (Recommended)

### Prerequisites
- Docker installed and running
- Azure CLI installed locally
- kubectl installed locally
- Access to Azure subscription with AKS and ACR

### Step 1: Start Jenkins with Java 11

```bash
# Pull and run Jenkins with Java 11
docker run -d \
  --name jenkins \
  -p 8080:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts-jdk11

# Get initial admin password
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

### Step 2: Initial Jenkins Setup
1. Open http://localhost:8080
2. Enter the initial admin password
3. **Install suggested plugins** (or select plugins to install)
4. Create admin user
5. Configure Jenkins URL: `http://localhost:8080`

### Step 3: Install Required Plugins

#### 3.1 Access Plugin Manager
- **Manage Jenkins** → **Plugins** → **Available plugins**

#### 3.2 Install These Plugins (Search and Install Each):

**Essential Pipeline Plugins:**
- ✅ **Pipeline** (should already be installed)
- ✅ **Pipeline: Stage View**
- ✅ **JUnit**
- ✅ **HTML Publisher**
- ✅ **Timestamper**
- ✅ **Workspace Cleanup**

**Tool Integration Plugins:**
- ✅ **Maven Integration**
- ✅ **Azure CLI**
- ✅ **Kubernetes**
- ✅ **Docker Pipeline**

**Additional Useful Plugins:**
- ✅ **Build Timeout**
- ✅ **Pipeline: Multibranch**
- ✅ **Git**
- ✅ **GitHub**

#### 3.3 Restart Jenkins
After installation: **Manage Jenkins** → **Restart Jenkins when no jobs are running**

### Step 4: Configure Global Tools

#### 4.1 Configure Maven
1. **Manage Jenkins** → **Tools**
2. **Maven installations** → **Add Maven**
   - **Name**: `Maven-3.9`
   - **Install automatically**: ✅ **Checked**
   - **Version**: Select latest Maven 3.9.x

#### 4.2 Configure JDK
1. **JDK installations** → **Add JDK**
   - **Name**: `Java-11`
   - **Install automatically**: ✅ **Checked**
   - **Add Installer** → **Install from Adoptium.net**
   - **Version**: Select **jdk-11.0.x+y** (latest available)

#### 4.3 Configure Docker
1. **Docker installations** → **Add Docker**
   - **Name**: `Docker`
   - **Install automatically**: ✅ **Checked**
   - **Add Installer** → **Download from docker.com**
   - **Docker version**: Select latest

**Save** the configuration.

### Step 5: Set Up Credentials

#### 5.1 Azure Service Principal Credentials
1. **Manage Jenkins** → **Credentials** → **System** → **Global credentials**
2. **Add Credentials** → **Azure Service Principal**
   - **ID**: `azure-credentials`
   - **Description**: `Azure Service Principal for AKS and ACR`
   - **Subscription ID**: Your Azure subscription ID
   - **Client ID**: Your service principal client ID
   - **Client Secret**: Your service principal secret
   - **Tenant ID**: Your Azure tenant ID

#### 5.2 Azure Container Registry Credentials
1. **Add Credentials** → **Username with password**
   - **ID**: `acr-credentials`
   - **Description**: `Azure Container Registry Credentials`
   - **Username**: Your ACR username (or service principal client ID)
   - **Password**: Your ACR password (or service principal secret)

#### 5.3 Kubernetes Config (Optional)
1. **Add Credentials** → **Secret file**
   - **ID**: `k8s-staging-config`
   - **Description**: `Kubernetes config for staging`
   - **File**: Upload your kubeconfig file

### Step 6: Create Pipeline Jobs

#### 6.1 Create Staging Pipeline
1. **New Item** → **Pipeline** → Name: `user-service-staging`
2. **Pipeline** section:
   - **Definition**: `Pipeline script from SCM`
   - **SCM**: `Git`
   - **Repository URL**: Your repository URL
   - **Credentials**: Select your Git credentials
   - **Branch Specifier**: `*/develop`
   - **Script Path**: `jenkins/microservices/user-service/Jenkinsfile-stage-plugins`

#### 6.2 Create Multibranch Pipeline (Recommended)
1. **New Item** → **Multibranch Pipeline** → Name: `user-service-pipelines`
2. **Branch Sources** → **Git**
   - **Project Repository**: Your repository URL
   - **Credentials**: Select your Git credentials
   - **Discover branches**: `All branches`
3. **Build Configuration**:
   - **Mode**: `by Jenkinsfile`
   - **Script Path**: `jenkins/microservices/user-service/Jenkinsfile-stage-plugins`

### Step 7: Test the Pipeline

1. **Trigger a build** manually or push to the `develop` branch
2. **Monitor the build** in Jenkins console output
3. **Check the stages** in the Stage View
4. **Review test results** and artifacts

---

## Approach 2: Direct Tool Installation (Minimal Setup)

### Prerequisites
- Same as above

### Step 1: Start Jenkins (Minimal Plugins)

```bash
# Pull and run Jenkins with Java 11
docker run -d \
  --name jenkins \
  -p 8080:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts-jdk11
```

### Step 2: Minimal Plugin Installation
Install only these essential plugins:
- ✅ **Pipeline**
- ✅ **JUnit**
- ✅ **HTML Publisher**
- ✅ **Timestamper**

### Step 3: No Global Tools Configuration Required
The pipeline will install tools directly during execution.

### Step 4: Set Up Basic Credentials
Only need:
- Azure Service Principal credentials (`azure-credentials`)

### Step 5: Use Direct Installation Pipeline
Use the pipeline: `jenkins/microservices/user-service/Jenkinsfile-stage`

---

## Comparison: Plugin-Based vs Direct Installation

| Feature | Plugin-Based | Direct Installation |
|---------|-------------|-------------------|
| **Setup Complexity** | Higher | Lower |
| **Plugin Dependencies** | Many | Minimal |
| **Tool Management** | Jenkins manages | Pipeline manages |
| **Performance** | Faster (cached tools) | Slower (downloads each time) |
| **Reliability** | Higher | Lower |
| **Production Ready** | ✅ Yes | ⚠️ Limited |
| **Learning Curve** | Steeper | Gentler |

## Recommendation

- **For Learning**: Start with Direct Installation approach
- **For Production**: Use Plugin-Based approach
- **For Teams**: Use Plugin-Based approach with proper tool management

---

## Troubleshooting

### Common Issues

#### 1. Plugin Installation Fails
```bash
# Restart Jenkins container
docker restart jenkins

# Check Jenkins logs
docker logs jenkins
```

#### 2. Tool Configuration Issues
- Verify Global Tools configuration
- Check tool installation logs in build console
- Ensure proper permissions

#### 3. Azure Authentication Issues
- Verify service principal credentials
- Check Azure subscription permissions
- Test Azure CLI login manually

#### 4. Kubernetes Connection Issues
- Verify kubeconfig file
- Check AKS cluster status
- Test kubectl connection manually

### Getting Help

1. **Jenkins Logs**: `docker logs jenkins`
2. **Build Console Output**: Check individual build logs
3. **Plugin Documentation**: Visit Jenkins plugin pages
4. **Azure Documentation**: Check Azure CLI and AKS docs

---

## Next Steps

1. **Set up additional microservice pipelines**
2. **Configure production pipelines**
3. **Add notification integrations** (Slack, Teams)
4. **Set up monitoring and alerting**
5. **Implement security scanning**

## Step 1: Configure Jenkins Credentials

### 1.1 Azure Service Principal
1. Go to Jenkins → Manage Jenkins → Credentials → System → Global credentials
2. Click "Add Credentials"
3. Select "Azure Service Principal"
4. Add your Azure credentials:
   - **ID**: `azure-credentials`
   - **Subscription ID**: Your Azure subscription ID
   - **Client ID**: Your service principal client ID
   - **Client Secret**: Your service principal secret
   - **Tenant ID**: Your Azure tenant ID

### 1.2 Azure Container Registry (Optional)
1. Add Credentials → Username with password
2. Set:
   - **ID**: `acr-credentials`
   - **Username**: Your ACR username
   - **Password**: Your ACR password

## Step 2: Configure Global Environment Variables

1. Go to Jenkins → Manage Jenkins → System
2. Find "Global properties" → "Environment variables"
3. Add these variables:

```
AZURE_RESOURCE_GROUP = AKS-ecommerce
AKS_CLUSTER_NAME = KubeCluster
ACR_NAME = acrecommerce69
```

## Step 3: Create Pipeline Jobs

### 3.1 Infrastructure Pipeline

1. New Item → Pipeline → Name: `infrastructure-deployment`
2. Pipeline Configuration:
   - **Definition**: Pipeline script from SCM
   - **SCM**: Git
   - **Repository URL**: Your repository URL
   - **Script Path**: `jenkins/infrastructure/Jenkinsfile-infrastructure`
3. Build Triggers: Manual (for safety)
4. Save

### 3.2 User Service Pipelines

#### Development Pipeline
1. New Item → Multibranch Pipeline → Name: `user-service-dev`
2. Branch Sources → Git:
   - **Repository URL**: Your repository URL
   - **Discover branches**: All branches except main/develop
3. Build Configuration:
   - **Script Path**: `jenkins/microservices/user-service/Jenkinsfile-dev`
4. Save

#### Staging Pipeline  
1. New Item → Pipeline → Name: `user-service-staging`
2. Pipeline Configuration:
   - **Definition**: Pipeline script from SCM
   - **SCM**: Git
   - **Branches**: `develop`
   - **Script Path**: `jenkins/microservices/user-service/Jenkinsfile-stage`
3. Build Triggers: GitHub hook trigger for GITScm polling
4. Save

#### Production Pipeline
1. New Item → Pipeline → Name: `user-service-production`
2. Pipeline Configuration:
   - **Definition**: Pipeline script from SCM
   - **SCM**: Git  
   - **Branches**: `main` or `master`
   - **Script Path**: `jenkins/microservices/user-service/Jenkinsfile-prod`
3. Build Triggers: Manual (for safety)
4. Save

## Step 4: Configure Branch Protection (Recommended)

### Git Branch Strategy
```
main/master     → Production deployments (manual approval)
develop         → Staging deployments (automatic)
feature/*       → Development builds (automatic)
hotfix/*        → Development builds (automatic)
```

## Step 5: Test Your Setup

### 5.1 Test Infrastructure Pipeline
1. Run the `infrastructure-deployment` job first
2. Verify all infrastructure services are deployed
3. Check the generated report

### 5.2 Test Development Pipeline
1. Create a feature branch: `git checkout -b feature/test-pipeline`
2. Make a small change to user-service
3. Push the branch
4. Verify the development pipeline runs automatically

### 5.3 Test Staging Pipeline
1. Merge your feature branch to `develop`
2. Verify the staging pipeline runs automatically
3. Check the staging environment

### 5.4 Test Production Pipeline
1. Merge `develop` to `main`
2. Run the production pipeline manually
3. Approve the production deployment when prompted

## Step 6: Pipeline Workflow

### Normal Development Flow:
```
1. Developer creates feature branch
   └── Triggers: user-service-dev pipeline
   
2. Feature branch merged to develop  
   └── Triggers: user-service-staging pipeline
   
3. Develop branch merged to main
   └── Triggers: user-service-production pipeline (manual approval)
```

## Step 7: Monitoring and Maintenance

### 7.1 Set up Build Notifications
Add Slack/Teams/Email notifications in the `post` sections of pipelines:

```groovy
post {
    success {
        slackSend channel: '#deployments', 
                  color: 'good',
                  message: "✅ ${SERVICE_NAME} deployed successfully to ${environment}"
    }
    failure {
        slackSend channel: '#deployments',
                  color: 'danger', 
                  message: "❌ ${SERVICE_NAME} deployment failed in ${environment}"
    }
}
```

### 7.2 Pipeline Monitoring
- Monitor build success rates
- Set up alerts for failed deployments
- Review test reports regularly

### 7.3 Security Considerations
- Rotate Azure credentials regularly
- Use separate service principals for different environments
- Implement least-privilege access for Jenkins

## Step 8: Extending to Other Microservices

To add pipelines for other microservices (product-service, order-service, etc.):

1. Copy the user-service pipeline files:
```bash
cp -r jenkins/microservices/user-service jenkins/microservices/product-service
```

2. Update the environment variables in each Jenkinsfile:
```groovy
environment {
    SERVICE_NAME = 'product-service'  // Change this
    SERVICE_PORT = '8800'             // Change this
    // ... other variables
}
```

3. Create the Jenkins jobs following the same pattern as user-service

## Troubleshooting

### Common Issues:

1. **Azure Authentication Fails**
   - Verify service principal has correct permissions
   - Check credential configuration in Jenkins

2. **Kubernetes Connection Issues**  
   - Ensure kubectl is configured correctly
   - Verify AKS cluster is running

3. **Docker Build Fails**
   - Check Docker daemon is running on Jenkins agents
   - Verify Dockerfile syntax

4. **Pipeline Doesn't Load Shared Functions**
   - Ensure shared function files are in the correct path
   - Check Git repository structure

### Debug Commands:
```bash
# Check Jenkins agent capabilities
kubectl version --client
docker version
az version

# Check AKS connectivity
az aks get-credentials --resource-group AKS-ecommerce --name KubeCluster
kubectl cluster-info

# Check ACR access
az acr login --name acrecommerce69
```

## Next Steps

1. Set up additional microservice pipelines
2. Implement advanced testing (performance, security)
3. Add monitoring and alerting
4. Configure automated rollbacks
5. Implement GitOps workflows

## Support

For issues with this pipeline setup:
1. Check Jenkins console logs
2. Review Kubernetes events: `kubectl get events --sort-by=.metadata.creationTimestamp`
3. Check Azure resources status in Azure Portal

---

**Happy Deployments!** 🚀 