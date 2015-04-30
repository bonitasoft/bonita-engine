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
import java.util.List;

import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runners.model.Statement;

public class BonitaSuiteRunner extends Suite {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface Initializer {

        public Class<?> value();
    }

    public BonitaSuiteRunner(final Class<?> klass, final Class<?>[] suiteClasses) throws InitializationError {
        super(klass, suiteClasses);
        BonitaTestContext.initializeRunner(klass, this);
    }

    public BonitaSuiteRunner(final Class<?> klass, final List<Runner> runners) throws InitializationError {
        super(klass, runners);
        BonitaTestContext.initializeRunner(klass, this);
    }

    public BonitaSuiteRunner(final Class<?> klass, final RunnerBuilder builder) throws InitializationError {
        super(klass, builder);
        BonitaTestContext.initializeRunner(klass, this);
    }

    public BonitaSuiteRunner(final RunnerBuilder builder, final Class<?> klass, final Class<?>[] suiteClasses) throws InitializationError {
        super(builder, klass, suiteClasses);
        BonitaTestContext.initializeRunner(klass, this);
    }

    public BonitaSuiteRunner(final RunnerBuilder builder, final Class<?>[] classes) throws InitializationError {
        super(builder, classes);
    }

    @Override
    protected Statement childrenInvoker(final RunNotifier notifier) {
        return super.childrenInvoker(notifier);
    }

    @Override
    protected List<Runner> getChildren() {
        return super.getChildren();
    }

    @Override
    public void run(final RunNotifier notifier) {
        super.run(notifier);
    }

    @Override
    protected Statement classBlock(final RunNotifier notifier) {

        Statement statement = super.classBlock(notifier);
        statement = withGlobalBefore(statement);
        statement = withGlobalAfter(statement);
        return statement;
    }

    private Statement withGlobalBefore(final Statement statement) {
        return new WithGlobalBefore(statement, this);
    }

    private Statement withGlobalAfter(final Statement statement) {
        return new WithGlobalAfter(statement, this);
    }

}
