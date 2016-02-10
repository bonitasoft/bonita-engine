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
package org.bonitasoft.engine.bpm.connector.impl;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.bpm.connector.ConnectorDefinition;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinitionWithInputValues;

/**
 * @author Baptiste Mesta
 */
public class ConnectorDefinitionWithInputValuesImpl implements ConnectorDefinitionWithInputValues {

    private static final long serialVersionUID = -8750501172227766274L;

    private final ConnectorDefinition connectorDefinition;

    private final Map<String, Map<String, Serializable>> inputValues;

    public ConnectorDefinitionWithInputValuesImpl(final ConnectorDefinition connectorDefinition, final Map<String, Map<String, Serializable>> inputValues) {
        super();
        this.connectorDefinition = connectorDefinition;
        this.inputValues = inputValues;
    }

    @Override
    public ConnectorDefinition getConnectorDefinition() {
        return connectorDefinition;
    }

    @Override
    public Map<String, Map<String, Serializable>> getInputValues() {
        return inputValues;
    }

}
