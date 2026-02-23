package com.travelxp.controllers;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

import com.travelxp.models.Booking;
import com.travelxp.services.BookingService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class BookingController {

	@FXML private TableView<Booking> bookingTable;
	@FXML private TableColumn<Booking, Integer> bookingIdCol;
	@FXML private TableColumn<Booking, Integer> userIdCol;
	@FXML private TableColumn<Booking, Integer> tripIdCol;
	@FXML private TableColumn<Booking, Integer> serviceIdCol;
	@FXML private TableColumn<Booking, Date> bookingDateCol;
	@FXML private TableColumn<Booking, String> bookingStatusCol;

	@FXML private TextField userIdField;
	@FXML private TextField tripIdField;
	@FXML private TextField serviceIdField;
	@FXML private DatePicker bookingDatePicker;
	@FXML private ComboBox<String> bookingStatusCombo;

	private final BookingService bookingService = new BookingService();
	private final ObservableList<Booking> bookingData = FXCollections.observableArrayList();

	@FXML
	public void initialize() {
		bookingIdCol.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
		userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
		tripIdCol.setCellValueFactory(new PropertyValueFactory<>("tripId"));
		serviceIdCol.setCellValueFactory(new PropertyValueFactory<>("serviceId"));
		bookingDateCol.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
		bookingStatusCol.setCellValueFactory(new PropertyValueFactory<>("bookingStatus"));

		bookingStatusCombo.setItems(FXCollections.observableArrayList("PENDING", "CONFIRMED", "CANCELLED", "COMPLETED"));
		bookingStatusCombo.getSelectionModel().select("PENDING");

		bookingTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
			if (selected != null) {
				populateForm(selected);
			}
		});

		loadBookings();
	}

	@FXML
	private void handleAddBooking() {
		try {
			int userId = parseRequiredInt(userIdField.getText(), "User ID");
			int tripId = parseRequiredInt(tripIdField.getText(), "Trip ID");
			int serviceId = parseRequiredInt(serviceIdField.getText(), "Service ID");
			LocalDate selectedDate = bookingDatePicker.getValue();
			if (selectedDate == null) {
				throw new IllegalArgumentException("Booking date is required.");
			}

			String status = bookingStatusCombo.getValue();
			if (status == null || status.isBlank()) {
				throw new IllegalArgumentException("Booking status is required.");
			}

			Booking booking = new Booking(userId, tripId, serviceId, Date.valueOf(selectedDate), status);
			bookingService.addBooking(booking);

			loadBookings();
			clearForm();
			showAlert(Alert.AlertType.INFORMATION, "Success", "Booking Created", "Booking added successfully.");
		} catch (IllegalArgumentException e) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid Input", e.getMessage());
		} catch (SQLException e) {
			showAlert(Alert.AlertType.ERROR, "Database Error", "Create Failed", e.getMessage());
		}
	}

	@FXML
	private void handleUpdateBookingStatus() {
		Booking selected = bookingTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(Alert.AlertType.WARNING, "No Selection", "Update Failed", "Please select a booking first.");
			return;
		}

		String newStatus = bookingStatusCombo.getValue();
		if (newStatus == null || newStatus.isBlank()) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid Status", "Please select a booking status.");
			return;
		}

		try {
			bookingService.updateBookingStatus(selected.getBookingId(), newStatus);
			loadBookings();
			showAlert(Alert.AlertType.INFORMATION, "Success", "Booking Updated", "Booking status updated successfully.");
		} catch (SQLException e) {
			showAlert(Alert.AlertType.ERROR, "Database Error", "Update Failed", e.getMessage());
		}
	}

	@FXML
	private void handleDeleteBooking() {
		Booking selected = bookingTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(Alert.AlertType.WARNING, "No Selection", "Delete Failed", "Please select a booking first.");
			return;
		}

		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
		confirm.setTitle("Delete Booking");
		confirm.setHeaderText("Delete booking #" + selected.getBookingId() + "?");
		confirm.setContentText("This action cannot be undone.");

		confirm.showAndWait().ifPresent(response -> {
			if (response == ButtonType.OK) {
				try {
					bookingService.deleteBooking(selected.getBookingId());
					loadBookings();
					clearForm();
					showAlert(Alert.AlertType.INFORMATION, "Success", "Booking Deleted", "Booking deleted successfully.");
				} catch (SQLException e) {
					showAlert(Alert.AlertType.ERROR, "Database Error", "Delete Failed", e.getMessage());
				}
			}
		});
	}

	@FXML
	private void handleClearForm() {
		clearForm();
	}

	@FXML
	private void handleRefreshBookings() {
		loadBookings();
	}

	private void loadBookings() {
		try {
			bookingData.setAll(bookingService.getAllBookings());
			bookingTable.setItems(bookingData);
		} catch (SQLException e) {
			showAlert(Alert.AlertType.ERROR, "Database Error", "Load Failed", e.getMessage());
		}
	}

	private void populateForm(Booking booking) {
		userIdField.setText(String.valueOf(booking.getUserId()));
		tripIdField.setText(String.valueOf(booking.getTripId()));
		serviceIdField.setText(String.valueOf(booking.getServiceId()));
		bookingDatePicker.setValue(booking.getBookingDate() != null ? booking.getBookingDate().toLocalDate() : null);
		bookingStatusCombo.setValue(booking.getBookingStatus());
	}

	private void clearForm() {
		userIdField.clear();
		tripIdField.clear();
		serviceIdField.clear();
		bookingDatePicker.setValue(null);
		bookingStatusCombo.getSelectionModel().select("PENDING");
		bookingTable.getSelectionModel().clearSelection();
	}

	private int parseRequiredInt(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " is required.");
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(fieldName + " must be a valid number.");
		}
	}

	private void showAlert(Alert.AlertType type, String title, String header, String content) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}
}
