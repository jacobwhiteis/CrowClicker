import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * A window that is paired with an instance of AutoClicker.
 * This window listens for global key events and takes action when the toggle key is pushed.
 * When this window is closed, the autoclicker is disabled.
 *
 * @author Jacob Whiteis
 */
public class AutoClickerWindow implements NativeKeyListener {

    private final double cps;
    private final double x;
    private final double y;
    private final String activateBind;
    private final String lockBind;
    private final String activationTypeSelection;
    private final String mouseButtonString;
    private boolean clickerEnabled = false;
    private boolean clickerLocked = false;
    private final AutoClicker autoclicker;
    private Label clickerStatus;

    // Probability distribution for the click intervals
    protected EnumeratedIntegerDistribution distribution = null;

    /**
     * Constructor that reads in probability distribution stuff from text files.
     * @param cps clicks per second
     * @param activateBind the key binded to the clicker
     * @param mouseButtonSelection the mouse button that will be clicked (int)
     * @param mouseButtonString the mouse button that will be clicked (string)
     * @param activationTypeSelection activation type
     * @param enableRandomizer whether the randomizer is enabled
     * @param burst the number of clicks in a burst (-1 if no burst)
     * @param x x coord of previous window
     * @param y y coord of previous window
     */
    public AutoClickerWindow(double cps, String activateBind, String lockBind, int mouseButtonSelection, String mouseButtonString, String activationTypeSelection, boolean enableRandomizer, int burst, double x, double y) {
        // Read in probability distribution from file
        int[] singletons = new int[0];
        double[] probabilities = new double[0];
        if (enableRandomizer) {
            singletons = readSingletons();
            probabilities = readProbabilities();
            this.distribution = new EnumeratedIntegerDistribution(singletons, probabilities);
        }
        this.cps = cps;
        this.activateBind = activateBind;
        this.lockBind = lockBind;
        autoclicker = new AutoClicker(cps, this, mouseButtonSelection, burst, distribution, enableRandomizer);
        this.activationTypeSelection = activationTypeSelection;
        this.mouseButtonString = mouseButtonString;
        this.x = x;
        this.y = y;
    }

    /**
     * Display the window and set up the autoclicker.
     */
    public void display() {
        Stage window = new Stage();
        window.setX(x);
        window.setY(y);

        // Window title
        window.setTitle("AutoClicker");
        window.setMinWidth(250);

        // Clicker info
        Label clickerInfo = new Label();
        clickerInfo.setText(mouseButtonString + " mouse at " +  cps + " clicks per second.");
        Label clickerInfo2 = new Label();
        clickerInfo2.setText("Activation Type: " + activationTypeSelection);
        Label clickerInfo3 = new Label();
        clickerInfo3.setText("Key: " + activateBind);
        VBox clickerInfoVBox = new VBox();
        clickerInfoVBox.getChildren().addAll(clickerInfo, clickerInfo2, clickerInfo3);
        clickerInfoVBox.setAlignment(Pos.CENTER);
        clickerStatus = new Label();
        clickerStatus.setText("Status: DISABLED");

        // Exit autoclicker
        Button closeButton = new Button("Close autoclicker");

        VBox layout = new VBox(10);
        layout.getChildren().addAll(clickerInfoVBox, clickerStatus, closeButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20, 20, 20, 20));

        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.show();

        // Close the window and stop the autoclicker
        closeButton.setOnAction(e -> {
            cleanGlobalScreen();
            autoclicker.stopThread();
            window.close();
        });

