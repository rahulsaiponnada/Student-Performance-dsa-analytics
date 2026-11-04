package com.studentanalytics.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
        System.getenv().getOrDefault(
            "DB_URL",
            "jdbc:mysql://localhost:3306/student_analytics?useSSL=false&serverTimezone=UTC"
        );

    private static final String USER =
        System.getenv().getOrDefault("DB_USER", "root");

    private static final String PASSWORD =
        System.getenv("DB_PASSWORD");

    public static Connection getConnection() throws SQLException {

        if (PASSWORD == null || PASSWORD.isEmpty()) {
            throw new SQLException("DB_PASSWORD environment variable is not set.");
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
