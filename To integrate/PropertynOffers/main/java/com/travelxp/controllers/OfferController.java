package com.travelxp.controllers;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.travelxp.models.Offer;
import com.travelxp.services.OfferService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class OfferController {

	@FXML private TableView<Offer> offerTable;
	@FXML private TableColumn<Offer, Long> idCol;
	@FXML private TableColumn<Offer, Long> propertyIdCol;
	@FXML private TableColumn<Offer, String> titleCol;
	@FXML private TableColumn<Offer, String> descriptionCol;
	@FXML private TableColumn<Offer, BigDecimal> discountCol;
	@FXML private TableColumn<Offer, LocalDate> startDateCol;
	@FXML private TableColumn<Offer, LocalDate> endDateCol;
	@FXML private TableColumn<Offer, Boolean> isActiveCol;
	@FXML private TableColumn<Offer, LocalDateTime> createdAtCol;

	@FXML private TextField propertyIdField;
	@FXML private TextField titleField;
	@FXML private TextField descriptionField;
	@FXML private TextField discountField;
	@FXML private DatePicker startDatePicker;
	@FXML private DatePicker endDatePicker;
	@FXML private CheckBox isActiveCheck;

	private final OfferService offerService = new OfferService();
	private final ObservableList<Offer> offerData = FXCollections.observableArrayList();

	@FXML
	public void initialize() {
		idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
		propertyIdCol.setCellValueFactory(new PropertyValueFactory<>("propertyId"));
		titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
		descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
		discountCol.setCellValueFactory(new PropertyValueFactory<>("discountPercentage"));
		startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
		endDateCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
		isActiveCol.setCellValueFactory(new PropertyValueFactory<>("isActive"));
		createdAtCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

		offerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
			if (selected != null) {
				populateForm(selected);
			}
		});

		loadOffers();
	}

	@FXML
	private void handleAddOffer() {
		try {
			Long propertyId = parseRequiredLong(propertyIdField.getText(), "Property ID");
			String title = parseRequiredString(titleField.getText(), "Title");
			String description = descriptionField.getText();
			BigDecimal discount = parseRequiredBigDecimal(discountField.getText(), "Discount Percentage");
			LocalDate startDate = startDatePicker.getValue();
			LocalDate endDate = endDatePicker.getValue();
			Boolean isActive = isActiveCheck.isSelected();
			LocalDateTime createdAt = LocalDateTime.now();

			if (startDate == null) throw new IllegalArgumentException("Start date is required.");
			if (endDate == null) throw new IllegalArgumentException("End date is required.");

			Offer offer = new Offer(propertyId, title, description, discount, startDate, endDate, isActive, createdAt);
			offerService.addOffer(offer);

			loadOffers();
			clearForm();
			showAlert(Alert.AlertType.INFORMATION, "Success", "Offer Created", "Offer added successfully.");
		} catch (IllegalArgumentException e) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid Input", e.getMessage());
		} catch (SQLException e) {
			showAlert(Alert.AlertType.ERROR, "Database Error", "Create Failed", e.getMessage());
		}
	}

	@FXML
	private void handleUpdateOffer() {
		Offer selected = offerTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(Alert.AlertType.WARNING, "No Selection", "Update Failed", "Please select an offer first.");
			return;
		}
		try {
			Long propertyId = parseRequiredLong(propertyIdField.getText(), "Property ID");
			String title = parseRequiredString(titleField.getText(), "Title");
			String description = descriptionField.getText();
			BigDecimal discount = parseRequiredBigDecimal(discountField.getText(), "Discount Percentage");
			LocalDate startDate = startDatePicker.getValue();
			LocalDate endDate = endDatePicker.getValue();
			Boolean isActive = isActiveCheck.isSelected();
			LocalDateTime createdAt = selected.getCreatedAt();

			if (startDate == null) throw new IllegalArgumentException("Start date is required.");
			if (endDate == null) throw new IllegalArgumentException("End date is required.");

			Offer updated = new Offer(selected.getId(), propertyId, title, description, discount, startDate, endDate, isActive, createdAt);
			offerService.updateOffer(updated);
			loadOffers();
			showAlert(Alert.AlertType.INFORMATION, "Success", "Offer Updated", "Offer updated successfully.");
		} catch (IllegalArgumentException e) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid Input", e.getMessage());
		} catch (SQLException e) {
			showAlert(Alert.AlertType.ERROR, "Database Error", "Update Failed", e.getMessage());
		}
	}

	@FXML
	private void handleDeleteOffer() {
		Offer selected = offerTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(Alert.AlertType.WARNING, "No Selection", "Delete Failed", "Please select an offer first.");
			return;
		}

		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
		confirm.setTitle("Delete Offer");
		confirm.setHeaderText("Delete offer #" + selected.getId() + "?");
		confirm.setContentText("This action cannot be undone.");

		confirm.showAndWait().ifPresent(response -> {
			if (response == ButtonType.OK) {
				try {
					offerService.deleteOffer(selected.getId());
					loadOffers();
					clearForm();
					showAlert(Alert.AlertType.INFORMATION, "Success", "Offer Deleted", "Offer deleted successfully.");
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
	private void handleRefreshOffers() {
		loadOffers();
	}

	private void loadOffers() {
		try {
			offerData.setAll(offerService.getAllOffers());
			offerTable.setItems(offerData);
		} catch (SQLException e) {
			showAlert(Alert.AlertType.ERROR, "Database Error", "Load Failed", e.getMessage());
		}
	}

	private void populateForm(Offer offer) {
		propertyIdField.setText(String.valueOf(offer.getPropertyId()));
		titleField.setText(offer.getTitle());
		descriptionField.setText(offer.getDescription());
		discountField.setText(offer.getDiscountPercentage() != null ? offer.getDiscountPercentage().toString() : "");
		startDatePicker.setValue(offer.getStartDate());
		endDatePicker.setValue(offer.getEndDate());
		isActiveCheck.setSelected(Boolean.TRUE.equals(offer.getIsActive()));
	}

	private void clearForm() {
		propertyIdField.clear();
		titleField.clear();
		descriptionField.clear();
		discountField.clear();
		startDatePicker.setValue(null);
		endDatePicker.setValue(null);
		isActiveCheck.setSelected(true);
		offerTable.getSelectionModel().clearSelection();
	}

	private Long parseRequiredLong(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " is required.");
		}
		try {
			return Long.parseLong(value.trim());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(fieldName + " must be a valid number.");
		}
	}

	private BigDecimal parseRequiredBigDecimal(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " is required.");
		}
		try {
			return new BigDecimal(value.trim());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(fieldName + " must be a valid decimal number.");
		}
	}

	private String parseRequiredString(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " is required.");
		}
		return value.trim();
	}

	private void showAlert(Alert.AlertType type, String title, String header, String content) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}
}
