import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import java.awt.*;
import java.awt.event.InputEvent;
import java.sql.SQLOutput;
import java.util.concurrent.TimeUnit;

public class AutoClicker {

    private final double cps;
    private final AutoClickerWindow autoClickerWindow;
    private final int mouseButtonSelection;
    private final int limiter;
    private int counter;

    private volatile boolean threadRunning = false;

    public AutoClicker(double cps, AutoClickerWindow autoClickerWindow, int mouseButtonSelection, int limiter) {
        this.cps = cps;
        this.autoClickerWindow = autoClickerWindow;
        this.mouseButtonSelection = mouseButtonSelection;
        this.limiter = limiter;
    }

    public void run() {
        System.out.println("Awaiting toggle key to start thread.");
    }

    Thread t;

    public void startThread() {
        t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Robot robot = new Robot();
                    // robot.delay(2000); // Wait 2 seconds before starting auto-clicker
                    int intervalMS = 1000/(int)cps;
                    threadRunning = true;
                    counter = 0;
                    while (threadRunning) {
                        System.out.println("Click!" + counter);
                        robot.mousePress(mouseButtonSelection);
                        robot.mouseRelease(mouseButtonSelection);
                        counter++;
                        if (counter==limiter) {
                            System.out.println("Burst limit of " + limiter + " reached.");
                            counter=0;
                            autoClickerWindow.disableClickerFlag();
                            stopThread();
                        }
                        robot.delay(intervalMS);
                    }
                } catch (AWTException e) {
                    System.out.println("Threw AWTException");
                    e.printStackTrace();
                }
            }
        });

        t.start();

    }

    public void stopThread() {
        threadRunning = false;
    }

    public void restartBurst() {
        counter = 0;
    }
}
