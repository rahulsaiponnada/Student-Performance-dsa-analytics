CREATE DATABASE student_analytics;
USE student_analytics;

CREATE TABLE students (
    student_id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(50),
    year INT
);

CREATE TABLE marks (
    mark_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT,
    subject VARCHAR(100),
    marks DOUBLE,
    max_marks DOUBLE,
    FOREIGN KEY (student_id) REFERENCES students(student_id)
);

CREATE TABLE dsa_problems (
    problem_id INT PRIMARY KEY,
    title VARCHAR(150),
    topic VARCHAR(100),
    difficulty VARCHAR(20),
    solved BOOLEAN
);