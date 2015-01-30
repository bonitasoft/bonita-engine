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
package org.bonitasoft.engine.core.process.instance.model.archive.builder.impl;

import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAActivityInstanceBuilderFactory;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public abstract class SAActivityInstanceBuilderFactoryImpl extends SAFlowNodeInstanceBuilderFactoryImpl implements SAActivityInstanceBuilderFactory {

    protected static final String PRIORITY_KEY = "priority";

    protected static final String ASSIGNEE_ID_KEY = "assigneeId";

    protected static final String ACTIVITY_INSTANCE_ID_KEY = "activityInstanceId";

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

}
