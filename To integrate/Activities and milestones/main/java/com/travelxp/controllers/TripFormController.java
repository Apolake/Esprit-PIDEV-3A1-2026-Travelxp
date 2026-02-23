package com.travelxp.controllers;

import com.travelxp.entities.Trip;
import com.travelxp.services.TripService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;

public class TripFormController {

    @FXML private Label formTitle;

    @FXML private TextField userIdField;
    @FXML private TextField tripNameField;
    @FXML private TextField originField;
    @FXML private TextField destinationField;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> statusCombo;

    @FXML private TextField budgetField;
    @FXML private TextField currencyField;
    @FXML private TextField expensesField;
    @FXML private TextField xpField;

    @FXML private TextArea notesArea;
    @FXML private TextField coverImageUrlField;

    @FXML private Label errorLabel;

    private TripService tripService;
    private Runnable onSaved;

    private boolean editMode = false;
    private Trip editingTrip;

    public void setTripService(TripService tripService) {
        this.tripService = tripService;
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    @FXML
    public void initialize() {
        statusCombo.getItems().setAll("PLANNED", "ONGOING", "COMPLETED", "CANCELLED");
        statusCombo.setValue("PLANNED");
        hideError();
    }

    public void setAddMode() {
        editMode = false;
        editingTrip = null;
        formTitle.setText("Add Trip");
        hideError();
    }

    public void setEditMode(Trip t) {
        editMode = true;
        editingTrip = t;

        formTitle.setText("Edit Trip (ID: " + t.getId() + ")");

        userIdField.setText(t.getUserId() == null ? "" : String.valueOf(t.getUserId()));
        tripNameField.setText(nvl(t.getTripName()));
        originField.setText(nvl(t.getOrigin()));
        destinationField.setText(nvl(t.getDestination()));
        descriptionArea.setText(nvl(t.getDescription()));

        startDatePicker.setValue(t.getStartDate());
        endDatePicker.setValue(t.getEndDate());

        statusCombo.setValue((t.getStatus() == null || t.getStatus().isBlank()) ? "PLANNED" : t.getStatus());

        budgetField.setText(t.getBudgetAmount() == null ? "" : String.valueOf(t.getBudgetAmount()));
        currencyField.setText(nvl(t.getCurrency()));
        expensesField.setText(t.getTotalExpenses() == null ? "" : String.valueOf(t.getTotalExpenses()));
        xpField.setText(t.getTotalXpEarned() == null ? "" : String.valueOf(t.getTotalXpEarned()));

        notesArea.setText(nvl(t.getNotes()));
        coverImageUrlField.setText(nvl(t.getCoverImageUrl()));

        hideError();
    }

    @FXML
    public void save() {
        hideError();

        if (tripService == null) {
            showError("Internal error: TripService is not set (controller injection missing).");
            return;
        }

        try {
            Trip t = buildTripFromInputs();

            if (!editMode) {
                tripService.addTrip(t);
            } else {
                t.setId(editingTrip.getId());
                tripService.updateTrip(t);
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

    private Trip buildTripFromInputs() {
        Trip t = new Trip();

        // user id optional
        String uidTxt = userIdField.getText() == null ? "" : userIdField.getText().trim();
        if (!uidTxt.isEmpty()) t.setUserId(parseLong(uidTxt, "User ID must be a number."));
        else t.setUserId(1L);

        String name = tripNameField.getText() == null ? "" : tripNameField.getText().trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Trip name is required.");
        t.setTripName(name);

        t.setOrigin(emptyToNull(originField.getText()));
        t.setDestination(emptyToNull(destinationField.getText()));
        t.setDescription(emptyToNull(descriptionArea.getText()));

        LocalDate s = startDatePicker.getValue();
        LocalDate e = endDatePicker.getValue();
        if (s == null || e == null) throw new IllegalArgumentException("Start date and end date are required.");
        if (e.isBefore(s)) throw new IllegalArgumentException("End date must be after start date.");
        t.setStartDate(s);
        t.setEndDate(e);

        t.setStatus(statusCombo.getValue());

        String budTxt = budgetField.getText() == null ? "" : budgetField.getText().trim();
        if (!budTxt.isEmpty()) {
            double b = parseDouble(budTxt, "Budget must be a valid number.");
            if (b < 0) throw new IllegalArgumentException("Budget cannot be negative.");
            t.setBudgetAmount(b);
        } else {
            t.setBudgetAmount(null);
        }

        t.setCurrency(emptyToNull(currencyField.getText()));

        String expTxt = expensesField.getText() == null ? "" : expensesField.getText().trim();
        if (!expTxt.isEmpty()) {
            double ex = parseDouble(expTxt, "Expenses must be a valid number.");
            if (ex < 0) throw new IllegalArgumentException("Expenses cannot be negative.");
            t.setTotalExpenses(ex);
        } else {
            t.setTotalExpenses(0.0);
        }

        String xpTxt = xpField.getText() == null ? "" : xpField.getText().trim();
        if (!xpTxt.isEmpty()) {
            int xp = parseInt(xpTxt, "XP must be a valid integer.");
            if (xp < 0) throw new IllegalArgumentException("XP cannot be negative.");
            t.setTotalXpEarned(xp);
        } else {
            t.setTotalXpEarned(0);
        }

        t.setNotes(emptyToNull(notesArea.getText()));
        t.setCoverImageUrl(emptyToNull(coverImageUrlField.getText()));

        return t;
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

    private static long parseLong(String s, String msg) {
        try { return Long.parseLong(s.trim()); }
        catch (Exception e) { throw new IllegalArgumentException(msg); }
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