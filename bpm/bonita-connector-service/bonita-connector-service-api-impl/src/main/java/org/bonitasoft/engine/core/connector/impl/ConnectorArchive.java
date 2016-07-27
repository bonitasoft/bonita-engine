package org.bonitasoft.engine.core.connector.impl;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Baptiste Mesta
 */
class ConnectorArchive {

    private Map<String, byte[]> dependencies = new HashMap<>();
    private String connectorImplName;
    private byte[] connectorImplContent;

    public void addDependency(String entryName, byte[] fileContent) {
        dependencies.put(entryName, fileContent);
    }

    public Map<String, byte[]> getDependencies() {
        return dependencies;
    }

    public void setConnectorImpl(String connectorImplName, byte[] connectorImplContent) {
        this.connectorImplName = connectorImplName;
        this.connectorImplContent = connectorImplContent;
    }

    public byte[] getConnectorImplContent() {
        return connectorImplContent;
    }

    public String getConnectorImplName() {
        return connectorImplName;
    }
}
