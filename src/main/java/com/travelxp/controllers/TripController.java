package com.travelxp.controllers;

import com.itextpdf.text.pdf.*;
import com.travelxp.Main;
import com.travelxp.models.Activity;
import com.travelxp.models.Trip;
import com.travelxp.services.*;
import com.travelxp.utils.EmailTemplates;
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
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.itextpdf.text.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileOutputStream;


import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


import java.util.concurrent.CompletableFuture;


import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import javafx.scene.Scene;
import javafx.animation.TranslateTransition;

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

    @FXML private AnchorPane aiDrawer;
    @FXML private StackPane aiDrawerContent;
    //ai

    @FXML private Pane aiOverlay;

    private boolean aiDrawerOpen = false;
    private TripAIPanelController aiPanelController;


    private final TripService tripService = new TripService();
    private final ActivityService activityService = new ActivityService();
    private final UserService userService = new UserService();
    private final ObservableList<Trip> tripData = FXCollections.observableArrayList();
    private final Random random = new Random();

    private boolean isMyTripsMode = false;



    private final WeatherService weatherService = new WeatherService();



    // Dynamic filter UI
    private TextField searchField;
    private ComboBox<String> statusFilterCombo;
    private TextField minBudgetField;
    private TextField maxBudgetField;
    private Button clearFiltersBtn;

    // Filtering engine
    private FilteredList<Trip> filteredTrips;

    private final EmailService emailService = new EmailService();



    private void openTrips(ActionEvent event, boolean myTrips) {
        try {

            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/com/travelxp/views/trip-view.fxml"));

            Parent root = loader.load();

            TripController controller = loader.getController();
            controller.setMyTripsMode(myTrips);

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            Scene scene = stage.getScene();

            if (scene == null) {
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            ThemeManager.applyTheme(scene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

        Platform.runLater(() -> {
            buildAndInjectFilterBar();
            setupFilteringEngine();
            loadTrips(); // keep your loadTrips but we will adjust it
        });

    }


    private void buildAndInjectFilterBar() {

        // 1) Build components
        searchField = new TextField();
        searchField.setPromptText("Search (name / origin / destination / status)");

        statusFilterCombo = new ComboBox<>();
        statusFilterCombo.setPromptText("Status");
        statusFilterCombo.setItems(FXCollections.observableArrayList(
                "ALL", "PLANNED", "ONGOING", "COMPLETED", "CANCELLED"
        ));
        statusFilterCombo.getSelectionModel().select("ALL");

        minBudgetField = new TextField();
        minBudgetField.setPromptText("Min budget");
        minBudgetField.setPrefWidth(110);

        maxBudgetField = new TextField();
        maxBudgetField.setPromptText("Max budget");
        maxBudgetField.setPrefWidth(110);

        clearFiltersBtn = new Button("Clear");
        clearFiltersBtn.getStyleClass().add("secondary-button"); // optional
        clearFiltersBtn.setOnAction(e -> clearFilters());

        // optional: restrict budget fields to numbers (safe)
        minBudgetField.textProperty().addListener((obs, ov, nv) -> {
            if (!nv.matches("\\d*(\\.\\d*)?")) minBudgetField.setText(ov);
        });
        maxBudgetField.textProperty().addListener((obs, ov, nv) -> {
            if (!nv.matches("\\d*(\\.\\d*)?")) maxBudgetField.setText(ov);
        });

        HBox bar = new HBox(10, searchField, statusFilterCombo, minBudgetField, maxBudgetField, clearFiltersBtn);
        bar.setPadding(new Insets(10));
        bar.setStyle("-fx-background-color: transparent;"); // keep your theme clean

        // 2) Inject into layout based on mode
        boolean isAdmin = Main.getSession().getUser().getRole().equals("ADMIN");

        if (isAdmin) {
            // Admin mode: try to place above tripTable
            injectAboveNode(bar, tripTable);
        } else {
            // User mode: place above the scrollPane
            injectAboveNode(bar, userScrollPane);
        }
    }

    private void injectAboveNode(Node bar, Node targetNode) {
        if (targetNode == null) return;

        Parent parent = targetNode.getParent();
        if (parent == null) return;

        if (parent instanceof VBox vbox) {
            int idx = vbox.getChildren().indexOf(targetNode);
            if (idx >= 0) vbox.getChildren().add(idx, bar);
            else vbox.getChildren().add(0, bar);
            return;
        }

        if (parent instanceof BorderPane bp) {
            // If target is center, we can wrap center in VBox
            Node center = bp.getCenter();
            if (center == targetNode) {
                VBox wrapper = new VBox(8, bar, targetNode);
                wrapper.setPadding(new Insets(0));
                bp.setCenter(wrapper);
                return;
            }
        }

        if (parent instanceof AnchorPane ap) {
            // Wrap target in VBox at same anchors (simple)
            VBox wrapper = new VBox(8, bar, targetNode);
            AnchorPane.setTopAnchor(wrapper, AnchorPane.getTopAnchor(targetNode));
            AnchorPane.setBottomAnchor(wrapper, AnchorPane.getBottomAnchor(targetNode));
            AnchorPane.setLeftAnchor(wrapper, AnchorPane.getLeftAnchor(targetNode));
            AnchorPane.setRightAnchor(wrapper, AnchorPane.getRightAnchor(targetNode));

            int idx = ap.getChildren().indexOf(targetNode);
            ap.getChildren().remove(targetNode);
            ap.getChildren().add(idx, wrapper);
            return;
        }

        // Fallback: cannot inject safely
        System.out.println("Could not inject filter bar: unsupported parent layout " + parent.getClass());
    }

    private void setupFilteringEngine() {

        filteredTrips = new FilteredList<>(tripData, t -> true);

        // Admin TableView uses sorted list
        if (tripTable != null) {
            SortedList<Trip> sorted = new SortedList<>(filteredTrips);
            sorted.comparatorProperty().bind(tripTable.comparatorProperty());
            tripTable.setItems(sorted);
        }

        // listeners -> apply filters
        if (searchField != null) searchField.textProperty().addListener((o, a, b) -> applyFilters());
        if (statusFilterCombo != null) statusFilterCombo.valueProperty().addListener((o, a, b) -> applyFilters());
        if (minBudgetField != null) minBudgetField.textProperty().addListener((o, a, b) -> applyFilters());
        if (maxBudgetField != null) maxBudgetField.textProperty().addListener((o, a, b) -> applyFilters());
    }

    private void applyFilters() {
        if (filteredTrips == null) return;

        String q = (searchField != null && searchField.getText() != null)
                ? searchField.getText().trim().toLowerCase()
                : "";

        String status = (statusFilterCombo != null && statusFilterCombo.getValue() != null)
                ? statusFilterCombo.getValue()
                : "ALL";

        Double minBudget = parseDoubleOrNull(minBudgetField != null ? minBudgetField.getText() : null);
        Double maxBudget = parseDoubleOrNull(maxBudgetField != null ? maxBudgetField.getText() : null);

        filteredTrips.setPredicate(trip -> {

            // 1) search
            boolean matchesText = true;
            if (!q.isEmpty()) {
                String name = safe(trip.getTripName());
                String origin = safe(trip.getOrigin());
                String dest = safe(trip.getDestination());
                String st = safe(trip.getStatus());

                matchesText =
                        name.toLowerCase().contains(q) ||
                                origin.toLowerCase().contains(q) ||
                                dest.toLowerCase().contains(q) ||
                                st.toLowerCase().contains(q);
            }

            // 2) status filter
            boolean matchesStatus = true;
            if (status != null && !"ALL".equalsIgnoreCase(status)) {
                matchesStatus = status.equalsIgnoreCase(safe(trip.getStatus()));
            }

            // 3) budget range filter
            double budget = (trip.getBudgetAmount() != null) ? trip.getBudgetAmount() : 0.0;

            boolean matchesBudget = true;
            if (minBudget != null) matchesBudget = budget >= minBudget;
            if (matchesBudget && maxBudget != null) matchesBudget = budget <= maxBudget;

            return matchesText && matchesStatus && matchesBudget;
        });

        refreshUserCardsFromFiltered();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private Double parseDoubleOrNull(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        try { return Double.parseDouble(s); }
        catch (Exception e) { return null; }
    }

    private void refreshUserCardsFromFiltered() {
        if (userTripsContainer == null) return;

        userTripsContainer.getChildren().clear();

        for (Trip t : filteredTrips) {
            userTripsContainer.getChildren().add(createTripCard(t));
        }
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

            // apply filters after loading data
            applyFilters();

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

    private void clearFilters() {
        if (searchField != null) searchField.clear();
        if (statusFilterCombo != null) statusFilterCombo.getSelectionModel().select("ALL");
        if (minBudgetField != null) minBudgetField.clear();
        if (maxBudgetField != null) maxBudgetField.clear();
        applyFilters();
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
        Button aiBtn = new Button("AI Assistant");
        aiBtn.getStyleClass().add("secondary-button");
        aiBtn.setOnAction(e -> openTripAI(t));
        actions.getChildren().add(aiBtn);

        Button exportPdfBtn = new Button("Export PDF");
        exportPdfBtn.getStyleClass().add("export-pdf-btn");
        exportPdfBtn.setMaxWidth(Double.MAX_VALUE);
        exportPdfBtn.setOnAction(e -> handleExportTripPdf(t));
        HBox.setHgrow(exportPdfBtn, Priority.ALWAYS);

        // If you want export to be alone in its own row (cleaner):
        VBox exportRow = new VBox(exportPdfBtn);
        exportRow.setPadding(new Insets(6, 0, 0, 0));


        // --- QR AREA (inside the card) ---
        ImageView cardQrImg = new ImageView();
        cardQrImg.setFitWidth(160);
        cardQrImg.setFitHeight(160);
        cardQrImg.setPreserveRatio(true);
        cardQrImg.setSmooth(true);
        cardQrImg.setVisible(false);
        cardQrImg.setManaged(false); // so it doesn't take space when hidden

        VBox qrBox = new VBox(8);
        qrBox.setPadding(new Insets(10, 0, 0, 0));
        qrBox.setVisible(false);
        qrBox.setManaged(false);

        Label qrLabel = new Label("Trip QR Code");
        qrLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        Button qrBtn = new Button("Show QR");
        qrBtn.getStyleClass().add("secondary-button");
        qrBtn.setMaxWidth(Double.MAX_VALUE);

        qrBtn.setOnAction(e -> {
            try {
                String qrText = buildTripQrText(t);
                String fileName = "trip_qr_" + t.getId() + ".png";
                File qrFile = generateQRCode(qrText, 260, 260, fileName);

                if (qrFile != null && qrFile.exists()) {
                    cardQrImg.setImage(new Image(qrFile.toURI().toString()));

                    qrBox.setVisible(true);
                    qrBox.setManaged(true);
                    cardQrImg.setVisible(true);
                    cardQrImg.setManaged(true);

                    qrBtn.setText("Refresh QR");
                } else {
                    showAlert(Alert.AlertType.ERROR, "QR Code", "Generation Failed", "Could not generate QR Code.");
                }
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "QR Code", "Error", ex.getMessage());
            }
        });

        qrBox.getChildren().addAll(qrLabel, cardQrImg);

        // Then add exportRow to the card instead of adding exportPdfBtn inside actions
        card.getChildren().addAll(nameLab, routeLab, costLab, activitiesBox, actions, exportRow, qrBtn, qrBox);
        return card;
    }
    //ai method
    private void openTripAI(Trip trip) {
        try {
            if (aiPanelController == null) {

                var url = getClass().getResource("/com/travelxp/views/trip-ai-panel.fxml");
                if (url == null) {
                    showAlert(Alert.AlertType.ERROR,
                            "AI Panel",
                            "FXML Not Found",
                            "Could not find: /com/travelxp/views/trip-ai-panel.fxml\n" +
                                    "Make sure the file exists under src/main/resources/com/travelxp/views/");
                    return;
                }

                FXMLLoader loader = new FXMLLoader(url);
                Parent panel = loader.load();
                aiPanelController = loader.getController();

                aiDrawerContent.getChildren().setAll(panel);
            }

            aiPanelController.initWithTrip(trip);
            openAIDrawerAnimated();

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "AI Panel", "Failed to open AI panel", ex.getMessage());
        }
    }

    private void openAIDrawerAnimated() {
        if (aiDrawer == null) return;
        if (aiDrawerOpen) return;

        aiDrawerOpen = true;

        aiOverlay.setVisible(true);
        aiOverlay.setManaged(true);

        aiDrawer.setVisible(true);
        aiDrawer.setManaged(true);
        aiOverlay.setMouseTransparent(false);

        // Start hidden to the right
        aiDrawer.setTranslateX(aiDrawer.getPrefWidth());

        TranslateTransition tt = new TranslateTransition(Duration.millis(220), aiDrawer);
        tt.setToX(0);
        tt.play();
    }

    @FXML
    private void closeAIDrawer() {
        if (aiDrawer == null) return;
        if (!aiDrawerOpen) return;

        aiDrawerOpen = false;

        TranslateTransition tt = new TranslateTransition(Duration.millis(220), aiDrawer);
        tt.setToX(aiDrawer.getPrefWidth());
        tt.setOnFinished(e -> {
            aiDrawer.setVisible(false);
            aiDrawer.setManaged(false);

            aiOverlay.setVisible(false);
            aiOverlay.setManaged(false);
            aiOverlay.setMouseTransparent(true);
        });
        tt.play();
    }
//endai

    private void handleExportTripPdf(Trip trip) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Trip PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        String safeName = (trip.getTripName() == null ? "trip" : trip.getTripName().replaceAll("[\\\\/:*?\"<>|]", "_"));
        fileChooser.setInitialFileName("Trip_" + safeName + ".pdf");

        Stage stage = (Stage) (pageTitle != null ? pageTitle.getScene().getWindow() : null);
        File selectedFile = fileChooser.showSaveDialog(stage);
        if (selectedFile == null) return;

        try {
            // A4 + margins
            Document document = new Document(PageSize.A4, 48, 48, 60, 55);

            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(selectedFile));
            writer.setPageEvent(new TripPdfFooterEvent()); // footer + page number
            document.open();

            // Colors (Luxury + clean)
            BaseColor primary = new BaseColor(17, 24, 39);     // #111827
            BaseColor accent = new BaseColor(212, 175, 55);    // Gold-ish
            BaseColor softGray = new BaseColor(243, 244, 246); // #F3F4F6
            BaseColor border = new BaseColor(229, 231, 235);   // #E5E7EB
            BaseColor headerBg = new BaseColor(17, 24, 39);    // dark header

            // Fonts
            Font h1 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, primary);
            Font h2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, primary);
            Font label = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new BaseColor(75, 85, 99)); // gray-600
            Font value = FontFactory.getFont(FontFactory.HELVETICA, 11, primary);
            Font small = FontFactory.getFont(FontFactory.HELVETICA, 9, new BaseColor(107, 114, 128)); // gray-500

            // Header (brand + title)
            PdfPTable top = new PdfPTable(2);
            top.setWidthPercentage(100);
            top.setWidths(new float[]{70, 30});

            PdfPCell brand = new PdfPCell();
            brand.setBorder(Rectangle.NO_BORDER);
            Paragraph brandP = new Paragraph();
            brandP.add(new Chunk("TravelXP", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, accent)));
            brandP.add(new Chunk("  |  Trip Report", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, primary)));
            brand.addElement(brandP);

            PdfPCell meta = new PdfPCell();
            meta.setBorder(Rectangle.NO_BORDER);
            meta.setHorizontalAlignment(Element.ALIGN_RIGHT);

            String genAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            Paragraph metaP = new Paragraph("Generated: " + genAt, small);
            metaP.setAlignment(Element.ALIGN_RIGHT);
            meta.addElement(metaP);

            top.addCell(brand);
            top.addCell(meta);
            document.add(top);

            // Divider line
            Paragraph line = new Paragraph(" ");
            line.setSpacingBefore(8);
            line.setSpacingAfter(10);
            document.add(line);

            // Trip title
            Paragraph tripTitle = new Paragraph(nvl(trip.getTripName()), h1);
            tripTitle.setSpacingAfter(8f);
            document.add(tripTitle);

            // Route + status line
            Paragraph route = new Paragraph(
                    nvl(trip.getOrigin()) + "  ➜  " + nvl(trip.getDestination()) + "   •   Status: " + nvl(trip.getStatus()),
                    FontFactory.getFont(FontFactory.HELVETICA, 11, new BaseColor(55, 65, 81))
            );
            route.setSpacingAfter(14f);
            document.add(route);

            // Summary box (2 columns)
            PdfPTable summary = new PdfPTable(2);
            summary.setWidthPercentage(100);
            summary.setSpacingAfter(16f);
            summary.setWidths(new float[]{50, 50});

            PdfPCell box = new PdfPCell();
            box.setColspan(2);
            box.setPadding(12f);
            box.setBackgroundColor(softGray);
            box.setBorderColor(border);
            box.setBorderWidth(1f);

            PdfPTable inner = new PdfPTable(2);
            inner.setWidthPercentage(100);
            inner.setWidths(new float[]{35, 65});

            addKV(inner, "Trip Name", nvl(trip.getTripName()), label, value);
            addKV(inner, "Start Date", trip.getStartDate() != null ? trip.getStartDate().toString() : "-", label, value);
            addKV(inner, "End Date", trip.getEndDate() != null ? trip.getEndDate().toString() : "-", label, value);
            addKV(inner, "Entry/Budget", trip.getBudgetAmount() != null ? money(trip.getBudgetAmount()) : "-", label, value);

            if (isMyTripsMode) {
                addKV(inner, "My Total Expenses", money(trip.getTotalExpenses()), label, value);
                addKV(inner, "XP Earned", String.valueOf(trip.getTotalXpEarned()), label, value);
            }

            box.addElement(new Paragraph("Trip Summary", h2));
            box.addElement(new Paragraph(" ", small));
            box.addElement(inner);

            summary.addCell(box);
            document.add(summary);

            // Activities header
            Paragraph actsTitle = new Paragraph("Activities", h2);
            actsTitle.setSpacingAfter(8f);
            document.add(actsTitle);

            List<Activity> activities;
            try {
                activities = activityService.getActivitiesByTripId(trip.getId());
            } catch (SQLException ex) {
                activities = new ArrayList<>();
            }

            if (activities.isEmpty()) {
                Paragraph empty = new Paragraph("No activities found for this trip.", small);
                empty.setSpacingAfter(12f);
                document.add(empty);
            } else {
                PdfPTable actTable = new PdfPTable(4);
                actTable.setWidthPercentage(100);
                actTable.setSpacingBefore(4f);
                actTable.setSpacingAfter(14f);
                actTable.setWidths(new float[]{45, 18, 17, 20});

                addHeaderCellNice(actTable, "Title", headerBg);
                addHeaderCellNice(actTable, "Date", headerBg);
                addHeaderCellNice(actTable, "Cost", headerBg);
                addHeaderCellNice(actTable, "Status", headerBg);

                double totalCost = 0.0;
                int doneCount = 0;

                for (int i = 0; i < activities.size(); i++) {
                    Activity a = activities.get(i);
                    BaseColor rowBg = (i % 2 == 0) ? BaseColor.WHITE : new BaseColor(249, 250, 251); // zebra

                    String titleTxt = nvl(a.getTitle());
                    String dateTxt = a.getActivityDate() != null ? a.getActivityDate().toString() : "-";
                    double costVal = a.getCostAmount() != null ? a.getCostAmount() : 0.0;
                    String costTxt = money(costVal);
                    String statusTxt = nvl(a.getStatus());

                    if ("DONE".equalsIgnoreCase(statusTxt)) doneCount++;
                    totalCost += costVal;

                    addBodyCell(actTable, titleTxt, rowBg);
                    addBodyCell(actTable, dateTxt, rowBg);
                    addBodyCell(actTable, costTxt, rowBg);
                    addBodyCell(actTable, statusTxt, rowBg);
                }

                document.add(actTable);

                // Totals line
                PdfPTable totals = new PdfPTable(3);
                totals.setWidthPercentage(100);
                totals.setWidths(new float[]{34, 33, 33});

                totals.addCell(makeStatCell("Total Activities", String.valueOf(activities.size()), softGray, primary));
                totals.addCell(makeStatCell("Joined (DONE)", String.valueOf(doneCount), softGray, primary));
                totals.addCell(makeStatCell("Activities Total Cost", money(totalCost), softGray, primary));

                document.add(totals);
            }

            document.close();
            showAlert(Alert.AlertType.INFORMATION, "Export", "PDF Generated", "Saved to:\n" + selectedFile.getAbsolutePath());

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Export Error", "PDF Failed", e.getMessage());
        }
    }

    private void addKV(PdfPTable table, String k, String v, Font keyFont, Font valFont) {
        PdfPCell c1 = new PdfPCell(new Phrase(k, keyFont));
        PdfPCell c2 = new PdfPCell(new Phrase(v, valFont));

        c1.setBorder(Rectangle.NO_BORDER);
        c2.setBorder(Rectangle.NO_BORDER);

        c1.setPaddingTop(4f);
        c1.setPaddingBottom(6f);
        c2.setPaddingTop(4f);
        c2.setPaddingBottom(6f);

        table.addCell(c1);
        table.addCell(c2);
    }

    private void addHeaderCellNice(PdfPTable table, String text, BaseColor bg) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(9f);
        cell.setBorderColor(new BaseColor(31, 41, 55));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text, BaseColor bg) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 10, new BaseColor(17, 24, 39));
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(8f);
        cell.setBorderColor(new BaseColor(229, 231, 235));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private PdfPCell makeStatCell(String label, String value, BaseColor bg, BaseColor textColor) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bg);
        cell.setBorderColor(new BaseColor(229, 231, 235));
        cell.setPadding(10f);

        Font f1 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new BaseColor(107, 114, 128));
        Font f2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, textColor);

        Paragraph p = new Paragraph();
        p.add(new Chunk(label + "\n", f1));
        p.add(new Chunk(value, f2));

        cell.addElement(p);
        return cell;
    }

    private String money(double val) {
        DecimalFormat df = new DecimalFormat("$0.00");
        return df.format(val);
    }

    private String nvl(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private static class TripPdfFooterEvent extends PdfPageEventHelper {
        private final Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new BaseColor(107, 114, 128));

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();

            String left = "TravelXP";
            String right = "Page " + writer.getPageNumber();

            float y = document.bottom() - 18;

            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase(left, footerFont),
                    document.left(), y, 0);

            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase(right, footerFont),
                    document.right(), y, 0);
        }
    }





    // QRCode Generation Functions


    private String buildTripQrText(Trip trip) {
        return "TravelXP - Trip\n"
                + "Trip ID: " + trip.getId() + "\n"
                + "Name: " + nvl(trip.getTripName()) + "\n"
                + "Route: " + nvl(trip.getOrigin()) + " -> " + nvl(trip.getDestination()) + "\n"
                + "Start: " + (trip.getStartDate() != null ? trip.getStartDate() : "-") + "\n"
                + "End: " + (trip.getEndDate() != null ? trip.getEndDate() : "-") + "\n"
                + "Status: " + nvl(trip.getStatus());
    }

    public static File generateQRCode(String data, int width, int height, String fileName) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1); // nicer QR (small white margin)

            BitMatrix bitMatrix = new MultiFormatWriter()
                    .encode(data, BarcodeFormat.QR_CODE, width, height, hints);

            Path path = Paths.get(System.getProperty("java.io.tmpdir"), fileName);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

            return path.toFile();
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    //End QRCode


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


        showDestinationWeatherThenOpenDialog(template);
    }


    private void showDestinationWeatherThenOpenDialog(Trip template) {
        String destination = template.getDestination();

        // Optional: if destination is empty, just continue
        if (destination == null || destination.isBlank()) {
            openParticipateDialog(template);
            return;
        }

        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return weatherService.getCurrentWeatherByCity(destination);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .thenAccept(weather -> Platform.runLater(() -> {
                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setTitle("Destination Weather");
                    a.setHeaderText("Current weather in " + weather.location);

                    String content =
                            "Condition: " + weather.condition + "\n" +
                                    "Temperature: " + String.format("%.1f", weather.temperatureC) + " °C\n" +
                                    "Wind: " + String.format("%.1f", weather.windKmh) + " km/h\n" +
                                    "Code: " + weather.weatherCode;

                    a.setContentText(content);

                    a.showAndWait(); // after OK -> continue
                    openParticipateDialog(template);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        // if weather fails, do NOT block the participate flow
                        showAlert(Alert.AlertType.WARNING,
                                "Weather",
                                "Could not fetch weather",
                                "Destination: " + destination + "\nReason: " + ex.getCause().getMessage());

                        openParticipateDialog(template);
                    });
                    return null;
                });
    }


    private void openParticipateDialog(Trip template) {
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


                //send_SMS();


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

                String userEmail = Main.getSession().getUser().getEmail(); // make sure this exists
                String fullName = Main.getSession().getUser().getUsername(); // or username

                String subject = "TravelXP - Participation Confirmed";
                String body = EmailTemplates.tripConfirmationHtml(fullName, template, totalCost);

                CompletableFuture.runAsync(() -> {
                    try {
                        emailService.sendEmail(userEmail, subject, body);
                        Platform.runLater(() ->
                                showAlert(Alert.AlertType.INFORMATION, "Email", "Confirmation Sent", "Email sent to: " + userEmail)
                        );
                    } catch (Exception ex) {
                        Platform.runLater(() ->
                                showAlert(Alert.AlertType.WARNING, "Email", "Could not send email",
                                        "Participation succeeded, but email failed.\nReason: " + ex.getMessage())
                        );
                    }
                });

            } catch (SQLException e) { showAlert(Alert.AlertType.ERROR, "Error", "Failed to join", e.getMessage()); }
        });
    }

    void send_SMS(){
        // Initialisation de la bibliothèque Twilio avec les informations de votre compte
        String ACCOUNT_SID = "AC784cfbd82e1605c281e334ef318b2dad";
        String AUTH_TOKEN = "c5c65a010e4c1623e22f9c7e9174d836";

        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        String recipientNumber = "+21655206757";
        String message = "Dear Customer,\n\n"
                + "We are pleased to inform you that your participation in the selected trip has been successfully confirmed.\n"
                + "Your booking and selected activities have been added to your account.\n\n"
                + "If you need any further information or assistance, please contact the administration.\n\n"
                + "Thank you for choosing TravelXP. We look forward to providing you with an amazing experience.\n\n"
                + "Best regards,\n"
                + "The TravelXP Team";

        com.twilio.rest.api.v2010.account.Message twilioMessage = Message.creator(
                new com.twilio.type.PhoneNumber(recipientNumber),
                new PhoneNumber("+17164033215"),message).create();

        System.out.println("SMS envoyé : " + twilioMessage.getSid());
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
    private void handleBrowseTrips(ActionEvent event) {
        openTrips(event, false);
    }

    @FXML
    private void handleMyTrips(ActionEvent event) {
        openTrips(event, true);
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

    @FXML private void handleMyBookings(ActionEvent event) {
        changeScene(event, Main.getSession().getUser().getRole().equals("ADMIN") ? "/com/travelxp/views/admin-booking-view.fxml" : "/com/travelxp/views/booking-view.fxml");
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