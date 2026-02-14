# Assessment Feedback System (AFS)

A comprehensive Java-based academic management system designed for educational institutions to streamline assessment management, grading, feedback collection, and student-lecturer interactions.

## Overview

The Assessment Feedback System (AFS) is a desktop application built with Java Swing that provides a complete solution for managing academic assessments, student records, lecturer workloads, and performance analytics. The system supports multiple user roles with tailored dashboards and functionality for each.

## Key Features
### Multi-Role Authentication System
- **Students**: View grades, submit assessments, track attendance, access timetables
- **Lecturers**: Grade assignments, manage modules, provide feedback, track submissions
- **Academic Leaders**: Monitor lecturer workloads, assign modules, generate reports
- **Administrators**: Manage users, configure grading standards, oversee system-wide operations

### Core Functionalities

#### For Students
- View assessment results and detailed feedback
- Access personal timetables and class schedules
- Track attendance records
- View profile and academic information
- Monitor submission status for assignments

#### For Lecturers
- Grade student assessments with detailed feedback
- Manage multiple modules and classes
- Track student submissions and deadlines
- Provide personalised feedback to students
- View grading analytics and statistics

#### For Academic Leaders
- Manage lecturer assignments and workloads
- Assign modules to lecturers
- Generate comprehensive performance reports
- Analyse grading patterns and trends
- Balance teaching loads across faculty

#### For Administrators
- Complete user management (students, staff, lecturers)
- Class and module management
- Configure grading standards and rules
- Manage system-wide settings
- Generate administrative reports

## Technical Stack

- **Language**: Java (SE 8+)
- **GUI Framework**: Java Swing
- **Data Storage**: Text-based file system (CSV format)
- **Architecture**: MVC-inspired design pattern
- **Build Tool**: Standard Java compilation

## Getting Started
### Prerequisites

- Java Development Kit (JDK) 8 or higher
- Java Runtime Environment (JRE)
- Apache NetBeans

### Default Login Credentials

The system comes with sample data for testing.

## Usage Guide
### First-Time Setup

1. Launch the application
2. Log in with your credentials
3. Navigate through the role-specific dashboard
4. Access features via the navigation menu

### Data Management

The system uses text files for data persistence. All data files are stored in the project root directory and follow CSV format for easy import/export.

### Grading System Configuration

Administrators can customise grading standards:
- Define grade boundaries (A+, A, B+, B, C+, C, C-, D, F+, F, F-)
- Set minimum and maximum marks for each grade
- Ensure complete coverage of the 0-100 mark range
- Prevent overlapping grade ranges

## Features Walkthrough

### Assessment Management
- Create and manage assessments per module
- Set deadlines and submission requirements
- Track submission status in real-time
- Generate automated reminders

### Grading & Feedback
- Streamlined grading interface
- Support for multiple assessment types
- Rich text feedback capabilities
- Grade analytics and distribution charts

### Reporting System
- Student performance reports
- Lecturer workload analysis
- Module-wise statistics
- Export capabilities for external use

### Class Management
- Create and manage classes
- Assign students to classes
- Track attendance and participation
- Generate class rosters

## Security Features

- Role-based access control (RBAC)
- Secure password authentication
- Session management
- Data validation and sanitisation
- File-based permissions

### Coding Standards
- Follow Java naming conventions
- Document all public methods
- Write meaningful commit messages
- Add comments for complex logic

## 📝 Future Enhancements

- [ ] Database integration (MySQL/PostgreSQL)
- [ ] Web-based interface
- [ ] Email notification system
- [ ] Mobile application
- [ ] Advanced analytics dashboard
- [ ] Integration with Learning Management Systems (LMS)
- [ ] Export to PDF/Excel functionality
- [ ] Multi-language support
- [ ] Cloud storage integration

## Known Issues

- Text file concurrency: Multiple simultaneous writes may cause conflicts
- Limited scalability: File-based storage is not suitable for large institutions
- No real-time synchronisation across multiple instances

## Acknowledgments

- Developed as part of the Object-Oriented Development with Java course
- Built for an academic environment
- Inspired by modern learning management systems

**Note**: This system was created for educational purposes.
