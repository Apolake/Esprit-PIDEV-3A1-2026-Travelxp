package com.travelxp.controllers;

import com.travelxp.services.ActivityService;
import com.travelxp.services.TripMilestoneService;
import com.travelxp.services.TripService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.net.URL;

public class MainLayoutController {

    @FXML private StackPane contentHolder;

    @FXML private Button btnTrips;
    @FXML private Button btnActivities;
    @FXML private Button btnMilestones;

    private final TripService tripService = new TripService();
    private final ActivityService activityService = new ActivityService();
    private final TripMilestoneService milestoneService = new TripMilestoneService();

    private static final String APP_CSS = "/css/app.css";

    @FXML
    public void initialize() {
        openTrips();
    }

    @FXML
    public void openTrips() {
        setActive(btnTrips);
        loadIntoCenter("/trip/TripList.fxml");
    }

    @FXML
    public void openActivities() {
        setActive(btnActivities);
        loadIntoCenter("/activity/ActivityList.fxml");
    }

    @FXML
    public void openMilestones() {
        setActive(btnMilestones);
        loadIntoCenter("/milestone/TripMilestoneList.fxml");
    }

    private void loadIntoCenter(String fxmlPath) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                System.out.println("FXML not found: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            // هذا هو ControllerFactory (ليس ملفًا)
            loader.setControllerFactory(type -> {
                try {
                    Object controller = type.getDeclaredConstructor().newInstance();

                    if (controller instanceof TripListController tlc) {
                        tlc.setTripService(tripService);
                    }

                    if (controller instanceof ActivityListController alc) {
                        alc.setTripService(tripService);
                        alc.setActivityService(activityService);
                    }

                    if (controller instanceof TripMilestoneListController mlc) {
                        mlc.setTripService(tripService);
                        mlc.setMilestoneService(milestoneService);
                    }

                    return controller;

                } catch (Exception e) {
                    throw new RuntimeException("ControllerFactory error for: " + type.getName(), e);
                }
            });

            Parent view = loader.load();
            attachCssIfMissing(view);

            contentHolder.getChildren().setAll(view);

            // refresh بعد load فقط (آمن)
            Object c = loader.getController();
            if (c instanceof ActivityListController alc) alc.refresh();
            if (c instanceof TripMilestoneListController mlc) mlc.refresh();
            // TripList يعمل refresh داخل initialize عادةً

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Load error: " + fxmlPath + " -> " + e.getMessage());
        }
    }

    private void attachCssIfMissing(Parent root) {
        URL cssUrl = getClass().getResource(APP_CSS);
        if (cssUrl == null) return;

        String css = cssUrl.toExternalForm();

        if (root.getScene() != null && !root.getScene().getStylesheets().contains(css)) {
            root.getScene().getStylesheets().add(css);
        }
        if (!root.getStylesheets().contains(css)) {
            root.getStylesheets().add(css);
        }
    }

    private void setActive(Button activeBtn) {
        if (btnTrips != null) btnTrips.getStyleClass().remove("active");
        if (btnActivities != null) btnActivities.getStyleClass().remove("active");
        if (btnMilestones != null) btnMilestones.getStyleClass().remove("active");

        if (activeBtn != null && !activeBtn.getStyleClass().contains("active")) {
            activeBtn.getStyleClass().add("active");
        }
    }
}