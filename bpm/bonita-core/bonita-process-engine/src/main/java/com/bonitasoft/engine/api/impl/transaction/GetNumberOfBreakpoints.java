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
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;

import com.bonitasoft.engine.core.process.instance.api.BreakpointService;

/**
 * @author Baptiste Mesta
 */
public final class GetNumberOfBreakpoints implements TransactionContentWithResult<Long> {

    private final BreakpointService breakpointService;

    private long numberOfProcesses;

    public GetNumberOfBreakpoints(final BreakpointService breakpointService) {
        this.breakpointService = breakpointService;
    }

    @Override
    public void execute() throws SBonitaException {
        numberOfProcesses = breakpointService.getNumberOfBreakpoints();
    }

    @Override
    public Long getResult() {
        return numberOfProcesses;
    }
}
