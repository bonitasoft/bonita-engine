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
package org.bonitasoft.engine.core.migration.model.impl.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.migration.model.SConnectorDefinitionWithEnablement;
import org.bonitasoft.engine.core.migration.model.SMigrationMapping;
import org.bonitasoft.engine.core.migration.model.SOperationWithEnablement;
import org.bonitasoft.engine.core.migration.model.impl.SMigrationMappingImpl;
import org.bonitasoft.engine.xml.ElementBinding;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class SMigrationMappingBinding extends ElementBinding {

    private final List<SConnectorDefinitionWithEnablement> connectors = new ArrayList<SConnectorDefinitionWithEnablement>();

    private final List<SOperationWithEnablement> operations = new ArrayList<SOperationWithEnablement>();

    private final List<SMigrationMapping> mappings = new ArrayList<SMigrationMapping>();

    private String targetState;

    private String targetName;

    private String sourceState;

    private String sourceName;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        sourceName = attributes.get(XMLSMigrationPlan.SOURCE_NAME);
        targetName = attributes.get(XMLSMigrationPlan.TARGET_NAME);
        sourceState = attributes.get(XMLSMigrationPlan.SOURCE_STATE);
        targetState = attributes.get(XMLSMigrationPlan.TARGET_STATE);
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLSMigrationPlan.CONNECTOR_NODE.equals(name)) {
            connectors.add((SConnectorDefinitionWithEnablement) value);
        }
        if (XMLSMigrationPlan.MAPPING_OPERATION_NODE.equals(name)) {
            operations.add((SOperationWithEnablement) value);
        }
        if (XMLSMigrationPlan.MAPPING_NODE.equals(name)) {
            mappings.add((SMigrationMapping) value);
        }
    }

    @Override
    public String getElementTag() {
        return XMLSMigrationPlan.MAPPING_NODE;
    }

    @Override
    public SMigrationMapping getObject() {
        final SMigrationMapping sMigrationMappingImpl = new SMigrationMappingImpl(sourceName, targetName, Integer.valueOf(sourceState),
                targetState == null ? -1 : Integer.valueOf(targetState));
        sMigrationMappingImpl.getConnectors().addAll(connectors);
        sMigrationMappingImpl.getOperations().addAll(operations);
        return sMigrationMappingImpl;
    }

}
