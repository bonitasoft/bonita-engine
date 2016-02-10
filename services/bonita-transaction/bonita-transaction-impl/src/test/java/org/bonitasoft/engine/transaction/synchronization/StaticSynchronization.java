/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
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
