package com.nb2506.image_editor.controllers;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.nb2506.image_editor.MainApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import com.nb2506.image_editor.utils.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Контроллер для управления камерой и обработкой видеопотока.
 */
public class CameraController {
    @FXML
    private Button MakePhotoButton;

    @FXML
    private ImageView currentFrame;

    protected Stage stage;

    private ScheduledExecutorService timer;
    private final VideoCapture capture = new VideoCapture();
    private boolean cameraActive = false;
    private static final int cameraId = 0;

    /**
     * Инициализация контроллера. Запускает видеозахват, если камера доступна.
     */
    @FXML
    void initialize() {
        if (!this.cameraActive) {
            this.capture.open(cameraId);

            if (this.capture.isOpened()) {
                this.cameraActive = true;

                Runnable frameGrabber = () -> {
                    Mat frame = grabFrame();
                    Image imageToShow = Utils.mat2Image(frame);
                    updateImageView(currentFrame, imageToShow);
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
            } else {
                new MainApplication().createException("Невозможно открыть соединение с камерой...",
                        new Exception("Возможно камера не подключена или используется другим приложением"));
                System.err.println("Невозможно открыть соединение с камерой...");
            }
        }
    }

    /**
     * Действие, выполняемое при нажатии кнопки на GUI.
     *
     * @param event событие нажатия кнопки
     */
    @FXML
    private void MakePhotoButtonClick(ActionEvent event) {
        Image img = currentFrame.getImage();
        FXMLLoader mainView = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        try {
            mainView.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MainController controller = mainView.getController();
        controller.setImageView(img);
        MakePhotoButton.getScene().getWindow().hide();
        this.stopAcquisition();
        Parent root = mainView.getRoot();
        Stage stage = this.stage;
        stage.setScene(new Scene(root, 800, 600));
        stage.setTitle("opencv image editor");
        stage.show();
    }

    /**
     * Получение кадра из открытого видеопотока (если доступен).
     *
     * @return {@link Mat} для отображения
     */
    private Mat grabFrame() {
        Mat frame = new Mat();

        if (this.capture.isOpened()) {
            try {
                this.capture.read(frame);
            } catch (Exception e) {
                new MainApplication().createException("Ошибка при разборе изображения", e);
                System.err.println("Ошибка при разборе изображения " + e);
            }
        }

        return frame;
    }

    /**
     * Остановка захвата камеры и освобождение ресурсов.
     */
    private void stopAcquisition() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                new MainApplication().createException("Ошибка при попытке остановки захвата изображения с камеры", e);
                System.err.println("Ошибка при попытке остановки захвата изображения с камеры " + e);
            }
        }

        if (this.capture.isOpened()) {
            this.capture.release();
        }
    }

    /**
     * Обновление {@link ImageView} в главном потоке JavaFX.
     *
     * @param view элемент {@link ImageView} для обновления
     * @param image изображение {@link Image} для отображения
     */
    private void updateImageView(ImageView view, Image image) {
        Utils.onFXThread(view.imageProperty(), image);
    }

    /**
     * Прекращение съемки с камеры при закрытии приложения.
     */
    protected void Close() {
        this.stopAcquisition();
    }
}
