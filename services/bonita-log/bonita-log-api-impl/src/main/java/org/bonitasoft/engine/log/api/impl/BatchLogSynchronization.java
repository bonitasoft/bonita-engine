/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.log.api.impl;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.TransactionState;

public class BatchLogSynchronization implements BonitaTransactionSynchronization {

    private final PersistenceService persistenceService;

    private final List<SQueriableLog> logs = new ArrayList<SQueriableLog>();

    private Exception exception;

    private final boolean delayable;

    public BatchLogSynchronization(final PersistenceService persistenceService, final boolean delayable) {
        super();
        this.persistenceService = persistenceService;
        this.delayable = delayable;
    }

    @Override
    public void afterCompletion(final TransactionState transactionState) {
        if (this.delayable && TransactionState.COMMITTED == transactionState) {
            BatchLogBuffer.getInstance().addLogs(this.logs);
            final InsertBatchLogsJobRegister register = InsertBatchLogsJobRegister.getInstance();
            register.registerJobIfNotRegistered();
        }
    }

    @Override
    public void beforeCommit() {
        if (!this.delayable) {
            if (this.logs != null && !this.logs.isEmpty()) {
                try {
                    this.persistenceService.insertInBatch(new ArrayList<PersistentObject>(this.logs));
                    this.persistenceService.flushStatements();
                } catch (final SPersistenceException e) {
                    this.exception = e;
                    // FIXME what to do?
                } finally {
                    this.logs.clear();
                }
            }
        }
    }

    public Exception getException() {
        return this.exception;
    }

    public void addLog(final SQueriableLog sQueriableLog) {
        // no synchronized required as we are working on a threadLocal
        this.logs.add(sQueriableLog);
    }

}
