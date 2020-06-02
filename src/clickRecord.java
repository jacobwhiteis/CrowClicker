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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ClickRecord implements NativeMouseListener {

    ArrayList<Long> clickDurations;
    ArrayList<Long> clickIntervals;
    Instant clickStart;
    Instant clickFinish;
    Instant lastClickStart;
    long clickDuration;
    long betweenClicks;
    int clickCounter;

    public ClickRecord() {
        clickDurations = new ArrayList<>();
        clickIntervals = new ArrayList<>();
        clickCounter = 0;
    }

    public void display() {
        // Disabling logging spam
        LogManager.getLogManager().reset();
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);

        Stage window = new Stage();

        window.setTitle("Click record testing");
        window.setMinWidth(200);
        window.setMinHeight(300);

        Label label1 = new Label();
        label1.setText("Click to start recording clicks");

        Button startRecordingButton = new Button("Start");
        startRecordingButton.setOnAction(e -> {
            startRecording();
        });

        Button exitButton = new Button("Exit and write to file");
        exitButton.setOnAction(e -> {
            // Write click durations to file
            String fileName1 = "clickDurations.txt";
            String fileName2 = "clickIntervals.txt";
            try {
                File cdFile = new File(fileName1);
                File ciFile = new File(fileName2);
                if (cdFile.createNewFile() || ciFile.createNewFile()) {
                    System.out.println("File created");
                } else {
                    System.out.println("Files already exist.");
                }
                PrintWriter writer = new PrintWriter(fileName1);
                writer.write("Durations: ");
                if (cdFile.length() == 0) {
                    writer.print("");
                }
                for (Long clickDuration : clickDurations) {
                    writer.write(clickDuration + ",");
                }
                writer.close();
                PrintWriter writer2 = new PrintWriter(fileName2);
                writer2.write("Intervals: ");
                if (ciFile.length() == 0) {
                    writer.print("");
                }
                for (Long clickInterval : clickIntervals) {
                    writer2.write(clickInterval + ",");
                }
                int i = 0;
                for (Long l : clickIntervals) {
                    System.out.println(l + " | " + String.valueOf(i));
                    i++;
                }
                writer2.close();
                System.out.println("Total theoretical clicks: " + clickCounter);
                System.out.println("Length of click duration list: " + clickDurations.size());
                System.out.println("Length of click interval list: " + clickIntervals.size());
                writer.write("\n\nTotal clicks: " + clickCounter);
            } catch (IOException ex) {
                System.out.println("IOException");
                ex.printStackTrace();
            }

            cleanGlobalScreen();
            window.close();
        });

        window.setOnCloseRequest(e -> {
            cleanGlobalScreen();
            window.close();
        });

        VBox layout = new VBox(10);
        layout.getChildren().addAll(label1, startRecordingButton, exitButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20, 20, 20, 20));

        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.show();

    }

    public void startRecording() {
        System.out.println("Registering native hook...");
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
        GlobalScreen.addNativeMouseListener(this);
    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent nativeMouseEvent) {
        //
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {
        clickStart = Instant.now();
        if (clickCounter != 0) {
            betweenClicks = Duration.between(lastClickStart, clickStart).toMillis();
            clickIntervals.add(betweenClicks);
        }
        clickCounter++;
    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) {
        if (clickCounter != 0) {
            clickFinish = Instant.now();
            clickDuration = Duration.between(clickStart, clickFinish).toMillis();
            clickDurations.add(clickDuration);
            lastClickStart = clickStart;
            System.out.println("Click registered: #" + clickCounter + ", duration: " + clickDuration + " ms.");
        }
    }

    public void cleanGlobalScreen() {
        try {
            GlobalScreen.unregisterNativeHook();
            GlobalScreen.removeNativeMouseListener(this);
        } catch (NativeHookException nativeHookException) {
            System.out.println("Exception in unregisterHook");
            nativeHookException.printStackTrace();
        }
    }


}
