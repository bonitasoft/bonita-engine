/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.service.platform;

import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.TransactionState;

/**
 * @author Elias Ricken de Medeiros
 */
public class UpdatePlatformInfoSynchronization implements BonitaTransactionSynchronization {

    private final PlatformInformationProvider platformInformationProvider;

    public UpdatePlatformInfoSynchronization(PlatformInformationProvider platformInformationProvider) {
        this.platformInformationProvider = platformInformationProvider;
    }

    @Override
    public void beforeCommit() {
        //do nothing
    }

    @Override
    public void afterCompletion(final TransactionState txState) {
        if (TransactionState.COMMITTED.equals(txState)) {
            platformInformationProvider.register();
        }
    }

}
