/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.command;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;

import com.bonitasoft.engine.api.impl.transaction.RemoveBreakpoint;
import com.bonitasoft.engine.core.process.instance.api.BreakpointService;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SBreakpointNotFoundException;
import com.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Matthieu Chaffotte
 */
public class RemoveBreakpointCommand extends TenantCommand {

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor tenantAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        final long breakpointId = getLongMandadoryParameter(parameters, "breakpointId");
        final BreakpointService breakpointService = tenantAccessor.getBreakpointService();
        try {
            new RemoveBreakpoint(breakpointService, breakpointId).execute();
            return null;
        } catch (final SBreakpointNotFoundException sbnfe) {
            throw new SCommandExecutionException(sbnfe);
        } catch (final SBonitaException sbe) {
            throw new SCommandExecutionException(sbe);
        }
    }

}
