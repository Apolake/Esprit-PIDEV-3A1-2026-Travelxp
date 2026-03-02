package com.travelxp.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.travelxp.Main;
import com.travelxp.models.Gamification;
import com.travelxp.models.User;
import com.travelxp.models.UserViewModel;
import com.travelxp.services.GamificationService;
import com.travelxp.services.UserService;
import com.travelxp.utils.ThemeManager;

import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

public class AdminDashboardController {

    @FXML private TableView<UserViewModel> userTable;
    @FXML private TableColumn<UserViewModel, Integer> idCol;
    @FXML private TableColumn<UserViewModel, String> usernameCol;
    @FXML private TableColumn<UserViewModel, String> emailCol;
    @FXML private TableColumn<UserViewModel, String> bioCol;
    @FXML private TableColumn<UserViewModel, String> imageCol;
    @FXML private TableColumn<UserViewModel, String> roleCol;
    @FXML private TableColumn<UserViewModel, Integer> xpCol;
    @FXML private TableColumn<UserViewModel, Integer> levelCol;
    @FXML private TableColumn<UserViewModel, String> titleCol;
    @FXML private TableColumn<UserViewModel, Void> actionsCol;
    @FXML private Pane animatedBg;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilterCombo;
    @FXML private ComboBox<String> sortCombo;

