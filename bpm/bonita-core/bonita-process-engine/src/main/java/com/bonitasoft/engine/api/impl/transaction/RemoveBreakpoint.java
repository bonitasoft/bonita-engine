/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;

import com.bonitasoft.engine.core.process.instance.api.BreakpointService;

/**
 * @author Baptiste Mesta
 */
public class RemoveBreakpoint implements TransactionContent {

    private final BreakpointService breakpointService;

    private final long breakpointId;

    public RemoveBreakpoint(final BreakpointService breakpointService, final long breakpointId) {
        this.breakpointService = breakpointService;
        this.breakpointId = breakpointId;
    }

    @Override
    public void execute() throws SBonitaException {
        breakpointService.removeBreakpoint(breakpointId);
    }

}
