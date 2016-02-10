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

import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.archive.SAActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAUserTaskInstanceBuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Yanyan Liu
 * @author Celine Souchet
 */
public class SearchArchivedActivityInstanceDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> archivedActivityInstanceDescriptorKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> archivedActivityInstanceDescriptorAllFields;

    public SearchArchivedActivityInstanceDescriptor() {
        final SAUserTaskInstanceBuilderFactory keyProvider = BuilderFactory.get(SAUserTaskInstanceBuilderFactory.class);
        archivedActivityInstanceDescriptorKeys = new HashMap<>(10);
        archivedActivityInstanceDescriptorKeys.put(ArchivedActivityInstanceSearchDescriptor.NAME,
                new FieldDescriptor(SAActivityInstance.class, keyProvider.getNameKey()));
        archivedActivityInstanceDescriptorKeys.put(ArchivedActivityInstanceSearchDescriptor.PRIORITY, new FieldDescriptor(SAActivityInstance.class,
                keyProvider.getPriorityKey()));
        archivedActivityInstanceDescriptorKeys.put(ArchivedActivityInstanceSearchDescriptor.PROCESS_DEFINITION_ID, new FieldDescriptor(
                SAActivityInstance.class, keyProvider.getProcessDefinitionKey()));
        archivedActivityInstanceDescriptorKeys.put(ArchivedActivityInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID, new FieldDescriptor(
                SAActivityInstance.class, keyProvider.getRootProcessInstanceKey()));
        archivedActivityInstanceDescriptorKeys.put(ArchivedActivityInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID, new FieldDescriptor(
                SAActivityInstance.class, keyProvider.getParentProcessInstanceKey()));
        archivedActivityInstanceDescriptorKeys.put(ArchivedActivityInstanceSearchDescriptor.PARENT_ACTIVITY_INSTANCE_ID, new FieldDescriptor(
                SAActivityInstance.class, keyProvider.getParentActivityInstanceKey()));
        archivedActivityInstanceDescriptorKeys.put(ArchivedActivityInstanceSearchDescriptor.STATE_NAME, new FieldDescriptor(SAActivityInstance.class,
                keyProvider.getStateNameKey()));
        archivedActivityInstanceDescriptorKeys.put(ArchivedActivityInstanceSearchDescriptor.ASSIGNEE_ID, new FieldDescriptor(SAActivityInstance.class,
                keyProvider.getAssigneeIdKey()));
        archivedActivityInstanceDescriptorKeys.put(ArchivedActivityInstanceSearchDescriptor.DISPLAY_NAME, new FieldDescriptor(SAActivityInstance.class,
                keyProvider.getDisplayNameKey()));
        archivedActivityInstanceDescriptorKeys.put(ArchivedActivityInstanceSearchDescriptor.REACHED_STATE_DATE, new FieldDescriptor(SAActivityInstance.class,
                keyProvider.getReachedStateDateKey()));
        archivedActivityInstanceDescriptorKeys.put(ArchivedActivityInstanceSearchDescriptor.SOURCE_OBJECT_ID, new FieldDescriptor(SAActivityInstance.class,
                keyProvider.getSourceObjectIdKey()));
        archivedActivityInstanceDescriptorKeys.put(ArchivedActivityInstanceSearchDescriptor.ARCHIVE_DATE,
                new FieldDescriptor(SAActivityInstance.class, keyProvider.getArchivedDateKey()));

        archivedActivityInstanceDescriptorAllFields = new HashMap<>(1);
        final Set<String> humanFields = new HashSet<String>(2);
        humanFields.add(keyProvider.getNameKey());
        humanFields.add(keyProvider.getDisplayNameKey());
        archivedActivityInstanceDescriptorAllFields.put(SAActivityInstance.class, humanFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return archivedActivityInstanceDescriptorKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return archivedActivityInstanceDescriptorAllFields;
    }

}