    private final UserService userService = new UserService();
    private final GamificationService gamificationService = new GamificationService();
    private final ObservableList<UserViewModel> userData = FXCollections.observableArrayList();
    private FilteredList<UserViewModel> filteredData;
    private final Random random = new Random();

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        bioCol.setCellValueFactory(new PropertyValueFactory<>("bio"));
        imageCol.setCellValueFactory(new PropertyValueFactory<>("profileImage"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        xpCol.setCellValueFactory(new PropertyValueFactory<>("xp"));
        levelCol.setCellValueFactory(new PropertyValueFactory<>("level"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        addActionsToTable();

        // --- Search, Filter & Sort setup ---
        filteredData = new FilteredList<>(userData, p -> true);

        // Role filter combo
        roleFilterCombo.setItems(FXCollections.observableArrayList("All Roles", "USER", "ADMIN"));
        roleFilterCombo.setValue("All Roles");

        // Sort combo
        sortCombo.setItems(FXCollections.observableArrayList(
                "ID (Asc)", "ID (Desc)",
                "Username (A-Z)", "Username (Z-A)",
                "Email (A-Z)", "Email (Z-A)",
                "XP (High-Low)", "XP (Low-High)",
                "Level (High-Low)", "Level (Low-High)"
        ));

        // Live search listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        roleFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Sort listener
        sortCombo.valueProperty().addListener((obs, oldVal, newVal) -> applySorting());

        // Wrap in SortedList and bind to table
        SortedList<UserViewModel> sortedData = new SortedList<>(filteredData);
        userTable.setItems(sortedData);

        loadUsers();

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
        boolean isDark = ThemeManager.isDark();
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

    private void loadUsers() {
        try {
            userData.clear();
            List<User> users = userService.getAllUsers();
            for (User user : users) {
                Gamification g = gamificationService.getGamificationByUserId(user.getId());
                userData.add(new UserViewModel(user, g));
            }
            applyFilters();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Load Failed", e.getMessage());
        }
    }

    private void applyFilters() {
        String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String roleFilter = roleFilterCombo.getValue();

        filteredData.setPredicate(user -> {
            // Role filter
            if (roleFilter != null && !"All Roles".equals(roleFilter)) {
                if (!roleFilter.equals(user.getRole())) return false;
            }

            // Search filter
            if (!searchText.isEmpty()) {
                boolean matchesUsername = user.getUsername() != null && user.getUsername().toLowerCase().contains(searchText);
                boolean matchesEmail = user.getEmail() != null && user.getEmail().toLowerCase().contains(searchText);
                boolean matchesBio = user.getBio() != null && user.getBio().toLowerCase().contains(searchText);
                boolean matchesTitle = user.getTitle() != null && user.getTitle().toLowerCase().contains(searchText);
                boolean matchesId = String.valueOf(user.getId()).contains(searchText);
                if (!(matchesUsername || matchesEmail || matchesBio || matchesTitle || matchesId)) return false;
            }

            return true;
        });
    }

    private void applySorting() {
        String sortOption = sortCombo.getValue();
        if (sortOption == null) return;

        Comparator<UserViewModel> comparator = switch (sortOption) {
            case "ID (Asc)" -> Comparator.comparingInt(UserViewModel::getId);
            case "ID (Desc)" -> Comparator.comparingInt(UserViewModel::getId).reversed();
            case "Username (A-Z)" -> Comparator.comparing(u -> u.getUsername() != null ? u.getUsername().toLowerCase() : "");
            case "Username (Z-A)" -> Comparator.<UserViewModel, String>comparing(u -> u.getUsername() != null ? u.getUsername().toLowerCase() : "").reversed();
            case "Email (A-Z)" -> Comparator.comparing(u -> u.getEmail() != null ? u.getEmail().toLowerCase() : "");
            case "Email (Z-A)" -> Comparator.<UserViewModel, String>comparing(u -> u.getEmail() != null ? u.getEmail().toLowerCase() : "").reversed();
            case "XP (High-Low)" -> Comparator.comparingInt(UserViewModel::getXp).reversed();
            case "XP (Low-High)" -> Comparator.comparingInt(UserViewModel::getXp);
            case "Level (High-Low)" -> Comparator.comparingInt(UserViewModel::getLevel).reversed();
            case "Level (Low-High)" -> Comparator.comparingInt(UserViewModel::getLevel);
            default -> null;
        };

        if (comparator != null) {
            FXCollections.sort(userData, comparator);
        }
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        roleFilterCombo.setValue("All Roles");
        sortCombo.setValue(null);
    }

    private void addActionsToTable() {
        Callback<TableColumn<UserViewModel, Void>, TableCell<UserViewModel, Void>> cellFactory = param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final Button resetBtn = new Button("Key");

            {
                editBtn.getStyleClass().add("button");
                editBtn.setStyle("-fx-font-size: 10px; -fx-padding: 5 10;");
                editBtn.setOnAction(event -> handleEditUser(getTableView().getItems().get(getIndex())));

                deleteBtn.getStyleClass().add("button");
                deleteBtn.setStyle("-fx-font-size: 10px; -fx-padding: 5 10; -fx-background-color: #CF6679;");
                deleteBtn.setOnAction(event -> handleDeleteUser(getTableView().getItems().get(getIndex())));

                resetBtn.getStyleClass().add("button");
                resetBtn.setStyle("-fx-font-size: 10px; -fx-padding: 5 10; -fx-background-color: #3498DB;");
                resetBtn.setOnAction(event -> handleResetPassword(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox pane = new HBox(5, editBtn, resetBtn, deleteBtn);
                    setGraphic(pane);
                }
            }
        };
        actionsCol.setCellFactory(cellFactory);
    }

    private String selectedImagePath = "";

    private void handleEditUser(UserViewModel model) {
        Dialog<User> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        ThemeManager.applyThemeToNode(dialog.getDialogPane());
        dialog.setTitle("Edit User: " + model.getUsername());
        dialog.setHeaderText("Modify user details and role.");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        TextField usernameField = new TextField(model.getUsername());
        TextField emailField = new TextField(model.getEmail());
        DatePicker birthdayPicker = new DatePicker(model.getBirthday());
        TextArea bioArea = new TextArea(model.getBio());
        bioArea.setPrefHeight(100);
        bioArea.setWrapText(true);
        
        ComboBox<String> roleCombo = new ComboBox<>(FXCollections.observableArrayList("USER", "ADMIN"));
        roleCombo.setValue(model.getRole());

        TextField xpField = new TextField(String.valueOf(model.getXp()));
        TextField levelField = new TextField(String.valueOf(model.getLevel()));

        selectedImagePath = model.getProfileImage();
        Label pathLabel = new Label(selectedImagePath == null || selectedImagePath.isEmpty() ? "No image" : "Image selected");
        Button uploadBtn = new Button("Choose Image");
        uploadBtn.getStyleClass().add("secondary-button");
        uploadBtn.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            java.io.File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                String relativePath = com.travelxp.utils.ImageUtil.saveImage(file);
                if (relativePath != null) {
                    selectedImagePath = relativePath;
                    pathLabel.setText(file.getName());
                }
            }
        });

        content.getChildren().addAll(
            new Label("Username:"), usernameField,
            new Label("Email:"), emailField,
            new Label("Birthday:"), birthdayPicker,
            new Label("Bio:"), bioArea,
            new Label("Role:"), roleCombo,
            new HBox(15, new VBox(5, new Label("XP:"), xpField), new VBox(5, new Label("Level:"), levelField)),
            new Label("Profile Image:"), new HBox(10, uploadBtn, pathLabel)
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                User user = new User();
                user.setId(model.getId());
                user.setUsername(usernameField.getText());
                user.setEmail(emailField.getText());
                user.setBirthday(birthdayPicker.getValue());
                user.setBio(bioArea.getText());
                user.setRole(roleCombo.getValue());
                user.setProfileImage(selectedImagePath);
                return user;
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(user -> {
            try {
                if (userService.updateUserAsAdmin(user)) {
                    int newXp = Integer.parseInt(xpField.getText());
                    int newLevel = Integer.parseInt(levelField.getText());
                    gamificationService.updateGamification(user.getId(), newXp, newLevel);
                    loadUsers();
                }
            } catch (SQLException | NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Update Failed", "Check your inputs: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
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
    private void handleManageServices(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/service-view.fxml");
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
    private void handleTasks(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/tasks.fxml");
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
    private void handleBack(ActionEvent event) {
        changeScene(event, "/com/travelxp/views/admin_dashboard.fxml");
    }

    @FXML
    private void handleCreateUser() {
        Dialog<User> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        ThemeManager.applyThemeToNode(dialog.getDialogPane());
        dialog.setTitle("Create New User");
        dialog.setHeaderText("Enter details for the new account.");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setPrefWidth(400);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username (3-50 chars)");
        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password (8+ chars, 1 letter, 1 number)");
        DatePicker birthdayPicker = new DatePicker(LocalDate.now().minusYears(18));
        TextArea bioArea = new TextArea();
        bioArea.setPromptText("User Bio (Max 500 chars)");
        bioArea.setPrefHeight(80);
        ComboBox<String> roleCombo = new ComboBox<>(FXCollections.observableArrayList("USER", "ADMIN"));
        roleCombo.setValue("USER");

        TextField xpField = new TextField("0");
        TextField levelField = new TextField("1");

        selectedImagePath = "";
        Label pathLabel = new Label("No image selected");
        Button uploadBtn = new Button("Choose Image");
        uploadBtn.getStyleClass().add("secondary-button");
        uploadBtn.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            java.io.File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                String relativePath = com.travelxp.utils.ImageUtil.saveImage(file);
                if (relativePath != null) {
                    selectedImagePath = relativePath;
                    pathLabel.setText(file.getName());
                }
            }
        });

        content.getChildren().addAll(
            new Label("Username:"), usernameField,
            new Label("Email:"), emailField,
            new Label("Password:"), passwordField,
            new Label("Birthday:"), birthdayPicker,
            new Label("Bio:"), bioArea,
            new Label("Role:"), roleCombo,
            new HBox(15, new VBox(5, new Label("Initial XP:"), xpField), new VBox(5, new Label("Initial Level:"), levelField)),
            new Label("Profile Image:"), new HBox(10, uploadBtn, pathLabel)
        );

        dialog.getDialogPane().setContent(content);

        final Button btCreate = (Button) dialog.getDialogPane().lookupButton(createButtonType);
        btCreate.addEventFilter(ActionEvent.ACTION, event -> {
            String username = usernameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            LocalDate birthday = birthdayPicker.getValue();
            String bio = bioArea.getText();

            if (!validateRegistration(username, email, password, birthday, bio)) {
                event.consume();
            }
            try {
                Integer.parseInt(xpField.getText());
                Integer.parseInt(levelField.getText());
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid Stats", "XP and Level must be numbers.");
                event.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                User user = new User(
                    usernameField.getText(),
                    emailField.getText(),
                    passwordField.getText(),
                    birthdayPicker.getValue(),
                    bioArea.getText(),
                    selectedImagePath
                );
                user.setRole(roleCombo.getValue());
                return user;
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(user -> {
            try {
                if (userService.registerUser(user)) {
                    User registered = userService.getAllUsers().stream()
                            .filter(u -> u.getUsername().equals(user.getUsername()))
                            .findFirst().orElse(null);
                    
                    if (registered != null) {
                        int initialXp = Integer.parseInt(xpField.getText());
                        int initialLevel = Integer.parseInt(levelField.getText());
                        if (initialXp != 0 || initialLevel != 1) {
                            gamificationService.updateGamification(registered.getId(), initialXp, initialLevel);
                        }

                        if ("ADMIN".equals(user.getRole())) {
                            registered.setRole("ADMIN");
                            userService.updateUserAsAdmin(registered);
                        }
                    }
                    loadUsers();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "User Created", "New user has been registered successfully.");
                }
            } catch (SQLException | NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Creation Failed", e.getMessage());
            }
        });
    }

    @FXML
    private void toggleTheme(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        ThemeManager.toggleTheme(stage.getScene());
    }

    private void handleDeleteUser(UserViewModel model) {
        if (model.getId() == Main.getSession().getUser().getId()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Action Denied", "You cannot delete your own admin account.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        ThemeManager.applyThemeToNode(confirm.getDialogPane());
        confirm.setTitle("Delete User");
        confirm.setHeaderText("Are you sure you want to delete " + model.getUsername() + "?");
        confirm.setContentText("This action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (userService.deleteUserAsAdmin(model.getId())) {
                        loadUsers();
                    }
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Delete Failed", e.getMessage());
                }
            }
        });
    }

    private void handleResetPassword(UserViewModel model) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        ThemeManager.applyThemeToNode(dialog.getDialogPane());
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Set a temporary password for " + model.getUsername());
        dialog.setContentText("New Password:");

        dialog.showAndWait().ifPresent(newPassword -> {
            if (newPassword.length() >= 8) {
                try {
                    if (userService.resetPasswordAsAdmin(model.getId(), newPassword)) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Password Reset", "Password updated successfully.");
                    }
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Reset Failed", e.getMessage());
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid Password", "Password must be at least 8 characters.");
            }
        });
    }

    private boolean validateRegistration(String username, String email, String password, LocalDate birthday, String bio) {
        if (username.length() < 3 || username.length() > 50) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Invalid Username", "Username must be between 3 and 50 characters.");
            return false;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Invalid Email", "Please enter a valid email format.");
            return false;
        }
        if (password.length() < 8 || !password.matches(".*[a-zA-Z].*") || !password.matches(".*[0-9].*")) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Invalid Password", "Password must be at least 8 characters long, contain at least one letter and one number.");
            return false;
        }
        if (birthday == null || birthday.isAfter(LocalDate.now())) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Invalid Birthday", "Please select a valid past date.");
            return false;
        }
        if (bio != null && bio.length() > 500) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Invalid Bio", "Bio must be max 500 characters.");
            return false;
        }
        return true;
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
            showAlert(Alert.AlertType.ERROR, "Error", "Scene Error", "Failed to load view: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        ThemeManager.applyThemeToNode(alert.getDialogPane());
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
