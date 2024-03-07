/**
 * Copyright (C) 2019 Bonitasoft S.A.
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

/**
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 */
public class SearchActivityInstanceDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> activityInstanceDescriptorKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> activityInstanceDescriptorAllFields;

    public SearchActivityInstanceDescriptor() {
        final SUserTaskInstanceBuilderFactory keyProvider = BuilderFactory.get(SUserTaskInstanceBuilderFactory.class);
        activityInstanceDescriptorKeys = new HashMap<>();
        activityInstanceDescriptorKeys.put(ActivityInstanceSearchDescriptor.NAME,
                new FieldDescriptor(SActivityInstance.class, keyProvider.getNameKey()));
        activityInstanceDescriptorKeys.put(ActivityInstanceSearchDescriptor.STATE_NAME,
                new FieldDescriptor(SActivityInstance.class, keyProvider.getStateNameKey()));
        activityInstanceDescriptorKeys.put(ActivityInstanceSearchDescriptor.PROCESS_DEFINITION_ID,
                new FieldDescriptor(SActivityInstance.class,
                        keyProvider.getProcessDefinitionKey()));
        activityInstanceDescriptorKeys.put(ActivityInstanceSearchDescriptor.PROCESS_INSTANCE_ID,
                new FieldDescriptor(SActivityInstance.class,
                        keyProvider.getRootProcessInstanceKey()));
        activityInstanceDescriptorKeys.put(ActivityInstanceSearchDescriptor.PARENT_ACTIVITY_INSTANCE_ID,
                new FieldDescriptor(SActivityInstance.class,
                        keyProvider.getParentActivityInstanceKey()));
        activityInstanceDescriptorKeys.put(ActivityInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID,
                new FieldDescriptor(SActivityInstance.class,
                        keyProvider.getParentProcessInstanceKey()));
        activityInstanceDescriptorKeys.put(ActivityInstanceSearchDescriptor.PARENT_CONTAINER_ID,
                new FieldDescriptor(SActivityInstance.class,
                        keyProvider.getParentContainerIdKey()));
        activityInstanceDescriptorKeys.put(ActivityInstanceSearchDescriptor.LAST_MODIFICATION_DATE,
                new FieldDescriptor(SActivityInstance.class,
                        keyProvider.getLastUpdateDateKey()));
        activityInstanceDescriptorKeys.put(ActivityInstanceSearchDescriptor.DISPLAY_NAME,
                new FieldDescriptor(SActivityInstance.class, keyProvider.getDisplayNameKey()));
        activityInstanceDescriptorKeys.put(ActivityInstanceSearchDescriptor.USER_ID,
                new FieldDescriptor(SProcessSupervisor.class, SProcessSupervisor.USER_ID_KEY));
        activityInstanceDescriptorKeys.put(ActivityInstanceSearchDescriptor.GROUP_ID,
                new FieldDescriptor(SProcessSupervisor.class, SProcessSupervisor.GROUP_ID_KEY));
        activityInstanceDescriptorKeys.put(ActivityInstanceSearchDescriptor.ROLE_ID,
                new FieldDescriptor(SProcessSupervisor.class, SProcessSupervisor.ROLE_ID_KEY));

        activityInstanceDescriptorAllFields = new HashMap<>();
        final Set<String> tasksInstanceFields = new HashSet<>();
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
