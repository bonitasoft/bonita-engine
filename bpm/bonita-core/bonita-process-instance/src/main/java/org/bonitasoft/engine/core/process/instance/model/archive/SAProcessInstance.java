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
package org.bonitasoft.engine.core.process.instance.model.archive;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.persistence.ArchivedPersistentObject;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

@Data
@NoArgsConstructor
@Entity
@IdClass(PersistentObjectId.class)
@Table(name = "arch_process_instance")
public class SAProcessInstance implements ArchivedPersistentObject {

    @Id
    private long id;
    @Id
    private long tenantId;
    private long archiveDate;
    private long sourceObjectId;
    private String name;
    private long processDefinitionId;
    private String description;
    private int stateId;
    private long startDate;
    private long startedBy;
    private long startedBySubstitute;
    private long endDate;
    private long lastUpdate;
    private long rootProcessInstanceId = -1;
    private long callerId = -1;
    private String stringIndex1;
    private String stringIndex2;
    private String stringIndex3;
    private String stringIndex4;
    private String stringIndex5;

    public SAProcessInstance(final SProcessInstance processInstance) {
        sourceObjectId = processInstance.getId();
        name = processInstance.getName();
        processDefinitionId = processInstance.getProcessDefinitionId();
        description = processInstance.getDescription();
        startDate = processInstance.getStartDate();
        endDate = processInstance.getEndDate();
        startedBy = processInstance.getStartedBy();
        startedBySubstitute = processInstance.getStartedBySubstitute();
        lastUpdate = processInstance.getLastUpdate();
        stateId = processInstance.getStateId();
        rootProcessInstanceId = processInstance.getRootProcessInstanceId();
        callerId = processInstance.getCallerId();
        stringIndex1 = processInstance.getStringIndex1();
        stringIndex2 = processInstance.getStringIndex2();
        stringIndex3 = processInstance.getStringIndex3();
        stringIndex4 = processInstance.getStringIndex4();
        stringIndex5 = processInstance.getStringIndex5();
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SProcessInstance.class;
    }

}
