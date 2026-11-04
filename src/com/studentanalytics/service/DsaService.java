
package com.studentanalytics.service;

import java.sql.*;
import java.util.Scanner;
import com.studentanalytics.util.DatabaseConnection;

public class DsaService {

    Scanner sc = new Scanner(System.in);

    // Add DSA Problem
    public void addProblem() {

        try (Connection con = DatabaseConnection.getConnection()) {

            String sql = "INSERT INTO dsa_problems " +
                    "(title, topic, difficulty, solved) VALUES (?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(sql);

            System.out.print("Enter problem title: ");
            String title = sc.nextLine();

            System.out.print("Enter topic: ");
            String topic = sc.nextLine();

            System.out.print("Enter difficulty (Easy/Medium/Hard): ");
            String difficulty = sc.nextLine();

            System.out.print("Is problem solved? (yes/no): ");
            boolean solved = sc.nextLine().equalsIgnoreCase("yes");

            ps.setString(1, title);
            ps.setString(2, topic);
            ps.setString(3, difficulty);
            ps.setBoolean(4, solved);

            ps.executeUpdate();

            System.out.println("DSA Problem Added Successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // View DSA Problems
    public void viewProblems() {

        try (Connection con = DatabaseConnection.getConnection()) {

            String sql = "SELECT * FROM dsa_problems ORDER BY problem_id";

            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            System.out.println("\n===== DSA PROBLEMS =====");

            while (rs.next()) {

                System.out.println(
                    "ID: " + rs.getInt("problem_id") +
                    " | Title: " + rs.getString("title") +
                    " | Topic: " + rs.getString("topic") +
                    " | Difficulty: " + rs.getString("difficulty") +
                    " | Status: " +
                    (rs.getBoolean("solved") ? "Solved" : "Unsolved")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Show Overall DSA Progress
    public void showProgress() {

        try (Connection con = DatabaseConnection.getConnection()) {

            String sql = "SELECT COUNT(*) AS total, " +
                    "COALESCE(SUM(solved), 0) AS solved_count " +
                    "FROM dsa_problems";

            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            if (rs.next()) {

                int total = rs.getInt("total");
                int solved = rs.getInt("solved_count");
                int unsolved = total - solved;

                double percentage = total == 0 ? 0 :
                        (solved * 100.0) / total;

                System.out.println("\n===== DSA PROGRESS =====");
                System.out.println("Total Problems: " + total);
                System.out.println("Solved Problems: " + solved);
                System.out.println("Unsolved Problems: " + unsolved);

                System.out.printf("Completion Percentage: %.2f%%%n",
                        percentage);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Update DSA Problem Status
    public void updateProblemStatus() {

        try (Connection con = DatabaseConnection.getConnection()) {

            System.out.print("Enter Problem ID: ");
            int id = sc.nextInt();
            sc.nextLine();

            System.out.print("Mark as solved? (yes/no): ");
            boolean solved = sc.nextLine().equalsIgnoreCase("yes");

            String sql = "UPDATE dsa_problems SET solved = ? " +
                    "WHERE problem_id = ?";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setBoolean(1, solved);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Problem status updated successfully!");
            } else {
                System.out.println("Problem ID not found!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Topic-wise DSA Analytics
    public void topicWiseProgress() {

        try (Connection con = DatabaseConnection.getConnection()) {

            String sql = "SELECT COALESCE(topic, 'Uncategorized') AS topic, " +
                    "COUNT(*) AS total, " +
                    "COALESCE(SUM(solved), 0) AS solved_count " +
                    "FROM dsa_problems " +
                    "GROUP BY topic ORDER BY topic";

            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            System.out.println("\n========== TOPIC-WISE DSA ANALYTICS ==========");

            System.out.printf("%-20s %-10s %-10s %-12s%n",
                    "Topic", "Total", "Solved", "Percentage");

            System.out.println("-----------------------------------------------");

            boolean found = false;

            while (rs.next()) {

                found = true;

                String topic = rs.getString("topic");
                int total = rs.getInt("total");
                int solved = rs.getInt("solved_count");

                double percentage = total == 0 ? 0 :
                        (solved * 100.0) / total;

                System.out.printf("%-20s %-10d %-10d %.2f%%%n",
                        topic, total, solved, percentage);
            }

            if (!found) {
                System.out.println("No DSA problems available.");
            }

            System.out.println("===============================================");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
