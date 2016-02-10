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

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.model.SLoopActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SALoopActivityInstance;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class SALoopActivityInstanceImpl extends SAActivityInstanceImpl implements SALoopActivityInstance {

    private static final long serialVersionUID = 2016946775219587242L;

    private int loopCounter;

    private int loopMax;

    public SALoopActivityInstanceImpl() {
        super();
    }

    public SALoopActivityInstanceImpl(final SLoopActivityInstance sLoopActivityInstance) {
        super(sLoopActivityInstance);
        loopMax = sLoopActivityInstance.getLoopMax();
        loopCounter = sLoopActivityInstance.getLoopCounter();
    }

    @Override
    public SFlowNodeType getType() {
        return SFlowNodeType.LOOP_ACTIVITY;
    }

    @Override
    public String getDiscriminator() {
        return SALoopActivityInstance.class.getName();
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
        final SALoopActivityInstanceImpl other = (SALoopActivityInstanceImpl) obj;
        if (loopCounter != other.loopCounter) {
            return false;
        }
        if (loopMax != other.loopMax) {
            return false;
        }
        return true;
    }

    @Override
    public String getKind() {
        return "loop";
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SLoopActivityInstance.class;
    }

}
