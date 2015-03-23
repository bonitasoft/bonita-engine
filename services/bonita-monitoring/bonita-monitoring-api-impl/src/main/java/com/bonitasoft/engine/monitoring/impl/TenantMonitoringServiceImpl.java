/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.monitoring.impl;

import static java.util.Arrays.asList;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.HandlerRegistrationException;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
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
 * @author Laurent Vaills
 */
public class TenantMonitoringServiceImpl extends MonitoringServiceImpl implements TenantMonitoringService {

    private final IdentityService identityService;

    private final EventService eventService;

    private final SessionService sessionService;

    private final SJobHandlerImpl jobHandler;

    private final TransactionService transactionService;

    private final SessionAccessor sessionAccessor;
    private SServiceMXBean serviceBean;
    private SEntityMXBean entityBean;

    public TenantMonitoringServiceImpl(final boolean allowMbeansRegistration, final IdentityService identityService, final EventService eventService,
            final TransactionService transactionService, final SessionAccessor sessionAccessor, final SessionService sessionService,
            final SJobHandlerImpl jobHandler, final TechnicalLoggerService technicalLog) {
        super(allowMbeansRegistration, technicalLog);
        this.identityService = identityService;
        this.jobHandler = jobHandler;
        this.eventService = eventService;
        this.transactionService = transactionService;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
        addHandlers();
        addMBeans();
    }

    private void addMBeans() {
        entityBean = new SEntityMXBeanImpl(transactionService, this, sessionAccessor, sessionService);
        serviceBean = new SServiceMXBeanImpl(transactionService, this, sessionAccessor, sessionService);
        addMBean(entityBean);
        addMBean(serviceBean);
    }

    @Override
    public SEntityMXBean getEntityBean() {
        return entityBean;
    }

    @Override
    public SServiceMXBean getServiceBean() {
        return serviceBean;
    }

    private void addHandlers() {
        for (String event : asList(SJobHandlerImpl.JOB_COMPLETED, SJobHandlerImpl.JOB_EXECUTING, SJobHandlerImpl.JOB_FAILED)) {
            try {
                eventService.addHandler(event, jobHandler);
            } catch (HandlerRegistrationException hre) {
                // It has already been registered, just log a warning
                if (technicalLog.isLoggable(getClass(), TechnicalLogSeverity.WARNING)) {
                    technicalLog.log(getClass(), TechnicalLogSeverity.WARNING, hre);
                }
            }
        }
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
        return transactionService.getNumberOfActiveTransactions();
    }

    @Override
    public long getNumberOfExecutingJobs() {
        return jobHandler.getExecutingJobs();
    }

}
