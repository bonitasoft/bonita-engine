/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.classloader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.io.IOUtil.generateJar;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.connectors.TestConnector3;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.filter.user.TestFilterWithAutoAssign;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

public class ClassLoaderIT extends TestWithUser {

    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @After
    public void cleanUp() throws Exception {
        String businessDataModelVersion = getTenantAdministrationAPI().getBusinessDataModelVersion();
        if (businessDataModelVersion != null) {
            getTenantAdministrationAPI().pause();
            getTenantAdministrationAPI().cleanAndUninstallBusinessDataModel();
            getTenantAdministrationAPI().resume();
        }
    }

    @Test
    public void should_refresh_classloader_only_once_on_deploy_process() throws Exception {
        BusinessArchive businessArchive = createProcessWithDependencies();
        User user = getIdentityAPI().createUser("john", "bpm");

        systemOutRule.clearLog();

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(businessArchive, ACTOR_NAME, user);

        String processDeployLog = systemOutRule.getLog();

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance task = waitForUserTaskAndGetIt(processInstance, "step1");

        assertEquals("stringFromPublicMethod", task.getDisplayName());
        assertThat(processDeployLog)
                .containsOnlyOnce("Refreshing classloader PROCESS:" + processDefinition.getId());
    }

    public String toString() {
        return "MyObject";
    }

