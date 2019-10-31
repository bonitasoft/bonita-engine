package org.bonitasoft.engine.test.junit;


import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.BonitaDatabaseConfiguration;
import org.bonitasoft.engine.test.TestEngine;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class BonitaEngineRuleTest {

    private final MyTestEngine testEngineToInject = new MyTestEngine();
    @Rule
    public BonitaEngineRule bonitaEngineRule = BonitaEngineRule.createWith(testEngineToInject);


    @Test
    public void should_TestEngine_be_started() throws Exception {
        assertThat(testEngineToInject.isStarted).isTrue();
    }

    private static class MyTestEngine implements TestEngine {

        public boolean isStarted;

        @Override
        public boolean start() throws Exception {
            isStarted = true;
            return false;
        }

        @Override
        public void stop() throws Exception {

        }

        @Override
        public void clearData() throws Exception {

        }

        @Override
        public void setDropOnStart(boolean dropOnStart) {

        }

        @Override
        public void setDropOnStop(boolean dropOnStop) {

        }

        @Override
        public void setBonitaDatabaseProperties(BonitaDatabaseConfiguration database) {
            
        }

        @Override
        public void setBusinessDataDatabaseProperties(BonitaDatabaseConfiguration database) {

        }
    }
}