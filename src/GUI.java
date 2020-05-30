import com.sun.media.jfxmediaimpl.HostUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.w3c.dom.ls.LSOutput;

import java.awt.event.InputEvent;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class GUI extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    // This needs to be declared up here because it needs to be modified from inside a lambda
    private Label keybindLabel;

    @Override
    public void start(Stage window) {
        window.setTitle("ClickManager 1.0.5");
        window.getIcons().add(new Image("file:AutoClickerLogo.png"));

        // Click speed label
        Label cpsLabel = new Label("Clicks per second:");
        GridPane.setConstraints(cpsLabel, 0, 1);

        // Click speed input
        TextField cpsInput = new TextField();
        GridPane.setConstraints(cpsInput, 2, 1);

        // Activation type label (toggle or hold)
        Label activationTypeLabel = new Label("Activation type:");
        GridPane.setConstraints(activationTypeLabel, 0, 2);

        // Burst input text field
        TextField burstInput = new TextField();
        burstInput.setVisible(false);
        burstInput.setPromptText("Clicks per burst");

        // Activation type selection (toggle or hold)
        ChoiceBox<String> activationTypeSelection = new ChoiceBox<>();
        GridPane.setConstraints(activationTypeSelection, 2, 2);
        activationTypeSelection.getItems().addAll("Toggle", "Hold", "Burst");
        activationTypeSelection.setValue("Toggle");
        activationTypeSelection.setMinWidth(70);
        activationTypeSelection.setOnAction(e -> {
            burstInput.setVisible(activationTypeSelection.getValue().equals("Burst"));
            keybindLabel.setText(activationTypeSelection.getValue() + " auto-clicker KEY:");
        });

        // HBox for activation type selection
        HBox activationTypeInputs = new HBox();
        GridPane.setConstraints(activationTypeInputs, 2, 2);
        activationTypeInputs.getChildren().addAll(activationTypeSelection, burstInput);
        activationTypeInputs.setSpacing(10);

        // Key bind label
         keybindLabel = new Label("Toggle auto-clicker KEY:");
        GridPane.setConstraints(keybindLabel, 0, 3);

        // Key bind button
        Button keybindButton = new Button("[Bind key]");
        keybindButton.setFocusTraversable(false);
        keybindButton.setPrefWidth(70);
        keybindButton.setOnAction(e -> {
            BindKey.display("Toggle key", "Press a key to bind...", keybindButton);
        });

        // Unbind key button
        Button unbindKeyButton = new Button("[Unbind]");
        unbindKeyButton.setFocusTraversable(false);
        unbindKeyButton.setOnAction(e -> {
            keybindButton.setText("[Bind key]");
        });

        // HBox for keybind buttons
        HBox keybindButtons = new HBox();
        GridPane.setConstraints(keybindButtons, 2, 3);
        keybindButtons.setSpacing(10);
        keybindButtons.getChildren().addAll(keybindButton, unbindKeyButton);

        // Left/right mouse button selection label
        Label mouseButtonSelectionLabel = new Label("Select mouse button:");
        GridPane.setConstraints(mouseButtonSelectionLabel, 0, 4);

        // Left/right mouse button selection
        ChoiceBox<String> mouseButtonSelection = new ChoiceBox<>();
        GridPane.setConstraints(mouseButtonSelection, 2, 4);
        mouseButtonSelection.getItems().addAll("Left", "Right");
        mouseButtonSelection.setValue("Left");

        // Launch auto-clicker button
        Button launchAutoClicker = new Button("Launch AutoClicker");
        GridPane.setConstraints(launchAutoClicker, 2, 5);
        launchAutoClicker.setOnAction(e -> {
            try {
                if (cpsInput.getText().equals("")) {
                    throw new EmptyInputException("You must enter a CPS value.");
                } else if (activationTypeSelection.getValue().equals("Burst") && burstInput.getText().equals("")) {
                    throw new EmptyInputException("You must enter a burst value.");
                }
                System.out.println("keybindButton text: " + keybindButton.getText());
                String toggleKey = keybindButton.getText();
                int cps = Integer.parseInt(cpsInput.getText());
                int burst = -1;
                if (activationTypeSelection.getValue().equals("Burst")) {
                    burst = Integer.parseInt(burstInput.getText());
                }
                System.out.println("Clicking at " + cpsInput.getText() + " clicks per second.");
                AutoClickerWindow autoClickerWindow = new AutoClickerWindow(cps, toggleKey, getMouseChoice(mouseButtonSelection), mouseButtonSelection.getValue(), activationTypeSelection.getValue(), burst, window.getX(), window.getY());
                autoClickerWindow.display();
                try {
                    // Disabling logging spam
                    LogManager.getLogManager().reset();
                    Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
                    logger.setLevel(Level.OFF);
                    System.out.println("Registering native hook...");
                    GlobalScreen.registerNativeHook();
                    GlobalScreen.addNativeKeyListener(autoClickerWindow);
                } catch (NativeHookException ex) {
                    System.out.println("NativeHookException");
                    ex.printStackTrace();
                }
            } catch (NumberFormatException exception) {
                AlertBox.display("Invalid Format", "Inputs can only contain numbers.");
            } catch (EmptyInputException exception) {
                AlertBox.display("Invalid Input", exception.getMessage());
            }
        });

        // Exit button
        Button exitButton = new Button("Exit");
        GridPane.setConstraints(exitButton, 2, 6);
        exitButton.setOnAction(e -> closeProgram(window));

        // Clicks recording button
        Button clickRecordButton = new Button("Test Clicks");
        GridPane.setConstraints(clickRecordButton, 0, 6);
        clickRecordButton.setOnAction(e -> {
            ClickRecord clickRecord = new ClickRecord();
            clickRecord.display();
        });

        // GridPane
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);
        grid.getColumnConstraints().add(new ColumnConstraints(130));
        grid.getChildren().addAll(cpsLabel, cpsInput, activationTypeLabel, activationTypeInputs, keybindLabel, keybindButtons, launchAutoClicker, exitButton, mouseButtonSelectionLabel, mouseButtonSelection, clickRecordButton);

        window.setOnCloseRequest(e -> {
            e.consume();
            closeProgram(window);
        });

        Scene scene = new Scene(grid, 350, 230);
        window.setScene(scene);
        window.show();
    }

    private int getMouseChoice(ChoiceBox<String> choiceBox) {
        if (choiceBox.getValue().equals("Left")) {
            return InputEvent.BUTTON1_MASK;
        }
        else if (choiceBox.getValue().equals("Right")) {
            return InputEvent.BUTTON3_MASK;
        }
        return 0;
    }

    private void closeProgram(Stage window) {
        boolean answer = ConfirmBox.display("Confirm Exit", "Are you sure you want to exit?");
        if (answer) {
            window.close();
            Platform.exit();
            System.exit(0);
        }
    }

}
