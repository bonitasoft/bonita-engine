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
package org.bonitasoft.engine.business.data.impl;

import javax.persistence.EntityManager;
import javax.transaction.Status;

import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthieu Chaffotte
 */
public class RemoveEntityManagerSynchronization implements BonitaTransactionSynchronization {

    private static final Logger log = LoggerFactory.getLogger(RemoveEntityManagerSynchronization.class);

    private final ThreadLocal<EntityManager> localManager;

    public RemoveEntityManagerSynchronization(final ThreadLocal<EntityManager> localManager) {
        super();
        this.localManager = localManager;
    }

    @Override
    public void afterCompletion(final int txState) {
        if (txState == Status.STATUS_UNKNOWN) {
            log.error("BDM EntityManager cleanup after transaction completed with STATUS_UNKNOWN "
                    + "(heuristic mixed outcome). Some XA resources may have committed while others rolled back. "
                    + "The persistence context is in an undefined state and will be discarded. "
                    + "Manual verification of business data consistency may be required.");
        }
        try {
            EntityManager entityManager = localManager.get();
            // Ensure the EntityManager is not already closed before attempting to close it (EntityManager.close() throws
            // an exception if the EntityManager is already closed)
            if (entityManager != null && entityManager.isOpen()) {
                try {
                    entityManager.close();
                } catch (Exception e) {
                    log.warn("Failed to close BDM EntityManager during transaction completion (txState={}). "
                            + "This may indicate a resource leak. The EntityManager reference will still be removed "
                            + "from the thread to prevent stale state on the next operation.",
                            txState, e);
                    throw e;
                }
            }
        } finally {
            localManager.remove();
        }
    }

}
