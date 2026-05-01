# 🧪 Testing Guide and Future Development

## 📋 Testing Plan

### Manual Testing

#### 1. Registration and Authentication Testing

**T1.1: Register a new user**
```
Steps:
1. Go to http://localhost:8080/Folder_mangment/register.xhtml
2. Enter details:
   - Name: John Doe
   - Email: john@example.com
   - Password: SecurePassword123
3. Click "Create account"

Expected Result:
✅ Account created successfully
✅ Redirected to the login page
✅ Success message displayed
```

**T1.2: Login**
```
Steps:
1. Enter email: john@example.com
2. Enter password: SecurePassword123
3. Click "Next"

Expected Result:
✅ Logged in successfully
✅ Redirected to the dashboard
✅ User initial displayed in the header avatar
```

**T1.3: Invalid Login**
```
Steps:
1. Enter incorrect email or password
2. Click "Next"

Expected Result:
❌ Login fails
✅ Clear error message displayed
```

---

#### 2. Folder Management Testing

**T2.1: Create a new folder**
```
Steps:
1. Log in
2. In Dashboard, click "New folder"
3. Enter "Important Documents" in the folder name field
4. Click "Create"

Expected Result:
✅ "Created successfully" message
✅ New folder appears in the folders grid
✅ Physical path created:
   /home/abdulrahman/cloud_uploads/user_1/folder_1/
```

**T2.2: Delete a folder**
```
Steps:
1. In the folders grid, click the vertical ellipsis (⋮) and select "Remove folder"
2. Confirm deletion

Expected Result:
✅ Folder deleted from the database
✅ Folder disappears from the grid
✅ Physical path removed from the system
```

---

#### 3. File Management Testing

**T3.1: Upload a file**
```
Steps:
1. Create a folder first
2. Click "File upload"
3. Select the target folder from the dropdown
4. Choose a file from your device
5. Click "Upload"

Expected Result:
✅ "Uploaded successfully" message
✅ File appears when navigating into that folder
✅ File copied to path:
   /home/abdulrahman/cloud_uploads/user_1/folder_1/timestamp_filename
✅ File metadata saved (Name, Size, MIME Type)
```

**T3.2: View Files**
```
Steps:
1. Go to "My Drive"
2. Click on a folder card

Expected Result:
✅ Directed to folder-content.xhtml
✅ Displays files inside that specific folder
✅ Shows:
   - 📄 File Name
   - 💾 Size (in KB)
   - 🏷️ Type
```

**T3.3: Delete a file**
```
Steps:
1. Inside a folder, click the trash icon next to a file
2. Confirm deletion

Expected Result:
✅ File record deleted from database
✅ Physical file deleted from system
✅ File disappears from the table
```

---

#### 4. File Sharing Testing

**T4.1: Share a file with another user**
```
Preparation:
- Register two different users
- User 1: John (user_1)
- User 2: Jane (user_2)

Steps (for User 1):
1. Upload a file (e.g., report.pdf)
2. Go to sharing settings (or hypothetical share dialog):
   - Select the file (report.pdf)
   - Select the user (Jane)
   - Select permission (Viewer)
3. Click "Share"

Expected Result:
✅ Success message
✅ File appears in "Shared by me" section
✅ Share record saved in SHAREDFILES table
```

**T4.2: View files shared with me**
```
Steps (for User 2):
1. Login as Jane
2. Go to "Shared with me" sidebar link

Expected Result:
✅ report.pdf displayed in the list
✅ Owner's name (John) displayed
✅ Permission (Viewer) displayed
```

**T4.3: Change permissions**
```
Steps (for User 1):
1. Go to "Shared with me" page (or sharing settings)
2. Locate the shared file
3. Change permission to "Editor"

Expected Result:
✅ Permission updated in the database
✅ New permission displayed in the table
```

**T4.4: Revoke share**
```
Steps (for User 1):
1. Go to "Shared by me" section
2. Click "Remove access" icon

Expected Result:
✅ Share record deleted from database
✅ File disappears from Jane's "Shared with me" list
✅ Confirmation message
```

---

### Validation Tests

#### V1: Input Validation

