module gui{
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires org.fxmisc.richtext;
    requires org.apache.commons.io;
    requires java.desktop;
    requires javafx.base;
    requires javafx.graphics;

    opens gui to javafx.controls, javafx.fxml;
    exports gui;
}