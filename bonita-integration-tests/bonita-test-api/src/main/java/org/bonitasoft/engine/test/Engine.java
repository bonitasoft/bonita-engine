/*
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
 */

package org.bonitasoft.engine.test;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import static org.bonitasoft.engine.test.EngineInitializer.stopEngine;

/**
 * @author mazourd
 */
public class Engine extends BlockJUnit4ClassRunner {

    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
     * @param klass
     * @throws InitializationError if the test class is malformed.
     */
    public Engine(Class<?> klass) throws Exception {
        super(klass);
        System.out.println("start du runner");
    }

    static class Message extends Thread {

        public void run() {
            try {
                stopEngine();
            } catch (Exception e) {
                //ignore for now
            }
            System.out.println("Bye.");
        }
    }

    @Override
    protected Statement classBlock(final RunNotifier notifier) {

        Statement statement = super.classBlock(notifier);
        statement = withGlobalBefore(statement);
        return statement;
    }

    private Statement withGlobalBefore(final Statement statement) {
        return new WithGlobalBefore(statement, this);
    }

}
