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

/**
 * Handles the sending of clicks and determining click patterns.
 *
 * @author Jacob Whiteis
 */
public class AutoClicker {

    private final double averageInterval;
    private double distributionMean = 0.0;
    private final int mouseButtonSelection;
    private final int limiter; // Used for bursts
    private int counter;
    private final boolean enableRandomizer;
    private final AutoClickerWindow autoClickerWindow; // This is only necessary so we can disable the clickerEnabled boolean in AutoClickerWindow
    private EnumeratedIntegerDistribution probDistribution = null;
    private NormalDistribution normDistribution = null;

    private volatile boolean threadRunning = false;

    /**
     * Constructor.
     * @param cps clicks per second
     * @param autoClickerWindow instance of AutoClickerWindow that created this AutoClicker
     * @param mouseButtonSelection the mouse button to send clicks as
     * @param limiter the max number of clicks in a burst
     * @param distribution the probability distribution to grab randomized time intervals from
     * @param enableRandomizer enable randomizer boolean
     */
    public AutoClicker(double cps, AutoClickerWindow autoClickerWindow, int mouseButtonSelection, int limiter, EnumeratedIntegerDistribution distribution, boolean enableRandomizer) {
        this.enableRandomizer = enableRandomizer;
        this.averageInterval = 1000/cps;
        double averageDuration = averageInterval / 2;
        if (enableRandomizer) {
            this.probDistribution = distribution;
            this.normDistribution = new NormalDistribution(1, .2);
            distributionMean = distribution.getNumericalMean();
        }
        this.autoClickerWindow = autoClickerWindow;
        this.mouseButtonSelection = mouseButtonSelection;
        this.limiter = limiter;
    }

    Thread autoClickerThread;

    /**
     * Start the autoclicker thread.
     */
    public void startThread() {
        autoClickerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Robot robot = new Robot();
                    threadRunning = true;
                    counter = 0;
                    if (enableRandomizer) {
                        // Randomizing
                        while (threadRunning) {
                            int interval = (int)averageInterval;
                            int duration;
                            interval *= probDistribution.sample() / distributionMean;

                            // This will only need to run more than once if our normal distribution sample happens to be outside
                            // 3 standard deviations from the mean. Which has a very small chance of occurring.
                            // It is necessary to ensure there is zero chance, however small, of getting some absurd duration value
                            // That may either be 1) obviously inhuman, or 2) exceed the interval itself.
                            do {
                                duration = (int) ((interval/2.0) * (normDistribution.sample() / normDistribution.getMean()));
                            } while ((duration > (interval/2.0) * 1.6 || duration < (interval/2.0) * .4) && duration != 0);

                            // Interval would only have a chance of being 1 once you started using speeds of over 300 CPS.
                            // It shouldn't even be necessary now, since there is a 100 CPS cap when using the randomizer.
                            // I'm just going to leave it in anyways.
                            if (interval < 1) {
                                interval = 1;
                                duration = 0;
                            }

                            robot.mousePress(mouseButtonSelection);
                            robot.delay(duration);
                            robot.mouseRelease(mouseButtonSelection);
                            robot.delay(interval-duration);
                            counter++;
                            if (counter==limiter) {
                                // Our burst limit has been reached
                                counter=0;
                                autoClickerWindow.disableClickerFlag();
                                stopThread();
                            }
                        }
                    } else {
                        // Not randomizing (equal intervals between clicks)
                        while (threadRunning) {
                            System.out.println("Click!" + counter);
                            robot.mousePress(mouseButtonSelection);
                            robot.mouseRelease(mouseButtonSelection);
                            robot.delay((int)averageInterval);
                            counter++;
                        }
                    }
                } catch (AWTException e) {
                    e.printStackTrace();
                }
            }
        });
        autoClickerThread.start();
    }

    /**
     * Stop thread by setting threadRunning to false.
     * autoClickerThread will deal with this.
     */
    public void stopThread() {
        threadRunning = false;
    }

    /**
     * Restart the burst by setting the click counter to 0.
     * autoClickerThread will deal with this.
     */
    public void restartBurst() {
        counter = 0;
    }

}
