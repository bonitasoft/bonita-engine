package org.bonitasoft.engine.test.junit;

import org.bonitasoft.engine.test.TestEngine;
import org.bonitasoft.engine.test.TestEngineImpl;
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

    private TestEngine testEngine;

    protected BonitaEngineRule(TestEngine testEngine){
        this.testEngine = testEngine;
    }

    public static BonitaEngineRule create(){
        return new BonitaEngineRule(TestEngineImpl.getInstance());
    }

    public static BonitaEngineRule createWith(TestEngine testEngine){
        return new BonitaEngineRule(testEngine);
    }


    @Override
    public Statement apply(Statement statement, Description description) {
        //TODO parse annotations here
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
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    protected TestEngine getTestEngine() {
        return testEngine;
    }
}
