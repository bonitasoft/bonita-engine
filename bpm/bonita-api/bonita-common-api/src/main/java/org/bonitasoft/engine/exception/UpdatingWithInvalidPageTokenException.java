package org.bonitasoft.engine.exception;

public class UpdatingWithInvalidPageTokenException extends UpdateException {

    private static final long serialVersionUID = -4521026642699202555L;

    public UpdatingWithInvalidPageTokenException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
