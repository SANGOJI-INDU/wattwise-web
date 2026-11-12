package com.wattwise.model;

public class Appliance {

    private int id;
    private String name;
    private double powerWatts;
    private double hoursPerDay;
    private int quantity;

    public Appliance(int id, String name, double powerWatts,
                     double hoursPerDay, int quantity) {
        this.id = id;
        this.name = name;
        this.powerWatts = powerWatts;
        this.hoursPerDay = hoursPerDay;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPowerWatts() {
        return powerWatts;
    }

    public double getHoursPerDay() {
        return hoursPerDay;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPowerWatts(double powerWatts) {
        this.powerWatts = powerWatts;
    }

    public void setHoursPerDay(double hoursPerDay) {
        this.hoursPerDay = hoursPerDay;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getDailyEnergyKwh() {
        return (powerWatts * hoursPerDay * quantity) / 1000.0;
    }

    public double getMonthlyEnergyKwh() {
        return getDailyEnergyKwh() * 30;
    }
}