        window.setOnCloseRequest(e -> {
            cleanGlobalScreen();
            autoclicker.stopThread();
            window.close();
        });
    }

    /**
     * Read probabilities from text file into array.
     * @return array of probabilities
     */
    private double[] readProbabilities() {
        ArrayList<Double> probabilitiesList = new ArrayList<>();
        String probabilitiesFileName = "resources/probabilities.txt";
        try {
            File probabilitiesFile = new File(probabilitiesFileName);
            Scanner input = new Scanner(probabilitiesFile);
            double probability;
            while (input.hasNextLine()) {
                probability = Double.parseDouble(input.nextLine());
                probabilitiesList.add(probability);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return probabilitiesList.stream().mapToDouble(i -> i).toArray();
    }

    /**
     * Read singletons from text file into array.
     * @return array of singletons
     */
    private int[] readSingletons() {
        ArrayList<Integer> singletonsList = new ArrayList<>();
        String singletonsFileName = "resources/singletons.txt";
        try {
            File singletonFile = new File(singletonsFileName);
            Scanner input = new Scanner(singletonFile);
            int singleton;
            while (input.hasNextLine()) {
                singleton = Integer.parseInt(input.nextLine());
                singletonsList.add(singleton);
            }
            input.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return singletonsList.stream().mapToInt(i -> i).toArray();
    }

    /**
     * Unregister the native hook and remove this window as a key listener.
     * Necessary to stop reading key inputs and ensure the key listener starts up properly next time.
     */
    public void cleanGlobalScreen() {
        try {
            GlobalScreen.unregisterNativeHook();
            GlobalScreen.removeNativeKeyListener(this);
        } catch (NativeHookException nativeHookException) {
            System.out.println("Exception in unregisterHook");
            nativeHookException.printStackTrace();
        }
    }

    /**
     * Unused
     * @param e key event
     */
    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
    }

    /**
     * Handles the "key down" event
     * @param e key event
     */
    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        // Determine whether this is an ACTIVATE or a LOCK
        if (NativeKeyEvent.getKeyText(e.getKeyCode()).toLowerCase().equals(activateBind.toLowerCase()) && !clickerLocked) {
            determinePressedActionAndExecute();
        }
        else if (NativeKeyEvent.getKeyText(e.getKeyCode()).toLowerCase().equals(lockBind.toLowerCase())) {
            clickerLocked = !clickerLocked;
            System.out.println("clickerLocked: " + clickerLocked);
        }
    }

    /**
     * Handles the "key up" event.
     * @param e key event
     */
    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        if (NativeKeyEvent.getKeyText(e.getKeyCode()).toLowerCase().equals(activateBind.toLowerCase()) && !clickerLocked) {
            determineReleasedActionAndExecute();
        }
    }

    /**
     * This method exists so that an instance of AutoClicker can signal to this window that
     * it has stopped in the case of a burst ending, since this window does not keep track of when a burst ends.
     */
    public void disableClickerFlag() {
        clickerEnabled = false;
    }

    /**
     * Is called when we know that the autoclicker will be modified after a key or mousebutton is PRESSED.
     * Determines the activation type and subsequently what we should be doing, whether that's starting, stopping, or restarting the autoclicker.
     */
    private void determinePressedActionAndExecute() {
        switch (activationTypeSelection) {
            case "Toggle":
                // Toggling auto-clicker
                if (clickerEnabled) {
                    autoclicker.stopThread();
                    clickerEnabled = false;
                    Platform.runLater(() -> {
                        clickerStatus.setText("Status: DISABLED");
                    });
                } else {
                    autoclicker.startThread();
                    clickerEnabled = true;
                    Platform.runLater(() -> {
                        clickerStatus.setText("Status: ENABLED");
                    });
                }
                break;
            case "Hold":
                // Turning on auto-clicker
                if (!clickerEnabled) {
                    autoclicker.startThread();
                    clickerEnabled = true;
                    Platform.runLater(() -> {
                        clickerStatus.setText("Status: ENABLED");
                    });
                }
                break;
            case "Burst":
                if (clickerEnabled) {
                    // If there is an ongoing burst (clickerEnabled),
                    // we cancel the current burst and start a new one
                    autoclicker.restartBurst();
                } else {
                    // Turning on auto-clicker
                    autoclicker.startThread();
                    clickerEnabled = true;
                    Platform.runLater(() -> {
                        clickerStatus.setText("Status: ENABLED");
                    });
                }
                break;
        }
    }

    /**
     * Is called when we know that the autoclicker will be modified after a key or mousebutton is RELEASED.
     * Determines the activation type and subsequently whether we should be stopping the autoclicker.
     */
    private void determineReleasedActionAndExecute() {
        if (activationTypeSelection.equals("Hold")) {
            autoclicker.stopThread();
            clickerEnabled = false;
            Platform.runLater(() -> {
                clickerStatus.setText("Status: DISABLED");
            });
        }

    }
}
