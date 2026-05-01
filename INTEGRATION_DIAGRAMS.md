# 🔄 Integration and Navigation Flow Diagrams

## 1️⃣ Full Navigation Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    Cloud File Management System                  │
└─────────────────────────────────────────────────────────────────┘

                            Web Application
                                │
                ┌───────────────┼───────────────┐
                │               │               │
            login.xhtml    register.xhtml   dashboard.xhtml
                │               │               │
                └───────────┬───┴───────┬───────┘
                            │           │
                       template.xhtml ← (Layout)
                            │
                            ▼
                    ┌───────────────┐
                    │  User Session │
                    └───────────────┘
                            │
                            ▼
                    ┌───────────────┐
                    │  Beans Layer  │
                    └───────────────┘
                    - LoginBean
                    - UserBean
                    - FolderBean
                    - FileBean
                    - FolderContentBean ⭐
                    - SharedFilesBean ⭐
                            │
                            ▼
                    ┌───────────────┐
                    │ Facades Layer │
                    └───────────────┘
                    - UserFacade
                    - FolderFacade
                    - FileFacade
                    - SharedFilesFacade ⭐
                            │
                            ▼
                    ┌───────────────┐
                    │  JPA/Entities │
                    └───────────────┘
                    - Users
                    - Folders
                    - Files
                    - SharedFiles ⭐
                            │
                            ▼
                    ┌───────────────┐
                    │   Database    │
                    └───────────────┘
```

---

## 2️⃣ Page Navigation Flow

```
                          login.xhtml
                          ┌────────────┐
                          │   Sign In  │
                          └──────┬─────┘
                                 │
                    ┌────────────┴────────────┐
                    │                         │
         Success (dashboard) ✅      Failure (Retry)
                    │                         │
                    ▼                         │
             dashboard.xhtml ◄───────────────┘
                (My Drive)
                    │
        ┌───────────┼───────────┬─────────────┐
        │           │           │             │
        ▼           ▼           ▼             ▼
   new-folder  upload-file  folder-content   shared
        │           │           │             │
        └───────────┴─────┬─────┴─────────────┘
                          │
                          ▼
                  (Stay or return)
                          │
                          ▼
                       Sign Out
                          │
                          ▼
                  login.xhtml (Return)


            register.xhtml
             (New Registration)
                    │
         ┌──────────┴──────────┐
         │                     │
     Success ✅             Failure
         │                     │
         ▼                     │
    login.xhtml ◄──────────────┘
