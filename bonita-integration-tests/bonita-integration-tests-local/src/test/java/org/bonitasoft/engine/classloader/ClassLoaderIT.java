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
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.TestWithTechnicalUser;
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
import org.bonitasoft.engine.test.BuildTestUtil;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

public class ClassLoaderIT extends TestWithTechnicalUser {

    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog();

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
                .containsOnlyOnce("Refreshing classloader with key: PROCESS:" + processDefinition.getId());
    }

    @Test
    @Ignore("We should have only one refresh when deploying bdm see BS-19229")
    public void should_refresh_classloader_only_once_on_deploy_bdm() throws Exception {
        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
        final byte[] zip = converter.zip(buildCustomBOM());
        getTenantAdministrationAPI().pause();
        systemOutRule.clearLog();
        getTenantAdministrationAPI().installBusinessDataModel(zip);
        String deployBDMLog = systemOutRule.getLog();
        getTenantAdministrationAPI().resume();

        assertThat(deployBDMLog).containsOnlyOnce("Refreshing classloader with key: TENANT:");
    }

    @Test
    @Ignore("We should remove this call to refresh classloader on delete")
    public void should_not_refresh_classloader_on_delete_process_definition() throws Exception {
        BusinessArchive businessArchive = createProcessWithDependencies();
        User user = getIdentityAPI().createUser("john", "bpm");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(businessArchive, ACTOR_NAME, user);

        systemOutRule.clearLog();
        getProcessAPI().disableAndDeleteProcessDefinition(processDefinition.getId());
        String processDeployLog = systemOutRule.getLog();

        assertThat(processDeployLog)
                .doesNotContain("Refreshing classloader with key: PROCESS:" + processDefinition.getId());
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
