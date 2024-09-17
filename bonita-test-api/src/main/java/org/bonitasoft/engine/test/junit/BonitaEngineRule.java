/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.test.junit;

import java.lang.reflect.InvocationTargetException;

import org.bonitasoft.engine.test.TestEngine;
import org.bonitasoft.engine.test.TestEngineImpl;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * @author Baptiste Mesta
 */
public class BonitaEngineRule implements MethodRule {

    private final TestEngine testEngine;
    private boolean cleanAfterTest;

    protected BonitaEngineRule(TestEngine testEngine) {
        this.testEngine = testEngine;
    }

    public static BonitaEngineRule create() {
        try {
            var testEngineSpClazz = BonitaEngineRule.class.getClassLoader()
                    .loadClass("com.bonitasoft.engine.test.TestEngineSP");
            TestEngine testEngineSpInstance = (TestEngine) testEngineSpClazz.getMethod("getInstance").invoke(null);
            return new BonitaEngineRule(testEngineSpInstance);
        } catch (ClassNotFoundException e) {
            // Use Community TestEngine
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return new BonitaEngineRule(TestEngineImpl.getInstance());
    }

    public static BonitaEngineRule createWith(TestEngine testEngine) {
        return new BonitaEngineRule(testEngine);
    }

    public BonitaEngineRule withCleanAfterTest() {
        cleanAfterTest = true;
        return this;
    }

    // Used by Migration:
    public BonitaEngineRule reuseExistingPlatform() {
        testEngine.setDropOnStart(false);
        return this;
    }

    @Override
    public Statement apply(Statement statement, FrameworkMethod method, Object target) {
        Statement newStatement = new WithTestEngine(statement, getTestEngine());
        if (cleanAfterTest) {
            newStatement = new WithCleanAfterTest(newStatement, getTestEngine());
        }
        return newStatement;
    }

    private static class WithTestEngine extends Statement {

        private final Statement statement;
        private final TestEngine testEngine;

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
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        testEngine.stop();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));
            }
        }
    }

    protected TestEngine getTestEngine() {
        return testEngine;
    }

    private static class WithCleanAfterTest extends Statement {

        private final Statement statement;
        private final TestEngine testEngine;

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
