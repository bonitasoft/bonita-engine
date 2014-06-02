package com.bonitasoft.engine.page.impl.exception;

import org.bonitasoft.engine.persistence.SBonitaReadException;

public class SInvalidPageZipContentException extends SBonitaReadException {

    private static final long serialVersionUID = -7263291210428082852L;

    public SInvalidPageZipContentException(final String message) {
        super(message);
    }

}
