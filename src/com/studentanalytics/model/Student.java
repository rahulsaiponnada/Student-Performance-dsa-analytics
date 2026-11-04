package com.studentanalytics.model;

public class Student {
    private int studentId;
    private String name;
    private String department;
    private int year;

    public Student(int studentId, String name, String department, int year) {
        this.studentId = studentId;
        this.name = name;
        this.department = department;
        this.year = year;
    }

    public int getStudentId() { return studentId; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public int getYear() { return year; }
}