package com.travelxp.controllers;

import com.travelxp.Main;
import com.travelxp.models.Booking;
import com.travelxp.models.Offer;
import com.travelxp.models.Property;
import com.travelxp.models.Service;
import com.travelxp.services.BookingService;
import com.travelxp.services.OfferService;
import com.travelxp.services.PropertyService;
import com.travelxp.services.ServiceService;
import com.travelxp.services.TripService;
import com.travelxp.utils.ImageUtil;
import com.travelxp.utils.ThemeManager;
import com.travelxp.services.UserService;

import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

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
    @FXML private ScrollPane userScrollPane;
    @FXML private FlowPane userCardsContainer;
    @FXML private Pane animatedBg;

	private final PropertyService propertyService = new PropertyService();
	private final BookingService bookingService = new BookingService();
	private final OfferService offerService = new OfferService();
	private final ServiceService serviceService = new ServiceService();
	private final UserService userService = new UserService();
    private final TripService tripService = new TripService();
	private final ObservableList<Property> propertyData = FXCollections.observableArrayList();
    private final Random random = new Random();

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
		if (adminForm != null) {
            adminForm.setVisible(isAdmin);
            adminForm.setManaged(isAdmin);
        }
        if (propertyTable != null) {
            propertyTable.setVisible(isAdmin);
            propertyTable.setManaged(isAdmin);
        }
        if (userScrollPane != null) {
            userScrollPane.setVisible(!isAdmin);
            userScrollPane.setManaged(!isAdmin);
        }
		
		addActionsToTable();
		loadProperties();
        Platform.runLater(this::startBackgroundAnimation);
	}

    private void startBackgroundAnimation() {
        if (animatedBg == null) return;
        for (int i = 0; i < 10; i++) {
            Circle circle = createCircle();
            animatedBg.getChildren().add(circle);
            animateCircle(circle);
        }
    }

    private Circle createCircle() {
        double radius = 30 + random.nextDouble() * 120;
        Circle circle = new Circle(radius);
        circle.setCenterX(random.nextDouble() * 1200);
        circle.setCenterY(random.nextDouble() * 900);
        double opacity = 0.03 + random.nextDouble() * 0.05;
        boolean isDark = ThemeManager.isDark();
        String color = isDark ? "#D4AF37" : "#002b5c";
        circle.setFill(Color.web(color, opacity));
        circle.setStroke(Color.web(color, opacity * 1.5));
        circle.setStrokeWidth(1.5);
        circle.setEffect(new javafx.scene.effect.BoxBlur(10, 10, 2));
        return circle;
    }

    private void animateCircle(Circle circle) {
        double duration = 6 + random.nextDouble() * 6;
        TranslateTransition tt = new TranslateTransition(Duration.seconds(duration), circle);
        tt.setByX(random.nextDouble() * 500 - 250);
        tt.setByY(random.nextDouble() * 500 - 250);
        tt.setAutoReverse(true);
        tt.setCycleCount(Animation.INDEFINITE);
        tt.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
        tt.play();
    }

    @FXML
    private void handleTasks(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/tasks.fxml");
    }

    @FXML
    private void handleBrowseProperties(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/property-view.fxml");
    }

    @FXML
    private void handleMyBookings(ActionEvent event) {
        String fxml = "/com/travelxp/views/booking-view.fxml";
        if (Main.getSession().getUser().getRole().equals("ADMIN")) {
            fxml = "/com/travelxp/views/admin-booking-view.fxml";
        }
        changeScene(event, fxml);
    }

    @FXML
    private void handleEditProfile(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/edit_profile.fxml");
    }

    @FXML
    private void handleChangePassword(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/change_password.fxml");
    }

    @FXML
    private void handleFeedback(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/feedback-view.fxml");
    }

    @FXML
    private void handleManageProperties(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/admin-property-view.fxml");
    }

    @FXML
    private void handleManageOffers(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/offer-view.fxml");
    }

    @FXML
    private void handleManageBookings(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/admin-booking-view.fxml");
    }

    @FXML
    private void handleManageTrips(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/admin-trip-view.fxml");
    }

    @FXML
    private void handleBrowseTrips(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/travelxp/views/trip-view.fxml"));
            Parent root = loader.load();
            TripController controller = loader.getController();
            controller.setMyTripsMode(false);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            ThemeManager.applyTheme(stage.getScene());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMyTrips(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/travelxp/views/trip-view.fxml"));
            Parent root = loader.load();
            TripController controller = loader.getController();
            controller.setMyTripsMode(true);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            ThemeManager.applyTheme(stage.getScene());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleActivities(ActionEvent event) {
        if (Main.getSession().getUser().getRole().equals("ADMIN")) {
            changeScene(event, "/com/travelxp/views/admin-activity-view.fxml");
        }
    }

    @FXML
    private void handleManageComments(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/moderation-view.fxml");
    }

    @FXML
    private void handleManageServices(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/service-view.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        Main.setSession(null);
        changeScene(event, "/com/travelxp/views/login.fxml");
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

    @FXML
    private void toggleTheme(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        ThemeManager.toggleTheme(stage.getScene());
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

    private void loadProperties() {
        try {
            List<Property> properties = propertyService.getAllProperties();
            propertyData.setAll(properties);
            propertyTable.setItems(propertyData);
            
            if (userCardsContainer != null) {
                userCardsContainer.getChildren().clear();
                for (Property p : properties) {
                    userCardsContainer.getChildren().add(createUserPropertyCard(p));
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Load Failed", e.getMessage());
        }
    }

    private VBox createUserPropertyCard(Property p) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setPrefWidth(350);
        card.setPadding(new Insets(15));
        card.setAlignment(javafx.geometry.Pos.TOP_CENTER);

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(200);
        imageContainer.getStyleClass().add("showcase-image-container");
        
        ImageView iv = new ImageView();
        iv.setFitHeight(200);
        iv.setFitWidth(320);
        iv.setPreserveRatio(true);
        
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(320, 200);
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        imageContainer.setClip(clip);

        if (p.getImages() != null && !p.getImages().isEmpty()) {
            File imgFile = new File(p.getImages());
            if (imgFile.exists()) {
                iv.setImage(new Image(imgFile.toURI().toString(), true));
            }
        }
        imageContainer.getChildren().add(iv);

        VBox info = new VBox(5);
        Label title = new Label(p.getTitle());
        title.getStyleClass().add("title-4");
        title.setStyle("-fx-font-size: 18px;");
        
        Label loc = new Label(p.getCity() + ", " + p.getCountry());
        loc.getStyleClass().add("text-muted");
        
        Label price = new Label("$" + p.getPricePerNight() + " / night");
        price.getStyleClass().add("accent");
        price.setStyle("-fx-font-weight: bold;");

        Button bookBtn = new Button("Book Now");
        bookBtn.getStyleClass().add("accent");
        bookBtn.setMaxWidth(Double.MAX_VALUE);
        bookBtn.setOnAction(e -> handleBook(p));

        info.getChildren().addAll(title, loc, price, bookBtn);
        card.getChildren().addAll(imageContainer, info);
        return card;
    }

	private void handleBook(Property property) {
		Dialog<Booking> dialog = new Dialog<>();
		dialog.setTitle("Book: " + property.getTitle());
		
		final double basePrice = property.getPricePerNight().doubleValue();
		final List<Offer> allOffers = new ArrayList<>();
        final List<Service> availableExtraServices = new ArrayList<>();
        try {
            allOffers.addAll(offerService.getAllActiveOffersForProperty(property.getId()));
            availableExtraServices.addAll(serviceService.getAllServices());
        } catch (SQLException e) {
            // ignore
        }

		ButtonType bookButtonType = new ButtonType("Confirm & Pay", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(bookButtonType, ButtonType.CANCEL);

		VBox content = new VBox(10);
		content.setPadding(new Insets(20));
		
		DatePicker datePicker = new DatePicker(LocalDate.now());
		TextField durationField = new TextField("1");
		Label offerLabel = new Label("No active offers for selected date.");
        offerLabel.setStyle("-fx-text-fill: #999;");
		
        VBox servicesBox = new VBox(5);
        servicesBox.getChildren().add(new Label("Extra Services (Price per night):"));
        List<CheckBox> serviceCheckboxes = new ArrayList<>();
        for (Service s : availableExtraServices) {
            CheckBox cb = new CheckBox(s.getServiceType() + " (+$" + s.getPrice() + ")");
            cb.setUserData(s);
            serviceCheckboxes.add(cb);
            servicesBox.getChildren().add(cb);
        }

		Label totalLabel = new Label("Total Price: $" + String.format("%.2f", basePrice));
		
        java.lang.Runnable updatePrices = () -> {
            try {
                LocalDate selectedDate = datePicker.getValue();
                int days = Integer.parseInt(durationField.getText());
                if (selectedDate == null) return;

                double currentDiscount = 0;
                String currentOfferTitle = "";
                
                for (Offer o : allOffers) {
                    if (!selectedDate.isBefore(o.getStartDate()) && !selectedDate.isAfter(o.getEndDate())) {
                        currentDiscount = o.getDiscountPercentage().doubleValue();
                        currentOfferTitle = o.getTitle();
                        break;
                    }
                }

                if (currentDiscount > 0) {
                    offerLabel.setText("Applied Offer: " + currentOfferTitle + " (" + currentDiscount + "% OFF)");
                    offerLabel.setStyle("-fx-text-fill: #D4AF37; -fx-font-weight: bold;");
                } else {
                    offerLabel.setText("No active offers for selected date.");
                    offerLabel.setStyle("-fx-text-fill: #999;");
                }

                double servicesNightly = 0;
                for (CheckBox cb : serviceCheckboxes) {
                    if (cb.isSelected()) {
                        servicesNightly += ((Service)cb.getUserData()).getPrice();
                    }
                }

                double total = (basePrice + servicesNightly) * days * (1 - currentDiscount/100.0);
                totalLabel.setText("Total Price: $" + String.format("%.2f", total));
            } catch (NumberFormatException e) {
                totalLabel.setText("Invalid duration");
            }
        };

		durationField.textProperty().addListener((obs, oldV, newV) -> updatePrices.run());
        datePicker.valueProperty().addListener((obs, oldV, newV) -> updatePrices.run());
        for (CheckBox cb : serviceCheckboxes) {
            cb.selectedProperty().addListener((obs, oldV, newV) -> updatePrices.run());
        }

		content.getChildren().addAll(
			new Label("Booking Date:"), datePicker,
			new Label("Duration (Days):"), durationField,
            servicesBox,
            offerLabel,
			totalLabel
		);

		dialog.getDialogPane().setContent(content);
		dialog.setResultConverter(btn -> {
			if (btn == bookButtonType) {
				try {
					int duration = Integer.parseInt(durationField.getText());
                    LocalDate selectedDate = datePicker.getValue();
                    
                    double currentDiscount = 0;
                    for (Offer o : allOffers) {
                        if (!selectedDate.isBefore(o.getStartDate()) && !selectedDate.isAfter(o.getEndDate())) {
                            currentDiscount = o.getDiscountPercentage().doubleValue();
                            break;
                        }
                    }

                    double servicesNightly = 0;
                    List<Service> selectedServices = new ArrayList<>();
                    for (CheckBox cb : serviceCheckboxes) {
                        if (cb.isSelected()) {
                            Service s = (Service)cb.getUserData();
                            servicesNightly += s.getPrice();
                            selectedServices.add(s);
                        }
                    }

					double finalPrice = (basePrice + servicesNightly) * duration * (1 - currentDiscount/100.0);
					
					Booking b = new Booking(
						Main.getSession().getUser().getId(),
						property.getId(), 
						0, 
						0, 
						Date.valueOf(selectedDate),
						"CONFIRMED",
						duration,
						finalPrice
					);
                    b.setExtraServices(selectedServices);
                    return b;
				} catch (NumberFormatException e) {
					return null;
				}
			}
			return null;
		});

		Optional<Booking> result = dialog.showAndWait();
		if (result.isPresent()) {
            Booking booking = result.get();
			try {
                if (Main.getSession().getUser().getBalance() < booking.getTotalPrice()) {
                    showAlert(Alert.AlertType.ERROR, "Insufficient Funds", "Low Balance", "Your balance is too low for this booking.");
                    return;
                }
				bookingService.addBooking(booking);
				userService.updateBalance(booking.getUserId(), -booking.getTotalPrice());
				Main.getSession().setUser(userService.getUserById(booking.getUserId()));
				
				showAlert(Alert.AlertType.INFORMATION, "Success", "Booked & Paid", "Your booking has been confirmed and paid.");
			} catch (SQLException e) {
				showAlert(Alert.AlertType.ERROR, "Error", "Booking Failed", e.getMessage());
			}
		}
	}

	@FXML
	private void handleAddProperty() {
		try {
			int ownerId = parseRequiredInt(ownerIdField.getText(), "Owner ID");
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

	private void changeScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            ThemeManager.applyTheme(stage.getScene());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Scene Error", "Failed to load view: " + e.getMessage());
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
