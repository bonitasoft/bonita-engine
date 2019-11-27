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
package org.bonitasoft.engine.core.process.instance.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Elias Ricken de Medeiros
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SProcessInstance implements PersistentObject {

    private static final long DEFAULT_INTERRUPTING_EVENT_ID = -1L;
    public static final String STRING_INDEX_1_KEY = "stringIndex1";
    public static final String STRING_INDEX_2_KEY = "stringIndex2";
    public static final String STRING_INDEX_3_KEY = "stringIndex3";
    public static final String STRING_INDEX_4_KEY = "stringIndex4";
    public static final String STRING_INDEX_5_KEY = "stringIndex5";
    public static final String LAST_UPDATE_KEY = "lastUpdate";
    public static final String INTERRUPTING_EVENT_ID_KEY = "interruptingEventId";
    public static final String ID_KEY = "id";
    public static final String NAME_KEY = "name";
    public static final String PROCESSDEF_ID_KEY = "processDefinitionId";
    public static final String STATE_ID_KEY = "stateId";
    public static final String STATE_CATEGORY_KEY = "stateCategory";
    public static final String CONTAINER_ID_KEY = "containerId";
    public static final String END_DATE_KEY = "endDate";
    public static final String STARTED_BY_KEY = "startedBy";
    public static final String STARTED_BY_SUBSTITUTE_KEY = "startedBySubstitute";
    public static final String START_DATE_KEY = "startDate";
    public static final String CALLER_ID = "callerId";
    private long id;
    private long tenantId;
    private String name;
    private long processDefinitionId;
    private String description;
    private int stateId;
    @Builder.Default
    private long startDate = new Date().getTime();
    /**
     * id of the user who originally started the process
     */
    private long startedBy;
    /**
     * id of the user (delegate) who started the process for the original starter
     */
    private long startedBySubstitute;
    private long endDate;
    private long lastUpdate;
    private long containerId;
    @Builder.Default
    private long rootProcessInstanceId = -1;
    @Builder.Default
    private long callerId = -1;

    /**
     * The caller's SFlowNodeType if the it's called by a call activity or sub-process, null otherwise
     */
    private SFlowNodeType callerType;
    /**
     * Id of the end error event that interrupted the process instance or -1 if the process was not interrupted by a end error event
     */
    @Builder.Default
    private long interruptingEventId = DEFAULT_INTERRUPTING_EVENT_ID;
    @Builder.Default
    private SStateCategory stateCategory = SStateCategory.NORMAL;
    private String stringIndex1;
    private String stringIndex2;
    private String stringIndex3;
    private String stringIndex4;
    private String stringIndex5;



    public SProcessInstance(final String name, final long processDefinitionId) {
        this.name = name;
        this.processDefinitionId = processDefinitionId;
    }

    public SProcessInstance(final SProcessDefinition definition) {
        name= definition.getName();
        processDefinitionId = definition.getId();
        description = definition.getDescription();
    }

    @Override
    public void setId(final long id) {
        this.id = id;
        if (rootProcessInstanceId == -1) {
            rootProcessInstanceId = id;
        }
    }

    public SFlowElementsContainerType getContainerType() {
        return SFlowElementsContainerType.PROCESS;
    }


    public boolean hasBeenInterruptedByEvent() {
        return getInterruptingEventId() != -1;
    }


    /**
     * Determines if this instance is a root process instance. That is, it is neither a process called by a call activity, neither a sub-process
     * @return true if it's a root process instance; false otherwise.
     */
    public boolean isRootInstance() {
        return callerId <= 0;
    }
}
