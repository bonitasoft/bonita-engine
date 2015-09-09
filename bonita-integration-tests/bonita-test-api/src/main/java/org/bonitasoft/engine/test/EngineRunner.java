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
import org.junit.runners.model.Statement;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author mazourd
 */
public class EngineRunner extends BlockJUnit4ClassRunner {

    private Field engineField;
    private EngineInitializer initializer = new EngineInitializer();
    private String[] userAndPassword = new String[]{"defaultUser"};
    public EngineRunner(Class<?> klass) throws Exception {
        super(klass);
        processAnnotedField(klass);
    }

    private void processAnnotedField(Class<?> klass) throws Exception {
        Field[] fields = klass.getDeclaredFields();
        for (Field field : fields) {
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                if (annotation.annotationType().equals(EngineInterface.class)) {
                    field.setAccessible(true);
                    engineField = field;
                    EngineInterface engineAnnotation = (EngineInterface) annotation;
                    userAndPassword = new String[]{engineAnnotation.user(), engineAnnotation.password()};
                }
            }
        }
    }

    @Override
    protected Statement classBlock(final RunNotifier notifier) {

        Statement statement = super.classBlock(notifier);
        statement = withGlobalBefore(statement);
        return statement;
    }

    @Override
    protected Object createTest() throws Exception {
        Object test = super.createTest();
        initializer.defaultLogin();
        if(userAndPassword[0] != "defaultUser") {
            try {
                initializer.deleteUser(userAndPassword[0]);
            } catch (Exception e) {
                //ignore
            }
            initializer.createUser(userAndPassword[0], userAndPassword[1]);
        }
        if (engineField != null) {
            engineField.set(test, initializer);
        }
        return test;
    }

    private Statement withGlobalBefore(final Statement statement) {
        return new WithGlobalBefore(statement, this);
    }

    public EngineInitializer getInitializer() {
        return initializer;
    }
}
