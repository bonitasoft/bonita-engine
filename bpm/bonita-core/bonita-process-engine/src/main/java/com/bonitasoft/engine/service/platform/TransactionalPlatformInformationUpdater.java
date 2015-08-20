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

import com.bonitasoft.engine.execution.transaction.LockedTransactionExecutor;

/**
 * @author Elias Ricken de Medeiros
 */
public class TransactionalPlatformInformationUpdater implements Runnable {

    private final LockedTransactionExecutor lockedTransactionExecutor;
    private final PlatformInformationManagerImpl platformInformationManager;
    private final PlatformInformationProvider platformInformationProvider;

    public TransactionalPlatformInformationUpdater(LockedTransactionExecutor lockedTransactionExecutor,
            PlatformInformationManagerImpl platformInformationManager, PlatformInformationProvider platformInformationProvider) {
        this.lockedTransactionExecutor = lockedTransactionExecutor;
        this.platformInformationManager = platformInformationManager;
        this.platformInformationProvider = platformInformationProvider;
    }

    @Override
    public void run() {
        if (platformInformationProvider.get() > 0) {
            updateInfoInTransaction();
        }
    }

    private void updateInfoInTransaction() {
        lockedTransactionExecutor.executeInsideLock(PlatformInfoLock.build(), new UpdatePlatformInfoTransactionContent());
    }

    protected class UpdatePlatformInfoTransactionContent implements Callable<Void> {

        @Override
        public Void call() throws Exception {
            platformInformationManager.update();
            return null;
        }
    }

}
