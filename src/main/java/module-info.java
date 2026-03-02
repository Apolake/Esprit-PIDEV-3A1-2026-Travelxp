module com.travelxp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires jbcrypt;
    requires atlantafx.base;
    requires org.bytedeco.javacv;
    requires org.bytedeco.javacpp;
    requires org.bytedeco.opencv;
    requires com.google.zxing;
    requires com.google.zxing.javase;

    opens com.travelxp to javafx.graphics, javafx.fxml;
    opens com.travelxp.controllers to javafx.fxml;
    opens com.travelxp.models to javafx.base;

    exports com.travelxp;
    exports com.travelxp.models;
    exports com.travelxp.services;
    exports com.travelxp.controllers;
    exports com.travelxp.utils;
    exports com.travelxp.repositories;
}
