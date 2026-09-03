/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.engine.tenant;

/**
 * Determines whether the current node is responsible for executing a given named task.
 * <p>
 * In single-node deployments, always returns {@code true}.
 * In cluster deployments, uses Hazelcast partition ownership to ensure
 * exactly one node is responsible for each task at any given time.
 * <p>
 * Each task name maps to a distinct Hazelcast partition, so different tasks
 * may be owned by different cluster nodes, distributing the load.
 *
 * @see SingleNodeTaskCoordinatorLocal
 */
public interface SingleNodeTaskCoordinator {

    /** Task name used by the {@link org.bonitasoft.engine.tenant.restart.RecoveryScheduler}. */
    String TASK_RECOVERY = "RECOVER_NODE";

    /** Task name used by the (Subscription-specific) {@link com.bonitasoft.engine.retention.DataRetentionScheduler}. */
    String TASK_CLEANUP_OBSOLETE_DATA = "CLEANUP_OBSOLETE_DATA";

    /**
     * Check whether the current node is responsible for executing the given task.
     *
     * @param taskName a stable identifier for the task (used as Hazelcast partition key in cluster mode)
     * @return {@code true} if this node should execute the task, {@code false} otherwise
     */
    boolean isResponsibleForTask(String taskName);
}
