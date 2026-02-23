package com.travelxp.controllers;

import com.travelxp.Main;
import com.travelxp.models.Activity;
import com.travelxp.models.Trip;
import com.travelxp.services.ActivityService;
import com.travelxp.services.TripService;
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
import java.util.List;
import java.util.Random;

public class ActivityController {

    @FXML private TableView<Activity> activityTable;
    @FXML private TableColumn<Activity, Long> idCol;
    @FXML private TableColumn<Activity, String> titleCol;
    @FXML private TableColumn<Activity, String> typeCol;
    @FXML private TableColumn<Activity, LocalDate> dateCol;
    @FXML private TableColumn<Activity, String> statusCol;
    @FXML private TableColumn<Activity, Integer> xpCol;

    @FXML private TextField titleField;
    @FXML private TextField typeField;
    @FXML private DatePicker datePicker;
    @FXML private TextField costField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private ComboBox<Trip> tripCombo;

    @FXML private VBox userActivitiesContainer;
    @FXML private ScrollPane userScrollPane;
    @FXML private GridPane adminForm;
    @FXML private Pane animatedBg;

    private final ActivityService activityService = new ActivityService();
    private final TripService tripService = new TripService();
    private final ObservableList<Activity> activityData = FXCollections.observableArrayList();
    private final Random random = new Random();

    @FXML
    public void initialize() {
        if (idCol != null) {
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
            typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
            dateCol.setCellValueFactory(new PropertyValueFactory<>("activityDate"));
            statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
            xpCol.setCellValueFactory(new PropertyValueFactory<>("xpEarned"));

            activityTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) populateForm(newVal);
            });
        }

        if (statusCombo != null) {
            statusCombo.setItems(FXCollections.observableArrayList("PLANNED", "DONE", "CANCELLED"));
        }

        try {
            if (tripCombo != null) {
                tripCombo.setItems(FXCollections.observableArrayList(tripService.getAllTrips()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        boolean isAdmin = Main.getSession().getUser().getRole().equals("ADMIN");
        if (adminForm != null) {
            adminForm.setVisible(isAdmin);
            adminForm.setManaged(isAdmin);
        }
        if (userScrollPane != null) {
            userScrollPane.setVisible(!isAdmin);
            userScrollPane.setManaged(!isAdmin);
        }

        loadActivities();
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

    private void loadActivities() {
        try {
            List<Activity> activities = activityService.getAllActivities();
            activityData.setAll(activities);
            if (activityTable != null) activityTable.setItems(activityData);

            if (userActivitiesContainer != null) {
                userActivitiesContainer.getChildren().clear();
                for (Activity a : activities) {
                    userActivitiesContainer.getChildren().add(createActivityCard(a));
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Load Failed", e.getMessage());
        }
    }

    private VBox createActivityCard(Activity a) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(15));

        HBox top = new HBox(15);
        VBox left = new VBox(5);
        Label titleLab = new Label(a.getTitle());
        titleLab.getStyleClass().add("title-4");
        Label typeLab = new Label(a.getType());
        typeLab.getStyleClass().add("text-muted");
        left.getChildren().addAll(titleLab, typeLab);
        HBox.setHgrow(left, Priority.ALWAYS);

        VBox right = new VBox(5);
        Label dateLab = new Label(a.getActivityDate() != null ? a.getActivityDate().toString() : "No Date");
        dateLab.setStyle("-fx-font-weight: bold;");
        Label xpLab = new Label("+" + a.getXpEarned() + " XP");
        xpLab.getStyleClass().add("accent");
        right.getChildren().addAll(dateLab, xpLab);

        top.getChildren().addAll(left, right);
        card.getChildren().add(top);
        return card;
    }

    @FXML
    private void handleAddActivity() {
        try {
            Activity a = new Activity();
            a.setTitle(titleField.getText());
            a.setType(typeField.getText());
            a.setActivityDate(datePicker.getValue());
            a.setStatus(statusCombo.getValue());
            a.setCostAmount(Double.parseDouble(costField.getText()));
            Trip t = tripCombo.getValue();
            if (t != null) a.setTripId(t.getId());

            activityService.addActivity(a);
            loadActivities();
            clearForm();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Create Failed", e.getMessage());
        }
    }

    @FXML
    private void handleUpdateActivity() {
        Activity selected = activityTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            selected.setTitle(titleField.getText());
            selected.setType(typeField.getText());
            selected.setActivityDate(datePicker.getValue());
            selected.setStatus(statusCombo.getValue());
            selected.setCostAmount(Double.parseDouble(costField.getText()));
            Trip t = tripCombo.getValue();
            if (t != null) selected.setTripId(t.getId());

            activityService.updateActivity(selected);
            loadActivities();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Update Failed", e.getMessage());
        }
    }

    @FXML
    private void handleDeleteActivity() {
        Activity selected = activityTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            activityService.deleteActivity(selected.getId());
            loadActivities();
            clearForm();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Delete Failed", e.getMessage());
        }
    }

    private void populateForm(Activity a) {
        titleField.setText(a.getTitle());
        typeField.setText(a.getType());
        datePicker.setValue(a.getActivityDate());
        statusCombo.setValue(a.getStatus());
        costField.setText(String.valueOf(a.getCostAmount()));
    }

    @FXML private void handleClearForm() { clearForm(); }
    private void clearForm() {
        if (titleField != null) titleField.clear();
        if (typeField != null) typeField.clear();
        if (datePicker != null) datePicker.setValue(null);
        if (statusCombo != null) statusCombo.setValue("PLANNED");
        if (costField != null) costField.clear();
        if (activityTable != null) activityTable.getSelectionModel().clearSelection();
    }

    // Navigation
    @FXML private void handleTasks(ActionEvent event) { changeScene(event, "/com/travelxp/views/tasks.fxml"); }
    @FXML private void handleBrowseProperties(ActionEvent event) {
        String fxml = Main.getSession().getUser().getRole().equals("ADMIN") ? "/com/travelxp/views/admin-property-view.fxml" : "/com/travelxp/views/property-view.fxml";
        changeScene(event, fxml);
    }
    @FXML private void handleMyBookings(ActionEvent event) {
        String fxml = Main.getSession().getUser().getRole().equals("ADMIN") ? "/com/travelxp/views/admin-booking-view.fxml" : "/com/travelxp/views/booking-view.fxml";
        changeScene(event, fxml);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleMyTrips(ActionEvent event) {
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

    @FXML
    private void handleLogout(ActionEvent event) {
        Main.setSession(null);
        changeScene(event, "/com/travelxp/views/login.fxml");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        String fxml = Main.getSession().getUser().getRole().equals("ADMIN") ? "/com/travelxp/views/admin_dashboard.fxml" : "/com/travelxp/views/dashboard.fxml";
        changeScene(event, fxml);
    }

    @FXML
    private void toggleTheme(ActionEvent event) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title); alert.setHeaderText(header); alert.setContentText(content);
        alert.showAndWait();
    }
}
