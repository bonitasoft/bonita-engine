package com.bonitasoft.engine.page;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

public class SInvalidPageZipContentException extends SBonitaException {

    private static final long serialVersionUID = -7263291210428082852L;

    public SInvalidPageZipContentException(final String message) {
        super(message);
    }

}
