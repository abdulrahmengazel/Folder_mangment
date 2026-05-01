# 📁 Cloud File Management System

An advanced local storage file management system, built using **Jakarta EE** and **JavaServer Faces (JSF)** with full support for sharing and permissions.

---

## 🎯 Key Features

✅ **User Management**
- Secure new account registration with data validation
- Secure login mechanism
- Profile management

✅ **File and Folder Management**
- Create multi-level folder structures
- Upload files of various sizes
- Delete files and folders
- View file details (Name, Size, Date)

✅ **File Sharing and Permissions**
- Share files with other users
- Two permission levels: **Viewer (Read)** and **Editor (Write)**
- Manage shares (Add and Revoke)
- Easily modify permissions

✅ **Modern User Interface**
- Google Drive-inspired design
- Responsive and user-friendly interface
- Clear error messages and alerts

---

## 🏗️ Architecture

```
src/
├── main/
│   ├── java/
│   │   ├── bean/                    # JSF Managed Beans
│   │   │   ├── LoginBean.java       # Login Handling
│   │   │   ├── UserBean.java        # User Management
│   │   │   ├── FolderBean.java      # Folder Management
│   │   │   ├── FileBean.java        # File Management
│   │   │   ├── SharedFilesBean.java # Sharing Management
│   │   │   └── FolderContentBean.java # Folder View Logic
│   │   │
│   │   ├── entity/                  # JPA Entities
│   │   │   ├── Users.java
│   │   │   ├── Folders.java
│   │   │   ├── Files.java
│   │   │   └── SharedFiles.java
│   │   │
│   │   ├── facade/                  # EJB Stateless Services
│   │   │   ├── AbstractFacade.java
│   │   │   ├── UserFacade.java
│   │   │   ├── FolderFacade.java
│   │   │   ├── FileFacade.java
│   │   │   └── SharedFilesFacade.java
│   │   │
│   │   ├── facadeLocal/             # Local Interfaces
│   │   │   ├── UserFacadeLocal.java
│   │   │   ├── FolderFacadeLocal.java
│   │   │   ├── FileFacadeLocal.java
│   │   │   └── SharedFilesFacadeLocal.java
│   │   │
│   │   └── enums/                   # Enumerations
│   │       └── PermissionEnum.java  # READ, WRITE
│   │
│   ├── webapp/
│   │   ├── login.xhtml              # Login Page
│   │   ├── register.xhtml           # Registration Page
│   │   ├── dashboard.xhtml          # Main Dashboard
│   │   ├── template.xhtml           # Layout Template
│   │   ├── folder-content.xhtml     # Folder Contents View
│   │   ├── new-folder.xhtml         # Create Folder View
│   │   ├── shared.xhtml             # Shared Files View
│   │   ├── upload-file.xhtml        # File Upload View
│   │   ├── WEB-INF/
│   │   │   └── web.xml              # Application Configuration
│   │   └── resources/
│   │       └── css/
│   │           └── style.css        # Styles
│   │
│   └── resources/
│       └── META-INF/
│           ├── persistence.xml      # JPA Configuration
│           └── beans.xml            # CDI Configuration
│
└── pom.xml                          # Maven Build File
```

---

## 🗄️ Data Model

### Users
```
id          : Long (Primary Key)
name        : String
email       : String (Unique)
password    : String
```

### Folders
```
id              : Long (Primary Key)
name            : String
createdAt       : Date
owner_id        : Long (FK → Users)
parent_folder_id: Long (FK → Folders, for hierarchical structure)
```

### Files
```
id        : Long (Primary Key)
name      : String
size      : Long
type      : String (MIME Type)
path      : String (System path)
folder_id : Long (FK → Folders)
owner_id  : Long (FK → Users)
```

### SharedFiles
```
id          : Long (Primary Key)
file_id     : Long (FK → Files)
recipient_id: Long (FK → Users)
permission  : Enum (READ, WRITE)
```

---

## 🔧 Technologies Used

| Technology | Version | Purpose |
|--------|--------|---------|
| **Java** | 23 | Programming Language |
| **Jakarta EE** | 10.0.0 | Core Framework |
| **Jakarta Faces (JSF)** | Included | User Interface |
| **JPA (EclipseLink)** | 4.0.2 | Database ORM |
| **Maven** | - | Build Tool |
| **EJB 4.0** | Included | Business Services |
| **JUnit 5** | 5.10.2 | Testing |

---

## 📋 Core Functionalities

### 1. **Login (LoginBean)**
```java
public String login()
// Validates user credentials
// Creates user session
// Redirects to dashboard
```

