# Cloud File Management System

A Jakarta EE + JSF web application for personal cloud-style file management with sharing, favorites, recent files, and a full trash lifecycle.

## Key Functionality

- User registration and login with secure password hashing (`BCrypt`)
- Folder creation with per-user physical storage paths
- File upload with metadata persistence (`name`, `size`, `type`, `path`, `createdAt`)
- Soft delete for files and folders (`deleted` flag) with restore and permanent delete from Trash
- Starred files (`starred` flag) and Recent files (ordered by `createdAt` descending)
- File sharing with permission levels (`READ`, `WRITE`)

## Security Highlights

- Passwords are hashed in `UserBean.createUser()` using `BCrypt.hashpw(...)`
- Login validation in `UserFacade.login(...)` uses `BCrypt.checkpw(...)`
- Ownership checks are enforced before delete/share/star/restore operations
- Deleted files are excluded from normal lists and sharing views

## Architecture

```
src/main/java
  bean/
    LoginBean.java
    UserBean.java
    FolderBean.java
    FileBean.java
    FolderContentBean.java
    SharedFilesBean.java
    TrashBean.java
  entity/
    Users.java
    Folders.java
    Files.java
    SharedFiles.java
  facade/
    AbstractFacade.java
    UserFacade.java
    FolderFacade.java
    FileFacade.java
    SharedFilesFacade.java
  facadeLocal/
    UserFacadeLocal.java
    FolderFacadeLocal.java
    FileFacadeLocal.java
    SharedFilesFacadeLocal.java
  enums/
    PermissionEnum.java
```

## Main Pages

- `login.xhtml`: sign in
- `register.xhtml`: create account
- `dashboard.xhtml`: drive root and folders
- `folder-content.xhtml`: folder file list, star/unstar, delete
- `upload-file.xhtml`: upload into selected folder
- `shared.xhtml`: files shared by me / with me
- `starred.xhtml`: all starred files for current user
- `recent.xhtml`: most recent files for current user
- `trash.xhtml`: deleted files/folders with restore/permanent delete

## Data Model (Current)

### `Users`

- `id`
- `name`
- `email` (unique)
- `password` (BCrypt hash)

### `Folders`

- `id`
- `name`
- `createdAt`
- `deleted` (soft delete flag)
- `owner_id` -> `Users.id`
- `parent_folder_id` -> `Folders.id`

### `Files`

- `id`
- `name`
- `size`
- `type`
- `path`
- `createdAt`
- `deleted` (soft delete flag)
- `starred` (favorite flag)
- `folder_id` -> `Folders.id`
- `owner_id` -> `Users.id`

### `SharedFiles`

- `id`
- `file_id` -> `Files.id`
- `recipient_id` -> `Users.id`
- `permission` (`READ` or `WRITE`)

## Query Behavior (Facade Layer)

- `FileFacade.findAll()` returns only non-deleted files
- `FileFacade.findDeleted()` returns only deleted files
- `FileFacade.findStarredFiles(ownerId)` returns non-deleted starred files sorted by newest
- `FileFacade.findRecentFiles(ownerId)` returns non-deleted files sorted by newest
- `FolderFacade.findAll()` returns only non-deleted folders
- `FolderFacade.findDeleted()` returns only deleted folders

## Build and Run

### Requirements

- JDK 23
- Maven 3.8+
- Jakarta EE-compatible server (example: GlassFish 7)

### Local Build

```bash
cd /home/abdulrahman/Documents/Folder_mangment
./mvnw clean package
```

### Access URL

```text
http://localhost:8080/Folder_mangment/login.xhtml
```

## Related Documentation

- `FEATURES.md`: detailed feature-to-file mapping
- `DATABASE.md`: database schema, relationships, and SQL verification queries
- `QUICKSTART.md`: fast setup and smoke test
- `TESTING_GUIDE.md`: manual and integration test cases
- `INTEGRATION_CHECK.md`: verified page/bean/facade integration
- `INTEGRATION_DIAGRAMS.md`: architecture and flow diagrams

## Current Status

- Password hashing: implemented
- Soft delete and Trash lifecycle: implemented
- Starred and Recent views: implemented
- Sharing permission model: implemented

Last Update: 2026-05-02
