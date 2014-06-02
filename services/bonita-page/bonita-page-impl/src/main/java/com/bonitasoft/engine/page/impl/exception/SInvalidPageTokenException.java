package com.bonitasoft.engine.page.impl.exception;

import org.bonitasoft.engine.persistence.SBonitaReadException;

public class SInvalidPageTokenException extends SBonitaReadException {

    public SInvalidPageTokenException(final String message) {
        super(message);
    }

    private static final long serialVersionUID = 7161910286756340441L;

}