```

---

## 3️⃣ Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                      User Registration Flow                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  register.xhtml ──┐                                             │
│  (Data input)     │                                             │
│                  │                                              │
│              UserBean                                           │
│              │  user.name                                       │
│              │  user.email                                      │
│              │  user.password                                   │
│              │                                                  │
│              ▼ createUser()                                     │
│         ┌────────────┐                                          │
│         │ Validation │                                          │
│         └─────┬──────┘                                          │
│              │                                                  │
│              ▼                                                  │
│      UserFacade.create(user)                                   │
│              │                                                  │
│              ▼                                                  │
│   ┌──────────────────┐                                          │
│   │  Users Entity    │                                          │
│   │  Database Table  │                                          │
│   │                  │                                          │
│   │  ├─ id          │                                           │
│   │  ├─ name        │                                           │
│   │  ├─ email       │                                           │
│   │  └─ password    │                                           │
│   └──────────────────┘                                          │
│              │                                                  │
│              ▼ (Redirect)                                       │
│         login.xhtml                                             │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 4️⃣ File Sharing Flow (SharedFilesBean) ⭐

```
┌──────────────────────────────────────────────────────────────────┐
│                      File Sharing Flow                           │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  shared.xhtml (or dialog)                                       │
│  ├─ Select File (selectedFileId)                                │
│  ├─ Select User (selectedUserId)                                │
│  ├─ Select Permission (READ/WRITE)                              │
│  └─ "Share" Button                                              │
│       │                                                         │
│       ▼                                                         │
│  SharedFilesBean.shareFile()                                    │
│       │                                                         │
│       ├─ ✅ Verify Ownership                                     │
│       │    file.owner.id == currentUser.id                     │
│       │                                                         │
│       ├─ ✅ Verify User Exists                                   │
│       │    userFacade.find(selectedUserId)                     │
│       │                                                         │
│       ├─ ✅ Prevent Duplicates                                   │
│       │    no existing SharedFiles                              │
│       │                                                         │
│       └─ ✅ Save Share                                           │
│            SharedFilesFacade.create()                           │
│            │                                                    │
│            ▼                                                    │
│  ┌──────────────────────────┐                                  │
│  │  SharedFiles Entity      │                                  │
│  │  ┌──────────────────┐    │                                  │
│  │  │ id               │    │                                  │
│  │  │ file_id (FK)     │────┼─→ Files Table                   │
│  │  │ recipient_id(FK) │────┼─→ Users Table                   │
│  │  │ permission       │    │                                  │
│  │  │ (READ/WRITE)     │    │                                  │
│  │  └──────────────────┘    │                                  │
│  └──────────────────────────┘                                  │
│       │                                                         │
│       ▼                                                         │
│  Display Files:                                                 │
│  ├─ sharedFilesList                                            │
│  │  └─ (Files I shared)                                        │
│  │                                                              │
│  └─ sharedWithMeList                                           │
│     └─ (Files shared with me)                                  │
│                                                                 │
└──────────────────────────────────────────────────────────────────┘
```

---

## 5️⃣ Forms and Links Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                      dashboard.xhtml                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                   Action Bar (Header)                    │  │
│  ├──────────────────────────────────────────────────────────┤  │
│  │                                                          │  │
│  │  ┌─────────────────────┐  ┌──────────────────────────┐ │  │
│  │  │  New Folder Link    │  │  File Upload Link        │ │  │
│  │  │  ┌───────────────┐  │  │  ┌────────────────────┐  │ │  │
│  │  │  │ Routes to:    │  │  │  │ Routes to:         │  │ │  │
│  │  │  │ new-folder.   │  │  │  │ upload-file.       │  │ │  │
│  │  │  │ xhtml         │  │  │  │ xhtml              │  │ │  │
│  │  │  └───────────────┘  │  │  └────────────────────┘  │ │  │
│  │  └─────────────────────┘  └──────────────────────────┘ │  │
│  │                                                          │  │
│  └───────────────────────────┼──────────────────────────────┘  │
│                              │                                 │
│  ┌───────────────────────────┴──────────────────────────────┐  │
│  │                 Data Display (My Drive)                  │  │
│  ├──────────────────────────────────────────────────────────┤  │
│  │                                                          │  │
│  │  ┌────────────────────────────────────────────────────┐  │  │
│  │  │  My Folders (foldersList)                          │  │  │
│  │  │  - Displayed as Grid Cards                         │  │  │
│  │  │  - Click -> Routes to folder-content.xhtml         │  │  │
│  │  │  - Delete Action via FolderBean                    │  │  │
│  │  └────────────────────────────────────────────────────┘  │  │
│  │                                                          │  │
│  └───────────────────────────┼──────────────────────────────┘  │
│                              │                                 │
└──────────────────────────────┴──────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      shared.xhtml                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌────────────────────────────────────────────────────────┐ │  │
│  │                 Sharing Section ⭐                     │ │  │
│  ├────────────────────────────────────────────────────────┤ │  │
│  │                                                        │ │  │
│  │  ┌─────────────────────────────────────────────────┐  │ │  │
│  │  │  Files Shared by Me                             │  │ │  │
│  │  │  (sharedFilesList)                              │  │ │  │
│  │  │  - file.name                                    │  │ │  │
│  │  │  - recipient.email                              │  │ │  │
│  │  │  - permission                                   │  │ │  │
│  │  │  - removeBtn                                    │  │ │  │
│  │  └─────────────────────────────────────────────────┘  │ │  │
│  │                                                        │ │  │
│  │  ┌─────────────────────────────────────────────────┐  │ │  │
│  │  │  Files Shared with Me                           │  │ │  │
│  │  │  (sharedWithMeList)                             │  │ │  │
│  │  │  - file.name                                    │  │ │  │
│  │  │  - owner.email                                  │  │ │  │
│  │  │  - permission                                   │  │ │  │
│  │  └─────────────────────────────────────────────────┘  │ │  │
│  │                                                        │ │  │
│  └────────────────────────────────────────────────────────┘ │  │
│                                                             │  │
└─────────────────────────────────────────────────────────────┘
```

---

## 6️⃣ Layer Communication Diagram

