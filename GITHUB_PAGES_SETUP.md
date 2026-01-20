# GitHub Pages Setup - Quick Fix

## The Problem
If you're seeing the README.md document instead of the login page, GitHub Pages might not be configured correctly.

## Solution 1: Configure GitHub Pages to Serve from /docs (Recommended)

1. **Go to GitHub Repository Settings:**
   - Visit: https://github.com/NITIN-SINGH-2611/Student-Information-Management-System/settings/pages

2. **Configure Pages:**
   - Under **"Source"**, select:
     - **Branch**: `main`
     - **Folder**: `/docs` (NOT root)
   - Click **"Save"**

3. **Wait 2-3 minutes** for GitHub to rebuild

4. **Test the URL:**
   - Visit: https://nitin-singh-2611.github.io/Student-Information-Management-System/
   - You should see the login page

## Solution 2: If Pages is Set to Root

If GitHub Pages is configured to serve from root (/) instead of /docs:

1. The `index.html` redirect I just added should work
2. Wait 2-3 minutes after the push
3. Clear your browser cache (Ctrl+Shift+Delete)
4. Try the URL again

## Verify Configuration

To check your current GitHub Pages configuration:

1. Go to: https://github.com/NITIN-SINGH-2611/Student-Information-Management-System/settings/pages
2. Look at the "Source" section
3. It should show:
   - Branch: `main`
   - Folder: `/docs`

## If Still Not Working

1. **Clear browser cache completely**
2. **Try incognito/private mode**
3. **Wait 5 minutes** (GitHub Pages can take time to update)
4. **Check the GitHub Pages build status:**
   - Go to: https://github.com/NITIN-SINGH-2611/Student-Information-Management-System/actions
   - Look for "pages build and deployment" - should show green checkmark

## Direct Access

If configured correctly, these should work:
- https://nitin-singh-2611.github.io/Student-Information-Management-System/ (redirects to docs)
- https://nitin-singh-2611.github.io/Student-Information-Management-System/docs/index.html (direct)
