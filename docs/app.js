// Simple Data Storage
var simsData = {
    users: [
        { user_id: 1, username: "admin", password: "admin123", role: "ADMIN", email: "admin@school.edu" },
        { user_id: 2, username: "teacher", password: "teacher123", role: "TEACHER", email: "teacher@school.edu" },
        { user_id: 3, username: "student", password: "student123", role: "STUDENT", email: "student@school.edu" }
    ],
    students: [],
    courses: [],
    enrollments: [],
    attendance: [],
    grades: [],
    financialRecords: []
};

// Load from localStorage or initialize
function loadData() {
    var saved = localStorage.getItem('sims_data');
    if (saved) {
        try {
            var parsed = JSON.parse(saved);
            if (parsed.users) simsData.users = parsed.users;
            if (parsed.students) simsData.students = parsed.students;
            if (parsed.courses) simsData.courses = parsed.courses;
            if (parsed.enrollments) simsData.enrollments = parsed.enrollments;
            if (parsed.attendance) simsData.attendance = parsed.attendance;
            if (parsed.grades) simsData.grades = parsed.grades;
            if (parsed.financialRecords) simsData.financialRecords = parsed.financialRecords;
        } catch (e) {
            console.log('Error loading data:', e);
        }
    }
    // Try to load from data.json
    fetch('data.json').then(function(r) {
        if (r.ok) return r.json();
    }).then(function(fileData) {
        if (fileData) {
            if (fileData.students && (!simsData.students || simsData.students.length === 0)) {
                simsData.students = fileData.students;
            }
            if (fileData.courses && (!simsData.courses || simsData.courses.length === 0)) {
                simsData.courses = fileData.courses;
            }
            saveData();
        }
    }).catch(function() {});
}

function saveData() {
    localStorage.setItem('sims_data', JSON.stringify(simsData));
}

// Current user
var currentUser = null;

function getCurrentUser() {
    if (!currentUser) {
        var stored = sessionStorage.getItem('currentUser');
        if (stored) {
            try {
                currentUser = JSON.parse(stored);
            } catch (e) {}
        }
    }
    return currentUser;
}

function setCurrentUser(user) {
    currentUser = user;
    if (user) {
        sessionStorage.setItem('currentUser', JSON.stringify(user));
    } else {
        sessionStorage.removeItem('currentUser');
    }
}

// Login
function login(username, password) {
    var user = simsData.users.find(function(u) {
        return u.username === username && u.password === password;
    });
    if (user) {
        setCurrentUser(user);
        return true;
    }
    return false;
}

function isAdmin() {
    var u = getCurrentUser();
    return u && u.role === 'ADMIN';
}

function isTeacher() {
    var u = getCurrentUser();
    return u && u.role === 'TEACHER';
}

// Initialize
loadData();

// Wait for DOM
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
} else {
    init();
}

function init() {
    var loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }
    
    var user = getCurrentUser();
    if (user) {
        showDashboard();
    } else {
        showLogin();
    }
}

function handleLogin(e) {
    e.preventDefault();
    var username = document.getElementById('username').value;
    var password = document.getElementById('password').value;
    var errorMsg = document.getElementById('errorMessage');
    
    if (login(username, password)) {
        if (errorMsg) errorMsg.textContent = '';
        showDashboard();
    } else {
        if (errorMsg) errorMsg.textContent = 'Invalid username or password.';
    }
}

function showLogin() {
    var lc = document.getElementById('loginContainer');
    var dc = document.getElementById('dashboardContainer');
    if (lc) {
        lc.style.display = 'flex';
        lc.style.visibility = 'visible';
        lc.style.opacity = '1';
    }
    if (dc) {
        dc.style.display = 'none';
        dc.style.visibility = 'hidden';
    }
    document.body.classList.remove('dashboard-view');
}

function showDashboard() {
    var user = getCurrentUser();
    if (!user) {
        showLogin();
        return;
    }
    
    // Wait a tiny bit to ensure DOM is ready
    setTimeout(function() {
        var lc = document.getElementById('loginContainer');
        var dc = document.getElementById('dashboardContainer');
        var welcome = document.getElementById('dashboardWelcome');
        var menu = document.getElementById('menuBar');
        var content = document.getElementById('contentArea');
        
        if (!dc) {
            console.error('Dashboard container not found!');
            // Try to create it if missing
            var body = document.body;
            if (body) {
                var newDc = document.createElement('div');
                newDc.className = 'dashboard-container';
                newDc.id = 'dashboardContainer';
                newDc.innerHTML = 
                    '<div class="dashboard-header">' +
                    '<h2 id="dashboardWelcome">Welcome</h2>' +
                    '<button class="btn-logout" type="button" onclick="handleLogout()">Logout</button>' +
                    '</div>' +
                    '<div class="menu-bar" id="menuBar"></div>' +
                    '<div class="content-area" id="contentArea"></div>';
                body.appendChild(newDc);
                dc = newDc;
                welcome = document.getElementById('dashboardWelcome');
                menu = document.getElementById('menuBar');
                content = document.getElementById('contentArea');
            } else {
                return;
            }
        }
        
        if (lc) {
            lc.style.display = 'none';
            lc.style.visibility = 'hidden';
            lc.style.position = 'absolute';
            lc.style.left = '-9999px';
            lc.style.width = '0';
            lc.style.height = '0';
            lc.style.overflow = 'hidden';
        }
        if (dc) {
            dc.style.display = 'block';
            dc.style.visibility = 'visible';
            dc.style.opacity = '1';
            dc.style.width = '100%';
        }
        document.body.classList.add('dashboard-view');
        
        if (welcome) {
            welcome.textContent = 'Welcome, ' + user.username + ' (' + user.role + ')';
        }
        
        if (menu) {
            var menuHtml = '<button type="button" onclick="showHome()">Home</button>';
            if (isAdmin() || isTeacher()) {
                menuHtml += '<button type="button" onclick="showStudentManagement()">Student Management</button>';
                menuHtml += '<button type="button" onclick="showCourseManagement()">Course Management</button>';
                menuHtml += '<button type="button" onclick="showAttendanceManagement()">Attendance</button>';
                menuHtml += '<button type="button" onclick="showGradeManagement()">Grades</button>';
            }
            if (isAdmin()) {
                menuHtml += '<button type="button" onclick="showFinancialManagement()">Financial</button>';
            }
            menu.innerHTML = menuHtml;
        }
        
        if (content) {
            showHome();
        }
    }, 10);
}

