package org.bonitasoft.engine.log.api.impl;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Synchronization;

import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;

public class BatchLogSynchronization implements Synchronization {

    private final PersistenceService persistenceService;

    private final List<SQueriableLog> logs = new ArrayList<SQueriableLog>();

    private Exception exception;

    public BatchLogSynchronization(final PersistenceService persistenceService) {
        super();
        this.persistenceService = persistenceService;
    }

    @Override
    public void afterCompletion(final int arg0) {
        // NOTHING
    }

    @Override
    public void beforeCompletion() {
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

    public Exception getException() {
        return exception;
    }

    public void addLog(final SQueriableLog sQueriableLog) {
        // no synchronized required as we are working on a threadLocal
        logs.add(sQueriableLog);
    }

}
