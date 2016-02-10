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

import org.bonitasoft.engine.bpm.flownode.FlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SFlowNodeInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Elias Ricken de Medeiros
 */
public class SearchFlowNodeInstanceDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> flowNodeInstanceDescriptorKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> flowNodeInstanceDescriptorAllFields;

    public SearchFlowNodeInstanceDescriptor() {
        final SFlowNodeInstanceBuilderFactory keyProvider = BuilderFactory.get(SUserTaskInstanceBuilderFactory.class);
        flowNodeInstanceDescriptorKeys = new HashMap<String, FieldDescriptor>(8);
        flowNodeInstanceDescriptorKeys.put(FlowNodeInstanceSearchDescriptor.NAME,
                new FieldDescriptor(SFlowNodeInstance.class, keyProvider.getNameKey()));
        flowNodeInstanceDescriptorKeys.put(FlowNodeInstanceSearchDescriptor.STATE_NAME,
                new FieldDescriptor(SFlowNodeInstance.class, keyProvider.getStateNameKey()));
        flowNodeInstanceDescriptorKeys.put(FlowNodeInstanceSearchDescriptor.PROCESS_DEFINITION_ID, new FieldDescriptor(SFlowNodeInstance.class,
                keyProvider.getProcessDefinitionKey()));
        flowNodeInstanceDescriptorKeys.put(FlowNodeInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID, new FieldDescriptor(SFlowNodeInstance.class,
                keyProvider.getParentProcessInstanceKey()));
        flowNodeInstanceDescriptorKeys.put(FlowNodeInstanceSearchDescriptor.PARENT_ACTIVITY_INSTANCE_ID, new FieldDescriptor(SFlowNodeInstance.class,
                keyProvider.getParentActivityInstanceKey()));
        flowNodeInstanceDescriptorKeys.put(FlowNodeInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID, new FieldDescriptor(SFlowNodeInstance.class,
                keyProvider.getRootProcessInstanceKey()));
        flowNodeInstanceDescriptorKeys.put(FlowNodeInstanceSearchDescriptor.DISPLAY_NAME,
                new FieldDescriptor(SFlowNodeInstance.class, keyProvider.getDisplayNameKey()));
        flowNodeInstanceDescriptorKeys.put(FlowNodeInstanceSearchDescriptor.STATE_CATEGORY,
                new FieldDescriptor(SFlowNodeInstance.class, keyProvider.getStateCategoryKey()));
        flowNodeInstanceDescriptorKeys.put(FlowNodeInstanceSearchDescriptor.LAST_UPDATE_DATE,
                new FieldDescriptor(SFlowNodeInstance.class, keyProvider.getLastUpdateDateKey()));

        final Set<String> tasksInstanceFields = new HashSet<String>(2);
        tasksInstanceFields.add(keyProvider.getNameKey());
        tasksInstanceFields.add(keyProvider.getDisplayNameKey());
        flowNodeInstanceDescriptorAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        flowNodeInstanceDescriptorAllFields.put(SFlowNodeInstance.class, tasksInstanceFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return flowNodeInstanceDescriptorKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return flowNodeInstanceDescriptorAllFields;
    }

}
