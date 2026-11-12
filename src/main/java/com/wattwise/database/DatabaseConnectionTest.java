
package com.wattwise.database;

public class DatabaseConnectionTest {

    public static void main(String[] args) {
        System.out.println("Testing WattWise database connection...");

        if (DatabaseConnection.testConnection()) {
            System.out.println("SUCCESS: Connected to MySQL!");
            System.out.println("Database: wattwise_db");
        } else {
            System.out.println("FAILED: Could not connect to MySQL.");
        }
    }
}
