# Integration and Navigation Check Report

Date: 2026-05-02
Status: Completed

## Summary

Integration has been revalidated after implementing:

- BCrypt password hashing and verification
- Soft delete lifecycle for files/folders
- Trash restore and permanent delete
- Starred files and recent files views

## Navigation Verification

### Authentication

- `login.xhtml` -> `LoginBean.login()` -> `dashboard.xhtml` on success
- `login.xhtml` -> `register.xhtml`
- `register.xhtml` -> `UserBean.createUser()` -> `login.xhtml`

### Main Drive

- `dashboard.xhtml` -> `new-folder.xhtml`
- `dashboard.xhtml` -> `upload-file.xhtml`
- `dashboard.xhtml` folder click -> `folder-content.xhtml?folderId=...`
- `dashboard.xhtml` folder delete -> `FolderBean.deleteFolder(...)` (soft delete)

### Sidebar and Additional Views

- `template.xhtml` -> `dashboard.xhtml` (My Drive)
- `template.xhtml` -> `recent.xhtml`
- `template.xhtml` -> `starred.xhtml`
- `template.xhtml` -> `trash.xhtml`
- `template.xhtml` -> `shared.xhtml`
- `template.xhtml` logout -> `LoginBean.logout()` -> `login.xhtml`

## Data Flow Verification

### Registration and login

1. `register.xhtml` submits user data
2. `UserBean.createUser()` hashes password with BCrypt
3. `UserFacade.create(user)` persists hash
4. `login.xhtml` submits credentials
5. `UserFacade.login(email, password)` uses `BCrypt.checkpw(...)`
6. Session user set and redirected to dashboard

### Soft delete and trash

1. User deletes file/folder from active pages
2. Bean sets `deleted=true` and calls `edit(...)`
3. Active lists use non-deleted queries (`findAll()`)
4. Trash lists use deleted queries (`findDeleted()`)
5. Restore sets `deleted=false`
6. Permanent delete removes DB rows and storage entries

### Starred and recent

1. Star action toggles `Files.starred`
2. `starred.xhtml` uses `FileBean.getStarredFiles()`
3. `recent.xhtml` uses `FileBean.getRecentFiles()`
4. Both exclude deleted files and are scoped to current owner

## Bean-Facade Wiring Check

| Bean | Facade(s) | Verified Responsibility |
|---|---|---|
| `LoginBean` | `UserFacadeLocal` | Login/logout and session |
| `UserBean` | `UserFacadeLocal` | Registration with hashed password |
| `FolderBean` | `FolderFacadeLocal`, `FileFacadeLocal` | Folder create and recursive soft delete |
| `FileBean` | `FileFacadeLocal`, `FolderFacadeLocal` | Upload, list, soft delete, star/recent |
| `FolderContentBean` | `FolderFacadeLocal`, `FileFacadeLocal` | Folder file list and actions |
| `SharedFilesBean` | `SharedFilesFacadeLocal`, `FileFacadeLocal`, `UserFacadeLocal` | Sharing and permission checks |
| `TrashBean` | `FileFacadeLocal`, `FolderFacadeLocal`, `SharedFilesFacadeLocal` | Deleted lists, restore, permanent delete |

## Security and Validation Check

- Registration stores BCrypt hash, not plain password
- Login compares raw input against hash using BCrypt
- Ownership checks before delete, restore, permanent delete, and share
- Deleted files are blocked from sharing
- Shared views filter out deleted files

## Current Integration Checklist

- Routing and links are connected for all implemented pages
- Active views hide deleted records
- Trash view correctly shows deleted files/folders
- Restore and permanent delete paths are wired end-to-end
- Starred and recent views are integrated and query-backed
- Session-based current-user filtering is active in page beans

## Open Risks (Non-blocking)

- Several Java source files still contain mixed Arabic/English messages and comments; behavior is correct but text consistency may vary.
- UI and integration checks are manual; no browser automation suite is present yet.

Result: Integration is aligned with the current implemented feature set.
