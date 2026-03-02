package com.travelxp.controllers;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.travelxp.Main;
import com.travelxp.models.Booking;
import com.travelxp.models.Property;
import com.travelxp.models.Service;
import com.travelxp.services.BookingService;
import com.travelxp.services.CancellationPolicyEngine;
import com.travelxp.services.CurrencyExchangeService;
import com.travelxp.services.DynamicPricingEngine;
import com.travelxp.services.EmailService;
import com.travelxp.services.PropertyService;
import com.travelxp.services.UserService;
import com.travelxp.utils.ThemeManager;

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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

public class BookingController {

	@FXML private TableView<Booking> bookingTable;
	@FXML private TableColumn<Booking, Integer> bookingIdCol;
	@FXML private TableColumn<Booking, Integer> userIdCol;
    @FXML private TableColumn<Booking, Long> propertyIdCol;
	@FXML private TableColumn<Booking, Date> bookingDateCol;
	@FXML private TableColumn<Booking, Integer> durationCol;
	@FXML private TableColumn<Booking, Double> totalPriceCol;
	@FXML private TableColumn<Booking, String> bookingStatusCol;
	@FXML private TableColumn<Booking, Void> actionsCol;

	@FXML private TextField userIdField;
	@FXML private TextField tripIdField;
	@FXML private TextField serviceIdField;
	@FXML private DatePicker bookingDatePicker;
	@FXML private ComboBox<String> bookingStatusCombo;
	
	@FXML private GridPane adminForm;
    @FXML private ScrollPane userScrollPane;
    @FXML private VBox userBookingsContainer;
    @FXML private Pane animatedBg;
    @FXML private ComboBox<String> currencyCombo;
    @FXML private Label convertedPriceLabel;

	private final BookingService bookingService = new BookingService();
    private final PropertyService propertyService = new PropertyService();
    private final UserService userService = new UserService();
    private final CancellationPolicyEngine cancellationEngine = new CancellationPolicyEngine();
    private final DynamicPricingEngine pricingEngine = new DynamicPricingEngine();
    private final EmailService emailService = new EmailService();
    private final CurrencyExchangeService currencyService = new CurrencyExchangeService();
	private final ObservableList<Booking> bookingData = FXCollections.observableArrayList();
    private final Random random = new Random();
    private String selectedCurrency = "USD";

