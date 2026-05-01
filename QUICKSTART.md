# ⚡ Quick Start

## 🚀 Get up and running in 5 minutes

### Requirements
- ✅ Java 23+
- ✅ Maven 3.8.9+
- ✅ GlassFish 7 (or any Jakarta EE application server)

---

## 📥 Step 1: Preparation

```bash
# 1. Download the project
cd /path/to/Folder_mangment

# 2. Verify Java
java -version

# 3. Verify Maven
mvn -version
```

---

## 🔨 Step 2: Build

```bash
# Build the project
mvn clean package -DskipTests

# Result:
# ✅ BUILD SUCCESS
# 📦 target/Folder_mangment-1.0-SNAPSHOT.war
```

---

## 🚀 Step 3: Deployment

```bash
# Start GlassFish (if not already running)
$GLASSFISH_HOME/bin/asadmin start-domain

# Deploy the application
$GLASSFISH_HOME/bin/asadmin deploy --force=true \
  target/Folder_mangment-1.0-SNAPSHOT.war
```

---

## 🌐 Step 4: Access

```
http://localhost:8080/Folder_mangment/login.xhtml
```

---

## 👤 Step 5: Testing

### Create a test account:
```
Name: John Doe
Email: john.doe@example.com
Password: Test@123
```

### Then:
1. ✅ Create a folder
2. ✅ Upload a file
3. ✅ Share the file

---

## 🎯 Key Functionalities

| Functionality | Where to find |
|--------|----------------|
| **Registration** | register.xhtml |
| **Login** | login.xhtml |
| **Create Folder** | Dashboard → New folder (button) |
| **Upload File** | Dashboard → File upload (button) |
| **Sharing** | Dashboard → Right sidebar (Sharing Settings) |

---

## 🐛 Troubleshooting

### Error: Connection Refused
```bash
# Make sure GlassFish is started
$GLASSFISH_HOME/bin/asadmin start-domain
```

### Error: Port Already in Use
```bash
# Change the port or stop the other application
$GLASSFISH_HOME/bin/asadmin set \
  server.http-service.http-listener.http-listener-1.port=8081
```

### Error: Database Connection Failed
```bash
# Check the Data Source
$GLASSFISH_HOME/bin/asadmin list-jdbc-resources

# Recreate Connection Pool if necessary
$GLASSFISH_HOME/bin/asadmin create-jdbc-connection-pool \
  --datasourceclassname org.apache.derby.jdbc.embedded.EmbeddedDataSource \
  --property databaseName=/path/to/db:user=admin:password=admin \
  CloudDrivePool
```

---

## 📚 Further Information

| File | Topic |
|------|---------|
| **README.md** | Comprehensive Overview |
| **INSTALL.md** | Detailed Installation |
| **FEATURES.md** | Features Explanation |
| **TESTING_GUIDE.md** | Testing Plan |
| **COMPLETION_SUMMARY.md** | Completion Summary |

---

## 💡 Useful Tips

### Rapid Development
```bash
# Quick rebuild and deploy
mvn clean package -DskipTests && \
  $GLASSFISH_HOME/bin/asadmin deploy --force=true target/*.war
```

### View Logs
```bash
# In real-time
tail -f $GLASSFISH_HOME/domains/domain1/logs/server.log
```

### Access Admin Console
```
http://localhost:4848
Username: admin
Password: admin (or just press Enter)
```

---

## 🎉 That's it!

You are now ready to use the system!

**Status Report:** ✅ **Ready for Launch**

---

## 🆘 Need Help?

1. Read `README.md`
2. Read `INSTALL.md`
3. Open an Issue on GitHub
4. Contact us

---

**Last Update:** 2024-05-01 (Translated to English)
**Version:** 1.0.0