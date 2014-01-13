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

import java.util.Date;

import org.bonitasoft.engine.bpm.BaseElement;
import org.bonitasoft.engine.bpm.DescriptionElement;

/**
 * Gives access to deployment information of a process.<br/>
 * A <code>ProcessDeploymentInfo</code> has a ConfigurationState, which says if the process is resolved (all his basic configuration has a proper value), or
 * unresolved (some configuration has to be done before the process can be activated).<br/>
 * A <code>ProcessDeploymentInfo</code> has an ActivationState, which says if the process was set to enabled (logged users can start instances of this process),
 * or disabled (no start can be performed on the process).
 * 
 * @author Baptiste Mesta
 * @author Yanyan Liu
 * @author Hongwen Zang
 * @author Celine Souchet
 * @author Emmanuel Duchastenier
 */
public interface ProcessDeploymentInfo extends DescriptionElement, BaseElement {

    /**
     * @return the ID of the process, which is the same of the process definition ID.
     * @see ProcessDefinition
     */
    long getProcessId();

    /**
     * @return the version of the process, which is the same of the process definition version.
     * @see ProcessDefinition
     */
    String getVersion();

    /**
     * @return the display description of the process, as designed in the process definition.
     * @see ProcessDefinition
     */
    String getDisplayDescription();

    /**
     * @return the Date when the underlining process was deployed.
     */
    Date getDeploymentDate();

    /**
     * @return the ID of the Bonita BPM user who deployed the process.
     */
    long getDeployedBy();

    /**
     * @return the display name of the process, as designed in the process definition.
     * @see ProcessDefinition
     */
    String getDisplayName();

    /**
     * Get the date of the last time execute update statement
     * 
     * @return date of the last time execute update statement
     */
    Date getLastUpdateDate();

    /**
     * @return the bonita-home relative path to this process display icon.
     */
    String getIconPath();

    /**
     * @return the Configuration State
     */
    ConfigurationState getConfigurationState();

    /**
     * @return the Activation State
     */
    ActivationState getActivationState();

}