function handleLogout() {
    setCurrentUser(null);
    location.reload();
}

function showHome() {
    var content = document.getElementById('contentArea');
    if (!content) return;
    
    var user = getCurrentUser();
    var roleText = '';
    if (user.role === 'ADMIN') {
        roleText = '<p>You have full access to students, courses, attendance, grades, and financial records.</p>';
    } else if (user.role === 'TEACHER') {
        roleText = '<p>You can view and manage students, courses, attendance, and grades.</p>';
    } else if (user.role === 'STUDENT') {
        roleText = '<p>You have view-only access to your records.</p>';
    }
    
    content.innerHTML = 
        '<div class="home-welcome">' +
        '<h3>Welcome to Student Information Management System</h3>' +
        '<p>Use the menu above to navigate to different modules.</p>' +
        '<p><strong>Your role:</strong> ' + user.role + '</p>' +
        roleText +
        '</div>';
}

// Student Management
var selectedStudentId = null;

function showStudentManagement() {
    var content = document.getElementById('contentArea');
    if (!content) return;
    
    var admin = isAdmin();
    content.innerHTML = 
        '<div class="search-bar">' +
        '<input type="text" id="searchInput" placeholder="Search by name..." onkeyup="searchStudents()">' +
        '<button class="btn btn-primary" onclick="searchStudents()">Search</button>' +
        (admin ? '<button class="btn btn-success" onclick="showAddStudentModal()">Add Student</button>' : '') +
        '<button class="btn btn-secondary" onclick="loadStudents()">Refresh</button>' +
        '</div>' +
        '<table id="studentsTable">' +
        '<thead><tr><th>ID</th><th>Student Code</th><th>First Name</th><th>Last Name</th><th>Email</th><th>Phone</th><th>Status</th></tr></thead>' +
        '<tbody id="studentsTableBody"></tbody>' +
        '</table>' +
        '<div class="action-buttons">' +
        '<button class="btn btn-primary" onclick="viewStudentDetails()">View Details</button>' +
        (admin ? '<button class="btn btn-primary" onclick="editStudent()">Edit</button>' : '') +
        (admin ? '<button class="btn btn-danger" onclick="deleteStudent()">Delete</button>' : '') +
        '</div>';
    loadStudents();
}

function loadStudents() {
    var tbody = document.getElementById('studentsTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    simsData.students.forEach(function(student) {
        var row = tbody.insertRow();
        row.onclick = function() {
            document.querySelectorAll('#studentsTableBody tr').forEach(function(r) {
                r.classList.remove('selected');
            });
            row.classList.add('selected');
            selectedStudentId = student.student_id;
        };
        row.ondblclick = function() {
            viewStudentDetails();
        };
        row.innerHTML = 
            '<td>' + student.student_id + '</td>' +
            '<td>' + (student.student_code || '') + '</td>' +
            '<td>' + (student.first_name || '') + '</td>' +
            '<td>' + (student.last_name || '') + '</td>' +
            '<td>' + (student.email || '') + '</td>' +
            '<td>' + (student.phone || '') + '</td>' +
            '<td>' + (student.status || 'ACTIVE') + '</td>';
    });
}

function searchStudents() {
    var term = (document.getElementById('searchInput') || {}).value.toLowerCase();
    var tbody = document.getElementById('studentsTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    simsData.students.filter(function(s) {
        var fn = (s.first_name || '').toLowerCase();
        var ln = (s.last_name || '').toLowerCase();
        return fn.includes(term) || ln.includes(term);
    }).forEach(function(student) {
        var row = tbody.insertRow();
        row.onclick = function() {
            document.querySelectorAll('#studentsTableBody tr').forEach(function(r) {
                r.classList.remove('selected');
            });
            row.classList.add('selected');
            selectedStudentId = student.student_id;
        };
        row.ondblclick = function() {
            viewStudentDetails();
        };
        row.innerHTML = 
            '<td>' + student.student_id + '</td>' +
            '<td>' + (student.student_code || '') + '</td>' +
            '<td>' + (student.first_name || '') + '</td>' +
            '<td>' + (student.last_name || '') + '</td>' +
            '<td>' + (student.email || '') + '</td>' +
            '<td>' + (student.phone || '') + '</td>' +
            '<td>' + (student.status || 'ACTIVE') + '</td>';
    });
}

