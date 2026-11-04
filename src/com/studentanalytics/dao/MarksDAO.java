package com.studentanalytics.dao;

import com.studentanalytics.util.DatabaseConnection;
import java.sql.*;

public class MarksDAO {

    public void addMarks(int studentId, String subject,
                         double marks, double maxMarks) {

        String sql = "INSERT INTO marks (student_id, subject, marks, max_marks) VALUES (?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setString(2, subject);
            ps.setDouble(3, marks);
            ps.setDouble(4, maxMarks);

            ps.executeUpdate();
            System.out.println("Marks added successfully!");

        } catch (SQLException e) {
            System.out.println("Error adding marks: " + e.getMessage());
        }
    }

    public void displayMarks(int studentId) {

        String sql = "SELECT * FROM marks WHERE student_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                System.out.println(
                    rs.getString("subject") + " : " +
                    rs.getDouble("marks") + "/" +
                    rs.getDouble("max_marks")
                );
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving marks: " + e.getMessage());
        }
    }
}