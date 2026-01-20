# Student Information Management System

A comprehensive desktop application built with Java Swing for managing student records, attendance, courses, grades, and financial data.

## Technologies Used
- **Java** - Core programming language
- **JDBC** - Database connectivity
- **MySQL** - Database management
- **Swing** - GUI framework

## Features
- **Student Records Management**: Add, update, delete, and view student information
- **Attendance Tracking**: Record and monitor student attendance
- **Course Management**: Manage courses and enrollments
- **Grade Management**: Record and calculate student grades with GPA calculation
- **Financial Data**: Track fees, payments, and financial records
- **Role-Based Authentication**: Secure login system with different user roles (Admin, Teacher, Student)
- **Database Normalization**: Optimized database design (3NF) for data integrity
- **Performance-Optimized Queries**: Efficient SQL queries with proper indexing

## Project Structure
```
Manager/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── sims/
│   │   │           ├── Main.java
│   │   │           ├── database/
│   │   │           │   └── DatabaseConnection.java
│   │   │           ├── models/
│   │   │           │   ├── User.java
│   │   │           │   ├── Student.java
│   │   │           │   ├── Course.java
│   │   │           │   ├── Attendance.java
│   │   │           │   ├── Grade.java
│   │   │           │   └── FinancialRecord.java
│   │   │           ├── dao/
│   │   │           │   ├── UserDAO.java
│   │   │           │   ├── StudentDAO.java
│   │   │           │   ├── CourseDAO.java
│   │   │           │   ├── AttendanceDAO.java
│   │   │           │   ├── GradeDAO.java
│   │   │           │   └── FinancialDAO.java
│   │   │           ├── services/
│   │   │           │   └── AuthService.java
│   │   │           └── ui/
│   │   │               ├── LoginFrame.java
│   │   │               ├── DashboardFrame.java
│   │   │               ├── StudentManagementFrame.java
│   │   │               ├── CourseManagementFrame.java
│   │   │               ├── AttendanceManagementFrame.java
│   │   │               ├── GradeManagementFrame.java
│   │   │               └── FinancialManagementFrame.java
│   │   └── resources/
│   │       └── database/
│   │           └── schema.sql
├── pom.xml
└── README.md
```

## Prerequisites
- Java JDK 8 or higher
- MySQL Server 5.7 or higher
- Maven 3.6+ (optional, for building with Maven)
- MySQL JDBC Driver (mysql-connector-java)

## Database Setup

### 1. Create MySQL Database
```sql
CREATE DATABASE student_management;
```

### 2. Run Database Schema
Execute the SQL script to create all tables:
```bash
mysql -u root -p student_management < src/main/resources/database/schema.sql
```

Or manually run the SQL file in MySQL Workbench or command line.

### 3. Configure Database Connection
Update database credentials in `src/main/java/com/sims/database/DatabaseConnection.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/student_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
private static final String USERNAME = "root";
private static final String PASSWORD = "your_password";
```

## Building and Running

### Option 1: Using Maven
```bash
# Compile the project
mvn clean compile

# Run the application
mvn exec:java -Dexec.mainClass="com.sims.Main"
```

### Option 2: Using IDE (Eclipse/IntelliJ IDEA)
1. Import the project into your IDE
2. Add MySQL JDBC driver to classpath:
   - Download `mysql-connector-java-8.0.x.jar` from MySQL website
   - Add it to your project's build path
3. Run `Main.java`

### Option 3: Manual Compilation
```bash
# Compile all Java files
javac -cp ".:mysql-connector-java-8.0.x.jar" src/main/java/com/sims/**/*.java

# Run the application
java -cp ".:mysql-connector-java-8.0.x.jar:src/main/java" com.sims.Main
```

## Default Login Credentials
- **Admin**: 
  - Username: `admin`
  - Password: `admin123`
- **Teacher**: 
  - Username: `teacher`
  - Password: `teacher123`
- **Student**: 
  - Username: `student`
  - Password: `student123`

## Features Implementation Details

### Role-Based Authentication
- **Admin**: Full access to all modules (Student Management, Course Management, Attendance, Grades, Financial)
- **Teacher**: Access to Attendance, Grades, and Course Management
- **Student**: View-only access to personal records

### Database Normalization
- **Third Normal Form (3NF) compliance**
- Separate tables for students, courses, attendance, grades, users, and financial records
- Foreign key relationships for data integrity
- Proper indexing on frequently queried columns

### Performance Optimization
- **Indexed columns** for faster queries:
  - `username` in users table
  - `student_code` in students table
  - `course_code` in courses table
  - Composite indexes on (student_id, course_id) for attendance and grades
- **Prepared statements** to prevent SQL injection
- **Batch operations** for bulk data insertion
- **Optimized aggregate queries** for calculations (GPA, attendance percentage, balance)

### Key Modules

#### 1. Student Management
- Add, edit, delete, and search students
- View student details
- Manage student status (Active, Inactive, Graduated, Suspended)

#### 2. Course Management
- Create and manage courses
- Enroll students in courses
- Track course enrollments

#### 3. Attendance Management
- Record attendance for students in courses
- Batch attendance recording
- Calculate attendance percentage
- Filter by course and date

#### 4. Grade Management
- Record grades for assessments
- Calculate GPA (weighted by course credits)
- View grade history
- Automatic grade letter calculation

#### 5. Financial Management
- Record financial transactions (Fees, Payments, Refunds, Scholarships, Penalties)
- Track pending payments
- Calculate total balance
- Record payment details

## Database Schema Overview

### Tables
1. **users** - User accounts and authentication
2. **students** - Student information
3. **courses** - Course details
4. **course_enrollments** - Student-course relationships
5. **attendance** - Attendance records
6. **grades** - Grade records
7. **financial_records** - Financial transactions

### Relationships
- Students can be linked to users (for student login)
- Students can enroll in multiple courses
- Attendance and grades are linked to both students and courses
- Financial records are linked to students

## Troubleshooting

### Database Connection Issues
- Ensure MySQL server is running
- Verify database credentials in `DatabaseConnection.java`
- Check if database `student_management` exists
- Ensure MySQL JDBC driver is in classpath

### Compilation Errors
- Verify Java version (JDK 8+)
- Ensure all dependencies are in classpath
- Check for missing imports

### Runtime Errors
- Check database connection
- Verify all tables are created
- Ensure default users exist in database

## Future Enhancements
- Password encryption/hashing
- Report generation (PDF/Excel)
- Email notifications
- Advanced search and filtering
- Data export functionality
- Backup and restore features
- Multi-semester support
- Course prerequisites
- Attendance reports and analytics

## License
This project is created for educational purposes.

## Author
Student Information Management System - Developed with Java, JDBC, MySQL, and Swing
