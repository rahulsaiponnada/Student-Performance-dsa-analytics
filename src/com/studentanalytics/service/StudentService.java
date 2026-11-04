package com.studentanalytics.service;

import java.sql.*;
import java.util.Scanner;
import com.studentanalytics.util.DatabaseConnection;

public class StudentService {

    Scanner sc = new Scanner(System.in);

    public void addStudent() {

        try {
            Connection con = DatabaseConnection.getConnection();

            String sql = "INSERT INTO students (name, email) VALUES (?, ?)";

            PreparedStatement ps = con.prepareStatement(sql);

            System.out.print("Enter student name: ");
            ps.setString(1, sc.nextLine());

            System.out.print("Enter email: ");
            ps.setString(2, sc.nextLine());

            ps.executeUpdate();

            System.out.println("Student added successfully!");

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void viewStudents() {

        try {
            Connection con = DatabaseConnection.getConnection();

            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery("SELECT * FROM students");

            while (rs.next()) {
                System.out.println(
                    rs.getInt("student_id") + " | " +
                    rs.getString("name") + " | " +
                    rs.getString("email")
                );
            }

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}