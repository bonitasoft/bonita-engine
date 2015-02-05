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

import org.bonitasoft.engine.bpm.BaseElement;
import org.bonitasoft.engine.bpm.NamedElement;

/**
 * Represents the Definition of a process. Gives access to basic information of the process, whereas it is deployed or not.
 * For information about deployment information, use {@link ProcessDeploymentInfo}.
 *
 * @see ProcessDeploymentInfo
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public interface ProcessDefinition extends NamedElement, BaseElement {

    /**
     * @return The version of the process definition
     */
    String getVersion();

    /**
     * @return The description of the process definition, as set at design-time.
     */
    String getDescription();

}
