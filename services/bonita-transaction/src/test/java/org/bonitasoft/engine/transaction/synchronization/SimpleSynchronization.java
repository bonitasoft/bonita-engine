/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import javax.transaction.Status;

import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;

public class SimpleSynchronization implements BonitaTransactionSynchronization {

    private boolean beforeCompletion = false;

    private boolean afterCompletion = false;

    private int afterCompletionStatus = Status.STATUS_NO_TRANSACTION;

    private final boolean failOnBeforeCompletion;

    private final boolean failOnAfterCompletion;

    public SimpleSynchronization() {
        this.failOnBeforeCompletion = false;
        this.failOnAfterCompletion = false;
    }

    @Override
    public void beforeCompletion() {
        if (this.failOnBeforeCompletion) {
            throw new RuntimeException();
        }
        this.beforeCompletion = true;
    }

    @Override
    public void afterCompletion(final int status) {
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

    public int getAfterCompletionStatus() {
        return this.afterCompletionStatus;
    }

}
