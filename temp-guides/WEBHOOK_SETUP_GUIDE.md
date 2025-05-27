# GitHub Webhook Setup Guide for Jenkins

## 🚀 Overview
This guide will help you set up GitHub webhooks to automatically trigger your Jenkins pipeline when you push to the `develop` branch.

## ✅ Pipeline Configuration (Already Done)
Your Jenkinsfile now includes:
- ✅ GitHub webhook trigger: `githubPush()`
- ✅ Branch restriction: Only runs on `develop` branch
- ✅ Automatic build triggering when webhook is received

## 📝 Step-by-Step Setup

### Step 1: Expose Jenkins to the Internet

Since Jenkins is running locally, GitHub needs to reach it. Use ngrok:

```bash
# Open a new terminal and run:
ngrok http 8080

# This will give you a public URL like:
# https://1234-56-78-90-123.ngrok-free.app
```

**⚠️ Keep this terminal open while testing webhooks!**

### Step 2: Configure Jenkins GitHub Plugin

1. **Go to Jenkins Dashboard** → **Manage Jenkins** → **Plugins**
2. **Install GitHub Plugin** (if not already installed):
   - Search for "GitHub"
   - Install "GitHub Plugin"
   - Restart Jenkins if needed

3. **Configure GitHub Plugin**:
   - Go to **Manage Jenkins** → **System**
   - Find **GitHub** section
   - Add your GitHub server URL: `https://api.github.com`
   - Save configuration

### Step 3: Create Jenkins Job

1. **Create New Job**:
   - Dashboard → **New Item**
   - Enter name: `user-service-stage-minikube`
   - Choose **Pipeline**
   - Click **OK**

2. **Configure Pipeline**:
   - **Pipeline Definition**: `Pipeline script from SCM`
   - **SCM**: `Git`
   - **Repository URL**: `https://github.com/YOUR_USERNAME/YOUR_REPO.git`
   - **Branch Specifier**: `*/develop` (or `*/*` to allow all branches)
   - **Script Path**: `jenkins/minikube/user-service/Jenkinsfile-stage-minikube`

3. **Configure GitHub Hook**:
   - Check ✅ **GitHub hook trigger for GITScm polling**
   - Save the job

### Step 4: Set Up GitHub Webhook

1. **Go to Your GitHub Repository**
2. **Click Settings** → **Webhooks** → **Add webhook**
3. **Configure Webhook**:
   ```
   Payload URL: https://YOUR_NGROK_URL.ngrok-free.app/github-webhook/
   Content type: application/json
   Secret: (leave empty for now)
   Events: ✅ Just the push event
   Active: ✅ Checked
   ```

4. **Click Add webhook**

### Step 5: Test the Setup

1. **Make a change** to any file in your repository
2. **Commit and push to develop branch**:
   ```bash
   git checkout develop
   git add .
   git commit -m "test webhook trigger"
   git push origin develop
   ```

3. **Check Jenkins Dashboard** - should see a new build triggered automatically

## 🔧 Alternative Setup (Without ngrok)

If you don't want to use ngrok, you can:

### Option A: Manual Polling
In Jenkins job configuration:
- Uncheck GitHub hook trigger
- Check ✅ **Poll SCM**
- Set schedule: `H/5 * * * *` (every 5 minutes)

### Option B: Use GitHub Actions
Create `.github/workflows/jenkins-trigger.yml`:
```yaml
name: Trigger Jenkins
on:
  push:
    branches: [ develop ]

jobs:
  trigger-jenkins:
    runs-on: ubuntu-latest
    steps:
      - name: Trigger Jenkins Build
        run: |
          curl -X POST "http://YOUR_JENKINS_URL/job/user-service-stage-minikube/build" \
               --user "YOUR_USERNAME:YOUR_API_TOKEN"
```

## 🐛 Troubleshooting

### Webhook Not Triggering
1. **Check ngrok is running** and URL is correct
2. **Check Jenkins logs**: Dashboard → Manage Jenkins → System Log
3. **Check GitHub webhook deliveries**: Repo Settings → Webhooks → Recent Deliveries

### Branch Not Building
1. **Verify branch name** in webhook payload matches `develop`
2. **Check Jenkins console output** for branch restrictions

### Permission Issues
1. **Make sure Jenkins user** has read access to repository
2. **Check GitHub repository visibility** (public vs private)

## 📋 Quick Verification Checklist

- [ ] Jenkins is running on port 8080
- [ ] ngrok is exposing Jenkins (or alternative setup)
- [ ] GitHub plugin is installed in Jenkins
- [ ] Jenkins job is configured with correct repo URL
- [ ] Webhook is added to GitHub repo
- [ ] Pipeline only runs on develop branch
- [ ] Test push to develop triggers build

## 🎯 Expected Flow

1. **Developer pushes** to `develop` branch
2. **GitHub sends webhook** to Jenkins
3. **Jenkins receives webhook** and checks branch
4. **If branch = develop**: Pipeline starts automatically
5. **If branch ≠ develop**: Pipeline skips with "NOT_BUILT" status
6. **Pipeline runs**: Checkout → Build → Test → Package → Docker Image

## 📞 Need Help?

If you encounter issues:
1. Check Jenkins system logs
2. Verify ngrok URL is accessible
3. Test webhook delivery in GitHub
4. Ensure `develop` branch exists and has commits 