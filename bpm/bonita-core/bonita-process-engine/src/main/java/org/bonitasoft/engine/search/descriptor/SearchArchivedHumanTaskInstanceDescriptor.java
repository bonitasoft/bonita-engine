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

import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.archive.SAHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAUserTaskInstanceBuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Julien Mege
 * @author Matthieu Chaffotte
 */
public class SearchArchivedHumanTaskInstanceDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> entityKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> humanTaskInstanceAllFields;

    public SearchArchivedHumanTaskInstanceDescriptor() {
        final SAUserTaskInstanceBuilderFactory keyProvider = BuilderFactory.get(SAUserTaskInstanceBuilderFactory.class);
        entityKeys = new HashMap<String, FieldDescriptor>(10);
        entityKeys.put(ArchivedHumanTaskInstanceSearchDescriptor.NAME, new FieldDescriptor(SAHumanTaskInstance.class, keyProvider.getNameKey()));
        entityKeys.put(ArchivedHumanTaskInstanceSearchDescriptor.PRIORITY, new FieldDescriptor(SAHumanTaskInstance.class, keyProvider.getPriorityKey()));
        entityKeys.put(ArchivedHumanTaskInstanceSearchDescriptor.PROCESS_DEFINITION_ID,
                new FieldDescriptor(SAHumanTaskInstance.class, keyProvider.getProcessDefinitionKey()));
        entityKeys.put(ArchivedHumanTaskInstanceSearchDescriptor.PROCESS_INSTANCE_ID,
                new FieldDescriptor(SAHumanTaskInstance.class, keyProvider.getRootProcessInstanceKey()));
        entityKeys.put(ArchivedHumanTaskInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID,
                new FieldDescriptor(SAHumanTaskInstance.class, keyProvider.getRootProcessInstanceKey()));
        entityKeys.put(ArchivedHumanTaskInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID,
                new FieldDescriptor(SAHumanTaskInstance.class, keyProvider.getParentProcessInstanceKey()));
        entityKeys.put(ArchivedHumanTaskInstanceSearchDescriptor.ORIGINAL_HUMAN_TASK_ID,
                new FieldDescriptor(SAHumanTaskInstance.class, keyProvider.getSourceObjectIdKey()));
        entityKeys.put(ArchivedHumanTaskInstanceSearchDescriptor.PARENT_ACTIVITY_INSTANCE_ID,
                new FieldDescriptor(SAHumanTaskInstance.class, keyProvider.getParentActivityInstanceKey()));
        entityKeys.put(ArchivedHumanTaskInstanceSearchDescriptor.STATE_NAME, new FieldDescriptor(SAHumanTaskInstance.class, keyProvider.getStateNameKey()));
        entityKeys.put(ArchivedHumanTaskInstanceSearchDescriptor.ASSIGNEE_ID,
                new FieldDescriptor(SAHumanTaskInstance.class, keyProvider.getAssigneeIdKey()));
        entityKeys.put(ArchivedHumanTaskInstanceSearchDescriptor.DISPLAY_NAME,
                new FieldDescriptor(SAHumanTaskInstance.class, keyProvider.getDisplayNameKey()));
        entityKeys.put(ArchivedHumanTaskInstanceSearchDescriptor.REACHED_STATE_DATE,
                new FieldDescriptor(SAHumanTaskInstance.class, keyProvider.getReachedStateDateKey()));
        entityKeys.put(ArchivedHumanTaskInstanceSearchDescriptor.TERMINAL, new FieldDescriptor(SAHumanTaskInstance.class, keyProvider.getTerminalKey()));
        entityKeys.put(ArchivedHumanTaskInstanceSearchDescriptor.ARCHIVE_DATE, new FieldDescriptor(SAHumanTaskInstance.class, keyProvider.getArchivedDateKey()));

        humanTaskInstanceAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> humanFields = new HashSet<String>(2);
        humanFields.add(keyProvider.getNameKey());
        humanFields.add(keyProvider.getDisplayNameKey());
        humanTaskInstanceAllFields.put(SAHumanTaskInstance.class, humanFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return entityKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return humanTaskInstanceAllFields;
    }

}
