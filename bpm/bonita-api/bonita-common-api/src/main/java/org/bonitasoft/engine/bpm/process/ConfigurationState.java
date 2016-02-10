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
package org.bonitasoft.engine.bpm.process;

/**
 * Autoset by Bonita BPM Engine. Determines if a process is resolved / unresolved, that is, if a process can be started or not.
 * Reasons for unresolved processes are:
 * <ul>
 * <li>Not all actors of the process have mappings to users / groups / roles / memberships</li>
 * <li>Some business data used in the process are not available on the currently deployed Business Data Model version (Subscription editions only)</li>
 * <li>Not all connectors used in the process have an implementation defined</li>
 * <li>Not all user filters used in the process have an implementation defined</li>
 * <li>Not all parameters used in the process have a value defined (Subscription editions only)</li>
 * </ul>
 * <p>Use {@link ProcessDeploymentInfo#getConfigurationState()} to retrieve the configuration state for a process.</p>
 *
 * @see ProcessDeploymentInfo#getConfigurationState()
 * @author Celine Souchet
 * @author Emmanuel Duchastenier
 * @version 6.3.5
 * @since 6.0.0
 */
public enum ConfigurationState {

    /**
     * The process is unresolved, for one or more of the causes defined above.
     */
    UNRESOLVED,

    /**
     * The process is resolved, and can be started to create a new instance.
     */
    RESOLVED
}
