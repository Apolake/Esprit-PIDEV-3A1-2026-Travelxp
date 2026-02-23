package com.travelxp.controllers;

import com.travelxp.Main;
import com.travelxp.models.Gamification;
import com.travelxp.models.User;
import com.travelxp.models.UserViewModel;
import com.travelxp.services.GamificationService;
import com.travelxp.services.UserService;
import com.travelxp.utils.ThemeManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    private final UserService userService = new UserService();
    private final GamificationService gamificationService = new GamificationService();
    private final ObservableList<UserViewModel> userData = FXCollections.observableArrayList();

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
        loadUsers();
    }

    private void loadUsers() {
        try {
            userData.clear();
            List<User> users = userService.getAllUsers();
            for (User user : users) {
                Gamification g = gamificationService.getGamificationByUserId(user.getId());
                userData.add(new UserViewModel(user, g));
            }
            userTable.setItems(userData);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Load Failed", e.getMessage());
        }
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

        selectedImagePath = model.getProfileImage();
        Label pathLabel = new Label(selectedImagePath == null || selectedImagePath.isEmpty() ? "No image" : "Image selected");
        Button uploadBtn = new Button("Choose Image");
        uploadBtn.getStyleClass().add("secondary-button");
        uploadBtn.setOnAction(e -> {
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            java.io.File file = fc.showOpenDialog(null);
            if (file != null) {
                selectedImagePath = file.getAbsolutePath();
                pathLabel.setText(file.getName());
            }
        });

        content.getChildren().addAll(
            new Label("Username:"), usernameField,
            new Label("Email:"), emailField,
            new Label("Birthday:"), birthdayPicker,
            new Label("Bio:"), bioArea,
            new Label("Role:"), roleCombo,
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
                    loadUsers();
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Update Failed", e.getMessage());
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
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

        selectedImagePath = "";
        Label pathLabel = new Label("No image selected");
        Button uploadBtn = new Button("Choose Image");
        uploadBtn.getStyleClass().add("secondary-button");
        uploadBtn.setOnAction(e -> {
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            java.io.File file = fc.showOpenDialog(null);
            if (file != null) {
                selectedImagePath = file.getAbsolutePath();
                pathLabel.setText(file.getName());
            }
        });

        content.getChildren().addAll(
            new Label("Username:"), usernameField,
            new Label("Email:"), emailField,
            new Label("Password:"), passwordField,
            new Label("Birthday:"), birthdayPicker,
            new Label("Bio:"), bioArea,
            new Label("Role:"), roleCombo,
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
                    if ("ADMIN".equals(user.getRole())) {
                        User registered = userService.getAllUsers().stream()
                                .filter(u -> u.getUsername().equals(user.getUsername()))
                                .findFirst().orElse(null);
                        if (registered != null) {
                            registered.setRole("ADMIN");
                            userService.updateUserAsAdmin(registered);
                        }
                    }
                    loadUsers();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "User Created", "New user has been registered successfully.");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Creation Failed", e.getMessage());
            }
        });
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
    private void toggleTheme(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        ThemeManager.toggleTheme(stage.getScene());
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
