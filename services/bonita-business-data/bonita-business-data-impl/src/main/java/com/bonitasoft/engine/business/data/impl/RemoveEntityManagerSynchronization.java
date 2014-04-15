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
