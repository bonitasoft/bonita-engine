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

import static org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SUserTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisor;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisorBuilderFactory;

/**
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SearchProcessInstanceDescriptor extends SearchEntityDescriptor {

    protected final Map<String, FieldDescriptor> searchEntityKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> processInstanceAllFields;

    protected final Set<String> processFields;

    public SearchProcessInstanceDescriptor() {
        final SProcessInstanceBuilderFactory instanceBuilder = BuilderFactory.get(SProcessInstanceBuilderFactory.class);
        final SUserTaskInstanceBuilderFactory sUserTaskInstanceBuilder = BuilderFactory.get(SUserTaskInstanceBuilderFactory.class);

        searchEntityKeys = new HashMap<>();
        searchEntityKeys.put(NAME, new FieldDescriptor(SProcessInstance.class, instanceBuilder.getNameKey()));
        searchEntityKeys.put(PROCESS_DEFINITION_ID,
                new FieldDescriptor(SProcessInstance.class, instanceBuilder.getProcessDefinitionIdKey()));
        searchEntityKeys.put(LAST_UPDATE,
                new FieldDescriptor(SProcessInstance.class, instanceBuilder.getLastUpdateKey()));
        searchEntityKeys.put(START_DATE,
                new FieldDescriptor(SProcessInstance.class, instanceBuilder.getStartDateKey()));
        searchEntityKeys.put(END_DATE, new FieldDescriptor(SProcessInstance.class, instanceBuilder.getEndDateKey()));
        searchEntityKeys.put(STATE_ID, new FieldDescriptor(SProcessInstance.class, instanceBuilder.getStateIdKey()));
        searchEntityKeys.put(STATE_NAME, new FieldDescriptor(SProcessInstance.class, instanceBuilder.getStateIdKey()));
        searchEntityKeys.put(ID, new FieldDescriptor(SProcessInstance.class, instanceBuilder.getIdKey()));
        searchEntityKeys.put(STARTED_BY,
                new FieldDescriptor(SProcessInstance.class, instanceBuilder.getStartedByKey()));
        searchEntityKeys.put(CALLER_ID, new FieldDescriptor(SProcessInstance.class, instanceBuilder.getCallerIdKey()));
        searchEntityKeys.put(PROCESS_SUPERVISOR_USER_ID, new FieldDescriptor(SProcessSupervisor.class,
                BuilderFactory.get(SProcessSupervisorBuilderFactory.class).getUserIdKey()));
        searchEntityKeys.put(PROCESS_SUPERVISOR_GROUP_ID, new FieldDescriptor(SProcessSupervisor.class,
                BuilderFactory.get(SProcessSupervisorBuilderFactory.class).getGroupIdKey()));
        searchEntityKeys.put(PROCESS_SUPERVISOR_ROLE_ID, new FieldDescriptor(SProcessSupervisor.class,
                BuilderFactory.get(SProcessSupervisorBuilderFactory.class).getRoleIdKey()));
        searchEntityKeys.put(ASSIGNEE_ID,
                new FieldDescriptor(SUserTaskInstance.class, sUserTaskInstanceBuilder.getAssigneeIdKey()));

        processInstanceAllFields = new HashMap<>();
        processFields = new HashSet<>();
        processFields.add(instanceBuilder.getNameKey());
        processInstanceAllFields.put(SProcessInstance.class, processFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return searchEntityKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return processInstanceAllFields;
    }

    @Override
    protected Serializable convertFilterValue(final String filterField, final Serializable filterValue) {
        if (STATE_NAME.equals(filterField)) {
            if (filterValue instanceof String) {
                return ProcessInstanceState.valueOf((String) filterValue).getId();
            } else if (filterValue instanceof ProcessInstanceState) {
                return ((ProcessInstanceState) filterValue).getId();
            } else {
                throw new IllegalArgumentException("The state name must be a String or a ProcessInstanceState !!");
            }
        }
        return filterValue;
    }
}