function viewStudentDetails() {
    var selected = document.querySelector('#studentsTableBody tr.selected');
    if (!selected && !selectedStudentId) {
        alert('Please select a student first.');
        return;
    }
    var studentId = selectedStudentId || parseInt(selected.dataset.studentId);
    var student = simsData.students.find(function(s) {
        return s.student_id === studentId;
    });
    if (!student) {
        alert('Student not found.');
        return;
    }
    showModal('Student Details', 
        '<div class="form-container">' +
        '<div class="form-row">' +
        '<div class="form-group"><label>Student ID:</label><input type="text" value="' + student.student_id + '" readonly></div>' +
        '<div class="form-group"><label>Student Code:</label><input type="text" value="' + (student.student_code || '') + '" readonly></div>' +
        '</div>' +
        '<div class="form-row">' +
        '<div class="form-group"><label>First Name:</label><input type="text" value="' + (student.first_name || '') + '" readonly></div>' +
        '<div class="form-group"><label>Last Name:</label><input type="text" value="' + (student.last_name || '') + '" readonly></div>' +
        '</div>' +
        '<div class="form-row">' +
        '<div class="form-group"><label>Date of Birth:</label><input type="text" value="' + (student.date_of_birth || '') + '" readonly></div>' +
        '<div class="form-group"><label>Gender:</label><input type="text" value="' + (student.gender || '') + '" readonly></div>' +
        '</div>' +
        '<div class="form-row">' +
        '<div class="form-group"><label>Email:</label><input type="text" value="' + (student.email || '') + '" readonly></div>' +
        '<div class="form-group"><label>Phone:</label><input type="text" value="' + (student.phone || '') + '" readonly></div>' +
        '</div>' +
        '<div class="form-row full">' +
        '<div class="form-group"><label>Address:</label><textarea readonly>' + (student.address || '') + '</textarea></div>' +
        '</div>' +
        '<div class="form-row">' +
        '<div class="form-group"><label>Enrollment Date:</label><input type="text" value="' + (student.enrollment_date || '') + '" readonly></div>' +
        '<div class="form-group"><label>Status:</label><input type="text" value="' + (student.status || 'ACTIVE') + '" readonly></div>' +
        '</div>' +
        '<div class="action-buttons">' +
        '<button class="btn btn-secondary" onclick="closeModal()">Close</button>' +
        '</div>' +
        '</div>'
    );
}

function showAddStudentModal() {
    showModal('Add Student',
        '<form id="addStudentForm" onsubmit="saveStudent(event, null); return false;">' +
        '<div class="form-row">' +
        '<div class="form-group"><label>Student Code *:</label><input type="text" name="student_code" required></div>' +
        '<div class="form-group"><label>First Name *:</label><input type="text" name="first_name" required></div>' +
        '</div>' +
        '<div class="form-row">' +
        '<div class="form-group"><label>Last Name *:</label><input type="text" name="last_name" required></div>' +
        '<div class="form-group"><label>Date of Birth *:</label><input type="date" name="date_of_birth" required></div>' +
        '</div>' +
        '<div class="form-row">' +
        '<div class="form-group"><label>Gender *:</label>' +
        '<select name="gender" required><option value="MALE">Male</option><option value="FEMALE">Female</option><option value="OTHER">Other</option></select>' +
        '</div>' +
        '<div class="form-group"><label>Email:</label><input type="email" name="email"></div>' +
        '</div>' +
        '<div class="form-row">' +
        '<div class="form-group"><label>Phone:</label><input type="text" name="phone"></div>' +
        '<div class="form-group"><label>Enrollment Date *:</label><input type="date" name="enrollment_date" required></div>' +
        '</div>' +
        '<div class="form-row full">' +
        '<div class="form-group"><label>Address:</label><textarea name="address"></textarea></div>' +
        '</div>' +
        '<div class="form-row">' +
        '<div class="form-group"><label>Status:</label>' +
        '<select name="status"><option value="ACTIVE">Active</option><option value="INACTIVE">Inactive</option><option value="GRADUATED">Graduated</option><option value="SUSPENDED">Suspended</option></select>' +
        '</div>' +
        '</div>' +
        '<div class="action-buttons">' +
        '<button type="submit" class="btn btn-success">Save</button>' +
        '<button type="button" class="btn btn-secondary" onclick="closeModal()">Cancel</button>' +
        '</div>' +
        '</form>'
    );
}

