/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

package org.bonitasoft.engine.test.runner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.bonitasoft.engine.test.runner.BonitaSuiteRunner.Initializer;
import org.junit.runner.Runner;
import org.junit.runners.model.InitializationError;

/**
 * @author Baptiste Mesta
 */
public class BonitaTestContext {

    public static Set<Runner> runnerThatUsesBonita = new HashSet<>();

    public static Class<?> initializer;
    private static boolean isInitialized;

    public static void setInitializer(Class<?> initializer) {
        if (initializer != null) {
            BonitaTestContext.initializer = initializer;
        }
    }

    public static void initializeEngine() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (!isInitialized) {
            if (initializer == null) {
                throw new IllegalStateException("Unable to run the suite as a root suite because it does not have a @Initialize set");
            }
            System.out.println("Found these runners: " + runnerThatUsesBonita);
            Method declaredMethod = initializer.getDeclaredMethod("beforeAll");
            declaredMethod.invoke(null);
            isInitialized = true;
        }
    }

    public static void shutdownEngine() throws Throwable {
        try {
            if (!runnerThatUsesBonita.isEmpty()) {
                return;
            }
            if (initializer == null) {
                throw new IllegalStateException("Unable to run the suite as a root suite because it does not have a @Initialize set");
            }
            Method declaredMethod = initializer.getDeclaredMethod("afterAll");
            declaredMethod.invoke(null);
        } catch (final InvocationTargetException e) {
            throw e.getCause();
        }
    }

    public static void addRunner(Runner runner) {
        runnerThatUsesBonita.add(runner);
    }

    public static void removeRunner(Runner runner) {
        runnerThatUsesBonita.remove(runner);
    }

    public static void initializeRunner(Class<?> klass, Runner runner) throws InitializationError {
        BonitaTestContext.setInitializer(getAnnotatedInitializer(klass));
        BonitaTestContext.addRunner(runner);
    }

    static Class<?> getAnnotatedInitializer(final Class<?> klass) throws InitializationError {
        final Initializer annotation = klass.getAnnotation(Initializer.class);
        return annotation == null ? null : annotation.value();
    }
}
