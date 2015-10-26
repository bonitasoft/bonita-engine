package org.bonitasoft.engine.test.junit;

import java.lang.reflect.Field;

import org.bonitasoft.engine.test.TestEngine;
import org.bonitasoft.engine.test.TestEngineImpl;
import org.bonitasoft.engine.test.annotation.Engine;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 *
 *
 *
 * @author Baptiste Mesta
 */
public class BonitaEngineRule implements MethodRule {

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
    public Statement apply(Statement statement, FrameworkMethod method, Object target) {
        try {
            handleFieldsAnnotations(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        return new WithTestEngine(statement, getTestEngine());
    }

    private void handleFieldsAnnotations(Object target) throws IllegalAccessException {
        for (Field field : target.getClass().getFields()) {
            handleFieldsAnnotation(field,target);
        }
    }

    private void handleFieldsAnnotation(Field field, Object target) throws IllegalAccessException {
        if(field.getAnnotation(Engine.class) != null){
            field.set(target,testEngine);
        }
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
