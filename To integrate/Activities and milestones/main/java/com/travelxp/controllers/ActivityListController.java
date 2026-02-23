package com.travelxp.controllers;

import com.travelxp.entities.Activity;
import com.travelxp.services.ActivityService;
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

public class ActivityListController {

    @FXML private TableView<Activity> activityTable;

    @FXML private TableColumn<Activity, Long> colId;
    @FXML private TableColumn<Activity, Long> colTripId;
    @FXML private TableColumn<Activity, String> colTitle;
    @FXML private TableColumn<Activity, String> colType;
    @FXML private TableColumn<Activity, Object> colDate;
    @FXML private TableColumn<Activity, Object> colStart;
    @FXML private TableColumn<Activity, Object> colEnd;
    @FXML private TableColumn<Activity, String> colLocation;
    @FXML private TableColumn<Activity, Double> colCost;
    @FXML private TableColumn<Activity, String> colCurrency;
    @FXML private TableColumn<Activity, Integer> colXp;
    @FXML private TableColumn<Activity, String> colStatus;

    @FXML private Label errorLabel;

    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    private ActivityService activityService;
    private TripService tripService;

    private final ObservableList<Activity> activities = FXCollections.observableArrayList();

    // ✅ injection from MainLayoutController
    public void setActivityService(ActivityService activityService) {
        this.activityService = activityService;
    }

    public void setTripService(TripService tripService) {
        this.tripService = tripService;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTripId.setCellValueFactory(new PropertyValueFactory<>("tripId"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("activityDate"));
        colStart.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colEnd.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("locationName"));
        colCost.setCellValueFactory(new PropertyValueFactory<>("costAmount"));
        colCurrency.setCellValueFactory(new PropertyValueFactory<>("currency"));
        colXp.setCellValueFactory(new PropertyValueFactory<>("xpEarned"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        activityTable.setItems(activities);

        // ✅ disable edit/delete until selection
        if (btnEdit != null) btnEdit.setDisable(true);
        if (btnDelete != null) btnDelete.setDisable(true);

        activityTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            boolean disable = (n == null);
            if (btnEdit != null) btnEdit.setDisable(disable);
            if (btnDelete != null) btnDelete.setDisable(disable);
        });

        hideError();
    }

    @FXML
    public void refresh() {
        hideError();

        if (activityService == null) {
            showError("Internal error: ActivityService is not injected.");
            return;
        }

        try {
            activities.setAll(activityService.getAllActivities());
        } catch (SQLException e) {
            showError("DB Error: " + e.getMessage());
        }
    }

    @FXML
    public void openAdd() {
        openActivityForm(null);
    }

    @FXML
    public void openEdit() {
        Activity selected = activityTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Select an activity first.");
            return;
        }
        openActivityForm(selected);
    }

    @FXML
    public void deleteSelected() {
        Activity selected = activityTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Select an activity first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Activity ID: " + selected.getId() + " ?");
        confirm.setContentText("This action cannot be undone.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            activityService.deleteActivity(selected.getId());
            refresh();
        } catch (SQLException e) {
            showError("DB Error: " + e.getMessage());
        }
    }

    private void openActivityForm(Activity toEdit) {
        if (activityService == null) {
            showError("Internal error: ActivityService is not injected.");
            return;
        }
        if (tripService == null) {
            showError("Internal error: TripService is not injected (needed for Trip ID / combo).");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/activity/ActivityForm.fxml"));
            Parent root = loader.load();

            ActivityFormController controller = loader.getController();
            controller.setActivityService(activityService);
            controller.setTripService(tripService);
            controller.setOnSaved(this::refresh);

            if (toEdit == null) controller.setAddMode();
            else controller.setEditMode(toEdit);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(toEdit == null ? "Add Activity" : "Edit Activity");

            Scene scene = new Scene(root);

            // ✅ add CSS once
            URL cssUrl = getClass().getResource("/css/app.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.out.println("⚠ CSS not found: /css/app.css");
            }

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