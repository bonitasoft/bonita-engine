/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
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

import org.bonitasoft.engine.api.impl.PageIndexCheckingUtil;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.exception.PageOutOfRangeException;
import org.bonitasoft.engine.persistence.OrderAndField;
import org.bonitasoft.engine.transaction.STransactionException;

import com.bonitasoft.engine.bpm.model.breakpoint.BreakpointCriterion;
import com.bonitasoft.engine.core.process.instance.api.BreakpointService;
import com.bonitasoft.engine.core.process.instance.model.breakpoint.SBreakpoint;
import com.bonitasoft.engine.core.process.instance.model.builder.SBreakpointBuilder;
import com.bonitasoft.engine.service.SPModelConvertor;
import com.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Matthieu Chaffotte
 */
public class GetBreakpointsCommand extends TenantCommand {

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor tenantAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        final int pageNumber = getIntegerMandadoryParameter(parameters, "pageNumber");
        final int numberPerPage = getIntegerMandadoryParameter(parameters, "numberPerPage");
        final BreakpointCriterion sort = getParameter(parameters, "sort", "");
        final BreakpointService breakpointService = tenantAccessor.getBreakpointService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SBreakpointBuilder builder = tenantAccessor.getBPMInstanceBuilders().getSBreakpointBuilder();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final long totalNumber = breakpointService.getNumberOfBreakpoints();
                PageIndexCheckingUtil.checkIfPageIsOutOfRange(totalNumber, pageNumber, numberPerPage);
                final OrderAndField orderAndField = OrderAndFields.getOrderAndFieldForBreakpoints(sort, builder);
                final List<SBreakpoint> breakpoints = breakpointService.getBreakpoints(pageNumber * numberPerPage, numberPerPage, orderAndField.getField(),
                        orderAndField.getOrder());
                return (Serializable) SPModelConvertor.toBreakpoints(breakpoints);
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new SCommandExecutionException(e);
            } catch (final PageOutOfRangeException e) {
                transactionExecutor.setTransactionRollback();
                throw new SCommandExecutionException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new SCommandExecutionException(e);
        }
    }

}
