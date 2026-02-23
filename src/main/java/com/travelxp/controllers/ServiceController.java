package com.travelxp.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;

import com.travelxp.models.Service;
import com.travelxp.services.ServiceService;
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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ServiceController {

	@FXML private TableView<Service> serviceTable;
	@FXML private TableColumn<Service, Integer> serviceIdCol;
	@FXML private TableColumn<Service, String> providerNameCol;
	@FXML private TableColumn<Service, String> serviceTypeCol;
	@FXML private TableColumn<Service, Double> priceCol;
	@FXML private TableColumn<Service, Boolean> ecoFriendlyCol;
	@FXML private TableColumn<Service, Integer> xpRewardCol;

	@FXML private TextField providerNameField;
	@FXML private TextField serviceTypeField;
	@FXML private TextField priceField;
	@FXML private CheckBox ecoFriendlyCheckBox;
	@FXML private TextField xpRewardField;
    @FXML private Pane animatedBg;

	private final ServiceService serviceService = new ServiceService();
	private final ObservableList<Service> serviceData = FXCollections.observableArrayList();
    private final Random random = new Random();

	@FXML
	public void initialize() {
		serviceIdCol.setCellValueFactory(new PropertyValueFactory<>("serviceId"));
		providerNameCol.setCellValueFactory(new PropertyValueFactory<>("providerName"));
		serviceTypeCol.setCellValueFactory(new PropertyValueFactory<>("serviceType"));
		priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
		ecoFriendlyCol.setCellValueFactory(new PropertyValueFactory<>("ecoFriendly"));
		xpRewardCol.setCellValueFactory(new PropertyValueFactory<>("xpReward"));

		serviceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
			if (selected != null) {
				populateForm(selected);
			}
		});

		loadServices();
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
	private void handleAddService() {
		try {
			String providerName = requireText(providerNameField.getText(), "Provider name");
			String serviceType = requireText(serviceTypeField.getText(), "Service type");
			double price = parseRequiredDouble(priceField.getText(), "Price");
			int xpReward = parseRequiredInt(xpRewardField.getText(), "XP reward");

			if (price < 0) {
				throw new IllegalArgumentException("Price cannot be negative.");
			}
			if (xpReward < 0) {
				throw new IllegalArgumentException("XP reward cannot be negative.");
			}

			Service service = new Service(providerName, serviceType, price, ecoFriendlyCheckBox.isSelected(), xpReward);
			serviceService.addService(service);

			loadServices();
			clearForm();
			showAlert(Alert.AlertType.INFORMATION, "Success", "Service Created", "Service added successfully.");
		} catch (IllegalArgumentException e) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid Input", e.getMessage());
		} catch (SQLException e) {
			showAlert(Alert.AlertType.ERROR, "Database Error", "Create Failed", e.getMessage());
		}
	}

	@FXML
	private void handleUpdateService() {
		Service selected = serviceTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(Alert.AlertType.WARNING, "No Selection", "Update Failed", "Please select a service first.");
			return;
		}

		try {
			String providerName = requireText(providerNameField.getText(), "Provider name");
			String serviceType = requireText(serviceTypeField.getText(), "Service type");
			double price = parseRequiredDouble(priceField.getText(), "Price");
			int xpReward = parseRequiredInt(xpRewardField.getText(), "XP reward");

			if (price < 0) {
				throw new IllegalArgumentException("Price cannot be negative.");
			}
			if (xpReward < 0) {
				throw new IllegalArgumentException("XP reward cannot be negative.");
			}

			Service updated = new Service(providerName, serviceType, price, ecoFriendlyCheckBox.isSelected(), xpReward);
			updated.setServiceId(selected.getServiceId());

			serviceService.updateService(updated);
			loadServices();
			showAlert(Alert.AlertType.INFORMATION, "Success", "Service Updated", "Service updated successfully.");
		} catch (IllegalArgumentException e) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid Input", e.getMessage());
		} catch (SQLException e) {
			showAlert(Alert.AlertType.ERROR, "Database Error", "Update Failed", e.getMessage());
		}
	}

	@FXML
	private void handleDeleteService() {
		Service selected = serviceTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(Alert.AlertType.WARNING, "No Selection", "Delete Failed", "Please select a service first.");
			return;
		}

		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
		confirm.setTitle("Delete Service");
		confirm.setHeaderText("Delete service #" + selected.getServiceId() + "?");
		confirm.setContentText("This action cannot be undone.");

		confirm.showAndWait().ifPresent(response -> {
			if (response == ButtonType.OK) {
				try {
					serviceService.deleteService(selected.getServiceId());
					loadServices();
					clearForm();
					showAlert(Alert.AlertType.INFORMATION, "Success", "Service Deleted", "Service deleted successfully.");
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
	private void handleRefreshServices() {
		loadServices();
	}

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            String fxml = "/com/travelxp/views/admin_dashboard.fxml";
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
    private void handleActivities(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/admin-activity-view.fxml");
    }

    @FXML
    private void handleManageComments(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/moderation-view.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        com.travelxp.Main.setSession(null);
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

	private void loadServices() {
		try {
			serviceData.setAll(serviceService.getAllServices());
			serviceTable.setItems(serviceData);
		} catch (SQLException e) {
			showAlert(Alert.AlertType.ERROR, "Database Error", "Load Failed", e.getMessage());
		}
	}

	private void populateForm(Service service) {
		providerNameField.setText(service.getProviderName());
		serviceTypeField.setText(service.getServiceType());
		priceField.setText(String.valueOf(service.getPrice()));
		ecoFriendlyCheckBox.setSelected(service.isEcoFriendly());
		xpRewardField.setText(String.valueOf(service.getXpReward()));
	}

	private void clearForm() {
		providerNameField.clear();
		serviceTypeField.clear();
		priceField.clear();
		ecoFriendlyCheckBox.setSelected(false);
		xpRewardField.clear();
		serviceTable.getSelectionModel().clearSelection();
	}

	private String requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " is required.");
		}
		return value.trim();
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

	private double parseRequiredDouble(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " is required.");
		}
		try {
			return Double.parseDouble(value.trim());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(fieldName + " must be a valid decimal value.");
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
