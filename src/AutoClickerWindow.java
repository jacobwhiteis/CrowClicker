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

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class AutoClickerWindow implements NativeKeyListener {

    private final double cps;
    private final String toggleKey;
    private boolean clickerEnabled = false;
    private final AutoClicker autoclicker;
    private final String activationTypeSelection;
    private final String mouseButtonString;

    private Label clickerStatus;
    private final double x;
    private final double y;

    protected EnumeratedIntegerDistribution distribution = null;

    public AutoClickerWindow(double cps, String toggleKey, int mouseButtonSelection, String mouseButtonString, String activationTypeSelection, boolean enableRandomizer, int burst, double x, double y) {
        // Randomizer
        // Read in probability distribution from file
        int[] singletons = new int[0];
        double[] probabilities = new double[0];
        if (enableRandomizer) {
            String singletonsFileName = "singletons.txt";
            String probabilitiesFileName = "probabilities.txt";
            try {
                File singletonFile = new File(singletonsFileName);
                Scanner input = new Scanner(singletonFile);
                int singleton;
                ArrayList<Integer> singletonsList = new ArrayList<>();
                while (input.hasNextLine()) {
                    singleton = Integer.parseInt(input.nextLine());
                    singletonsList.add(singleton);
                }
                input.close();
                File probabilitiesFile = new File(probabilitiesFileName);
                input = new Scanner(probabilitiesFile);
                double probability;
                double probabilitySum = 0;
                ArrayList<Double> probabilitiesList = new ArrayList<>();
                while (input.hasNextLine()) {
                    probability = Double.parseDouble(input.nextLine());
                    probabilitiesList.add(probability);
                    probabilitySum += probability;
                }
                probabilitiesList.set(0, 0.0);
                singletons = singletonsList.stream().mapToInt(i -> i).toArray();
                probabilities = probabilitiesList.stream().mapToDouble(i -> i).toArray();
                this.distribution = new EnumeratedIntegerDistribution(singletons, probabilities);
            } catch (Exception e) {
                System.out.println("Error getting distribution txt file");
                e.printStackTrace();
            }
        }
        this.cps = cps;
        this.toggleKey = toggleKey;
        autoclicker = new AutoClicker(cps, this, mouseButtonSelection, burst, distribution, enableRandomizer);
        this.activationTypeSelection = activationTypeSelection;
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
        switch (activationTypeSelection) {
            case "Toggle":
                if (NativeKeyEvent.getKeyText(e.getKeyCode()).toLowerCase().equals(toggleKey.toLowerCase())) {
                    // Toggling auto-clicker
                    if (clickerEnabled) {
                        System.out.println("Stopping thread...");
                        autoclicker.stopThread();
                        clickerEnabled = false;
                        Platform.runLater(() -> {
                            clickerStatus.setText("Status: DISABLED");
                        });
                    } else {
                        System.out.println("Starting thread...");
                        autoclicker.startThread();
                        clickerEnabled = true;
                        Platform.runLater(() -> {
                            clickerStatus.setText("Status: ENABLED");
                        });
                    }
                }
                break;
            case "Hold":
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
                break;
            case "Burst":
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
                break;
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
