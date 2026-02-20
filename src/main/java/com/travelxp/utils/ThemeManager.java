package com.travelxp.utils;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.animation.FadeTransition;
import javafx.application.Application;
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
        if (isDark) {
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        } else {
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        }

        if (scene != null && scene.getRoot() != null) {
            FadeTransition ft = new FadeTransition(Duration.millis(400), scene.getRoot());
            ft.setFromValue(0.8);
            ft.setToValue(1.0);
            ft.play();
        }
    }

    public static void applyThemeToNode(Parent root) {
        // AtlantaFX handles theme globally via setUserAgentStylesheet
        // We can still use this if we want to add specific classes
    }

    public static boolean isDark() {
        return isDark;
    }
}