```
V1.1: Missing Field
- Try to submit forms without filling required fields
- Result: Clear error message ❌

V1.2: Duplicate Email
- Try to register with an existing email
- Result: Error message ❌
```

#### V2: Permission Checks

```
V2.1: Try to delete another user's file
- Login as User 2
- Try to trigger deletion for User 1's file ID
- Result: Not allowed ✅

V2.2: Share unowned file
- Try to share a file you do not own
- Result: Error message "You do not own this file" ❌
```

#### V3: Duplicate Shares

```
V3.1: Share same file twice
- Share a file with a user
- Try to share the same file with the same user again
- Result: Warning message "Already shared" ⚠️
```

---

## 🔍 Performance Tests

### P1: Large File Test

```
Max supported:
- Single file size: ∞ (depends on storage/server config)

Test:
1. Upload a 100 MB file
2. Upload 100 small files
3. Check performance

Expected Result:
- Fast upload (network dependent)
- No freezing
```

### P2: Heavy Sharing Test

```
Test:
1. Share 1 file with 100 users
2. View "Shared with me" list

Expected Result:
- Fast loading
- No slowdowns
```

---

## 🐛 Browser & Device Testing

### B1: Browser Compatibility

```
Tested Browsers:
- ✅ Chrome (Latest)
- ✅ Firefox (Latest)
- ✅ Safari (Latest)
- ✅ Edge (Latest)

Checkpoints:
- Table rendering
- Form rendering
- Icons rendering
- UI Responsiveness
```

### B2: Device Testing

```
Devices:
- ✅ Desktop (1920x1080)
- ✅ Laptop (1366x768)
- ✅ Tablet (768x1024)
- ✅ Mobile (375x667)

Checkpoints:
- Responsive layout (flexbox/grid adjustments)
- Usability on touch screens
```

---

## 📊 Database Testing

### D1: Data Integrity

```
Tests:
1. Create user → Check table ✅
2. Create folder → Check relationships ✅
3. Upload file → Check paths ✅
4. Share → Check SHAREDFILES ✅

SQL Verification Queries:
SELECT COUNT(*) FROM Users;
SELECT COUNT(*) FROM Folders WHERE owner_id = 1;
SELECT * FROM Files WHERE owner_id = 1;
SELECT * FROM SharedFiles WHERE recipient_id = 1;
```

### D2: Cascading Deletes

```
Test:
1. Delete a folder
2. Verify associated files are deleted

Test:
1. Delete a user
2. Verify impact on data
```

---

## 🚀 Proposed Future Enhancements

### New Features

#### 1. Search and Filtering
```java
// Search for files
public List<Files> searchFiles(String query)

// Sort by date
public List<Files> sortByDate()

// Sort by size
public List<Files> sortBySize()
```

#### 2. Starred Files
```java
// New table: FavoritedFiles
// Add star ⭐ icon next to files
public void toggleFavorite(Files f)
```

#### 3. Trash Bin
```java
// New table: DeletedFiles (with timestamp)
// Show "Trash" folder
public List<Files> getDeletedFiles()
public void restoreFile(Files f)
```

#### 4. Notifications
```java
// On file share: send email
// On share revoke: notify recipient
public void sendNotification(User user, String message)
```

#### 5. Automated Backups
```java
// Daily backups
// Restore from backup
public void createBackup()
public void restoreFromBackup(Date date)
```

---

## 📝 Quick Test Steps

```bash
# 1. Build
mvn clean package

# 2. Deploy
./asadmin deploy --force=true target/Folder_mangment-1.0-SNAPSHOT.war

# 3. Open Browser
# http://localhost:8080/Folder_mangment/login.xhtml

# 4. Test now!
```

---

## ✅ Final Checklist

```
Before Launch:
☑️ Test Registration
☑️ Test Login
☑️ Test Folder Creation
☑️ Test File Upload
☑️ Test Sharing
☑️ Test Deletion
☑️ Test Permissions
☑️ Test Performance
☑️ Test Compatibility
☑️ Test Database
☑️ Security Check
☑️ Documentation Check
☑️ Code Review
```

---

**Last Update:** 2024-05-01 (Translated to English)
**Version:** 1.0.0
