package kpi.manfredi.utils;

public class WrongArgumentsException extends Exception {
    public WrongArgumentsException() {
        super("Wrong list of arguments.");
    }

    public WrongArgumentsException(String message) {
        super(message);
    }
}
