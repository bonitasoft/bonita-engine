/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.service.platform;

import java.util.concurrent.Callable;

import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.lock.SLockException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Elias Ricken de Medeiros
 */
public class TransactionalPlatformInformationUpdater implements Runnable {

    public static final int NONE_TENANT_ID = -1;
    public static final String PLATFORM_INFO_LOCK_KEY = "platformInfo";
    public static final int PLATFORM_INFO_LOCK_ID = 1;
    private final LockService lockService;
    private final TransactionService transactionService;
    private final PlatformInformationManagerImpl platformInformationManager;
    private final PlatformInformationProvider platformInformationProvider;
    private final TechnicalLoggerService loggerService;

    public TransactionalPlatformInformationUpdater(LockService lockService, TransactionService transactionService,
                                                   PlatformInformationManagerImpl platformInformationManager, PlatformInformationProvider platformInformationProvider,
                                                   TechnicalLoggerService loggerService) {
        this.lockService = lockService;
        this.transactionService = transactionService;
        this.platformInformationManager = platformInformationManager;
        this.platformInformationProvider = platformInformationProvider;
        this.loggerService = loggerService;
    }

    @Override
    public void run() {
        if(platformInformationProvider.get() > 0) {
            updateInfoInTransaction();
        }
    }

    private void updateInfoInTransaction() {
        BonitaLock lock = null;
        try {
            lock = lockService.lock(PLATFORM_INFO_LOCK_ID, PLATFORM_INFO_LOCK_KEY, NONE_TENANT_ID);
            transactionService.executeInTransaction(new UpdatePlatformInfoTransactionContent());
        } catch (Exception e) {
            if(loggerService.isLoggable(getClass(), TechnicalLogSeverity.ERROR)) {
                loggerService.log(getClass(), TechnicalLogSeverity.ERROR, "Unable to update the platform information.", e);
            }
        } finally {
            releaseLock(lock);
        }
    }

    private void releaseLock(final BonitaLock lock) {
        if (lock != null) {
            try {
                lockService.unlock(lock, NONE_TENANT_ID);
            } catch (SLockException e) {
                loggerService.log(getClass(), TechnicalLogSeverity.ERROR, "Unable to release platform lock. Please, restart your server", e);
            }
        }
    }

    protected class UpdatePlatformInfoTransactionContent implements Callable<Void> {

        @Override
        public Void call() throws Exception {
            platformInformationManager.update();
            return null;
        }
    }

}
