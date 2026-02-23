package com.travelxp.controllers;

import com.travelxp.Main;
import com.travelxp.models.Activity;
import com.travelxp.models.Trip;
import com.travelxp.services.ActivityService;
import com.travelxp.services.TripService;
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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class TripController {

    @FXML private Label pageTitle;
    @FXML private FlowPane userTripsContainer;
    @FXML private ScrollPane userScrollPane;
    @FXML private GridPane adminForm;
    @FXML private Pane animatedBg;
    
    @FXML private Button tripsNavBtn;
    @FXML private Button myTripsNavBtn;

    // Admin Table
    @FXML private TableView<Trip> tripTable;
    @FXML private TableColumn<Trip, Long> idCol;
    @FXML private TableColumn<Trip, String> nameCol;
    @FXML private TableColumn<Trip, String> originCol;
    @FXML private TableColumn<Trip, String> destinationCol;
    @FXML private TableColumn<Trip, LocalDate> startDateCol;
    @FXML private TableColumn<Trip, LocalDate> endDateCol;
    @FXML private TableColumn<Trip, String> statusCol;
    @FXML private TableColumn<Trip, Integer> xpCol;

    // Admin Fields
    @FXML private TextField nameField;
    @FXML private TextField originField;
    @FXML private TextField destinationField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField budgetField;
    @FXML private ComboBox<String> statusCombo;

    private final TripService tripService = new TripService();
    private final ActivityService activityService = new ActivityService();
    private final UserService userService = new UserService();
    private final ObservableList<Trip> tripData = FXCollections.observableArrayList();
    private final Random random = new Random();

    private boolean isMyTripsMode = false;

    @FXML
    public void initialize() {
        boolean isAdmin = Main.getSession().getUser().getRole().equals("ADMIN");

        if (idCol != null) {
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameCol.setCellValueFactory(new PropertyValueFactory<>("tripName"));
            originCol.setCellValueFactory(new PropertyValueFactory<>("origin"));
            destinationCol.setCellValueFactory(new PropertyValueFactory<>("destination"));
            startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
            endDateCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
            statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
            xpCol.setCellValueFactory(new PropertyValueFactory<>("totalXpEarned"));

            tripTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) populateForm(newVal);
            });
        }

        if (statusCombo != null) {
            statusCombo.setItems(FXCollections.observableArrayList("PLANNED", "ONGOING", "COMPLETED", "CANCELLED"));
        }

        if (adminForm != null) {
            adminForm.setVisible(isAdmin);
            adminForm.setManaged(isAdmin);
        }
        if (userScrollPane != null) {
            userScrollPane.setVisible(!isAdmin);
            userScrollPane.setManaged(!isAdmin);
        }

        javafx.application.Platform.runLater(this::loadTrips);
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

    public void setMyTripsMode(boolean mode) {
        this.isMyTripsMode = mode;
        if (pageTitle != null) pageTitle.setText(mode ? "My Trips" : "Browse Trips");
        updateNavStyles();
        loadTrips();
    }

    private void updateNavStyles() {
        if (tripsNavBtn != null) {
            tripsNavBtn.getStyleClass().remove("accent");
            if (!tripsNavBtn.getStyleClass().contains("flat")) tripsNavBtn.getStyleClass().add("flat");
        }
        if (myTripsNavBtn != null) {
            myTripsNavBtn.getStyleClass().remove("accent");
            if (!myTripsNavBtn.getStyleClass().contains("flat")) myTripsNavBtn.getStyleClass().add("flat");
        }
        
        if (isMyTripsMode && myTripsNavBtn != null) {
            myTripsNavBtn.getStyleClass().remove("flat");
            myTripsNavBtn.getStyleClass().add("accent");
        } else if (!isMyTripsMode && tripsNavBtn != null) {
            tripsNavBtn.getStyleClass().remove("flat");
            tripsNavBtn.getStyleClass().add("accent");
        }
    }

    private void loadTrips() {
        try {
            boolean isAdmin = Main.getSession().getUser().getRole().equals("ADMIN");
            List<Trip> trips;
            
            if (isAdmin && !isMyTripsMode) {
                trips = tripService.getAllTrips();
            } else if (isMyTripsMode) {
                trips = tripService.getTripsByUserId(Main.getSession().getUser().getId());
            } else {
                trips = tripService.getAllTrips().stream()
                        .filter(t -> t.getUserId() == null)
                        .toList();
            }

            tripData.setAll(trips);
            if (tripTable != null) tripTable.setItems(tripData);

            if (userTripsContainer != null) {
                userTripsContainer.getChildren().clear();
                for (Trip t : trips) {
                    userTripsContainer.getChildren().add(createTripCard(t));
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Load Failed", e.getMessage());
        }
    }

    private VBox createTripCard(Trip t) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(350);
        card.setPadding(new Insets(20));

        Label nameLab = new Label(t.getTripName());
        nameLab.getStyleClass().add("title-4");
        
        Label routeLab = new Label(t.getOrigin() + " ➔ " + t.getDestination());
        routeLab.getStyleClass().add("text-muted");

        double displayCost = isMyTripsMode ? t.getTotalExpenses() : (t.getBudgetAmount() != null ? t.getBudgetAmount() : 0.0);
        String prefix = isMyTripsMode ? "My Expenses: $" : "Entry Cost: $";
        
        Label costLab = new Label(prefix + String.format("%.2f", displayCost));
        costLab.getStyleClass().add("accent");
        costLab.setStyle("-fx-font-weight: bold;");

        VBox activitiesBox = new VBox(5);
        if (isMyTripsMode) {
            try {
                List<Activity> joined = activityService.getActivitiesByTripId(t.getId()).stream()
                        .filter(a -> "DONE".equals(a.getStatus()))
                        .toList();
                if (!joined.isEmpty()) {
                    Label header = new Label("Joined Activities:");
                    header.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
                    activitiesBox.getChildren().add(header);
                    for (Activity a : joined) {
                        Label al = new Label("• " + a.getTitle());
                        al.setStyle("-fx-font-size: 11px;");
                        activitiesBox.getChildren().add(al);
                    }
                }
            } catch (SQLException e) {}
        }

        HBox actions = new HBox(10);
        actions.setPadding(new Insets(10, 0, 0, 0));
        
        if (isMyTripsMode) {
            Button manageBtn = new Button("Activities");
            manageBtn.getStyleClass().add("secondary-button");
            manageBtn.setOnAction(e -> handleManageActivities(t));
            
            Button cancelBtn = new Button("Cancel Trip");
            cancelBtn.getStyleClass().add("danger-button");
            cancelBtn.setOnAction(e -> handleCancelTrip(t));
            
            actions.getChildren().addAll(manageBtn, cancelBtn);
        } else {
            Button participateBtn = new Button("Participate");
            participateBtn.getStyleClass().add("primary-button");
            participateBtn.setMaxWidth(Double.MAX_VALUE);
            participateBtn.setOnAction(e -> handleParticipate(t));
            HBox.setHgrow(participateBtn, Priority.ALWAYS);
            actions.getChildren().add(participateBtn);
        }

        card.getChildren().addAll(nameLab, routeLab, costLab, activitiesBox, actions);
        return card;
    }

    private void handleCancelTrip(Trip trip) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Trip");
        confirm.setHeaderText("Cancel your participation in " + trip.getTripName() + "?");
        confirm.setContentText("You will be refunded: $" + String.format("%.2f", trip.getTotalExpenses()));
        
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    int userId = Main.getSession().getUser().getId();
                    double refund = trip.getTotalExpenses();
                    userService.updateBalance(userId, refund);
                    Main.getSession().setUser(userService.getUserById(userId));
                    if (trip.getParentId() != null) {
                        tripService.removeTripParticipant(trip.getParentId(), userId);
                    }
                    tripService.deleteTrip(trip.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Refunded", "Trip Cancelled", "Your balance has been refunded.");
                    loadTrips();
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Cancellation Failed", e.getMessage());
                }
            }
        });
    }

    private void handleParticipate(Trip template) {
        try {
            int userId = Main.getSession().getUser().getId();
            if (tripService.isUserParticipating(template.getId(), userId)) {
                showAlert(Alert.AlertType.WARNING, "Already Participating", "Action Denied", "You are already a participant.");
                return;
            }
        } catch (SQLException e) { e.printStackTrace(); }

        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Participate: " + template.getTripName());
        dialog.setHeaderText("Join this trip and select activities.");
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);
        final double baseEntry = template.getBudgetAmount() != null ? template.getBudgetAmount() : 0.0;
        Label baseLabel = new Label("Entry Fee: $" + String.format("%.2f", baseEntry));
        baseLabel.setStyle("-fx-font-weight: bold;");
        VBox activityList = new VBox(10);
        activityList.getChildren().add(new Label("Available Activities:"));
        List<CheckBox> checkBoxes = new ArrayList<>();
        try {
            List<Activity> templateActivities = activityService.getActivitiesByTripId(template.getId());
            for (Activity ta : templateActivities) {
                CheckBox cb = new CheckBox(ta.getTitle() + " (+$" + (ta.getCostAmount() != null ? ta.getCostAmount() : 0.0) + ")");
                cb.setUserData(ta);
                checkBoxes.add(cb);
                activityList.getChildren().add(cb);
            }
        } catch (SQLException e) {}
        Label totalLabel = new Label("Total Cost: $" + String.format("%.2f", baseEntry));
        Runnable updateTotal = () -> {
            double total = baseEntry;
            for (CheckBox cb : checkBoxes) if (cb.isSelected()) total += ((Activity)cb.getUserData()).getCostAmount();
            totalLabel.setText("Total Cost: $" + String.format("%.2f", total));
        };
        for (CheckBox cb : checkBoxes) cb.setOnAction(e -> updateTotal.run());
        content.getChildren().addAll(baseLabel, activityList, new Separator(), totalLabel);
        dialog.getDialogPane().setContent(content);
        ButtonType joinType = new ButtonType("Join & Pay", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(joinType, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> {
            if (btn == joinType) {
                double total = baseEntry;
                for (CheckBox cb : checkBoxes) if (cb.isSelected()) total += ((Activity)cb.getUserData()).getCostAmount();
                return total;
            }
            return null;
        });
        Optional<Double> result = dialog.showAndWait();
        result.ifPresent(totalCost -> {
            try {
                int userId = Main.getSession().getUser().getId();
                if (Main.getSession().getUser().getBalance() < totalCost) {
                    showAlert(Alert.AlertType.ERROR, "Insufficient Funds", "Low Balance", "You need $" + totalCost);
                    return;
                }
                userService.updateBalance(userId, -totalCost);
                Main.getSession().setUser(userService.getUserById(userId));
                Trip myTrip = new Trip();
                myTrip.setUserId((long) userId); myTrip.setParentId(template.getId());
                myTrip.setTripName(template.getTripName()); myTrip.setOrigin(template.getOrigin());
                myTrip.setDestination(template.getDestination()); myTrip.setStartDate(template.getStartDate());
                myTrip.setEndDate(template.getEndDate()); myTrip.setStatus("PLANNED");
                myTrip.setTotalExpenses(totalCost); myTrip.setTotalXpEarned(0);
                myTrip.setBudgetAmount(baseEntry);
                tripService.addTrip(myTrip);
                tripService.addTripParticipant(template.getId(), userId);
                for (CheckBox cb : checkBoxes) {
                    Activity ta = (Activity) cb.getUserData();
                    Activity ma = new Activity();
                    ma.setTripId(myTrip.getId()); ma.setTitle(ta.getTitle());
                    ma.setType(ta.getType()); ma.setDescription(ta.getDescription());
                    ma.setActivityDate(ta.getActivityDate()); ma.setCostAmount(ta.getCostAmount());
                    ma.setXpEarned(ta.getXpEarned()); ma.setStatus(cb.isSelected() ? "DONE" : "PLANNED");
                    activityService.addActivity(ma);
                    if (cb.isSelected()) tripService.addActivityParticipant(ta.getId(), userId);
                }
                showAlert(Alert.AlertType.INFORMATION, "Success", "Joined Trip", "Trip added to your journeys!");
                setMyTripsMode(true);
            } catch (SQLException e) { showAlert(Alert.AlertType.ERROR, "Error", "Failed to join", e.getMessage()); }
        });
    }

    private void handleManageActivities(Trip trip) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Activities: " + trip.getTripName());
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(450);
        try {
            List<Activity> activities = activityService.getActivitiesByTripId(trip.getId());
            if (activities.isEmpty()) {
                content.getChildren().add(new Label("No activities available."));
            } else {
                for (Activity a : activities) {
                    HBox row = new HBox(10);
                    row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    Label info = new Label(a.getTitle() + " ($" + (a.getCostAmount() != null ? a.getCostAmount() : 0.0) + ")");
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    if ("DONE".equals(a.getStatus())) {
                        Label participating = new Label("✓ Joined");
                        participating.setStyle("-fx-text-fill: #4CAF50;");
                        Button refundBtn = new Button("Refund");
                        refundBtn.getStyleClass().add("danger-button");
                        refundBtn.setOnAction(e -> {
                            try {
                                double refund = a.getCostAmount() != null ? a.getCostAmount() : 0.0;
                                int userId = Main.getSession().getUser().getId();
                                userService.updateBalance(userId, refund);
                                Main.getSession().setUser(userService.getUserById(userId));
                                a.setStatus("PLANNED");
                                activityService.updateActivity(a);
                                trip.setTotalExpenses(trip.getTotalExpenses() - refund);
                                tripService.updateTrip(trip);
                                if (trip.getParentId() != null) {
                                    List<Activity> templateActs = activityService.getActivitiesByTripId(trip.getParentId());
                                    for (Activity tact : templateActs) {
                                        if (tact.getTitle().equals(a.getTitle())) {
                                            tripService.removeActivityParticipant(tact.getId(), userId); break;
                                        }
                                    }
                                }
                                dialog.close(); loadTrips();
                            } catch (SQLException ex) { showAlert(Alert.AlertType.ERROR, "Error", "Refund Failed", ex.getMessage()); }
                        });
                        row.getChildren().addAll(info, spacer, participating, refundBtn);
                    } else {
                        Button payBtn = new Button("Pay & Join");
                        payBtn.getStyleClass().add("accent");
                        payBtn.setOnAction(e -> {
                            try {
                                double cost = a.getCostAmount() != null ? a.getCostAmount() : 0.0;
                                int userId = Main.getSession().getUser().getId();
                                if (Main.getSession().getUser().getBalance() < cost) {
                                    showAlert(Alert.AlertType.ERROR, "Insufficient Funds", "Low Balance", "You need $" + cost); return;
                                }
                                userService.updateBalance(userId, -cost);
                                Main.getSession().setUser(userService.getUserById(userId));
                                a.setStatus("DONE");
                                activityService.updateActivity(a);
                                trip.setTotalExpenses(trip.getTotalExpenses() + cost);
                                tripService.updateTrip(trip);
                                if (trip.getParentId() != null) {
                                    List<Activity> templateActs = activityService.getActivitiesByTripId(trip.getParentId());
                                    for (Activity tact : templateActs) {
                                        if (tact.getTitle().equals(a.getTitle())) {
                                            tripService.addActivityParticipant(tact.getId(), userId); break;
                                        }
                                    }
                                }
                                dialog.close(); loadTrips();
                            } catch (SQLException ex) { showAlert(Alert.AlertType.ERROR, "Error", "Payment Failed", ex.getMessage()); }
                        });
                        row.getChildren().addAll(info, spacer, payBtn);
                    }
                    content.getChildren().add(row);
                }
            }
        } catch (SQLException e) {}
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    @FXML
    private void handleViewParticipants() {
        Trip selected = tripTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            List<Trip> participants = tripService.getParticipantsByTripId(selected.getId());
            ListView<String> listView = new ListView<>();
            if (participants.isEmpty()) listView.getItems().add("No participants yet.");
            else {
                for (Trip pt : participants) listView.getItems().add("User ID: " + pt.getUserId() + " | Total Spent: $" + String.format("%.2f", pt.getTotalExpenses()));
            }
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Participants: " + selected.getTripName());
            dialog.getDialogPane().setContent(listView);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();
        } catch (SQLException e) {}
    }

    @FXML
    private void handleAddTrip() {
        try {
            Trip t = new Trip();
            t.setUserId(null); 
            t.setTripName(nameField.getText());
            t.setOrigin(originField.getText());
            t.setDestination(destinationField.getText());
            t.setStartDate(startDatePicker.getValue());
            t.setEndDate(endDatePicker.getValue());
            t.setStatus(statusCombo.getValue());
            t.setBudgetAmount(Double.parseDouble(budgetField.getText()));
            t.setTotalExpenses(0.0); t.setTotalXpEarned(0);
            tripService.addTrip(t);
            loadTrips(); clearForm();
        } catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Error", "Create Failed", e.getMessage()); }
    }

    @FXML
    private void handleUpdateTrip() {
        Trip selected = tripTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            selected.setTripName(nameField.getText()); selected.setOrigin(originField.getText());
            selected.setDestination(destinationField.getText()); selected.setStartDate(startDatePicker.getValue());
            selected.setEndDate(endDatePicker.getValue()); selected.setStatus(statusCombo.getValue());
            selected.setBudgetAmount(Double.parseDouble(budgetField.getText()));
            tripService.updateTrip(selected); loadTrips();
        } catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Error", "Update Failed", e.getMessage()); }
    }

    @FXML
    private void handleDeleteTrip() {
        Trip selected = tripTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try { tripService.deleteTrip(selected.getId()); loadTrips(); clearForm(); }
        catch (SQLException e) { showAlert(Alert.AlertType.ERROR, "Error", "Delete Failed", e.getMessage()); }
    }

    private void populateForm(Trip t) {
        if (nameField != null) nameField.setText(t.getTripName());
        if (originField != null) originField.setText(t.getOrigin());
        if (destinationField != null) destinationField.setText(t.getDestination());
        if (startDatePicker != null) startDatePicker.setValue(t.getStartDate());
        if (endDatePicker != null) endDatePicker.setValue(t.getEndDate());
        if (statusCombo != null) statusCombo.setValue(t.getStatus());
        if (budgetField != null) budgetField.setText(t.getBudgetAmount() != null ? String.valueOf(t.getBudgetAmount()) : "0");
    }

    @FXML private void handleClearForm() { clearForm(); }
    private void clearForm() {
        if (nameField != null) nameField.clear();
        if (originField != null) originField.clear();
        if (destinationField != null) destinationField.clear();
        if (startDatePicker != null) startDatePicker.setValue(null);
        if (endDatePicker != null) endDatePicker.setValue(null);
        if (statusCombo != null) statusCombo.setValue("PLANNED");
        if (budgetField != null) budgetField.clear();
        if (tripTable != null) tripTable.getSelectionModel().clearSelection();
    }

    @FXML private void handleTasks(ActionEvent event) { changeScene(event, "/com/travelxp/views/tasks.fxml"); }
    @FXML private void handleBrowseProperties(ActionEvent event) {
        changeScene(event, Main.getSession().getUser().getRole().equals("ADMIN") ? "/com/travelxp/views/admin-property-view.fxml" : "/com/travelxp/views/property-view.fxml");
    }
    @FXML private void handleBrowseTrips(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/travelxp/views/trip-view.fxml"));
            Parent root = loader.load();
            TripController controller = loader.getController();
            controller.setMyTripsMode(false);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            ThemeManager.applyTheme(stage.getScene());
        } catch (IOException e) { e.printStackTrace(); }
    }
    @FXML private void handleMyBookings(ActionEvent event) {
        changeScene(event, Main.getSession().getUser().getRole().equals("ADMIN") ? "/com/travelxp/views/admin-booking-view.fxml" : "/com/travelxp/views/booking-view.fxml");
    }
    @FXML private void handleMyTrips(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/travelxp/views/trip-view.fxml"));
            Parent root = loader.load();
            TripController controller = loader.getController();
            controller.setMyTripsMode(true);
            Stage stage = (Stage) (event == null ? (pageTitle != null ? pageTitle.getScene().getWindow() : null) : ((Node) event.getSource()).getScene().getWindow());
            if (stage != null) { stage.getScene().setRoot(root); ThemeManager.applyTheme(stage.getScene()); }
        } catch (IOException e) { e.printStackTrace(); }
    }
    @FXML private void handleEditProfile(ActionEvent event) { changeScene(event, "/com/travelxp/views/edit_profile.fxml"); }
    @FXML private void handleChangePassword(ActionEvent event) { changeScene(event, "/com/travelxp/views/change_password.fxml"); }
    @FXML private void handleFeedback(ActionEvent event) { changeScene(event, "/com/travelxp/views/feedback-view.fxml"); }
    @FXML private void handleManageProperties(ActionEvent event) { changeScene(event, "/com/travelxp/views/admin-property-view.fxml"); }
    @FXML private void handleManageOffers(ActionEvent event) { changeScene(event, "/com/travelxp/views/offer-view.fxml"); }
    @FXML private void handleManageBookings(ActionEvent event) { changeScene(event, "/com/travelxp/views/admin-booking-view.fxml"); }
    @FXML private void handleManageComments(ActionEvent event) { changeScene(event, "/com/travelxp/views/moderation-view.fxml"); }
    @FXML private void handleManageServices(ActionEvent event) { changeScene(event, "/com/travelxp/views/service-view.fxml"); }
    @FXML private void handleManageTrips(ActionEvent event) { changeScene(event, "/com/travelxp/views/admin-trip-view.fxml"); }
    @FXML private void handleActivities(ActionEvent event) { changeScene(event, "/com/travelxp/views/admin-activity-view.fxml"); }
    @FXML private void handleLogout(ActionEvent event) { Main.setSession(null); changeScene(event, "/com/travelxp/views/login.fxml"); }
    @FXML private void handleBack(ActionEvent event) {
        changeScene(event, Main.getSession().getUser().getRole().equals("ADMIN") ? "/com/travelxp/views/admin_dashboard.fxml" : "/com/travelxp/views/dashboard.fxml");
    }
    @FXML private void toggleTheme(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        ThemeManager.toggleTheme(stage.getScene());
    }
    private void changeScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            ThemeManager.applyTheme(stage.getScene());
        } catch (IOException e) { e.printStackTrace(); }
    }
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title); alert.setHeaderText(header); alert.setContentText(content);
        alert.showAndWait();
    }
}
