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
package org.bonitasoft.engine.search.descriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.bpm.connector.ConnectorInstancesSearchDescriptor;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Baptiste Mesta
 */
public class SearchConnectorInstanceDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> searchEntityKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> connectorInstanceAllFields;

    SearchConnectorInstanceDescriptor() {
        searchEntityKeys = new HashMap<>(7);
        searchEntityKeys.put(ConnectorInstancesSearchDescriptor.NAME,
                new FieldDescriptor(SConnectorInstance.class, SConnectorInstance.NAME_KEY));
        searchEntityKeys.put(ConnectorInstancesSearchDescriptor.ACTIVATION_EVENT,
                new FieldDescriptor(SConnectorInstance.class, SConnectorInstance.ACTIVATION_EVENT_KEY));
        searchEntityKeys.put(ConnectorInstancesSearchDescriptor.CONNECTOR_DEFINITION_ID,
                new FieldDescriptor(SConnectorInstance.class,
                        SConnectorInstance.CONNECTOR_ID_KEY));
        searchEntityKeys.put(ConnectorInstancesSearchDescriptor.CONNECTOR_DEFINITION_VERSION,
                new FieldDescriptor(SConnectorInstance.class,
                        SConnectorInstance.VERSION_KEY));
        searchEntityKeys.put(ConnectorInstancesSearchDescriptor.CONTAINER_ID,
                new FieldDescriptor(SConnectorInstance.class, SConnectorInstance.CONTAINER_ID_KEY));
        searchEntityKeys.put(ConnectorInstancesSearchDescriptor.CONTAINER_TYPE,
                new FieldDescriptor(SConnectorInstance.class, SConnectorInstance.CONTAINER_TYPE_KEY));
        searchEntityKeys.put(ConnectorInstancesSearchDescriptor.STATE,
                new FieldDescriptor(SConnectorInstance.class, SConnectorInstance.STATE_KEY));
        searchEntityKeys.put(ConnectorInstancesSearchDescriptor.EXECUTION_ORDER,
                new FieldDescriptor(SConnectorInstance.class, SConnectorInstance.EXECUTION_ORDER));

        connectorInstanceAllFields = new HashMap<>(1);
        final Set<String> connectorFields = new HashSet<String>(2);
        connectorFields.add(SConnectorInstance.NAME_KEY);
        connectorFields.add(SConnectorInstance.CONNECTOR_ID_KEY);

        connectorInstanceAllFields.put(SConnectorInstance.class, connectorFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return searchEntityKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return connectorInstanceAllFields;
    }

}
