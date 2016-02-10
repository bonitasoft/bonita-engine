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
package org.bonitasoft.engine.bpm.flownode.impl.internal;

import org.bonitasoft.engine.bpm.flownode.BoundaryEventInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeType;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class BoundaryEventInstanceImpl extends CatchEventInstanceImpl implements BoundaryEventInstance {

    private static final long serialVersionUID = 1371653839686575195L;

    private String activityName;

    private long activityInstanceId;

    public BoundaryEventInstanceImpl(final String name, final long flownodeDefinitionId, final long activityInstanceId) {
        super(name, flownodeDefinitionId);
        this.activityInstanceId = activityInstanceId;
    }

    @Override
    public FlowNodeType getType() {
        return FlowNodeType.BOUNDARY_EVENT;
    }

    @Override
    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(final String activityName) {
        this.activityName = activityName;
    }

    @Override
    public long getActivityInstanceId() {
        return activityInstanceId;
    }

    public void setActivityInstanceId(final long activityInstanceId) {
        this.activityInstanceId = activityInstanceId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (activityName == null ? 0 : activityName.hashCode());
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
        final BoundaryEventInstanceImpl other = (BoundaryEventInstanceImpl) obj;
        if (activityName == null) {
            if (other.activityName != null) {
                return false;
            }
        } else if (!activityName.equals(other.activityName)) {
            return false;
        }
        return true;
    }

}
