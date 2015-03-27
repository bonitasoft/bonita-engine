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

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public class BonitaTestRunner extends BlockJUnit4ClassRunner {

    public BonitaTestRunner(final Class<?> klass) throws InitializationError {
        super(klass);
        BonitaTestContext.initializeRunner(klass, this);
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
