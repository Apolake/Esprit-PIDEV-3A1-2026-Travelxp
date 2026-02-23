package com.travelxp.controllers;

import com.travelxp.entities.TripMilestone;
import com.travelxp.services.TripMilestoneService;
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

public class TripMilestoneListController {

    @FXML private TableView<TripMilestone> milestoneTable;

    @FXML private TableColumn<TripMilestone, Long> colId;
    @FXML private TableColumn<TripMilestone, Long> colTripId;
    @FXML private TableColumn<TripMilestone, String> colTitle;
    @FXML private TableColumn<TripMilestone, Object> colDate;
    @FXML private TableColumn<TripMilestone, String> colStatus;
    @FXML private TableColumn<TripMilestone, Integer> colXp;

    @FXML private Label errorLabel;

    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    private TripMilestoneService milestoneService;
    private TripService tripService;

    private final ObservableList<TripMilestone> milestones = FXCollections.observableArrayList();

    public void setMilestoneService(TripMilestoneService milestoneService) {
        this.milestoneService = milestoneService;
    }

    public void setTripService(TripService tripService) {
        this.tripService = tripService;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTripId.setCellValueFactory(new PropertyValueFactory<>("tripId"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("milestoneDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colXp.setCellValueFactory(new PropertyValueFactory<>("xpEarned"));

        milestoneTable.setItems(milestones);

        if (btnEdit != null) btnEdit.setDisable(true);
        if (btnDelete != null) btnDelete.setDisable(true);

        milestoneTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            boolean disable = (n == null);
            if (btnEdit != null) btnEdit.setDisable(disable);
            if (btnDelete != null) btnDelete.setDisable(disable);
        });

        hideError();
    }

    @FXML
    public void refresh() {
        hideError();

        if (milestoneService == null) {
            showError("Internal error: TripMilestoneService is not injected.");
            return;
        }

        try {
            milestones.setAll(milestoneService.getAllMilestones());
        } catch (SQLException e) {
            showError("DB Error: " + e.getMessage());
        }
    }

    @FXML
    public void openAdd() {
        openMilestoneForm(null);
    }

    @FXML
    public void openEdit() {
        TripMilestone selected = milestoneTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Select a milestone first.");
            return;
        }
        openMilestoneForm(selected);
    }

    @FXML
    public void deleteSelected() {
        TripMilestone selected = milestoneTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Select a milestone first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Milestone ID: " + selected.getId() + " ?");
        confirm.setContentText("This action cannot be undone.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            milestoneService.deleteMilestone(selected.getId());
            refresh();
        } catch (SQLException e) {
            showError("DB Error: " + e.getMessage());
        }
    }

    private void openMilestoneForm(TripMilestone toEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/milestone/TripMilestoneForm.fxml"));
            Parent root = loader.load();

            TripMilestoneFormController controller = loader.getController();
            controller.setTripService(tripService);
            controller.setMilestoneService(milestoneService);
            controller.setOnSaved(this::refresh);

            if (toEdit == null) controller.setAddMode();
            else controller.setEditMode(toEdit);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(toEdit == null ? "Add Milestone" : "Edit Milestone");

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