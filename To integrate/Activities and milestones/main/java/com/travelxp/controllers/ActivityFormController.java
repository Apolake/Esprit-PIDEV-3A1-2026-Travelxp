package com.travelxp.controllers;

import com.travelxp.entities.Activity;
import com.travelxp.entities.Trip;
import com.travelxp.services.ActivityService;
import com.travelxp.services.TripService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ActivityFormController {

    @FXML private Label formTitle;

    @FXML private ComboBox<Trip> tripCombo;

    @FXML private TextField titleField;
    @FXML private TextField typeField;
    @FXML private TextArea descriptionArea;

    @FXML private DatePicker activityDatePicker;
    @FXML private TextField startTimeField; // HH:mm
    @FXML private TextField endTimeField;   // HH:mm

    @FXML private TextField locationField;
    @FXML private TextField transportField;

    @FXML private TextField costField;
    @FXML private TextField currencyField;
    @FXML private TextField xpField;

    @FXML private ComboBox<String> statusCombo;

    @FXML private Label errorLabel;

    private ActivityService activityService;
    private TripService tripService;
    private Runnable onSaved;

    private boolean editMode = false;
    private Activity editingActivity;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public void setActivityService(ActivityService activityService) {
        this.activityService = activityService;
    }

    public void setTripService(TripService tripService) {
        this.tripService = tripService;
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    @FXML
    public void initialize() {
        statusCombo.getItems().addAll("PLANNED", "ONGOING", "COMPLETED", "CANCELLED");
        statusCombo.setValue("PLANNED");

        // حتى يظهر Trip بشكل جميل داخل ComboBox
        tripCombo.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Trip item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText("ID: " + item.getId() + " — " + safe(item.getTripName()));
            }
        });
        tripCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Trip item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText("ID: " + item.getId() + " — " + safe(item.getTripName()));
            }
        });

        hideError();
    }

    public void setAddMode() {
        editMode = false;
        editingActivity = null;
        formTitle.setText("Add Activity");
        hideError();
        loadTripsIntoCombo();
    }

    public void setEditMode(Activity a) {
        editMode = true;
        editingActivity = a;
        formTitle.setText("Edit Activity (ID: " + a.getId() + ")");
        hideError();
        loadTripsIntoCombo();

        // تعبئة الحقول
        titleField.setText(safe(a.getTitle()));
        typeField.setText(safe(a.getType()));
        descriptionArea.setText(safe(a.getDescription()));

        activityDatePicker.setValue(a.getActivityDate());

        startTimeField.setText(a.getStartTime() == null ? "" : a.getStartTime().format(TIME_FMT));
        endTimeField.setText(a.getEndTime() == null ? "" : a.getEndTime().format(TIME_FMT));

        locationField.setText(safe(a.getLocationName()));
        transportField.setText(safe(a.getTransportType()));

        costField.setText(a.getCostAmount() == null ? "" : String.valueOf(a.getCostAmount()));
        currencyField.setText(safe(a.getCurrency()));

        xpField.setText(a.getXpEarned() == null ? "" : String.valueOf(a.getXpEarned()));
        statusCombo.setValue(a.getStatus() == null || a.getStatus().isBlank() ? "PLANNED" : a.getStatus());

        // اختيار Trip من القائمة حسب tripId
        if (a.getTripId() != null && tripCombo.getItems() != null) {
            for (Trip t : tripCombo.getItems()) {
                if (t != null && t.getId() != null && t.getId().equals(a.getTripId())) {
                    tripCombo.setValue(t);
                    break;
                }
            }
        }
    }

    private void loadTripsIntoCombo() {
        if (tripService == null) return;
        try {
            List<Trip> trips = tripService.getAllTrips();
            tripCombo.getItems().setAll(trips);
        } catch (SQLException e) {
            showError("Cannot load trips: " + e.getMessage());
        }
    }

    @FXML
    public void save() {
        hideError();

        if (activityService == null) {
            showError("Internal error: ActivityService is not injected.");
            return;
        }

        try {
            Activity a = buildFromInputs();

            if (!editMode) {
                activityService.addActivity(a);
            } else {
                a.setId(editingActivity.getId());
                activityService.updateActivity(a);
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

    private Activity buildFromInputs() {
        Activity a = new Activity();

        // Trip (required)
        Trip selectedTrip = tripCombo.getValue();
        if (selectedTrip == null || selectedTrip.getId() == null) {
            throw new IllegalArgumentException("Trip is required (select a trip).");
        }
        a.setTripId(selectedTrip.getId());

        // title (required)
        String title = titleField.getText().trim();
        if (title.isEmpty()) throw new IllegalArgumentException("Title is required.");
        a.setTitle(title);

        a.setType(emptyToNull(typeField.getText()));
        a.setDescription(emptyToNull(descriptionArea.getText()));

        // date (required)
        LocalDate d = activityDatePicker.getValue();
        if (d == null) throw new IllegalArgumentException("Activity date is required.");
        a.setActivityDate(d);

        // time (optional HH:mm)
        a.setStartTime(parseTimeOrNull(startTimeField.getText(), "Start time must be HH:mm (e.g. 09:30)."));
        a.setEndTime(parseTimeOrNull(endTimeField.getText(), "End time must be HH:mm (e.g. 18:00)."));

        if (a.getStartTime() != null && a.getEndTime() != null && a.getEndTime().isBefore(a.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time.");
        }

        a.setLocationName(emptyToNull(locationField.getText()));
        a.setTransportType(emptyToNull(transportField.getText()));

        // cost (optional >=0)
        String costTxt = costField.getText().trim();
        if (!costTxt.isEmpty()) {
            double cost = parseDouble(costTxt, "Cost must be a valid number.");
            if (cost < 0) throw new IllegalArgumentException("Cost cannot be negative.");
            a.setCostAmount(cost);
        } else {
            a.setCostAmount(null);
        }

        a.setCurrency(emptyToNull(currencyField.getText()));

        // xp (optional >=0)
        String xpTxt = xpField.getText().trim();
        if (!xpTxt.isEmpty()) {
            int xp = parseInt(xpTxt, "XP must be a valid integer.");
            if (xp < 0) throw new IllegalArgumentException("XP cannot be negative.");
            a.setXpEarned(xp);
        } else {
            a.setXpEarned(0);
        }

        a.setStatus(statusCombo.getValue() == null ? "PLANNED" : statusCombo.getValue());

        return a;
    }

    private LocalTime parseTimeOrNull(String txt, String msg) {
        String t = (txt == null) ? "" : txt.trim();
        if (t.isEmpty()) return null;
        try {
            return LocalTime.parse(t, TIME_FMT);
        } catch (Exception e) {
            throw new IllegalArgumentException(msg);
        }
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

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static int parseInt(String s, String msg) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { throw new IllegalArgumentException(msg); }
    }

    private static double parseDouble(String s, String msg) {
        try { return Double.parseDouble(s.trim()); }
        catch (Exception e) { throw new IllegalArgumentException(msg); }
    }
}