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

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runners.model.Statement;

public class BonitaSuiteRunner extends Suite implements BonitaRunner {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface Initializer {

        public Class<?> value();
    }

    static Class<?> getAnnotatedInitializer(final Class<?> klass) throws InitializationError {
        final Initializer annotation = klass.getAnnotation(Initializer.class);
        if (annotation == null) {
            throw new InitializationError(String.format("class '%s' must have a Initializer annotation", klass.getName()));
        }
        return annotation.value();
    }

    private boolean isRoot;

    private Class<?> annotatedInitializer;

    public BonitaSuiteRunner(final Class<?> klass, final Class<?>[] suiteClasses) throws InitializationError {
        super(klass, suiteClasses);
        isRoot = true;
    }

    public BonitaSuiteRunner(final Class<?> klass, final List<Runner> runners) throws InitializationError {
        super(klass, runners);
        isRoot = true;
    }

    public BonitaSuiteRunner(final Class<?> klass, final RunnerBuilder builder) throws InitializationError {
        super(klass, builder);
        annotatedInitializer = getAnnotatedInitializer(klass);
        isRoot = true;
    }

    public BonitaSuiteRunner(final RunnerBuilder builder, final Class<?> klass, final Class<?>[] suiteClasses) throws InitializationError {
        super(builder, klass, suiteClasses);
        isRoot = true;
    }

    public BonitaSuiteRunner(final RunnerBuilder builder, final Class<?>[] classes) throws InitializationError {
        super(builder, classes);
        isRoot = true;
    }

    @Override
    protected Statement childrenInvoker(final RunNotifier notifier) {
        // TODO Auto-generated method stub
        return super.childrenInvoker(notifier);
    }

    @Override
    protected List<Runner> getChildren() {
        // TODO Auto-generated method stub
        return super.getChildren();
    }

    @Override
    public void run(final RunNotifier notifier) {
        initializeClasses();
        super.run(notifier);
    }

    private void initializeClasses() {
        final List<Runner> children = getChildren();
        setRootToFalse(children);
    }

    private void setRootToFalse(final List<Runner> children) {
        for (final Runner runner : children) {
            if (runner instanceof BonitaRunner) {
                final BonitaRunner bonitaRunner = (BonitaRunner) runner;
                bonitaRunner.setRoot(false);
            } else if (runner instanceof Suite) {
                final Suite suite = (Suite) runner;
                try {
                    final Method method = Suite.class.getDeclaredMethod("getChildren");
                    method.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    final List<Runner> invoke = (List<Runner>) method.invoke(suite);
                    setRootToFalse(invoke);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void setRoot(final boolean isRoot) {
        this.isRoot = isRoot;
        setRootToFalse(getChildren());
    }

    @Override
    protected Statement classBlock(final RunNotifier notifier) {

        Statement statement = super.classBlock(notifier);
        statement = withGlobalBefore(statement);
        statement = withGlobalAfter(statement);
        return statement;
    }

    @Override
    public boolean isRoot() {
        return isRoot;
    }

    private Statement withGlobalBefore(final Statement statement) {
        return new WithGlobalBefore(statement, this, annotatedInitializer);
    }

    private Statement withGlobalAfter(final Statement statement) {
        return new WithGlobalAfter(statement, this, annotatedInitializer);
    }

}
