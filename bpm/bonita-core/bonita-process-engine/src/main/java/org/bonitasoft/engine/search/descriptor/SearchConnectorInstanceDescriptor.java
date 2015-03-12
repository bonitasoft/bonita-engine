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
package org.bonitasoft.engine.search.descriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.bpm.connector.ConnectorInstancesSearchDescriptor;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SConnectorInstanceBuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Baptiste Mesta
 */
public class SearchConnectorInstanceDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> searchEntityKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> connectorInstanceAllFields;

    SearchConnectorInstanceDescriptor() {
        final SConnectorInstanceBuilderFactory keyprovider = BuilderFactory.get(SConnectorInstanceBuilderFactory.class);

        searchEntityKeys = new HashMap<String, FieldDescriptor>(7);
        searchEntityKeys.put(ConnectorInstancesSearchDescriptor.NAME, new FieldDescriptor(SConnectorInstance.class, keyprovider.getNameKey()));
        searchEntityKeys.put(ConnectorInstancesSearchDescriptor.ACTIVATION_EVENT,
                new FieldDescriptor(SConnectorInstance.class, keyprovider.getActivationEventKey()));
        searchEntityKeys.put(ConnectorInstancesSearchDescriptor.CONNECTOR_DEFINITION_ID, new FieldDescriptor(SConnectorInstance.class,
                keyprovider.getConnectorIdKey()));
        searchEntityKeys.put(ConnectorInstancesSearchDescriptor.CONNECTOR_DEFINITION_VERSION, new FieldDescriptor(SConnectorInstance.class,
                keyprovider.getVersionKey()));
        searchEntityKeys.put(ConnectorInstancesSearchDescriptor.CONTAINER_ID,
                new FieldDescriptor(SConnectorInstance.class, keyprovider.getContainerIdKey()));
        searchEntityKeys.put(ConnectorInstancesSearchDescriptor.CONTAINER_TYPE,
                new FieldDescriptor(SConnectorInstance.class, keyprovider.getContainerTypeKey()));
        searchEntityKeys.put(ConnectorInstancesSearchDescriptor.STATE, new FieldDescriptor(SConnectorInstance.class, keyprovider.getStateKey()));

        connectorInstanceAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> connectorFields = new HashSet<String>(2);
        connectorFields.add(keyprovider.getNameKey());
        connectorFields.add(keyprovider.getConnectorIdKey());

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