### 2. **User Management (UserBean)**
```java
public String createUser()        // Creates a new account
public void editUser()            // Edits user data
public void deleteUser(Users u)   // Deletes a user
```

### 3. **Folder Management (FolderBean)**
```java
public String createFolder()      // Creates a folder
public void deleteFolder(Folders) // Deletes a folder
```

### 4. **File Management (FileBean)**
```java
public String uploadFile()        // Uploads a file
public List<Files> getFilesList() // Retrieves user files
```

### 5. **File Sharing (SharedFilesBean)**
```java
public void shareFile()                        // Shares a file
public void removeSharedFile(SharedFiles)      // Revokes a share
public void changePermission(SharedFiles, Perm) // Modifies permissions
public List<SharedFiles> getSharedWithMe()     // Files shared with me
public List<SharedFiles> getMySharedFiles()    // Files I shared
```

---

## 🚀 How to Run

### Requirements
- **JDK 23** or newer
- **Maven 3.8.9** or newer
- **Application Server** (GlassFish 7+, Tomcat 10+)
- **Database** (Derby, MySQL, PostgreSQL)

### Execution Steps

1. **Clone the repository:**
```bash
git clone https://github.com/your-repo/Folder_mangment.git
cd Folder_mangment
```

2. **Build the project:**
```bash
mvn clean install
```

3. **Deploy the application:**
```bash
# For GlassFish
asadmin deploy target/Folder_mangment-1.0-SNAPSHOT.war

# For Tomcat
cp target/Folder_mangment-1.0-SNAPSHOT.war $CATALINA_HOME/webapps/
```

4. **Access the application:**
```
http://localhost:8080/Folder_mangment/login.xhtml
```

---

## 📖 User Guide

### 1️⃣ Create a New Account
- Navigate to the registration page.
- Enter your full name, email, and password.
- Click "Create account".

### 2️⃣ Sign In
- Enter your email and password.
- Click "Next".

### 3️⃣ Create Folders
- In the dashboard, click "New folder".
- Enter the folder name.
- Click "Create".

### 4️⃣ Upload Files
- Click "File upload".
- Select the target folder.
- Choose the file to upload.
- Click "Upload".

### 5️⃣ Share Files
- Go to "Shared with me" or the sharing settings panel.
- Select the file and the user.
- Choose the permission level (Viewer/Editor).
- Click "Share".

---

## ⚙️ Important Configurations

### persistence.xml
```xml
<persistence-unit name="CloudDrivePu" transaction-type="JTA">
    <provider>org.eclipse.persistence.jpa.PersistenceProvider</provider>
    <jta-data-source>jdbc/CloudDrivePu</jta-data-source>
    <properties>
        <property name="eclipselink.ddl-generation" value="create-or-extend-tables"/>
    </properties>
</persistence-unit>
```

### web.xml
```xml
<!-- JSF Configuration -->
<servlet>
    <servlet-name>Faces Servlet</servlet-name>
    <servlet-class>jakarta.faces.webapp.FacesServlet</servlet-class>
    <load-on-startup>1</load-on-startup>
</servlet>

<servlet-mapping>
    <servlet-name>Faces Servlet</servlet-name>
    <url-pattern>*.xhtml</url-pattern>
</servlet-mapping>
```

---

## 🐛 Error Handling

The system provides clear error messages in the following scenarios:
- ❌ Invalid input data
- ❌ Attempting to register an existing email
- ❌ File upload failure
- ❌ Attempting to delete a protected folder
- ❌ Attempting to share a file you do not own

---

## 🔐 Security

✅ Server-side data validation
✅ User permissions verification
✅ Use of JPA to prevent SQL Injection
✅ Secure sessions management

---

## 📈 Future Enhancements

- [ ] Advanced search and sorting
- [ ] Mark files as starred/favorite
- [ ] Trash/Recycle bin functionality
- [ ] Public sharing (public links)
- [ ] Sort by date and size
- [ ] Support for ZIP archives
- [ ] Automatic backups
- [ ] Email notifications

---

## 👨‍💻 Contributing

You can contribute to the project's development:
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

- 📧 Email: [your-email@example.com]
- 🐙 GitHub: [your-github-profile]
- 💬 Discord: [your-discord-username]

---

## 🙏 Acknowledgments

Special thanks to:
- The Jakarta EE Team
- The Java Community
- All contributors and supporters

---

**Last Update:** 2024-05-01 (Translated to English)
**Version:** 1.0.0
**Status:** ✅ Ready for Production