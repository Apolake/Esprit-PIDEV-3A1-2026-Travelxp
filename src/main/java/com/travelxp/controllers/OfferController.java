package com.travelxp.controllers;

import com.travelxp.Main;
import com.travelxp.models.Offer;
import com.travelxp.services.OfferService;
import com.travelxp.utils.ThemeManager;

import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
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
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

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
	
	@FXML private GridPane adminForm;
	@FXML private TableColumn<Offer, Void> actionsCol;
    @FXML private Pane animatedBg;

	private final OfferService offerService = new OfferService();
	private final ObservableList<Offer> offerData = FXCollections.observableArrayList();
    private final Random random = new Random();

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

		boolean isAdmin = Main.getSession().getUser().getRole().equals("ADMIN");
		adminForm.setVisible(isAdmin);
		adminForm.setManaged(isAdmin);
		
		addActionsToTable();
		loadOffers();
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

	private void addActionsToTable() {
		javafx.util.Callback<TableColumn<Offer, Void>, TableCell<Offer, Void>> cellFactory = param -> new TableCell<>() {
			private final Button bookBtn = new Button("Book Now");
			{
				bookBtn.getStyleClass().add("primary-button");
				bookBtn.setOnAction(event -> handleBookFromOffer(getTableView().getItems().get(getIndex())));
			}
			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) setGraphic(null);
				else setGraphic(bookBtn);
			}
		};
		actionsCol.setCellFactory(cellFactory);
	}

	private void handleBookFromOffer(Offer offer) {
		showAlert(Alert.AlertType.INFORMATION, "Offer Applied", "Booking with " + offer.getDiscountPercentage() + "% discount", "Please proceed to booking in the Properties menu.");
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

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            String fxml = "/com/travelxp/views/admin_dashboard.fxml";
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
    private void handleManageProperties(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/admin-property-view.fxml");
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
    private void handleActivities(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/admin-activity-view.fxml");
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

    private void changeScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            ThemeManager.applyTheme(stage.getScene());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleTheme(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        ThemeManager.toggleTheme(stage.getScene());
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
