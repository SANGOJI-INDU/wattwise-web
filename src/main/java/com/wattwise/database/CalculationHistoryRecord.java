
package com.wattwise.database;

import java.time.LocalDateTime;

public class CalculationHistoryRecord {

    private final long id;
    private final LocalDateTime calculatedAt;
    private final double dailyEnergy;
    private final double dailyCost;
    private final double monthlyEnergy;
    private final double monthlyCost;
    private final double tariff;

    public CalculationHistoryRecord(
            long id,
            LocalDateTime calculatedAt,
            double dailyEnergy,
            double dailyCost,
            double monthlyEnergy,
            double monthlyCost,
            double tariff) {

        this.id = id;
        this.calculatedAt = calculatedAt;
        this.dailyEnergy = dailyEnergy;
        this.dailyCost = dailyCost;
        this.monthlyEnergy = monthlyEnergy;
        this.monthlyCost = monthlyCost;
        this.tariff = tariff;
    }

    public long getId() { return id; }
    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public double getDailyEnergy() { return dailyEnergy; }
    public double getDailyCost() { return dailyCost; }
    public double getMonthlyEnergy() { return monthlyEnergy; }
    public double getMonthlyCost() { return monthlyCost; }
    public double getTariff() { return tariff; }
}
