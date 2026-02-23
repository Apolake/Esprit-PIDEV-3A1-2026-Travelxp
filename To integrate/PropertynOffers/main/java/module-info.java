module com.travelxp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;

    opens com.travelxp to javafx.graphics, javafx.fxml;
    opens com.travelxp.controllers to javafx.fxml;
    opens com.travelxp.models to javafx.base;

    exports com.travelxp;
    exports com.travelxp.models;
    exports com.travelxp.services;
    exports com.travelxp.controllers;
    exports com.travelxp.utils;
}
