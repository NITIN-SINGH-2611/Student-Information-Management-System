// Data Storage using localStorage (similar to data.json)
class DataStorage {
    constructor() {
        this._readyResolve = null;
        this.ready = new Promise(function(r) { this._readyResolve = r; }.bind(this));
        this.loadDataFromFile();
    }

    async loadDataFromFile() {
        try {
            const response = await fetch('data.json');
            if (response.ok) {
                const fileData = await response.json();
                const localData = localStorage.getItem('sims_data');
                if (localData) {
                    const local = JSON.parse(localData);
                    if (!local.students || local.students.length === 0) local.students = fileData.students || [];
                    if (!local.courses || local.courses.length === 0) local.courses = fileData.courses || [];
                    if (!local.enrollments || local.enrollments.length === 0) local.enrollments = fileData.enrollments || [];
                    if (!local.attendance || local.attendance.length === 0) local.attendance = fileData.attendance || [];
                    if (!local.grades || local.grades.length === 0) local.grades = fileData.grades || [];
                    if (!local.financialRecords || local.financialRecords.length === 0) local.financialRecords = fileData.financialRecords || [];
                    localStorage.setItem('sims_data', JSON.stringify(local));
                } else {
                    localStorage.setItem('sims_data', JSON.stringify(fileData));
                }
                if (this._readyResolve) this._readyResolve();
                return;
            }
        } catch (error) {
            console.log('Could not load data.json, using default data');
        }
        this.initDefaultData();
        if (this._readyResolve) this._readyResolve();
    }

    initDefaultData() {
        if (!localStorage.getItem('sims_data')) {
            var d = {
                users: [
                    { user_id: 1, username: "admin", password: "admin123", role: "ADMIN", email: "admin@school.edu" },
                    { user_id: 2, username: "teacher", password: "teacher123", role: "TEACHER", email: "teacher@school.edu" },
                    { user_id: 3, username: "student", password: "student123", role: "STUDENT", email: "student@school.edu" }
                ],
                students: [], courses: [], enrollments: [], attendance: [], grades: [], financialRecords: []
            };
            localStorage.setItem('sims_data', JSON.stringify(d));
        }
    }

    getData() {
        return JSON.parse(localStorage.getItem('sims_data') || '{}');
    }

    saveData(data) {
        localStorage.setItem('sims_data', JSON.stringify(data));
    }

    getUsers() { return this.getData().users || []; }
    getStudents() { return this.getData().students || []; }
    getCourses() { return this.getData().courses || []; }
    getEnrollments() { return this.getData().enrollments || []; }
    getAttendance() { return this.getData().attendance || []; }
    getGrades() { return this.getData().grades || []; }
    getFinancialRecords() { return this.getData().financialRecords || []; }

    setStudents(students) {
        const data = this.getData();
        data.students = students;
        this.saveData(data);
    }

    setCourses(courses) {
        const data = this.getData();
        data.courses = courses;
        this.saveData(data);
    }

    setEnrollments(enrollments) {
        const data = this.getData();
        data.enrollments = enrollments;
        this.saveData(data);
    }

    setAttendance(attendance) {
        const data = this.getData();
        data.attendance = attendance;
        this.saveData(data);
    }

    setGrades(grades) {
        const data = this.getData();
        data.grades = grades;
        this.saveData(data);
    }

    setFinancialRecords(records) {
        const data = this.getData();
        data.financialRecords = records;
        this.saveData(data);
    }

    getNextId(entityType) {
        const data = this.getData();
        const entityMap = {
            'user': 'users',
            'student': 'students',
            'course': 'courses',
            'enrollment': 'enrollments',
            'attendance': 'attendance',
            'grade': 'grades',
            'financial': 'financialRecords'
        };
        const entities = data[entityMap[entityType]] || [];
        let maxId = 0;
        const idKey = entityType + '_id';
        entities.forEach(entity => {
            const id = entity[idKey] || 0;
            if (id > maxId) maxId = id;
        });
        return maxId + 1;
    }
}

// Authentication
class AuthService {
    constructor(storage) {
        this.storage = storage;
        this.currentUser = null;
    }

    login(username, password) {
        const users = this.storage.getUsers();
        const user = users.find(u => u.username === username && u.password === password);
        if (user) {
            this.currentUser = user;
            sessionStorage.setItem('currentUser', JSON.stringify(user));
            return user;
        }
        return null;
    }

    getCurrentUser() {
        if (!this.currentUser) {
            const stored = sessionStorage.getItem('currentUser');
            if (stored) {
                this.currentUser = JSON.parse(stored);
            }
        }
        return this.currentUser;
    }

    logout() {
        this.currentUser = null;
        sessionStorage.removeItem('currentUser');
    }

    isAdmin() { return this.getCurrentUser()?.role === 'ADMIN'; }
    isTeacher() { return this.getCurrentUser()?.role === 'TEACHER'; }
    isStudent() { return this.getCurrentUser()?.role === 'STUDENT'; }
}

// Application State
const storage = new DataStorage();
const authService = new AuthService(storage);
let selectedStudentId = null;
let selectedCourseId = null;

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    var loginForm = document.getElementById('loginForm');
    if (loginForm) loginForm.addEventListener('submit', handleLogin);

    function go() {
        var cu = authService.getCurrentUser();
        if (cu) showDashboard(); else showLogin();
    }

    Promise.race([ storage.ready, new Promise(function(r){ setTimeout(r, 2500); }) ])
        .then(go)
        .catch(function() { go(); });
});

function handleLogin(e) {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const errorMsg = document.getElementById('errorMessage');
    const user = authService.login(username, password);
    if (user) {
        errorMsg.textContent = '';
        showDashboard();
    } else {
        errorMsg.textContent = 'Invalid username or password.';
    }
}