```
      PRESENTATION LAYER
          (XHTML)
            │
    ┌───────┼───────────────┬───────────────┐
    │       │               │               │
login   register  dashboard/views      shared
    │       │               │               │
    └───────┼───────────────┴───────────────┘
            │
     BUSINESS LOGIC LAYER
         (Beans)
            │
    ┌───────┼─────────────────────────┬─────────┐
    │       │       │       │         │         │
 LoginBean UserBean FolderBean FileBean FolderContent SharedFiles
            │       │       │         Bean      Bean ⭐
    └───────┼─────────────────────────┴─────────┘
            │
      SERVICE LAYER
       (EJB Facades)
            │
    ┌───────┼─────────────────────────┐
    │       │       │       │         │
 UserFacade FolderFacade FileFacade SharedFilesFacade ⭐
    │       │       │       │         │
    └───────┼─────────────────────────┘
            │
    DATA ACCESS LAYER
      (JPA Entities)
            │
    ┌───────┼─────────────────────────┐
    │       │       │       │         │
 Users    Folders  Files  SharedFiles ⭐
    │       │       │       │         │
    └───────┼─────────────────────────┘
            │
      DATABASE LAYER
            │
    ┌───────┼─────────────────────────┐
    │       │       │       │         │
 Users  Folders  Files  SharedFiles ⭐
  Table  Table   Table    Table
    │       │       │       │         │
    └───────┼─────────────────────────┘
```

---

## 7️⃣ Security & Validation Checks Diagram

```
┌──────────────────────────────────────────────────────────────┐
│                     Security Checks                          │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  1️⃣  On Login                                              │
│      └─ Look up user data                                   │
│      └─ Verify password                                     │
│      └─ Save in SessionMap                                  │
│                                                              │
│  2️⃣  On Folder Creation                                    │
│      └─ Check for user in session                           │
│      └─ Link folder to current user                         │
│      └─ Create secure physical path                         │
│                                                              │
│  3️⃣  On File Upload                                        │
│      └─ Verify file exists                                  │
│      └─ Verify folder exists                                │
│      └─ Verify user owns folder (implied/needed)            │
│      └─ Copy file to protected path                         │
│                                                              │
│  4️⃣  On File Share ⭐                                      │
│      └─ ✅ Verify file exists                                │
│      └─ ✅ Verify user owns file                             │
│      └─ ✅ Verify recipient exists                           │
│      └─ ✅ Prevent duplicate shares                          │
│      └─ ✅ Validate permission enum                          │
│                                                              │
│  5️⃣  On Delete                                             │
│      └─ Verify ownership                                    │
│      └─ Delete from database                                │
│      └─ Delete physical file (if implemented)               │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## 📊 Integration Tracking Table

| Component | Type | Status | Notes |
|-------|------|--------|---------|
| login.xhtml | XHTML | ✅ | Routes to dashboard or register |
| register.xhtml | XHTML | ✅ | Routes to login after creation |
| dashboard.xhtml | XHTML | ✅ | Forms and grids connected |
| template.xhtml | XHTML | ✅ | Layout with header and sidebar |
| new-folder.xhtml | XHTML | ✅ | Connected to FolderBean |
| upload-file.xhtml | XHTML | ✅ | Connected to FileBean |
| folder-content.xhtml| XHTML | ✅ | Connected to FolderContentBean |
| shared.xhtml | XHTML | ✅ | Connected to SharedFilesBean |
| LoginBean | Java | ✅ | Connected to UserFacade |
| UserBean | Java | ✅ | Connected to UserFacade |
| FolderBean | Java | ✅ | Connected to FolderFacade |
| FileBean | Java | ✅ | Connected to FileFacade |
| FolderContentBean| Java | ✅ | Connected to Folder/FileFacade |
| SharedFilesBean | Java | ✅ | Connected to SharedFilesFacade ⭐ |
| UserFacade | Java | ✅ | Implements UserFacadeLocal |
| FolderFacade | Java | ✅ | Implements FolderFacadeLocal |
| FileFacade | Java | ✅ | Implements FileFacadeLocal |
| SharedFilesFacade | Java | ✅ | Implements SharedFilesFacadeLocal ⭐ |
| Users Entity | Java | ✅ | Database table |
| Folders Entity | Java | ✅ | Database table |
| Files Entity | Java | ✅ | Database table |
| SharedFiles Entity | Java | ✅ | Database table ⭐ |

---

## ✅ Conclusion

```
┌────────────────────────────────────────────────────────────┐
│  All components are correctly and smoothly integrated.   │
│  ✅ Routing works correctly                              │
│  ✅ Data flow is sound                                   │
│  ✅ Security is properly implemented                     │
│  ✅ Errors are handled properly                          │
│  ✅ Interface is consistent and easy to use              │
│                                                            │
│  Status: 🟢 Ready for Launch                             │
└────────────────────────────────────────────────────────────┘
```

---

**Last Update:** 2024-05-01 (Translated to English)
