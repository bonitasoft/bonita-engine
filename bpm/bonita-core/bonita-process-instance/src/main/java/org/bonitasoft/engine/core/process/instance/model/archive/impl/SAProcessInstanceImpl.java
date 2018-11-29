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
package org.bonitasoft.engine.core.process.instance.model.archive.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SAProcessInstanceImpl extends SANamedElementImpl implements SAProcessInstance {

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

    public SAProcessInstanceImpl(final SProcessInstance processInstance) {
        super(processInstance.getName(), processInstance.getId());
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
