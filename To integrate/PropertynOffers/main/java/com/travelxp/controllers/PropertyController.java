package com.travelxp.controllers;

import java.math.BigDecimal;
import java.sql.SQLException;

import com.travelxp.models.Property;
import com.travelxp.services.PropertyService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class PropertyController {

	@FXML private TableView<Property> propertyTable;
	@FXML private TableColumn<Property, Long> idCol;
	@FXML private TableColumn<Property, Long> ownerIdCol;
	@FXML private TableColumn<Property, String> titleCol;
	@FXML private TableColumn<Property, String> descriptionCol;
	@FXML private TableColumn<Property, String> propertyTypeCol;
	@FXML private TableColumn<Property, String> addressCol;
	@FXML private TableColumn<Property, String> cityCol;
	@FXML private TableColumn<Property, String> countryCol;
	@FXML private TableColumn<Property, Integer> bedroomsCol;
	@FXML private TableColumn<Property, Integer> bathroomsCol;
	@FXML private TableColumn<Property, Integer> maxGuestsCol;
	@FXML private TableColumn<Property, BigDecimal> priceCol;
	@FXML private TableColumn<Property, String> imagesCol;
	@FXML private TableColumn<Property, Boolean> isActiveCol;

	@FXML private TextField ownerIdField;
	@FXML private TextField titleField;
	@FXML private TextField descriptionField;
	@FXML private TextField propertyTypeField;
	@FXML private TextField addressField;
	@FXML private TextField cityField;
	@FXML private TextField countryField;
	@FXML private TextField bedroomsField;
	@FXML private TextField bathroomsField;
	@FXML private TextField maxGuestsField;
	@FXML private TextField priceField;
	@FXML private TextField imagesField;
	@FXML private CheckBox isActiveCheck;

	private final PropertyService propertyService = new PropertyService();
	private final ObservableList<Property> propertyData = FXCollections.observableArrayList();

	@FXML
	public void initialize() {
		idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
		ownerIdCol.setCellValueFactory(new PropertyValueFactory<>("ownerId"));
		titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
		descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
		propertyTypeCol.setCellValueFactory(new PropertyValueFactory<>("propertyType"));
		addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
		cityCol.setCellValueFactory(new PropertyValueFactory<>("city"));
		countryCol.setCellValueFactory(new PropertyValueFactory<>("country"));
		bedroomsCol.setCellValueFactory(new PropertyValueFactory<>("bedrooms"));
		bathroomsCol.setCellValueFactory(new PropertyValueFactory<>("bathrooms"));
		maxGuestsCol.setCellValueFactory(new PropertyValueFactory<>("maxGuests"));
		priceCol.setCellValueFactory(new PropertyValueFactory<>("pricePerNight"));
		imagesCol.setCellValueFactory(new PropertyValueFactory<>("images"));
		isActiveCol.setCellValueFactory(new PropertyValueFactory<>("isActive"));

		propertyTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
			if (selected != null) {
				populateForm(selected);
			}
		});

		loadProperties();
	}

	@FXML
	private void handleAddProperty() {
		try {
			Long ownerId = parseRequiredLong(ownerIdField.getText(), "Owner ID");
			String title = parseRequiredString(titleField.getText(), "Title");
			String description = descriptionField.getText();
			String propertyType = parseRequiredString(propertyTypeField.getText(), "Property Type");
			String address = parseRequiredString(addressField.getText(), "Address");
			String city = parseRequiredString(cityField.getText(), "City");
			String country = parseRequiredString(countryField.getText(), "Country");
			Integer bedrooms = parseRequiredInt(bedroomsField.getText(), "Bedrooms");
			Integer bathrooms = parseRequiredInt(bathroomsField.getText(), "Bathrooms");
			Integer maxGuests = parseRequiredInt(maxGuestsField.getText(), "Max Guests");
			BigDecimal price = parseRequiredBigDecimal(priceField.getText(), "Price Per Night");
			String images = imagesField.getText();
			Boolean isActive = isActiveCheck.isSelected();

			Property property = new Property(ownerId, title, description, propertyType, address, city, country, bedrooms, bathrooms, maxGuests, price, images, isActive);
			propertyService.addProperty(property);

			loadProperties();
			clearForm();
			showAlert(Alert.AlertType.INFORMATION, "Success", "Property Created", "Property added successfully.");
		} catch (IllegalArgumentException e) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid Input", e.getMessage());
		} catch (SQLException e) {
			showAlert(Alert.AlertType.ERROR, "Database Error", "Create Failed", e.getMessage());
		}
	}

	@FXML
	private void handleUpdateProperty() {
		Property selected = propertyTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(Alert.AlertType.WARNING, "No Selection", "Update Failed", "Please select a property first.");
			return;
		}
		try {
			Long ownerId = parseRequiredLong(ownerIdField.getText(), "Owner ID");
			String title = parseRequiredString(titleField.getText(), "Title");
			String description = descriptionField.getText();
			String propertyType = parseRequiredString(propertyTypeField.getText(), "Property Type");
			String address = parseRequiredString(addressField.getText(), "Address");
			String city = parseRequiredString(cityField.getText(), "City");
			String country = parseRequiredString(countryField.getText(), "Country");
			Integer bedrooms = parseRequiredInt(bedroomsField.getText(), "Bedrooms");
			Integer bathrooms = parseRequiredInt(bathroomsField.getText(), "Bathrooms");
			Integer maxGuests = parseRequiredInt(maxGuestsField.getText(), "Max Guests");
			BigDecimal price = parseRequiredBigDecimal(priceField.getText(), "Price Per Night");
			String images = imagesField.getText();
			Boolean isActive = isActiveCheck.isSelected();

			Property updated = new Property(selected.getId(), ownerId, title, description, propertyType, address, city, country, bedrooms, bathrooms, maxGuests, price, images, isActive);
			propertyService.updateProperty(updated);
			loadProperties();
			showAlert(Alert.AlertType.INFORMATION, "Success", "Property Updated", "Property updated successfully.");
		} catch (IllegalArgumentException e) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid Input", e.getMessage());
		} catch (SQLException e) {
			showAlert(Alert.AlertType.ERROR, "Database Error", "Update Failed", e.getMessage());
		}
	}

	@FXML
	private void handleDeleteProperty() {
		Property selected = propertyTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(Alert.AlertType.WARNING, "No Selection", "Delete Failed", "Please select a property first.");
			return;
		}

		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
		confirm.setTitle("Delete Property");
		confirm.setHeaderText("Delete property #" + selected.getId() + "?");
		confirm.setContentText("This action cannot be undone.");

		confirm.showAndWait().ifPresent(response -> {
			if (response == ButtonType.OK) {
				try {
					propertyService.deleteProperty(selected.getId());
					loadProperties();
					clearForm();
					showAlert(Alert.AlertType.INFORMATION, "Success", "Property Deleted", "Property deleted successfully.");
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
	private void handleRefreshProperties() {
		loadProperties();
	}

	private void loadProperties() {
		try {
			propertyData.setAll(propertyService.getAllProperties());
			propertyTable.setItems(propertyData);
		} catch (SQLException e) {
			showAlert(Alert.AlertType.ERROR, "Database Error", "Load Failed", e.getMessage());
		}
	}

	private void populateForm(Property property) {
		ownerIdField.setText(String.valueOf(property.getOwnerId()));
		titleField.setText(property.getTitle());
		descriptionField.setText(property.getDescription());
		propertyTypeField.setText(property.getPropertyType());
		addressField.setText(property.getAddress());
		cityField.setText(property.getCity());
		countryField.setText(property.getCountry());
		bedroomsField.setText(property.getBedrooms() != null ? property.getBedrooms().toString() : "");
		bathroomsField.setText(property.getBathrooms() != null ? property.getBathrooms().toString() : "");
		maxGuestsField.setText(property.getMaxGuests() != null ? property.getMaxGuests().toString() : "");
		priceField.setText(property.getPricePerNight() != null ? property.getPricePerNight().toString() : "");
		imagesField.setText(property.getImages());
		isActiveCheck.setSelected(Boolean.TRUE.equals(property.getIsActive()));
	}

	private void clearForm() {
		ownerIdField.clear();
		titleField.clear();
		descriptionField.clear();
		propertyTypeField.clear();
		addressField.clear();
		cityField.clear();
		countryField.clear();
		bedroomsField.clear();
		bathroomsField.clear();
		maxGuestsField.clear();
		priceField.clear();
		imagesField.clear();
		isActiveCheck.setSelected(true);
		propertyTable.getSelectionModel().clearSelection();
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

	private Integer parseRequiredInt(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " is required.");
		}
		try {
			return Integer.parseInt(value.trim());
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
