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
package org.bonitasoft.engine.scheduler.impl;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.FireEventException;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.JobExecutionException;
import org.bonitasoft.engine.scheduler.SJobConfigurationException;
import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Matthieu Chaffotte
 */
public class JobWrapper implements StatelessJob {

    private static final long serialVersionUID = 7145451610635400449L;

    private final StatelessJob statelessJob;

    private final TransactionService transactionService;

    private final TechnicalLoggerService logger;

    private final long tenantId;

    private final SEvent jobExecuting;

    private final SEvent jobCompleted;

    private final EventService eventService;

    private final String name;

    private final SessionAccessor sessionAccessor;

    private final SessionService sessionService;

    private final boolean wrapInTransaction;

    public JobWrapper(final String name, final TransactionService transactionService, final QueriableLoggerService logService, final StatelessJob statelessJob,
            final TechnicalLoggerService logger, final long tenantId, final EventService eventService, final JobTruster jobTruster,
            final SessionService sessionService, final SessionAccessor sessionAccessor) {
        this.name = name;
        this.sessionService = sessionService;
        this.sessionAccessor = sessionAccessor;
        this.transactionService = transactionService;
        this.statelessJob = statelessJob;
        this.logger = logger;
        this.tenantId = tenantId;
        this.eventService = eventService;
        wrapInTransaction = statelessJob.isWrappedInTransaction();
        jobExecuting = eventService.getEventBuilder().createNewInstance(JOB_EXECUTING).done();
        jobCompleted = eventService.getEventBuilder().createNewInstance(JOB_COMPLETED).done();
        if (jobTruster.isTrusted(statelessJob)) {// FIXME
            try {
                ClassReflector.invokeMethod(statelessJob, "setQueriableLoggerService", QueriableLoggerService.class, logService);
            } catch (final IllegalArgumentException e) {
                e.printStackTrace();
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
            } catch (final InvocationTargetException e) {
                e.printStackTrace();
            } catch (final SecurityException e) {
                e.printStackTrace();
            } catch (final NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return statelessJob.getDescription();
    }

    @Override
    public void execute() throws JobExecutionException, FireEventException {
        SSession session = null;
        try {
            session = createSession();// FIXME get the technical user of the tenant
            sessionAccessor.setSessionInfo(session.getId(), session.getTenantId());// FIXME do that in the session service?

            if (eventService.hasHandlers(JOB_EXECUTING, null)) {
                jobExecuting.setObject(this);
                eventService.fireEvent(jobExecuting);
            }
            if (wrapInTransaction) {
                transactionService.begin();
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "start execution of " + statelessJob.getName());
            }
            statelessJob.execute();
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "finished execution of " + statelessJob.getName());
            }
        } catch (final Exception e) {
            logger.log(this.getClass(), TechnicalLogSeverity.ERROR, "Error executing job " + name + " exception was thrown:" + e.getMessage());
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, e);
            }
            if (wrapInTransaction) {
                try {
                    transactionService.setRollbackOnly();
                } catch (final STransactionException te) {
                    throw new JobExecutionException(te);
                }
            }
            throw new JobExecutionException(e);
        } finally {
            if (eventService.hasHandlers(JOB_COMPLETED, null)) {
                jobCompleted.setObject(this);
                eventService.fireEvent(jobCompleted);
            }
            try {
                if (wrapInTransaction) {
                    transactionService.complete();
                }
            } catch (final Exception e) {
                throw new JobExecutionException(e);
            } finally {
                if (session != null) {
                    try {
                        sessionAccessor.deleteSessionId();
                        sessionService.deleteSession(session.getId());
                    } catch (final SSessionNotFoundException e) {
                        logger.log(this.getClass(), TechnicalLogSeverity.ERROR, e);// FIXME
                    }
                }
            }
        }
    }

    private SSession createSession() throws Exception {
        try {
            transactionService.begin();
            return sessionService.createSession(tenantId, "scheduler");
        } catch (final Exception e) {
            transactionService.setRollbackOnly();
            throw e;
        } finally {
            transactionService.complete();
        }
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) throws SJobConfigurationException {
        statelessJob.setAttributes(attributes);
    }

    @Override
    public boolean isWrappedInTransaction() {
        return false;
    }

}
