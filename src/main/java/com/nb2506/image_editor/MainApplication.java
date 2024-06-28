package com.nb2506.image_editor;

import com.nb2506.image_editor.controllers.ExceptionController;
import com.nb2506.image_editor.controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.opencv.core.Core;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        stage.setTitle("opencv image editor");
        stage.setScene(scene);
        stage.show();
    }

    public void createException(String label, Exception e){
        FXMLLoader mainView = new FXMLLoader(MainApplication.class.getResource("exception-view.fxml"));
        try {
            mainView.load();
        } catch (IOException eIO) {
            throw new RuntimeException(eIO);
        }
        Parent root = mainView.getRoot();
        Stage stage = new Stage();
        stage.setScene(new Scene(root,320, 200));
        stage.setTitle("Ошибка");
        stage.setResizable(false);
        ExceptionController controller = mainView.getController();
        controller.setExceptionLabel(label);
        controller.setExceptionText(e);
        stage.showAndWait();
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }
}