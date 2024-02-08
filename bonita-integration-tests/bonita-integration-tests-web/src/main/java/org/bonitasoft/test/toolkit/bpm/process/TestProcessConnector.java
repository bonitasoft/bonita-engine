/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.test.toolkit.bpm.process;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;

/**
 * @author Colin PUY
 */
public class TestProcessConnector {

    private final String name;
    private final String id;
    private final String version;
    private final String implementationClassname;
    private final String implementationId;
    private final ConnectorEvent connectorEvent;

    private final String resourceFileName;
    private final String resourceFilePath;

    private final List<String> dependencies = new ArrayList<>();

    public TestProcessConnector(String name, String id, String version, String implementationClassname,
            String implementationId,
            ConnectorEvent connectorEvent, String resourceFileName, String resourceFilePath) {
        super();
        this.name = name;
        this.id = id;
        this.version = version;
        this.implementationClassname = implementationClassname;
        this.implementationId = implementationId;
        this.connectorEvent = connectorEvent;
        this.resourceFileName = resourceFileName;
        this.resourceFilePath = resourceFilePath;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public ConnectorEvent getConnectorEvent() {
        return connectorEvent;
    }

    public String getResourceFileName() {
        return resourceFileName;
    }

    public String getResourceFilePath() {
        return resourceFilePath;
    }

    public String getImplementationClassname() {
        return implementationClassname;
    }

    public String getImplementationId() {
        return implementationId;
    }

    public TestProcessConnector addDependency(String dependency) {
        dependencies.add(dependency);
        return this;
    }

    public List<String> getDependencies() {
        return dependencies;
    }
}
