package com.travelxp.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.RectVector;

import com.travelxp.Main;
import com.travelxp.models.User;
import com.travelxp.services.FaceRecognitionService;
import com.travelxp.services.TotpService;
import com.travelxp.services.UserService;
import com.travelxp.utils.ThemeManager;

import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Pane animatedBg;

    private final UserService userService = new UserService();
    private final TotpService totpService = new TotpService();
    private final Random random = new Random();

    @FXML
    public void initialize() {
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
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Empty Fields", "Please enter both email and password.");
            return;
        }

        try {
            User user = userService.login(email, password);
            if (user != null) {
                if (user.isTotpEnabled() && user.getTotpSecret() != null) {
                    // Show TOTP verification dialog before completing login
                    showTotpVerificationDialog(event, user);
                } else {
                    // No 2FA — proceed directly
                    completeLogin(event, user);
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Login Failed", "Invalid email or password.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Database Error", e.getMessage());
        }
    }

    /**
     * Show a modal dialog asking the user for their 6-digit TOTP code.
     * If verified, completes the login. If cancelled or wrong, stays on login screen.
     */
    private void showTotpVerificationDialog(ActionEvent event, User user) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(((Node) event.getSource()).getScene().getWindow());
        dialog.setTitle("Two-Factor Authentication");
        dialog.setResizable(false);

        Label titleLabel = new Label("Two-Factor Authentication");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label instructionLabel = new Label("Enter the 6-digit code from your authenticator app.");
        instructionLabel.setStyle("-fx-font-size: 13px;");
        instructionLabel.setWrapText(true);

        TextField codeField = new TextField();
        codeField.setPromptText("000000");
        codeField.setMaxWidth(200);
        codeField.setStyle("-fx-font-size: 20px; -fx-alignment: center; -fx-letter-spacing: 8px;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        Button verifyBtn = new Button("Verify");
        verifyBtn.getStyleClass().add("accent");
        verifyBtn.setPrefWidth(140);
        verifyBtn.setDefaultButton(true);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("flat");
        cancelBtn.setPrefWidth(140);

        HBox btnBox = new HBox(15, verifyBtn, cancelBtn);
        btnBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox(20, titleLabel, instructionLabel, codeField, errorLabel, btnBox);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.setStyle("-fx-background-color: -color-bg-default;");

        Scene dialogScene = new Scene(layout, 400, 320);
        ThemeManager.applyTheme(dialogScene);
        dialog.setScene(dialogScene);

        final boolean[] verified = {false};

        verifyBtn.setOnAction(e -> {
            String code = codeField.getText().trim();
            if (code.length() != 6 || !code.matches("\\d{6}")) {
                errorLabel.setText("Please enter a valid 6-digit code.");
                errorLabel.setVisible(true);
                return;
            }
            if (totpService.validateCode(user.getTotpSecret(), code)) {
                verified[0] = true;
                dialog.close();
            } else {
                errorLabel.setText("Invalid code. Please try again.");
                errorLabel.setVisible(true);
                codeField.clear();
                codeField.requestFocus();
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());

        Platform.runLater(codeField::requestFocus);
        dialog.showAndWait();

        if (verified[0]) {
            completeLogin(event, user);
        }
    }

    /**
     * Complete the login process: set session and navigate to dashboard.
     */
    private void completeLogin(ActionEvent event, User user) {
        Main.setSession(new com.travelxp.UserSession(user));
        String fxml = user.getRole().equals("ADMIN")
            ? "/com/travelxp/views/admin_dashboard.fxml"
            : "/com/travelxp/views/dashboard.fxml";
        changeScene(event, fxml);
    }

    @FXML
    private void handleRegisterRedirect(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/register.fxml");
    }

    @FXML
    private void handleFaceLogin(ActionEvent event) {
        try {
            FaceRecognitionService faceService = new FaceRecognitionService();
            if (!faceService.hasEnrolledFaces()) {
                showAlert(Alert.AlertType.WARNING, "Face ID", "No Faces Enrolled",
                    "No users have registered Face ID yet.\nLogin with email/password first, then register Face ID from Edit Profile.");
                return;
            }

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(((Node) event.getSource()).getScene().getWindow());
            dialog.setTitle("Face ID Login");

            ImageView webcamView = new ImageView();
            webcamView.setFitWidth(640);
            webcamView.setFitHeight(480);
            webcamView.setPreserveRatio(true);

            Label statusLabel = new Label("Position your face in front of the camera");
            statusLabel.setStyle("-fx-font-size: 14px;");

            Button scanBtn = new Button("Scan Face");
            scanBtn.getStyleClass().add("accent");
            scanBtn.setPrefWidth(150);
            Button cancelBtn = new Button("Cancel");
            cancelBtn.getStyleClass().add("flat");
            cancelBtn.setPrefWidth(150);

            HBox btns = new HBox(15, scanBtn, cancelBtn);
            btns.setAlignment(Pos.CENTER);

            VBox layout = new VBox(20, webcamView, statusLabel, btns);
            layout.setAlignment(Pos.CENTER);
            layout.setPadding(new Insets(30));
            layout.setStyle("-fx-background-color: -color-bg-default;");

            Scene dialogScene = new Scene(layout, 720, 620);
            ThemeManager.applyTheme(dialogScene);
            dialog.setScene(dialogScene);

            faceService.startWebcam();

            final AtomicReference<Mat> lastFrame = new AtomicReference<>();

            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "webcam-login-thread");
                t.setDaemon(true);
                return t;
            });

            executor.scheduleAtFixedRate(() -> {
                try {
                    Mat frame = faceService.grabFrame();
                    if (frame != null) {
                        lastFrame.set(frame);
                        Mat display = frame.clone();
                        RectVector faces = faceService.detectFaces(display);
                        faceService.drawFaceRects(display, faces);
                        Image fxImage = FaceRecognitionService.matToImage(display);
                        boolean detected = faces.size() > 0;
                        Platform.runLater(() -> {
                            webcamView.setImage(fxImage);
                            if (detected) {
                                statusLabel.setText("Face detected! Click 'Scan Face' to login.");
                                statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60;");
                            } else {
                                statusLabel.setText("Position your face in front of the camera");
                                statusLabel.setStyle("-fx-font-size: 14px;");
                            }
                        });
                    }
                } catch (Exception ex) {
                    // Ignore frame grab errors during streaming
                }
            }, 0, 33, TimeUnit.MILLISECONDS);

            scanBtn.setOnAction(e -> {
                Mat frame = lastFrame.get();
                if (frame == null) {
                    statusLabel.setText("Waiting for camera... Try again in a moment.");
                    statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e74c3c;");
                    return;
                }
                Mat face = faceService.extractFace(frame);
                if (face == null) {
                    statusLabel.setText("No face detected. Please look directly at the camera.");
                    statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e74c3c;");
                    return;
                }
                int userId = faceService.recognizeFace(face);
                if (userId > 0) {
                    executor.shutdownNow();
                    faceService.stopWebcam();
                    dialog.close();
                    try {
                        User user = userService.getUserById(userId);
                        if (user != null) {
                            Main.setSession(new com.travelxp.UserSession(user));
                            String fxml = user.getRole().equals("ADMIN")
                                ? "/com/travelxp/views/admin_dashboard.fxml"
                                : "/com/travelxp/views/dashboard.fxml";
                            changeScene(event, fxml);
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Error", "User Not Found",
                                "The recognized face does not match any active user.");
                        }
                    } catch (SQLException ex) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Database Error", ex.getMessage());
                    }
                } else {
                    statusLabel.setText("Face not recognized. Try again or login with email/password.");
                    statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e74c3c;");
                }
            });

            Runnable cleanup = () -> {
                executor.shutdownNow();
                faceService.stopWebcam();
            };

            cancelBtn.setOnAction(e -> { cleanup.run(); dialog.close(); });
            dialog.setOnCloseRequest(e -> cleanup.run());

            dialog.showAndWait();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Face ID Error",
                "Failed to initialize Face ID: " + e.getMessage());
            e.printStackTrace();
        }
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
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
