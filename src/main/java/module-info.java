module com.travelxp {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive javafx.web;
    requires transitive java.sql;
    requires jbcrypt;
    requires java.net.http;
    requires com.google.gson;

    opens com.travelxp to javafx.graphics, javafx.fxml;
    opens com.travelxp.controllers to javafx.fxml;
    opens com.travelxp.models to javafx.base;

    exports com.travelxp;
    exports com.travelxp.models;
    exports com.travelxp.services;
    exports com.travelxp.controllers;
    exports com.travelxp.utils;
}