function showLogin() {
    document.body.classList.remove('dashboard-view');
    var lc = document.getElementById('loginContainer');
    var dc = document.getElementById('dashboardContainer');
    if (lc) lc.style.display = 'flex';
    if (dc) dc.style.display = 'none';
}

function showDashboard() {
    try {
        var user = authService.getCurrentUser();
        if (!user) {
            console.error('No user found');
            showLogin();
            return;
        }

        var lc = document.getElementById('loginContainer');
        var dc = document.getElementById('dashboardContainer');
        var welcome = document.getElementById('dashboardWelcome');
        var menu = document.getElementById('menuBar');
        var content = document.getElementById('contentArea');

        if (!dc) {
            console.error('Dashboard container not found!');
            return;
        }

        if (lc) lc.style.display = 'none';
        dc.style.display = 'block';
        dc.style.visibility = 'visible';
        dc.style.opacity = '1';
        document.body.classList.add('dashboard-view');

        if (welcome) welcome.textContent = 'Welcome, ' + (user.username || '') + ' (' + (user.role || '') + ')';

        if (menu) {
            menu.innerHTML =
                '<button type="button" onclick="showHome()">Home</button>' +
                (authService.isAdmin() || authService.isTeacher() ? '<button type="button" onclick="showStudentManagement()">Student Management</button>' : '') +
                (authService.isAdmin() || authService.isTeacher() ? '<button type="button" onclick="showCourseManagement()">Course Management</button>' : '') +
                (authService.isAdmin() || authService.isTeacher() ? '<button type="button" onclick="showAttendanceManagement()">Attendance</button>' : '') +
                (authService.isAdmin() || authService.isTeacher() ? '<button type="button" onclick="showGradeManagement()">Grades</button>' : '') +
                (authService.isAdmin() ? '<button type="button" onclick="showFinancialManagement()">Financial</button>' : '');
        }

        if (content) {
            showHome();
        } else {
            console.error('Content area not found!');
        }
    } catch (e) {
        console.error('Error in showDashboard:', e);
        alert('Error loading dashboard. Please refresh the page.');
    }
}

function showHome() {
    try {
        var user = authService.getCurrentUser();
        if (!user) {
            console.error('No user in showHome');
            return;
        }
        var el = document.getElementById('contentArea');
        if (!el) {
            console.error('Content area not found in showHome');
            return;
        }
        var roleText = '';
        if (user.role === 'ADMIN') roleText = '<p>You have full access to students, courses, attendance, grades, and financial records.</p>';
        else if (user.role === 'TEACHER') roleText = '<p>You can view and manage students, courses, attendance, and grades.</p>';
        else if (user.role === 'STUDENT') roleText = '<p>You have view-only access to your records.</p>';
        
        el.innerHTML = 
            '<div class="home-welcome">' +
            '<h3>Welcome to Student Information Management System</h3>' +
            '<p>Use the menu above to navigate to different modules.</p>' +
            '<p><strong>Your role:</strong> ' + (user.role || '') + '</p>' +
            roleText +
            '</div>';
    } catch (e) {
        console.error('Error in showHome:', e);
    }
}

function handleLogout() {
    authService.logout();
    location.reload();
}

// Student Management
function showStudentManagement() {
    const isAdmin = authService.isAdmin();
    document.getElementById('contentArea').innerHTML = `
        <div class="search-bar">
            <input type="text" id="searchInput" placeholder="Search by name..." onkeyup="searchStudents()">
            <button class="btn btn-primary" onclick="searchStudents()">Search</button>
            ${isAdmin ? '<button class="btn btn-success" onclick="showAddStudentModal()">Add Student</button>' : ''}
            <button class="btn btn-secondary" onclick="loadStudents()">Refresh</button>
        </div>
        <table id="studentsTable">
            <thead>
                <tr><th>ID</th><th>Student Code</th><th>First Name</th><th>Last Name</th><th>Email</th><th>Phone</th><th>Status</th></tr>
            </thead>
            <tbody id="studentsTableBody"></tbody>
        </table>
        <div class="action-buttons">
            <button class="btn btn-primary" onclick="viewStudentDetails()">View Details</button>
            ${isAdmin ? '<button class="btn btn-primary" onclick="editStudent()">Edit</button>' : ''}
            ${isAdmin ? '<button class="btn btn-danger" onclick="deleteStudent()">Delete</button>' : ''}
        </div>
    `;
    loadStudents();
}

function loadStudents() {
    const students = storage.getStudents();
    const tbody = document.getElementById('studentsTableBody');
    tbody.innerHTML = '';
    students.forEach(student => {
        const row = tbody.insertRow();
        row.onclick = () => { selectStudentRow(row); selectedStudentId = student.student_id; };
        row.ondblclick = () => viewStudentDetails();
        row.dataset.studentId = student.student_id;
        row.innerHTML = `
            <td>${student.student_id}</td>
            <td>${student.student_code || ''}</td>
            <td>${student.first_name || ''}</td>
            <td>${student.last_name || ''}</td>
            <td>${student.email || ''}</td>
            <td>${student.phone || ''}</td>
            <td>${student.status || 'ACTIVE'}</td>
        `;
    });
}

function selectStudentRow(row) {
    document.querySelectorAll('#studentsTableBody tr').forEach(r => r.classList.remove('selected'));
    row.classList.add('selected');
}

