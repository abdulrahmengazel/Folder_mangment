# Quick Start

Get the project running quickly and verify the latest functionality.

## Prerequisites

- Java 23
- Maven 3.8+
- Jakarta EE server (example: GlassFish 7)

## 1) Open the project

```bash
cd /home/abdulrahman/Documents/Folder_mangment
java -version
mvn -version
```

## 2) Build

```bash
./mvnw clean package
```

Expected artifact:

```text
target/Folder_mangment-1.0-SNAPSHOT.war
```

## 3) Deploy (GlassFish example)

```bash
$GLASSFISH_HOME/bin/asadmin start-domain
$GLASSFISH_HOME/bin/asadmin deploy --force=true target/Folder_mangment-1.0-SNAPSHOT.war
```

## 4) Open the app

```text
http://localhost:8080/Folder_mangment/login.xhtml
```

## 5) 10-minute smoke test

Create a user, then validate the full lifecycle:

1. Register and log in.
2. Create a folder from `dashboard.xhtml`.
3. Upload a file from `upload-file.xhtml`.
4. Open `folder-content.xhtml` and star/unstar the file.
5. Open `recent.xhtml` and confirm newest-first ordering.
6. Open `starred.xhtml` and confirm starred items appear.
7. Delete a file/folder (soft delete).
8. Open `trash.xhtml`, restore it, then test permanent delete.
9. Share a non-deleted file in `shared.xhtml`.

## Where Features Live

| Feature | Page | Bean/Facade |
|---|---|---|
| Registration + password hashing | `register.xhtml` | `UserBean`, `UserFacade` |
| Login + password verification | `login.xhtml` | `LoginBean`, `UserFacade` |
| Create folder | `new-folder.xhtml` | `FolderBean`, `FolderFacade` |
| Upload file | `upload-file.xhtml` | `FileBean`, `FileFacade` |
| Folder files + star/delete | `folder-content.xhtml` | `FolderContentBean` |
| Recent files | `recent.xhtml` | `FileBean`, `FileFacade.findRecentFiles` |
| Starred files | `starred.xhtml` | `FileBean`, `FileFacade.findStarredFiles` |
| Trash lifecycle | `trash.xhtml` | `TrashBean`, `FileFacade`, `FolderFacade` |
| Sharing | `shared.xhtml` | `SharedFilesBean`, `SharedFilesFacade` |

## Troubleshooting

### Application not reachable

```bash
$GLASSFISH_HOME/bin/asadmin list-applications
$GLASSFISH_HOME/bin/asadmin list-domains
```

### Port already in use

```bash
$GLASSFISH_HOME/bin/asadmin set server.http-service.http-listener.http-listener-1.port=8081
```

### JDBC resource missing

```bash
$GLASSFISH_HOME/bin/asadmin list-jdbc-resources
```

## More Docs

- `README.md`
- `FEATURES.md`
- `TESTING_GUIDE.md`
- `INTEGRATION_CHECK.md`
- `INTEGRATION_DIAGRAMS.md`

Last Update: 2026-05-02
