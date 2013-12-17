package com.bonitasoft.engine.business.data;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

public class NonUniqueResultException extends SBonitaException {

    private static final long serialVersionUID = 7573132495695445017L;

    public NonUniqueResultException(final Throwable cause) {
        super(cause);
    }

}