function searchStudents() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const students = storage.getStudents();
    const filtered = students.filter(s => 
        (s.first_name || '').toLowerCase().includes(searchTerm) ||
        (s.last_name || '').toLowerCase().includes(searchTerm)
    );
    const tbody = document.getElementById('studentsTableBody');
    tbody.innerHTML = '';
    filtered.forEach(student => {
        const row = tbody.insertRow();
        row.onclick = () => { selectStudentRow(row); selectedStudentId = student.student_id; };
        row.ondblclick = () => viewStudentDetails();
        row.dataset.studentId = student.student_id;
        row.innerHTML = `
            <td>${student.student_id}</td>
            <td>${student.student_code || ''}</td>
            <td>${student.first_name || ''}</td>
            <td>${student.last_name || ''}</td>
            <td>${student.email || ''}</td>
            <td>${student.phone || ''}</td>
            <td>${student.status || 'ACTIVE'}</td>
        `;
    });
}

function viewStudentDetails() {
    const selectedRow = document.querySelector('#studentsTableBody tr.selected');
    if (!selectedRow && !selectedStudentId) {
        alert('Please select a student first.');
        return;
    }
    const studentId = selectedStudentId || parseInt(selectedRow?.dataset.studentId);
    const students = storage.getStudents();
    const student = students.find(s => s.student_id === studentId);
    if (!student) {
        alert('Student not found.');
        return;
    }
    showModal('Student Details', `
        <div class="form-container">
            <div class="form-row">
                <div class="form-group"><label>Student ID:</label><input type="text" value="${student.student_id}" readonly></div>
                <div class="form-group"><label>Student Code:</label><input type="text" value="${student.student_code || ''}" readonly></div>
            </div>
            <div class="form-row">
                <div class="form-group"><label>First Name:</label><input type="text" value="${student.first_name || ''}" readonly></div>
                <div class="form-group"><label>Last Name:</label><input type="text" value="${student.last_name || ''}" readonly></div>
            </div>
            <div class="form-row">
                <div class="form-group"><label>Date of Birth:</label><input type="text" value="${student.date_of_birth || ''}" readonly></div>
                <div class="form-group"><label>Gender:</label><input type="text" value="${student.gender || ''}" readonly></div>
            </div>
            <div class="form-row">
                <div class="form-group"><label>Email:</label><input type="text" value="${student.email || ''}" readonly></div>
                <div class="form-group"><label>Phone:</label><input type="text" value="${student.phone || ''}" readonly></div>
            </div>
            <div class="form-row full">
                <div class="form-group"><label>Address:</label><textarea readonly>${student.address || ''}</textarea></div>
            </div>
            <div class="form-row">
                <div class="form-group"><label>Enrollment Date:</label><input type="text" value="${student.enrollment_date || ''}" readonly></div>
                <div class="form-group"><label>Status:</label><input type="text" value="${student.status || 'ACTIVE'}" readonly></div>
            </div>
            <div class="action-buttons">
                <button class="btn btn-secondary" onclick="closeModal()">Close</button>
            </div>
        </div>
    `);
}

function showAddStudentModal() {
    showModal('Add Student', `
        <form id="addStudentForm" onsubmit="saveStudent(event, null)">
            <div class="form-row">
                <div class="form-group"><label>Student Code *:</label><input type="text" name="student_code" required></div>
                <div class="form-group"><label>First Name *:</label><input type="text" name="first_name" required></div>
            </div>
            <div class="form-row">
                <div class="form-group"><label>Last Name *:</label><input type="text" name="last_name" required></div>
                <div class="form-group"><label>Date of Birth *:</label><input type="date" name="date_of_birth" required></div>
            </div>
            <div class="form-row">
                <div class="form-group"><label>Gender *:</label>
                    <select name="gender" required>
                        <option value="MALE">Male</option>
                        <option value="FEMALE">Female</option>
                        <option value="OTHER">Other</option>
                    </select>
                </div>
                <div class="form-group"><label>Email:</label><input type="email" name="email"></div>
            </div>
            <div class="form-row">
                <div class="form-group"><label>Phone:</label><input type="text" name="phone"></div>
                <div class="form-group"><label>Enrollment Date *:</label><input type="date" name="enrollment_date" required></div>
            </div>
            <div class="form-row full">
                <div class="form-group"><label>Address:</label><textarea name="address"></textarea></div>
            </div>
            <div class="form-row">
                <div class="form-group"><label>Status:</label>
                    <select name="status">
                        <option value="ACTIVE">Active</option>
                        <option value="INACTIVE">Inactive</option>
                        <option value="GRADUATED">Graduated</option>
                        <option value="SUSPENDED">Suspended</option>
                    </select>
                </div>
            </div>
            <div class="action-buttons">
                <button type="submit" class="btn btn-success">Save</button>
                <button type="button" class="btn btn-secondary" onclick="closeModal()">Cancel</button>
            </div>
        </form>
    `);
}

