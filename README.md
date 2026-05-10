# 📁 Cloud File Management System

An advanced local storage file management system, built using **Jakarta EE** and **JavaServer Faces (JSF)** with full support for sharing, permissions, and file lifecycle management.

---

## 🎯 Key Features

✅ **User Management & Security**
- Secure account registration with **BCrypt password hashing**
- Secure login mechanism with hashed password verification
- Profile management and account deletion

✅ **Hierarchical File and Folder Management**
- Create **multi-level nested folder structures** (Folders inside Folders)
- Upload files of various sizes directly to disk
- Soft delete files and nested folders (moved to Trash)
- View file details (Name, Size, Date, Type)

✅ **File Sharing and Permissions**
- Share files with other users
- Two permission levels: **Viewer (Read)** and **Editor (Write)**
- Manage shares (Add, Update, Revoke)
- Secure verification to ensure only owners can share

✅ **Lifecycle Management & Organization**
- **Trash/Recycle Bin:** Restore soft-deleted items or permanently delete them
- **Starred Files:** Mark important files as favorites for quick access
- **Recent Files:** Automatically view recently uploaded files sorted by newest first

✅ **Modern User Interface**
- Google Drive-inspired design built with JSF and CSS
- Responsive and user-friendly interface fully localized in **English**
- Clear error messages and alerts for smooth UX

---

## 🐳 Running with Docker

For a quick and easy setup, you can run the entire application using Docker.

1. **Prerequisites:**
   - Docker
   - Docker Compose

2. **Build and Run:**
   Clone the repository and run the following command from the project root:
   ```bash
   docker-compose up --build
   ```
   This will:
   - Build the Java application using Maven.
   - Create a Docker image with the Payara Micro server.
   - Start the application container on port `8080`.
   - Start a PostgreSQL database container.

3. **Access the application:**
   ```text
   http://localhost:8080/
   ```

---

## 🏗️ Architecture

```
src/
├── main/
│   ├── java/
│   │   ├── bean/                    # JSF Managed Beans (@ViewScoped)
│   │   │   ├── LoginBean.java
│   │   │   ├── UserBean.java
│   │   │   ├── FolderBean.java      # Hierarchical Folder Creation
│   │   │   ├── FileBean.java
│   │   │   ├── SharedFilesBean.java
│   │   │   ├── FolderContentBean.java # Handles Subfolders & Files view
│   │   │   └── TrashBean.java       # Manages Trash Lifecycle
│   │   │
│   │   ├── entity/                  # JPA Entities
│   │   │   ├── Users.java
│   │   │   ├── Folders.java         # Contains self-referencing parent_folder_id
│   │   │   ├── Files.java
│   │   │   └── SharedFiles.java
│   │   │
│   │   ├── facade/                  # EJB Stateless Services
│   │   │   ├── UserFacade.java
│   │   │   ├── FolderFacade.java
│   │   │   ├── FileFacade.java
│   │   │   └── SharedFilesFacade.java
│   │   │
│   │   ├── filter/
│   │   │   └── AuthFilter.java      # Route protection & security
│   │   │
│   │   └── enums/                   # Enumerations
│   │       └── PermissionEnum.java  # READ, WRITE
│   │
│   ├── webapp/                      # JSF Facelets (UI)
│   │   ├── login.xhtml, register.xhtml
│   │   ├── dashboard.xhtml, template.xhtml
│   │   ├── folder-content.xhtml, new-folder.xhtml
│   │   ├── shared.xhtml, upload-file.xhtml
│   │   ├── profile.xhtml, recent.xhtml, starred.xhtml, trash.xhtml
│   │   ├── WEB-INF/web.xml
│   │   └── resources/css/style.css
│   │
│   └── resources/META-INF/
│       ├── persistence.xml          # JPA Configuration
│       └── beans.xml                # CDI Configuration
│
├── .github/workflows/               # CI/CD
│   └── maven.yml                    # Automated build & testing
├── Dockerfile                       # Container Build script
└── docker-compose.yml               # Multi-container orchestration
```

---

## 🔧 Technologies Used

| Technology | Version | Purpose |
|--------|--------|---------|
| **Java** | 23 | Programming Language |
| **Jakarta EE** | 10.0.0 | Core Framework |
| **Jakarta Faces (JSF)** | Included | User Interface |
| **JPA (EclipseLink)** | 4.0.2 | Database ORM |
| **EJB 4.0** | Included | Business Services |
| **jBCrypt** | 0.4 | Password Security |
| **Maven** | 3.8+ | Build Tool |
| **Docker** | - | Containerization & Deployment |
| **GitHub Actions** | - | Continuous Integration (CI) |

---

## 🚀 Manual Run (Without Docker)

### Requirements
- **JDK 23** or newer
- **Maven 3.8+**
- **Application Server** (e.g., GlassFish 7+, Payara, Tomcat 10+)
- **Database** (Configured via your application server's JDBC pool `jdbc/CloudDrivePu`)

### Execution Steps

1. **Clone the repository:**
```bash
git clone https://github.com/abdulrahmengazel/Folder_mangment.git
cd Folder_mangment
```

2. **Build the project:**
```bash
./mvnw clean package
```

3. **Deploy the application:**
```bash
# For GlassFish
asadmin start-domain
asadmin deploy --force=true target/Folder_mangment-1.0-SNAPSHOT.war
```

4. **Access the application:**
```text
http://localhost:8080/Folder_mangment/login.xhtml
```

*(Note: Physical files are saved to `/home/abdulrahman/cloud_uploads` by default. Update the `ROOT_UPLOAD_DIR` in the Beans if running on a different environment/OS.)*

---

## 📈 Future Enhancements

- [ ] Advanced search and sorting
- [ ] Public sharing (generate public download links)
- [ ] Support for ZIP archives (Upload/Download)
- [ ] Automatic backups
- [ ] Email notifications

---

## 👨‍💻 Contributing

Contributions are welcome!
1. Fork the project
2. Create a new branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 📞 Contact

- 📧 Email: abdulrahmengazel@gmail.com
- 🐙 GitHub: [github.com/abdulrahmengazel](https://github.com/abdulrahmengazel)

---

**Version:** 1.1.0
**Status:** ✅ Ready for Open Source / Production