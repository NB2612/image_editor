package com.nb2506.image_editor.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 * Контроллер для отображения информации об исключении.
 */
public class ExceptionController {

    @FXML
    private TextArea exceptionText;

    @FXML
    private Label exceptionLabel;

    /**
     * Обработчик события закрытия окна исключения.
     *
     * @param actionEvent событие закрытия окна
     */
    public void onCloseButtonClick(ActionEvent actionEvent) {
        exceptionLabel.getScene().getWindow().hide();
    }

    /**
     * Установка текста исключения в поле TextArea.
     *
     * @param e исключение для отображения текста
     */
    public void setExceptionText(Exception e) {
        exceptionText.setText(e.getMessage());
    }

    /**
     * Установка текста заголовка исключения в Label.
     *
     * @param label текст заголовка исключения
     */
    public void setExceptionLabel(String label) {
        exceptionLabel.setText(label);
    }
}
