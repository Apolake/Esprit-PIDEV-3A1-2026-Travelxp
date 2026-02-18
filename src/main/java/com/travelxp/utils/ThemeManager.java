package com.travelxp.utils;

import javafx.animation.FadeTransition;
import javafx.scene.Scene;
import javafx.util.Duration;

public class ThemeManager {
    private static boolean isDark = true;

    public static void toggleTheme(Scene scene) {
        isDark = !isDark;
        applyTheme(scene);
    }

    public static void applyTheme(Scene scene) {
        // Simple fade-in effect when theme changes
        FadeTransition ft = new FadeTransition(Duration.millis(400), scene.getRoot());
        ft.setFromValue(0.8);
        ft.setToValue(1.0);
        ft.play();

        if (isDark) {
            scene.getRoot().getStyleClass().remove("light-theme");
        } else {
            if (!scene.getRoot().getStyleClass().contains("light-theme")) {
                scene.getRoot().getStyleClass().add("light-theme");
            }
        }
    }

    public static boolean isDark() {
        return isDark;
    }
}
