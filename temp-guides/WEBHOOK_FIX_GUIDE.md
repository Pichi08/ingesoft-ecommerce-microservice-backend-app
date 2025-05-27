# GitHub Webhook 403 Error Fix Guide

## 🚨 Problem
GitHub webhook delivery showing "Invalid HTTP Response: 403"

## 🔍 Root Causes & Solutions

### **Solution 1: Fix Jenkins CSRF Protection (Most Common)**

Jenkins has CSRF protection enabled by default, which blocks webhooks.

#### **Option A: Disable CSRF for GitHub Webhooks (Recommended)**
1. Go to **Manage Jenkins** → **Security**
2. Find **CSRF Protection** section
3. Add `/github-webhook/` to **Excluded URLs**
4. Click **Save**

#### **Option B: Use GitHub Plugin Webhook URL**
Instead of `/github-webhook/`, use the GitHub plugin's specific endpoint:
```
Webhook URL: https://YOUR_NGROK_URL.ngrok-free.app/github-webhook/
```

### **Solution 2: Fix GitHub Webhook Configuration**

Update your GitHub webhook settings:

1. **Go to GitHub Repository** → **Settings** → **Webhooks**
2. **Edit your webhook**
3. **Update configuration**:
   ```
   Payload URL: https://YOUR_NGROK_URL.ngrok-free.app/github-webhook/
   Content type: application/json
   Secret: (leave empty)
   SSL verification: ✅ Enable SSL verification
   Events: ✅ Just the push event
   Active: ✅ Active
   ```

### **Solution 3: Check Jenkins Authentication**

1. **Go to Manage Jenkins** → **Security**
2. **Authorization Strategy** should allow anonymous read access, OR
3. **Create API token** for webhook authentication

### **Solution 4: Alternative Webhook Endpoint**

If still having issues, try using the generic trigger endpoint:

1. **Install Generic Webhook Trigger Plugin**
2. **Use URL**: `https://YOUR_NGROK_URL.ngrok-free.app/generic-webhook-trigger/invoke`
3. **Configure in pipeline**:
   ```groovy
   triggers {
       GenericTrigger(
           genericVariables: [
               [key: 'ref', value: '$.ref'],
               [key: 'repository', value: '$.repository.name']
           ],
           causeString: 'Triggered by GitHub webhook',
           regexpFilterExpression: 'refs/heads/develop',
           regexpFilterText: '$ref'
       )
   }
   ```

## 🧪 Testing Solutions

### **Test 1: Manual Webhook Test**
```bash
# Test with proper GitHub headers
curl -X POST https://YOUR_NGROK_URL.ngrok-free.app/github-webhook/ \
  -H "Content-Type: application/json" \
  -H "X-GitHub-Event: push" \
  -H "X-GitHub-Delivery: 12345" \
  -d '{
    "ref": "refs/heads/develop",
    "repository": {
      "name": "your-repo"
    }
  }'
```

### **Test 2: Check ngrok Logs**
```bash
# In ngrok terminal, check for incoming requests
# Should see 200 OK responses, not 403
```

### **Test 3: Jenkins System Log**
1. **Manage Jenkins** → **System Log**
2. **Add new log recorder** for `com.cloudbees.jenkins.GitHubPushTrigger`
3. **Check logs** when webhook triggers

## ⚡ Quick Fix (Most Likely Solution)

**The most common cause is CSRF protection. Try this first:**

1. **Manage Jenkins** → **Security**
2. **Find "CSRF Protection"**
3. **Add to "Excluded URLs"**: `/github-webhook/`
4. **Save**
5. **Test webhook again**

## 🔧 Alternative: Use Polling Instead

If webhooks continue to fail, switch to polling:

1. **Edit your Jenkins job**
2. **Uncheck** "GitHub hook trigger for GITScm polling"
3. **Check** "Poll SCM"
4. **Set schedule**: `H/2 * * * *` (every 2 minutes)
5. **Save**

This will poll GitHub every 2 minutes for changes instead of using webhooks.

## ✅ Verification Steps

After applying fixes:

1. **Push to develop branch**
2. **Check GitHub webhook deliveries** (should show 200 OK)
3. **Check Jenkins job** (should trigger automatically)
4. **Check ngrok logs** (should show successful requests)

## 📞 Still Having Issues?

If none of these work:
1. Share your ngrok URL format
2. Share Jenkins security configuration
3. Share exact GitHub webhook settings
4. Check if Jenkins is behind any firewall/proxy 