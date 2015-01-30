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
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.persistence.OrderAndField;

import com.bonitasoft.engine.bpm.breakpoint.BreakpointCriterion;
import com.bonitasoft.engine.core.process.instance.api.BreakpointService;
import com.bonitasoft.engine.core.process.instance.model.breakpoint.SBreakpoint;
import com.bonitasoft.engine.service.SPModelConvertor;
import com.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Matthieu Chaffotte
 */
public class GetBreakpointsCommand extends TenantCommand {

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor tenantAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        final int startIndex = getIntegerMandadoryParameter(parameters, "startIndex");
        final int maxResults = getIntegerMandadoryParameter(parameters, "maxResults");
        final BreakpointCriterion sort = getParameter(parameters, "sort", "");
        final BreakpointService breakpointService = tenantAccessor.getBreakpointService();
        try {
            final OrderAndField orderAndField = OrderAndFields.getOrderAndFieldForBreakpoints(sort);
            final List<SBreakpoint> breakpoints = breakpointService.getBreakpoints(startIndex, maxResults, orderAndField.getField(),
                    orderAndField.getOrder());
            return (Serializable) SPModelConvertor.toBreakpoints(breakpoints);
        } catch (final SBonitaException e) {
            throw new SCommandExecutionException(e);
        }
    }

}
