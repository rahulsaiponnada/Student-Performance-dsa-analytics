package com.studentanalytics.app;

import java.sql.Connection;
import com.studentanalytics.util.DatabaseConnection;

public class DatabaseTest {

   public static void main(String[] args) throws Exception {

        Connection con = DatabaseConnection.getConnection();

        if (con != null) {
            System.out.println("Database Connected Successfully!");
        }
    }
}