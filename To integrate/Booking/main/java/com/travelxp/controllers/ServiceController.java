package com.travelxp.controllers;

import java.sql.SQLException;

import com.travelxp.models.Service;
import com.travelxp.services.ServiceService;

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

	private final ServiceService serviceService = new ServiceService();
	private final ObservableList<Service> serviceData = FXCollections.observableArrayList();

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
