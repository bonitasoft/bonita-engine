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
import org.bonitasoft.engine.core.migration.model.SMigrationPlan;
import org.bonitasoft.engine.core.migration.model.SOperationWithEnablement;
import org.bonitasoft.engine.core.migration.model.impl.SMigrationPlanImpl;
import org.bonitasoft.engine.xml.ElementBinding;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class SMigrationPlanBinding extends ElementBinding {

    private final List<SConnectorDefinitionWithEnablement> connectors = new ArrayList<SConnectorDefinitionWithEnablement>();

    private final List<SOperationWithEnablement> operations = new ArrayList<SOperationWithEnablement>();

    private final List<SMigrationMapping> mappings = new ArrayList<SMigrationMapping>();

    private String targetVersion;

    private String targetName;

    private String sourceVersion;

    private String sourceName;

    private String description;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        description = attributes.get(XMLSMigrationPlan.DESCRIPTION);
        sourceName = attributes.get(XMLSMigrationPlan.SOURCE_NAME);
        targetName = attributes.get(XMLSMigrationPlan.TARGET_NAME);
        sourceVersion = attributes.get(XMLSMigrationPlan.SOURCE_VERSION);
        targetVersion = attributes.get(XMLSMigrationPlan.TARGET_VERSION);
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
        return XMLSMigrationPlan.MIGRATION_PLAN_NODE;
    }

    @Override
    public SMigrationPlan getObject() {
        final SMigrationPlanImpl sMigrationPlanImpl = new SMigrationPlanImpl(description, targetVersion, targetName, sourceVersion, sourceName);
        sMigrationPlanImpl.getConnectors().addAll(connectors);
        sMigrationPlanImpl.getOperations().addAll(operations);
        sMigrationPlanImpl.getMappings().addAll(mappings);
        return sMigrationPlanImpl;
    }

}
