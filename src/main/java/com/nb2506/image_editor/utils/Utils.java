package com.nb2506.image_editor.utils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;

import com.nb2506.image_editor.MainApplication;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritablePixelFormat;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 * Предоставляет универсальные методы для обработки данных OpenCV-JavaFX.
 * Кроме того, содержит некоторые "низкоуровневые" методы для обеспечения
 * соответствия поведения JavaFX.
 */
public final class Utils {
    /**
     * Преобразует объект Mat (OpenCV) в соответствующий объект Image для JavaFX
     *
     * @param frame объект {@link Mat}, представляющий текущий кадр
     * @return объект {@link Image} для отображения
     */
    public static Image mat2Image(Mat frame) {
        try {
            return SwingFXUtils.toFXImage(matToBufferedImage(frame), null);
        } catch (Exception e) {
            new MainApplication().createException("Невозможно конвертировать объект Mat: ", e);
            System.err.println("Невозможно конвертировать объект Mat: " + e);
            return null;
        }
    }

    /**
     * Универсальный метод для выполнения задач, работающих не в потоке JavaFX,
     * в потоке JavaFX, чтобы корректно обновить пользовательский интерфейс
     *
     * @param property свойство {@link ObjectProperty}
     * @param value значение для установки данного {@link ObjectProperty}
     */
    public static <T> void onFXThread(final ObjectProperty<T> property, final T value) {
        Platform.runLater(() -> {
            property.set(value);
        });
    }

    /**
     * Поддержка метода преобразования объекта Mat в BufferedImage
     *
     * @param original объект {@link Mat} в формате BGR или градациях серого
     * @return соответствующий объект {@link BufferedImage}
     */
    private static BufferedImage matToBufferedImage(Mat original) {
        // инициализация
        BufferedImage image = null;
        int width = original.width(), height = original.height(), channels = original.channels();
        byte[] sourcePixels = new byte[width * height * channels];
        original.get(0, 0, sourcePixels);

        if (original.channels() > 1) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        } else {
            image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        }
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

        return image;
    }

    /**
     * Преобразует объект Image (JavaFX) в соответствующий объект Mat для OpenCV
     *
     * @param img объект {@link Image}
     * @return объект {@link Mat}
     */
    public static Mat img2Mat(Image img) {
        Mat matImg = new Mat();
        try {
            matImg = bufferedImageToMat(toBufferedImage(img));
        } catch (Exception e) {
            new MainApplication().createException("Ошибка при конвертации изображения: ", e);
            System.err.println("Ошибка при конвертации изображения: " + e);
        }
        return matImg;
    }

    /**
     * Преобразует объект Image (JavaFX) в BufferedImage
     *
     * @param img объект {@link Image}
     * @return объект {@link BufferedImage}
     */
    private static BufferedImage toBufferedImage(Image img) {
        int width = (int) img.getWidth();
        int height = (int) img.getHeight();
        BufferedImage bimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        PixelReader pixelReader = img.getPixelReader();
        int[] buffer = new int[width * height];
        pixelReader.getPixels(0, 0, width, height, WritablePixelFormat.getIntArgbInstance(), buffer, 0, width);
        bimage.setRGB(0, 0, width, height, buffer, 0, width);
        return bimage;
    }

    /**
     * Преобразует объект BufferedImage в Mat для OpenCV
     *
     * @param bi объект {@link BufferedImage}
     * @return объект {@link Mat}
     */
    private static Mat bufferedImageToMat(BufferedImage bi) {
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        if (bi.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
            mat.put(0, 0, data);
        } else if (bi.getType() == BufferedImage.TYPE_INT_RGB || bi.getType() == BufferedImage.TYPE_INT_ARGB) {
            int[] data = ((DataBufferInt) bi.getRaster().getDataBuffer()).getData();
            byte[] byteData = new byte[data.length * 3];
            for (int i = 0; i < data.length; i++) {
                byteData[i * 3] = (byte) ((data[i] >> 16) & 0xFF); // Красный
                byteData[i * 3 + 1] = (byte) ((data[i] >> 8) & 0xFF); // Зеленый
                byteData[i * 3 + 2] = (byte) (data[i] & 0xFF); // Синий
            }
            mat.put(0, 0, byteData);
        }
        return mat;
    }
}