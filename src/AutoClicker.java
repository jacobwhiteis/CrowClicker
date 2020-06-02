import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import java.awt.*;
import java.awt.event.InputEvent;
import java.sql.SQLOutput;
import java.util.concurrent.TimeUnit;

public class AutoClicker {

    private final double cps;
    private final double averageInterval;
    private final double averageDuration;
    private EnumeratedIntegerDistribution probDistribution = null;
    private NormalDistribution normDistribution = null;
    private double distributionMean = 0.0;
    private final AutoClickerWindow autoClickerWindow;
    private final int mouseButtonSelection;
    private final int limiter;
    private int counter;
    private final boolean enableRandomizer;

    private volatile boolean threadRunning = false;

    public AutoClicker(double cps, AutoClickerWindow autoClickerWindow, int mouseButtonSelection, int limiter, EnumeratedIntegerDistribution distribution, boolean enableRandomizer) {
        this.enableRandomizer = enableRandomizer;
        this.cps = cps;
        this.averageInterval = 1000/cps;
        this.averageDuration = averageInterval/2;
        if (enableRandomizer) {
            this.probDistribution = distribution;
            this.normDistribution = new NormalDistribution(1, .2);
            distributionMean = distribution.getNumericalMean();
        }
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
                    threadRunning = true;
                    counter = 0;
                    if (enableRandomizer) {
                        while (threadRunning) {
                            int interval = (int)averageInterval;
                            int duration;
                            interval *= probDistribution.sample() / distributionMean;

                            do {
                                duration = (int) ((interval/2.0) * (normDistribution.sample() / normDistribution.getMean()));
                            } while ((duration > (interval/2.0) * 1.6 || duration < (interval/2.0) * .4) && duration != 0);

                            if (interval < 1) {
                                interval = 1;
                                duration = 0;
                            }

                            System.out.println("Click!" + counter);
                            robot.mousePress(mouseButtonSelection);
                            robot.delay(duration);
                            robot.mouseRelease(mouseButtonSelection);
                            robot.delay(interval-duration);
                            counter++;
                            if (counter==limiter) {
                                System.out.println("Burst limit of " + limiter + " reached.");
                                counter=0;
                                autoClickerWindow.disableClickerFlag();
                                stopThread();
                            }
                        }
                    } else {
                        while (threadRunning) {
                            System.out.println("Click!" + counter);
                            robot.mousePress(mouseButtonSelection);
                            robot.mouseRelease(mouseButtonSelection);
                            robot.delay((int)averageInterval);
                            counter++;
                        }
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
