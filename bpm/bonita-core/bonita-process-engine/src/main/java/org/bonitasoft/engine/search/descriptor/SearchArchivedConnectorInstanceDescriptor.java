/**
 * Copyright (C) 2013 BonitaSoft S.A.
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

import org.bonitasoft.engine.bpm.connector.ArchiveConnectorInstancesSearchDescriptor;
import org.bonitasoft.engine.core.process.instance.model.archive.SAConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAConnectorInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Baptiste Mesta
 */
public class SearchArchivedConnectorInstanceDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> searchEntityKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> archivedConnectorssAllFields;

    SearchArchivedConnectorInstanceDescriptor(final BPMInstanceBuilders instanceBuilders) {
        final SAConnectorInstanceBuilder builder = instanceBuilders.getSAConnectorInstanceBuilder();
        searchEntityKeys = new HashMap<String, FieldDescriptor>(7);
        searchEntityKeys.put(ArchiveConnectorInstancesSearchDescriptor.NAME, new FieldDescriptor(SAConnectorInstance.class, builder.getNameKey()));
        searchEntityKeys.put(ArchiveConnectorInstancesSearchDescriptor.ACTIVATION_EVENT,
                new FieldDescriptor(SAConnectorInstance.class, builder.getActivationEventKey()));
        searchEntityKeys.put(ArchiveConnectorInstancesSearchDescriptor.CONNECTOR_DEFINITION_ID,
                new FieldDescriptor(SAConnectorInstance.class, builder.getConnectorIdKey()));
        searchEntityKeys.put(ArchiveConnectorInstancesSearchDescriptor.CONNECTOR_DEFINITION_VERSION,
                new FieldDescriptor(SAConnectorInstance.class, builder.getVersionKey()));
        searchEntityKeys.put(ArchiveConnectorInstancesSearchDescriptor.CONTAINER_ID,
                new FieldDescriptor(SAConnectorInstance.class, builder.getContainerIdKey()));
        searchEntityKeys.put(ArchiveConnectorInstancesSearchDescriptor.CONTAINER_TYPE,
                new FieldDescriptor(SAConnectorInstance.class, builder.getContainerTypeKey()));
        searchEntityKeys.put(ArchiveConnectorInstancesSearchDescriptor.STATE, new FieldDescriptor(SAConnectorInstance.class, builder.getStateKey()));

        archivedConnectorssAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> connectorFields = new HashSet<String>(2);
        connectorFields.add(builder.getNameKey());
        connectorFields.add(builder.getConnectorIdKey());

        archivedConnectorssAllFields.put(SAConnectorInstance.class, connectorFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return searchEntityKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return archivedConnectorssAllFields;
    }

}
