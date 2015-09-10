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

import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.Statement;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author mazourd
 */
public class BonitaEngineRunner extends BlockJUnit4ClassRunner {

    private Field engineField;
    private Field processBARField;
    private BonitaEngineTester initializer = new BonitaEngineTester();
    private String[] userAndPassword = new String[]{"defaultUser"};
    private String barToBeDeployed;
    private ProcessDefinition processDefinition;
    public BonitaEngineRunner(Class<?> klass) throws Exception {
        super(klass);
        processAnnotedField(klass);
    }

    private void processAnnotedField(Class<?> klass) throws Exception {
        Field[] fields = klass.getDeclaredFields();
        for (Field field : fields) {
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                if (annotation.annotationType().equals(InjectEngine.class)) {
                    field.setAccessible(true);
                    engineField = field;
                    InjectEngine engineAnnotation = (InjectEngine) annotation;
                    userAndPassword = new String[]{engineAnnotation.user(), engineAnnotation.password()};
                }
                if (annotation.annotationType().equals(DeployBAR.class)){
                    field.setAccessible(true);
                    processBARField = field;
                    barToBeDeployed = new String(((DeployBAR) annotation).name());
                }
            }
        }
    }

    @Override
    protected Statement classBlock(final RunNotifier notifier) {
        Statement statement = super.classBlock(notifier);
        statement = withGlobalBefore(statement);
        if (processBARField != null) {
            try {
                initializer.getProcessDeployer().disableAndDeleteProcess(processDefinition.getId());
            } catch (Exception e) {
                //ignore
            }
        }
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
        if(barToBeDeployed != "noBar" && barToBeDeployed != null){
            BusinessArchive businessArchive = BusinessArchiveFactory.readBusinessArchive(this.getClass().getResourceAsStream(barToBeDeployed));
            processDefinition = initializer.getProcessDeployer().deployAndEnableProcess(businessArchive);
        }
        if (processBARField != null){
            processBARField.set(test,processDefinition);
        }
        return test;
    }

    private Statement withGlobalBefore(final Statement statement) {
        return new WithGlobalBefore(statement, this);
    }

    public BonitaEngineTester getInitializer() {
        return initializer;
    }
}
