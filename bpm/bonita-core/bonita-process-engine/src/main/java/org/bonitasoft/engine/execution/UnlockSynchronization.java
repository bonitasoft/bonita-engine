package org.bonitasoft.engine.execution;

import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.lock.SLockException;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.TransactionState;

public class UnlockSynchronization implements BonitaTransactionSynchronization {

    private final LockService lockService;

    private final long lockId;

    private final String type;

    public UnlockSynchronization(LockService lockService, long lockId, String type) {
        this.lockService = lockService;
        this.lockId = lockId;
        this.type = type;
    }

    @Override
    public void beforeCommit() {
    }

    @Override
    public void afterCompletion(TransactionState txState) {
        try {
            lockService.unlock(lockId, type);
        } catch (SLockException e) {
            throw new BonitaRuntimeException("Unable to unlock " + type + "_" + lockId, e);
        }
    }
}
