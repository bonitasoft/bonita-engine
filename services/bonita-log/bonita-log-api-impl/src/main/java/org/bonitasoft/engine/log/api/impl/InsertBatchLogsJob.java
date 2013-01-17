/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package org.bonitasoft.engine.log.api.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.events.model.FireEventException;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.scheduler.JobExecutionException;
import org.bonitasoft.engine.scheduler.SJobConfigurationException;
import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.transaction.BusinessTransaction;
import org.bonitasoft.engine.transaction.SBadTransactionStateException;
import org.bonitasoft.engine.transaction.STransactionCreationException;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Baptiste Mesta
 */
public class InsertBatchLogsJob implements StatelessJob {

    private static final long serialVersionUID = -4356390646702427686L;

    private static PersistenceService persistenceService;

    private static TransactionService transactionService;

    public static void setPersistenceService(final PersistenceService persistenceService) {
        InsertBatchLogsJob.persistenceService = persistenceService;
    }

    @Override
    public String getName() {
        return "BATCH_LOGS_INSERT";
    }

    @Override
    public String getDescription() {
        return "Batch logs insert";
    }

    @Override
    public void execute() throws JobExecutionException, FireEventException {
        final List<SQueriableLog> logs = BatchLogBuffer.getInstance().clearLogs();
        if (logs.size() > 0) {
            BusinessTransaction tx;
            try {
                tx = transactionService.createTransaction();
            } catch (final STransactionCreationException e) {
                throw new JobExecutionException(e);
            }
            try {
                tx.begin();
                persistenceService.insertInBatch(new ArrayList<PersistentObject>(logs));
            } catch (final SBonitaException e) {
                try {
                    tx.setRollbackOnly();
                } catch (final SBadTransactionStateException e1) {
                }
                throw new JobExecutionException(e);
            } finally {
                try {
                    tx.complete();
                } catch (final SBonitaException e) {
                    throw new JobExecutionException(e);
                }
            }
        }
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) throws SJobConfigurationException {
    }

    @Override
    public boolean isWrappedInTransaction() {
        return false;
    }

    public static void setTransactionService(final TransactionService transactionService) {
        InsertBatchLogsJob.transactionService = transactionService;
    }

}
