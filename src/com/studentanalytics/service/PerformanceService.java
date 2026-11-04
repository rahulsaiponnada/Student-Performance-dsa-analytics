
package com.studentanalytics.service;

import java.sql.*;
import java.util.Scanner;

import com.studentanalytics.util.DatabaseConnection;

public class PerformanceService {

    Scanner sc = new Scanner(System.in);

    // Add Student Marks
    public void addMarks() {

        try (Connection con = DatabaseConnection.getConnection()) {

            System.out.print("Enter Student ID: ");
            int studentId = sc.nextInt();
            sc.nextLine();

            System.out.print("Enter Subject Name: ");
            String subject = sc.nextLine();

            System.out.print("Enter Marks Obtained: ");
            double marks = sc.nextDouble();

            System.out.print("Enter Maximum Marks: ");
            double maxMarks = sc.nextDouble();
            sc.nextLine();

            if (maxMarks <= 0 || marks < 0 || marks > maxMarks) {
                System.out.println("Invalid marks entered!");
                return;
            }

            String sql = "INSERT INTO marks " +
                    "(student_id, subject, marks, max_marks) " +
                    "VALUES (?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, studentId);
            ps.setString(2, subject);
            ps.setDouble(3, marks);
            ps.setDouble(4, maxMarks);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Marks added successfully!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // View Overall Student Performance
    public void viewPerformance() {

        String sql = "SELECT s.student_id, s.name, " +
                "SUM(m.marks) AS total_marks, " +
                "SUM(m.max_marks) AS max_marks, " +
                "AVG(m.marks * 100.0 / NULLIF(m.max_marks, 0)) AS percentage " +
                "FROM students s " +
                "JOIN marks m ON s.student_id = m.student_id " +
                "GROUP BY s.student_id, s.name " +
                "ORDER BY percentage DESC";

        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.println("\n===== STUDENT PERFORMANCE =====");

            while (rs.next()) {

                double percentage = rs.getDouble("percentage");

                System.out.println("\nStudent ID: " +
                        rs.getInt("student_id"));

                System.out.println("Name: " +
                        rs.getString("name"));

                System.out.println("Total Marks: " +
                        rs.getDouble("total_marks") + "/" +
                        rs.getDouble("max_marks"));

                System.out.printf("Percentage: %.2f%%%n",
                        percentage);

                System.out.println("Grade: " + calculateGrade(percentage));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Calculate Grade
    private String calculateGrade(double percentage) {

        if (percentage >= 90)
            return "A+";
        else if (percentage >= 80)
            return "A";
        else if (percentage >= 70)
            return "B";
        else if (percentage >= 60)
            return "C";
        else if (percentage >= 50)
            return "D";
        else
            return "F";
    }

    // Individual Student Report
    public void individualReport() {

        try (Connection con = DatabaseConnection.getConnection()) {

            System.out.print("Enter Student ID: ");
            int id = sc.nextInt();
            sc.nextLine();

            String studentSQL =
                    "SELECT student_id, name, email FROM students " +
                    "WHERE student_id = ?";

            PreparedStatement studentPS =
                    con.prepareStatement(studentSQL);

            studentPS.setInt(1, id);

            ResultSet studentRS = studentPS.executeQuery();

            if (!studentRS.next()) {
                System.out.println("Student not found!");
                return;
            }

            System.out.println("\n===== INDIVIDUAL STUDENT REPORT =====");

            System.out.println("Student ID: " +
                    studentRS.getInt("student_id"));

            System.out.println("Name: " +
                    studentRS.getString("name"));

            System.out.println("Email: " +
                    studentRS.getString("email"));

            String marksSQL =
                    "SELECT subject, marks, max_marks " +
                    "FROM marks WHERE student_id = ?";

            PreparedStatement marksPS =
                    con.prepareStatement(marksSQL);

            marksPS.setInt(1, id);

            ResultSet marksRS = marksPS.executeQuery();

            double totalMarks = 0;
            double maxMarks = 0;
            boolean found = false;

            System.out.println("\nSubject-wise Marks:");
            System.out.println("--------------------------------");

            while (marksRS.next()) {

                found = true;

                double marks = marksRS.getDouble("marks");
                double maximum = marksRS.getDouble("max_marks");

                totalMarks += marks;
                maxMarks += maximum;

                System.out.println(
                        marksRS.getString("subject") +
                        ": " + marks + "/" + maximum
                );
            }

            if (!found) {
                System.out.println("No marks recorded for this student.");
                return;
            }

            double percentage = (totalMarks / maxMarks) * 100;

            System.out.println("--------------------------------");
            System.out.println("Total Marks: " +
                    totalMarks + "/" + maxMarks);

            System.out.printf("Percentage: %.2f%%%n", percentage);

            System.out.println("Grade: " +
                    calculateGrade(percentage));

            System.out.println("================================");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Highest Performing Student
    public void showTopper() {

        String sql = "SELECT s.student_id, s.name, " +
                "SUM(m.marks) AS total_marks, " +
                "SUM(m.max_marks) AS max_marks, " +
                "(SUM(m.marks) / NULLIF(SUM(m.max_marks), 0)) * 100 AS percentage " +
                "FROM students s " +
                "JOIN marks m ON s.student_id = m.student_id " +
                "GROUP BY s.student_id, s.name " +
                "HAVING SUM(m.max_marks) > 0 " +
                "ORDER BY percentage DESC LIMIT 1";

        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.println("\n===== TOP PERFORMING STUDENT =====");

            if (rs.next()) {

                System.out.println("Student ID: " +
                        rs.getInt("student_id"));

                System.out.println("Name: " +
                        rs.getString("name"));

                System.out.println("Total Marks: " +
                        rs.getDouble("total_marks") + "/" +
                        rs.getDouble("max_marks"));

                System.out.printf("Percentage: %.2f%%%n",
                        rs.getDouble("percentage"));

            } else {
                System.out.println("No performance records available.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
