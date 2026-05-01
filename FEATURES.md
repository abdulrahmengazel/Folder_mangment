# 📋 Features and Files Summary

## 🎯 Core Functionalities

### 1. **Registration and Authentication**

#### Login Page (`login.xhtml`)
- ✅ Email field
- ✅ Password field
- ✅ Login button
- ✅ Link to create a new account
- ✅ Error message display

**Used Bean:** `LoginBean`
**Facade:** `UserFacade` → `UserFacadeLocal`

---

#### Registration Page (`register.xhtml`)
- ✅ Full name field
- ✅ Email field
- ✅ Password field
- ✅ Data validation
- ✅ Physical storage folder creation
- ✅ Link to return to login

**Used Bean:** `UserBean`
**Facade:** `UserFacade` → `UserFacadeLocal`

---

### 2. **Folder Management**

#### Create a New Folder
```
Dashboard → "New folder" Button
```

**Features:**
- ✅ Enter folder name
- ✅ Save to database
- ✅ Create physical path on the system
- ✅ Success/error message display

**Bean:** `FolderBean.createFolder()`
**Facade:** `FolderFacade` → `FolderFacadeLocal`
**Entity:** `Folders`

#### Delete a Folder
```
Dashboard → My Drive → Folders Grid → "Delete" Action
```

**Features:**
- ✅ Deletion confirmation
- ✅ Delete from database
- ✅ Delete physical path (optional logic based on implementation)

**Bean:** `FolderBean.deleteFolder(Folders f)`

---

### 3. **File Management**

#### Upload a New File
```
Dashboard → "File upload" Button
```

**Features:**
- ✅ Select target folder
- ✅ Choose file from device
- ✅ Save file information in the database
- ✅ Copy file to storage path
- ✅ Path structure: `ROOT_UPLOAD_DIR/user_ID/folder_ID/filename`

**Bean:** `FileBean.uploadFile()`
**Facade:** `FileFacade` → `FileFacadeLocal`
**Entity:** `Files`
**Physical Path:** `/home/abdulrahman/cloud_uploads/`

#### View Files
```
Dashboard → Select Folder → Folder Content Page
```

**Columns:**
- 📄 File Name
- 💾 File Size (in KB)
- 🏷️ Type
- ❌ Delete button

**Bean:** `FolderContentBean.getFilesInFolder()`

---

### 4. **File Sharing and Permissions**

#### Share a file with another user
```
Dashboard → "Shared with me" Page (or hypothetical Sharing Dialog)
```

**Steps:**
1. Select file from your files list
2. Select user to share with
3. Choose permission type:
   - 👁️ **Viewer** (READ) - View file only
   - ✏️ **Editor** (WRITE) - View and edit file

**Bean:** `SharedFilesBean.shareFile()`
**Facade:** `SharedFilesFacade` → `SharedFilesFacadeLocal`
**Entity:** `SharedFiles`
**Enum:** `PermissionEnum` (READ, WRITE)

**Validation:**
- ✅ Ensure current user is the file owner
- ✅ Prevent duplicate sharing with the same user
- ✅ Ensure recipient user exists

#### Revoke Sharing
```
Shared Page → "Shared by me" section → "Remove access" action
```

**Bean:** `SharedFilesBean.removeSharedFile(SharedFiles sf)`

#### View Files Shared With Me
```
Shared Page → "Shared with me" section
```

**Information Displayed:**
- 📄 File Name
- 👤 Owner Name/Email
- 🔐 Permission Type

**Bean:** `SharedFilesBean.getSharedWithMeList()`

#### View Files I Shared
```
Shared Page → "Shared by me" section
```

**Information Displayed:**
- 📄 File Name
- 👥 Shared With User
- 🔐 Permission
- ❌ Revoke action

**Bean:** `SharedFilesBean.getSharedFilesList()`

---

## 📁 UI Files (XHTML)

| File | Function | Used Bean |
|------|--------|-----------------|
| `login.xhtml` | User Login | `LoginBean` |
| `register.xhtml` | Create new account | `UserBean` |
| `dashboard.xhtml` | Main Dashboard (Folders) | `FolderBean`, `FileBean` |
| `folder-content.xhtml` | View files in a folder | `FolderContentBean` |
| `new-folder.xhtml` | Create a folder form | `FolderBean` |
| `upload-file.xhtml` | Upload file form | `FileBean`, `FolderBean` |
| `shared.xhtml` | Shared files view | `SharedFilesBean` |
| `template.xhtml` | Global Layout Template | - |

---

## ☕ Java Bean Files

| File | Function | Scope |
|------|--------|---------|
| `LoginBean.java` | Handle login process | `@ViewScoped` |
| `UserBean.java` | Create and manage users | `@ViewScoped` |
| `FolderBean.java` | Create and delete folders | `@ViewScoped` |
| `FileBean.java` | Upload and manage files | `@ViewScoped` |
| `SharedFilesBean.java` | Share files and permissions | `@ViewScoped` |
| `FolderContentBean.java` | View contents of a specific folder | `@ViewScoped` |

