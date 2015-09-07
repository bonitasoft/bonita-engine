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

import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import java.util.List;

import static org.bonitasoft.engine.test.EngineInitializer.startEngine;
import static org.bonitasoft.engine.test.EngineInitializer.stopEngine;

/**
 * @author mazourd
 */
public class EngineSuite extends Suite {

    public EngineSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(klass, builder);
        System.out.println("start du runner");
        try {
            startEngine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Runtime.getRuntime().addShutdownHook(new Message());
    }

    public EngineSuite(RunnerBuilder builder, Class<?>[] classes) throws InitializationError {
        super(builder, classes);
                System.out.println("start du runner");
        try {
            startEngine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Runtime.getRuntime().addShutdownHook(new Message());
    }

    protected EngineSuite(Class<?> klass, Class<?>[] suiteClasses) throws InitializationError {
        super(klass, suiteClasses);
                System.out.println("start du runner");
        try {
            startEngine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Runtime.getRuntime().addShutdownHook(new Message());
    }

    protected EngineSuite(RunnerBuilder builder, Class<?> klass, Class<?>[] suiteClasses) throws InitializationError {
        super(builder, klass, suiteClasses);
                System.out.println("start du runner");
        try {
            startEngine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Runtime.getRuntime().addShutdownHook(new Message());
    }

    protected EngineSuite(Class<?> klass, List<Runner> runners) throws InitializationError {
        super(klass, runners);
                System.out.println("start du runner");
        try {
            startEngine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Runtime.getRuntime().addShutdownHook(new Message());
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
}
