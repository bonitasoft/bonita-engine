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

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.model.SLoopActivityInstance;

/**
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class SLoopActivityInstanceImpl extends SActivityInstanceImpl implements SLoopActivityInstance {

    private static final long serialVersionUID = -2778635412407060681L;

    private int loopCounter;

    private int loopMax;

    public SLoopActivityInstanceImpl() {
        super();
    }

    public SLoopActivityInstanceImpl(final String name, final long flowNodeDefinitionId, final long rootContainerId, final long parentContainerId,
            final long logicalGroup1, final long logicalGroup2) {
        super(name, flowNodeDefinitionId, rootContainerId, parentContainerId, logicalGroup1, logicalGroup2);
        loopCounter = 0;
        loopMax = -1;
    }

    @Override
    public SFlowNodeType getType() {
        return SFlowNodeType.LOOP_ACTIVITY;
    }

    @Override
    public int getLoopCounter() {
        return loopCounter;
    }

    @Override
    public void setLoopCounter(final int loopCounter) {
        this.loopCounter = loopCounter;
    }

    @Override
    public int getLoopMax() {
        return loopMax;
    }

    public void setLoopMax(final int loopMax) {
        this.loopMax = loopMax;
    }
    
    @Override
    public boolean mustExecuteOnAbortOrCancelProcess() {
        // it's not necessary to execute it because this will be done when the child reaches the aborted state
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + loopCounter;
        result = prime * result + loopMax;
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
        final SLoopActivityInstanceImpl other = (SLoopActivityInstanceImpl) obj;
        if (loopCounter != other.loopCounter) {
            return false;
        }
        if (loopMax != other.loopMax) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("SLoopActivityInstanceImpl [loopCounter=");
        builder.append(loopCounter);
        builder.append(", loopMax=");
        builder.append(loopMax);
        builder.append("]");
        return builder.toString();
    }

}
