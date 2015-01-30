/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.data.impl;

import javax.persistence.EntityManager;

import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.TransactionState;

/**
 * @author Matthieu Chaffotte
 */
public class RemoveEntityManagerSynchronization implements BonitaTransactionSynchronization {

    private final ThreadLocal<EntityManager> localManager;

    public RemoveEntityManagerSynchronization(final ThreadLocal<EntityManager> localManager) {
        super();
        this.localManager = localManager;
    }

    @Override
    public void beforeCommit() {
        localManager.remove();
    }

    @Override
    public void afterCompletion(final TransactionState txState) {
        // Nothing to do
    }

}
