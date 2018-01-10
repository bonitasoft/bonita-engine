package org.bonitasoft.engine.test.junit;

import java.lang.reflect.Field;

import org.bonitasoft.engine.test.TestEngine;
import org.bonitasoft.engine.test.TestEngineImpl;
import org.bonitasoft.engine.test.annotation.Engine;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * @author Baptiste Mesta
 */
public class BonitaEngineRule implements MethodRule {

    public static String CUSTOM_CONFIG_PLATFORM_INIT = "engine-server/conf/platform-init/bonita-platform-init-custom.xml";
    public static String CUSTOM_CONFIG_PLATFORM = "engine-server/conf/platform/bonita-platform-custom.xml";
    public static String CUSTOM_CONFIG_TENANT = "engine-server/conf/tenants/template/bonita-tenants-custom.xml";

    private TestEngine testEngine;
    private boolean cleanAfterTest;

    protected BonitaEngineRule(TestEngine testEngine) {
        this.testEngine = testEngine;
    }

    public static BonitaEngineRule create() {
        return new BonitaEngineRule(TestEngineImpl.getInstance());
    }

    public static BonitaEngineRule createWith(TestEngine testEngine) {
        return new BonitaEngineRule(testEngine);
    }

    // Used by bonita-web-sp:
    public BonitaEngineRule withCleanAfterTest() {
        cleanAfterTest = true;
        return this;
    }

    // Used by Migration:
    public BonitaEngineRule reuseExistingPlatform() {
        testEngine.setDropOnStart(false);
        return this;
    }

    // Used by Migration:
    public BonitaEngineRule keepPlatformOnShutdown() {
        testEngine.setDropOnStop(false);
        return this;
    }

    @Override
    public Statement apply(Statement statement, FrameworkMethod method, Object target) {
        try {
            handleFieldsAnnotations(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        Statement newStatement = new WithTestEngine(statement, getTestEngine());
        if (cleanAfterTest) {
            newStatement = new WithCleanAfterTest(newStatement, getTestEngine());
        }
        return newStatement;
    }

    private void handleFieldsAnnotations(Object target) throws IllegalAccessException {
        for (Field field : target.getClass().getFields()) {
            handleFieldsAnnotation(field, target);
        }
    }

    private void handleFieldsAnnotation(Field field, Object target) throws IllegalAccessException {
        if (field.getAnnotation(Engine.class) != null) {
            field.set(target, testEngine);
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
            if (start) {
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

    private class WithCleanAfterTest extends Statement {

        private Statement statement;
        private TestEngine testEngine;

        public WithCleanAfterTest(Statement statement, TestEngine testEngine) {
            this.statement = statement;
            this.testEngine = testEngine;
        }

        @Override
        public void evaluate() throws Throwable {
            statement.evaluate();
            testEngine.clearData();
        }
    }
}
