/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import org.bonitasoft.engine.bpm.BaseElement;
import org.bonitasoft.engine.bpm.NamedElement;

/**
 * Represents a connector, once instanciated by the containinig activity or process at runtime.
 *
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface ConnectorInstance extends NamedElement, BaseElement {

    /**
     *
     */
    String FLOWNODE_TYPE = "flowNode";

    /**
     *
     */
    String PROCESS_TYPE = "process";

    /**
     * @return The identifier of the containing element (process or activity)
     */
    long getContainerId();

    /**
     * @return The type of the connector container (PROCESS or ACTIVITY)
     */
    String getContainerType();

    /**
     * @return The identifier of the connector.
     */
    String getConnectorId();

    /**
     * @return the version of the connector.
     */
    String getVersion();

    /**
     * @return where this connector should be activated.
     * @see ConnectorEvent
     */
    ConnectorEvent getActivationEvent();

    /**
     * @return the state of the connector ({@link ConnectorState#TO_BE_EXECUTED} before the first execution)
     * @see ConnectorState
     */
    ConnectorState getState();

}
