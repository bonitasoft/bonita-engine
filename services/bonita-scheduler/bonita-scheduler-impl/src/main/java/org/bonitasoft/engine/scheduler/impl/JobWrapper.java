/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SFireEventException;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.scheduler.exception.SJobConfigurationException;
import org.bonitasoft.engine.scheduler.exception.SJobExecutionException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class JobWrapper implements StatelessJob {

    private static final long serialVersionUID = 7145451610635400449L;

    private final StatelessJob statelessJob;

    private final TechnicalLoggerService logger;

    private final long tenantId;

    private final SEvent jobExecuting;

    private final SEvent jobCompleted;

    private final EventService eventService;

    private final String name;

    private final SessionAccessor sessionAccessor;

    private final TransactionService transactionService;

    public JobWrapper(final String name, final StatelessJob statelessJob, final TechnicalLoggerService logger, final long tenantId,
            final EventService eventService, final SessionAccessor sessionAccessor, final TransactionService transactionService) {
        this.name = name;
        this.sessionAccessor = sessionAccessor;
        this.statelessJob = statelessJob;
        this.logger = logger;
        this.tenantId = tenantId;
        this.eventService = eventService;
        this.transactionService = transactionService;
        jobExecuting = BuilderFactory.get(SEventBuilderFactory.class).createNewInstance(JOB_EXECUTING).done();
        jobCompleted = BuilderFactory.get(SEventBuilderFactory.class).createNewInstance(JOB_COMPLETED).done();
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
    public void execute() throws SJobExecutionException, SFireEventException {
        try {
            sessionAccessor.setTenantId(tenantId);
            if (eventService.hasHandlers(JOB_EXECUTING, null)) {
                jobExecuting.setObject(this);
                eventService.fireEvent(jobExecuting);
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "Start execution of " + statelessJob.getName());
            }
            statelessJob.execute();
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "Finished execution of " + statelessJob.getName());
            }
        } catch (final SFireEventException e) {
            logFailedJob(e);
            throw e;
        } catch (final SJobExecutionException e) {
            logFailedJob(e);
            throw e;
        } finally {
            if (eventService.hasHandlers(JOB_COMPLETED, null)) {
                jobCompleted.setObject(this);
                eventService.fireEvent(jobCompleted);
            }
            try {
                transactionService.registerBonitaSynchronization(new BonitaTransactionSynchronizationImpl(sessionAccessor));
            } catch (final STransactionNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void logFailedJob(final SBonitaException e) {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.ERROR)) {
            logger.log(this.getClass(), TechnicalLogSeverity.ERROR, "Error while executing job " + name + " : " + e.getMessage(), e);
        }
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) throws SJobConfigurationException {
        statelessJob.setAttributes(attributes);
    }

    public StatelessJob getStatelessJob() {
        return statelessJob;
    }

}