function editStudent() {
    if (!authService.isAdmin()) return;
    if (!selectedStudentId) {
        alert('Please select a student to edit.');
        return;
    }
    const students = storage.getStudents();
    const student = students.find(s => s.student_id === selectedStudentId);
    if (!student) {
        alert('Student not found.');
        return;
    }
    showModal('Edit Student', `
        <form id="editStudentForm" onsubmit="saveStudent(event, ${student.student_id})">
            <div class="form-row">
                <div class="form-group"><label>Student Code:</label><input type="text" name="student_code" value="${student.student_code || ''}" readonly></div>
                <div class="form-group"><label>First Name *:</label><input type="text" name="first_name" value="${student.first_name || ''}" required></div>
            </div>
            <div class="form-row">
                <div class="form-group"><label>Last Name *:</label><input type="text" name="last_name" value="${student.last_name || ''}" required></div>
                <div class="form-group"><label>Date of Birth *:</label><input type="date" name="date_of_birth" value="${student.date_of_birth || ''}" required></div>
            </div>
            <div class="form-row">
                <div class="form-group"><label>Gender *:</label>
                    <select name="gender" required>
                        <option value="MALE" ${student.gender === 'MALE' ? 'selected' : ''}>Male</option>
                        <option value="FEMALE" ${student.gender === 'FEMALE' ? 'selected' : ''}>Female</option>
                        <option value="OTHER" ${student.gender === 'OTHER' ? 'selected' : ''}>Other</option>
                    </select>
                </div>
                <div class="form-group"><label>Email:</label><input type="email" name="email" value="${student.email || ''}"></div>
            </div>
            <div class="form-row">
                <div class="form-group"><label>Phone:</label><input type="text" name="phone" value="${student.phone || ''}"></div>
                <div class="form-group"><label>Enrollment Date *:</label><input type="date" name="enrollment_date" value="${student.enrollment_date || ''}" required></div>
            </div>
            <div class="form-row full">
                <div class="form-group"><label>Address:</label><textarea name="address">${student.address || ''}</textarea></div>
            </div>
            <div class="form-row">
                <div class="form-group"><label>Status:</label>
                    <select name="status">
                        <option value="ACTIVE" ${student.status === 'ACTIVE' ? 'selected' : ''}>Active</option>
                        <option value="INACTIVE" ${student.status === 'INACTIVE' ? 'selected' : ''}>Inactive</option>
                        <option value="GRADUATED" ${student.status === 'GRADUATED' ? 'selected' : ''}>Graduated</option>
                        <option value="SUSPENDED" ${student.status === 'SUSPENDED' ? 'selected' : ''}>Suspended</option>
                    </select>
                </div>
            </div>
            <div class="action-buttons">
                <button type="submit" class="btn btn-success">Update</button>
                <button type="button" class="btn btn-secondary" onclick="closeModal()">Cancel</button>
            </div>
        </form>
    `);
}

function saveStudent(e, studentId) {
    e.preventDefault();
    const form = e.target;
    const formData = new FormData(form);
    const students = storage.getStudents();
    
    if (studentId) {
        // Update existing
        const index = students.findIndex(s => s.student_id === studentId);
        if (index !== -1) {
            students[index] = {
                ...students[index],
                first_name: formData.get('first_name'),
                last_name: formData.get('last_name'),
                date_of_birth: formData.get('date_of_birth'),
                gender: formData.get('gender'),
                email: formData.get('email'),
                phone: formData.get('phone'),
                address: formData.get('address'),
                enrollment_date: formData.get('enrollment_date'),
                status: formData.get('status')
            };
        }
    } else {
        // Add new
        students.push({
            student_id: storage.getNextId('student'),
            student_code: formData.get('student_code'),
            first_name: formData.get('first_name'),
            last_name: formData.get('last_name'),
            date_of_birth: formData.get('date_of_birth'),
            gender: formData.get('gender'),
            email: formData.get('email'),
            phone: formData.get('phone'),
            address: formData.get('address'),
            enrollment_date: formData.get('enrollment_date'),
            status: formData.get('status') || 'ACTIVE',
            user_id: null
        });
    }
    
    storage.setStudents(students);
    closeModal();
    loadStudents();
    alert(studentId ? 'Student updated successfully!' : 'Student added successfully!');
}

function deleteStudent() {
    if (!authService.isAdmin()) return;
    if (!selectedStudentId) {
        alert('Please select a student to delete.');
        return;
    }
    if (!confirm('Are you sure you want to delete this student?')) return;
    
    const students = storage.getStudents();
    const filtered = students.filter(s => s.student_id !== selectedStudentId);
    storage.setStudents(filtered);
    selectedStudentId = null;
    loadStudents();
    alert('Student deleted successfully!');
}

// Course Management
function showCourseManagement() {
    const isAdmin = authService.isAdmin();
    document.getElementById('contentArea').innerHTML = `
        <div class="search-bar">
            ${isAdmin ? '<button class="btn btn-success" onclick="showAddCourseModal()">Add Course</button>' : ''}
            <button class="btn btn-secondary" onclick="loadCourses()">Refresh</button>
        </div>
        <table id="coursesTable">
            <thead>
                <tr><th>ID</th><th>Course Code</th><th>Course Name</th><th>Credits</th><th>Semester</th><th>Academic Year</th><th>Status</th></tr>
            </thead>
            <tbody id="coursesTableBody"></tbody>
        </table>
        <div class="action-buttons">
            ${isAdmin ? '<button class="btn btn-primary" onclick="editCourse()">Edit</button>' : ''}
            ${isAdmin ? '<button class="btn btn-success" onclick="showEnrollStudentModal()">Enroll Student</button>' : ''}
            ${isAdmin ? '<button class="btn btn-danger" onclick="deleteCourse()">Delete</button>' : ''}
        </div>
    `;
    loadCourses();
}

function deleteCourse() {
    if (!authService.isAdmin()) return;
    if (!selectedCourseId) { alert('Please select a course to delete.'); return; }
    if (!confirm('Delete this course? Enrollments will remain but the course will be removed.')) return;
    const courses = storage.getCourses().filter(c => c.course_id !== selectedCourseId);
    storage.setCourses(courses);
    selectedCourseId = null;
    loadCourses();
    alert('Course deleted.');
}

function loadCourses() {
    const courses = storage.getCourses();
    const tbody = document.getElementById('coursesTableBody');
    tbody.innerHTML = '';
    courses.forEach(course => {
        const row = tbody.insertRow();
        row.onclick = () => { 
            document.querySelectorAll('#coursesTableBody tr').forEach(r => r.classList.remove('selected'));
            row.classList.add('selected');
            selectedCourseId = course.course_id;
        };
        row.dataset.courseId = course.course_id;
        row.innerHTML = `
            <td>${course.course_id}</td>
            <td>${course.course_code || ''}</td>
            <td>${course.course_name || ''}</td>
            <td>${course.credits || 0}</td>
            <td>${course.semester || ''}</td>
            <td>${course.academic_year || ''}</td>
            <td>${course.status || 'ACTIVE'}</td>
        `;
    });
}

