/*******************************************************************************
 * Copyright (C) 2011-2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.monitoring.impl;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.HandlerRegistrationException;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;

import com.bonitasoft.engine.monitoring.SMonitoringException;
import com.bonitasoft.engine.monitoring.TenantMonitoringService;
import com.bonitasoft.engine.monitoring.mbean.SEntityMXBean;
import com.bonitasoft.engine.monitoring.mbean.SServiceMXBean;
import com.bonitasoft.engine.monitoring.mbean.impl.SEntityMXBeanImpl;
import com.bonitasoft.engine.monitoring.mbean.impl.SServiceMXBeanImpl;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class TenantMonitoringServiceImpl extends MonitoringServiceImpl implements TenantMonitoringService {

    private final IdentityService identityService;

    private final EventService eventService;

    private final SessionService sessionService;

    private final STransactionHandlerImpl transactionHandler;

    private final SJobHandlerImpl jobHandler;

    private final TransactionService transactionService;

    private final SessionAccessor sessionAccessor;

    public TenantMonitoringServiceImpl(final boolean allowMbeansRegistration, final IdentityService identityService, final EventService eventService,
            final TransactionService transactionService, final SessionAccessor sessionAccessor, final SessionService sessionService,
            final STransactionHandlerImpl transactionHandler, final SJobHandlerImpl jobHandler, final TechnicalLoggerService technicalLog)
            throws HandlerRegistrationException {
        super(allowMbeansRegistration, technicalLog);
        this.identityService = identityService;
        this.transactionHandler = transactionHandler;
        this.jobHandler = jobHandler;
        this.eventService = eventService;
        this.transactionService = transactionService;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;

        addHandlers();
        addMBeans();
    }

    private void addMBeans() {
        final SEntityMXBean entityBean = new SEntityMXBeanImpl(transactionService, this, sessionAccessor, sessionService);
        final SServiceMXBean serviceBean = new SServiceMXBeanImpl(transactionService, this, sessionAccessor, sessionService);
        addMBean(entityBean);
        addMBean(serviceBean);
    }

    private void addHandlers() throws HandlerRegistrationException {
        eventService.addHandler(STransactionHandlerImpl.TRANSACTION_ACTIVE_EVT, transactionHandler);
        eventService.addHandler(STransactionHandlerImpl.TRANSACTION_COMMITED_EVT, transactionHandler);
        eventService.addHandler(STransactionHandlerImpl.TRANSACTION_ROLLEDBACK_EVT, transactionHandler);

        eventService.addHandler(SJobHandlerImpl.JOB_COMPLETED, jobHandler);
        eventService.addHandler(SJobHandlerImpl.JOB_EXECUTING, jobHandler);
        eventService.addHandler(SJobHandlerImpl.JOB_FAILED, jobHandler);
    }

    @Override
    public long getNumberOfUsers() throws SMonitoringException {
        try {
            return identityService.getNumberOfUsers();
        } catch (final SIdentityException e) {
            throw new SMonitoringException("Impossible to retrieve number of users: ", e);
        }
    }

    @Override
    public long getNumberOfActiveTransactions() {
        return transactionHandler.getNumberOfActiveTransactions();
    }

    @Override
    public long getNumberOfExecutingJobs() {
        return jobHandler.getExecutingJobs();
    }

}
