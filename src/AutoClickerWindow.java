import javafx.application.Application;
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
    private final int burst;

    public AutoClickerWindow(double cps, String toggleKey, int mouseButtonSelection, String activationTypeSelection, int burst) {
        this.cps = cps;
        this.toggleKey = toggleKey;
        autoclicker = new AutoClicker(cps, this, mouseButtonSelection);
        this.mouseButtonSelection = mouseButtonSelection;
        this.activationTypeSelection = activationTypeSelection;
        this.burst = burst;
    }

    public void display() {
        Stage window = new Stage();

        // window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("AutoClicker");
        window.setMinWidth(250);

        Label label1 = new Label();
        label1.setText("Clicking at " + cps + " clicks per second.");


        // Close window button
        Button closeButton = new Button("Close the window");

        VBox layout = new VBox(10);
        layout.getChildren().addAll(label1, closeButton);
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
    //asdf

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
                }
                else {
                    System.out.println("Starting thread...");
                    autoclicker.startThread(burst);
                    clickerEnabled = true;
                }
            }
        } else if (activationTypeSelection.equals("Hold")) {
            if (NativeKeyEvent.getKeyText(e.getKeyCode()).toLowerCase().equals(toggleKey.toLowerCase())) {
                // Turning on auto-clicker
                if (!clickerEnabled) {
                    System.out.println("Starting thread...");
                    autoclicker.startThread(burst);
                    clickerEnabled = true;
                }
            }
        } else if (activationTypeSelection.equals("Burst")) {
            if (NativeKeyEvent.getKeyText(e.getKeyCode()).toLowerCase().equals(toggleKey.toLowerCase())) {
                // Turning on auto-clicker
                System.out.println("Starting thread...");
                autoclicker.startThread(burst);
                clickerEnabled = true;
            }
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        if (NativeKeyEvent.getKeyText(e.getKeyCode()).toLowerCase().equals(toggleKey.toLowerCase())) {
            if (activationTypeSelection.equals("Hold")) {
                autoclicker.stopThread();
                clickerEnabled = false;
            }
        }
    }

}
