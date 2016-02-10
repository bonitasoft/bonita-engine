/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.execution.transaction;

import java.util.concurrent.Callable;

import com.bonitasoft.engine.execution.LockInfo;
import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.lock.SLockException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Elias Ricken de Medeiros
 */
public class LockedTransactionExecutor {

    private final LockService lockService;
    private final TransactionService transactionService;
    private final TechnicalLoggerService loggerService;

    public LockedTransactionExecutor(LockService lockService, TransactionService transactionService, TechnicalLoggerService loggerService) {
        this.lockService = lockService;
        this.transactionService = transactionService;
        this.loggerService = loggerService;
    }

    /**
     * Executes the code of given callable inside a transaction after taking a lock using the given lock fields.
     * @param lockInfo identifies the fields used to hold the lock (id and type).
     * @param callable code to be executed inside a locked transaction.
     */
    public void executeInsideLock(LockInfo lockInfo, Callable<?> callable) {
        BonitaLock lock = null;
        try {
            lock = lockService.lock(lockInfo.getId(), lockInfo.getType(), lockInfo.getTenantId());
            transactionService.executeInTransaction(callable);
        } catch (Exception e) {
            if(loggerService.isLoggable(getClass(), TechnicalLogSeverity.ERROR)) {
                loggerService.log(getClass(), TechnicalLogSeverity.ERROR, "Unable to execute transaction.", e);
            }
        } finally {
            releaseLock(lock, lockInfo);
        }
    }

    private void releaseLock(final BonitaLock lock, final LockInfo lockInfo) {
        if (lock != null) {
            try {
                lockService.unlock(lock, lockInfo.getTenantId());
            } catch (SLockException e) {
                loggerService.log(getClass(), TechnicalLogSeverity.ERROR, "Unable to release lock: " + lockInfo + ". Please, restart your server", e);
            }
        }
    }

}
