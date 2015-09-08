/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.service.platform;

import org.bonitasoft.engine.platform.exception.SPlatformNotFoundException;
import org.bonitasoft.engine.platform.exception.SPlatformUpdateException;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Elias Ricken de Medeiros
 */
public class SynchronizationPlatformInfoManager implements PlatformInformationManager {

    private final TransactionService transactionService;
    private final UpdatePlatformInfoSynchronization synchronization;

    public SynchronizationPlatformInfoManager(TransactionService transactionService, UpdatePlatformInfoSynchronization synchronization) {
        this.transactionService = transactionService;
        this.synchronization = synchronization;
    }

    @Override
    public void update() throws SPlatformNotFoundException, SPlatformUpdateException {
        try {
            transactionService.registerBonitaSynchronization(synchronization);
        } catch (STransactionNotFoundException e) {
            throw new SPlatformUpdateException("Unable to update the platform information", e);
        }
    }

}
