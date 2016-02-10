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

import java.util.Date;

import org.bonitasoft.engine.bpm.BaseElement;
import org.bonitasoft.engine.bpm.DescriptionElement;

/**
 * Gives access to the {@link ProcessDefinition} deployment information.<br>
 * A <code>ProcessDeploymentInfo</code> has a {@link ConfigurationState}, which says if the process is <code>resolved</code> (all its basic configuration has a
 * proper value), or <code>unresolved</code> (some configuration has to be done before the process can be activated).
 * <p>
 * A <code>ProcessDeploymentInfo</code> has an {@link ActivationState}, which says if the process was set to <code>enabled</code> (logged users can start
 * instances of this process), or <code>disabled</code> (no start can be performed on the process).
 *
 * @author Baptiste Mesta
 * @author Yanyan Liu
 * @author Hongwen Zang
 * @author Celine Souchet
 * @author Emmanuel Duchastenier
 * @see ProcessDefinition
 * @see ConfigurationState
 * @see ActivationState
 */
public interface ProcessDeploymentInfo extends DescriptionElement, BaseElement {

    /**
     * Retrieves the {@link ProcessDefinition} identifier
     *
     * @return a <code>long</code> representing the <code>ProcessDefinition</code> identifier
     * @see ProcessDefinition
     */
    long getProcessId();

    /**
     * Retrieves the {@link ProcessDefinition} version
     *
     * @return a String representing the <code>ProcessDefinition</code> version.
     * @see ProcessDefinition
     */
    String getVersion();

    /**
     * Retrieves the {@link ProcessDefinition} display description. Unlike <code>description</code> that is static, the <code>display description</code> can be
     * updated via {@link org.bonitasoft.engine.api.ProcessManagementAPI#updateProcessDeploymentInfo(long, ProcessDeploymentInfoUpdater)}.
     * <p>
     * When set, this field is used by the Bonita BPM Portal in the place of <code>description</code>.
     *
     * @return a String representing the {@link ProcessDefinition} display description.
     * @see ProcessDefinition
     * @see org.bonitasoft.engine.api.ProcessManagementAPI#updateProcessDeploymentInfo(long, ProcessDeploymentInfoUpdater)
     */
    String getDisplayDescription();

    /**
     * Retrieves the Date when the underlining {@link ProcessDefinition} was deployed.
     *
     * @return the Date when the underlining <code>ProcessDefinition</code> was deployed.
     * @see ProcessDefinition
     */
    Date getDeploymentDate();

    /**
     * Retrieves the identifier of the Bonita BPM {@link org.bonitasoft.engine.identity.User} which deployed the {@link ProcessDefinition}
     *
     * @return a long representing the identifier of the <code>Bonita BPM user</code> which deployed the process.
     * @see org.bonitasoft.engine.identity.User
     * @see ProcessDefinition
     */
    long getDeployedBy();

    /**
     * Retrieves the {@link ProcessDefinition} display name. Unlike <code>name</code> that is static, the <code>display name</code> can be
     * updated via {@link org.bonitasoft.engine.api.ProcessManagementAPI#updateProcessDeploymentInfo(long, ProcessDeploymentInfoUpdater)}.
     * <p>
     * When set this field is used by the Bonita BPM Portal in the place of <code>name</code>.
     *
     * @return a String representing the <code>ProcessDefinition</code> display name.
     * @see ProcessDefinition
     * @see org.bonitasoft.engine.api.ProcessManagementAPI#updateProcessDeploymentInfo(long, ProcessDeploymentInfoUpdater)
     */
    String getDisplayName();

    /**
     * Retrieves the date of the last update statement
     *
     * @return the date of the last update statement
     */
    Date getLastUpdateDate();

    /**
     * Retrieves the process icon path. The path is relative to the <code>bonita.home</code> folder.
     *
     * @return a String representing the process icon path.
     */
    String getIconPath();

    /**
     * Retrieves the {@link ProcessDefinition} {@link ConfigurationState}.
     *
     * @return the <code>ProcessDefinition</code> <code>ConfigurationState</code>
     * @see ProcessDefinition
     * @see ConfigurationState
     */
    ConfigurationState getConfigurationState();

    /**
     * Retrieves the {@link ProcessDefinition} {@link ActivationState}.
     *
     * @return the <code>ProcessDefinition</code> <code>ActivationState</code>
     */
    ActivationState getActivationState();

}
