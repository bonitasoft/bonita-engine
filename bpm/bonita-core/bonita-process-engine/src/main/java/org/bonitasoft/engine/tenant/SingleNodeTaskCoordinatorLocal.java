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

import static org.bonitasoft.engine.Profiles.NOT_IN_CLUSTER;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Local (single-node) implementation of {@link SingleNodeTaskCoordinator}.
 * Always returns {@code true} since there is only one node to run any task.
 */
@Component
@Profile(NOT_IN_CLUSTER)
public class SingleNodeTaskCoordinatorLocal implements SingleNodeTaskCoordinator {

    @Override
    public boolean isResponsibleForTask(String taskName) {
        return true;
    }
}
