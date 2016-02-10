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

import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
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

    protected final Map<Class<? extends PersistentObject>, Set<String>> processInstanceAllFields;

    protected final Set<String> processFields;

    public SearchProcessInstanceDescriptor() {
        final SProcessInstanceBuilderFactory instanceBuilder = BuilderFactory.get(SProcessInstanceBuilderFactory.class);
        final SUserTaskInstanceBuilderFactory sUserTaskInstanceBuilder = BuilderFactory.get(SUserTaskInstanceBuilderFactory.class);

        searchEntityKeys = new HashMap<String, FieldDescriptor>(14);
        searchEntityKeys.put(ProcessInstanceSearchDescriptor.NAME, new FieldDescriptor(SProcessInstance.class, instanceBuilder.getNameKey()));
        searchEntityKeys.put(ProcessInstanceSearchDescriptor.PROCESS_DEFINITION_ID,
                new FieldDescriptor(SProcessInstance.class, instanceBuilder.getProcessDefinitionIdKey()));
        searchEntityKeys.put(ProcessInstanceSearchDescriptor.LAST_UPDATE, new FieldDescriptor(SProcessInstance.class, instanceBuilder.getLastUpdateKey()));
        searchEntityKeys.put(ProcessInstanceSearchDescriptor.START_DATE, new FieldDescriptor(SProcessInstance.class, instanceBuilder.getStartDateKey()));
        searchEntityKeys.put(ProcessInstanceSearchDescriptor.END_DATE, new FieldDescriptor(SProcessInstance.class, instanceBuilder.getEndDateKey()));
        searchEntityKeys.put(ProcessInstanceSearchDescriptor.STATE_ID, new FieldDescriptor(SProcessInstance.class, instanceBuilder.getStateIdKey()));
        searchEntityKeys.put(ProcessInstanceSearchDescriptor.STATE_NAME, new FieldDescriptor(SProcessInstance.class, instanceBuilder.getStateIdKey()));
        searchEntityKeys.put(ProcessInstanceSearchDescriptor.ID, new FieldDescriptor(SProcessInstance.class, instanceBuilder.getIdKey()));
        searchEntityKeys.put(ProcessInstanceSearchDescriptor.STARTED_BY, new FieldDescriptor(SProcessInstance.class, instanceBuilder.getStartedByKey()));
        searchEntityKeys.put(ProcessInstanceSearchDescriptor.CALLER_ID,
                new FieldDescriptor(SProcessInstance.class, instanceBuilder.getCallerIdKey()));
        searchEntityKeys.put(ProcessInstanceSearchDescriptor.USER_ID,
                new FieldDescriptor(SProcessSupervisor.class, BuilderFactory.get(SProcessSupervisorBuilderFactory.class).getUserIdKey()));
        searchEntityKeys.put(ProcessInstanceSearchDescriptor.GROUP_ID,
                new FieldDescriptor(SProcessSupervisor.class, BuilderFactory.get(SProcessSupervisorBuilderFactory.class).getGroupIdKey()));
        searchEntityKeys.put(ProcessInstanceSearchDescriptor.ROLE_ID,
                new FieldDescriptor(SProcessSupervisor.class, BuilderFactory.get(SProcessSupervisorBuilderFactory.class).getRoleIdKey()));
        searchEntityKeys.put(ProcessInstanceSearchDescriptor.ASSIGNEE_ID,
                new FieldDescriptor(SUserTaskInstance.class, sUserTaskInstanceBuilder.getAssigneeIdKey()));

        processInstanceAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        processFields = new HashSet<String>(6);
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
        if (ProcessInstanceSearchDescriptor.STATE_NAME.equals(filterField)) {
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
