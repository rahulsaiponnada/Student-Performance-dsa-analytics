package com.studentanalytics.model;

public class Marks {
    private int studentId;
    private String subject;
    private double marks;
    private double maxMarks;

    public Marks(int studentId, String subject, double marks, double maxMarks) {
        this.studentId = studentId;
        this.subject = subject;
        this.marks = marks;
        this.maxMarks = maxMarks;
    }

    public int getStudentId() { return studentId; }
    public String getSubject() { return subject; }
    public double getMarks() { return marks; }
    public double getMaxMarks() { return maxMarks; }

    public double getPercentage() {
        return maxMarks > 0 ? (marks / maxMarks) * 100 : 0;
    }
}