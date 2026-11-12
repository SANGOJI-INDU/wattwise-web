
package com.wattwise;

import com.wattwise.database.CalculationHistoryDAO;
import com.wattwise.database.CalculationHistoryRecord;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class CalculationHistoryView {

    private final CalculationHistoryDAO historyDAO =
            new CalculationHistoryDAO();

    private final TableView<CalculationHistoryRecord> table =
            new TableView<>();

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("WattWise | Calculation History");

        TableColumn<CalculationHistoryRecord, Long> idColumn =
                new TableColumn<>("ID");
        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("id"));

        TableColumn<CalculationHistoryRecord, String> dateColumn =
                new TableColumn<>("Calculated At");
        dateColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(
                        cell.getValue().getCalculatedAt().format(
                                DateTimeFormatter.ofPattern(
                                        "dd-MM-yyyy HH:mm:ss"))));

        TableColumn<CalculationHistoryRecord, Double> dailyEnergyColumn =
                new TableColumn<>("Daily kWh");
        dailyEnergyColumn.setCellValueFactory(
                new PropertyValueFactory<>("dailyEnergy"));

        TableColumn<CalculationHistoryRecord, Double> dailyCostColumn =
                new TableColumn<>("Daily Cost (₹)");
        dailyCostColumn.setCellValueFactory(
                new PropertyValueFactory<>("dailyCost"));

        TableColumn<CalculationHistoryRecord, Double> monthlyEnergyColumn =
                new TableColumn<>("Monthly kWh");
        monthlyEnergyColumn.setCellValueFactory(
                new PropertyValueFactory<>("monthlyEnergy"));

        TableColumn<CalculationHistoryRecord, Double> monthlyCostColumn =
                new TableColumn<>("Monthly Cost (₹)");
        monthlyCostColumn.setCellValueFactory(
                new PropertyValueFactory<>("monthlyCost"));

        TableColumn<CalculationHistoryRecord, Double> tariffColumn =
                new TableColumn<>("Tariff (₹/kWh)");
        tariffColumn.setCellValueFactory(
                new PropertyValueFactory<>("tariff"));

        table.getColumns().addAll(
                idColumn,
                dateColumn,
                dailyEnergyColumn,
                dailyCostColumn,
                monthlyEnergyColumn,
                monthlyCostColumn,
                tariffColumn
        );

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        Button refreshButton = new Button("Refresh History");
        refreshButton.setOnAction(event -> loadHistory());

        HBox toolbar = new HBox(refreshButton);
        toolbar.setPadding(new Insets(12));

        BorderPane root = new BorderPane();
        root.setTop(toolbar);
        root.setCenter(table);
        root.setPadding(new Insets(10));

        loadHistory();

        stage.setScene(new Scene(root, 1050, 450));
        stage.show();
    }

    private void loadHistory() {
        try {
            List<CalculationHistoryRecord> records =
                    historyDAO.getCalculationHistory();

            ObservableList<CalculationHistoryRecord> data =
                    FXCollections.observableArrayList(records);

            table.setItems(data);

        } catch (SQLException exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Could not load calculation history");
            alert.setContentText(exception.getMessage());
            alert.showAndWait();
        }
    }
}
