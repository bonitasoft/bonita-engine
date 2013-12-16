package com.bonitasoft.engine.business.data;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

public class BusinessDataNotFoundException extends SBonitaException {

    private static final long serialVersionUID = -4470717601583219790L;

    public BusinessDataNotFoundException(final String message) {
        super(message);
    }

    public BusinessDataNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
