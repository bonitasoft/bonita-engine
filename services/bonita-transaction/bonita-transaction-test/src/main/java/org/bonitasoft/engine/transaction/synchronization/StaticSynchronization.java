package org.bonitasoft.engine.transaction.synchronization;

import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.TransactionState;

public class StaticSynchronization implements BonitaTransactionSynchronization {

    private final String beforeCompletionComment;

    private final String afterCompletionComment;

    private boolean failOnBefore = false;

    private boolean failOnAfter = false;

    public StaticSynchronization(final int id) {
        super();
        this.beforeCompletionComment = "sync" + id + "Before";
        this.afterCompletionComment = "sync" + id + "After";
    }

    public StaticSynchronization(final int id, final boolean failOnBefore, final boolean failOnAfter) {
        this(id);
        this.failOnBefore = failOnBefore;
        this.failOnAfter = failOnAfter;
    }

    public String getBeforeCompletionComment() {
        return this.beforeCompletionComment;
    }

    public String getAfterCompletionComment() {
        return this.afterCompletionComment;
    }

    @Override
    public void beforeCommit() {
        StaticSynchronizationResult.COMMENT += this.beforeCompletionComment;
        if (this.failOnBefore) {
            throw new RuntimeException();
        }
    }

    @Override
    public void afterCompletion(final TransactionState status) {
        StaticSynchronizationResult.COMMENT += this.afterCompletionComment;
        if (this.failOnAfter) {
            throw new RuntimeException();
        }
    }

}
