
package com.wattwise;

import com.wattwise.model.Appliance;
import com.wattwise.service.ApplianceService;
import com.wattwise.database.CalculationHistoryDAO;

import java.sql.SQLException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class App extends Application {

    private static final String NAVY = "#142B4A";
    private static final String BLUE = "#2563EB";
    private static final String BACKGROUND = "#F3F6FB";
    private static final String MUTED = "#64748B";

    private static final Path SAVE_FILE = Path.of(
            System.getProperty("user.home"),
            ".wattwise.properties"
    );

    private final List<ApplianceEntry> appliances = new ArrayList<>();
    private final ApplianceService applianceService = new ApplianceService();
    private final CalculationHistoryDAO historyDAO =
            new CalculationHistoryDAO();

    private final List<Double> lastEnergies = new ArrayList<>();
    private final List<String> lastNames = new ArrayList<>();

    private Stage stage;
    private VBox applianceRows;
    private TextField tariffInput;
    private TextField budgetInput;
    private ComboBox<String> billingMode;

    private Label energyValue;
    private Label costValue;
    private Label monthlyEnergyValue;
    private Label monthlyCostValue;
    private Label yearlyEnergyValue;
    private Label yearlyCostValue;
    private Label highestApplianceValue;
    private Label message;
    private Label budgetStatus;

    private Label savingRecommendation;
    private Label dailySavingsValue;
    private Label monthlySavingsValue;
    private Label yearlySavingsValue;
    private Label revisedBillValue;

    private VBox insightsList;
    private BarChart<String, Number> energyChart;

    private double lastTariff;
    private double lastTotalEnergy;
    private double lastDailyCost;
    private double lastMonthlyEnergy;
    private double lastMonthlyCost;
    private double lastYearlyEnergy;
    private double lastYearlyCost;

    private double lastDailySavings;
    private double lastMonthlySavings;
    private double lastYearlySavings;
    private double lastRevisedMonthlyCost;

    private boolean calculationValid;

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        Label logo = new Label("W");
        logo.setMinSize(44, 44);
        logo.setAlignment(Pos.CENTER);
        logo.setStyle(
                "-fx-background-color: " + BLUE + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 22px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 12px;"
        );

        Label title = new Label("WattWise");
        title.setStyle(
                "-fx-font-size: 25px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: white;"
        );

        Label tagline = new Label("SMART ENERGY ESTIMATOR");
        tagline.setStyle(
                "-fx-text-fill: #CBD5E1; -fx-font-size: 10px;"
        );

        VBox branding = new VBox(3, title, tagline);
        HBox headerContent = new HBox(13, logo, branding);
        headerContent.setAlignment(Pos.CENTER_LEFT);

        VBox header = new VBox(headerContent);
        header.setPadding(new Insets(22, 30, 22, 30));
        header.setStyle("-fx-background-color: " + NAVY + ";");

        Label welcome = sectionTitle("Electricity Cost Calculator");
        welcome.setStyle(
                "-fx-font-size: 23px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + NAVY + ";"
        );

        Label description = new Label(
                "Estimate electricity use, understand costs, and find ways to save."
        );
        description.setWrapText(true);
        description.setStyle(
                "-fx-font-size: 13px; -fx-text-fill: " + MUTED + ";"
        );

        VBox intro = new VBox(7, welcome, description);

        applianceRows = new VBox(10);
        createApplianceEntry();

        Button addButton = makeButton(
                "+ Add Appliance", "#E8F0FF", BLUE
        );
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setOnAction(event -> createApplianceEntry());

        Label tariffHeading = smallHeading("ELECTRICITY RATE");
        tariffInput = createInput("Example: 8");

        VBox tariffField = new VBox(
                6,
                tariffHeading,
                hint("Rate in ₹ per kWh (Fixed Rate mode only)"),
                tariffInput
        );

        billingMode = new ComboBox<>();
        billingMode.getItems().addAll(
                "Fixed Rate",
                "Slab-Based Billing"
        );
        billingMode.setValue("Fixed Rate");
        billingMode.setMaxWidth(Double.MAX_VALUE);

        billingMode.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    boolean slabMode =
                            "Slab-Based Billing".equals(newValue);

                    tariffInput.setDisable(slabMode);

                    tariffInput.setPromptText(
                            slabMode
                                    ? "Not required for slab billing"
                                    : "Example: 8"
                    );

                    calculationValid = false;
                }
        );

        Label billingModeHeading = smallHeading("BILLING MODE");

        VBox billingModeField = new VBox(
                6,
                billingModeHeading,
                hint("Choose how your electricity bill is estimated"),
                billingMode
        );

        Label budgetHeading =
                smallHeading("MONTHLY ELECTRICITY BUDGET");

        budgetInput = createInput("Example: 1000");

        VBox budgetField = new VBox(
                6,
                budgetHeading,
                hint("Your maximum target bill in rupees"),
                budgetInput
        );

        Button saveButton = makeButton(
                "Save Details", BLUE, "white"
        );
        saveButton.setOnAction(event -> saveDetails());

        Button loadButton = makeButton(
                "Load Saved Details", "#E8F0FF", BLUE
        );
        loadButton.setOnAction(event -> loadSavedDetails(true));

        Button databaseLoadButton = makeButton(
                "Load from MySQL", "#E8F0FF", BLUE
        );
        databaseLoadButton.setOnAction(event -> loadFromDatabase());

        HBox saveLoadButtons = new HBox(
                10, saveButton, loadButton, databaseLoadButton
        );

        HBox.setHgrow(saveButton, Priority.ALWAYS);
        HBox.setHgrow(loadButton, Priority.ALWAYS);
        HBox.setHgrow(databaseLoadButton, Priority.ALWAYS);

        saveButton.setMaxWidth(Double.MAX_VALUE);
        loadButton.setMaxWidth(Double.MAX_VALUE);
        databaseLoadButton.setMaxWidth(Double.MAX_VALUE);

        VBox inputCard = new VBox(
                18,
                sectionTitle("Your appliances"),
                applianceRows,
                addButton,
                tariffField,
                billingModeField,
                budgetField,
                saveLoadButtons
        );
        styleCard(inputCard);

        Button calculateButton = makeButton(
                "Calculate my cost  →", BLUE, "white"
        );

        calculateButton.setMaxWidth(Double.MAX_VALUE);
        calculateButton.setPrefHeight(46);
        calculateButton.setStyle(
                "-fx-background-color: " + BLUE + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10px;" +
                "-fx-cursor: hand;"
        );

        calculateButton.setOnAction(event -> calculateTotals());

        Button historyButton = makeButton(
                "View History", "#E8F0FF", BLUE
        );

        historyButton.setMaxWidth(Double.MAX_VALUE);
        historyButton.setPrefHeight(42);
        historyButton.setOnAction(event ->
                new CalculationHistoryView().show()
        );

        energyValue = new Label("-- kWh");
        costValue = new Label("₹ --");

        HBox dailyResults = pairCards(
                resultCard("Daily energy consumption", energyValue, BLUE),
                resultCard("Daily estimated cost", costValue, "#15803D")
        );

        monthlyEnergyValue = new Label("-- kWh");
        monthlyCostValue = new Label("₹ --");

        HBox monthlyResults = pairCards(
                resultCard("Monthly energy (30 days)", monthlyEnergyValue, BLUE),
                resultCard("Monthly estimated cost", monthlyCostValue, "#15803D")
        );

        yearlyEnergyValue = new Label("-- kWh");
        yearlyCostValue = new Label("₹ --");

        HBox yearlyResults = pairCards(
                resultCard("Yearly energy (365 days)", yearlyEnergyValue, BLUE),
                resultCard("Yearly estimated cost", yearlyCostValue, "#15803D")
        );

        budgetStatus = new Label(
                "Enter a monthly budget to check your estimate."
        );
        budgetStatus.setWrapText(true);
        budgetStatus.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;"
        );

        VBox budgetCard = new VBox(
                10,
                sectionTitle("Monthly budget check"),
                budgetStatus
        );
        styleCard(budgetCard);

        highestApplianceValue = new Label(
                "Calculate to discover your highest-consuming appliance."
        );
        highestApplianceValue.setWrapText(true);
        highestApplianceValue.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + BLUE + ";"
        );

        insightsList = new VBox(10);

        VBox insightsCard = new VBox(
                12,
                sectionTitle("Consumption insights"),
                highestApplianceValue,
                hint("Appliance-wise share of daily consumption"),
                insightsList
        );
        styleCard(insightsCard);

        savingRecommendation = new Label(
                "Calculate your usage to receive an energy-saving suggestion."
        );
        savingRecommendation.setWrapText(true);
        savingRecommendation.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: " + NAVY + ";"
        );

        dailySavingsValue = new Label("₹ --");
        monthlySavingsValue = new Label("₹ --");
        yearlySavingsValue = new Label("₹ --");
        revisedBillValue = new Label("₹ --");

        HBox savingsCards = pairCards(
                resultCard("Potential daily savings", dailySavingsValue, "#15803D"),
                resultCard("Potential monthly savings", monthlySavingsValue, "#15803D")
        );

        HBox yearlySavingsCards = pairCards(
                resultCard("Potential yearly savings", yearlySavingsValue, "#15803D"),
                resultCard("Revised monthly bill", revisedBillValue, BLUE)
        );

        VBox savingsCard = new VBox(
                14,
                sectionTitle("Energy-saving recommendations"),
                savingRecommendation,
                savingsCards,
                yearlySavingsCards,
                hint("Illustrative estimates assume the same tariff and usage pattern.")
        );
        styleCard(savingsCard);

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Appliance");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Energy (kWh/day)");
        yAxis.setForceZeroInRange(true);

        energyChart = new BarChart<>(xAxis, yAxis);
        energyChart.setTitle("Daily Energy by Appliance");
        energyChart.setLegendVisible(false);
        energyChart.setAnimated(false);
        energyChart.setCategoryGap(25);
        energyChart.setBarGap(5);
        energyChart.setPrefHeight(340);
        energyChart.setMinHeight(280);

        VBox chartCard = new VBox(
                12,
                sectionTitle("Energy usage comparison"),
                hint("Compare the daily energy used by each appliance."),
                energyChart
        );
        styleCard(chartCard);

        Button exportButton = makeButton(
                "Export CSV Report", BLUE, "white"
        );
        exportButton.setMaxWidth(Double.MAX_VALUE);
        exportButton.setOnAction(event -> exportCsv());

        message = new Label(
                "Enter appliance details, tariff, and monthly budget."
        );
        message.setWrapText(true);
        message.setStyle(
                "-fx-text-fill: " + MUTED + "; -fx-font-size: 12px;"
        );

        VBox content = new VBox(
                20,
                intro,
                inputCard,
                calculateButton,
                historyButton,
                sectionTitle("Daily estimate"),
                dailyResults,
                sectionTitle("Monthly estimate"),
                monthlyResults,
                sectionTitle("Yearly estimate"),
                yearlyResults,
                budgetCard,
                insightsCard,
                savingsCard,
                chartCard,
                exportButton,
                message
        );

        content.setPadding(new Insets(26, 30, 30, 30));
        content.setStyle("-fx-background-color: " + BACKGROUND + ";");

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle(
                "-fx-background: " + BACKGROUND + ";" +
                "-fx-background-color: " + BACKGROUND + ";"
        );

        VBox page = new VBox(header, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Scene scene = new Scene(page, 800, 1000);
        stage.setTitle("WattWise | Smart Energy Estimator");
        stage.setMinWidth(650);
        stage.setMinHeight(650);
        stage.setScene(scene);

        // Start with a fresh screen. Saved details are loaded manually.
        stage.show();
    }

    private void createApplianceEntry() {
        addApplianceEntry("", "", "");
    }

    private void addApplianceEntry(
            String savedName,
            String savedPower,
            String savedHours) {

        TextField nameInput = createInput("Appliance *");
        TextField powerInput = createInput("Watts *");
        TextField hoursInput = createInput("Hours/day *");

        nameInput.setPrefWidth(180);
        powerInput.setPrefWidth(100);
        hoursInput.setPrefWidth(105);

        HBox.setHgrow(nameInput, Priority.ALWAYS);

        nameInput.setText(savedName);
        powerInput.setText(savedPower);
        hoursInput.setText(savedHours);

        Button removeButton = makeButton("×", "#FEE2E2", "#B91C1C");
        removeButton.setStyle(
                "-fx-background-color: #FEE2E2;" +
                "-fx-text-fill: #B91C1C;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8px;" +
                "-fx-cursor: hand;"
        );

        ApplianceEntry entry = new ApplianceEntry(
                nameInput, powerInput, hoursInput
        );

        HBox row = new HBox(
                8, nameInput, powerInput, hoursInput, removeButton
        );
        row.setAlignment(Pos.CENTER_LEFT);

        appliances.add(entry);
        applianceRows.getChildren().add(row);

        removeButton.setOnAction(event -> {
            if (appliances.size() > 1) {
                appliances.remove(entry);
                applianceRows.getChildren().remove(row);
                calculationValid = false;
            } else {
                showError("At least one appliance is required.");
            }
        });
    }

    // Load appliance records explicitly from MySQL.
    private void loadFromDatabase() {
        try {
            List<Appliance> savedAppliances =
                    applianceService.loadAppliances();

            if (savedAppliances.isEmpty()) {
                showSuccess(
                        "No appliance records were found in MySQL. "
                        + "Save appliance details first."
                );
                return;
            }

            appliances.clear();
            applianceRows.getChildren().clear();

            for (Appliance appliance : savedAppliances) {
                addApplianceEntry(
                        appliance.getName(),
                        Double.toString(appliance.getPowerWatts()),
                        Double.toString(appliance.getHoursPerDay())
                );
            }

            calculationValid = false;

            showSuccess(
                    savedAppliances.size()
                    + " appliance record(s) loaded from MySQL successfully. "
                    + "Your current billing settings have been retained."
            );

        } catch (SQLException exception) {
            exception.printStackTrace();

            showError(
                    "Could not load appliance details from MySQL: "
                    + exception.getMessage()
            );
        }
    }

    // Progressive monthly slab calculation.
    private double calculateSlabBill(double monthlyUnits) {

        double remainingUnits = monthlyUnits;
        double bill = 0;

        double[] slabLimits = {100, 200, 300};
        double[] slabRates = {1.50, 3.50, 5.50};

        double previousLimit = 0;

        for (int i = 0; i < slabLimits.length; i++) {
            double unitsInSlab = Math.min(
                    remainingUnits,
                    slabLimits[i] - previousLimit
            );

            if (unitsInSlab > 0) {
                bill += unitsInSlab * slabRates[i];
                remainingUnits -= unitsInSlab;
            }

            previousLimit = slabLimits[i];

            if (remainingUnits <= 0) {
                return bill;
            }
        }

        if (remainingUnits > 0) {
            bill += remainingUnits * 7.00;
        }

        return bill;
    }

    private double calculateBill(double energy, double tariff) {
        if ("Slab-Based Billing".equals(billingMode.getValue())) {
            return calculateSlabBill(energy);
        }

        return energy * tariff;
    }

    private void calculateTotals() {
        calculationValid = false;

        try {
            boolean slabMode =
                    "Slab-Based Billing".equals(billingMode.getValue());

            double tariff = 0;

            if (!slabMode) {
                if (tariffInput.getText().trim().isEmpty()) {
                    showError("Please enter the electricity rate.");
                    return;
                }

                tariff = Double.parseDouble(
                        tariffInput.getText().trim()
                );

                if (!Double.isFinite(tariff) || tariff < 0) {
                    showError("Tariff must be zero or greater.");
                    return;
                }
            }

            if (budgetInput.getText().trim().isEmpty()) {
                showError("Please enter your monthly electricity budget.");
                return;
            }

            double budget = Double.parseDouble(
                    budgetInput.getText().trim()
            );

            if (!Double.isFinite(budget) || budget < 0) {
                showError("Monthly budget must be zero or greater.");
                return;
            }

            if (appliances.isEmpty()) {
                showError("Please add at least one appliance.");
                return;
            }

            double totalEnergy = 0;
            double highestEnergy = -1;
            String highestName = "";
            double highestPower = 0;
            double highestHours = 0;

            List<Double> energies = new ArrayList<>();
            List<String> names = new ArrayList<>();

            XYChart.Series<String, Number> series =
                    new XYChart.Series<>();

            Map<String, Integer> nameCounts = new HashMap<>();

            for (ApplianceEntry appliance : appliances) {
                String name = appliance.name.getText().trim();

                if (name.isEmpty()) {
                    showError("Appliance name is mandatory.");
                    return;
                }

                if (appliance.power.getText().trim().isEmpty()) {
                    showError("Power in Watts is mandatory.");
                    return;
                }

                if (appliance.hours.getText().trim().isEmpty()) {
                    showError("Hours per day is mandatory.");
                    return;
                }

                double power = Double.parseDouble(
                        appliance.power.getText().trim()
                );

                double hours = Double.parseDouble(
                        appliance.hours.getText().trim()
                );

                if (!Double.isFinite(power) || power <= 0) {
                    showError("Appliance power must be greater than 0 Watts.");
                    return;
                }

                if (!Double.isFinite(hours) || hours <= 0 || hours > 24) {
                    showError("Appliance hours must be greater than 0 and at most 24.");
                    return;
                }

                double used = power * hours / 1000.0;

                if (!Double.isFinite(used)) {
                    showError("Appliance energy value is too large.");
                    return;
                }

                totalEnergy += used;
                energies.add(used);
                names.add(name);

                if (used > highestEnergy) {
                    highestEnergy = used;
                    highestName = name;
                    highestPower = power;
                    highestHours = hours;
                }

                int count = nameCounts.getOrDefault(name, 0) + 1;
                nameCounts.put(name, count);

                String chartName = count == 1
                        ? name
                        : name + " (" + count + ")";

                series.getData().add(
                        new XYChart.Data<>(chartName, used)
                );
            }

            double dailyCost;
            double monthlyEnergy = totalEnergy * 30;
            double monthlyCost;
            double yearlyEnergy = totalEnergy * 365;
            double yearlyCost;

            if (slabMode) {
                monthlyCost = calculateBill(monthlyEnergy, 0);
                dailyCost = monthlyCost / 30.0;

                // Estimate 365-day annual cost using average monthly usage.
                yearlyCost = calculateBill(yearlyEnergy / 12.0, 0) * 12.0;
            } else {
                dailyCost = totalEnergy * tariff;
                monthlyCost = dailyCost * 30;
                yearlyCost = dailyCost * 365;
            }

            if (!Double.isFinite(totalEnergy)
                    || !Double.isFinite(dailyCost)
                    || !Double.isFinite(monthlyCost)
                    || !Double.isFinite(yearlyCost)
                    || !Double.isFinite(yearlyEnergy)) {
                showError("The calculated values are too large to display.");
                return;
            }

            // Effective average tariff is saved with the history record.
            double effectiveTariff = monthlyEnergy > 0
                    ? monthlyCost / monthlyEnergy
                    : tariff;

            lastNames.clear();
            lastNames.addAll(names);

            lastEnergies.clear();
            lastEnergies.addAll(energies);

            lastTariff = effectiveTariff;
            lastTotalEnergy = totalEnergy;
            lastDailyCost = dailyCost;
            lastMonthlyEnergy = monthlyEnergy;
            lastMonthlyCost = monthlyCost;
            lastYearlyEnergy = yearlyEnergy;
            lastYearlyCost = yearlyCost;

            energyValue.setText(String.format(
                    Locale.US, "%.3f kWh/day", totalEnergy
            ));

            costValue.setText(String.format(
                    Locale.US, "₹%.2f/day", dailyCost
            ));

            monthlyEnergyValue.setText(String.format(
                    Locale.US, "%.3f kWh", monthlyEnergy
            ));

            monthlyCostValue.setText(String.format(
                    Locale.US, "₹%.2f", monthlyCost
            ));

            yearlyEnergyValue.setText(String.format(
                    Locale.US, "%.3f kWh", yearlyEnergy
            ));

            yearlyCostValue.setText(String.format(
                    Locale.US, "₹%.2f", yearlyCost
            ));

            highestApplianceValue.setText(String.format(
                    Locale.US,
                    "Highest-consuming appliance: %s (%.3f kWh/day)",
                    highestName, highestEnergy
            ));

            insightsList.getChildren().clear();

            for (int i = 0; i < names.size(); i++) {
                double percentage = totalEnergy > 0
                        ? energies.get(i) / totalEnergy * 100
                        : 0;

                Label insight = new Label(String.format(
                        Locale.US,
                        "%s — %.3f kWh/day (%.1f%%)",
                        names.get(i), energies.get(i), percentage
                ));

                insight.setWrapText(true);
                insight.setStyle(
                        "-fx-font-size: 13px;" +
                        "-fx-text-fill: " + NAVY + ";" +
                        "-fx-padding: 7 0 7 0;"
                );

                insightsList.getChildren().add(insight);
            }

            double hoursReduced = Math.min(1.0, highestHours);
            double dailyEnergySaved =
                    highestPower * hoursReduced / 1000.0;

            double monthlyEnergySaved = dailyEnergySaved * 30;
            double yearlyEnergySaved = dailyEnergySaved * 365;

            if (slabMode) {
                double reducedMonthlyBill = calculateBill(
                        Math.max(0, monthlyEnergy - monthlyEnergySaved), 0
                );

                double reducedYearlyBill = calculateBill(
                        Math.max(0, yearlyEnergy - yearlyEnergySaved) / 12.0,
                        0
                ) * 12.0;

                lastMonthlySavings = Math.max(
                        0, monthlyCost - reducedMonthlyBill
                );

                lastYearlySavings = Math.max(
                        0, yearlyCost - reducedYearlyBill
                );

                lastDailySavings = lastMonthlySavings / 30.0;

                lastRevisedMonthlyCost = reducedMonthlyBill;

            } else {
                lastDailySavings = dailyEnergySaved * tariff;
                lastMonthlySavings = lastDailySavings * 30;
                lastYearlySavings = lastDailySavings * 365;

                lastRevisedMonthlyCost = Math.max(
                        0, monthlyCost - lastMonthlySavings
                );
            }

            savingRecommendation.setText(String.format(
                    Locale.US,
                    "Recommendation: Reduce %s usage by %.1f hour(s) per day. This could save %.3f kWh daily.",
                    highestName, hoursReduced, dailyEnergySaved
            ));

            dailySavingsValue.setText(String.format(
                    Locale.US, "₹%.2f/day", lastDailySavings
            ));

            monthlySavingsValue.setText(String.format(
                    Locale.US, "₹%.2f/month", lastMonthlySavings
            ));

            yearlySavingsValue.setText(String.format(
                    Locale.US, "₹%.2f/year", lastYearlySavings
            ));

            revisedBillValue.setText(String.format(
                    Locale.US, "₹%.2f", lastRevisedMonthlyCost
            ));

            energyChart.getData().clear();
            energyChart.getData().add(series);

            if (monthlyCost > budget) {
                budgetStatus.setText(String.format(
                        Locale.US,
                        "⚠ Over budget by ₹%.2f. Estimated bill: ₹%.2f | Budget: ₹%.2f",
                        monthlyCost - budget, monthlyCost, budget
                ));

                budgetStatus.setStyle(
                        "-fx-text-fill: #B91C1C;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
                );

            } else {
                budgetStatus.setText(String.format(
                        Locale.US,
                        "✓ Within budget! ₹%.2f remaining. Estimated bill: ₹%.2f | Budget: ₹%.2f",
                        budget - monthlyCost, monthlyCost, budget
                ));

                budgetStatus.setStyle(
                        "-fx-text-fill: #15803D;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
                );
            }

            calculationValid = true;

            try {
                historyDAO.saveCalculation(
                        totalEnergy,
                        dailyCost,
                        monthlyEnergy,
                        monthlyCost,
                        effectiveTariff
                );

                setStatus("Calculation updated and saved to MySQL successfully.");

            } catch (SQLException exception) {
                exception.printStackTrace();
                setStatus(
                        "Calculation completed, but saving to MySQL failed: "
                        + exception.getMessage()
                );
            }

        } catch (NumberFormatException exception) {
            showError(
                    "Please enter valid numeric values for power, hours, tariff, and budget."
            );

        } catch (Exception exception) {
            exception.printStackTrace();
            showError("Calculation failed. Check the terminal for details.");
        }
    }

    private void saveDetails() {
        try {
            boolean slabMode =
                    "Slab-Based Billing".equals(billingMode.getValue());

            double tariff = 0;

            if (!slabMode) {
                if (tariffInput.getText().trim().isEmpty()) {
                    showError("Please enter the electricity rate.");
                    return;
                }

                tariff = Double.parseDouble(tariffInput.getText().trim());

                if (!Double.isFinite(tariff) || tariff < 0) {
                    showError("Tariff must be zero or greater.");
                    return;
                }
            }

            if (budgetInput.getText().trim().isEmpty()) {
                showError("Please enter your monthly electricity budget.");
                return;
            }

            double budget = Double.parseDouble(budgetInput.getText().trim());

            if (!Double.isFinite(budget) || budget < 0) {
                showError("Monthly budget must be zero or greater.");
                return;
            }

            if (appliances.isEmpty()) {
                showError("Add at least one appliance before saving.");
                return;
            }

            Properties properties = new Properties();

            properties.setProperty("tariff", Double.toString(tariff));
            properties.setProperty("budget", budgetInput.getText().trim());
            properties.setProperty("billingMode", billingMode.getValue());
            properties.setProperty("count", Integer.toString(appliances.size()));

            List<Appliance> databaseAppliances = new ArrayList<>();

            for (int i = 0; i < appliances.size(); i++) {
                ApplianceEntry entry = appliances.get(i);

                String name = entry.name.getText().trim();

                if (name.isEmpty()) {
                    showError("Appliance name is mandatory.");
                    return;
                }

                if (entry.power.getText().trim().isEmpty()) {
                    showError("Power in Watts is mandatory.");
                    return;
                }

                if (entry.hours.getText().trim().isEmpty()) {
                    showError("Hours per day is mandatory.");
                    return;
                }

                double power = Double.parseDouble(entry.power.getText().trim());
                double hours = Double.parseDouble(entry.hours.getText().trim());

                if (!Double.isFinite(power) || power <= 0) {
                    showError("Appliance power must be greater than 0 Watts.");
                    return;
                }

                if (!Double.isFinite(hours) || hours <= 0 || hours > 24) {
                    showError("Appliance hours must be greater than 0 and at most 24.");
                    return;
                }

                properties.setProperty("name." + i, name);
                properties.setProperty("power." + i, Double.toString(power));
                properties.setProperty("hours." + i, Double.toString(hours));

                databaseAppliances.add(
                        new Appliance(0, name, power, hours, 1)
                );
            }

            try (var output = Files.newOutputStream(SAVE_FILE)) {
                properties.store(output, "WattWise saved appliance details");
            }

            applianceService.saveAppliances(databaseAppliances);

            calculationValid = false;

            showSuccess(
                    "Appliance details saved locally and to MySQL successfully."
            );

        } catch (NumberFormatException exception) {
            showError(
                    "Enter valid numbers for appliance power, hours, tariff, and budget."
            );

        } catch (SQLException exception) {
            exception.printStackTrace();
            showError(
                    "Details were saved locally, but MySQL saving failed: "
                    + exception.getMessage()
            );

        } catch (IllegalArgumentException exception) {
            showError(
                    "Please check your appliance details: "
                    + exception.getMessage()
            );

        } catch (IOException exception) {
            exception.printStackTrace();
            showError(
                    "Could not save the local backup: "
                    + exception.getMessage()
            );
        }
    }

    private void loadSavedDetails(boolean showMessage) {
        if (!Files.exists(SAVE_FILE)) {
            if (showMessage) {
                showError("No saved details found yet. Save your details first.");
            }
            return;
        }

        try {
            Properties properties = new Properties();

            try (var input = Files.newInputStream(SAVE_FILE)) {
                properties.load(input);
            }

            int count = Integer.parseInt(
                    properties.getProperty("count", "0")
            );

            if (count < 1 || count > 100) {
                showError("Saved appliance data is invalid.");
                return;
            }

            appliances.clear();
            applianceRows.getChildren().clear();

            for (int i = 0; i < count; i++) {
                addApplianceEntry(
                        properties.getProperty("name." + i, ""),
                        properties.getProperty("power." + i, ""),
                        properties.getProperty("hours." + i, "")
                );
            }

            tariffInput.setText(properties.getProperty("tariff", ""));
            budgetInput.setText(properties.getProperty("budget", ""));

            String savedBillingMode =
                    properties.getProperty("billingMode", "Fixed Rate");

            if (billingMode.getItems().contains(savedBillingMode)) {
                billingMode.setValue(savedBillingMode);
            }

            calculationValid = false;

            if (showMessage) {
                showSuccess("Saved appliance details loaded successfully.");
            }

        } catch (Exception exception) {
            exception.printStackTrace();
            showError(
                    "Could not load saved details. The save file may be invalid."
            );
        }
    }

    private void exportCsv() {
        calculateTotals();

        if (!calculationValid) {
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export WattWise Report");
        chooser.setInitialFileName("WattWise_Report.csv");

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "CSV files (*.csv)", "*.csv"
                )
        );

        var file = chooser.showSaveDialog(stage);

        if (file == null) {
            return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(
                file.toPath(), StandardCharsets.UTF_8)) {

            writer.write("WattWise Electricity Report");
            writer.newLine();

            writer.write("Billing mode," + csv(billingMode.getValue()));
            writer.newLine();

            writer.write("Effective average tariff (rupees per kWh)," + lastTariff);
            writer.newLine();

            writer.write("Daily total energy (kWh)," + lastTotalEnergy);
            writer.newLine();

            writer.write("Daily estimated cost (rupees)," + lastDailyCost);
            writer.newLine();

            writer.write("Monthly energy (kWh)," + lastMonthlyEnergy);
            writer.newLine();

            writer.write("Monthly estimated cost (rupees)," + lastMonthlyCost);
            writer.newLine();

            writer.write("Yearly energy (kWh)," + lastYearlyEnergy);
            writer.newLine();

            writer.write("Yearly estimated cost (rupees)," + lastYearlyCost);
            writer.newLine();

            writer.write("Potential daily savings (rupees)," + lastDailySavings);
            writer.newLine();

            writer.write("Potential monthly savings (rupees)," + lastMonthlySavings);
            writer.newLine();

            writer.write("Potential yearly savings (rupees)," + lastYearlySavings);
            writer.newLine();

            writer.write(
                    "Revised monthly bill estimate (rupees),"
                    + lastRevisedMonthlyCost
            );
            writer.newLine();
            writer.newLine();

            writer.write(
                    "Appliance,Power (W),Hours per day,Daily energy (kWh),Daily cost (rupees),Share of total (%)"
            );
            writer.newLine();

            for (int i = 0; i < appliances.size(); i++) {
                ApplianceEntry entry = appliances.get(i);

                double power = Double.parseDouble(entry.power.getText().trim());
                double hours = Double.parseDouble(entry.hours.getText().trim());
                double used = lastEnergies.get(i);

                double share = lastTotalEnergy > 0
                        ? used / lastTotalEnergy * 100
                        : 0;

                writer.write(
                        csv(entry.name.getText()) + ","
                        + power + ","
                        + hours + ","
                        + used + ","
                        + (used * lastTariff) + ","
                        + share
                );

                writer.newLine();
            }

            showSuccess(
                    "CSV report exported successfully to "
                    + file.getAbsolutePath()
            );

        } catch (IOException | NumberFormatException exception) {
            exception.printStackTrace();
            showError("Could not export the report: " + exception.getMessage());
        }
    }

    private String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private void setStatus(String text) {
        message.setText(text);
        message.setStyle(
                "-fx-text-fill: #15803D; -fx-font-size: 12px;"
        );
    }

    private void showSuccess(String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("WattWise");
        alert.setHeaderText("Success");
        alert.setContentText(text);
        alert.initOwner(stage);
        alert.showAndWait();
    }

    private void showError(String text) {
        message.setText(text);
        message.setStyle(
                "-fx-text-fill: #B91C1C; -fx-font-size: 12px;"
        );

        System.out.println("WattWise: " + text);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("WattWise");
        alert.setHeaderText("Please check your details");
        alert.setContentText(text);
        alert.initOwner(stage);
        alert.showAndWait();
    }

    private Button makeButton(
            String text,
            String background,
            String foreground) {

        Button button = new Button(text);

        button.setStyle(
                "-fx-background-color: " + background + ";" +
                "-fx-text-fill: " + foreground + ";" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8px;" +
                "-fx-padding: 10;" +
                "-fx-cursor: hand;"
        );

        return button;
    }

    private Label smallHeading(String text) {
        Label label = new Label(text);

        label.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + NAVY + ";"
        );

        return label;
    }

    private Label hint(String text) {
        Label label = new Label(text);
        label.setWrapText(true);

        label.setStyle(
                "-fx-font-size: 11px; -fx-text-fill: " + MUTED + ";"
        );

        return label;
    }

    private HBox pairCards(VBox first, VBox second) {
        HBox row = new HBox(14, first, second);
        row.setAlignment(Pos.CENTER);

        HBox.setHgrow(first, Priority.ALWAYS);
        HBox.setHgrow(second, Priority.ALWAYS);

        return row;
    }

    private VBox resultCard(
            String caption,
            Label value,
            String color) {

        Label heading = new Label(caption);
        heading.setWrapText(true);

        heading.setStyle(
                "-fx-text-fill: " + MUTED + "; -fx-font-size: 12px;"
        );

        value.setWrapText(true);

        value.setStyle(
                "-fx-font-size: 21px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + color + ";"
        );

        VBox card = new VBox(10, heading, value);
        card.setMaxWidth(Double.MAX_VALUE);

        styleCard(card);

        return card;
    }

    private TextField createInput(String placeholder) {
        TextField field = new TextField();
        field.setPromptText(placeholder);
        field.setPrefHeight(40);

        field.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #D8E0EC;" +
                "-fx-border-radius: 8px;" +
                "-fx-background-radius: 8px;" +
                "-fx-padding: 0 12px;" +
                "-fx-font-size: 13px;"
        );

        return field;
    }

    private Label sectionTitle(String text) {
        Label label = new Label(text);

        label.setStyle(
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + NAVY + ";"
        );

        return label;
    }

    private void styleCard(VBox card) {
        card.setPadding(new Insets(20));

        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 14px;" +
                "-fx-border-color: #E2E8F0;" +
                "-fx-border-radius: 14px;"
        );
    }

    private static class ApplianceEntry {
        TextField name;
        TextField power;
        TextField hours;

        ApplianceEntry(
                TextField name,
                TextField power,
                TextField hours) {

            this.name = name;
            this.power = power;
            this.hours = hours;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
