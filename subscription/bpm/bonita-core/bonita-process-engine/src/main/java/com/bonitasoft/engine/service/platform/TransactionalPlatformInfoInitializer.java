/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.service.platform;

import java.util.concurrent.Callable;

import com.bonitasoft.engine.execution.transaction.LockedTransactionExecutor;

/**
 * @author Elias Ricken de Medeiros
 */
public class TransactionalPlatformInfoInitializer {

    private final PlatformInfoInitializer platformInfoInitializer;
    private final LockedTransactionExecutor lockedTransactionExecutor;

    public TransactionalPlatformInfoInitializer(PlatformInfoInitializer platformInfoInitializer, LockedTransactionExecutor lockedTransactionExecutor) {
        this.platformInfoInitializer = platformInfoInitializer;
        this.lockedTransactionExecutor = lockedTransactionExecutor;
    }

    public void ensurePlatformInfoIsSet() {
        lockedTransactionExecutor.executeInsideLock(PlatformInfoLock.build(), new PlatformInfoInitContent());
    }

    protected class PlatformInfoInitContent implements Callable<Void> {

        @Override
        public Void call() throws Exception {
            platformInfoInitializer.ensurePlatformInfoIsSet();
            return null;
        }
    }

}
