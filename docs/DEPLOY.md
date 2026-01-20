# Deploying to GitHub Pages

## Quick Deployment Steps

### 1. Commit and Push the docs folder

```bash
cd Manager
git add docs/
git commit -m "Add fully functional web version for GitHub Pages"
git push origin main
```

### 2. Configure GitHub Pages

1. Go to: https://github.com/NITIN-SINGH-2611/Student-Information-Management-System/settings/pages
2. Under **Source**, select:
   - **Branch**: `main`
   - **Folder**: `/docs`
3. Click **Save**

### 3. Wait for Deployment

- GitHub will build and deploy your site
- Usually takes 1-2 minutes
- Your site will be available at: **https://nitin-singh-2611.github.io/Student-Information-Management-System/**

## Features Available in Web Version

✅ **Complete Student Management**
- Add, Edit, Delete students (Admin only)
- View student details (Admin & Teacher)
- Search students
- Double-click to view details

✅ **Course Management**
- Add, Edit courses (Admin only)
- View courses (Admin & Teacher)
- Enroll students in courses

✅ **Attendance Management**
- Record attendance for students
- View attendance by course and date
- Batch attendance recording

✅ **Grade Management**
- Record grades for assessments
- Automatic grade letter calculation
- View grades by student and course

✅ **Financial Management** (Admin only)
- Add financial transactions
- Track payments
- Calculate student balance
- Record payments

## Syncing Data from Desktop App

### Option 1: Manual Sync

1. Copy `Manager/data.json` file
2. Replace `Manager/docs/data.json`
3. Commit and push:
   ```bash
   git add docs/data.json
   git commit -m "Update student data"
   git push origin main
   ```

### Option 2: Use Sync Tool

1. Open `docs/sync-data.html` in your browser
2. Upload your `data.json` file from the Manager folder
3. Click "Sync Data"
4. Data will be saved to browser localStorage

## Data Storage

- **Web Version**: Uses browser localStorage (persists in browser)
- **Desktop App**: Uses `data.json` file
- **Sync**: Update `docs/data.json` to sync data to web version

## Default Login Credentials

- **Admin**: `admin` / `admin123`
- **Teacher**: `teacher` / `teacher123`
- **Student**: `student` / `student123`

## Notes

- The web version loads data from `docs/data.json` on first visit
- User changes are stored in browser localStorage
- To share data across devices, update `docs/data.json` and push to GitHub
- All CRUD operations work exactly like the desktop version