	@FXML
	public void initialize() {
		bookingIdCol.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
		userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        if (propertyIdCol != null) propertyIdCol.setCellValueFactory(new PropertyValueFactory<>("propertyId"));
		bookingDateCol.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
		durationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));
		totalPriceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
		bookingStatusCol.setCellValueFactory(new PropertyValueFactory<>("bookingStatus"));

		bookingStatusCombo.setItems(FXCollections.observableArrayList("PENDING", "CONFIRMED", "CANCELLED", "COMPLETED"));
		bookingStatusCombo.getSelectionModel().select("PENDING");

		bookingTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
			if (selected != null) {
				populateForm(selected);
			}
		});

		boolean isAdmin = Main.getSession().getUser().getRole().equals("ADMIN");
		if (adminForm != null) {
            adminForm.setVisible(isAdmin);
            adminForm.setManaged(isAdmin);
        }
        if (bookingTable != null) {
            bookingTable.setVisible(isAdmin);
            bookingTable.setManaged(isAdmin);
        }
        if (userScrollPane != null) {
            userScrollPane.setVisible(!isAdmin);
            userScrollPane.setManaged(!isAdmin);
        }
		
		addActionsToTable();
		loadBookings();
        
        // Initialize currency selector
        if (currencyCombo != null) {
            Set<String> currencies = new TreeSet<>(currencyService.getSupportedCurrencies());
            currencyCombo.setItems(FXCollections.observableArrayList(new ArrayList<>(currencies)));
            currencyCombo.setValue("USD");
            currencyCombo.setOnAction(e -> {
                selectedCurrency = currencyCombo.getValue();
                loadBookings(); // refresh cards with new currency
            });
        }
        
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
        changeScene(event, "/com/travelxp/views/booking-view.fxml");
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
    private void toggleTheme(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        ThemeManager.toggleTheme(stage.getScene());
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
        // Calculate refund using cancellation policy engine
        java.sql.Timestamp createdAt = booking.getCreatedAt();
        if (createdAt == null) {
            try { createdAt = bookingService.getBookingCreatedAt(booking.getBookingId()); } catch (SQLException ignored) {}
        }
        CancellationPolicyEngine.CancellationResult result = cancellationEngine.calculateRefund(booking, createdAt);

		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
		confirm.setTitle("Cancel Booking");
		confirm.setHeaderText("Cancellation Policy");
        confirm.setContentText(
            "Policy: " + result.getPolicyApplied() + "\n\n" +
            "Refund: $" + String.format("%.2f", result.getRefundAmount()) +
            " (" + String.format("%.0f", result.getRefundPercentage()) + "% of $" +
            String.format("%.2f", booking.getTotalPrice()) + ")\n\n" +
            "Do you want to proceed with the cancellation?"
        );
		confirm.showAndWait().ifPresent(btn -> {
			if (btn == ButtonType.OK) {
				try {
					bookingService.updateBookingStatus(booking.getBookingId(), "CANCELLED");
                    userService.updateBalance(booking.getUserId(), result.getRefundAmount());
                    Main.getSession().setUser(userService.getUserById(booking.getUserId()));
					loadBookings();

                    showAlert(Alert.AlertType.INFORMATION, "Cancelled",
                            "Booking Cancelled",
                            "Refund of $" + String.format("%.2f", result.getRefundAmount()) + " applied.\n" +
                            result.getPolicyApplied());

                    // Send cancellation email
                    String email = Main.getSession().getUser().getEmail();
                    if (email != null && !email.isEmpty()) {
                        new Thread(() -> emailService.sendCancellationNotice(
                            email, booking.getBookingId(), result.getRefundAmount(),
                            result.getPolicyApplied(), selectedCurrency
                        )).start();
                    }
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
				int newDuration = Integer.parseInt(durationStr);
                if (newDuration <= 0) return;

                double pricePerDay = booking.getTotalPrice() / booking.getDuration();
                double newTotalPrice = pricePerDay * newDuration;
                double diff = newTotalPrice - booking.getTotalPrice();

                if (diff > 0 && Main.getSession().getUser().getBalance() < diff) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Insufficient Funds", "You need $" + String.format("%.2f", diff) + " more.");
                    return;
                }

				bookingService.updateBookingDuration(booking.getBookingId(), newDuration);
                bookingService.updateBookingPrice(booking.getBookingId(), newTotalPrice);
                userService.updateBalance(booking.getUserId(), -diff);
                Main.getSession().setUser(userService.getUserById(booking.getUserId()));
                
                // Send modification email
                String email = Main.getSession().getUser().getEmail();
                if (email != null && !email.isEmpty()) {
                    final double emailPrice = newTotalPrice;
                    new Thread(() -> emailService.sendBookingModification(
                        email, booking.getBookingId(),
                        "Duration changed from " + booking.getDuration() + " to " + newDuration + " days",
                        emailPrice, selectedCurrency
                    )).start();
                }
                
				loadBookings();
			} catch (NumberFormatException | SQLException e) {
				showAlert(Alert.AlertType.ERROR, "Error", "Update Failed", "Invalid duration or database error.");
			}
		});
	}

    private void loadBookings() {
        try {
            boolean isAdmin = Main.getSession().getUser().getRole().equals("ADMIN");
            java.util.List<Booking> bookings;
            if (isAdmin) {
                bookings = bookingService.getAllBookings();
            } else {
                bookings = bookingService.getBookingsByUserId(Main.getSession().getUser().getId());
            }
            bookingData.setAll(bookings);
            bookingTable.setItems(bookingData);
            
            if (userBookingsContainer != null) {
                userBookingsContainer.getChildren().clear();
                for (Booking b : bookings) {
                    userBookingsContainer.getChildren().add(createUserBookingCard(b));
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Load Failed", e.getMessage());
        }
    }

    private VBox createUserBookingCard(Booking b) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20));

        HBox top = new HBox(20);
        top.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox left = new VBox(5);
        Label idLab = new Label("Booking #" + b.getBookingId());
        idLab.getStyleClass().add("title-4");
        Label dateLab = new Label("Date: " + b.getBookingDate());
        dateLab.getStyleClass().add("text-muted");
        Label durationLab = new Label("Duration: " + b.getDuration() + " days");
        durationLab.getStyleClass().add("text-muted");
        Label guestsLab = new Label("Guests: " + (b.getNumGuests() > 0 ? b.getNumGuests() : "N/A"));
        guestsLab.getStyleClass().add("text-muted");
        
        String servicesStr = b.getExtraServices().stream()
                .map(Service::getServiceType)
                .collect(Collectors.joining(", "));
        Label servicesLab = new Label("Extra Services: " + (servicesStr.isEmpty() ? "None" : servicesStr));
        servicesLab.getStyleClass().add("text-small");
        servicesLab.setStyle("-fx-text-fill: -fx-accent-color;");

        left.getChildren().addAll(idLab, dateLab, durationLab, guestsLab, servicesLab);
        HBox.setHgrow(left, javafx.scene.layout.Priority.ALWAYS);

        VBox right = new VBox(5);
        right.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        Label priceLab = new Label(String.format("$%.2f", b.getTotalPrice()));
        priceLab.getStyleClass().add("accent");
        priceLab.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
        
        // Currency conversion display
        if (!"USD".equals(selectedCurrency)) {
            double converted = currencyService.convert(b.getTotalPrice(), "USD", selectedCurrency);
            if (converted >= 0) {
                Label convertedLab = new Label(String.format("≈ %.2f %s", converted, selectedCurrency));
                convertedLab.setStyle("-fx-text-fill: #D4AF37; -fx-font-size: 13px;");
                right.getChildren().add(convertedLab);
            }
        }
        
        Label statusLab = new Label(b.getBookingStatus());
        statusLab.setStyle("-fx-text-fill: " + getStatusColor(b.getBookingStatus()) + "; -fx-font-weight: bold;");
        right.getChildren().addAll(priceLab, statusLab);

        // Cancellation policy preview (for active bookings)
        if (!b.getBookingStatus().equals("CANCELLED") && !b.getBookingStatus().equals("COMPLETED")) {
            java.sql.Timestamp createdAt = b.getCreatedAt();
            if (createdAt == null) {
                try { createdAt = bookingService.getBookingCreatedAt(b.getBookingId()); } catch (SQLException ignored) {}
            }
            CancellationPolicyEngine.CancellationResult policyPreview = cancellationEngine.calculateRefund(b, createdAt);
            Label policyLab = new Label("Cancellation: " + policyPreview.getPolicyApplied());
            policyLab.setStyle("-fx-text-fill: #999; -fx-font-size: 11px; -fx-wrap-text: true;");
            policyLab.setWrapText(true);
            right.getChildren().add(policyLab);
        }

        top.getChildren().addAll(left, right);

        HBox actions = new HBox(10);
        actions.setPadding(new Insets(10, 0, 0, 0));
        
        if (b.getPropertyId() != null) {
            Button viewPropBtn = new Button("\uD83D\uDD0D View Property");
            viewPropBtn.getStyleClass().add("flat");
            viewPropBtn.setOnAction(e -> handleViewProperty(b.getPropertyId()));
            actions.getChildren().add(viewPropBtn);
        }

        if (!b.getBookingStatus().equals("CANCELLED") && !b.getBookingStatus().equals("COMPLETED")) {
            Button editBtn = new Button("\u270F\uFE0F Edit Duration");
            editBtn.getStyleClass().add("secondary-button");
            editBtn.setOnAction(e -> handleEditDuration(b));

            Button cancelBtn = new Button("\u274C Cancel Booking");
            cancelBtn.getStyleClass().add("danger-button");
            cancelBtn.setOnAction(e -> handleCancelBooking(b));
            
            Button pricingBtn = new Button("\uD83D\uDCB0 Price Breakdown");
            pricingBtn.getStyleClass().add("flat");
            pricingBtn.setOnAction(e -> handleShowPriceBreakdown(b));
            
            actions.getChildren().addAll(editBtn, cancelBtn, pricingBtn);
        }

        card.getChildren().addAll(top, actions);
        return card;
    }

    private void handleShowPriceBreakdown(Booking b) {
        try {
            double basePrice = 100.0; // default
            if (b.getPropertyId() != null) {
                Property p = propertyService.getPropertyById(b.getPropertyId());
                if (p != null && p.getPricePerNight() != null) {
                    basePrice = p.getPricePerNight().doubleValue();
                }
            }
            LocalDate bookingDate = b.getBookingDate() != null ? b.getBookingDate().toLocalDate() : LocalDate.now();
            int guests = b.getNumGuests() > 0 ? b.getNumGuests() : 1;
            DynamicPricingEngine.PricingBreakdown breakdown = pricingEngine.calculatePrice(
                    basePrice, bookingDate, b.getDuration(), guests,
                    b.getPropertyId(), b.getExtraServices());

            String currencyNote = "";
            if (!"USD".equals(selectedCurrency)) {
                double converted = currencyService.convert(breakdown.getTotalPrice(), "USD", selectedCurrency);
                if (converted >= 0) {
                    currencyNote = "\n\nConverted: " + String.format("%.2f %s", converted, selectedCurrency);
                }
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Dynamic Price Breakdown");
            alert.setHeaderText("Booking #" + b.getBookingId() + " – Price Analysis");
            alert.setContentText(breakdown.getExplanation() + currencyNote);
            alert.getDialogPane().setMinWidth(500);
            alert.showAndWait();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Pricing Error", e.getMessage());
        }
    }

    private void handleViewProperty(Long propertyId) {
        try {
            Property p = propertyService.getPropertyById(propertyId);
            if (p != null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Property Details");
                alert.setHeaderText(p.getTitle());
                alert.setContentText(
                    "Location: " + p.getCity() + ", " + p.getCountry() + "\n" +
                    "Address: " + p.getAddress() + "\n" +
                    "Type: " + p.getPropertyType() + "\n" +
                    "Price: $" + p.getPricePerNight() + " / night\n\n" +
                    "Description: " + p.getDescription()
                );
                alert.show();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load property", e.getMessage());
        }
    }

    private String getStatusColor(String status) {
        switch (status) {
            case "CONFIRMED": return "#4CAF50";
            case "PENDING": return "#D4AF37";
            case "CANCELLED": return "#f44336";
            default: return "#999";
        }
    }

	@FXML
	private void handleAddBooking() {
		try {
			int userId = parseRequiredInt(userIdField.getText(), "User ID");
			int tripId = tripIdField.getText().isEmpty() ? 0 : Integer.parseInt(tripIdField.getText());
			int serviceId = serviceIdField.getText().isEmpty() ? 0 : Integer.parseInt(serviceIdField.getText());
			LocalDate selectedDate = bookingDatePicker.getValue();
			if (selectedDate == null) throw new IllegalArgumentException("Booking date is required.");
			String status = bookingStatusCombo.getValue();
			Booking booking = new Booking(userId, null, tripId, serviceId, Date.valueOf(selectedDate), status, 1, 0.0);
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
