package com.travelxp.controllers;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import com.travelxp.models.Property;
import com.travelxp.services.PropertyService;
import com.travelxp.services.RecommendationService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

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
	@FXML private TextField latitudeField;
	@FXML private TextField longitudeField;
	@FXML private CheckBox isActiveCheck;



	/* filter/search controls */
	@FXML private TextField searchField;
	@FXML private TextField filterCityField;
	@FXML private TextField minPriceField;
	@FXML private TextField maxPriceField;
	@FXML private CheckBox activeOnlyCheck;
	@FXML private javafx.scene.control.ComboBox<String> sortCombo;

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
			Double latitude = parseOptionalDouble(latitudeField.getText(), "Latitude");
			Double longitude = parseOptionalDouble(longitudeField.getText(), "Longitude");

			Property property = new Property(ownerId, title, description, propertyType, address, city, country, bedrooms, bathrooms, maxGuests, price, images, isActive);
			property.setLatitude(latitude);
			property.setLongitude(longitude);

			loadProperties();
			clearForm();
			showAlert(Alert.AlertType.INFORMATION, "Success", "Property Created", "Property added successfully.");
		} catch (IllegalArgumentException e) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid Input", e.getMessage());
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
			Double latitude = parseOptionalDouble(latitudeField.getText(), "Latitude");
			Double longitude = parseOptionalDouble(longitudeField.getText(), "Longitude");

			Property updated = new Property(selected.getId(), ownerId, title, description, propertyType, address, city, country, bedrooms, bathrooms, maxGuests, price, images, isActive);
			updated.setLatitude(latitude);
			updated.setLongitude(longitude);
			propertyService.updateProperty(updated);
			loadProperties();
			showAlert(Alert.AlertType.INFORMATION, "Success", "Property Updated", "Property updated successfully.");
		} catch (IllegalArgumentException e) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid Input", e.getMessage());
		} catch (SQLException e) {
			showAlert(Alert.AlertType.ERROR, "Database Error", "Update Failed", e.getMessage());
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

	@FXML
	private void handleApplyFilters() {
		try {
			String keyword = searchField.getText();
			String city = filterCityField.getText();
			BigDecimal min = parseOptionalBigDecimal(minPriceField.getText(), "Min Price");
			BigDecimal max = parseOptionalBigDecimal(maxPriceField.getText(), "Max Price");
			Boolean active = activeOnlyCheck.isSelected() ? Boolean.TRUE : null;
			String order = sortCombo.getValue();
			boolean asc = true; // could add toggle later
			
			propertyData.setAll(propertyService.findProperties(keyword, city, min, max, active, order, asc));
			propertyTable.setItems(propertyData);
		} catch (SQLException e) {
			showAlert(Alert.AlertType.ERROR, "Database Error", "Filter failed", e.getMessage());
		} catch (IllegalArgumentException e) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid filter", e.getMessage());
		}
	}

	@FXML
	private void handleRecommend() {
		try {
			// example weights (could be user-configurable)
			double offerW = 0.25;
			double priceW = 0.25;
			double ratingW = 0.25;
			double distW = 0.25;
			// use map view center or some fixed point; here reuse previously selected property
			Property selected = propertyTable.getSelectionModel().getSelectedItem();
		double lat = selected.getLatitude();
		double lon = selected.getLongitude();
			List<Property> rec = new RecommendationService()
				.rankProperties(lat, lon, offerW, priceW, ratingW, distW);
			propertyData.setAll(rec);
			propertyTable.setItems(propertyData);
		} catch (SQLException e) {
			showAlert(Alert.AlertType.ERROR, "Recommendation Error", "Unable to compute recommendations", e.getMessage());
		}
	}

	@FXML
	private void handleShowMap() {
		Property selected = propertyTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(Alert.AlertType.WARNING, "No Selection", "Map Failed", "Please select a property first.");
			return;
		}
		if (selected.getLatitude() == null || selected.getLongitude() == null) {
			showAlert(Alert.AlertType.INFORMATION, "Coordinates Missing", "Cannot show map", "Property does not have latitude/longitude values.");
			return;
		}

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/property_map.fxml"));
			Parent root = loader.load();
			MapController mapCtrl = loader.getController();
			mapCtrl.initLocation(selected.getLatitude(), selected.getLongitude(), selected.getTitle());

			Stage stage = new Stage();
			stage.setTitle("Location - " + selected.getTitle());
			stage.setScene(new Scene(root, 640, 480));
			stage.show();
		} catch (IOException e) {
			showAlert(Alert.AlertType.ERROR, "Map Error", "Unable to open map", e.getMessage());
		}
	}

	private void loadProperties() {
		try {
			// reuse search method with no criteria
			propertyData.setAll(propertyService.findProperties(
					null, null, null, null, null, null, true));
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
		latitudeField.setText(property.getLatitude() != null ? property.getLatitude().toString() : "");
		longitudeField.setText(property.getLongitude() != null ? property.getLongitude().toString() : "");
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
		latitudeField.clear();
		longitudeField.clear();
		imagesField.clear();
		isActiveCheck.setSelected(true);
		propertyTable.getSelectionModel().clearSelection();
	}

	private Long parseRequiredLong(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " is required.");
		}
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(fieldName + " must be a valid number.");
		}
	}

	private Integer parseRequiredInt(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " is required.");
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(fieldName + " must be a valid number.");
		}
	}

	private Double parseOptionalDouble(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(fieldName + " must be a valid decimal number.");
		}
	}

	private BigDecimal parseOptionalBigDecimal(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return new BigDecimal(value.trim());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(fieldName + " must be a valid decimal number.");
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
