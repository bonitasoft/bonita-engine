package com.bonitasoft.engine.business.data;

public class BusinessDataNotFoundException extends Exception {

    private static final long serialVersionUID = -4470717601583219790L;

    public BusinessDataNotFoundException(final String message) {
        super(message);
    }

    public BusinessDataNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
