
package com.wattwise.database;

import com.wattwise.model.Appliance;

import java.sql.SQLException;
import java.util.List;

public class ApplianceRepositoryTest {

    public static void main(String[] args) {

        ApplianceRepository repository = new ApplianceRepository();

        try {
            Appliance appliance = new Appliance(
                    0,
                    "Test Fan",
                    75.0,
                    8.0,
                    1
            );

            repository.save(appliance);
            System.out.println("SUCCESS: Appliance saved to MySQL!");

            List<Appliance> appliances = repository.findAll();

            System.out.println("\nAppliances stored in database:");

            for (Appliance item : appliances) {
                System.out.println(
                        item.getName()
                        + " | Power: " + item.getPowerWatts() + " W"
                        + " | Hours/day: " + item.getHoursPerDay()
                        + " | Quantity: " + item.getQuantity()
                );
            }

            System.out.println("\nSUCCESS: Appliances retrieved!");

        } catch (SQLException exception) {
            System.err.println("Database test failed:");
            System.err.println(exception.getMessage());
        }
    }
}
