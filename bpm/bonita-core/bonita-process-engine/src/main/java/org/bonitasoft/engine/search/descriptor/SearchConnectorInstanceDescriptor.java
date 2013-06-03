/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.search.descriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.bpm.connector.ConnectorInstancesSearchDescriptor;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SConnectorInstanceBuilder;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Baptiste Mesta
 */
public class SearchConnectorInstanceDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> searchEntityKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> connectorInstanceAllFields;

    SearchConnectorInstanceDescriptor(final SConnectorInstanceBuilder sConnectorInstanceBuilder) {
        searchEntityKeys = new HashMap<String, FieldDescriptor>(7);
        searchEntityKeys.put(ConnectorInstancesSearchDescriptor.NAME, new FieldDescriptor(SConnectorInstance.class, sConnectorInstanceBuilder.getNameKey()));
        searchEntityKeys.put(ConnectorInstancesSearchDescriptor.ACTIVATION_EVENT,
                new FieldDescriptor(SConnectorInstance.class, sConnectorInstanceBuilder.getActivationEventKey()));
        searchEntityKeys.put(ConnectorInstancesSearchDescriptor.CONNECTOR_DEFINITION_ID, new FieldDescriptor(SConnectorInstance.class,
                sConnectorInstanceBuilder.getConnectorIdKey()));
        searchEntityKeys.put(ConnectorInstancesSearchDescriptor.CONNECTOR_DEFINITION_VERSION, new FieldDescriptor(SConnectorInstance.class,
                sConnectorInstanceBuilder.getVersionKey()));
        searchEntityKeys.put(ConnectorInstancesSearchDescriptor.CONTAINER_ID,
                new FieldDescriptor(SConnectorInstance.class, sConnectorInstanceBuilder.getContainerIdKey()));
        searchEntityKeys.put(ConnectorInstancesSearchDescriptor.CONTAINER_TYPE,
                new FieldDescriptor(SConnectorInstance.class, sConnectorInstanceBuilder.getContainerTypeKey()));
        searchEntityKeys.put(ConnectorInstancesSearchDescriptor.STATE, new FieldDescriptor(SConnectorInstance.class, sConnectorInstanceBuilder.getStateKey()));

        connectorInstanceAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> connectorFields = new HashSet<String>(2);
        connectorFields.add(sConnectorInstanceBuilder.getNameKey());
        connectorFields.add(sConnectorInstanceBuilder.getConnectorIdKey());

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
