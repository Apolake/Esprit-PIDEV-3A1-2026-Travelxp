package com.travelxp.controllers;

import com.travelxp.Main;
import com.travelxp.models.Booking;
import com.travelxp.services.BookingService;
import com.travelxp.utils.ThemeManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class BookingController {

	@FXML private TableView<Booking> bookingTable;
	@FXML private TableColumn<Booking, Integer> bookingIdCol;
	@FXML private TableColumn<Booking, Integer> userIdCol;
	@FXML private TableColumn<Booking, Date> bookingDateCol;
	@FXML private TableColumn<Booking, Integer> durationCol;
	@FXML private TableColumn<Booking, String> bookingStatusCol;
	@FXML private TableColumn<Booking, Void> actionsCol;

	@FXML private TextField userIdField;
	@FXML private TextField tripIdField;
	@FXML private TextField serviceIdField;
	@FXML private DatePicker bookingDatePicker;
	@FXML private ComboBox<String> bookingStatusCombo;
	
	@FXML private GridPane adminForm;

	private final BookingService bookingService = new BookingService();
	private final ObservableList<Booking> bookingData = FXCollections.observableArrayList();

	@FXML
	public void initialize() {
		bookingIdCol.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
		userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
		bookingDateCol.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
		durationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));
		bookingStatusCol.setCellValueFactory(new PropertyValueFactory<>("bookingStatus"));

		bookingStatusCombo.setItems(FXCollections.observableArrayList("PENDING", "CONFIRMED", "CANCELLED", "COMPLETED"));
		bookingStatusCombo.getSelectionModel().select("PENDING");

		bookingTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
			if (selected != null) {
				populateForm(selected);
			}
		});

		boolean isAdmin = Main.getSession().getUser().getRole().equals("ADMIN");
		adminForm.setVisible(isAdmin);
		adminForm.setManaged(isAdmin);
		
		addActionsToTable();
		loadBookings();
	}

	private void addActionsToTable() {
		Callback<TableColumn<Booking, Void>, TableCell<Booking, Void>> cellFactory = param -> new TableCell<>() {
			private final Button cancelBtn = new Button("Cancel");
			private final Button editBtn = new Button("Edit Duration");

			{
				cancelBtn.getStyleClass().add("danger-button");
				cancelBtn.setOnAction(event -> handleCancelBooking(getTableView().getItems().get(getIndex())));
				
				editBtn.getStyleClass().add("secondary-button");
				editBtn.setOnAction(event -> handleEditDuration(getTableView().getItems().get(getIndex())));
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setGraphic(null);
				} else {
					Booking b = getTableView().getItems().get(getIndex());
					if (b.getBookingStatus().equals("CANCELLED") || b.getBookingStatus().equals("COMPLETED")) {
						setGraphic(null);
					} else {
						HBox box = new HBox(5, editBtn, cancelBtn);
						setGraphic(box);
					}
				}
			}
		};
		actionsCol.setCellFactory(cellFactory);
	}

	private void handleCancelBooking(Booking booking) {
		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
		confirm.setTitle("Cancel Booking");
		confirm.setHeaderText("Are you sure you want to cancel this booking?");
		confirm.showAndWait().ifPresent(btn -> {
			if (btn == ButtonType.OK) {
				try {
					bookingService.updateBookingStatus(booking.getBookingId(), "CANCELLED");
					loadBookings();
				} catch (SQLException e) {
					showAlert(Alert.AlertType.ERROR, "Error", "Cancellation Failed", e.getMessage());
				}
			}
		});
	}

	private void handleEditDuration(Booking booking) {
		TextInputDialog dialog = new TextInputDialog(String.valueOf(booking.getDuration()));
		dialog.setTitle("Edit Duration");
		dialog.setHeaderText("Update the duration (in days) for booking #" + booking.getBookingId());
		dialog.setContentText("New Duration:");
		
		dialog.showAndWait().ifPresent(durationStr -> {
			try {
				int duration = Integer.parseInt(durationStr);
				bookingService.updateBookingDuration(booking.getBookingId(), duration);
				loadBookings();
			} catch (NumberFormatException | SQLException e) {
				showAlert(Alert.AlertType.ERROR, "Error", "Update Failed", "Invalid duration or database error.");
			}
		});
	}

	@FXML
	private void handleAddBooking() {
		try {
			int userId = parseRequiredInt(userIdField.getText(), "User ID");
			int tripId = tripIdField.getText().isEmpty() ? 0 : Integer.parseInt(tripIdField.getText());
			int serviceId = serviceIdField.getText().isEmpty() ? 0 : Integer.parseInt(serviceIdField.getText());
			
			LocalDate selectedDate = bookingDatePicker.getValue();
			if (selectedDate == null) {
				throw new IllegalArgumentException("Booking date is required.");
			}

			String status = bookingStatusCombo.getValue();
			
			Booking booking = new Booking(userId, tripId, serviceId, Date.valueOf(selectedDate), status, 1);
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

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            String fxml = "/com/travelxp/views/dashboard.fxml";
            if (com.travelxp.Main.getSession().getUser().getRole().equals("ADMIN")) {
                fxml = "/com/travelxp/views/admin_dashboard.fxml";
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            ThemeManager.applyTheme(stage.getScene());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Navigation Failed", "Failed to load dashboard: " + e.getMessage());
        }
    }

	private void loadBookings() {
		try {
			boolean isAdmin = Main.getSession().getUser().getRole().equals("ADMIN");
			if (isAdmin) {
				bookingData.setAll(bookingService.getAllBookings());
			} else {
				bookingData.setAll(bookingService.getBookingsByUserId(Main.getSession().getUser().getId()));
			}
			bookingTable.setItems(bookingData);
		} catch (SQLException e) {
			showAlert(Alert.AlertType.ERROR, "Database Error", "Load Failed", e.getMessage());
		}
	}

	private void populateForm(Booking booking) {
		userIdField.setText(String.valueOf(booking.getUserId()));
		// Optional fields
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
