/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.log.api.impl;

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
            try {
                persistenceService.insertInBatch(new ArrayList<PersistentObject>(logs));
            } catch (final SBonitaException e) {
                throw new JobExecutionException(e);
            }
        }
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) throws SJobConfigurationException {
    }

    public static void setTransactionService(final TransactionService transactionService) {
        InsertBatchLogsJob.transactionService = transactionService;
    }

}
