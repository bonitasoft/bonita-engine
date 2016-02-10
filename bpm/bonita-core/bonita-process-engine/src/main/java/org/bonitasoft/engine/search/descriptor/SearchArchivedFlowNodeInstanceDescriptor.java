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

import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAFlowNodeInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAUserTaskInstanceBuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class SearchArchivedFlowNodeInstanceDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> archFlowNodeDescriptorKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> flowNodeInstanceDescriptorAllFields;

    public SearchArchivedFlowNodeInstanceDescriptor() {
        final SAFlowNodeInstanceBuilderFactory keyProvider = BuilderFactory.get(SAUserTaskInstanceBuilderFactory.class);
        archFlowNodeDescriptorKeys = new HashMap<String, FieldDescriptor>(13);
        archFlowNodeDescriptorKeys.put(ArchivedFlowNodeInstanceSearchDescriptor.NAME,
                new FieldDescriptor(SAFlowNodeInstance.class, keyProvider.getNameKey()));
        archFlowNodeDescriptorKeys.put(ArchivedFlowNodeInstanceSearchDescriptor.STATE_NAME,
                new FieldDescriptor(SAFlowNodeInstance.class, keyProvider.getStateNameKey()));
        archFlowNodeDescriptorKeys.put(ArchivedFlowNodeInstanceSearchDescriptor.PROCESS_DEFINITION_ID, new FieldDescriptor(SAFlowNodeInstance.class,
                keyProvider.getProcessDefinitionKey()));
        archFlowNodeDescriptorKeys.put(ArchivedFlowNodeInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID, new FieldDescriptor(SAFlowNodeInstance.class,
                keyProvider.getParentProcessInstanceKey()));
        archFlowNodeDescriptorKeys.put(ArchivedFlowNodeInstanceSearchDescriptor.PARENT_ACTIVITY_INSTANCE_ID, new FieldDescriptor(SAFlowNodeInstance.class,
                keyProvider.getParentActivityInstanceKey()));
        archFlowNodeDescriptorKeys.put(ArchivedFlowNodeInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID, new FieldDescriptor(SAFlowNodeInstance.class,
                keyProvider.getRootProcessInstanceKey()));
        archFlowNodeDescriptorKeys.put(ArchivedFlowNodeInstanceSearchDescriptor.DISPLAY_NAME,
                new FieldDescriptor(SAFlowNodeInstance.class, keyProvider.getDisplayNameKey()));
        archFlowNodeDescriptorKeys.put(ArchivedFlowNodeInstanceSearchDescriptor.FLOW_NODE_TYPE, new FieldDescriptor(SAFlowNodeInstance.class,
                keyProvider.getKindKey()));
        archFlowNodeDescriptorKeys.put(ArchivedFlowNodeInstanceSearchDescriptor.ORIGINAL_FLOW_NODE_ID, new FieldDescriptor(SAFlowNodeInstance.class,
                keyProvider.getSourceObjectIdKey()));
        archFlowNodeDescriptorKeys.put(ArchivedFlowNodeInstanceSearchDescriptor.TERMINAL,
                new FieldDescriptor(SAFlowNodeInstance.class, keyProvider.getTerminalKey()));
        archFlowNodeDescriptorKeys.put(ArchivedFlowNodeInstanceSearchDescriptor.REACHED_STATE_DATE,
                new FieldDescriptor(SAFlowNodeInstance.class, keyProvider.getReachedStateDateKey()));
        archFlowNodeDescriptorKeys.put(ArchivedFlowNodeInstanceSearchDescriptor.ARCHIVE_DATE,
                new FieldDescriptor(SAFlowNodeInstance.class, keyProvider.getArchivedDateKey()));
        archFlowNodeDescriptorKeys.put(ArchivedFlowNodeInstanceSearchDescriptor.STATE_ID,
                new FieldDescriptor(SAFlowNodeInstance.class, keyProvider.getStateIdKey()));

        final Set<String> tasksInstanceFields = new HashSet<String>(2);
        tasksInstanceFields.add(keyProvider.getNameKey());
        tasksInstanceFields.add(keyProvider.getDisplayNameKey());
        flowNodeInstanceDescriptorAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        flowNodeInstanceDescriptorAllFields.put(SAFlowNodeInstance.class, tasksInstanceFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return archFlowNodeDescriptorKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return flowNodeInstanceDescriptorAllFields;
    }

}
