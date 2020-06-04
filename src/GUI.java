import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.w3c.dom.ls.LSOutput;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * The home GUI window of the autoclicker app. From here, settings such as speed, controls, and bindings can be modified.
 * The autoclicker is also launched from this window by the press of a button.
 *
 * @author Jacob Whiteis
 */
public class GUI extends Application {

    private Label activateBindLabel;

    /**
     * Call start main GUI
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Start the main GUI
     * @param window window
     */
    @Override
    public void start(Stage window) {

        // Window title and icon
        window.setTitle("CrowClicker");
        Image image = new Image("file:resources/AutoClickerLogo.png");
        window.getIcons().add(image);

        // Click speed label
        Label cpsLabel = new Label("Clicks per second:");
        GridPane.setConstraints(cpsLabel, 0, 1);

        // Click speed input
        TextField cpsInput = new TextField();
        GridPane.setConstraints(cpsInput, 2, 1);

        // Burst input text field (only visible when activationTypeLabel is set to "Burst")
        TextField burstInput = new TextField();
        burstInput.setVisible(false);
        burstInput.setPromptText("Clicks per burst");

        // Activation type label (activate or hold)
        Label activationTypeLabel = new Label("Activation type:");
        GridPane.setConstraints(activationTypeLabel, 0, 2);

        // Activation type selection (activate or hold)
        ChoiceBox<String> activationTypeSelection = new ChoiceBox<>();
        GridPane.setConstraints(activationTypeSelection, 2, 2);
        activationTypeSelection.getItems().addAll("Toggle", "Hold", "Burst");
        activationTypeSelection.setValue("Toggle");
        activationTypeSelection.setMinWidth(70);
        activationTypeSelection.setOnAction(e -> {
            burstInput.setVisible(activationTypeSelection.getValue().equals("Burst"));
            activateBindLabel.setText(activationTypeSelection.getValue() + " auto-clicker KEY:");
        });

        // HBox for activation type selection
        HBox activationTypeInputs = new HBox();
        GridPane.setConstraints(activationTypeInputs, 2, 2);
        activationTypeInputs.getChildren().addAll(activationTypeSelection, burstInput);
        activationTypeInputs.setSpacing(10);

        // Activation bind label
        activateBindLabel = new Label("Activation bind:");
        GridPane.setConstraints(activateBindLabel, 0, 3);

        // Activation bind button
        Button activateBindButton = new Button("[Bind key]");
        activateBindButton.setFocusTraversable(false);
        activateBindButton.setPrefWidth(90);
        activateBindButton.setOnAction(e -> {
            BindKey.display("activate key", "Press a key to bind...", activateBindButton);
        });

        // Unbind activation button
        Button unbindActivateButton = new Button("[Unbind]");
        unbindActivateButton.setFocusTraversable(false);
        unbindActivateButton.setOnAction(e -> {
            activateBindButton.setText("[Bind key]");
        });

        // HBox for activation bind buttons
        HBox activateBindButtons = new HBox();
        GridPane.setConstraints(activateBindButtons, 2, 3);
        activateBindButtons.setSpacing(10);
        activateBindButtons.getChildren().addAll(activateBindButton, unbindActivateButton);

        // Lock bind label
        Label lockBindLabel = new Label("Lock bind:");
        GridPane.setConstraints(lockBindLabel, 0, 4);

        // Lock bind button
        Button lockBindButton = new Button("[Bind key]");
        lockBindButton.setFocusTraversable(false);
        lockBindButton.setPrefWidth(90);
        lockBindButton.setOnAction(e -> {
           BindKey.display("Lock key", "Press a key to bind...", lockBindButton);
        });

        // Unbind lock button
        Button unbindLockButton = new Button("[Unbind]");
        unbindLockButton.setFocusTraversable(false);
        unbindLockButton.setOnAction(e -> {
            lockBindButton.setText("[Bind key]");
        });

        // HBox for lock bind buttons
        HBox lockBindButtons = new HBox();
        GridPane.setConstraints(lockBindButtons, 2, 4);
        lockBindButtons.setSpacing(10);
        lockBindButtons.getChildren().addAll(lockBindButton, unbindLockButton);

        // Left/right mouse button selection label
        Label mouseButtonSelectionLabel = new Label("Select mouse button:");
        GridPane.setConstraints(mouseButtonSelectionLabel, 0, 5);

        // Left/right mouse button selection choice box
        ChoiceBox<String> mouseButtonSelection = new ChoiceBox<>();
        GridPane.setConstraints(mouseButtonSelection, 2, 5);
        mouseButtonSelection.getItems().addAll("Left", "Right");
        mouseButtonSelection.setValue("Left");

        // Randomize label
        Label randomizeLabel = new Label("Enabled randomizer");
        GridPane.setConstraints(randomizeLabel, 0, 6);

        // Randomize selection
        CheckBox randomizeCheck = new CheckBox();
        randomizeCheck.setSelected(true);
        GridPane.setConstraints(randomizeCheck, 2, 6);

        // Launch auto-clicker button
        Button launchAutoClicker = new Button("Launch AutoClicker");
        GridPane.setConstraints(launchAutoClicker, 2, 7);
        launchAutoClicker.setOnAction(e -> {
            try {
                if (cpsInput.getText().equals("")) {
                    throw new EmptyInputException("You must enter a CPS value.");
                } else if (activationTypeSelection.getValue().equals("Burst") && burstInput.getText().equals("")) {
                    throw new EmptyInputException("You must enter a burst value.");
                } else if (randomizeCheck.isSelected() && Integer.parseInt(cpsInput.getText()) > 100) {
                    throw new HighClickSpeedException("The clicker is capped at 100 CPS when the randomizer is enabled.\nRead the README for more info.");
                } else if (!randomizeCheck.isSelected() && Integer.parseInt(cpsInput.getText()) > 500) {
                    throw new HighClickSpeedException("The clicker is capped at 500 CPS.\nRead the README for more info.");
                } else if (activateBindButton.getText().equals("[Bind key]")) {
                    throw new NoKeybindException("You need to bind a key to the clicker.");
                } else if (activateBindButton.getText().equals(lockBindButton.getText())) {
                    throw new SameBindException("Use a unique bind for activation and lock.");
                }
                String activateBind = activateBindButton.getText();
                String lockBind = lockBindButton.getText();
                int cps = Integer.parseInt(cpsInput.getText()); // Clicks per second
                int burst = -1;
                if (activationTypeSelection.getValue().equals("Burst")) {
                    burst = Integer.parseInt(burstInput.getText()); // if Burst mode is enabled, burst is set to a positive value. Otherwise, burst = -1
                }
                boolean enableRandomizer = randomizeCheck.isSelected(); // Check whether we should enable the pattern randomizer, or just click consistently
                AutoClickerWindow autoClickerWindow = new AutoClickerWindow(cps, activateBind, lockBind, getMouseChoice(mouseButtonSelection), mouseButtonSelection.getValue(), activationTypeSelection.getValue(), enableRandomizer, burst, window.getX(), window.getY());
                autoClickerWindow.display();
                try {
                    // Disabling logging spam
                    LogManager.getLogManager().reset();
                    Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
                    logger.setLevel(Level.OFF);
                    // Prepare jnativehook's global key listener
                    GlobalScreen.registerNativeHook();
                    GlobalScreen.addNativeKeyListener(autoClickerWindow);
                } catch (NativeHookException ex) {
                    ex.printStackTrace();
                }
            } catch (NumberFormatException exception) {
                AlertBox.display("Invalid Format", "Inputs can only contain numbers.");
            } catch (EmptyInputException exception) {
                AlertBox.display("Invalid Input", exception.getMessage());
            } catch (HighClickSpeedException exception) {
                AlertBox.display("CPS Error", exception.getMessage());
            } catch (NoKeybindException exception) {
                AlertBox.display("No Keybind", exception.getMessage());
            } catch (SameBindException exception) {
                AlertBox.display("Same Keybind", exception.getMessage());
            }
        });

        // Exit button
        Button exitButton = new Button("Exit");
        GridPane.setConstraints(exitButton, 2, 8);
        exitButton.setOnAction(e -> closeProgram(window));

        // On close
        window.setOnCloseRequest(e -> {
            e.consume();
            closeProgram(window);
        });

        // GridPane
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);
        grid.getColumnConstraints().add(new ColumnConstraints(130));
        grid.getChildren().addAll(cpsLabel, cpsInput, activationTypeLabel, activationTypeInputs, activateBindLabel, activateBindButtons, lockBindLabel, lockBindButtons, launchAutoClicker, exitButton, mouseButtonSelectionLabel, mouseButtonSelection, randomizeLabel, randomizeCheck);

        Scene scene = new Scene(grid, 350, 280);
        window.setScene(scene);
        window.show();
    }

    /**
     * Determines which mouse button the user wants the autoclicker to click.
     *
     * @param choiceBox the ChoiceBox for the mousebutton selection
     * @return the integer value of the button down mask selected
     */
    private int getMouseChoice(ChoiceBox<String> choiceBox) {
        if (choiceBox.getValue().equals("Left")) {
            return InputEvent.BUTTON1_DOWN_MASK;
        }
        else if (choiceBox.getValue().equals("Right")) {
            return InputEvent.BUTTON3_DOWN_MASK;
        }
        return 0;
    }

    /**
     * Shows a confirmation box and ensures a clean exit upon request.
     *
     * @param window the window being closed
     */
    private void closeProgram(Stage window) {
        boolean answer = ConfirmBox.display("Confirm Exit", "Are you sure you want to exit?");
        if (answer) {
            window.close();
            Platform.exit();
            System.exit(0);
        }
    }

}