---

## 🗄️ Entity Files (Entities)

| File | Table | Columns |
|------|--------|--------|
| `Users.java` | USERS | id, name, email, password |
| `Folders.java` | FOLDERS | id, name, createdAt, owner_id, parent_folder_id |
| `Files.java` | FILES | id, name, size, type, path, folder_id, owner_id |
| `SharedFiles.java` | SHAREDFILES | id, file_id, recipient_id, permission |

---

## 🔧 Service Files (Facades)

### Implementations

| File | Interface | Entity |
|------|--------|----------|
| `UserFacade.java` | `UserFacadeLocal` | `Users` |
| `FolderFacade.java` | `FolderFacadeLocal` | `Folders` |
| `FileFacade.java` | `FileFacadeLocal` | `Files` |
| `SharedFilesFacade.java` | `SharedFilesFacadeLocal` | `SharedFiles` |
| `AbstractFacade.java` | - | Base Class |

### Local Interfaces

```
facadeLocal/
├── UserFacadeLocal.java
├── FolderFacadeLocal.java
├── FileFacadeLocal.java
└── SharedFilesFacadeLocal.java
```

**Common Methods:**
- `create(Entity e)` - Create
- `edit(Entity e)` - Update
- `remove(Entity e)` - Delete
- `find(Object id)` - Find by ID
- `findAll()` - Get all

**Specific Methods:**
- `UserFacade.login(String email, String password)` - Authentication

---

## 🎨 Design Files (CSS)

```
resources/css/
└── style.css
```

**Main Styles (Google Drive Theme):**
- ✅ Data Tables (`.files-table`)
- ✅ Buttons (`.btn-drive`, `.btn-primary`)
- ✅ Form Inputs (`.form-control`)
- ✅ Grid Layouts (`.folders-grid`, `.folder-card`)
- ✅ Sidebar Navigation

---

## ⚙️ Configuration Files

### `pom.xml`
```xml
<!-- Project Dependencies -->
- jakarta.platform:jakarta.jakartaee-api:10.0.0
- org.eclipse.persistence:org.eclipse.persistence.jpa:4.0.2
- org.junit.jupiter:junit-jupiter:5.10.2
```

### `persistence.xml`
```xml
<!-- JPA Configuration -->
- Persistence Unit: CloudDrivePu
- Provider: EclipseLink
- Data Source: jdbc/CloudDrivePu
- DDL: create-or-extend-tables
```

### `web.xml`
```xml
<!-- Application Server Configuration -->
- JSF Servlet: jakarta.faces.webapp.FacesServlet
- URL Pattern: *.xhtml
- Welcome File: login.xhtml
```

---

## 🔄 Data Flow

```
┌─────────────┐
│   XHTML     │ (User Interface)
│ (JSF Pages) │
└──────┬──────┘
       │
       ↓
┌─────────────┐
│    Bean     │ (Logic Processing)
│  (6 Beans)  │
└──────┬──────┘
       │
       ↓
┌─────────────┐
│   Facade    │ (EJB Services)
│ (4 Facades) │
└──────┬──────┘
       │
       ↓
┌─────────────┐
│     JPA     │ (Data Access Standard)
│  (Entities) │
└──────┬──────┘
       │
       ↓
┌─────────────┐
│ Database    │ (Database)
│(Derby/MySQL)│
└─────────────┘
```

---

## 📊 Use Cases

### Case 1: Register a new user
```
register.xhtml → UserBean.createUser()
→ UserFacade.create() → Users Entity → Database
```

### Case 2: Upload a file
```
upload-file.xhtml → FileBean.uploadFile()
→ FileFacade.create() → Files Entity → Database + File System
```

### Case 3: Share a file
```
shared.xhtml (or dashboard) → SharedFilesBean.shareFile()
→ SharedFilesFacade.create() → SharedFiles Entity → Database
```

### Case 4: View files shared with me
```
shared.xhtml → SharedFilesBean.getSharedWithMeList()
→ SharedFilesFacade.findAll() → Filtered by recipient_id
```

---

## ✨ Additional Features

### Error Handling
- ✅ Input data validation
- ✅ Clear error messages in English
- ✅ Exception handling

### Security
- ✅ User permissions verification
- ✅ Prevent deletion of others' files
- ✅ Verify file ownership before sharing

### Usability
- ✅ Modern Google Drive-like interface
- ✅ Deletion confirmation dialogs
- ✅ Dropdown menus populated with real data
- ✅ Empty states for empty folders/drives

---

**Last Update:** 2024-05-01 (Translated & Updated structure)
**Version:** 1.0.0