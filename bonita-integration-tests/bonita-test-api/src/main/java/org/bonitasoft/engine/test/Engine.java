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
public class Engine extends BlockJUnit4ClassRunner {

    private Field engineField;
    private EngineInitializer initializer = new EngineInitializer();

    public Engine(Class<?> klass) throws Exception {
        super(klass);
        processAnnotedField(klass);
    }

    private void processAnnotedField(Class<?> klass) throws Exception {
        Field[] fields = klass.getDeclaredFields();
        for (Field field : fields) {
            System.out.println("========");
            System.out.println(field.getName());
            System.out.println(field.getType());
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                if (annotation.annotationType().equals(EngineAnnotationInterface.class)) {

                    field.setAccessible(true);
                    engineField = field;
                    EngineAnnotationInterface engineAnnotation = (EngineAnnotationInterface) annotation;
                    String user = engineAnnotation.user();
                    String password = engineAnnotation.password();
                    initializer.defaultLogin();
                    initializer.setTest(user+password);
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

    private Statement withGlobalBefore(final Statement statement) {
        return new WithGlobalBefore(statement, this);
    }

    public EngineInitializer getInitializer() {
        return initializer;
    }
}