function showAddCourseModal() {
    showModal('Add Course', `
        <form id="addCourseForm" onsubmit="saveCourse(event, null)">
            <div class="form-row">
                <div class="form-group"><label>Course Code *:</label><input type="text" name="course_code" required></div>
                <div class="form-group"><label>Course Name *:</label><input type="text" name="course_name" required></div>
            </div>
            <div class="form-row full">
                <div class="form-group"><label>Description:</label><textarea name="description"></textarea></div>
            </div>
            <div class="form-row">
                <div class="form-group"><label>Credits *:</label><input type="number" name="credits" min="1" required></div>
                <div class="form-group"><label>Semester:</label><input type="text" name="semester" placeholder="e.g., FALL"></div>
            </div>
            <div class="form-row">
                <div class="form-group"><label>Academic Year:</label><input type="text" name="academic_year" placeholder="e.g., 2023-2024"></div>
                <div class="form-group"><label>Status:</label>
                    <select name="status">
                        <option value="ACTIVE">Active</option>
                        <option value="INACTIVE">Inactive</option>
                    </select>
                </div>
            </div>
            <div class="action-buttons">
                <button type="submit" class="btn btn-success">Save</button>
                <button type="button" class="btn btn-secondary" onclick="closeModal()">Cancel</button>
            </div>
        </form>
    `);
}

function saveCourse(e, courseId) {
    e.preventDefault();
    const form = e.target;
    const formData = new FormData(form);
    const courses = storage.getCourses();
    
    if (courseId) {
        const index = courses.findIndex(c => c.course_id === courseId);
        if (index !== -1) {
            courses[index] = {
                ...courses[index],
                course_name: formData.get('course_name'),
                description: formData.get('description'),
                credits: parseInt(formData.get('credits')),
                semester: formData.get('semester'),
                academic_year: formData.get('academic_year'),
                status: formData.get('status')
            };
        }
    } else {
        courses.push({
            course_id: storage.getNextId('course'),
            course_code: formData.get('course_code'),
            course_name: formData.get('course_name'),
            description: formData.get('description'),
            credits: parseInt(formData.get('credits')),
            semester: formData.get('semester'),
            academic_year: formData.get('academic_year'),
            status: formData.get('status') || 'ACTIVE',
            instructor_id: null
        });
    }
    
    storage.setCourses(courses);
    closeModal();
    loadCourses();
    alert(courseId ? 'Course updated successfully!' : 'Course added successfully!');
}

function editCourse() {
    if (!authService.isAdmin()) return;
    if (!selectedCourseId) {
        alert('Please select a course to edit.');
        return;
    }
    const courses = storage.getCourses();
    const course = courses.find(c => c.course_id === selectedCourseId);
    if (!course) {
        alert('Course not found.');
        return;
    }
    showModal('Edit Course', `
        <form id="editCourseForm" onsubmit="saveCourse(event, ${course.course_id})">
            <div class="form-row">
                <div class="form-group"><label>Course Code:</label><input type="text" value="${course.course_code || ''}" readonly></div>
                <div class="form-group"><label>Course Name *:</label><input type="text" name="course_name" value="${course.course_name || ''}" required></div>
            </div>
            <div class="form-row full">
                <div class="form-group"><label>Description:</label><textarea name="description">${course.description || ''}</textarea></div>
            </div>
            <div class="form-row">
                <div class="form-group"><label>Credits *:</label><input type="number" name="credits" value="${course.credits || 0}" min="1" required></div>
                <div class="form-group"><label>Semester:</label><input type="text" name="semester" value="${course.semester || ''}"></div>
            </div>
            <div class="form-row">
                <div class="form-group"><label>Academic Year:</label><input type="text" name="academic_year" value="${course.academic_year || ''}"></div>
                <div class="form-group"><label>Status:</label>
                    <select name="status">
                        <option value="ACTIVE" ${course.status === 'ACTIVE' ? 'selected' : ''}>Active</option>
                        <option value="INACTIVE" ${course.status === 'INACTIVE' ? 'selected' : ''}>Inactive</option>
                    </select>
                </div>
            </div>
            <div class="action-buttons">
                <button type="submit" class="btn btn-success">Update</button>
                <button type="button" class="btn btn-secondary" onclick="closeModal()">Cancel</button>
            </div>
        </form>
    `);
}

function showEnrollStudentModal() {
    if (!selectedCourseId) {
        alert('Please select a course first.');
        return;
    }
    const students = storage.getStudents();
    const enrollments = storage.getEnrollments();
    const enrolledIds = enrollments.filter(e => e.course_id === selectedCourseId && e.status === 'ENROLLED').map(e => e.student_id);
    const availableStudents = students.filter(s => !enrolledIds.includes(s.student_id));
    
    if (availableStudents.length === 0) {
        alert('All students are already enrolled in this course.');
        return;
    }
    
    let options = availableStudents.map(s => 
        `<option value="${s.student_id}">${s.student_code} - ${s.first_name} ${s.last_name}</option>`
    ).join('');
    
    showModal('Enroll Student', `
        <form id="enrollForm" onsubmit="enrollStudent(event)">
            <div class="form-row full">
                <div class="form-group">
                    <label>Select Student:</label>
                    <select name="student_id" required>
                        <option value="">-- Select Student --</option>
                        ${options}
                    </select>
                </div>
            </div>
            <div class="action-buttons">
                <button type="submit" class="btn btn-success">Enroll</button>
                <button type="button" class="btn btn-secondary" onclick="closeModal()">Cancel</button>
            </div>
        </form>
    `);
}

