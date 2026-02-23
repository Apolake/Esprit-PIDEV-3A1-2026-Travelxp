package com.travelxp.controllers;

import com.travelxp.Main;
import com.travelxp.models.Comment;
import com.travelxp.models.Feedback;
import com.travelxp.services.FeedbackService;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class ModerationController {

    @FXML private TableView<Feedback> feedbackTable;
    @FXML private TableColumn<Feedback, Integer> fIdCol;
    @FXML private TableColumn<Feedback, Integer> fUserIdCol;
    @FXML private TableColumn<Feedback, String> fContentCol;
    @FXML private TableColumn<Feedback, LocalDateTime> fDateCol;
    @FXML private TableColumn<Feedback, Void> fActionsCol;

    @FXML private TableView<Comment> commentTable;
    @FXML private TableColumn<Comment, Integer> cIdCol;
    @FXML private TableColumn<Comment, Integer> cFeedbackIdCol;
    @FXML private TableColumn<Comment, Integer> cUserIdCol;
    @FXML private TableColumn<Comment, String> cContentCol;
    @FXML private TableColumn<Comment, LocalDateTime> cDateCol;
    @FXML private TableColumn<Comment, Void> cActionsCol;
    @FXML private Pane animatedBg;

    private final FeedbackService feedbackService = new FeedbackService();
    private final ObservableList<Feedback> feedbackData = FXCollections.observableArrayList();
    private final ObservableList<Comment> commentData = FXCollections.observableArrayList();
    private final Random random = new Random();

    @FXML
    public void initialize() {
        // Feedback Table
        fIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        fUserIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        fContentCol.setCellValueFactory(new PropertyValueFactory<>("content"));
        fDateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        addFeedbackActions();

        // Comment Table
        cIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        cFeedbackIdCol.setCellValueFactory(new PropertyValueFactory<>("feedbackId"));
        cUserIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        cContentCol.setCellValueFactory(new PropertyValueFactory<>("content"));
        cDateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        addCommentActions();

        loadAllData();
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

    private void loadAllData() {
        try {
            List<Feedback> feedbacks = feedbackService.getAllFeedback();
            feedbackData.setAll(feedbacks);
            feedbackTable.setItems(feedbackData);

            commentData.clear();
            for (Feedback f : feedbacks) {
                commentData.addAll(feedbackService.getCommentsByFeedback(f.getId()));
            }
            commentTable.setItems(commentData);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Load Failed", e.getMessage());
        }
    }

    private void addFeedbackActions() {
        Callback<TableColumn<Feedback, Void>, TableCell<Feedback, Void>> cellFactory = param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            {
                editBtn.getStyleClass().add("secondary-button");
                editBtn.setOnAction(e -> handleEditFeedback(getTableView().getItems().get(getIndex())));
                deleteBtn.getStyleClass().add("danger-button");
                deleteBtn.setOnAction(e -> handleDeleteFeedback(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(new HBox(5, editBtn, deleteBtn));
            }
        };
        fActionsCol.setCellFactory(cellFactory);
    }

    private void addCommentActions() {
        Callback<TableColumn<Comment, Void>, TableCell<Comment, Void>> cellFactory = param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            {
                editBtn.getStyleClass().add("secondary-button");
                editBtn.setOnAction(e -> handleEditComment(getTableView().getItems().get(getIndex())));
                deleteBtn.getStyleClass().add("danger-button");
                deleteBtn.setOnAction(e -> handleDeleteComment(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(new HBox(5, editBtn, deleteBtn));
            }
        };
        cActionsCol.setCellFactory(cellFactory);
    }

    private void handleEditFeedback(Feedback feedback) {
        TextInputDialog dialog = new TextInputDialog(feedback.getContent());
        dialog.setTitle("Edit Feedback");
        dialog.setHeaderText("Modify feedback content");
        dialog.showAndWait().ifPresent(content -> {
            try {
                feedback.setContent(content);
                feedbackService.updateFeedback(feedback);
                loadAllData();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Update Failed", e.getMessage());
            }
        });
    }

    private void handleDeleteFeedback(Feedback feedback) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this feedback and all its comments?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    feedbackService.deleteFeedback(feedback.getId());
                    loadAllData();
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Delete Failed", e.getMessage());
                }
            }
        });
    }

    private void handleEditComment(Comment comment) {
        TextInputDialog dialog = new TextInputDialog(comment.getContent());
        dialog.setTitle("Edit Comment");
        dialog.setHeaderText("Modify comment content");
        dialog.showAndWait().ifPresent(content -> {
            try {
                comment.setContent(content);
                feedbackService.updateComment(comment);
                loadAllData();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Update Failed", e.getMessage());
            }
        });
    }

    private void handleDeleteComment(Comment comment) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this comment?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    feedbackService.deleteComment(comment.getId());
                    loadAllData();
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Delete Failed", e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/travelxp/views/admin_dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            ThemeManager.applyTheme(stage.getScene());
        } catch (IOException e) {
            e.printStackTrace();
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

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
