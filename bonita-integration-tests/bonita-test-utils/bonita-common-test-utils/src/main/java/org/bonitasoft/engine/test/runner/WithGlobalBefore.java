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

import java.lang.reflect.Method;

import org.junit.runners.model.Statement;

final class WithGlobalBefore extends Statement {

    private final Statement classBlock;

    private final BonitaRunner testRunner;

    private final Class<?> initializer;

    WithGlobalBefore(final Statement classBlock, final BonitaRunner testRunner, final Class<?> annotatedInitializer) {
        this.classBlock = classBlock;
        this.testRunner = testRunner;
        this.initializer = annotatedInitializer;
    }

    @Override
    public void evaluate() throws Throwable {
        if (testRunner.isRoot()) {
            if(initializer == null){
                throw new IllegalStateException("Unable to run the suite as a root suite because it does not have a @Initialize set");
            }
            Method declaredMethod = initializer.getDeclaredMethod("beforeAll");
            declaredMethod.invoke(null);
        }
        classBlock.evaluate();
    }
}
