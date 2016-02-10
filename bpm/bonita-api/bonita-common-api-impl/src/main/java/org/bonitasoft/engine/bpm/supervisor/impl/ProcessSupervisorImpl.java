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
package org.bonitasoft.engine.bpm.supervisor.impl;

import org.bonitasoft.engine.bpm.supervisor.ProcessSupervisor;

/**
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class ProcessSupervisorImpl implements ProcessSupervisor {

    private static final long serialVersionUID = 1034289223722146165L;

    private long supervisorId;

    private long processDefinitionId;

    private long userId;

    private long groupId;

    private long roleId;

    public ProcessSupervisorImpl(final long supervisorId, final long processDefinitionId) {
        super();
        this.supervisorId = supervisorId;
        this.processDefinitionId = processDefinitionId;
    }

    public ProcessSupervisorImpl() {
    }

    @Override
    public long getSupervisorId() {
        return supervisorId;
    }

    @Override
    public long getProcessDefinitionId() {
        return processDefinitionId;
    }

    @Override
    public long getUserId() {
        return userId;
    }

    public void setId(final long supervisorId) {
        this.supervisorId = supervisorId;
    }

    public void setProcessDefinitionId(final long processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public void setUserId(final long userId) {
        this.userId = userId;
    }

    @Override
    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(final long groupId) {
        this.groupId = groupId;
    }

    @Override
    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(final long roleId) {
        this.roleId = roleId;
    }

    public void setSupervisorId(final long supervisorId) {
        this.supervisorId = supervisorId;
    }

}
