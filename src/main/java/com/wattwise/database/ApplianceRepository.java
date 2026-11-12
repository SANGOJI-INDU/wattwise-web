package com.wattwise.database;

import com.wattwise.model.Appliance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ApplianceRepository {

    // Create
    public void save(Appliance appliance) throws SQLException {

        String sql = """
                INSERT INTO appliances
                (name, power_watts, hours_per_day, quantity)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, appliance.getName());
            statement.setDouble(2, appliance.getPowerWatts());
            statement.setDouble(3, appliance.getHoursPerDay());
            statement.setInt(4, appliance.getQuantity());

            statement.executeUpdate();
        }
    }

    // Read
    public List<Appliance> findAll() throws SQLException {

        List<Appliance> appliances = new ArrayList<>();

        String sql = """
                SELECT id, name, power_watts, hours_per_day, quantity
                FROM appliances
                ORDER BY id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                appliances.add(new Appliance(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getDouble("power_watts"),
                        resultSet.getDouble("hours_per_day"),
                        resultSet.getInt("quantity")
                ));
            }
        }

        return appliances;
    }

    // Update
    public boolean update(Appliance appliance) throws SQLException {

        String sql = """
                UPDATE appliances
                SET name = ?, power_watts = ?,
                    hours_per_day = ?, quantity = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, appliance.getName());
            statement.setDouble(2, appliance.getPowerWatts());
            statement.setDouble(3, appliance.getHoursPerDay());
            statement.setInt(4, appliance.getQuantity());
            statement.setInt(5, appliance.getId());

            return statement.executeUpdate() > 0;
        }
    }

    // Delete one appliance
    public boolean deleteById(int id) throws SQLException {

        String sql = "DELETE FROM appliances WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    // Replace the complete appliance list in one transaction.
    // If insertion fails, the previous database records are restored.
    public void replaceAll(List<Appliance> appliances)
            throws SQLException {

        String deleteSql = "DELETE FROM appliances";

        String insertSql = """
                INSERT INTO appliances
                (name, power_watts, hours_per_day, quantity)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                try (PreparedStatement deleteStatement =
                             connection.prepareStatement(deleteSql)) {
                    deleteStatement.executeUpdate();
                }

                try (PreparedStatement insertStatement =
                             connection.prepareStatement(insertSql)) {

                    for (Appliance appliance : appliances) {
                        insertStatement.setString(
                                1, appliance.getName()
                        );
                        insertStatement.setDouble(
                                2, appliance.getPowerWatts()
                        );
                        insertStatement.setDouble(
                                3, appliance.getHoursPerDay()
                        );
                        insertStatement.setInt(
                                4, appliance.getQuantity()
                        );

                        insertStatement.addBatch();
                    }

                    insertStatement.executeBatch();
                }

                connection.commit();

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }
}