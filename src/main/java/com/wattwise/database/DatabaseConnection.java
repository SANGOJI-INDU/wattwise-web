
package com.wattwise.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private static final String URL = System.getenv().getOrDefault(
            "WATTWISE_DB_URL", "jdbc:mysql://localhost:3306/wattwise_db");

    private static final String USER =
            System.getenv().getOrDefault("WATTWISE_DB_USER", "root");

    private static final String PASSWORD =
            System.getenv("WATTWISE_DB_PASSWORD");

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        if (PASSWORD == null || PASSWORD.isBlank()) {
            throw new SQLException(
                    "WATTWISE_DB_PASSWORD is missing or empty."
            );
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** Creates the tables if they do not exist (needed on a fresh cloud DB). */
    public static void initSchema() {
        try (Connection connection = getConnection();
             java.sql.Statement st = connection.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS appliances ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "name VARCHAR(100) NOT NULL, "
                    + "power_watts DOUBLE NOT NULL, "
                    + "hours_per_day DOUBLE NOT NULL, "
                    + "quantity INT NOT NULL DEFAULT 1)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS calculation_history ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "daily_energy_kwh DOUBLE NOT NULL, "
                    + "daily_cost DOUBLE NOT NULL, "
                    + "monthly_energy_kwh DOUBLE NOT NULL, "
                    + "monthly_cost DOUBLE NOT NULL, "
                    + "tariff_per_kwh DOUBLE NOT NULL)");
            System.out.println("Database tables ready.");
        } catch (SQLException exception) {
            System.err.println("Could not initialise database: "
                    + exception.getMessage());
        }
    }

    public static boolean testConnection() {
        System.out.println("Database URL: " + URL);
        System.out.println("Database user: " + USER);
        System.out.println("Password variable available: "
                + (PASSWORD != null && !PASSWORD.isBlank()));

        try (Connection connection = getConnection()) {
            System.out.println("Connection valid: "
                    + connection.isValid(5));
            return true;
        } catch (SQLException exception) {
            System.err.println("Database error code: "
                    + exception.getErrorCode());
            System.err.println("SQL state: "
                    + exception.getSQLState());
            System.err.println("Database error: "
                    + exception.getMessage());
            return false;
        }
    }
}
