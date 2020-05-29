import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class BindKey {

    public static KeyCode display(String title, String message, Button bindButton) {
        Stage window = new Stage();
        KeyCode[] keyCode = {null};

        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);
        window.setMinWidth(250);
        window.setMinHeight(100);

        // Message label
        Label label1 = new Label();
        label1.setText(message);

        // Cancel and exit button
        Button cancelButton = new Button("Cancel");
        cancelButton.setFocusTraversable(false);
        cancelButton.setOnAction(e -> window.close());

        VBox layout = new VBox(10);
        layout.getChildren().addAll(label1, cancelButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20, 20, 20, 20));

        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.show();

        scene.setOnKeyPressed( e-> {
            bindButton.setText(String.valueOf(e.getCode()));
            window.close();
            keyCode[0] = e.getCode();
        });

        return keyCode[0];
    }

}
