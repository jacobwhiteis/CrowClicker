import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class AutoClickerWindow implements NativeKeyListener {

    private final double cps;
    private final String toggleKey;
    private boolean clickerEnabled = false;
    private final AutoClicker autoclicker;
    private final int mouseButtonSelection;
    private final String activationTypeSelection;
    private final String mouseButtonString;
    private final int burst;
    private Label clickerStatus;
    private final double x;
    private final double y;

    public AutoClickerWindow(double cps, String toggleKey, int mouseButtonSelection, String mouseButtonString, String activationTypeSelection, int burst, double x, double y) {
        this.cps = cps;
        this.toggleKey = toggleKey;
        autoclicker = new AutoClicker(cps, this, mouseButtonSelection, burst);
        this.mouseButtonSelection = mouseButtonSelection;
        this.activationTypeSelection = activationTypeSelection;
        this.burst = burst;
        this.mouseButtonString = mouseButtonString;
        this.x = x;
        this.y = y;
    }

    public void display() {
        Stage window = new Stage();
        window.setX(x);
        window.setY(y);

        // window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("AutoClicker");
        window.setMinWidth(250);

        Label clickerInfo = new Label();
        clickerInfo.setText(mouseButtonString + " mouse at " +  cps + " clicks per second.");

        Label clickerInfo2 = new Label();
        clickerInfo2.setText("Activation Type: " + activationTypeSelection);

        Label clickerInfo3 = new Label();
        clickerInfo3.setText("Key: " + toggleKey);

        VBox clickerInfoVBox = new VBox();
        clickerInfoVBox.getChildren().addAll(clickerInfo, clickerInfo2, clickerInfo3);
        clickerInfoVBox.setAlignment(Pos.CENTER);

        clickerStatus = new Label();
        clickerStatus.setText("Status: DISABLED");

        // Close window button
        Button closeButton = new Button("Close the window");

        VBox layout = new VBox(10);
        layout.getChildren().addAll(clickerInfoVBox, clickerStatus, closeButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20, 20, 20, 20));

        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.show();

        // Run auto-clicker
        autoclicker.run();

        // Handle stopping auto-clicker when closeButton is pressed without user pressing the "stop clicking" button
        closeButton.setOnAction(e -> {
            cleanGlobalScreen();
            autoclicker.stopThread();
            window.close();
        });

        // Handle stopping auto-clicker when window is forcibly closed from the control buttons without user pressing the "stop clicking" button
        window.setOnCloseRequest(e -> {
            cleanGlobalScreen();
            autoclicker.stopThread();
            window.close();
        });
    }

    public void cleanGlobalScreen() {
        try {
            GlobalScreen.unregisterNativeHook();
            GlobalScreen.removeNativeKeyListener(this);
        } catch (NativeHookException nativeHookException) {
            System.out.println("Exception in unregisterHook");
            nativeHookException.printStackTrace();
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        // System.out.println("Key pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (activationTypeSelection.equals("Toggle")) {
            if (NativeKeyEvent.getKeyText(e.getKeyCode()).toLowerCase().equals(toggleKey.toLowerCase())) {
                // Toggling auto-clicker
                if (clickerEnabled) {
                    System.out.println("Stopping thread...");
                    autoclicker.stopThread();
                    clickerEnabled = false;
                    Platform.runLater(() -> {
                        clickerStatus.setText("Status: DISABLED");
                    });
                }
                else {
                    System.out.println("Starting thread...");
                    autoclicker.startThread();
                    clickerEnabled = true;
                    Platform.runLater(() -> {
                        clickerStatus.setText("Status: ENABLED");
                    });
                }
            }
        } else if (activationTypeSelection.equals("Hold")) {
            if (NativeKeyEvent.getKeyText(e.getKeyCode()).toLowerCase().equals(toggleKey.toLowerCase())) {
                // Turning on auto-clicker
                if (!clickerEnabled) {
                    System.out.println("Starting thread...");
                    autoclicker.startThread();
                    clickerEnabled = true;
                    Platform.runLater(() -> {
                        clickerStatus.setText("Status: ENABLED");
                    });
                }
            }
        } else if (activationTypeSelection.equals("Burst")) {
            if (NativeKeyEvent.getKeyText(e.getKeyCode()).toLowerCase().equals(toggleKey.toLowerCase())) {
                if (clickerEnabled) {
                    // Set the counter to 0 in AutoClicker.java
                    autoclicker.restartBurst();
                } else {
                    // Turning on auto-clicker
                    System.out.println("Starting thread...");
                    autoclicker.startThread();
                    clickerEnabled = true;
                    Platform.runLater(() -> {
                        clickerStatus.setText("Status: ENABLED");
                    });
                }
            }
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        if (NativeKeyEvent.getKeyText(e.getKeyCode()).toLowerCase().equals(toggleKey.toLowerCase())) {
            if (activationTypeSelection.equals("Hold")) {
                autoclicker.stopThread();
                clickerEnabled = false;
                Platform.runLater(() -> {
                    clickerStatus.setText("Status: DISABLED");
                });
            }
        }
    }

    public void disableClickerFlag() {
        clickerEnabled = false;
    }

}
