package org.bonitasoft.engine.connector;

import java.util.Map;

/**
 * @author Baptiste Mesta.
 */
public interface ConnectorCallback {

    void connectorFinished(SConnector sConnector, Map<String, Object> result);
    void connectorFailed(SConnector sConnector, Exception e);
}
