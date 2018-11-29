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
package org.bonitasoft.engine.supervisor.mapping.model.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisor;

/**
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
@Data
@NoArgsConstructor
public class SProcessSupervisorImpl implements SProcessSupervisor {

    private long id;
    private long tenantId;
    private long processDefId;
    private long userId = -1;
    private long groupId = -1;
    private long roleId = -1;

    public SProcessSupervisorImpl(final long processDefId) {
        this.processDefId = processDefId;
    }

    public SProcessSupervisorImpl(final long id, final long tenantId, final long processDefId, final long userId, final long groupId, final long roleId) {
        this.id = id;
        this.tenantId = tenantId;
        this.processDefId = processDefId;
        this.userId = userId;
        this.groupId = groupId;
        this.roleId = roleId;
    }

}
