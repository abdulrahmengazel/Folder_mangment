# Testing Guide

This guide focuses on current implemented behavior, including secure authentication, soft delete, trash restore/permanent delete, starred files, and recent files.

## Test Scope

- Authentication and password security
- Folder and file lifecycle
- Trash lifecycle (restore and permanent delete)
- Starred and recent file features
- Sharing and permission checks

## A. Manual Functional Tests

### A1) Registration and Login

#### T-A1-01 Register a new user

Steps:

1. Open `register.xhtml`
2. Enter name, email, and password
3. Submit

Expected:

- User is created
- Redirect to login page
- Password is not stored in plain text

#### T-A1-02 Login with correct password

Steps:

1. Open `login.xhtml`
2. Enter valid email/password
3. Submit

Expected:

- Login succeeds
- Redirect to `dashboard.xhtml`

#### T-A1-03 Login with wrong password

Expected:

- Login fails with error message

### A2) Folder and File Core Operations

#### T-A2-01 Create folder

Expected:

- Folder appears in dashboard list
- Physical directory is created under `/home/abdulrahman/cloud_uploads`

#### T-A2-02 Upload file

Expected:

- File appears in `folder-content.xhtml`
- DB metadata saved (`name`, `size`, `type`, `path`, `createdAt`)

### A3) Soft Delete and Trash Lifecycle

#### T-A3-01 Soft delete a file

Steps:

1. Open `folder-content.xhtml`
2. Delete a file

Expected:

- File disappears from normal folder list
- File appears in `trash.xhtml`
- Record remains in DB with `deleted=true`

#### T-A3-02 Soft delete a folder

Expected:

- Folder disappears from dashboard
- Folder appears in `trash.xhtml`
- Nested files/folders are marked deleted

#### T-A3-03 Restore from trash

Expected:

- Restored item returns to active views
- `deleted=false`

#### T-A3-04 Permanent delete from trash

Expected:

- Record removed from DB
- Physical file/folder cleanup attempted
- Related share entries removed for deleted files

### A4) Starred and Recent Features

#### T-A4-01 Toggle star in folder-content

Expected:

- Star state changes and persists
- Appears/disappears in `starred.xhtml`

#### T-A4-02 Recent ordering

Expected:

- `recent.xhtml` shows newest-first based on `createdAt`
- Deleted files do not appear

### A5) Sharing and Permissions

#### T-A5-01 Share file as owner

Expected:

- Share row created in `SharedFiles`
- Recipient sees file in "shared with me"

#### T-A5-02 Prevent duplicate share

Expected:

- Duplicate share attempt is blocked with warning

#### T-A5-03 Prevent sharing deleted file

Expected:

- Share is rejected for files in trash

## B. Validation and Security Tests

### B1 Input validation

- Missing required fields on login/register/upload/share must show errors

### B2 Ownership enforcement

- User cannot delete or restore another user item
- User cannot share file they do not own

### B3 Password security checks

- Verify stored `Users.password` begins with BCrypt format (example prefix: `$2a$`, `$2b$`, or `$2y$`)
- Verify login only passes for correct plain password against hash

## C. Database Verification Queries

Use equivalent SQL for your database dialect.

```sql
SELECT id, email, password FROM Users;
SELECT id, name, deleted, created_at FROM Folders;
SELECT id, name, deleted, starred, created_at FROM Files ORDER BY created_at DESC;
SELECT id, file_id, recipient_id, permission FROM SharedFiles;
```

Checks:

- Deleted rows stay in DB until permanently deleted
- Starred rows are represented by `starred=true`
- Recent ordering aligns with `created_at`

## D. Regression Checklist

- Registration + login still work after schema extension
- Dashboard hides deleted files/folders
- Trash restore updates active views immediately
- Trash permanent delete removes DB row and cleanup is attempted
- Shared lists exclude deleted files
- Star and recent pages only show current user data

## E. Recommended Build/Test Commands

```bash
cd /home/abdulrahman/Documents/Folder_mangment
./mvnw test
./mvnw package
```

Last Update: 2026-05-02
