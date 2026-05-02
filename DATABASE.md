# Database Reference

This document describes the current database model used by the Cloud File Management System.

## Scope

- JPA entities: `Users`, `Folders`, `Files`, `SharedFiles`
- Key relationships and constraints
- Soft delete and lifecycle behavior
- Query patterns used by the application
- SQL verification queries for manual checks

## Persistence Configuration

Defined in `src/main/resources/META-INF/persistence.xml`:

- Persistence unit: `CloudDrivePu`
- Provider: EclipseLink
- Data source: `jdbc/CloudDrivePu`
- DDL generation: `create-or-extend-tables`

## Tables and Columns

### `Users`

Source: `src/main/java/entity/Users.java`

| Column | Type (logical) | Constraints | Notes |
|---|---|---|---|
| `id` | Long | PK, identity | Primary key |
| `Name` | String(100) | NOT NULL | Display name |
| `Email` | String(100) | NOT NULL, UNIQUE | Login identifier |
| `Password` | String(100) | NOT NULL | Stores BCrypt hash, not plain text |

### `Folders`

Source: `src/main/java/entity/Folders.java`

| Column | Type (logical) | Constraints | Notes |
|---|---|---|---|
| `id` | Long | PK, identity | Primary key |
| `Name` | String(100) | NOT NULL | Folder name |
| `Created_At` | Timestamp | NOT NULL | Auto-set via `@PrePersist` |
| `owner_id` | Long | FK -> `Users.id`, NOT NULL | Folder owner |
| `parent_folder_id` | Long | FK -> `Folders.id`, NULL | Parent for nested folders |
| `Deleted` | Boolean | NOT NULL, default `false` | Soft delete flag |

### `Files`

Source: `src/main/java/entity/Files.java`

| Column | Type (logical) | Constraints | Notes |
|---|---|---|---|
| `id` | Long | PK, identity | Primary key |
| `Name` | String(200) | NOT NULL | Original file name |
| `Size` | Long | NOT NULL | File size in bytes |
| `Type` | String(50) | NOT NULL | MIME type |
| `Path` | String(500) | NOT NULL | Physical path on disk |
| `Created_At` | Timestamp | NULL in DB, set in app | Auto-set via `@PrePersist` |
| `folder_id` | Long | FK -> `Folders.id`, NULL | Containing folder |
| `owner_id` | Long | FK -> `Users.id`, NOT NULL | File owner |
| `Deleted` | Boolean | NOT NULL, default `false` | Soft delete flag |
| `Starred` | Boolean | NOT NULL, default `false` | Favorite marker |

### `SharedFiles`

Source: `src/main/java/entity/SharedFiles.java`

| Column | Type (logical) | Constraints | Notes |
|---|---|---|---|
| `id` | Long | PK, identity | Primary key |
| `file_id` | Long | FK -> `Files.id`, NOT NULL | Shared file |
| `recipient_id` | Long | FK -> `Users.id`, NOT NULL | Target user |
| `Permission` | Enum string | NOT NULL | `READ` or `WRITE` |

## Relationship Model

- One `Users` -> many `Folders` (owner)
- One `Users` -> many `Files` (owner)
- One `Folders` -> many `Files` (`folder_id`)
- One `Folders` -> many child `Folders` (`parent_folder_id`)
- One `Files` -> many `SharedFiles`
- One `Users` (recipient) -> many `SharedFiles`

## Lifecycle Semantics

### Password Security

- Passwords are hashed using BCrypt in registration flow (`UserBean.createUser()`).
- Password validation uses BCrypt (`UserFacade.login(...)`).

### Soft Delete

- Files and folders are not immediately removed from the database when deleted from active UI.
- The app sets `Deleted=true` and hides records from active lists.
- Deleted records are shown in Trash views.

### Restore and Permanent Delete

- Restore sets `Deleted=false`.
- Permanent delete removes database records and attempts file system cleanup.
- File permanent delete also removes related rows from `SharedFiles`.

### Starred and Recent

- Starred files: `Starred=true` and `Deleted=false`.
- Recent files: sorted by `Created_At` descending and `Deleted=false`.

## Facade Query Behavior

From `src/main/java/facade/FileFacade.java` and `src/main/java/facade/FolderFacade.java`:

- `FileFacade.findAll()` -> files where `Deleted=false`
- `FileFacade.findDeleted()` -> files where `Deleted=true`
- `FileFacade.findStarredFiles(ownerId)` -> owner + `Deleted=false` + `Starred=true`, newest first
- `FileFacade.findRecentFiles(ownerId)` -> owner + `Deleted=false`, newest first
- `FolderFacade.findAll()` -> folders where `Deleted=false`
- `FolderFacade.findDeleted()` -> folders where `Deleted=true`

## SQL Verification Queries

Use SQL syntax appropriate for your database.

### 1) Users and password hash check

```sql
SELECT id, Email, Password
FROM Users;
```

Expected: `Password` values are BCrypt hashes (not plain text).

### 2) Active vs deleted folders

```sql
SELECT id, Name, Deleted, owner_id, parent_folder_id
FROM Folders
ORDER BY id;
```

### 3) Active vs deleted files

```sql
SELECT id, Name, Deleted, Starred, owner_id, folder_id, Created_At
FROM Files
ORDER BY Created_At DESC;
```

### 4) Starred files for one owner

```sql
SELECT id, Name, Starred, Deleted, owner_id, Created_At
FROM Files
WHERE owner_id = ?
  AND Starred = true
  AND Deleted = false
ORDER BY Created_At DESC;
```

### 5) Recent files for one owner

```sql
SELECT id, Name, Deleted, owner_id, Created_At
FROM Files
WHERE owner_id = ?
  AND Deleted = false
ORDER BY Created_At DESC;
```

### 6) Share records and consistency

```sql
SELECT sf.id, sf.file_id, sf.recipient_id, sf.Permission
FROM SharedFiles sf
ORDER BY sf.id;
```

### 7) Optional duplicate-share audit

```sql
SELECT file_id, recipient_id, COUNT(*) AS cnt
FROM SharedFiles
GROUP BY file_id, recipient_id
HAVING COUNT(*) > 1;
```

Expected: zero rows.

## Operational Notes

- The application also stores physical files under `/home/abdulrahman/cloud_uploads`.
- Database soft delete state and physical storage cleanup are both part of lifecycle operations.
- For production, consider adding explicit DB indexes on `owner_id`, `Deleted`, and `Created_At` columns for list queries.

Last Update: 2026-05-02

