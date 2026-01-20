# Quick Setup Guide

## Step 1: Install Prerequisites

1. **Java JDK 8+**
   - Download from: https://www.oracle.com/java/technologies/downloads/
   - Verify installation: `java -version`

2. **MySQL Server**
   - Download from: https://dev.mysql.com/downloads/mysql/
   - Install and start MySQL service
   - Verify installation: `mysql --version`

3. **Maven (Optional)**
   - Download from: https://maven.apache.org/download.cgi
   - Verify installation: `mvn -version`

## Step 2: Database Setup

1. **Start MySQL Server**
   ```bash
   # Windows
   net start MySQL80
   
   # Linux/Mac
   sudo systemctl start mysql
   ```

2. **Create Database**
   ```sql
   mysql -u root -p
   CREATE DATABASE student_management;
   EXIT;
   ```

3. **Run Schema Script**
   ```bash
   mysql -u root -p student_management < src/main/resources/database/schema.sql
   ```

   Or manually:
   ```sql
   mysql -u root -p
   USE student_management;
   SOURCE src/main/resources/database/schema.sql;
   ```

## Step 3: Configure Database Connection

Edit `src/main/java/com/sims/database/DatabaseConnection.java`:

```java
private static final String URL = "jdbc:mysql://localhost:3306/student_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
private static final String USERNAME = "root";  // Change if needed
private static final String PASSWORD = "";      // Enter your MySQL password
```

## Step 4: Download MySQL JDBC Driver

1. Download `mysql-connector-java-8.0.33.jar` from:
   https://dev.mysql.com/downloads/connector/j/

2. Place it in your project's `lib/` folder (create if doesn't exist)

## Step 5: Build and Run

### Using Maven:
```bash
# Navigate to Manager directory
cd Manager

# Compile
mvn clean compile

# Run
mvn exec:java -Dexec.mainClass="com.sims.Main"
```

### Using IDE (Eclipse/IntelliJ):
1. Import project
2. Add MySQL JDBC driver to classpath:
   - Right-click project → Properties → Java Build Path → Libraries → Add External JARs
   - Select `mysql-connector-java-8.0.33.jar`
3. Run `Main.java`

### Manual Compilation:
```bash
# Create lib directory and add JDBC driver
mkdir -p lib
# Copy mysql-connector-java-8.0.33.jar to lib/

# Compile
javac -cp "lib/*:src/main/java" -d out src/main/java/com/sims/**/*.java

# Run
java -cp "lib/*:out" com.sims.Main
```

## Step 6: Login

Use default credentials:
- **Admin**: admin / admin123
- **Teacher**: teacher / teacher123
- **Student**: student / student123

## Troubleshooting

### "ClassNotFoundException: com.mysql.cj.jdbc.Driver"
- Ensure MySQL JDBC driver is in classpath
- Check if driver JAR file is accessible

### "Access denied for user"
- Verify MySQL username and password in `DatabaseConnection.java`
- Ensure MySQL user has privileges on `student_management` database

### "Unknown database 'student_management'"
- Run the schema.sql script to create database and tables
- Verify database name in connection URL

### "Table doesn't exist"
- Execute schema.sql script
- Check if all tables are created: `SHOW TABLES;` in MySQL

## Testing the Application

1. **Login as Admin**
   - Test student management (Add, Edit, Delete)
   - Test course management
   - Test attendance recording
   - Test grade entry
   - Test financial records

2. **Login as Teacher**
   - Test attendance recording
   - Test grade entry
   - Test course viewing

3. **Login as Student**
   - Test viewing personal records

## Next Steps

- Change default passwords for production use
- Implement password hashing
- Configure database connection pooling
- Add logging functionality
- Set up backup procedures
