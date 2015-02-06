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
package org.bonitasoft.engine.bpm.connector;

import org.bonitasoft.engine.bpm.ArchivedElement;
import org.bonitasoft.engine.bpm.BaseElement;
import org.bonitasoft.engine.bpm.NamedElement;

/**
 * The archived connector
 * 
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface ArchivedConnectorInstance extends NamedElement, BaseElement, ArchivedElement {

    /**
     * Container type : FlowNode
     */
    String FLOWNODE_TYPE = "flowNode";

    /**
     * Container type : Process
     */
    String PROCESS_TYPE = "process";

    /**
     * @return The identifier of the container of the connector
     */
    long getContainerId();

    /**
     * @return The type of the container of the connector. The connector can be on a FlowNode or a Process.
     */
    String getContainerType();

    /**
     * @return The identifier of the container of the connector
     */
    String getConnectorId();

    /**
     * @return The version of the connector
     */
    String getVersion();

    /**
     * @return The event to activate the connector
     */
    ConnectorEvent getActivationEvent();

    /**
     * @return The state of the connector
     */
    ConnectorState getState();

}
