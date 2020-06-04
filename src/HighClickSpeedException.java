/**
 * An exception to handle an event where the user tries to launch the autoclicker with too high of a CPS value.
 *
 * @author Jacob Whiteis
 */
public class HighClickSpeedException extends Exception{

    /**
     * Constructor
     * @param s message
     */
    public HighClickSpeedException(String s) {
        super(s);
    }

}
