/**
 * An exception to handle an event where the user tries to launch the autoclicker without setting a keybind.
 *
 * @author Jacob Whiteis
 */
public class NoKeybindException extends Exception{

    /**
     * Constructor
     * @param s message
     */
    public NoKeybindException(String s) {
        super(s);
    }

}
