/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bpm.breakpoint.impl;

import org.bonitasoft.engine.bpm.internal.BaseElementImpl;

import com.bonitasoft.engine.bpm.breakpoint.Breakpoint;

/**
 * @author Baptiste Mesta
 */
public class BreakpointImpl extends BaseElementImpl implements Breakpoint {

    private static final long serialVersionUID = 3732740221256934159L;

    private int interruptedStateId;

    private int stateId;

    private String elementName;

    private boolean instanceScope;

    private long instanceId;

    private long definitionId;

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
        return instanceScope;
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

    /**
     * @param interruptedStateId
     *            the interruptedStateId to set
     */
    public void setInterruptedStateId(final int interruptedStateId) {
        this.interruptedStateId = interruptedStateId;
    }

    /**
     * @param stateId
     *            the stateId to set
     */
    public void setStateId(final int stateId) {
        this.stateId = stateId;
    }

    /**
     * @param elementName
     *            the elementName to set
     */
    public void setElementName(final String elementName) {
        this.elementName = elementName;
    }

    /**
     * @param instanceScope
     *            the instanceScope to set
     */
    public void setInstanceScope(final boolean instanceScope) {
        this.instanceScope = instanceScope;
    }

    /**
     * @param instanceId
     *            the instanceId to set
     */
    public void setInstanceId(final long instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * @param definitionId
     *            the definitionId to set
     */
    public void setDefinitionId(final long definitionId) {
        this.definitionId = definitionId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (definitionId ^ definitionId >>> 32);
        result = prime * result + (elementName == null ? 0 : elementName.hashCode());
        result = prime * result + (int) (instanceId ^ instanceId >>> 32);
        result = prime * result + (instanceScope ? 1231 : 1237);
        result = prime * result + interruptedStateId;
        result = prime * result + stateId;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BreakpointImpl other = (BreakpointImpl) obj;
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
        if (instanceScope != other.instanceScope) {
            return false;
        }
        if (interruptedStateId != other.interruptedStateId) {
            return false;
        }
        if (stateId != other.stateId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "BreakpointImpl [interruptedStateId=" + interruptedStateId + ", stateId=" + stateId + ", elementName=" + elementName + ", instanceScope="
                + instanceScope + ", instanceId=" + instanceId + ", definitionId=" + definitionId + "]";
    }

}