    @Test
    public void should_be_able_to_execute_scripts_on_processes_having_same_classes() throws Exception {
        byte[] myObjectJar1 = generateJar(
                Pair.of("MyObject",
                        "public interface MyObject extends java.io.Serializable {}"),
                Pair.of("MyObjectImpl1",
                        "public class MyObjectImpl1 implements MyObject {\n" +
                                "   public String toString() {\n" +
                                "        return \"MyObjectImpl\";\n" +
                                "    }\n" +
                                "}"));
        byte[] myObjectJar2 = generateJar(
                Pair.of("MyObject",
                        "public interface MyObject extends java.io.Serializable {}"),
                Pair.of("MyObjectImpl2",
                        "public class MyObjectImpl2 implements MyObject {\n" +
                                "   public String toString() {\n" +
                                "        return \"MyObjectImpl\";\n" +
                                "    }\n" +
                                "}"));
        BusinessArchive bar1 = new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(new ProcessDefinitionBuilder().createNewInstance("process1 with MyObject", "1.0")
                        .addData("myObject", "MyObject", new ExpressionBuilder().createGroovyScriptExpression("s1",
                                "new MyObjectImpl1()", "MyObject"))
                        .getProcess())
                .addClasspathResource(new BarResource("myObjectJar.jar", myObjectJar1)).done();
        BusinessArchive bar2 = new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(new ProcessDefinitionBuilder().createNewInstance("process2 with MyObject", "1.0")
                        .addData("myObject", "MyObject", new ExpressionBuilder().createGroovyScriptExpression("s1",
                                "new MyObjectImpl2()", "MyObject"))
                        .getProcess())
                .addClasspathResource(new BarResource("myObjectJar.jar", myObjectJar2)).done();

        ProcessDefinition processDefinition1 = deployAndEnableProcess(bar1);
        ProcessDefinition processDefinition2 = deployAndEnableProcess(bar2);

        ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition1.getId());
        waitForProcessToFinish(processInstance1);
        ProcessInstance processInstance2 = getProcessAPI().startProcess(processDefinition2.getId());
        waitForProcessToFinish(processInstance2);
    }

    @Test
    public void should_refresh_classloader_only_once_on_deploy_bdm() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
        final byte[] zip = converter.zip(buildCustomBOM());
        getTenantAdministrationAPI().pause();
        getTenantAdministrationAPI().cleanAndUninstallBusinessDataModel();
        systemOutRule.clearLog();
        getTenantAdministrationAPI().installBusinessDataModel(zip);
        String deployBDMLog = systemOutRule.getLog();
        getTenantAdministrationAPI().resume();

        assertThat(deployBDMLog).containsOnlyOnce("Refreshing classloader TENANT:");
    }

    @Test
    public void should_not_refresh_classloader_on_delete_process_definition() throws Exception {
        BusinessArchive businessArchive = createProcessWithDependencies();
        User user = getIdentityAPI().createUser("john", "bpm");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(businessArchive, ACTOR_NAME, user);

        systemOutRule.clearLog();
        getProcessAPI().disableAndDeleteProcessDefinition(processDefinition.getId());
        String processDeployLog = systemOutRule.getLog();

        assertThat(processDeployLog)
                .doesNotContain("Refreshing classloader PROCESS:" + processDefinition.getId());
    }

    @Test
    public void should_be_able_to_fix_groovy_script_by_updating_dependency_in_platform_classloader() throws Exception {
        PlatformSession session = loginOnPlatform();
        PlatformAPIAccessor.getPlatformCommandAPI(session).addDependency("hello-there-1.0.0.jar", generateJar("Hello",
                "public class Hello{",
                "public String hello(){",
                "return \"Hello there!\";",
                "}",
                "}"));
        logoutOnPlatform(session);
        loginOnDefaultTenantWithDefaultTechnicalUser();

        ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(
                "processWithDisplayName",
                "1.0");
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addUserTask("step0", ACTOR_NAME).addDisplayName(
                new ExpressionBuilder().createGroovyScriptExpression("groovyExpr",
                        "new Hello().hello()", String.class.getName()));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(),
                ACTOR_NAME, user);
        ProcessInstance p1 = getProcessAPI().startProcess(processDefinition.getId());

        assertThat(waitForUserTaskAndGetIt(p1, "step0").getDisplayName()).isEqualTo("Hello there!");

        session = loginOnPlatform();
        PlatformAPIAccessor.getPlatformCommandAPI(session).removeDependency("hello-there-1.0.0.jar");
        PlatformAPIAccessor.getPlatformCommandAPI(session).addDependency("hello-there-1.0.1.jar", generateJar("Hello",
                "public class Hello{",
                "public String hello(){",
                "return \"Hello there! General Kenobi.\";",
                "}",
                "}"));
        logoutOnPlatform(session);
        loginOnDefaultTenantWithDefaultTechnicalUser();

        ProcessInstance p2 = getProcessAPI().startProcess(processDefinition.getId());

        assertThat(waitForUserTaskAndGetIt(p2, "step0").getDisplayName()).isEqualTo("Hello there! General Kenobi.");

        disableAndDeleteProcess(processDefinition);
    }

    private BusinessObjectModel buildCustomBOM() {
        final SimpleField name = new SimpleField();
        name.setName("name");
        name.setType(FieldType.STRING);
        final BusinessObject somethings = new BusinessObject();
        somethings.setQualifiedName("org.acme.pojo.Something");
        somethings.addField(name);
        final BusinessObjectModel model = new BusinessObjectModel();
        model.addBusinessObject(somethings);
        return model;
    }

    private BusinessArchive createProcessWithDependencies() throws InvalidExpressionException,
            InvalidProcessDefinitionException, IOException, InvalidBusinessArchiveFormatException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder();
        final ProcessDefinitionBuilder pBuilder = processDefinitionBuilder.createNewInstance("emptyProcess",
                String.valueOf(System.currentTimeMillis()));
        pBuilder.addShortTextData("aData", new ExpressionBuilder().createGroovyScriptExpression("myScript",
                "new org.bonitasoft.engine.test.TheClassOfMyLibrary().aPublicMethod()", String.class.getName()));
        pBuilder.addActor(ACTOR_NAME)
                .addUserTask("step1", ACTOR_NAME)
                .addDisplayName(
                        new ExpressionBuilder().createGroovyScriptExpression("myScript",
                                "new org.bonitasoft.engine.test.TheClassOfMyLibrary().aPublicMethod()",
                                String.class.getName()));
        final DesignProcessDefinition designProcessDefinition = pBuilder.done();

        final BusinessArchiveBuilder builder = new BusinessArchiveBuilder()
                .createNewBusinessArchive()
                .setProcessDefinition(designProcessDefinition);
        builder.addClasspathResource(new BarResource("mylibrary.jar",
                IOUtils.toByteArray(CommonAPIIT.class.getResourceAsStream("/mylibrary-jar.bak"))));
        builder.addClasspathResource(BuildTestUtil.generateJarAndBuildBarResource(TestFilterWithAutoAssign.class,
                "TestFilterWithAutoAssign.jar"));
        builder.addClasspathResource(
                BuildTestUtil.generateJarAndBuildBarResource(TestConnector3.class, "TestConnector3.jar"));
        return builder.done();
    }
}
