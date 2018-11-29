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
package org.bonitasoft.engine.core.process.instance.model.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;

/**
 * @author Elias Ricken de Medeiros
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SProcessInstanceImpl extends SNamedElementImpl implements SProcessInstance {

    private static final long DEFAULT_INTERRUPTING_EVENT_ID = -1L;
    private long processDefinitionId;
    private String description;
    private int stateId;
    private long startDate;
    private long startedBy;
    private long startedBySubstitute;
    private long endDate;
    private long lastUpdate;
    private long containerId;
    private long rootProcessInstanceId = -1;
    private long callerId = -1;
    private SFlowNodeType callerType;
    private long interruptingEventId = DEFAULT_INTERRUPTING_EVENT_ID;
    private SStateCategory stateCategory;
    private String stringIndex1;
    private String stringIndex2;
    private String stringIndex3;
    private String stringIndex4;
    private String stringIndex5;


    public SProcessInstanceImpl(final String name, final long processDefinitionId) {
        super(name);
        this.processDefinitionId = processDefinitionId;
    }

    public SProcessInstanceImpl(final SProcessDefinition definition) {
        super(definition.getName());
        processDefinitionId = definition.getId();
    }

    @Override
    public void setId(final long id) {
        super.setId(id);
        if (rootProcessInstanceId == -1) {
            rootProcessInstanceId = id;
        }
    }

    @Override
    public SFlowElementsContainerType getContainerType() {
        return SFlowElementsContainerType.PROCESS;
    }


    @Override
    public boolean hasBeenInterruptedByEvent() {
        return getInterruptingEventId() != -1;
    }

    @Override
    public boolean isRootInstance() {
        return callerId <= 0;
    }
}
