package view;

import javafx.scene.control.Button;
import javafx.scene.text.Font;

public class ButtonStyler {
    public static void styleButton(Button button, String color) {
        button.setFont(Font.loadFont(ButtonStyler.class.getResourceAsStream("/font/NanumGothicBold.ttf"), 18));
        button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 10 20;" +
                        "-fx-font-weight: bold;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 10, 0.5, 0, 2);"
        );
        button.setPrefWidth(150);
    }
}
