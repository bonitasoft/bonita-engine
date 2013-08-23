package org.bonitasoft.engine.execution;

import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.lock.SLockException;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.TransactionState;

public class UnlockSynchronization implements BonitaTransactionSynchronization {

    private final LockService lockService;

    private final BonitaLock lock;

    public UnlockSynchronization(LockService lockService, BonitaLock lock) {
        this.lockService = lockService;
        this.lock = lock;
    }

    @Override
    public void beforeCommit() {
    }

    @Override
    public void afterCompletion(TransactionState txState) {
        try {
            lockService.unlock(lock);
        } catch (SLockException e) {
            throw new BonitaRuntimeException("Unable to unlock " + lock.getObjectType() + "_" + lock.getObjectToLockId(), e);
        }
    }
}
