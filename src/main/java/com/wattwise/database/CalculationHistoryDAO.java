
package com.wattwise.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
public class CalculationHistoryDAO {

    public void saveCalculation(
            double dailyEnergy,
            double dailyCost,
            double monthlyEnergy,
            double monthlyCost,
            double tariff) throws SQLException {

        String sql = """
                INSERT INTO calculation_history
                (daily_energy_kwh, daily_cost,
                 monthly_energy_kwh, monthly_cost,
                 tariff_per_kwh)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setDouble(1, dailyEnergy);
            statement.setDouble(2, dailyCost);
            statement.setDouble(3, monthlyEnergy);
            statement.setDouble(4, monthlyCost);
            statement.setDouble(5, tariff);

            statement.executeUpdate();

            System.out.println(
                    "Calculation saved to database successfully!");
        }
    }
    
public List<CalculationHistoryRecord> getCalculationHistory()
        throws SQLException {

    List<CalculationHistoryRecord> history = new ArrayList<>();

    String sql = """
            SELECT id, calculated_at,
                   daily_energy_kwh, daily_cost,
                   monthly_energy_kwh, monthly_cost,
                   tariff_per_kwh
            FROM calculation_history
            ORDER BY id DESC
            """;

    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
         ResultSet resultSet = statement.executeQuery()) {

        while (resultSet.next()) {

            Timestamp timestamp = resultSet.getTimestamp("calculated_at");

            history.add(new CalculationHistoryRecord(
                    resultSet.getLong("id"),
                    timestamp.toLocalDateTime(),
                    resultSet.getDouble("daily_energy_kwh"),
                    resultSet.getDouble("daily_cost"),
                    resultSet.getDouble("monthly_energy_kwh"),
                    resultSet.getDouble("monthly_cost"),
                    resultSet.getDouble("tariff_per_kwh")
            ));
        }
    }

    return history;
}

}
