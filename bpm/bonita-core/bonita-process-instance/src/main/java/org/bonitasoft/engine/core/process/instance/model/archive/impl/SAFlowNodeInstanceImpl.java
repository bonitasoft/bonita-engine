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
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class SAFlowNodeInstanceImpl extends SAFlowElementInstanceImpl implements SAFlowNodeInstance {

    private int stateId;
    private String stateName;
    private boolean terminal;
    private boolean stable;
    private long reachedStateDate;
    private long lastUpdateDate;
    private String displayDescription;
    private String displayName;
    private String description;
    private long executedBy;
    private long executedBySubstitute;
    private String kind;
    private long flowNodeDefinitionId;

    public SAFlowNodeInstanceImpl(final SFlowNodeInstance flowNodeInstance) {
        super(flowNodeInstance);
        stateId = flowNodeInstance.getStateId();
        stateName = flowNodeInstance.getStateName();
        reachedStateDate = flowNodeInstance.getReachedStateDate();
        lastUpdateDate = flowNodeInstance.getLastUpdateDate();
        terminal = flowNodeInstance.isTerminal();
        stable = flowNodeInstance.isStable();
        displayName = flowNodeInstance.getDisplayName();
        displayDescription = flowNodeInstance.getDisplayDescription();
        description = flowNodeInstance.getDescription();
        executedBy = flowNodeInstance.getExecutedBy();
        executedBySubstitute = flowNodeInstance.getExecutedBySubstitute();
        flowNodeDefinitionId = flowNodeInstance.getFlowNodeDefinitionId();
    }

}
