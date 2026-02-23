package com.travelxp.controllers;

import com.travelxp.entities.Trip;
import com.travelxp.entities.TripMilestone;
import com.travelxp.services.TripMilestoneService;
import com.travelxp.services.TripService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class TripMilestoneFormController {

    @FXML private Label formTitle;

    @FXML private ComboBox<Trip> tripCombo;
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker milestoneDatePicker;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextField xpField;

    @FXML private Label errorLabel;

    private TripService tripService;
    private TripMilestoneService milestoneService;
    private Runnable onSaved;

    private boolean editMode = false;
    private TripMilestone editing;

    public void setTripService(TripService tripService) {
        this.tripService = tripService;
        loadTrips();
    }

    public void setMilestoneService(TripMilestoneService milestoneService) {
        this.milestoneService = milestoneService;
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    @FXML
    public void initialize() {
        statusCombo.getItems().addAll("PLANNED", "ONGOING", "COMPLETED", "CANCELLED");
        statusCombo.setValue("PLANNED");

        // لعرض اسم الرحلة بدل Object
        tripCombo.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Trip item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : (item.getId() + " - " + item.getTripName()));
            }
        });
        tripCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Trip item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : (item.getId() + " - " + item.getTripName()));
            }
        });

        hideError();
    }

    private void loadTrips() {
        if (tripCombo == null || tripService == null) return;
        try {
            List<Trip> trips = tripService.getAllTrips();
            tripCombo.getItems().setAll(trips);
        } catch (Exception e) {
            // لا نوقف الفورم، فقط رسالة
            System.out.println("Could not load trips: " + e.getMessage());
        }
    }

    public void setAddMode() {
        editMode = false;
        editing = null;
        formTitle.setText("Add Milestone");
        hideError();
    }

    public void setEditMode(TripMilestone m) {
        editMode = true;
        editing = m;
        formTitle.setText("Edit Milestone (ID: " + m.getId() + ")");

        // Trip selected
        if (tripCombo != null) {
            for (Trip t : tripCombo.getItems()) {
                if (t != null && t.getId() != null && t.getId().equals(m.getTripId())) {
                    tripCombo.setValue(t);
                    break;
                }
            }
        }

        titleField.setText(nvl(m.getTitle()));
        descriptionArea.setText(nvl(m.getDescription()));
        milestoneDatePicker.setValue(m.getMilestoneDate());
        statusCombo.setValue(m.getStatus() == null || m.getStatus().isBlank() ? "PLANNED" : m.getStatus());
        xpField.setText(m.getXpEarned() == null ? "0" : String.valueOf(m.getXpEarned()));

        hideError();
    }

    @FXML
    public void save() {
        hideError();

        if (milestoneService == null) {
            showError("Internal error: TripMilestoneService is not set.");
            return;
        }

        try {
            TripMilestone m = buildFromInputs();

            if (!editMode) {
                milestoneService.addMilestone(m);
            } else {
                m.setId(editing.getId());
                milestoneService.updateMilestone(m);
            }

            if (onSaved != null) onSaved.run();
            close();

        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (SQLException ex) {
            showError("DB Error: " + ex.getMessage());
        }
    }

    @FXML
    public void cancel() {
        close();
    }

    private TripMilestone buildFromInputs() {
        Trip selectedTrip = tripCombo.getValue();
        if (selectedTrip == null || selectedTrip.getId() == null) {
            throw new IllegalArgumentException("Trip is required.");
        }

        String title = titleField.getText().trim();
        if (title.isEmpty()) throw new IllegalArgumentException("Title is required.");

        LocalDate date = milestoneDatePicker.getValue();
        if (date == null) throw new IllegalArgumentException("Milestone date is required.");

        int xp = 0;
        String xpTxt = xpField.getText().trim();
        if (!xpTxt.isEmpty()) {
            xp = parseInt(xpTxt, "XP must be a valid integer.");
            if (xp < 0) throw new IllegalArgumentException("XP cannot be negative.");
        }

        TripMilestone m = new TripMilestone();
        m.setTripId(selectedTrip.getId());
        m.setTitle(title);
        m.setDescription(emptyToNull(descriptionArea.getText()));
        m.setMilestoneDate(date);
        m.setStatus(statusCombo.getValue());
        m.setXpEarned(xp);

        return m;
    }

    private void close() {
        Stage stage = (Stage) formTitle.getScene().getWindow();
        stage.close();
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

    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static int parseInt(String s, String msg) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { throw new IllegalArgumentException(msg); }
    }
}