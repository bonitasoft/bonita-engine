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

import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public abstract class SActivityInstanceImpl extends SFlowNodeInstanceImpl implements SActivityInstance {

    private static final long serialVersionUID = 4599639229663431703L;

    private long abortedByBoundary = 0;

    public SActivityInstanceImpl() {
    }

    public SActivityInstanceImpl(final String name, final long flowNodeDefinitionId, final long rootContainerId, final long parentContainerId,
            final long logicalGroup1, final long logicalGroup2) {
        super(name, flowNodeDefinitionId, rootContainerId, parentContainerId, logicalGroup1, logicalGroup2);
    }

    @Override
    public String getDiscriminator() {
        return SActivityInstance.class.getName();
    }

    @Override
    public long getAbortedByBoundary() {
        return abortedByBoundary;
    }

    public void setAbortedByBoundary(final long abortedByBoundaryEventId) {
        abortedByBoundary = abortedByBoundaryEventId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (abortedByBoundary ^ abortedByBoundary >>> 32);
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
        final SActivityInstanceImpl other = (SActivityInstanceImpl) obj;
        if (abortedByBoundary != other.abortedByBoundary) {
            return false;
        }
        return true;
    }

}
