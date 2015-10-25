package org.bonitasoft.engine.test.junit;

import com.sun.istack.internal.logging.Logger;
import org.bonitasoft.engine.test.TestEngine;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 *
 *
 *
 * @author Baptiste Mesta
 */
public class BonitaEngineRule implements TestRule {

    @Override
    public Statement apply(Statement statement, Description description) {
        return new WithTestEngine(statement, getTestEngine());
    }

    private static class WithTestEngine extends Statement {
        private Statement statement;
        private TestEngine testEngine;

        public WithTestEngine(Statement statement, TestEngine testEngine) {
            this.statement = statement;
            this.testEngine = testEngine;
        }

        @Override
        public void evaluate() throws Throwable {
            startEngine();
            statement.evaluate();
        }

        private void startEngine() throws Exception {
            final boolean start = testEngine.start();
            if(start){
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        try {
                            testEngine.stop();
                        } catch (Exception e) {
                            Logger.getLogger(TestEngine.class).severe("unable to stop the engine",e);
                        }
                    }
                });
            }
        }
    }

    protected TestEngine getTestEngine() {
        return TestEngine.getInstance();
    }
}
