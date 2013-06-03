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
package org.bonitasoft.engine.search.descriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.bpm.flownode.FlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.builder.SFlowNodeInstanceBuilder;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Elias Ricken de Medeiros
 */
public class SearchFlowNodeInstanceDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> flowNodeInstanceDescriptorKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> flowNodeInstanceDescriptorAllFields;

    public SearchFlowNodeInstanceDescriptor(final BPMInstanceBuilders bpmInstanceBuilders, final FlowNodeStateManager flowNodeStateManager) {
        final SFlowNodeInstanceBuilder flowNodeKeyProvider = bpmInstanceBuilders.getSUserTaskInstanceBuilder();
        flowNodeInstanceDescriptorKeys = new HashMap<String, FieldDescriptor>(8);
        flowNodeInstanceDescriptorKeys.put(FlowNodeInstanceSearchDescriptor.NAME,
                new FieldDescriptor(SFlowNodeInstance.class, flowNodeKeyProvider.getNameKey()));
        flowNodeInstanceDescriptorKeys.put(FlowNodeInstanceSearchDescriptor.STATE_NAME,
                new FieldDescriptor(SFlowNodeInstance.class, flowNodeKeyProvider.getStateNameKey()));
        flowNodeInstanceDescriptorKeys.put(FlowNodeInstanceSearchDescriptor.PROCESS_DEFINITION_ID, new FieldDescriptor(SFlowNodeInstance.class,
                flowNodeKeyProvider.getProcessDefinitionKey()));
        flowNodeInstanceDescriptorKeys.put(FlowNodeInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID, new FieldDescriptor(SFlowNodeInstance.class,
                flowNodeKeyProvider.getParentProcessInstanceKey()));
        flowNodeInstanceDescriptorKeys.put(FlowNodeInstanceSearchDescriptor.PARENT_ACTIVITY_INSTANCE_ID, new FieldDescriptor(SFlowNodeInstance.class,
                flowNodeKeyProvider.getParentActivityInstanceKey()));
        flowNodeInstanceDescriptorKeys.put(FlowNodeInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID, new FieldDescriptor(SFlowNodeInstance.class,
                flowNodeKeyProvider.getRootProcessInstanceKey()));
        flowNodeInstanceDescriptorKeys.put(FlowNodeInstanceSearchDescriptor.DISPLAY_NAME,
                new FieldDescriptor(SFlowNodeInstance.class, flowNodeKeyProvider.getDisplayNameKey()));
        flowNodeInstanceDescriptorKeys.put(FlowNodeInstanceSearchDescriptor.STATE_CATEGORY,
                new FieldDescriptor(SFlowNodeInstance.class, flowNodeKeyProvider.getStateCategoryKey()));

        final Set<String> tasksInstanceFields = new HashSet<String>(2);
        tasksInstanceFields.add(flowNodeKeyProvider.getNameKey());
        tasksInstanceFields.add(flowNodeKeyProvider.getDisplayNameKey());
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
