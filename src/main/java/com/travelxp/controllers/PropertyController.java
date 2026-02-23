package com.travelxp.controllers;

import com.travelxp.Main;
import com.travelxp.models.Booking;
import com.travelxp.models.Offer;
import com.travelxp.models.Property;
import com.travelxp.services.BookingService;
import com.travelxp.services.OfferService;
import com.travelxp.services.PropertyService;
import com.travelxp.utils.ImageUtil;
import com.travelxp.utils.ThemeManager;
import com.travelxp.services.UserService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
	@FXML private TableColumn<Property, Void> actionsCol;

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
	
	@FXML private GridPane adminForm;

	private final PropertyService propertyService = new PropertyService();
	private final BookingService bookingService = new BookingService();
	private final OfferService offerService = new OfferService();
	private final UserService userService = new UserService();
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

		boolean isAdmin = Main.getSession().getUser().getRole().equals("ADMIN");
		adminForm.setVisible(isAdmin);
		adminForm.setManaged(isAdmin);
		
		addActionsToTable();
		loadProperties();
	}

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Property Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(ownerIdField.getScene().getWindow());
        if (selectedFile != null) {
            String relativePath = ImageUtil.saveImage(selectedFile);
            if (relativePath != null) {
                imagesField.setText(relativePath);
            }
        }
    }

	private void addActionsToTable() {
		Callback<TableColumn<Property, Void>, TableCell<Property, Void>> cellFactory = param -> new TableCell<>() {
			private final Button bookBtn = new Button("Book");

			{
				bookBtn.getStyleClass().add("primary-button");
				bookBtn.setOnAction(event -> handleBook(getTableView().getItems().get(getIndex())));
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setGraphic(null);
				} else {
					setGraphic(bookBtn);
				}
			}
		};
		actionsCol.setCellFactory(cellFactory);
	}

	private void handleBook(Property property) {
		Dialog<Booking> dialog = new Dialog<>();
		dialog.setTitle("Book: " + property.getTitle());
		
		double basePrice = property.getPricePerNight().doubleValue();
		double discount = 0;
		String offerTitle = "";
		
		try {
			List<Offer> activeOffers = offerService.getActiveOffersByPropertyId(property.getId());
			if (!activeOffers.isEmpty()) {
				Offer bestOffer = activeOffers.get(0);
				discount = bestOffer.getDiscountPercentage().doubleValue();
				offerTitle = bestOffer.getTitle();
			}
		} catch (SQLException e) {
			// Ignore
		}

		String headerText = "Base Price: $" + basePrice + " / night";
		if (discount > 0) {
			headerText += "\nSpecial Offer! " + offerTitle + ": " + discount + "% OFF!";
		}
		dialog.setHeaderText(headerText);

		ButtonType bookButtonType = new ButtonType("Confirm & Pay", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(bookButtonType, ButtonType.CANCEL);

		VBox content = new VBox(10);
		content.setPadding(new Insets(20));
		
		DatePicker datePicker = new DatePicker(LocalDate.now());
		TextField durationField = new TextField("1");
		Label totalLabel = new Label("Total Price: $" + (basePrice * (1 - discount/100.0)));
		
		durationField.textProperty().addListener((obs, oldV, newV) -> {
			try {
				int days = Integer.parseInt(newV);
				double total = basePrice * days * (1 - discount/100.0);
				totalLabel.setText("Total Price: $" + String.format("%.2f", total));
			} catch (NumberFormatException e) {
				totalLabel.setText("Invalid duration");
			}
		});

		content.getChildren().addAll(
			new Label("Booking Date:"), datePicker,
			new Label("Duration (Days):"), durationField,
			totalLabel
		);

		dialog.getDialogPane().setContent(content);
		dialog.setResultConverter(btn -> {
			if (btn == bookButtonType) {
				try {
					int duration = Integer.parseInt(durationField.getText());
					double finalPrice = basePrice * duration * (1 - discount/100.0);
					
					if (Main.getSession().getUser().getBalance() < finalPrice) {
						showAlert(Alert.AlertType.ERROR, "Insufficient Funds", "Low Balance", "Your balance is too low for this booking.");
						return null;
					}

					return new Booking(
						Main.getSession().getUser().getId(),
						0, 
						0, 
						Date.valueOf(datePicker.getValue()),
						"CONFIRMED", // Auto confirm if paid
						duration,
						finalPrice
					);
				} catch (NumberFormatException e) {
					return null;
				}
			}
			return null;
		});

		dialog.showAndWait().ifPresent(booking -> {
			try {
				bookingService.addBooking(booking);
				// Deduct balance
				userService.updateBalance(booking.getUserId(), -booking.getTotalPrice());
				// Update session
				Main.getSession().setUser(userService.getUserById(booking.getUserId()));
				
				showAlert(Alert.AlertType.INFORMATION, "Success", "Booked & Paid", "Your booking has been confirmed and paid.");
			} catch (SQLException e) {
				showAlert(Alert.AlertType.ERROR, "Error", "Booking Failed", e.getMessage());
			}
		});
	}

	@FXML
	private void handleAddProperty() {
		try {
			int ownerId = parseRequiredInt(ownerIdField.getText(), "Owner ID");
			// Validate owner exists
			if (userService.getUserById(ownerId) == null) {
				throw new IllegalArgumentException("Owner with ID " + ownerId + " does not exist.");
			}

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

			Property property = new Property((long)ownerId, title, description, propertyType, address, city, country, bedrooms, bathrooms, maxGuests, price, images, isActive);
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
			int ownerId = parseRequiredInt(ownerIdField.getText(), "Owner ID");
			// Validate owner exists
			if (userService.getUserById(ownerId) == null) {
				throw new IllegalArgumentException("Owner with ID " + ownerId + " does not exist.");
			}

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

			Property updated = new Property(selected.getId(), (long)ownerId, title, description, propertyType, address, city, country, bedrooms, bathrooms, maxGuests, price, images, isActive);
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

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            String fxml = "/com/travelxp/views/dashboard.fxml";
            if (Main.getSession().getUser().getRole().equals("ADMIN")) {
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
