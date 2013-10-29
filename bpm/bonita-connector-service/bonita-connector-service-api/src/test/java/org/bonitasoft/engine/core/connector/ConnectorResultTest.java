package org.bonitasoft.engine.core.connector;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ConnectorResultTest {

    @Test
    public void test() {
        final ConnectorResult result = new ConnectorResult(null, null);
        assertTrue(result.getResult().isEmpty());
    }

}
