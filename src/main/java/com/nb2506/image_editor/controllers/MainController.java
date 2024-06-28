package com.nb2506.image_editor.controllers;

import com.nb2506.image_editor.MainApplication;
import com.nb2506.image_editor.utils.Utils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Контроллер главного окна приложения для редактирования изображений.
 */
public class MainController {

    @FXML
    private RadioButton bChannel;

    @FXML
    private TextField borderSizeArea;

    @FXML
    private ToggleGroup channel;

    @FXML
    private RadioButton gChannel;

    @FXML
    private ImageView imageView;

    @FXML
    private RadioButton rChannel;

    @FXML
    private TextField startXArea;

    @FXML
    private TextField startYArea;

    @FXML
    private TextField endXArea;

    @FXML
    private TextField endYArea;

    @FXML
    private TextField thicknessArea;

    private Mat origImage = null;

    /**
     * Установка изображения в ImageView.
     *
     * @param img изображение для отображения
     */
    public void setImageView(Image img){
        assert (img != null);
        imageView.setFitHeight(img.getHeight());
        imageView.setFitWidth(img.getWidth());
        imageView.setImage(img);
        origImage = Utils.img2Mat(img);
    }

    @FXML
    void initialize() { }

    @FXML
    private void onClearMenuItemClick() {
        imageView.setImage(null);
        origImage = null;
    }

    @FXML
    void onApplyLineButtonClick() {
        Mat inputImage = origImage;
        int startX = 0, startY = 0, endX = 0, endY = 0, thickness = 0;
        try {
            startX = Integer.parseInt(startXArea.getText());
            startY = Integer.parseInt(startYArea.getText());
            endX = Integer.parseInt(endXArea.getText());
            endY = Integer.parseInt(endYArea.getText());
            thickness = Integer.parseInt(thicknessArea.getText());
        } catch (Exception e){
            new MainApplication().createException("Ошибка ",
                    new Exception("Введен некорректный символ: \n" + e.getMessage()));
        }

        Imgproc.line(inputImage, new Point(startX,startY), new Point(endX,endY), new Scalar(0, 255, 0), thickness);
        imageView.setImage(Utils.mat2Image(inputImage));
    }

    @FXML
    void onBorderApplyButtonClick() {
        int borderSize = 0;
        try {
            borderSize = Integer.parseInt(this.borderSizeArea.getText());
        } catch (Exception e){
            new MainApplication().createException("Ошибка ",
                    new Exception("Введен некорректный символ: \n" + e.getMessage()));
            System.err.println("Ошибка: " + e);
        }
        Mat borderedImage = new Mat();
        try {
            Core.copyMakeBorder(origImage, borderedImage, borderSize, borderSize, borderSize, borderSize,
                    Core.BORDER_CONSTANT, new Scalar(0, 0, 0));
        } catch (Exception e){
            new MainApplication().createException("Объект для редактирования не найден", e);
            System.err.println("Объект для редактирования не найден: " + e.getMessage());
        }
        assert (borderedImage != null);

        imageView.setImage(Utils.mat2Image(borderedImage));
    }

    @FXML
    void onCreateImageButtonClick() {
        imageView.getScene().getWindow().hide();
        FXMLLoader cameraView = new FXMLLoader(MainApplication.class.getResource("camera-view.fxml"));
        try {
            cameraView.load();
        } catch (IOException e) {
            new MainApplication().createException("Ошибка", e);
            throw new RuntimeException(e);
        }
        Parent root = cameraView.getRoot();
        Stage stage = new Stage();
        stage.setScene(new Scene(root,800,600));
        stage.setTitle("opencv image editor");
        stage.show();
        CameraController cameraController = cameraView.getController();
        cameraController.stage = (Stage) imageView.getScene().getWindow();
        stage.setOnCloseRequest((we -> {
            cameraController.Close();
            try {
                new MainApplication().start(new Stage());
            } catch (IOException e) {
                new MainApplication().createException("Ошибка", e);
            }
        }));
    }

    @FXML
    private void onOpenImageButtonClick() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("ImageFiles (*.jpg, *.jpeg, *.png)",
                "*.jpg", "*.jpeg", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(new Stage());
        try {
            Image img = new Image(file.toString());
            setImageView(img);
        } catch (Exception ignored){}
    }

    @FXML
    private void onColorConfirmButtonClick() {
        Mat img = origImage;
        if (rChannel.isSelected()) img = getSpecificChannelMat(2);
        if (gChannel.isSelected()) img = getSpecificChannelMat(1);
        if (bChannel.isSelected()) img = getSpecificChannelMat(0);

        imageView.setImage(Utils.mat2Image(img));
    }

    /**
     * Получение матрицы изображения для конкретного цветового канала.
     *
     * @param channel индекс цветового канала: 0 - синий, 1 - зеленый, 2 - красный
     * @return матрица изображения для выбранного канала
     */
    private Mat getSpecificChannelMat(int channel) {
        Mat currentImage = origImage;

        List<Mat> channels = new ArrayList<>();
        Core.split(currentImage, channels);

        Mat emptyChannel = Mat.zeros(channels.get(0).size(), channels.get(0).type());
        List<Mat> singleChannelList = switch (channel) {
            case 0 -> List.of(channels.get(0), emptyChannel, emptyChannel);
            case 1 -> List.of(emptyChannel, channels.get(1), emptyChannel);
            case 2 -> List.of(emptyChannel, emptyChannel, channels.get(2));
            default -> List.of(emptyChannel, emptyChannel, emptyChannel);
        };

        Mat resultMat = new Mat();
        Core.merge(singleChannelList, resultMat);
        return resultMat;
    }

    @FXML
    private void onColorGrayButtonClick() {
        Mat img = origImage;
        Mat grayImage = new Mat(img.size(), CvType.CV_8UC1);
        Imgproc.cvtColor(img, grayImage, Imgproc.COLOR_BGR2GRAY);
        imageView.setImage(Utils.mat2Image(grayImage));
    }
}
