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

/**
 * @author mazourd
 */
public class Engine extends BlockJUnit4ClassRunner {


    private EngineInitializer initializer = new EngineInitializer();
    public Engine(Class<?> klass) throws Exception {
        super(klass);
        //processAnnotedField(klass);
        System.out.println("start du runner");
    }

    /*private void processAnnotedField(Class<?> klass) {
        Field[] fields = klass.getDeclaredFields();
        for (Field field : fields) {
            System.out.println("========");
            System.out.println(field.getName());
            System.out.println(field.getType());
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                System.out.println(annotation);
                System.out.println(annotation.annotationType());
                if (annotation.annotationType().equals(BusinessArchive.class)) {
                    //deploy
                    field.setAccessible(true);

                    businessArchives.put(field, ((BusinessArchive) annotation).resource());
                }
                if (annotation.annotationType().equals(Engine.class)) {
                    //deploy
                    field.setAccessible(true);
                    engineField = field;
                    Engine engineAnnotation = (Engine) annotation;
                    String type = engineAnnotation.type();
                    String url = engineAnnotation.url();
                    String name = engineAnnotation.name();

                    if (type == null || type.isEmpty() || type.equals("LOCAL")) {
                        engine = BonitaTestEngine.defaultLocalEngine();
                    } else if (type.equals("HTTP")) {
                        engine = BonitaTestEngine.remoteHttp(url, name);
                    }
                }
            }
        }
    }*/

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
