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

import org.bonitasoft.engine.bpm.flownode.ArchivedLoopActivityInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeType;

/**
 * @author Baptiste Mesta
 */
public class ArchivedLoopActivityInstanceImpl extends ArchivedActivityInstanceImpl implements ArchivedLoopActivityInstance {

    private static final long serialVersionUID = -1606721131308631806L;

    private int loopCounter;

    private int loopMax;

    public ArchivedLoopActivityInstanceImpl(final String name) {
        super(name);
    }

    @Override
    public FlowNodeType getType() {
        return FlowNodeType.LOOP_ACTIVITY;
    }

    @Override
    public int getLoopCounter() {
        return loopCounter;
    }

    @Override
    public int getLoopMax() {
        return loopMax;
    }

    public void setLoopCounter(final int loopCounter) {
        this.loopCounter = loopCounter;
    }

    public void setLoopMax(final int loopMax) {
        this.loopMax = loopMax;
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
        final ArchivedLoopActivityInstanceImpl other = (ArchivedLoopActivityInstanceImpl) obj;
        if (loopCounter != other.loopCounter) {
            return false;
        }
        if (loopMax != other.loopMax) {
            return false;
        }
        return true;
    }

}
