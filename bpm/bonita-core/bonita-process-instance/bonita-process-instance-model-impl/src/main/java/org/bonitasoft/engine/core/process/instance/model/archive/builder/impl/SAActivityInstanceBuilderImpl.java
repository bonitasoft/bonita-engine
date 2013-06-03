/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.instance.model.archive.builder.impl;

import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAActivityInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.archive.impl.SAActivityInstanceImpl;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public abstract class SAActivityInstanceBuilderImpl extends SAFlowNodeInstanceBuilderImpl implements SAActivityInstanceBuilder {

    private static final String PRIORITY_KEY = "priority";

    private static final String ASSIGNEE_ID_KEY = "assigneeId";

    private static final String ACTIVITY_INSTANCE_ID_KEY = "activityInstanceId";

    private SAActivityInstanceImpl saActivityInstanceImpl;

    @Override
    public String getPriorityKey() {
        return PRIORITY_KEY;
    }

    @Override
    public String getActivityInstanceIdKey() {
        return ACTIVITY_INSTANCE_ID_KEY;
    }

    @Override
    public String getAssigneeIdKey() {
        return ASSIGNEE_ID_KEY;
    }

    @Override
    public SAActivityInstanceBuilder setParentActivityInstanceId(final long parentActivityInstanceId) {
        saActivityInstanceImpl.setLogicalGroup(PROCESS_DEFINITION_INDEX, parentActivityInstanceId);
        return this;
    }

    @Override
    public SAActivityInstanceBuilder setProcessDefinitionId(final long processDefinitionId) {
        saActivityInstanceImpl.setLogicalGroup(PROCESS_DEFINITION_INDEX, processDefinitionId);
        return this;
    }

    @Override
    public SAActivityInstanceBuilder setRootProcessInstanceId(final long processInstanceId) {
        saActivityInstanceImpl.setLogicalGroup(ROOT_PROCESS_INSTANCE_INDEX, processInstanceId);
        return this;
    }

}
