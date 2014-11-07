package org.bonitasoft.engine.exception;


public class InvalidPageTokenException extends CreationException {

    private static final long serialVersionUID = -4521026642699202555L;

    public InvalidPageTokenException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public InvalidPageTokenException(String message) {
        super(message);
    }
}
