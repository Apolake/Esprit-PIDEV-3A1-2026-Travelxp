package com.travelxp.controllers;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.RectVector;

import com.travelxp.Main;
import com.travelxp.models.User;
import com.travelxp.services.FaceRecognitionService;
import com.travelxp.services.TotpService;
import com.travelxp.services.UserService;

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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class EditProfileController {

    @FXML private TextField emailField;
    @FXML private DatePicker birthdayPicker;
    @FXML private TextArea bioArea;
    @FXML private Label imagePathLabel;
    @FXML private Pane animatedBg;

    private String selectedImagePath = "";
    private final UserService userService = new UserService();
    private User currentUser;
    private final Random random = new Random();

    @FXML
    public void initialize() {
        currentUser = Main.getSession().getUser();
        if (currentUser != null) {
            emailField.setText(currentUser.getEmail());
            birthdayPicker.setValue(currentUser.getBirthday());
            bioArea.setText(currentUser.getBio());
            selectedImagePath = currentUser.getProfileImage();
            if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                imagePathLabel.setText(new File(selectedImagePath).getName());
            }
        }
        Platform.runLater(this::startBackgroundAnimation);
    }

    private void startBackgroundAnimation() {
        if (animatedBg == null) return;
        
        // Fewer, more intentional circles for a cleaner look
        for (int i = 0; i < 10; i++) {
            Circle circle = createCircle();
            animatedBg.getChildren().add(circle);
            animateCircle(circle);
        }
    }

    private Circle createCircle() {
        // Varied sizes for depth
        double radius = 30 + random.nextDouble() * 120;
        Circle circle = new Circle(radius);
        
        circle.setCenterX(random.nextDouble() * 1200);
        circle.setCenterY(random.nextDouble() * 900);
        
        // Dynamic opacity for professional layering
        double opacity = 0.03 + random.nextDouble() * 0.05;
        
        // Check current theme state at creation time
        boolean isDark = com.travelxp.utils.ThemeManager.isDark();
        String color = isDark ? "#D4AF37" : "#002b5c";
        
        circle.setFill(Color.web(color, opacity));
        circle.setStroke(Color.web(color, opacity * 1.5));
        circle.setStrokeWidth(1.5);
        
        // Add a slight blur to the circles themselves
        circle.setEffect(new javafx.scene.effect.BoxBlur(10, 10, 2));
        
        return circle;
    }

    private void animateCircle(Circle circle) {
        // Faster duration: 6 to 12 seconds
        double duration = 6 + random.nextDouble() * 6;
        
        TranslateTransition tt = new TranslateTransition(Duration.seconds(duration), circle);
        tt.setByX(random.nextDouble() * 500 - 250);
        tt.setByY(random.nextDouble() * 500 - 250);
        tt.setAutoReverse(true);
        tt.setCycleCount(Animation.INDEFINITE);
        // Smooth easing is key for professionalism
        tt.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
        tt.play();
    }

    @FXML
    private void handleImageUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (selectedFile != null) {
            String relativePath = com.travelxp.utils.ImageUtil.saveImage(selectedFile);
            if (relativePath != null) {
                selectedImagePath = relativePath;
                imagePathLabel.setText(new File(selectedImagePath).getName());
            }
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        String newEmail = emailField.getText();
        LocalDate newBirthday = birthdayPicker.getValue();
        String newBio = bioArea.getText();

        if (validateInput(newEmail, newBirthday, newBio)) {
            try {
                boolean success = true;
                
                if (!newEmail.equals(currentUser.getEmail())) {
                    success &= userService.updateEmail(currentUser.getId(), newEmail);
                }
                
                if (!newBirthday.equals(currentUser.getBirthday())) {
                    success &= userService.updateBirthday(currentUser.getId(), newBirthday);
                }
                
                if (newBio != null && !newBio.equals(currentUser.getBio())) {
                    success &= userService.updateBio(currentUser.getId(), newBio);
                }
                
                if (selectedImagePath != null && !selectedImagePath.equals(currentUser.getProfileImage())) {
                    success &= userService.updateProfileImage(currentUser.getId(), selectedImagePath);
                }

                if (success) {
                    // Refresh session user data
                    Main.getSession().setUser(userService.getUserById(currentUser.getId()));
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Profile Updated", "Your profile changes have been saved.");
                    handleCancel(event);
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Update Failed", e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        String fxml = "/com/travelxp/views/dashboard.fxml";
        if (Main.getSession().getUser().getRole().equals("ADMIN")) {
            fxml = "/com/travelxp/views/admin_dashboard.fxml";
        }
        changeScene(event, fxml);
    }

    @FXML
    private void handleTasks(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/tasks.fxml");
    }

    @FXML
    private void handleBrowseProperties(ActionEvent event) {
        String fxml = "/com/travelxp/views/property-view.fxml";
        if (Main.getSession().getUser().getRole().equals("ADMIN")) {
            fxml = "/com/travelxp/views/admin-property-view.fxml";
        }
        changeScene(event, fxml);
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
    private void handleBrowseTrips(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/travelxp/views/trip-view.fxml"));
            Parent root = loader.load();
            TripController controller = loader.getController();
            controller.setMyTripsMode(false);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            com.travelxp.utils.ThemeManager.applyTheme(stage.getScene());
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
            com.travelxp.utils.ThemeManager.applyTheme(stage.getScene());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleActivities(ActionEvent event) {
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
    private void handleFaceEnrollment(ActionEvent event) {
        try {
            FaceRecognitionService faceService = new FaceRecognitionService();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(((Node) event.getSource()).getScene().getWindow());
            dialog.setTitle("Register Face ID");

            ImageView webcamView = new ImageView();
            webcamView.setFitWidth(640);
            webcamView.setFitHeight(480);
            webcamView.setPreserveRatio(true);

            Label statusLabel = new Label("Position your face and capture 5 photos from slightly different angles.");
            statusLabel.setStyle("-fx-font-size: 14px;");
            statusLabel.setWrapText(true);

            Label counterLabel = new Label("Photos: 0 / 5");
            counterLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            Button captureBtn = new Button("Capture Photo");
            captureBtn.getStyleClass().add("accent");
            captureBtn.setPrefWidth(150);
            Button saveBtn = new Button("Save Face ID");
            saveBtn.getStyleClass().addAll("accent");
            saveBtn.setPrefWidth(150);
            saveBtn.setDisable(true);
            Button cancelBtn = new Button("Cancel");
            cancelBtn.getStyleClass().add("flat");
            cancelBtn.setPrefWidth(150);

            HBox btns = new HBox(15, captureBtn, saveBtn, cancelBtn);
            btns.setAlignment(Pos.CENTER);

            VBox layout = new VBox(20, webcamView, statusLabel, counterLabel, btns);
            layout.setAlignment(Pos.CENTER);
            layout.setPadding(new Insets(30));
            layout.setStyle("-fx-background-color: -color-bg-default;");

            Scene dialogScene = new Scene(layout, 720, 680);
            com.travelxp.utils.ThemeManager.applyTheme(dialogScene);
            dialog.setScene(dialogScene);

            faceService.startWebcam();

            List<Mat> capturedFaces = new ArrayList<>();
            final AtomicReference<Mat> lastFrame = new AtomicReference<>();

            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "webcam-enroll-thread");
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
                        Platform.runLater(() -> webcamView.setImage(fxImage));
                    }
                } catch (Exception ex) {
                    // Ignore frame grab errors during streaming
                }
            }, 0, 33, TimeUnit.MILLISECONDS);

            captureBtn.setOnAction(e -> {
                if (capturedFaces.size() >= 5) return;
                Mat frame = lastFrame.get();
                if (frame == null) {
                    statusLabel.setText("Waiting for camera... Try again.");
                    statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e74c3c;");
                    return;
                }
                Mat face = faceService.extractFace(frame);
                if (face != null) {
                    capturedFaces.add(face);
                    counterLabel.setText("Photos: " + capturedFaces.size() + " / 5");
                    if (capturedFaces.size() >= 5) {
                        captureBtn.setDisable(true);
                        saveBtn.setDisable(false);
                        statusLabel.setText("All photos captured! Click 'Save Face ID' to finish.");
                        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60;");
                    } else {
                        statusLabel.setText("Photo " + capturedFaces.size() + " captured! Move your head slightly for the next one.");
                        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60;");
                    }
                } else {
                    statusLabel.setText("No face detected. Look directly at the camera and try again.");
                    statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e74c3c;");
                }
            });

            saveBtn.setOnAction(e -> {
                try {
                    faceService.enrollFace(currentUser.getId(), capturedFaces);
                    userService.setFaceRegistered(currentUser.getId(), true);
                    currentUser = userService.getUserById(currentUser.getId());
                    Main.getSession().setUser(currentUser);

                    executor.shutdownNow();
                    faceService.stopWebcam();
                    dialog.close();

                    showAlert(Alert.AlertType.INFORMATION, "Success", "Face ID Registered",
                        "Your Face ID has been registered successfully!\nYou can now use it to login.");
                } catch (Exception ex) {
                    statusLabel.setText("Error saving face data: " + ex.getMessage());
                    statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e74c3c;");
                    ex.printStackTrace();
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
    private void handleLogout(ActionEvent event) {
        Main.setSession(null);
        changeScene(event, "/com/travelxp/views/login.fxml");
    }

    @FXML
    private void handleTotpSetup(ActionEvent event) {
        if (currentUser.isTotpEnabled()) {
            // Already enabled — offer to disable
            handleTotpDisable(event);
        } else {
            // Not enabled — start setup flow
            handleTotpEnable(event);
        }
    }

    private void handleTotpEnable(ActionEvent event) {
        try {
            TotpService totpService = new TotpService();
            String secret = totpService.generateSecret();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(((Node) event.getSource()).getScene().getWindow());
            dialog.setTitle("Setup Two-Factor Authentication");
            dialog.setResizable(false);

            Label titleLabel = new Label("Setup Two-Factor Authentication");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            Label instructionLabel = new Label(
                "1. Scan the QR code below with your authenticator app\n" +
                "   (Google Authenticator, Authy, Microsoft Authenticator, etc.)\n\n" +
                "2. Enter the 6-digit code shown in the app to verify setup.");
            instructionLabel.setStyle("-fx-font-size: 12px;");
            instructionLabel.setWrapText(true);

            ImageView qrView = new ImageView();
            qrView.setFitWidth(250);
            qrView.setFitHeight(250);
            qrView.setPreserveRatio(true);

            javafx.scene.image.Image qrImage = totpService.generateQrCodeImage(secret, currentUser.getEmail());
            qrView.setImage(qrImage);

            Label secretLabel = new Label("Manual key: " + secret);
            secretLabel.setStyle("-fx-font-size: 11px; -fx-font-family: monospace;");
            secretLabel.setWrapText(true);

            Label codePrompt = new Label("Verification Code:");
            codePrompt.setStyle("-fx-font-size: 13px;");

            TextField codeField = new TextField();
            codeField.setPromptText("000000");
            codeField.setMaxWidth(180);
            codeField.setStyle("-fx-font-size: 18px; -fx-alignment: center;");

            Label errorLabel = new Label();
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
            errorLabel.setVisible(false);

            Button verifyBtn = new Button("Enable 2FA");
            verifyBtn.getStyleClass().add("accent");
            verifyBtn.setPrefWidth(140);
            verifyBtn.setDefaultButton(true);

            Button cancelBtn = new Button("Cancel");
            cancelBtn.getStyleClass().add("flat");
            cancelBtn.setPrefWidth(140);

            HBox btnBox = new HBox(15, verifyBtn, cancelBtn);
            btnBox.setAlignment(Pos.CENTER);

            VBox layout = new VBox(15, titleLabel, instructionLabel, qrView, secretLabel,
                                   codePrompt, codeField, errorLabel, btnBox);
            layout.setAlignment(Pos.CENTER);
            layout.setPadding(new Insets(30));
            layout.setStyle("-fx-background-color: -color-bg-default;");

            Scene dialogScene = new Scene(layout, 450, 680);
            com.travelxp.utils.ThemeManager.applyTheme(dialogScene);
            dialog.setScene(dialogScene);

            verifyBtn.setOnAction(e -> {
                String code = codeField.getText().trim();
                if (code.length() != 6 || !code.matches("\\d{6}")) {
                    errorLabel.setText("Please enter a valid 6-digit code.");
                    errorLabel.setVisible(true);
                    return;
                }
                if (totpService.validateCode(secret, code)) {
                    try {
                        userService.setTotpSecret(currentUser.getId(), secret);
                        userService.setTotpEnabled(currentUser.getId(), true);
                        currentUser = userService.getUserById(currentUser.getId());
                        Main.getSession().setUser(currentUser);
                        dialog.close();
                        showAlert(Alert.AlertType.INFORMATION, "Success", "2FA Enabled",
                            "Two-Factor Authentication has been enabled!\nYou will need your authenticator code on every login.");
                    } catch (Exception ex) {
                        errorLabel.setText("Error saving: " + ex.getMessage());
                        errorLabel.setVisible(true);
                    }
                } else {
                    errorLabel.setText("Invalid code. Check your authenticator and try again.");
                    errorLabel.setVisible(true);
                    codeField.clear();
                    codeField.requestFocus();
                }
            });

            cancelBtn.setOnAction(e -> dialog.close());

            Platform.runLater(codeField::requestFocus);
            dialog.showAndWait();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "2FA Setup Error",
                "Failed to initialize 2FA setup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleTotpDisable(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Disable 2FA");
        confirm.setHeaderText("Disable Two-Factor Authentication?");
        confirm.setContentText("Are you sure you want to disable 2FA? This will make your account less secure.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                // Ask for current TOTP code to confirm
                Stage dialog = new Stage();
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(((Node) event.getSource()).getScene().getWindow());
                dialog.setTitle("Confirm Disable 2FA");
                dialog.setResizable(false);

                Label titleLabel = new Label("Confirm with Authenticator Code");
                titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

                Label instructionLabel = new Label("Enter your current 6-digit authenticator code to disable 2FA.");
                instructionLabel.setStyle("-fx-font-size: 13px;");
                instructionLabel.setWrapText(true);

                TextField codeField = new TextField();
                codeField.setPromptText("000000");
                codeField.setMaxWidth(180);
                codeField.setStyle("-fx-font-size: 18px; -fx-alignment: center;");

                Label errorLabel = new Label();
                errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
                errorLabel.setVisible(false);

                Button disableBtn = new Button("Disable 2FA");
                disableBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                disableBtn.setPrefWidth(140);
                disableBtn.setDefaultButton(true);

                Button cancelBtn = new Button("Cancel");
                cancelBtn.getStyleClass().add("flat");
                cancelBtn.setPrefWidth(140);

                HBox btnBox = new HBox(15, disableBtn, cancelBtn);
                btnBox.setAlignment(Pos.CENTER);

                VBox layout = new VBox(20, titleLabel, instructionLabel, codeField, errorLabel, btnBox);
                layout.setAlignment(Pos.CENTER);
                layout.setPadding(new Insets(40));
                layout.setStyle("-fx-background-color: -color-bg-default;");

                Scene dialogScene = new Scene(layout, 400, 320);
                com.travelxp.utils.ThemeManager.applyTheme(dialogScene);
                dialog.setScene(dialogScene);

                TotpService totpService = new TotpService();

                disableBtn.setOnAction(e -> {
                    String code = codeField.getText().trim();
                    if (code.length() != 6 || !code.matches("\\d{6}")) {
                        errorLabel.setText("Please enter a valid 6-digit code.");
                        errorLabel.setVisible(true);
                        return;
                    }
                    if (totpService.validateCode(currentUser.getTotpSecret(), code)) {
                        try {
                            userService.disableTotp(currentUser.getId());
                            currentUser = userService.getUserById(currentUser.getId());
                            Main.getSession().setUser(currentUser);
                            dialog.close();
                            showAlert(Alert.AlertType.INFORMATION, "Success", "2FA Disabled",
                                "Two-Factor Authentication has been disabled.");
                        } catch (Exception ex) {
                            errorLabel.setText("Error: " + ex.getMessage());
                            errorLabel.setVisible(true);
                        }
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
            }
        });
    }

    @FXML
    private void toggleTheme(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        com.travelxp.utils.ThemeManager.toggleTheme(stage.getScene());
    }

    private boolean validateInput(String email, LocalDate birthday, String bio) {
        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid Email", "Please enter a valid email format.");
            return false;
        }

        if (birthday == null || birthday.isAfter(LocalDate.now())) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid Birthday", "Please select a valid past date for your birthday.");
            return false;
        }

        if (bio != null && bio.length() > 500) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid Bio", "Bio must be max 500 characters.");
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    private void changeScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            com.travelxp.utils.ThemeManager.applyTheme(stage.getScene());
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
