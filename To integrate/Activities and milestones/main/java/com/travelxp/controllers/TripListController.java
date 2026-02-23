package com.travelxp.controllers;

import com.travelxp.entities.Trip;
import com.travelxp.services.TripService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;

public class TripListController {

    @FXML private TableView<Trip> tripTable;

    @FXML private TableColumn<Trip, Long> colId;
    @FXML private TableColumn<Trip, String> colName;
    @FXML private TableColumn<Trip, String> colOrigin;
    @FXML private TableColumn<Trip, String> colDestination;
    @FXML private TableColumn<Trip, Object> colStart;
    @FXML private TableColumn<Trip, Object> colEnd;
    @FXML private TableColumn<Trip, String> colStatus;
    @FXML private TableColumn<Trip, Integer> colXp;

    @FXML private Label errorLabel;

    // (اختياري) إذا وضعت fx:id للأزرار في TripList.fxml
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    // ✅ لم تعد final حتى نقدر نحقنها من MainLayoutController
    private TripService tripService = new TripService();

    private final ObservableList<Trip> trips = FXCollections.observableArrayList();

    // ✅ هذه الدالة مطلوبة لأنك تستعملها في MainLayoutController
    public void setTripService(TripService tripService) {
        if (tripService != null) this.tripService = tripService;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("tripName"));
        colOrigin.setCellValueFactory(new PropertyValueFactory<>("origin"));
        colDestination.setCellValueFactory(new PropertyValueFactory<>("destination"));
        colStart.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEnd.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colXp.setCellValueFactory(new PropertyValueFactory<>("totalXpEarned"));

        tripTable.setItems(trips);

        if (btnEdit != null) btnEdit.setDisable(true);
        if (btnDelete != null) btnDelete.setDisable(true);

        tripTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            boolean disable = (newV == null);
            if (btnEdit != null) btnEdit.setDisable(disable);
            if (btnDelete != null) btnDelete.setDisable(disable);
        });

        hideError();
        refresh();
    }

    @FXML
    public void refresh() {
        hideError();
        try {
            trips.setAll(tripService.getAllTrips());
        } catch (SQLException e) {
            showError("DB Error: " + e.getMessage());
        }
    }

    @FXML
    public void openAdd() {
        openTripForm(null);
    }

    @FXML
    public void openEdit() {
        Trip selected = tripTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Select a trip first.");
            return;
        }
        openTripForm(selected);
    }

    @FXML
    public void deleteSelected() {
        Trip selected = tripTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Select a trip first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Trip ID: " + selected.getId() + " ?");
        confirm.setContentText("This action cannot be undone.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            tripService.deleteTrip(selected.getId());
            refresh();
        } catch (SQLException e) {
            showError("DB Error: " + e.getMessage());
        }
    }

    private void openTripForm(Trip toEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/trip/TripForm.fxml"));
            Parent root = loader.load();

            TripFormController controller = loader.getController();
            controller.setTripService(tripService);
            controller.setOnSaved(this::refresh);

            if (toEdit == null) controller.setAddMode();
            else controller.setEditMode(toEdit);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(toEdit == null ? "Add Trip" : "Edit Trip");

            Scene scene = new Scene(root);

            URL cssUrl = getClass().getResource("/css/app.css");
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

            dialog.setScene(scene);
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showError("UI Error: " + e.getMessage());
        }
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Info");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.getStyleClass().remove("error-label-hidden");
    }

    private void hideError() {
        errorLabel.setText("");
        if (!errorLabel.getStyleClass().contains("error-label-hidden")) {
            errorLabel.getStyleClass().add("error-label-hidden");
        }
    }
}