function saveStudent(e, studentId) {
    e.preventDefault();
    var form = e.target;
    var formData = new FormData(form);
    
    if (studentId) {
        var idx = simsData.students.findIndex(function(s) {
            return s.student_id === studentId;
        });
        if (idx !== -1) {
            simsData.students[idx].first_name = formData.get('first_name');
            simsData.students[idx].last_name = formData.get('last_name');
            simsData.students[idx].date_of_birth = formData.get('date_of_birth');
            simsData.students[idx].gender = formData.get('gender');
            simsData.students[idx].email = formData.get('email');
            simsData.students[idx].phone = formData.get('phone');
            simsData.students[idx].address = formData.get('address');
            simsData.students[idx].enrollment_date = formData.get('enrollment_date');
            simsData.students[idx].status = formData.get('status');
        }
    } else {
        var maxId = 0;
        simsData.students.forEach(function(s) {
            if (s.student_id > maxId) maxId = s.student_id;
        });
        simsData.students.push({
            student_id: maxId + 1,
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
    
    saveData();
    closeModal();
    loadStudents();
    alert(studentId ? 'Student updated successfully!' : 'Student added successfully!');
}

function editStudent() {
    if (!isAdmin()) return;
    if (!selectedStudentId) {
        alert('Please select a student to edit.');
        return;
    }
    var student = simsData.students.find(function(s) {
        return s.student_id === selectedStudentId;
    });
    if (!student) {
        alert('Student not found.');
        return;
    }
    showModal('Edit Student',
        '<form id="editStudentForm" onsubmit="saveStudent(event, ' + student.student_id + '); return false;">' +
        '<div class="form-row">' +
        '<div class="form-group"><label>Student Code:</label><input type="text" value="' + (student.student_code || '') + '" readonly></div>' +
        '<div class="form-group"><label>First Name *:</label><input type="text" name="first_name" value="' + (student.first_name || '') + '" required></div>' +
        '</div>' +
        '<div class="form-row">' +
        '<div class="form-group"><label>Last Name *:</label><input type="text" name="last_name" value="' + (student.last_name || '') + '" required></div>' +
        '<div class="form-group"><label>Date of Birth *:</label><input type="date" name="date_of_birth" value="' + (student.date_of_birth || '') + '" required></div>' +
        '</div>' +
        '<div class="form-row">' +
        '<div class="form-group"><label>Gender *:</label>' +
        '<select name="gender" required>' +
        '<option value="MALE"' + (student.gender === 'MALE' ? ' selected' : '') + '>Male</option>' +
        '<option value="FEMALE"' + (student.gender === 'FEMALE' ? ' selected' : '') + '>Female</option>' +
        '<option value="OTHER"' + (student.gender === 'OTHER' ? ' selected' : '') + '>Other</option>' +
        '</select>' +
        '</div>' +
        '<div class="form-group"><label>Email:</label><input type="email" name="email" value="' + (student.email || '') + '"></div>' +
        '</div>' +
        '<div class="form-row">' +
        '<div class="form-group"><label>Phone:</label><input type="text" name="phone" value="' + (student.phone || '') + '"></div>' +
        '<div class="form-group"><label>Enrollment Date *:</label><input type="date" name="enrollment_date" value="' + (student.enrollment_date || '') + '" required></div>' +
        '</div>' +
        '<div class="form-row full">' +
        '<div class="form-group"><label>Address:</label><textarea name="address">' + (student.address || '') + '</textarea></div>' +
        '</div>' +
        '<div class="form-row">' +
        '<div class="form-group"><label>Status:</label>' +
        '<select name="status">' +
        '<option value="ACTIVE"' + (student.status === 'ACTIVE' ? ' selected' : '') + '>Active</option>' +
        '<option value="INACTIVE"' + (student.status === 'INACTIVE' ? ' selected' : '') + '>Inactive</option>' +
        '<option value="GRADUATED"' + (student.status === 'GRADUATED' ? ' selected' : '') + '>Graduated</option>' +
        '<option value="SUSPENDED"' + (student.status === 'SUSPENDED' ? ' selected' : '') + '>Suspended</option>' +
        '</select>' +
        '</div>' +
        '</div>' +
        '<div class="action-buttons">' +
        '<button type="submit" class="btn btn-success">Update</button>' +
        '<button type="button" class="btn btn-secondary" onclick="closeModal()">Cancel</button>' +
        '</div>' +
        '</form>'
    );
}

function deleteStudent() {
    if (!isAdmin()) return;
    if (!selectedStudentId) {
        alert('Please select a student to delete.');
        return;
    }
    if (!confirm('Are you sure you want to delete this student?')) return;
    
    simsData.students = simsData.students.filter(function(s) {
        return s.student_id !== selectedStudentId;
    });
    saveData();
    selectedStudentId = null;
    loadStudents();
    alert('Student deleted successfully!');
}

// Course Management
var selectedCourseId = null;

function showCourseManagement() {
    var content = document.getElementById('contentArea');
    if (!content) return;
    
    var admin = isAdmin();
    content.innerHTML = 
        '<div class="search-bar">' +
        (admin ? '<button class="btn btn-success" onclick="showAddCourseModal()">Add Course</button>' : '') +
        '<button class="btn btn-secondary" onclick="loadCourses()">Refresh</button>' +
        '</div>' +
        '<table id="coursesTable">' +
        '<thead><tr><th>ID</th><th>Course Code</th><th>Course Name</th><th>Credits</th><th>Semester</th><th>Academic Year</th><th>Status</th></tr></thead>' +
        '<tbody id="coursesTableBody"></tbody>' +
        '</table>' +
        '<div class="action-buttons">' +
        (admin ? '<button class="btn btn-primary" onclick="editCourse()">Edit</button>' : '') +
        (admin ? '<button class="btn btn-success" onclick="showEnrollStudentModal()">Enroll Student</button>' : '') +
        (admin ? '<button class="btn btn-danger" onclick="deleteCourse()">Delete</button>' : '') +
        '</div>';
    loadCourses();
}

function loadCourses() {
    var tbody = document.getElementById('coursesTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    simsData.courses.forEach(function(course) {
        var row = tbody.insertRow();
        row.onclick = function() {
            document.querySelectorAll('#coursesTableBody tr').forEach(function(r) {
                r.classList.remove('selected');
            });
            row.classList.add('selected');
            selectedCourseId = course.course_id;
        };
        row.innerHTML = 
            '<td>' + course.course_id + '</td>' +
            '<td>' + (course.course_code || '') + '</td>' +
            '<td>' + (course.course_name || '') + '</td>' +
            '<td>' + (course.credits || 0) + '</td>' +
            '<td>' + (course.semester || '') + '</td>' +
            '<td>' + (course.academic_year || '') + '</td>' +
            '<td>' + (course.status || 'ACTIVE') + '</td>';
    });
}

function showAddCourseModal() {
    showModal('Add Course',
        '<form id="addCourseForm" onsubmit="saveCourse(event, null); return false;">' +
        '<div class="form-row">' +
        '<div class="form-group"><label>Course Code *:</label><input type="text" name="course_code" required></div>' +
        '<div class="form-group"><label>Course Name *:</label><input type="text" name="course_name" required></div>' +
        '</div>' +
        '<div class="form-row full">' +
        '<div class="form-group"><label>Description:</label><textarea name="description"></textarea></div>' +
        '</div>' +
        '<div class="form-row">' +
        '<div class="form-group"><label>Credits *:</label><input type="number" name="credits" min="1" required></div>' +
        '<div class="form-group"><label>Semester:</label><input type="text" name="semester" placeholder="e.g., FALL"></div>' +
        '</div>' +
        '<div class="form-row">' +
        '<div class="form-group"><label>Academic Year:</label><input type="text" name="academic_year" placeholder="e.g., 2023-2024"></div>' +
        '<div class="form-group"><label>Status:</label>' +
        '<select name="status"><option value="ACTIVE">Active</option><option value="INACTIVE">Inactive</option></select>' +
        '</div>' +
        '</div>' +
        '<div class="action-buttons">' +
        '<button type="submit" class="btn btn-success">Save</button>' +
        '<button type="button" class="btn btn-secondary" onclick="closeModal()">Cancel</button>' +
        '</div>' +
        '</form>'
    );
}

function saveCourse(e, courseId) {
    e.preventDefault();
    var form = e.target;
    var formData = new FormData(form);
    
    if (courseId) {
        var idx = simsData.courses.findIndex(function(c) {
            return c.course_id === courseId;
        });
        if (idx !== -1) {
            simsData.courses[idx].course_name = formData.get('course_name');
            simsData.courses[idx].description = formData.get('description');
            simsData.courses[idx].credits = parseInt(formData.get('credits'));
            simsData.courses[idx].semester = formData.get('semester');
            simsData.courses[idx].academic_year = formData.get('academic_year');
            simsData.courses[idx].status = formData.get('status');
        }
    } else {
        var maxId = 0;
        simsData.courses.forEach(function(c) {
            if (c.course_id > maxId) maxId = c.course_id;
        });
        simsData.courses.push({
            course_id: maxId + 1,
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
    
    saveData();
    closeModal();
    loadCourses();
    alert(courseId ? 'Course updated successfully!' : 'Course added successfully!');
}

function editCourse() {
    if (!isAdmin()) return;
    if (!selectedCourseId) {
        alert('Please select a course to edit.');
        return;
    }
    var course = simsData.courses.find(function(c) {
        return c.course_id === selectedCourseId;
    });
    if (!course) {
        alert('Course not found.');
        return;
    }
    showModal('Edit Course',
        '<form id="editCourseForm" onsubmit="saveCourse(event, ' + course.course_id + '); return false;">' +
        '<div class="form-row">' +
        '<div class="form-group"><label>Course Code:</label><input type="text" value="' + (course.course_code || '') + '" readonly></div>' +
        '<div class="form-group"><label>Course Name *:</label><input type="text" name="course_name" value="' + (course.course_name || '') + '" required></div>' +
        '</div>' +
        '<div class="form-row full">' +
        '<div class="form-group"><label>Description:</label><textarea name="description">' + (course.description || '') + '</textarea></div>' +
        '</div>' +
        '<div class="form-row">' +
        '<div class="form-group"><label>Credits *:</label><input type="number" name="credits" value="' + (course.credits || 0) + '" min="1" required></div>' +
        '<div class="form-group"><label>Semester:</label><input type="text" name="semester" value="' + (course.semester || '') + '"></div>' +
        '</div>' +
        '<div class="form-row">' +
        '<div class="form-group"><label>Academic Year:</label><input type="text" name="academic_year" value="' + (course.academic_year || '') + '"></div>' +
        '<div class="form-group"><label>Status:</label>' +
        '<select name="status">' +
        '<option value="ACTIVE"' + (course.status === 'ACTIVE' ? ' selected' : '') + '>Active</option>' +
        '<option value="INACTIVE"' + (course.status === 'INACTIVE' ? ' selected' : '') + '>Inactive</option>' +
        '</select>' +
        '</div>' +
        '</div>' +
        '<div class="action-buttons">' +
        '<button type="submit" class="btn btn-success">Update</button>' +
        '<button type="button" class="btn btn-secondary" onclick="closeModal()">Cancel</button>' +
        '</div>' +
        '</form>'
    );
}

function showEnrollStudentModal() {
    if (!selectedCourseId) {
        alert('Please select a course first.');
        return;
    }
    var enrolledIds = simsData.enrollments.filter(function(e) {
        return e.course_id === selectedCourseId && e.status === 'ENROLLED';
    }).map(function(e) {
        return e.student_id;
    });
    var available = simsData.students.filter(function(s) {
        return enrolledIds.indexOf(s.student_id) === -1;
    });
    
    if (available.length === 0) {
        alert('All students are already enrolled in this course.');
        return;
    }
    
    var options = available.map(function(s) {
        return '<option value="' + s.student_id + '">' + (s.student_code || '') + ' - ' + (s.first_name || '') + ' ' + (s.last_name || '') + '</option>';
    }).join('');
    
    showModal('Enroll Student',
        '<form id="enrollForm" onsubmit="enrollStudent(event); return false;">' +
        '<div class="form-row full">' +
        '<div class="form-group">' +
        '<label>Select Student:</label>' +
        '<select name="student_id" required>' +
        '<option value="">-- Select Student --</option>' +
        options +
        '</select>' +
        '</div>' +
        '</div>' +
        '<div class="action-buttons">' +
        '<button type="submit" class="btn btn-success">Enroll</button>' +
        '<button type="button" class="btn btn-secondary" onclick="closeModal()">Cancel</button>' +
        '</div>' +
        '</form>'
    );
}

function enrollStudent(e) {
    e.preventDefault();
    var form = e.target;
    var studentId = parseInt(form.student_id.value);
    
    var exists = simsData.enrollments.find(function(e) {
        return e.student_id === studentId && e.course_id === selectedCourseId;
    });
    if (exists) {
        exists.status = 'ENROLLED';
    } else {
        var maxId = 0;
        simsData.enrollments.forEach(function(e) {
            if (e.enrollment_id > maxId) maxId = e.enrollment_id;
        });
        simsData.enrollments.push({
            enrollment_id: maxId + 1,
            student_id: studentId,
            course_id: selectedCourseId,
            enrollment_date: new Date().toISOString().split('T')[0],
            status: 'ENROLLED'
        });
    }
    
    saveData();
    closeModal();
    alert('Student enrolled successfully!');
}

function deleteCourse() {
    if (!isAdmin()) return;
    if (!selectedCourseId) {
        alert('Please select a course to delete.');
        return;
    }
    if (!confirm('Delete this course?')) return;
    
    simsData.courses = simsData.courses.filter(function(c) {
        return c.course_id !== selectedCourseId;
    });
    saveData();
    selectedCourseId = null;
    loadCourses();
    alert('Course deleted.');
}

// Attendance Management
function showAttendanceManagement() {
    var content = document.getElementById('contentArea');
    if (!content) return;
    
    content.innerHTML = 
        '<div class="search-bar">' +
        '<label>Course:</label>' +
        '<select id="attendanceCourseSelect" onchange="loadStudentsForAttendance()">' +
        '<option value="">-- Select Course --</option>' +
        '</select>' +
        '<label>Date:</label>' +
        '<input type="date" id="attendanceDate" value="' + new Date().toISOString().split('T')[0] + '" onchange="loadStudentsForAttendance()">' +
        '<button class="btn btn-primary" onclick="loadStudentsForAttendance()">Load Students</button>' +
        '<button class="btn btn-success" onclick="saveAttendance()">Save Attendance</button>' +
        '</div>' +
        '<table id="attendanceTable">' +
        '<thead><tr><th>Student Code</th><th>Student Name</th><th>Status</th></tr></thead>' +
        '<tbody id="attendanceTableBody"></tbody>' +
        '</table>';
    loadCoursesForAttendance();
}

function loadCoursesForAttendance() {
    var select = document.getElementById('attendanceCourseSelect');
    if (!select) return;
    
    simsData.courses.forEach(function(course) {
        var option = document.createElement('option');
        option.value = course.course_id;
        option.textContent = (course.course_code || '') + ' - ' + (course.course_name || '');
        select.appendChild(option);
    });
}

function loadStudentsForAttendance() {
    var courseId = parseInt((document.getElementById('attendanceCourseSelect') || {}).value);
    var date = (document.getElementById('attendanceDate') || {}).value;
    
    if (!courseId) {
        alert('Please select a course.');
        return;
    }
    
    var enrolledIds = simsData.enrollments.filter(function(e) {
        return e.course_id === courseId && e.status === 'ENROLLED';
    }).map(function(e) {
        return e.student_id;
    });
    var students = simsData.students.filter(function(s) {
        return enrolledIds.indexOf(s.student_id) !== -1;
    });
    var attendance = simsData.attendance;
    
    var tbody = document.getElementById('attendanceTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    students.forEach(function(student) {
        var existing = attendance.find(function(a) {
            return a.student_id === student.student_id && a.course_id === courseId && a.attendance_date === date;
        });
        var row = tbody.insertRow();
        row.innerHTML = 
            '<td>' + (student.student_code || '') + '</td>' +
            '<td>' + (student.first_name || '') + ' ' + (student.last_name || '') + '</td>' +
            '<td>' +
            '<select class="attendance-status" data-student-id="' + student.student_id + '">' +
            '<option value="PRESENT"' + (existing && existing.status === 'PRESENT' ? ' selected' : '') + '>Present</option>' +
            '<option value="ABSENT"' + (existing && existing.status === 'ABSENT' ? ' selected' : '') + '>Absent</option>' +
            '<option value="LATE"' + (existing && existing.status === 'LATE' ? ' selected' : '') + '>Late</option>' +
            '<option value="EXCUSED"' + (existing && existing.status === 'EXCUSED' ? ' selected' : '') + '>Excused</option>' +
            '</select>' +
            '</td>';
    });
}

function saveAttendance() {
    var courseId = parseInt((document.getElementById('attendanceCourseSelect') || {}).value);
    var date = (document.getElementById('attendanceDate') || {}).value;
    var user = getCurrentUser();
    
    if (!courseId || !date) {
        alert('Please select course and date.');
        return;
    }
    
    var selects = document.querySelectorAll('.attendance-status');
    selects.forEach(function(select) {
        var studentId = parseInt(select.dataset.studentId);
        var status = select.value;
        var existing = simsData.attendance.findIndex(function(a) {
            return a.student_id === studentId && a.course_id === courseId && a.attendance_date === date;
        });
        
        if (existing !== -1) {
            simsData.attendance[existing].status = status;
            simsData.attendance[existing].recorded_by = user.user_id;
        } else {
            var maxId = 0;
            simsData.attendance.forEach(function(a) {
                if (a.attendance_id > maxId) maxId = a.attendance_id;
            });
            simsData.attendance.push({
                attendance_id: maxId + 1,
                student_id: studentId,
                course_id: courseId,
                attendance_date: date,
                status: status,
                remarks: '',
                recorded_by: user.user_id
            });
        }
    });
    
    saveData();
    alert('Attendance recorded successfully!');
}

// Grade Management
function showGradeManagement() {
    var content = document.getElementById('contentArea');
    if (!content) return;
    
    content.innerHTML = 
        '<div class="search-bar">' +
        '<label>Student:</label>' +
        '<select id="gradeStudentSelect"><option value="">-- Select Student --</option></select>' +
        '<label>Course:</label>' +
        '<select id="gradeCourseSelect"><option value="">-- Select Course --</option></select>' +
        '<button class="btn btn-primary" onclick="loadGrades()">Load Grades</button>' +
        '<button class="btn btn-success" onclick="showAddGradeModal()">Add Grade</button>' +
        '</div>' +
        '<table id="gradesTable">' +
        '<thead><tr><th>ID</th><th>Assessment Type</th><th>Assessment Name</th><th>Marks Obtained</th><th>Total Marks</th><th>Percentage</th><th>Grade</th></tr></thead>' +
        '<tbody id="gradesTableBody"></tbody>' +
        '</table>';
    loadStudentsForGrades();
    loadCoursesForGrades();
}

function loadStudentsForGrades() {
    var select = document.getElementById('gradeStudentSelect');
    if (!select) return;
    
    simsData.students.forEach(function(student) {
        var option = document.createElement('option');
        option.value = student.student_id;
        option.textContent = (student.student_code || '') + ' - ' + (student.first_name || '') + ' ' + (student.last_name || '');
        select.appendChild(option);
    });
}

function loadCoursesForGrades() {
    var select = document.getElementById('gradeCourseSelect');
    if (!select) return;
    
    simsData.courses.forEach(function(course) {
        var option = document.createElement('option');
        option.value = course.course_id;
        option.textContent = (course.course_code || '') + ' - ' + (course.course_name || '');
        select.appendChild(option);
    });
}

function loadGrades() {
    var studentId = parseInt((document.getElementById('gradeStudentSelect') || {}).value);
    var courseId = parseInt((document.getElementById('gradeCourseSelect') || {}).value);
    
    if (!studentId || !courseId) {
        alert('Please select both student and course.');
        return;
    }
    
    var grades = simsData.grades.filter(function(g) {
        return g.student_id === studentId && g.course_id === courseId;
    });
    
    var tbody = document.getElementById('gradesTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    grades.forEach(function(grade) {
        var row = tbody.insertRow();
        row.innerHTML = 
            '<td>' + grade.grade_id + '</td>' +
            '<td>' + (grade.assessment_type || '') + '</td>' +
            '<td>' + (grade.assessment_name || '') + '</td>' +
            '<td>' + (grade.marks_obtained || 0) + '</td>' +
            '<td>' + (grade.total_marks || 0) + '</td>' +
            '<td>' + (grade.percentage ? grade.percentage.toFixed(2) + '%' : '0%') + '</td>' +
            '<td>' + (grade.grade_letter || '') + '</td>';
    });
}

function showAddGradeModal() {
    var studentId = (document.getElementById('gradeStudentSelect') || {}).value;
    var courseId = (document.getElementById('gradeCourseSelect') || {}).value;
    
    if (!studentId || !courseId) {
        alert('Please select both student and course first.');
        return;
    }
    
    showModal('Add Grade',
        '<form id="addGradeForm" onsubmit="saveGrade(event); return false;">' +
        '<div class="form-row">' +
        '<div class="form-group"><label>Assessment Type *:</label><input type="text" name="assessment_type" placeholder="e.g., Quiz, Exam, Assignment" required></div>' +
        '<div class="form-group"><label>Assessment Name:</label><input type="text" name="assessment_name"></div>' +
        '</div>' +
        '<div class="form-row">' +
        '<div class="form-group"><label>Marks Obtained *:</label><input type="number" name="marks_obtained" step="0.01" required></div>' +
        '<div class="form-group"><label>Total Marks *:</label><input type="number" name="total_marks" step="0.01" required></div>' +
        '</div>' +
        '<div class="form-row">' +
        '<div class="form-group"><label>Semester:</label><input type="text" name="semester" placeholder="e.g., FALL"></div>' +
        '<div class="form-group"><label>Academic Year:</label><input type="text" name="academic_year" placeholder="e.g., 2023-2024"></div>' +
        '</div>' +
        '<div class="action-buttons">' +
        '<button type="submit" class="btn btn-success">Save</button>' +
        '<button type="button" class="btn btn-secondary" onclick="closeModal()">Cancel</button>' +
        '</div>' +
        '</form>'
    );
}

function saveGrade(e) {
    e.preventDefault();
    var form = e.target;
    var formData = new FormData(form);
    var user = getCurrentUser();
    var studentId = parseInt((document.getElementById('gradeStudentSelect') || {}).value);
    var courseId = parseInt((document.getElementById('gradeCourseSelect') || {}).value);
    
    var marksObtained = parseFloat(formData.get('marks_obtained'));
    var totalMarks = parseFloat(formData.get('total_marks'));
    var percentage = (marksObtained / totalMarks) * 100;
    var gradeLetter = calculateGradeLetter(percentage);
    
    var maxId = 0;
    simsData.grades.forEach(function(g) {
        if (g.grade_id > maxId) maxId = g.grade_id;
    });
    simsData.grades.push({
        grade_id: maxId + 1,
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
    
    saveData();
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
var selectedFinancialId = null;

function showFinancialManagement() {
    if (!isAdmin()) return;
    
    var content = document.getElementById('contentArea');
    if (!content) return;
    
    content.innerHTML = 
        '<div class="search-bar">' +
        '<label>Student:</label>' +
        '<select id="financialStudentSelect" onchange="loadFinancialRecords()">' +
        '<option value="">-- Select Student --</option>' +
        '</select>' +
        '<button class="btn btn-success" onclick="showAddTransactionModal()">Add Transaction</button>' +
        '<div id="balanceDisplay" style="margin-left: 20px; font-weight: bold; font-size: 16px;">Balance: $0.00</div>' +
        '</div>' +
        '<table id="financialTable">' +
        '<thead><tr><th>ID</th><th>Type</th><th>Amount</th><th>Description</th><th>Date</th><th>Due Date</th><th>Status</th><th>Payment Date</th></tr></thead>' +
        '<tbody id="financialTableBody"></tbody>' +
        '</table>' +
        '<div class="action-buttons">' +
        '<button class="btn btn-primary" onclick="recordPayment()">Record Payment</button>' +
        '</div>';
    loadStudentsForFinancial();
}

function loadStudentsForFinancial() {
    var select = document.getElementById('financialStudentSelect');
    if (!select) return;
    
    simsData.students.forEach(function(student) {
        var option = document.createElement('option');
        option.value = student.student_id;
        option.textContent = (student.student_code || '') + ' - ' + (student.first_name || '') + ' ' + (student.last_name || '');
        select.appendChild(option);
    });
}

function loadFinancialRecords() {
    var studentId = parseInt((document.getElementById('financialStudentSelect') || {}).value);
    if (!studentId) {
        var tbody = document.getElementById('financialTableBody');
        if (tbody) tbody.innerHTML = '';
        var balance = document.getElementById('balanceDisplay');
        if (balance) balance.textContent = 'Balance: $0.00';
        return;
    }
    
    var records = simsData.financialRecords.filter(function(r) {
        return r.student_id === studentId;
    });
    
    var tbody = document.getElementById('financialTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    var balance = 0;
    records.forEach(function(record) {
        if (record.status !== 'CANCELLED') {
            if (record.transaction_type === 'FEE' || record.transaction_type === 'PENALTY') {
                balance += parseFloat(record.amount || 0);
            } else {
                balance -= parseFloat(record.amount || 0);
            }
        }
        
        var row = tbody.insertRow();
        row.onclick = function() {
            document.querySelectorAll('#financialTableBody tr').forEach(function(r) {
                r.classList.remove('selected');
            });
            row.classList.add('selected');
            selectedFinancialId = record.financial_id;
        };
        row.innerHTML = 
            '<td>' + record.financial_id + '</td>' +
            '<td>' + (record.transaction_type || '') + '</td>' +
            '<td>$' + parseFloat(record.amount || 0).toFixed(2) + '</td>' +
            '<td>' + (record.description || '') + '</td>' +
            '<td>' + (record.transaction_date || '') + '</td>' +
            '<td>' + (record.due_date || 'N/A') + '</td>' +
            '<td>' + (record.status || 'PENDING') + '</td>' +
            '<td>' + (record.payment_date || 'N/A') + '</td>';
    });
    
    var balanceEl = document.getElementById('balanceDisplay');
    if (balanceEl) balanceEl.textContent = 'Balance: $' + balance.toFixed(2);
}

function showAddTransactionModal() {
    var studentId = (document.getElementById('financialStudentSelect') || {}).value;
    if (!studentId) {
        alert('Please select a student first.');
        return;
    }
    
    showModal('Add Transaction',
        '<form id="addTransactionForm" onsubmit="saveTransaction(event); return false;">' +
        '<div class="form-row">' +
        '<div class="form-group"><label>Transaction Type *:</label>' +
        '<select name="transaction_type" required>' +
        '<option value="FEE">Fee</option>' +
        '<option value="PAYMENT">Payment</option>' +
        '<option value="REFUND">Refund</option>' +
        '<option value="SCHOLARSHIP">Scholarship</option>' +
        '<option value="PENALTY">Penalty</option>' +
        '</select>' +
        '</div>' +
        '<div class="form-group"><label>Amount *:</label><input type="number" name="amount" step="0.01" required></div>' +
        '</div>' +
        '<div class="form-row full">' +
        '<div class="form-group"><label>Description:</label><textarea name="description"></textarea></div>' +
        '</div>' +
        '<div class="form-row">' +
        '<div class="form-group"><label>Transaction Date *:</label><input type="date" name="transaction_date" value="' + new Date().toISOString().split('T')[0] + '" required></div>' +
        '<div class="form-group"><label>Due Date:</label><input type="date" name="due_date"></div>' +
        '</div>' +
        '<div class="action-buttons">' +
        '<button type="submit" class="btn btn-success">Save</button>' +
        '<button type="button" class="btn btn-secondary" onclick="closeModal()">Cancel</button>' +
        '</div>' +
        '</form>'
    );
}

function saveTransaction(e) {
    e.preventDefault();
    var form = e.target;
    var formData = new FormData(form);
    var user = getCurrentUser();
    var studentId = parseInt((document.getElementById('financialStudentSelect') || {}).value);
    
    var maxId = 0;
    simsData.financialRecords.forEach(function(r) {
        if (r.financial_id > maxId) maxId = r.financial_id;
    });
    simsData.financialRecords.push({
        financial_id: maxId + 1,
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
    
    saveData();
    closeModal();
    loadFinancialRecords();
    alert('Transaction recorded successfully!');
}

function recordPayment() {
    var selected = document.querySelector('#financialTableBody tr.selected');
    if (!selected) {
        alert('Please select a transaction to record payment.');
        return;
    }
    
    var financialId = selectedFinancialId;
    var paymentMethod = prompt('Enter payment method:');
    if (!paymentMethod) return;
    
    var record = simsData.financialRecords.find(function(r) {
        return r.financial_id === financialId;
    });
    if (record) {
        record.status = 'PAID';
        record.payment_method = paymentMethod;
        record.payment_date = new Date().toISOString().split('T')[0];
        record.receipt_number = 'REC' + financialId;
        saveData();
        loadFinancialRecords();
        alert('Payment recorded successfully!');
    }
}

// Modal functions
function showModal(title, content) {
    var modal = document.createElement('div');
    modal.className = 'modal';
    modal.style.display = 'block';
    modal.innerHTML = 
        '<div class="modal-content">' +
        '<div class="modal-header">' +
        '<h2>' + title + '</h2>' +
        '<span class="close" onclick="closeModal()">&times;</span>' +
        '</div>' +
        content +
        '</div>';
    document.body.appendChild(modal);
    modal.onclick = function(e) {
        if (e.target === modal) closeModal();
    };
}

function closeModal() {
    var modals = document.querySelectorAll('.modal');
    modals.forEach(function(m) {
        m.remove();
    });
}
