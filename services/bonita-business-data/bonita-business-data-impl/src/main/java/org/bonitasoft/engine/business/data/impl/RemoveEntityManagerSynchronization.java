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
package org.bonitasoft.engine.business.data.impl;

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
