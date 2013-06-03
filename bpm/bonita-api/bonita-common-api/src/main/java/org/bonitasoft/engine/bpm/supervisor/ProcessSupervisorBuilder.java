/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.supervisor;

import org.bonitasoft.engine.bpm.supervisor.impl.ProcessSupervisorImpl;

/**
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class ProcessSupervisorBuilder {

    private final ProcessSupervisorImpl supervisor;

    public ProcessSupervisorBuilder() {
        supervisor = new ProcessSupervisorImpl();
    }

    public ProcessSupervisorBuilder(final long supervisorId, final long processDefinitionId) {
        super();
        supervisor = new ProcessSupervisorImpl(supervisorId, processDefinitionId);
    }

    public ProcessSupervisorBuilder setSupervisorId(final long supervisorId) {
        supervisor.setId(supervisorId);
        return this;
    }

    public ProcessSupervisorBuilder setProcessDefinitionId(final long processDefinitionId) {
        supervisor.setProcessDefinitionId(processDefinitionId);
        return this;
    }

    public ProcessSupervisorBuilder setUserId(final long userId) {
        supervisor.setUserId(userId);
        return this;
    }

    public ProcessSupervisorBuilder setGroupId(final long groupId) {
        supervisor.setGroupId(groupId);
        return this;
    }

    public ProcessSupervisorBuilder setRoleId(final long roleId) {
        supervisor.setRoleId(roleId);
        return this;
    }

    public ProcessSupervisor done() {
        return supervisor;
    }

}
