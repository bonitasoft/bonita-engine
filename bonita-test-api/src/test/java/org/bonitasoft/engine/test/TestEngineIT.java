package org.bonitasoft.engine.test;

import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class TestEngineIT {

    @Test
    public void startEngine() throws Exception {
        final TestEngine testEngine = new TestEngine();
        testEngine.start();
        testEngine.stop();
    }

}
