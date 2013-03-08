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

import javax.transaction.Status;
import javax.transaction.Synchronization;

import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;

public class BatchLogSynchronization implements Synchronization {

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
    public void afterCompletion(final int transactionStatus) {
        if (delayable && Status.STATUS_COMMITTED == transactionStatus) {
            BatchLogBuffer.getInstance().addLogs(logs);
            final InsertBatchLogsJobRegister register = InsertBatchLogsJobRegister.getInstance();
            register.registerJobIfNotRegistered();
        }
    }

    @Override
    public void beforeCompletion() {
        if (!delayable) {
            if (logs != null && !logs.isEmpty()) {
                try {
                    persistenceService.insertInBatch(new ArrayList<PersistentObject>(logs));
                } catch (final SPersistenceException e) {
                    exception = e;
                    // FIXME what to do?
                } finally {
                    logs.clear();
                }
            }
        }
    }

    public Exception getException() {
        return exception;
    }

    public void addLog(final SQueriableLog sQueriableLog) {
        // no synchronized required as we are working on a threadLocal
        logs.add(sQueriableLog);
    }

}
