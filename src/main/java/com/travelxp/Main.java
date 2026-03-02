package com.travelxp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

public class Main extends Application {

    private static UserSession session;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/travelxp/views/login.fxml"));
        Parent root = loader.load();
        
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        
        primaryStage.setTitle("TravelXP - Travel like a pro");
        primaryStage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/image.png")));
        primaryStage.setResizable(true);
        primaryStage.setScene(scene);
        com.travelxp.utils.ThemeManager.applyTheme(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void setSession(UserSession session) {
        Main.session = session;
    }

    public static UserSession getSession() {
        return session;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
