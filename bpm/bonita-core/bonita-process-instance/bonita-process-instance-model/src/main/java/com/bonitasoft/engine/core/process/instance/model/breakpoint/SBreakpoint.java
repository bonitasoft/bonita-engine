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
package com.bonitasoft.engine.core.process.instance.model.breakpoint;

import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * A <code>SBreakpoint</code> is a programmatic object that can be placed on a BPM element to interrupt its execution under certain condition.
 * A <code>SBreakpoint</code> can be placed on a definition, or for a specific instance. It is also associated with a specific element state.
 * 
 * @author Baptiste Mesta
 * @author Emmanuel Duchastenier
 */
public interface SBreakpoint extends PersistentObject {

    /**
     * Get the ID of the definition object on which this <code>SBreakpoint</code> is placed. Can be a process definition ID of another definition ID in the
     * future.
     * 
     * @return the ID of the definition object.
     * @since 6.0
     */
    long getDefinitionId();

    /**
     * Get the ID of the instance on which this <code>SBreakpoint</code> is placed, if any. ex. ID of the activity instance, ...
     * 
     * @return the ID of the instance on which this <code>SBreakpoint</code> is placed, or 0 if not set.
     * @see #isInstanceScope()
     * @since 6.0
     */
    long getInstanceId();

    /**
     * Is this <code>SBreakpoint</code> for a specific instance? If so {@link #getInstanceId()} gives the instance ID of the element for this
     * <code>SBreakpoint</code>.
     * 
     * @return true if this <code>SBreakpoint</code> is associated with a specific instance, false otherwise.
     * @see #getInstanceId()
     * @since 6.0
     */
    boolean isInstanceScope();

    /**
     * Get the element definition name on which this <code>SBreakpoint</code> is placed. ex. the name of the activity, as defined in its definition model.
     * 
     * @return the element definition name on which this <code>SBreakpoint</code> is placed
     * @since 6.0
     */
    String getElementName();

    /**
     * Get the ID of the state of the element on which this <code>SBreakpoint</code> is placed
     * 
     * @return
     * @since 6.0
     */
    int getStateId();

    int getInterruptedStateId();
}
