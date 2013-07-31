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

import org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor;
import org.bonitasoft.engine.core.process.instance.model.SUserTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAProcessInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilder;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisor;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisorBuilder;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisorBuilders;

/**
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SearchArchivedProcessInstancesDescriptor extends SearchEntityDescriptor {

    protected final Map<String, FieldDescriptor> searchEntityKeys;

    protected final Map<Class<? extends PersistentObject>, Set<String>> archivedProcessInstanceAllFields;

    protected final Set<String> processInstanceFields;

    public SearchArchivedProcessInstancesDescriptor(final BPMInstanceBuilders instanceBuilders, final SProcessSupervisorBuilders sSupervisorBuilders) {
        final SAProcessInstanceBuilder instanceBuilder = instanceBuilders.getSAProcessInstanceBuilder();
        final SUserTaskInstanceBuilder sUserTaskInstanceBuilder = instanceBuilders.getSUserTaskInstanceBuilder();
        final SProcessSupervisorBuilder sSupervisorBuilder = sSupervisorBuilders.getSSupervisorBuilder();
        searchEntityKeys = new HashMap<String, FieldDescriptor>(14);
        searchEntityKeys.put(ArchivedProcessInstancesSearchDescriptor.NAME, new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getNameKey()));
        searchEntityKeys.put(ArchivedProcessInstancesSearchDescriptor.PROCESS_DEFINITION_ID,
                new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getProcessDefinitionIdKey()));
        searchEntityKeys.put(ArchivedProcessInstancesSearchDescriptor.ID, new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getIdKey()));
        searchEntityKeys.put(ArchivedProcessInstancesSearchDescriptor.STARTED_BY,
                new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getStartedByKey()));
        searchEntityKeys.put(ArchivedProcessInstancesSearchDescriptor.STARTED_BY_DELEGATE,
                new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getStartedByDelegateKey()));
        searchEntityKeys.put(ArchivedProcessInstancesSearchDescriptor.START_DATE,
                new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getStartDateKey()));
        searchEntityKeys.put(ArchivedProcessInstancesSearchDescriptor.END_DATE, new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getEndDateKey()));
        searchEntityKeys.put(ArchivedProcessInstancesSearchDescriptor.STATE_ID, new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getStateIdKey()));
        searchEntityKeys.put(ArchivedProcessInstancesSearchDescriptor.SOURCE_OBJECT_ID,
                new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getSourceObjectIdKey()));
        searchEntityKeys.put(ArchivedProcessInstancesSearchDescriptor.LAST_UPDATE,
                new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getLastUpdateKey()));
        searchEntityKeys.put(ArchivedProcessInstancesSearchDescriptor.ARCHIVE_DATE,
                new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getArchiveDateKey()));
        searchEntityKeys.put(ArchivedProcessInstancesSearchDescriptor.CALLER_ID,
                new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getCallerIdKey()));
        searchEntityKeys
                .put(ArchivedProcessInstancesSearchDescriptor.USER_ID, new FieldDescriptor(SProcessSupervisor.class, sSupervisorBuilder.getUserIdKey()));
        searchEntityKeys.put(ArchivedProcessInstancesSearchDescriptor.GROUP_ID,
                new FieldDescriptor(SProcessSupervisor.class, sSupervisorBuilder.getGroupIdKey()));
        searchEntityKeys
                .put(ArchivedProcessInstancesSearchDescriptor.ROLE_ID, new FieldDescriptor(SProcessSupervisor.class, sSupervisorBuilder.getRoleIdKey()));
        searchEntityKeys.put(ArchivedProcessInstancesSearchDescriptor.ASSIGNEE_ID,
                new FieldDescriptor(SUserTaskInstance.class, sUserTaskInstanceBuilder.getAssigneeIdKey()));

        archivedProcessInstanceAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        processInstanceFields = new HashSet<String>(1);
        // processInstanceFields.add(instanceBuilder.getStartedByKey());
        processInstanceFields.add(instanceBuilder.getNameKey());
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
