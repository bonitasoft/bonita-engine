/*******************************************************************************
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.exception;

import org.bonitasoft.engine.exception.ObjectNotFoundException;

import com.bonitasoft.engine.bpm.model.breakpoint.Breakpoint;

/**
 * @author Baptiste Mesta
 */
public class BreakpointNotFoundException extends ObjectNotFoundException {

    public BreakpointNotFoundException(final Throwable cause) {
        super(cause, Breakpoint.class);
    }

    public BreakpointNotFoundException(final long id) {
        super("No breakpoint found with id " + id, Breakpoint.class);
    }

    private static final long serialVersionUID = -8435632337750454880L;

}
