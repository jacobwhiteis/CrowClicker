/**
 * Exception to handle the activation bind and lock bind being the same upon launch of autoclicker
 *
 * @author Jacob Whiteis
 */
public class SameBindException extends Exception {

    /**
     * Constructor
     * @param s message
     */
    public SameBindException(String s) {
        super(s);
    }

}
