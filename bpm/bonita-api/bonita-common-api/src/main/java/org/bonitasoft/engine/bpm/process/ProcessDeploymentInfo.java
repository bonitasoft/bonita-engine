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
 * Gives access to deployment information of a process.
 * 
 * @author Baptiste Mesta
 * @author Yanyan Liu
 * @author Hongwen Zang
 * @author Celine Souchet
 * @author Emmanuel Duchastenier
 */
public interface ProcessDeploymentInfo extends DescriptionElement, BaseElement {

    /**
     * @return the ID of the process, which is the same of the process definition.
     */
    long getProcessId();

    String getVersion();

    String getDisplayDescription();

    Date getDeploymentDate();

    long getDeployedBy();

    String getDisplayName();

    /**
     * Get the dates of the last time execute update statement
     * 
     * @return date of the last time execute update statement
     */
    Date getLastUpdateDate();

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
