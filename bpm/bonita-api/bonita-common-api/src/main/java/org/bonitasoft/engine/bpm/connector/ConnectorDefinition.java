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

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.NamedElement;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.operation.Operation;

/**
 * The connector definition associated to a process definition or a flow node definition
 * 
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public interface ConnectorDefinition extends NamedElement {

    /**
     * @return The identifier of the connector definition
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
     * @return The inputs of the connector
     */
    Map<String, Expression> getInputs();

    /**
     * @return The outputs of the connector
     */
    List<Operation> getOutputs();

    /**
     * @return The fail action of the connector
     */
    FailAction getFailAction();

    /**
     * @return The error code of the connector
     */
    String getErrorCode();

}