function enrollStudent(e) {
    e.preventDefault();
    const form = e.target;
    const studentId = parseInt(form.student_id.value);
    const enrollments = storage.getEnrollments();
    
    // Check if already enrolled
    const exists = enrollments.find(e => e.student_id === studentId && e.course_id === selectedCourseId);
    if (exists) {
        exists.status = 'ENROLLED';
    } else {
        enrollments.push({
            enrollment_id: storage.getNextId('enrollment'),
            student_id: studentId,
            course_id: selectedCourseId,
            enrollment_date: new Date().toISOString().split('T')[0],
            status: 'ENROLLED'
        });
    }
    
    storage.setEnrollments(enrollments);
    closeModal();
    alert('Student enrolled successfully!');
}

// Attendance Management
function showAttendanceManagement() {
    document.getElementById('contentArea').innerHTML = `
        <div class="search-bar">
            <label>Course:</label>
            <select id="attendanceCourseSelect" onchange="loadStudentsForAttendance()">
                <option value="">-- Select Course --</option>
            </select>
            <label>Date:</label>
            <input type="date" id="attendanceDate" value="${new Date().toISOString().split('T')[0]}" onchange="loadStudentsForAttendance()">
            <button class="btn btn-primary" onclick="loadStudentsForAttendance()">Load Students</button>
            <button class="btn btn-success" onclick="saveAttendance()">Save Attendance</button>
        </div>
        <table id="attendanceTable">
            <thead>
                <tr><th>Student Code</th><th>Student Name</th><th>Status</th></tr>
            </thead>
            <tbody id="attendanceTableBody"></tbody>
        </table>
    `;
    loadCoursesForAttendance();
}

function loadCoursesForAttendance() {
    const courses = storage.getCourses();
    const select = document.getElementById('attendanceCourseSelect');
    courses.forEach(course => {
        const option = document.createElement('option');
        option.value = course.course_id;
        option.textContent = `${course.course_code} - ${course.course_name}`;
        select.appendChild(option);
    });
}

function loadStudentsForAttendance() {
    const courseId = parseInt(document.getElementById('attendanceCourseSelect').value);
    const date = document.getElementById('attendanceDate').value;
    
    if (!courseId) {
        alert('Please select a course.');
        return;
    }
    
    const enrollments = storage.getEnrollments();
    const enrolledStudentIds = enrollments.filter(e => e.course_id === courseId && e.status === 'ENROLLED').map(e => e.student_id);
    const students = storage.getStudents().filter(s => enrolledStudentIds.includes(s.student_id));
    const attendance = storage.getAttendance();
    
    const tbody = document.getElementById('attendanceTableBody');
    tbody.innerHTML = '';
    
    students.forEach(student => {
        const existing = attendance.find(a => a.student_id === student.student_id && a.course_id === courseId && a.attendance_date === date);
        const row = tbody.insertRow();
        row.dataset.studentId = student.student_id;
        row.innerHTML = `
            <td>${student.student_code || ''}</td>
            <td>${student.first_name || ''} ${student.last_name || ''}</td>
            <td>
                <select class="attendance-status" data-student-id="${student.student_id}">
                    <option value="PRESENT" ${existing?.status === 'PRESENT' ? 'selected' : ''}>Present</option>
                    <option value="ABSENT" ${existing?.status === 'ABSENT' ? 'selected' : ''}>Absent</option>
                    <option value="LATE" ${existing?.status === 'LATE' ? 'selected' : ''}>Late</option>
                    <option value="EXCUSED" ${existing?.status === 'EXCUSED' ? 'selected' : ''}>Excused</option>
                </select>
            </td>
        `;
    });
}

function saveAttendance() {
    const courseId = parseInt(document.getElementById('attendanceCourseSelect').value);
    const date = document.getElementById('attendanceDate').value;
    const user = authService.getCurrentUser();
    
    if (!courseId || !date) {
        alert('Please select course and date.');
        return;
    }
    
    const attendance = storage.getAttendance();
    const statusSelects = document.querySelectorAll('.attendance-status');
    
    statusSelects.forEach(select => {
        const studentId = parseInt(select.dataset.studentId);
        const status = select.value;
        const existing = attendance.findIndex(a => 
            a.student_id === studentId && a.course_id === courseId && a.attendance_date === date
        );
        
        if (existing !== -1) {
            attendance[existing].status = status;
            attendance[existing].recorded_by = user.user_id;
        } else {
            attendance.push({
                attendance_id: storage.getNextId('attendance'),
                student_id: studentId,
                course_id: courseId,
                attendance_date: date,
                status: status,
                remarks: '',
                recorded_by: user.user_id
            });
        }
    });
    
    storage.setAttendance(attendance);
    alert('Attendance recorded successfully!');
}

// Grade Management
function showGradeManagement() {
    document.getElementById('contentArea').innerHTML = `
        <div class="search-bar">
            <label>Student:</label>
            <select id="gradeStudentSelect">
                <option value="">-- Select Student --</option>
            </select>
            <label>Course:</label>
            <select id="gradeCourseSelect">
                <option value="">-- Select Course --</option>
            </select>
            <button class="btn btn-primary" onclick="loadGrades()">Load Grades</button>
            <button class="btn btn-success" onclick="showAddGradeModal()">Add Grade</button>
        </div>
        <table id="gradesTable">
            <thead>
                <tr><th>ID</th><th>Assessment Type</th><th>Assessment Name</th><th>Marks Obtained</th><th>Total Marks</th><th>Percentage</th><th>Grade</th></tr>
            </thead>
            <tbody id="gradesTableBody"></tbody>
        </table>
    `;
    loadStudentsForGrades();
    loadCoursesForGrades();
}

