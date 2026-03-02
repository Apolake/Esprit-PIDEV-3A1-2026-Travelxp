package com.travelxp.utils;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.util.Duration;

public class ThemeManager {
    private static boolean isDark = false;

    static {
        // Set initial global stylesheet before any UI loads
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
    }

    public static void toggleTheme(Scene scene) {
        isDark = !isDark;
        applyTheme(scene);
    }

    public static void applyTheme(Scene scene) {
        if (isDark) {
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        } else {
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        }

        if (scene != null && scene.getRoot() != null) {
            Parent root = scene.getRoot();
            root.getStyleClass().remove("dark-theme");
            root.getStyleClass().remove("light-theme");
            root.getStyleClass().add(isDark ? "dark-theme" : "light-theme");

            FadeTransition ft = new FadeTransition(Duration.millis(400), root);
            ft.setFromValue(0.8);
            ft.setToValue(1.0);
            ft.play();
        }
    }

    public static void applyThemeToNode(Parent root) {
        if (root != null) {
            root.getStyleClass().remove("dark-theme");
            root.getStyleClass().remove("light-theme");
            root.getStyleClass().add(isDark ? "dark-theme" : "light-theme");
        }
    }

    public static boolean isDark() {
        return isDark;
    }
}
