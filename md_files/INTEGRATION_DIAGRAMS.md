# Integration and Navigation Flow Diagrams

## 1) Full Navigation Map

```
                         +---------------------+
                         |  login.xhtml        |
                         +----------+----------+
                                    |
                      success       |       create account
                                    v
                         +---------------------+
                         |  dashboard.xhtml    |
                         +----+----+----+-----+
                              |    |    | \
                              |    |    |  \ sidebar
                              |    |    |   \-----------------------------+
                              |    |    |                                 |
                              v    v    v                                 v
                    +-----------+ +--------------+ +----------------+ +----------------+
                    |new-folder | |upload-file   | |folder-content  | |shared.xhtml    |
                    +-----------+ +--------------+ +----------------+ +----------------+
                                                                           |
                                                      +--------------------+----------------------+
                                                      |                    |                      |
                                                      v                    v                      v
                                                +-----------+        +------------+         +-----------+
                                                |recent.xhtml|       |starred.xhtml|        |trash.xhtml |
                                                +-----------+        +------------+         +-----------+

  register.xhtml -> login.xhtml
  template.xhtml provides global links and logout
```

## 2) Layer Diagram

```
XHTML Pages
  -> Beans
      LoginBean
      UserBean
      FolderBean
      FileBean
      FolderContentBean
      SharedFilesBean
      TrashBean
  -> EJB Facades
      UserFacade
      FolderFacade
      FileFacade
      SharedFilesFacade
  -> JPA Entities
      Users
      Folders
      Files
      SharedFiles
  -> Database
```

## 3) Authentication Flow (BCrypt)

```
register.xhtml
  -> UserBean.createUser()
  -> BCrypt.hashpw(rawPassword, gensalt)
  -> UserFacade.create(user)

login.xhtml
  -> LoginBean.login()
  -> UserFacade.login(email, rawPassword)
  -> BCrypt.checkpw(rawPassword, storedHash)
  -> session user set -> dashboard.xhtml
```

## 4) Soft Delete and Trash Flow

```
Active page action (dashboard/folder-content)
  -> set deleted=true
  -> facade.edit(entity)
  -> hidden from active lists (findAll uses deleted=false)
  -> visible in trash lists (findDeleted uses deleted=true)

trash.xhtml restore
  -> set deleted=false -> facade.edit

trash.xhtml permanent delete
  -> remove share links (for files)
  -> delete DB row
  -> attempt filesystem cleanup
```

## 5) Starred and Recent Flow

```
folder-content.xhtml star action
  -> toggle Files.starred
  -> FileFacade.edit(file)

starred.xhtml
  -> FileBean.getStarredFiles()
  -> FileFacade.findStarredFiles(ownerId)
  -> filter deleted=false, sort by createdAt desc

recent.xhtml
  -> FileBean.getRecentFiles()
  -> FileFacade.findRecentFiles(ownerId)
  -> filter deleted=false, sort by createdAt desc
```

## 6) Sharing Flow

```
shared.xhtml
  -> SharedFilesBean.shareFile()
      - validate current user
      - validate file exists and owner matches
      - block deleted file sharing
      - validate recipient exists
      - prevent duplicate share
  -> SharedFilesFacade.create(shared)

Shared lists
  - my shared files: owner scope, deleted files excluded
  - shared with me: recipient scope, deleted files excluded
```

## 7) Integration Tracking Table

| Component | Status | Notes |
|---|---|---|
| `login.xhtml` | integrated | login routing and error messages |
| `register.xhtml` | integrated | hashed password registration flow |
| `dashboard.xhtml` | integrated | folder list and main actions |
| `folder-content.xhtml` | integrated | file list, delete, star toggle |
| `upload-file.xhtml` | integrated | upload with target folder selection |
| `shared.xhtml` | integrated | share/list/revoke flows |
| `recent.xhtml` | integrated | recent query-backed list |
| `starred.xhtml` | integrated | starred query-backed list |
| `trash.xhtml` | integrated | deleted lists, restore, permanent delete |
| `TrashBean` | integrated | full lifecycle management |
| `FileFacade` | integrated | active/deleted/starred/recent queries |
| `FolderFacade` | integrated | active/deleted folder queries |
| `UserFacade` | integrated | BCrypt-based login verification |

Last Update: 2026-05-02
