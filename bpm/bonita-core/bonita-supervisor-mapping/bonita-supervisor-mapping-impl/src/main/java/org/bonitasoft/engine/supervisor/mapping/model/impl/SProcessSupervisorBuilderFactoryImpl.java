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

import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisorBuilder;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisorBuilderFactory;

/**
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class SProcessSupervisorBuilderFactoryImpl implements SProcessSupervisorBuilderFactory {

    private static final String ID_KEY = "id";

    private static final String USER_ID_KEY = "userId";

    private static final String GROUP_ID_KEY = "groupId";

    private static final String ROLE_ID_KEY = "roleId";

    private static final String PROCESS_DEF_ID_KEY = "processDefId";

    @Override
    public SProcessSupervisorBuilder createNewInstance(final long processDefId) {
        final SProcessSupervisorImpl supervisor = new SProcessSupervisorImpl(processDefId);
        return new SProcessSupervisorBuilderImpl(supervisor);
    }

    @Override
    public String getIdKey() {
        return ID_KEY;
    }

    @Override
    public String getUserIdKey() {
        return USER_ID_KEY;
    }

    @Override
    public String getProcessDefIdKey() {
        return PROCESS_DEF_ID_KEY;
    }

    @Override
    public String getGroupIdKey() {
        return GROUP_ID_KEY;
    }

    @Override
    public String getRoleIdKey() {
        return ROLE_ID_KEY;
    }

}
