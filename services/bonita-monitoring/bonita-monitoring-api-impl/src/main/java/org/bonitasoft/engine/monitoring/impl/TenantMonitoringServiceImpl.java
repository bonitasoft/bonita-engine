/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.monitoring.impl;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.HandlerRegistrationException;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.monitoring.SMonitoringException;
import org.bonitasoft.engine.monitoring.TenantMonitoringService;
import org.bonitasoft.engine.monitoring.mbean.SEntityMXBean;
import org.bonitasoft.engine.monitoring.mbean.SServiceMXBean;
import org.bonitasoft.engine.monitoring.mbean.impl.SEntityMXBeanImpl;
import org.bonitasoft.engine.monitoring.mbean.impl.SServiceMXBeanImpl;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;

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

    private final SUserHandlerImpl userHandler;

    private final boolean useCache;

    private final TransactionService transactionService;

    private final SessionAccessor sessionAccessor;

    private long numberOfUsers = -1;

    public TenantMonitoringServiceImpl(final boolean allowMbeansRegistration, final boolean useCache, final IdentityService identityService,
            final EventService eventService, final TransactionService transactionService, final SessionAccessor sessionAccessor,
            final SessionService sessionService, final STransactionHandlerImpl transactionHandler, final SJobHandlerImpl jobHandler,
            final SUserHandlerImpl userHandler, final TechnicalLoggerService technicalLog) throws HandlerRegistrationException {
        super(allowMbeansRegistration, technicalLog);
        this.identityService = identityService;
        this.transactionHandler = transactionHandler;
        this.jobHandler = jobHandler;
        this.eventService = eventService;
        this.userHandler = userHandler;
        this.useCache = useCache;
        this.transactionService = transactionService;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;

        addHandlers();
        addMBeans();
    }

    private void addMBeans() {
        final SEntityMXBean entityBean = new SEntityMXBeanImpl(transactionService, this, sessionAccessor, sessionService);
        final SServiceMXBean serviceBean = new SServiceMXBeanImpl(this, sessionAccessor, sessionService);
        addMBean(entityBean);
        addMBean(serviceBean);
    }

    private void initializeCache() throws SMonitoringException {
        try {
            numberOfUsers = identityService.getNumberOfUsers();
        } catch (final SIdentityException e) {
            throw new SMonitoringException("Impossible to retrive the number of users", e);
        }
        userHandler.setNbOfUsers(numberOfUsers);// initialize the number of user
    }

    private void addHandlers() throws HandlerRegistrationException {
        eventService.addHandler(STransactionHandlerImpl.TRANSACTION_ACTIVE_EVT, transactionHandler);
        eventService.addHandler(STransactionHandlerImpl.TRANSACTION_COMMITED_EVT, transactionHandler);
        eventService.addHandler(STransactionHandlerImpl.TRANSACTION_ROLLEDBACK_EVT, transactionHandler);

        eventService.addHandler(SJobHandlerImpl.JOB_COMPLETED, jobHandler);
        eventService.addHandler(SJobHandlerImpl.JOB_EXECUTING, jobHandler);
        eventService.addHandler(SJobHandlerImpl.JOB_FAILED, jobHandler);

        eventService.addHandler(SUserHandlerImpl.USER_CREATED, userHandler);
        eventService.addHandler(SUserHandlerImpl.USER_DELETED, userHandler);
    }

    @Override
    public long getNumberOfUsers() throws SMonitoringException {
        long nbOfUsers = 0;
        if (useCache) {
            if (numberOfUsers < 0) {
                initializeCache();
            }
            // we use the handler information to update the MXBean's information.
            nbOfUsers = userHandler.getNbOfUsers();
        } else {
            try {
                nbOfUsers = identityService.getNumberOfUsers();
            } catch (final SIdentityException e) {
                throw new SMonitoringException("Impossible to retrieve number of users: ", e);
            }
        }
        return nbOfUsers;
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
