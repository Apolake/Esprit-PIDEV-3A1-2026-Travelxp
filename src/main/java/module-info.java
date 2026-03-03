module com.travelxp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;
    requires atlantafx.base;
    requires itextpdf;
    requires twilio;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires jakarta.mail;
    requires com.google.gson;
    requires okhttp3;

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
