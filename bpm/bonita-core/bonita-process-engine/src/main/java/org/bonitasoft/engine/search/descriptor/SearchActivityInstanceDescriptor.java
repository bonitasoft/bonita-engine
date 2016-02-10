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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.bpm.flownode.ActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisor;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisorBuilderFactory;

/**
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 */
public class SearchActivityInstanceDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> activityInstanceDescriptorKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> activityInstanceDescriptorAllFields;

    public SearchActivityInstanceDescriptor() {
        final SUserTaskInstanceBuilderFactory keyProvider = BuilderFactory.get(SUserTaskInstanceBuilderFactory.class);
        activityInstanceDescriptorKeys = new HashMap<String, FieldDescriptor>(11);
        activityInstanceDescriptorKeys.put(ActivityInstanceSearchDescriptor.NAME,
                new FieldDescriptor(SActivityInstance.class, keyProvider.getNameKey()));
        activityInstanceDescriptorKeys.put(ActivityInstanceSearchDescriptor.STATE_NAME,
                new FieldDescriptor(SActivityInstance.class, keyProvider.getStateNameKey()));
        activityInstanceDescriptorKeys.put(ActivityInstanceSearchDescriptor.PROCESS_DEFINITION_ID, new FieldDescriptor(SActivityInstance.class,
                keyProvider.getProcessDefinitionKey()));
        activityInstanceDescriptorKeys.put(ActivityInstanceSearchDescriptor.PROCESS_INSTANCE_ID, new FieldDescriptor(SActivityInstance.class,
                keyProvider.getRootProcessInstanceKey()));
        activityInstanceDescriptorKeys.put(ActivityInstanceSearchDescriptor.PARENT_ACTIVITY_INSTANCE_ID, new FieldDescriptor(SActivityInstance.class,
                keyProvider.getParentActivityInstanceKey()));
        activityInstanceDescriptorKeys.put(ActivityInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID, new FieldDescriptor(SActivityInstance.class,
                keyProvider.getParentProcessInstanceKey()));
        activityInstanceDescriptorKeys.put(ActivityInstanceSearchDescriptor.PARENT_CONTAINER_ID, new FieldDescriptor(SActivityInstance.class,
                keyProvider.getParentContainerIdKey()));
        activityInstanceDescriptorKeys.put(ActivityInstanceSearchDescriptor.LAST_MODIFICATION_DATE, new FieldDescriptor(SActivityInstance.class,
                keyProvider.getLastUpdateDateKey()));
        activityInstanceDescriptorKeys.put(ActivityInstanceSearchDescriptor.DISPLAY_NAME,
                new FieldDescriptor(SActivityInstance.class, keyProvider.getDisplayNameKey()));
        activityInstanceDescriptorKeys.put(ActivityInstanceSearchDescriptor.USER_ID,
                new FieldDescriptor(SProcessSupervisor.class, BuilderFactory.get(SProcessSupervisorBuilderFactory.class).getUserIdKey()));
        activityInstanceDescriptorKeys.put(ActivityInstanceSearchDescriptor.GROUP_ID,
                new FieldDescriptor(SProcessSupervisor.class, BuilderFactory.get(SProcessSupervisorBuilderFactory.class).getGroupIdKey()));
        activityInstanceDescriptorKeys.put(ActivityInstanceSearchDescriptor.ROLE_ID,
                new FieldDescriptor(SProcessSupervisor.class, BuilderFactory.get(SProcessSupervisorBuilderFactory.class).getRoleIdKey()));

        activityInstanceDescriptorAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> tasksInstanceFields = new HashSet<String>(2);
        tasksInstanceFields.add(keyProvider.getNameKey());
        tasksInstanceFields.add(keyProvider.getDisplayNameKey());
        activityInstanceDescriptorAllFields.put(SActivityInstance.class, tasksInstanceFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return activityInstanceDescriptorKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return activityInstanceDescriptorAllFields;
    }

}
