# Features and Files Summary

This document maps implemented features to pages, beans, facades, and entities.

## Core Functionalities

### 1) Authentication and Account Creation

#### `login.xhtml`

- Email/password login form
- Error message on invalid credentials
- Redirect to dashboard on success

Bean/facade flow:

- `LoginBean.login()`
- `UserFacade.login(email, password)`
- `BCrypt.checkpw(...)` for password verification

#### `register.xhtml`

- Account creation with basic form validation
- Creates a physical root folder for the new user
- Redirects to login page after successful registration

Bean/facade flow:

- `UserBean.createUser()`
- `BCrypt.hashpw(...)` before persistence
- `UserFacade.create(user)`

### 2) Folder Management

#### Create Folder

- Page path: `dashboard.xhtml` -> `new-folder.xhtml`
- Persists folder metadata and creates physical directory

Bean/facade flow:

- `FolderBean.createFolder()`
- `FolderFacade.create(folder)`

#### Delete Folder (Soft Delete)

- Page path: `dashboard.xhtml` folder actions
- Marks folder and nested content as `deleted=true`
- Folder disappears from drive and appears in Trash

Bean/facade flow:

- `FolderBean.deleteFolder(folder)`
- `FolderBean.softDeleteFolderTree(...)`
- `FolderFacade.edit(folder)` and `FileFacade.edit(file)`

### 3) File Management

#### Upload File

- Page path: `dashboard.xhtml` -> `upload-file.xhtml`
- Stores file physically and metadata in DB
- `createdAt` is auto-filled (`@PrePersist`)

Bean/facade flow:

- `FileBean.uploadFile()`
- `FileFacade.create(file)`

#### Delete File (Soft Delete)

- Page path: `folder-content.xhtml`
- Marks file as `deleted=true`
- File is hidden from normal views and moved logically to Trash

Bean/facade flow:

- `FileBean.deleteFile(file)` and `FolderContentBean.deleteFile(file)`
- `FileFacade.edit(file)`

### 4) Trash Lifecycle

#### Trash View

- Page path: `trash.xhtml`
- Shows deleted files and folders for current user only

Bean/facade flow:

- `TrashBean.getDeletedFiles()` -> `FileFacade.findDeleted()`
- `TrashBean.getDeletedFolders()` -> `FolderFacade.findDeleted()`

#### Restore

- Restore file/folder to active state (`deleted=false`)
- Folder restore recursively restores nested deleted files/folders

Bean methods:

- `TrashBean.restoreFile(file)`
- `TrashBean.restoreFolder(folder)`

#### Permanent Delete

- Permanently removes DB records
- Cleans up physical storage (best effort)
- Removes related share records before file deletion

Bean methods:

- `TrashBean.permanentlyDeleteFile(file)`
- `TrashBean.permanentlyDeleteFolder(folder)`

### 5) Starred and Recent Files

#### Starred

- Pages: `folder-content.xhtml`, `starred.xhtml`
- Toggle favorite using `starred` boolean field

Bean/facade flow:

- `FileBean.toggleStar(file)` / `FolderContentBean.toggleStar(file)`
- `FileFacade.findStarredFiles(ownerId)`

#### Recent

- Page: `recent.xhtml`
- Returns current user files sorted by newest first

Bean/facade flow:

- `FileBean.getRecentFiles()`
- `FileFacade.findRecentFiles(ownerId)` ordered by `createdAt DESC`

### 6) Sharing and Permissions

- Page: `shared.xhtml`
- Share owned files with another user as `READ` or `WRITE`
- Prevent duplicate share entries for same file+recipient
- Hidden for deleted files

Bean/facade flow:

- `SharedFilesBean.shareFile()`
- `SharedFilesBean.removeSharedFile(sf)`
- `SharedFilesBean.changePermission(sf, permission)`

## UI Files

| File | Function | Main Bean(s) |
|---|---|---|
| `login.xhtml` | Login | `LoginBean` |
| `register.xhtml` | Registration | `UserBean` |
| `dashboard.xhtml` | Drive root and folders | `FolderBean`, `FileBean` |
| `new-folder.xhtml` | Create folder | `FolderBean` |
| `upload-file.xhtml` | Upload file | `FileBean`, `FolderBean` |
| `folder-content.xhtml` | Folder files, star/delete | `FolderContentBean` |
| `shared.xhtml` | Share management | `SharedFilesBean` |
| `starred.xhtml` | Starred files view | `FileBean` |
| `recent.xhtml` | Recent files view | `FileBean` |
| `trash.xhtml` | Restore/permanent delete | `TrashBean` |
| `template.xhtml` | Layout + sidebar navigation | Global |

## Bean Files

| Bean | Scope | Responsibility |
|---|---|---|
| `LoginBean` | `@ViewScoped` | Login/logout and session setup |
| `UserBean` | `@ViewScoped` | Registration and user CRUD |
| `FolderBean` | `@ViewScoped` | Folder create and soft delete tree |
| `FileBean` | `@ViewScoped` | Upload, file listing, starred/recent |
| `FolderContentBean` | `@ViewScoped` | Current folder file listing/actions |
| `SharedFilesBean` | `@ViewScoped` | Sharing workflows and permission checks |
| `TrashBean` | `@ViewScoped` | Trash listing, restore, permanent delete |

## Entity Schema Snapshot

| Entity | Important Columns |
|---|---|
| `Users` | `id`, `name`, `email`, `password` (BCrypt hash) |
| `Folders` | `id`, `name`, `createdAt`, `deleted`, `owner_id`, `parent_folder_id` |
| `Files` | `id`, `name`, `size`, `type`, `path`, `createdAt`, `deleted`, `starred`, `folder_id`, `owner_id` |
| `SharedFiles` | `id`, `file_id`, `recipient_id`, `permission` |

## Facade Responsibilities

- `UserFacade`: user CRUD + secure login verification
- `FolderFacade`: active/deleted folder queries
- `FileFacade`: active/deleted/starred/recent file queries
- `SharedFilesFacade`: share CRUD

## Configuration Notes

- `pom.xml`: includes `org.mindrot:jbcrypt` for password hashing
- `persistence.xml`: `eclipselink.ddl-generation=create-or-extend-tables`

## Current Feature Status

- Implemented: secure password hashing and verification
- Implemented: soft delete + Trash restore/permanent delete
- Implemented: starred files
- Implemented: recent files sorted by creation date
- Planned: advanced search/filtering, public links, backup/notifications

Last Update: 2026-05-02