function loadStudentsForGrades() {
    const students = storage.getStudents();
    const select = document.getElementById('gradeStudentSelect');
    students.forEach(student => {
        const option = document.createElement('option');
        option.value = student.student_id;
        option.textContent = `${student.student_code} - ${student.first_name} ${student.last_name}`;
        select.appendChild(option);
    });
}

function loadCoursesForGrades() {
    const courses = storage.getCourses();
    const select = document.getElementById('gradeCourseSelect');
    courses.forEach(course => {
        const option = document.createElement('option');
        option.value = course.course_id;
        option.textContent = `${course.course_code} - ${course.course_name}`;
        select.appendChild(option);
    });
}

function loadGrades() {
    const studentId = parseInt(document.getElementById('gradeStudentSelect').value);
    const courseId = parseInt(document.getElementById('gradeCourseSelect').value);
    
    if (!studentId || !courseId) {
        alert('Please select both student and course.');
        return;
    }
    
    const grades = storage.getGrades().filter(g => g.student_id === studentId && g.course_id === courseId);
    const tbody = document.getElementById('gradesTableBody');
    tbody.innerHTML = '';
    
    grades.forEach(grade => {
        const row = tbody.insertRow();
        row.innerHTML = `
            <td>${grade.grade_id}</td>
            <td>${grade.assessment_type || ''}</td>
            <td>${grade.assessment_name || ''}</td>
            <td>${grade.marks_obtained || 0}</td>
            <td>${grade.total_marks || 0}</td>
            <td>${grade.percentage ? grade.percentage.toFixed(2) + '%' : '0%'}</td>
            <td>${grade.grade_letter || ''}</td>
        `;
    });
}

function showAddGradeModal() {
    const studentId = document.getElementById('gradeStudentSelect').value;
    const courseId = document.getElementById('gradeCourseSelect').value;
    
    if (!studentId || !courseId) {
        alert('Please select both student and course first.');
        return;
    }
    
    showModal('Add Grade', `
        <form id="addGradeForm" onsubmit="saveGrade(event)">
            <div class="form-row">
                <div class="form-group"><label>Assessment Type *:</label><input type="text" name="assessment_type" placeholder="e.g., Quiz, Exam, Assignment" required></div>
                <div class="form-group"><label>Assessment Name:</label><input type="text" name="assessment_name"></div>
            </div>
            <div class="form-row">
                <div class="form-group"><label>Marks Obtained *:</label><input type="number" name="marks_obtained" step="0.01" required></div>
                <div class="form-group"><label>Total Marks *:</label><input type="number" name="total_marks" step="0.01" required></div>
            </div>
            <div class="form-row">
                <div class="form-group"><label>Semester:</label><input type="text" name="semester" placeholder="e.g., FALL"></div>
                <div class="form-group"><label>Academic Year:</label><input type="text" name="academic_year" placeholder="e.g., 2023-2024"></div>
            </div>
            <div class="action-buttons">
                <button type="submit" class="btn btn-success">Save</button>
                <button type="button" class="btn btn-secondary" onclick="closeModal()">Cancel</button>
            </div>
        </form>
    `);
}

function saveGrade(e) {
    e.preventDefault();
    const form = e.target;
    const formData = new FormData(form);
    const user = authService.getCurrentUser();
    const studentId = parseInt(document.getElementById('gradeStudentSelect').value);
    const courseId = parseInt(document.getElementById('gradeCourseSelect').value);
    
    const marksObtained = parseFloat(formData.get('marks_obtained'));
    const totalMarks = parseFloat(formData.get('total_marks'));
    const percentage = (marksObtained / totalMarks) * 100;
    const gradeLetter = calculateGradeLetter(percentage);
    
    const grades = storage.getGrades();
    grades.push({
        grade_id: storage.getNextId('grade'),
        student_id: studentId,
        course_id: courseId,
        assessment_type: formData.get('assessment_type'),
        assessment_name: formData.get('assessment_name'),
        marks_obtained: marksObtained,
        total_marks: totalMarks,
        percentage: percentage,
        grade_letter: gradeLetter,
        semester: formData.get('semester'),
        academic_year: formData.get('academic_year'),
        recorded_by: user.user_id,
        created_at: new Date().toISOString().split('T')[0]
    });
    
    storage.setGrades(grades);
    closeModal();
    loadGrades();
    alert('Grade recorded successfully!');
}

function calculateGradeLetter(percentage) {
    if (percentage >= 90) return 'A+';
    if (percentage >= 80) return 'A';
    if (percentage >= 70) return 'B+';
    if (percentage >= 60) return 'B';
    if (percentage >= 50) return 'C+';
    if (percentage >= 40) return 'C';
    return 'F';
}

// Financial Management
function showFinancialManagement() {
    if (!authService.isAdmin()) return;
    document.getElementById('contentArea').innerHTML = `
        <div class="search-bar">
            <label>Student:</label>
            <select id="financialStudentSelect" onchange="loadFinancialRecords()">
                <option value="">-- Select Student --</option>
            </select>
            <button class="btn btn-success" onclick="showAddTransactionModal()">Add Transaction</button>
            <div id="balanceDisplay" style="margin-left: 20px; font-weight: bold; font-size: 16px;">Balance: $0.00</div>
        </div>
        <table id="financialTable">
            <thead>
                <tr><th>ID</th><th>Type</th><th>Amount</th><th>Description</th><th>Date</th><th>Due Date</th><th>Status</th><th>Payment Date</th></tr>
            </thead>
            <tbody id="financialTableBody"></tbody>
        </table>
        <div class="action-buttons">
            <button class="btn btn-primary" onclick="recordPayment()">Record Payment</button>
        </div>
    `;
    loadStudentsForFinancial();
}

