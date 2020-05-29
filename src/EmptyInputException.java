// An exception class to handle cases of empty inputs in input fields
public class EmptyInputException extends Exception {

    public EmptyInputException(String s) {
        super(s);
    }

}
