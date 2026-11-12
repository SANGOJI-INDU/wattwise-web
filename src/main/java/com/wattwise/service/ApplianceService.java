package com.wattwise.service;

import com.wattwise.database.ApplianceRepository;
import com.wattwise.model.Appliance;

import java.sql.SQLException;
import java.util.List;

public class ApplianceService {

    private final ApplianceRepository repository =
            new ApplianceRepository();

    // Validate appliance details
    private void validateAppliance(Appliance appliance) {

        if (appliance == null) {
            throw new IllegalArgumentException(
                    "Appliance cannot be empty."
            );
        }

        if (appliance.getName() == null
                || appliance.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Appliance name is required."
            );
        }

        if (!Double.isFinite(appliance.getPowerWatts())
                || appliance.getPowerWatts() <= 0) {
            throw new IllegalArgumentException(
                    "Power must be greater than zero."
            );
        }

        if (!Double.isFinite(appliance.getHoursPerDay())
                || appliance.getHoursPerDay() <= 0
                || appliance.getHoursPerDay() > 24) {
            throw new IllegalArgumentException(
                    "Hours must be over 0 and at most 24."
            );
        }

        if (appliance.getQuantity() <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero."
            );
        }
    }

    // Create
    public void saveAppliance(Appliance appliance)
            throws SQLException {

        validateAppliance(appliance);
        repository.save(appliance);
    }

    // Read
    public List<Appliance> loadAppliances()
            throws SQLException {

        return repository.findAll();
    }

    // Update
    public boolean updateAppliance(Appliance appliance)
            throws SQLException {

        validateAppliance(appliance);

        if (appliance.getId() <= 0) {
            throw new IllegalArgumentException(
                    "A valid appliance ID is required for updating."
            );
        }

        return repository.update(appliance);
    }

    // Delete
    public boolean deleteAppliance(int id)
            throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "A valid appliance ID is required."
            );
        }

        return repository.deleteById(id);
    }

    // Synchronize the complete appliance list with MySQL
    public void saveAppliances(List<Appliance> appliances)
            throws SQLException {

        if (appliances == null || appliances.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one appliance is required."
            );
        }

        for (Appliance appliance : appliances) {
            validateAppliance(appliance);
        }

        repository.replaceAll(appliances);
    }

    // Calculate total daily energy consumption
    public double calculateTotalDailyEnergy(
            List<Appliance> appliances) {

        double total = 0;

        for (Appliance appliance : appliances) {
            total += appliance.getDailyEnergyKwh();
        }

        return total;
    }

    // Calculate total monthly energy consumption
    public double calculateTotalMonthlyEnergy(
            List<Appliance> appliances) {

        double total = 0;

        for (Appliance appliance : appliances) {
            total += appliance.getMonthlyEnergyKwh();
        }

        return total;
    }

    // Calculate monthly electricity cost
    public double calculateMonthlyCost(
            double monthlyEnergyKwh,
            double tariffPerUnit) {

        if (!Double.isFinite(monthlyEnergyKwh)
                || !Double.isFinite(tariffPerUnit)
                || monthlyEnergyKwh < 0
                || tariffPerUnit < 0) {

            throw new IllegalArgumentException(
                    "Energy and tariff must be valid non-negative numbers."
            );
        }

        return monthlyEnergyKwh * tariffPerUnit;
    }

    // Find the appliance with the highest daily consumption
    public Appliance findHighestConsumptionAppliance(
            List<Appliance> appliances) {

        Appliance highest = null;

        for (Appliance appliance : appliances) {
            if (highest == null
                    || appliance.getDailyEnergyKwh()
                    > highest.getDailyEnergyKwh()) {
                highest = appliance;
            }
        }

        return highest;
    }
}