function loadStudentsForFinancial() {
    const students = storage.getStudents();
    const select = document.getElementById('financialStudentSelect');
    students.forEach(student => {
        const option = document.createElement('option');
        option.value = student.student_id;
        option.textContent = `${student.student_code} - ${student.first_name} ${student.last_name}`;
        select.appendChild(option);
    });
}

function loadFinancialRecords() {
    const studentId = parseInt(document.getElementById('financialStudentSelect').value);
    if (!studentId) {
        document.getElementById('financialTableBody').innerHTML = '';
        document.getElementById('balanceDisplay').textContent = 'Balance: $0.00';
        return;
    }
    
    const records = storage.getFinancialRecords().filter(r => r.student_id === studentId);
    const tbody = document.getElementById('financialTableBody');
    tbody.innerHTML = '';
    
    let balance = 0;
    records.forEach(record => {
        if (record.status !== 'CANCELLED') {
            if (record.transaction_type === 'FEE' || record.transaction_type === 'PENALTY') {
                balance += parseFloat(record.amount || 0);
            } else {
                balance -= parseFloat(record.amount || 0);
            }
        }
        
        const row = tbody.insertRow();
        row.onclick = () => {
            document.querySelectorAll('#financialTableBody tr').forEach(r => r.classList.remove('selected'));
            row.classList.add('selected');
            selectedFinancialId = record.financial_id;
        };
        row.dataset.financialId = record.financial_id;
        row.innerHTML = `
            <td>${record.financial_id}</td>
            <td>${record.transaction_type || ''}</td>
            <td>$${parseFloat(record.amount || 0).toFixed(2)}</td>
            <td>${record.description || ''}</td>
            <td>${record.transaction_date || ''}</td>
            <td>${record.due_date || 'N/A'}</td>
            <td>${record.status || 'PENDING'}</td>
            <td>${record.payment_date || 'N/A'}</td>
        `;
    });
    
    document.getElementById('balanceDisplay').textContent = `Balance: $${balance.toFixed(2)}`;
}

let selectedFinancialId = null;

function showAddTransactionModal() {
    const studentId = document.getElementById('financialStudentSelect').value;
    if (!studentId) {
        alert('Please select a student first.');
        return;
    }
    
    showModal('Add Transaction', `
        <form id="addTransactionForm" onsubmit="saveTransaction(event)">
            <div class="form-row">
                <div class="form-group"><label>Transaction Type *:</label>
                    <select name="transaction_type" required>
                        <option value="FEE">Fee</option>
                        <option value="PAYMENT">Payment</option>
                        <option value="REFUND">Refund</option>
                        <option value="SCHOLARSHIP">Scholarship</option>
                        <option value="PENALTY">Penalty</option>
                    </select>
                </div>
                <div class="form-group"><label>Amount *:</label><input type="number" name="amount" step="0.01" required></div>
            </div>
            <div class="form-row full">
                <div class="form-group"><label>Description:</label><textarea name="description"></textarea></div>
            </div>
            <div class="form-row">
                <div class="form-group"><label>Transaction Date *:</label><input type="date" name="transaction_date" value="${new Date().toISOString().split('T')[0]}" required></div>
                <div class="form-group"><label>Due Date:</label><input type="date" name="due_date"></div>
            </div>
            <div class="action-buttons">
                <button type="submit" class="btn btn-success">Save</button>
                <button type="button" class="btn btn-secondary" onclick="closeModal()">Cancel</button>
            </div>
        </form>
    `);
}

function saveTransaction(e) {
    e.preventDefault();
    const form = e.target;
    const formData = new FormData(form);
    const user = authService.getCurrentUser();
    const studentId = parseInt(document.getElementById('financialStudentSelect').value);
    
    const records = storage.getFinancialRecords();
    records.push({
        financial_id: storage.getNextId('financial'),
        student_id: studentId,
        transaction_type: formData.get('transaction_type'),
        amount: parseFloat(formData.get('amount')),
        description: formData.get('description'),
        transaction_date: formData.get('transaction_date'),
        due_date: formData.get('due_date') || null,
        status: 'PENDING',
        payment_method: null,
        payment_date: null,
        receipt_number: null,
        recorded_by: user.user_id
    });
    
    storage.setFinancialRecords(records);
    closeModal();
    loadFinancialRecords();
    alert('Transaction recorded successfully!');
}

function recordPayment() {
    const selectedRow = document.querySelector('#financialTableBody tr.selected');
    if (!selectedRow) {
        alert('Please select a transaction to record payment.');
        return;
    }
    
    const financialId = parseInt(selectedRow.dataset.financialId);
    const paymentMethod = prompt('Enter payment method:');
    if (!paymentMethod) return;
    
    const records = storage.getFinancialRecords();
    const record = records.find(r => r.financial_id === financialId);
    if (record) {
        record.status = 'PAID';
        record.payment_method = paymentMethod;
        record.payment_date = new Date().toISOString().split('T')[0];
        record.receipt_number = 'REC' + financialId;
        storage.setFinancialRecords(records);
        loadFinancialRecords();
        alert('Payment recorded successfully!');
    }
}

// Modal helper functions
function showModal(title, content) {
    const modal = document.createElement('div');
    modal.className = 'modal';
    modal.style.display = 'block';
    modal.innerHTML = `
        <div class="modal-content">
            <div class="modal-header">
                <h2>${title}</h2>
                <span class="close" onclick="closeModal()">&times;</span>
            </div>
            ${content}
        </div>
    `;
    document.body.appendChild(modal);
    modal.onclick = (e) => { if (e.target === modal) closeModal(); };
}

function closeModal() {
    document.querySelectorAll('.modal').forEach(m => m.remove());
}
