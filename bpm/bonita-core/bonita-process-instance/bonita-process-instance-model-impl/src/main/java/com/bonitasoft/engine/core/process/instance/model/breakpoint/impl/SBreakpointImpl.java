/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package com.bonitasoft.engine.core.process.instance.model.breakpoint.impl;

import org.bonitasoft.engine.core.process.instance.model.impl.SPersistenceObjectImpl;

import com.bonitasoft.engine.core.process.instance.model.breakpoint.SBreakpoint;

/**
 * @author Baptiste Mesta
 */
public class SBreakpointImpl extends SPersistenceObjectImpl implements SBreakpoint {

    private static final long serialVersionUID = 5298259647116804468L;

    private int interruptedStateId;

    private int stateId;

    private String elementName;

    private boolean isInstanceScope;

    private long instanceId;

    private long definitionId;

    public SBreakpointImpl() {
    }

    @Override
    public String getDiscriminator() {
        return SBreakpoint.class.getName();
    }

    @Override
    public long getDefinitionId() {
        return definitionId;
    }

    @Override
    public long getInstanceId() {
        return instanceId;
    }

    @Override
    public boolean isInstanceScope() {
        return isInstanceScope;
    }

    @Override
    public String getElementName() {
        return elementName;
    }

    @Override
    public int getStateId() {
        return stateId;
    }

    @Override
    public int getInterruptedStateId() {
        return interruptedStateId;
    }

    public void setInterruptedStateId(final int interruptedStateId) {
        this.interruptedStateId = interruptedStateId;
    }

    public void setStateId(final int stateId) {
        this.stateId = stateId;
    }

    public void setElementName(final String elementName) {
        this.elementName = elementName;
    }

    public void setInstanceScope(final boolean isInstanceScope) {
        this.isInstanceScope = isInstanceScope;
    }

    public void setInstanceId(final long instanceId) {
        this.instanceId = instanceId;
    }

    public void setDefinitionId(final long definitionId) {
        this.definitionId = definitionId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (definitionId ^ definitionId >>> 32);
        result = prime * result + (elementName == null ? 0 : elementName.hashCode());
        result = prime * result + (int) (instanceId ^ instanceId >>> 32);
        result = prime * result + interruptedStateId;
        result = prime * result + (isInstanceScope ? 1231 : 1237);
        result = prime * result + stateId;
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
        final SBreakpointImpl other = (SBreakpointImpl) obj;
        if (definitionId != other.definitionId) {
            return false;
        }
        if (elementName == null) {
            if (other.elementName != null) {
                return false;
            }
        } else if (!elementName.equals(other.elementName)) {
            return false;
        }
        if (instanceId != other.instanceId) {
            return false;
        }
        if (interruptedStateId != other.interruptedStateId) {
            return false;
        }
        if (isInstanceScope != other.isInstanceScope) {
            return false;
        }
        if (stateId != other.stateId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SBreakpointImpl [interruptedStateId=" + interruptedStateId + ", stateId=" + stateId + ", elementName=" + elementName + ", isInstanceScope="
                + isInstanceScope + ", instanceId=" + instanceId + ", definitionId=" + definitionId + "]";
    }

}
