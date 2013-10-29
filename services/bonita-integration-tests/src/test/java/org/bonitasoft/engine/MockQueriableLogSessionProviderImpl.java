package org.bonitasoft.engine;

import org.bonitasoft.engine.services.QueriableLogSessionProvider;

/**
 * @author Elias Ricken de Medeiros
 */
public class MockQueriableLogSessionProviderImpl implements QueriableLogSessionProvider {

    @Override
    public String getUserId() {
        return "admin";
    }

    @Override
    public String getClusterNode() {
        return "node1";
    }

}
