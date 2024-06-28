module com.nb2506.image_editor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;

    requires opencv;
    requires java.desktop;

    opens com.nb2506.image_editor to javafx.fxml;
    exports com.nb2506.image_editor;
    exports com.nb2506.image_editor.controllers;
    opens com.nb2506.image_editor.controllers to javafx.fxml;
}