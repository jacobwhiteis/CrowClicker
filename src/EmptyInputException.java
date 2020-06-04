/**
 * Class to handle empty input fields
 *
 * @author Jacob Whiteis
 */
public class EmptyInputException extends Exception {

    /**
     * Constructor
     * @param s message
     */
    public EmptyInputException(String s) {
        super(s);
    }

}
