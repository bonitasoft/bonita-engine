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
import org.bonitasoft.engine.core.process.instance.model.SFlowElementInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class SFlowElementInstanceImpl extends SNamedElementImpl implements SFlowElementInstance {

    private long rootContainerId;
    private long parentContainerId;
    private SStateCategory stateCategory = SStateCategory.NORMAL;
    //process definition id
    private long logicalGroup1;
    //root process instance id
    private long logicalGroup2;
    //parent activity instance id
    private long logicalGroup3;
    //parent process instance id
    private long logicalGroup4;
    private String description;
    private boolean terminal;
    private boolean stable;

    public SFlowElementInstanceImpl(final String name) {
        super(name);
    }

    public SFlowElementInstanceImpl(final String name, final long rootContainerId, final long parentContainerId, final long logicalGroup1,
            final long logicalGroup2) {
        super(name);
        this.rootContainerId = rootContainerId;
        this.parentContainerId = parentContainerId;
        this.logicalGroup1 = logicalGroup1;
        this.logicalGroup2 = logicalGroup2;
    }

    @Override
    public long getProcessDefinitionId() {
        return logicalGroup1;
    }

    @Override
    public long getRootProcessInstanceId() {
        return logicalGroup2;
    }

    @Override
    public long getParentActivityInstanceId() {
        return logicalGroup3;
    }

    @Override
    public long getParentProcessInstanceId() {
        return logicalGroup4;
    }

    @Override
    public SFlowElementsContainerType getParentContainerType() {
        return getParentActivityInstanceId() <= 0 ? SFlowElementsContainerType.PROCESS : SFlowElementsContainerType.FLOWNODE;
    }

    @Override
    public long getLogicalGroup(final int index) {
        switch (index) {
            case 0:
                return logicalGroup1;
            case 1:
                return logicalGroup2;
            case 2:
                return logicalGroup3;
            case 3:
                return logicalGroup4;
            default:
                throw new IllegalArgumentException("Invalid index: the index must be 0, 1, 2 or 3");
        }
    }

    @Override
    public boolean isAborting() {
        return SStateCategory.ABORTING.equals(stateCategory);
    }

    @Override
    public boolean isCanceling() {
        return SStateCategory.CANCELLING.equals(stateCategory);
    }
    public void setLogicalGroup(final int index, final long value) {
        switch (index) {
            case 0:
                logicalGroup1 = value;
                break;
            case 1:
                logicalGroup2 = value;
                break;
            case 2:
                logicalGroup3 = value;
                break;
            case 3:
                logicalGroup4 = value;
                break;
            default:
                throw new IllegalArgumentException("Invalid index: the index must be 0, 1, 2 or 3");
        }
    }

}
