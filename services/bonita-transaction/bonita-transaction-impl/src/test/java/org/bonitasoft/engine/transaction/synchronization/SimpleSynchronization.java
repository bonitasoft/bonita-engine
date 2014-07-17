package org.bonitasoft.engine.transaction.synchronization;

import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.TransactionState;

public class SimpleSynchronization implements BonitaTransactionSynchronization {

    private boolean beforeCompletion = false;

    private boolean afterCompletion = false;

    private TransactionState afterCompletionStatus = TransactionState.NO_TRANSACTION;

    private final boolean failOnBeforeCompletion;

    private final boolean failOnAfterCompletion;

    public SimpleSynchronization() {
        this.failOnBeforeCompletion = false;
        this.failOnAfterCompletion = false;
    }

    public SimpleSynchronization(final boolean failOnBeforeCompletion, final boolean failOnAfterCompletion) {
        this.failOnBeforeCompletion = failOnBeforeCompletion;
        this.failOnAfterCompletion = failOnAfterCompletion;
    }

    @Override
    public void beforeCommit() {
        if (this.failOnBeforeCompletion) {
            throw new RuntimeException();
        }
        this.beforeCompletion = true;
    }

    @Override
    public void afterCompletion(final TransactionState status) {
        if (this.failOnAfterCompletion) {
            throw new RuntimeException();
        }
        this.afterCompletion = true;
        this.afterCompletionStatus = status;
    }

    public boolean isBeforeCompletion() {
        return this.beforeCompletion;
    }

    public boolean isAfterCompletion() {
        return this.afterCompletion;
    }

    public TransactionState getAfterCompletionStatus() {
        return this.afterCompletionStatus;
    }

}
