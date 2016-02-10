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
package org.bonitasoft.engine.search.descriptor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.TaskPriority;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisor;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisorBuilderFactory;

/**
 * @author Julien Mege
 * @author Zhang Bole
 * @author Matthieu Chaffotte
 */
public class SearchHumanTaskInstanceDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> humanTaskInstanceDescriptorKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> humanTaskInstanceDescriptorAllFields;

    public SearchHumanTaskInstanceDescriptor() {
        final SUserTaskInstanceBuilderFactory keyProvider = BuilderFactory.get(SUserTaskInstanceBuilderFactory.class);
        humanTaskInstanceDescriptorKeys = new HashMap<String, FieldDescriptor>(13);
        humanTaskInstanceDescriptorKeys.put(HumanTaskInstanceSearchDescriptor.NAME,
                new FieldDescriptor(SHumanTaskInstance.class, keyProvider.getNameKey()));
        humanTaskInstanceDescriptorKeys.put(HumanTaskInstanceSearchDescriptor.PRIORITY,
                new FieldDescriptor(SHumanTaskInstance.class, keyProvider.getPriorityKey()));
        humanTaskInstanceDescriptorKeys.put(HumanTaskInstanceSearchDescriptor.DUE_DATE,
                new FieldDescriptor(SHumanTaskInstance.class, keyProvider.getExpectedEndDateKey()));
        humanTaskInstanceDescriptorKeys.put(HumanTaskInstanceSearchDescriptor.STATE_NAME, new FieldDescriptor(SHumanTaskInstance.class,
                keyProvider.getStateNameKey()));
        humanTaskInstanceDescriptorKeys.put(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, new FieldDescriptor(SHumanTaskInstance.class,
                keyProvider.getAssigneeIdKey()));
        humanTaskInstanceDescriptorKeys.put(HumanTaskInstanceSearchDescriptor.PROCESS_DEFINITION_ID, new FieldDescriptor(SHumanTaskInstance.class,
                keyProvider.getProcessDefinitionKey()));
        humanTaskInstanceDescriptorKeys.put(HumanTaskInstanceSearchDescriptor.PROCESS_INSTANCE_ID, new FieldDescriptor(SHumanTaskInstance.class,
                keyProvider.getRootProcessInstanceKey()));
        humanTaskInstanceDescriptorKeys.put(HumanTaskInstanceSearchDescriptor.PARENT_ACTIVITY_INSTANCE_ID, new FieldDescriptor(SHumanTaskInstance.class,
                keyProvider.getParentActivityInstanceKey()));
        humanTaskInstanceDescriptorKeys.put(HumanTaskInstanceSearchDescriptor.PARENT_CONTAINER_ID, new FieldDescriptor(SHumanTaskInstance.class,
                keyProvider.getParentContainerIdKey()));
        humanTaskInstanceDescriptorKeys.put(HumanTaskInstanceSearchDescriptor.DISPLAY_NAME, new FieldDescriptor(SHumanTaskInstance.class,
                keyProvider.getDisplayNameKey()));
        humanTaskInstanceDescriptorKeys.put(HumanTaskInstanceSearchDescriptor.REACHED_STATE_DATE, new FieldDescriptor(SHumanTaskInstance.class,
                keyProvider.getReachStateDateKey()));
        humanTaskInstanceDescriptorKeys.put(HumanTaskInstanceSearchDescriptor.USER_ID,
                new FieldDescriptor(SProcessSupervisor.class, BuilderFactory.get(SProcessSupervisorBuilderFactory.class).getUserIdKey()));
        humanTaskInstanceDescriptorKeys.put(HumanTaskInstanceSearchDescriptor.GROUP_ID,
                new FieldDescriptor(SProcessSupervisor.class, BuilderFactory.get(SProcessSupervisorBuilderFactory.class).getGroupIdKey()));
        humanTaskInstanceDescriptorKeys.put(HumanTaskInstanceSearchDescriptor.ROLE_ID,
                new FieldDescriptor(SProcessSupervisor.class, BuilderFactory.get(SProcessSupervisorBuilderFactory.class).getRoleIdKey()));

        humanTaskInstanceDescriptorAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> tasksInstanceFields = new HashSet<String>(3);
        tasksInstanceFields.add(keyProvider.getNameKey());
        tasksInstanceFields.add(keyProvider.getDisplayNameKey());
        humanTaskInstanceDescriptorAllFields.put(SHumanTaskInstance.class, tasksInstanceFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return humanTaskInstanceDescriptorKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return humanTaskInstanceDescriptorAllFields;
    }

    @Override
    protected Serializable convertFilterValue(final String filterField, final Serializable filterValue) {
        if (filterValue instanceof TaskPriority) {
            return STaskPriority.valueOf(((TaskPriority) filterValue).name());
        }
        return filterValue;
    }

}
