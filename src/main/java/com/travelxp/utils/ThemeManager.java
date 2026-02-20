package com.travelxp.utils;

import javafx.animation.FadeTransition;
import javafx.scene.Parent;
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

        applyThemeToNode(scene.getRoot());
    }

    public static void applyThemeToNode(Parent root) {
        if (isDark) {
            root.getStyleClass().remove("light-theme");
        } else {
            if (!root.getStyleClass().contains("light-theme")) {
                root.getStyleClass().add("light-theme");
            }
        }
    }

    public static boolean isDark() {
        return isDark;
    }
}
