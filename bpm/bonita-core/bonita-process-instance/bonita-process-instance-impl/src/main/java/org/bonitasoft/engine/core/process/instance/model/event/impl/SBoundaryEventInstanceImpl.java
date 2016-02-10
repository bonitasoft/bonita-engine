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
package org.bonitasoft.engine.core.process.instance.model.event.impl;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.model.event.SBoundaryEventInstance;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class SBoundaryEventInstanceImpl extends SCatchEventInstanceImpl implements SBoundaryEventInstance {

    private static final long serialVersionUID = -9222619067525277584L;

    private long activityInstanceId;

    public SBoundaryEventInstanceImpl() {
        super();
    }

    public SBoundaryEventInstanceImpl(final String name, final long flowNodeDefinitionId, final long rootContainerId, final long parentContainerId,
            final long logicalGroup1, final long logicalGroup2) {
        super(name, flowNodeDefinitionId, rootContainerId, parentContainerId, logicalGroup1, logicalGroup2);
    }

    @Override
    public SFlowNodeType getType() {
        return SFlowNodeType.BOUNDARY_EVENT;
    }

    @Override
    public long getActivityInstanceId() {
        return activityInstanceId;
    }

    public void setActivityInstanceId(final long activityInstanceId) {
        this.activityInstanceId = activityInstanceId;
    }
    
    @Override
    public boolean mustExecuteOnAbortOrCancelProcess() {
        //a boundary event never must be executed when the process instance is aborted because it will be aborted by the attached activity
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (activityInstanceId ^ activityInstanceId >>> 32);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SBoundaryEventInstanceImpl other = (SBoundaryEventInstanceImpl) obj;
        if (activityInstanceId != other.activityInstanceId) {
            return false;
        }
        return true;
    }

}
