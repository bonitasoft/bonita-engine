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

import static org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.SUserTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAProcessInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisor;

/**
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SearchArchivedProcessInstancesDescriptor extends SearchEntityDescriptor {

    protected final Map<String, FieldDescriptor> searchEntityKeys;

    protected final Map<Class<? extends PersistentObject>, Set<String>> archivedProcessInstanceAllFields;

    protected final Set<String> processInstanceFields;

    public SearchArchivedProcessInstancesDescriptor() {
        final SAProcessInstanceBuilderFactory instanceBuilder = BuilderFactory
                .get(SAProcessInstanceBuilderFactory.class);
        final SUserTaskInstanceBuilderFactory sUserTaskInstanceBuilder = BuilderFactory
                .get(SUserTaskInstanceBuilderFactory.class);

        searchEntityKeys = new HashMap<>();
        searchEntityKeys.put(NAME, new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getNameKey()));
        searchEntityKeys.put(PROCESS_DEFINITION_ID,
                new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getProcessDefinitionIdKey()));
        searchEntityKeys.put(ID, new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getIdKey()));
        searchEntityKeys.put(STARTED_BY,
                new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getStartedByKey()));
        searchEntityKeys.put(STARTED_BY_SUBSTITUTE,
                new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getStartedBySubstituteKey()));
        searchEntityKeys.put(START_DATE,
                new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getStartDateKey()));
        searchEntityKeys.put(END_DATE, new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getEndDateKey()));
        searchEntityKeys.put(STATE_ID, new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getStateIdKey()));
        searchEntityKeys.put(SOURCE_OBJECT_ID,
                new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getSourceObjectIdKey()));
        searchEntityKeys.put(LAST_UPDATE,
                new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getLastUpdateKey()));
        searchEntityKeys.put(ARCHIVE_DATE,
                new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getArchiveDateKey()));
        searchEntityKeys.put(CALLER_ID,
                new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getCallerIdKey()));
        searchEntityKeys
                .put(USER_ID, new FieldDescriptor(SProcessSupervisor.class,
                        SProcessSupervisor.USER_ID_KEY));
        searchEntityKeys.put(GROUP_ID,
                new FieldDescriptor(SProcessSupervisor.class,
                        SProcessSupervisor.GROUP_ID_KEY));
        searchEntityKeys
                .put(ROLE_ID, new FieldDescriptor(SProcessSupervisor.class,
                        SProcessSupervisor.ROLE_ID_KEY));
        searchEntityKeys.put(ASSIGNEE_ID,
                new FieldDescriptor(SUserTaskInstance.class, sUserTaskInstanceBuilder.getAssigneeIdKey()));

        searchEntityKeys.put(STRING_INDEX_1,
                new FieldDescriptor(SAProcessInstance.class, STRING_INDEX_1));
        searchEntityKeys.put(STRING_INDEX_2,
                new FieldDescriptor(SAProcessInstance.class, STRING_INDEX_2));
        searchEntityKeys.put(STRING_INDEX_3,
                new FieldDescriptor(SAProcessInstance.class, STRING_INDEX_3));
        searchEntityKeys.put(STRING_INDEX_4,
                new FieldDescriptor(SAProcessInstance.class, STRING_INDEX_4));
        searchEntityKeys.put(STRING_INDEX_5,
                new FieldDescriptor(SAProcessInstance.class, STRING_INDEX_5));

        archivedProcessInstanceAllFields = new HashMap<>();
        processInstanceFields = new HashSet<>();
        processInstanceFields.add(instanceBuilder.getNameKey());
        processInstanceFields.add(STRING_INDEX_1);
        processInstanceFields.add(STRING_INDEX_2);
        processInstanceFields.add(STRING_INDEX_3);
        processInstanceFields.add(STRING_INDEX_4);
        processInstanceFields.add(STRING_INDEX_5);
        archivedProcessInstanceAllFields.put(SAProcessInstance.class, processInstanceFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return searchEntityKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return archivedProcessInstanceAllFields;
    }

}
