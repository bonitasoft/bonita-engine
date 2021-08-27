/**
 * Copyright (C) 2020 Bonitasoft S.A.
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
package org.bonitasoft.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.operation.OperatorType.ASSIGNMENT;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.assertj.core.api.Assertions;
import org.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.SubProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.LeftOperand;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.test.APITestUtil;
import org.junit.AfterClass;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author Baptiste Mesta.
 */
public class StringIndexIT extends CommonAPIIT {

    @Test
    public void should_set_string_index_of_current_process_using_operation_in_called_process() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        User user = createUser("john", "bpm");
        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("mainProcess", "1.0");
        builder.addCallActivity("call", stringConstant("calledProcess"), stringConstant("1.0"));
        builder.setStringIndex(1, "index1", stringConstant("initial value"));
        builder.setStringIndex(2, "index2", stringConstant("initial value"));
        ProcessDefinition mainProcess = deployAndEnableProcess(builder.done());

        builder = new ProcessDefinitionBuilder().createNewInstance("calledProcess", "1.0");
        builder.addAutomaticTask("task1").addOperation(theStringIndex(1), ASSIGNMENT, "",
                stringConstant("value from called process"));
        builder.addUserTask("userTask", "john");
        builder.addTransition("task1", "userTask");
        builder.addActor("john");
        builder.setStringIndex(1, "index1", stringConstant("initial value"));
        builder.setStringIndex(2, "index2", stringConstant("initial value"));
        SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess("signalSubProcess", true)
                .getSubProcessBuilder();
        subProcessBuilder.addStartEvent("signalStart").addSignalEventTrigger("mySignal")
                .addAutomaticTask("subTask")
                .addOperation(theStringIndex(2), ASSIGNMENT, "", stringConstant("value from sub process"))
                .addUserTask("subUserTask", "john")
                .addTransition("subTask", "subUserTask")
                .addTransition("signalStart", "subTask");

        ProcessDefinition calledProcess = deployAndEnableProcessWithActor(builder.done(), "john", user);

        ProcessInstance processInstance = getProcessAPI().startProcess(mainProcess.getId());
        HumanTaskInstance userTask = waitForUserTaskAndGetIt("userTask");
        getProcessAPI().sendSignal("mySignal");
        waitForUserTaskAndGetIt("subUserTask");

        Thread.sleep(2000);
        Assertions.assertThat(getProcessAPI().getProcessInstance(processInstance.getId()).getStringIndex1())
                .isEqualTo("initial value");
        Assertions.assertThat(getProcessAPI().getProcessInstance(processInstance.getId()).getStringIndex2())
                .isEqualTo("initial value");
        Assertions
                .assertThat(getProcessAPI().getProcessInstance(userTask.getParentProcessInstanceId()).getStringIndex1())
                .isEqualTo("value from called process");
        Assertions
                .assertThat(getProcessAPI().getProcessInstance(userTask.getParentProcessInstanceId()).getStringIndex2())
                .isEqualTo("value from sub process");
        disableAndDeleteProcess(mainProcess, calledProcess);
        deleteUser(user);
    }

    private LeftOperand theStringIndex(int index) {
        return new LeftOperandBuilder().createSearchIndexLeftOperand(index);
    }

    @Test
    public void should_initialize_string_index_in_call_activity() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        User user = createUser("john", "bpm");
        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("mainProcess", "1.0");
        builder.addCallActivity("call", stringConstant("calledProcess"), stringConstant("1.0"));
        builder.setStringIndex(1, "index1", stringConstant("value from main process"));
        ProcessDefinition mainProcess = deployAndEnableProcess(builder.done());

        builder = new ProcessDefinitionBuilder().createNewInstance("calledProcess", "1.0");
        builder.addAutomaticTask("task1");
        builder.addUserTask("userTask", "john");
        builder.addTransition("task1", "userTask");
        builder.addActor("john");
        builder.setStringIndex(1, "index1", stringConstant("value from sub process"));
        ProcessDefinition calledProcess = deployAndEnableProcessWithActor(builder.done(), "john", user);

        ProcessInstance processInstance = getProcessAPI().startProcess(mainProcess.getId());
        HumanTaskInstance userTask = waitForUserTaskAndGetIt("userTask");

        Assertions.assertThat(getProcessAPI().getProcessInstance(processInstance.getId()).getStringIndex1())
                .isEqualTo("value from main process");
        Assertions
                .assertThat(getProcessAPI().getProcessInstance(userTask.getParentProcessInstanceId()).getStringIndex1())
                .isEqualTo("value from sub process");
        disableAndDeleteProcess(mainProcess, calledProcess);
        deleteUser(user);
    }

    private Expression stringConstant(String value) throws InvalidExpressionException {
        return new ExpressionBuilder().createConstantStringExpression(value);
    }

    @Test
    public void should_be_able_to_initialize_a_search_index_using_a_business_data() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        final String qualifiedName = "com.company.test.Bo";
        final BusinessObjectModel bom = buildSimpleBom(qualifiedName);
        installBusinessDataModel(bom);

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addActor(APITestUtil.ACTOR_NAME);
        final String bizDataName = "myBizData";

        final Expression defaultBizDataValue = new ExpressionBuilder().createGroovyScriptExpression("createNewBo",
                "new com.company.test.Bo(aField:'Julius')",
                qualifiedName);
        processDefinitionBuilder.addBusinessData(bizDataName, qualifiedName, defaultBizDataValue);

        Expression businessDataExpression = new ExpressionBuilder().createBusinessDataExpression(bizDataName,
                qualifiedName);
        Expression javaMethodCallExpression = new ExpressionBuilder().createJavaMethodCallExpression("call_java_method",
                "getAField", "java.lang.String",
                businessDataExpression);
        processDefinitionBuilder.setStringIndex(1, "param0", javaMethodCallExpression);

        processDefinitionBuilder.addUserTask("step1", APITestUtil.ACTOR_NAME);
        User testUser = createUser("john", "bpm");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(),
                APITestUtil.ACTOR_NAME, testUser);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, "step1");

        assertThat(processInstance.getStringIndex1())
                .as("String index 1 should be initialized with the business data value").isEqualTo("Julius");

        disableAndDeleteProcess(processDefinition);
        deleteUser(testUser);
    }

    private BusinessObjectModel buildSimpleBom(final String boQualifiedName)
            throws IOException, JAXBException, SAXException {
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName(boQualifiedName);
        final SimpleField field = new SimpleField();
        field.setName("aField");
        field.setType(FieldType.STRING);
        bo.addField(field);
        final BusinessObjectModel model = new BusinessObjectModel();
        model.addBusinessObject(bo);
        return model;
    }

    private void installBusinessDataModel(final BusinessObjectModel bom) throws Exception {
        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
        final byte[] zip = converter.zip(bom);
        getTenantAdministrationAPI().pause();
        getTenantAdministrationAPI().cleanAndUninstallBusinessDataModel();
        final String businessDataModelVersion = getTenantAdministrationAPI().installBusinessDataModel(zip);
        getTenantAdministrationAPI().resume();
        assertThat(businessDataModelVersion).as("should have deployed BDM").isNotNull();
    }

}
