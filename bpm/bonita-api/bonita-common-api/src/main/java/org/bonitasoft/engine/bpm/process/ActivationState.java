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
package org.bonitasoft.engine.bpm.process;

/**
 * <p>Activation state of a process. A <code>ProcessDefinition</code> can be enabled or disabled, which toggles on/off the possibility to start a new instance
 * of
 * the process.</p>
 * <p>Use {@link ProcessDeploymentInfo#getActivationState()} to retrieve the activation state for a process.</p>
 *
 * @see ProcessDeploymentInfo#getActivationState()
 * @author Celine Souchet
 * @author Emmanuel Duchastenier
 */
public enum ActivationState {

    /**
     * The process is enabled and instances of the process can be started.
     */
    ENABLED,

    /**
     * The process is disabled and no instance can be started.
     */
    DISABLED

}
