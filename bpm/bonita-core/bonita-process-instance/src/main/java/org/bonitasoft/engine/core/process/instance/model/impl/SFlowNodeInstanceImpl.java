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

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainer;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Feng Hui
 * @author Zhao Na
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class SFlowNodeInstanceImpl extends SFlowElementInstanceImpl implements SFlowNodeInstance, SFlowElementsContainer, PersistentObject {

    private int stateId;
    private String stateName;
    private int previousStateId;
    private long reachedStateDate;
    private long lastUpdateDate;
    private String displayName;
    private String displayDescription;
    private int tokenCount = 0;
    private int loopCounter;
    private long executedBy;
    private long executedBySubstitute;
    private boolean stateExecuting;
    private long flowNodeDefinitionId;

    public SFlowNodeInstanceImpl(final String name, final long flowNodeDefinitionId, final long rootContainerId, final long parentContainerId,
            final long logicalGroup1, final long logicalGroup2) {
        super(name, rootContainerId, parentContainerId, logicalGroup1, logicalGroup2);
        this.flowNodeDefinitionId = flowNodeDefinitionId;
        long now = new Date().getTime();
        lastUpdateDate = now;
        reachedStateDate = now;
    }

    @Override
    public SFlowElementsContainerType getContainerType() {
        return SFlowElementsContainerType.FLOWNODE;
    }

    @Override
    public boolean mustExecuteOnAbortOrCancelProcess() {
        return isStable() && !isTerminal();
    }


}
