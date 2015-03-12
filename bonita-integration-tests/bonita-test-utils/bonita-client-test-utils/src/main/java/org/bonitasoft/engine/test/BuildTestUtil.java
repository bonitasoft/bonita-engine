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
package org.bonitasoft.engine.test;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.impl.BoundaryEventDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ConnectorDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.DocumentBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.SubProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.operation.LeftOperand;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;

public class BuildTestUtil {

    public static final String SUPERVISOR_ID_KEY = "supervisorId";

    public static final String ROLE_ID_KEY = "roleId";

    public static final String GROUP_ID_KEY = "groupId";

    public static final String PROCESS_DEFINITION_ID_KEY = "processDefinitionId";

    public static final String DEFAULT_TENANT = "default";

    public static final String ACTOR_NAME = "Employee actor";

    public static final String PROCESS_VERSION = "1.0";

    public static final String PROCESS_NAME = "ProcessName";

    public static final String EVENT_SUB_PROCESS_NAME = "eventSubProcess";

    public static final String DESCRIPTION = "Coding all-night-long";

    public static final String PASSWORD = "bpm";

    public static final String USERNAME = "william.jobs";

    public static final String GROUP_NAME = "R&D";

    public static final String ROLE_NAME = "Developper";

    public static byte[] buildConnectorImplementationFile(final String definitionId, final String definitionVersion, final String implementationId,
            final String implementationVersion, final String implementationClassname) {
        final StringBuilder stb = new StringBuilder();
        stb.append("<connectorImplementation>");
        stb.append("");
        stb.append("<definitionId>" + definitionId + "</definitionId>");
        stb.append("<definitionVersion>" + definitionVersion + "</definitionVersion>");
        stb.append("<implementationClassname>" + implementationClassname + "</implementationClassname>");
        stb.append("<implementationId>" + implementationId + "</implementationId>");
        stb.append("<implementationVersion>" + implementationVersion + "</implementationVersion>");
        stb.append("</connectorImplementation>");
        return stb.toString().getBytes();
    }

    public static BusinessArchiveBuilder buildBusinessArchiveWithConnectorAndUserFilter(final ProcessDefinitionBuilder processDefinitionBuilder,
            final List<BarResource> connectorImplementations, final List<BarResource> generateConnectorDependencies, final List<BarResource> userFilters)
            throws InvalidProcessDefinitionException {
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                processDefinitionBuilder.done());
        for (final BarResource barResource : connectorImplementations) {
            businessArchiveBuilder.addConnectorImplementation(barResource);
        }

        for (final BarResource barResource : generateConnectorDependencies) {
            businessArchiveBuilder.addClasspathResource(barResource);
        }

        for (final BarResource barResource : userFilters) {
            businessArchiveBuilder.addUserFilters(barResource);
        }
        return businessArchiveBuilder;
    }

    public static ProcessDefinitionBuilder buildProcessDefinitionWithCallActivity(final String processName, final Expression targetProcessExpr,
            final Expression targetVersionExpr) {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance(processName, PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addStartEvent("start");
        builder.addCallActivity("callActivity", targetProcessExpr, targetVersionExpr);
        builder.addEndEvent("end").addTerminateEventTrigger();
        builder.addTransition("start", "callActivity").addTransition("callActivity", "end");
        return builder;
    }

    public static ProcessDefinitionBuilder buildProcessDefinitionWithHumanTaskAndErrorEndEvent(final String processName, final String taskName) {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance(processName, PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addStartEvent("start");
        builder.addUserTask(taskName, ACTOR_NAME);
        builder.addEndEvent("end").addErrorEventTrigger("errorCode");
        builder.addTransition("start", taskName).addTransition(taskName, "end");
        return builder;
    }

    public static ProcessDefinitionBuilder buildProcessDefinitionWithAutomaticTaskAndErrorEndEvent(final String processName) {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance(processName, PROCESS_VERSION);
        builder.addStartEvent("start");
        builder.addAutomaticTask("step");
        builder.addEndEvent("end").addErrorEventTrigger("errorCode");
        builder.addTransition("start", "step").addTransition("step", "end");
        return builder;
    }

    public static ProcessDefinitionBuilder buildProcessDefinitionWithAutomaticTaskAndFailedGroovyScript(final String processName)
            throws InvalidExpressionException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance(processName, PROCESS_VERSION);
        builder.addStartEvent("start");
        builder.addAutomaticTask("activityThatFail").addData("data", String.class.getName(),
                new ExpressionBuilder().createGroovyScriptExpression("script", "throw new java.lang.RuntimeException()", String.class.getName()));
        builder.addEndEvent("end");
        builder.addTransition("start", "activityThatFail").addTransition("activityThatFail", "end");
        return builder;
    }

    public static ProcessDefinitionBuilder buildProcessDefinitionWithFailedConnector(final String processName) throws InvalidExpressionException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance(processName, PROCESS_VERSION);
        final ConnectorDefinitionBuilder connectorDefinitionBuilder = builder.addConnector("testConnectorThatThrowException",
                "testConnectorThatThrowException", "1.0", ConnectorEvent.ON_ENTER).throwErrorEventWhenFailed("errorCode");
        connectorDefinitionBuilder.addInput("kind", new ExpressionBuilder().createConstantStringExpression("plop"));
        builder.addStartEvent("start").addAutomaticTask("AutomaticStep").addEndEvent("end");
        builder.addTransition("start", "AutomaticStep").addTransition("AutomaticStep", "end");
        return builder;
    }

    public static ProcessDefinitionBuilder buildProcessDefinitionWithAutomaticTaskAndFailedConnector(final String processName)
            throws InvalidExpressionException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance(processName, PROCESS_VERSION);
        builder.addStartEvent("start");
        final ConnectorDefinitionBuilder connectorDefinitionBuilder = builder.addAutomaticTask("AutomaticStep").addConnector("testConnectorThatThrowException",
                "testConnectorThatThrowException", "1.0", ConnectorEvent.ON_ENTER).throwErrorEventWhenFailed("errorCode");
        connectorDefinitionBuilder.addInput("kind", new ExpressionBuilder().createConstantStringExpression("plop"));
        builder.addEndEvent("end");
        builder.addTransition("start", "AutomaticStep").addTransition("AutomaticStep", "end");
        return builder;
    }

    public static ProcessDefinitionBuilder buildProcessDefinitionWithUserTaskAndFailedConnector(final String processName)
            throws InvalidExpressionException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance(processName, PROCESS_VERSION);
        builder.addStartEvent("start");
        builder.addActor(ACTOR_NAME).addUserTask("StepBeforeFailedConnector", ACTOR_NAME);
        final ConnectorDefinitionBuilder connectorDefinitionBuilder = builder.addAutomaticTask("AutomaticStep")
                .addConnector("testConnectorThatThrowException",
                        "testConnectorThatThrowException", "1.0", ConnectorEvent.ON_ENTER).throwErrorEventWhenFailed("errorCode");
        connectorDefinitionBuilder.addInput("kind", new ExpressionBuilder().createConstantStringExpression("plop"));
        builder.addEndEvent("end");
        builder.addTransition("start", "StepBeforeFailedConnector").addTransition("StepBeforeFailedConnector", "AutomaticStep")
                .addTransition("AutomaticStep", "end");
        return builder;
    }

    public static ProcessDefinitionBuilder buildProcessDefinitionWithMultiInstanceUserTaskAndFailedConnector(final String processName, final String userTaskName)
            throws InvalidExpressionException {
        final String errorCode = "mistake";

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance(processName, PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        final UserTaskDefinitionBuilder userTaskBuilder = builder.addUserTask(userTaskName, ACTOR_NAME);
        userTaskBuilder.addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(3));
        final ConnectorDefinitionBuilder connectorDefinitionBuilder = userTaskBuilder.addConnector("testConnectorThatThrowException",
                "testConnectorThatThrowException", "1.0", ConnectorEvent.ON_FINISH).throwErrorEventWhenFailed(errorCode);
        connectorDefinitionBuilder.addInput("kind", new ExpressionBuilder().createConstantStringExpression("plop"));
        final BoundaryEventDefinitionBuilder boundaryEvent = userTaskBuilder.addBoundaryEvent("error", true);
        boundaryEvent.addErrorEventTrigger(errorCode);
        builder.addUserTask("normalFlow", ACTOR_NAME);
        builder.addUserTask("errorFlow", ACTOR_NAME);
        builder.addTransition(userTaskName, "normalFlow").addTransition("error", "errorFlow");
        return builder;
    }

    public static void buildErrorEventSubProcessWithUserTask(final String taskName, final ProcessDefinitionBuilder builder) {
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess(EVENT_SUB_PROCESS_NAME, true).getSubProcessBuilder();
        subProcessBuilder.addStartEvent("SubStart").addErrorEventTrigger();
        subProcessBuilder.addUserTask(taskName, ACTOR_NAME);
        subProcessBuilder.addEndEvent("SubEnd");
        subProcessBuilder.addTransition("SubStart", taskName).addTransition(taskName, "SubEnd");
    }

    public static Document buildDocument(final String documentName) {
        final String now = String.valueOf(System.currentTimeMillis());
        final String fileName = now + ".txt";
        final DocumentBuilder builder = new DocumentBuilder().createNewInstance(documentName, false);
        builder.setFileName(fileName);
        builder.setContentMimeType("plain/text");
        builder.setDescription("a generated document for tests");
        return builder.done();
    }

    public static SearchOptionsBuilder buildSearchOptions(final long processDefId, final int pageIndex, final int numberOfResults, final String orderByField,
            final Order order) {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(pageIndex, numberOfResults);
        builder.filter(ProcessInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDefId);
        builder.sort(orderByField, order);
        return builder;
    }

    public static SearchOptionsBuilder buildSearchOptions(final int pageIndex, final int numberOfResults, final String orderByField, final Order order) {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(pageIndex, numberOfResults);
        builder.sort(orderByField, order);
        return builder;
    }

    public static Operation buildStringOperation(final String dataInstanceName, final String newConstantValue, final boolean isTransient)
            throws InvalidExpressionException {
        final LeftOperand leftOperand = new LeftOperandBuilder().createNewInstance().setName(dataInstanceName)
                .setType(isTransient ? LeftOperand.TYPE_TRANSIENT_DATA : LeftOperand.TYPE_DATA).done();
        final Expression expression = new ExpressionBuilder().createConstantStringExpression(newConstantValue);
        final Operation operation;
        operation = new OperationBuilder().createNewInstance().setOperator("=").setLeftOperand(leftOperand).setType(OperatorType.ASSIGNMENT)
                .setRightOperand(expression).done();
        return operation;
    }

    public static Operation buildIntegerOperation(final String dataInstanceName, final int newConstantValue) throws InvalidExpressionException {
        final LeftOperand leftOperand = new LeftOperandBuilder().createNewInstance().setName(dataInstanceName).done();
        final Expression expression = new ExpressionBuilder().createConstantIntegerExpression(newConstantValue);
        final Operation operation;
        operation = new OperationBuilder().createNewInstance().setOperator("=").setLeftOperand(leftOperand).setType(OperatorType.ASSIGNMENT)
                .setRightOperand(expression).done();
        return operation;
    }

    public static Operation buildOperation(final String dataName, final boolean isTransient, final OperatorType operatorType, final String operator,
            final Expression rightOperand) {
        final OperationBuilder operationBuilder = new OperationBuilder().createNewInstance();
        operationBuilder.setOperator(operator);
        operationBuilder.setRightOperand(rightOperand);
        operationBuilder.setType(operatorType);
        operationBuilder.setLeftOperand(new LeftOperandBuilder().createNewInstance(dataName)
                .setType(isTransient ? LeftOperand.TYPE_TRANSIENT_DATA : LeftOperand.TYPE_DATA).done());
        return operationBuilder.done();
    }

    public static Operation buildAssignOperation(final String dataInstanceName, final String expressionContent, final ExpressionType expressionType,
            final String returnType) throws InvalidExpressionException {
        final LeftOperand leftOperand = new LeftOperandBuilder().createNewInstance().setName(dataInstanceName).done();
        final Expression expression = new ExpressionBuilder().createNewInstance(dataInstanceName).setContent(expressionContent)
                .setExpressionType(expressionType).setReturnType(returnType).done();
        return new OperationBuilder().createNewInstance().setOperator("=").setLeftOperand(leftOperand).setType(OperatorType.ASSIGNMENT)
                .setRightOperand(expression).done();
    }

    public static List<String> buildActorAndDescription(final ProcessDefinitionBuilder processBuilder, final int nbActor) {
        final List<String> actorList = new ArrayList<String>();
        for (int i = 1; i <= nbActor; i++) {
            final String actorName = ACTOR_NAME + i;
            processBuilder.addActor(actorName).addDescription(DESCRIPTION + i);
            actorList.add(actorName);
        }
        return actorList;
    }

    public static DesignProcessDefinition buildProcessDefinitionWithActorAndThreeHumanStepsAndThreeTransition() throws InvalidProcessDefinitionException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(BuildTestUtil.ACTOR_NAME).addDescription(DESCRIPTION);

        // 1 instance of process def:
        return processBuilder.addAutomaticTask("step1").addUserTask("step2", BuildTestUtil.ACTOR_NAME).addUserTask("step3", BuildTestUtil.ACTOR_NAME)
                .addUserTask("step4", BuildTestUtil.ACTOR_NAME).addTransition("step1", "step2").addTransition("step1", "step3").addTransition("step1", "step4")
                .getProcess();
    }

    public static List<DesignProcessDefinition> buildNbProcessDefinitionWithHumanAndAutomatic(final int nbProcess, final List<String> stepNames,
            final List<Boolean> isHuman) throws InvalidProcessDefinitionException {
        final List<DesignProcessDefinition> processDefinitions = new ArrayList<DesignProcessDefinition>();
        for (int i = 0; i < nbProcess; i++) {
            String processName = PROCESS_NAME;
            if (i >= 0 && i < 10) {
                processName += "0";
            }
            final DesignProcessDefinition designProcessDefinition = buildProcessDefinitionWithHumanAndAutomaticSteps(processName + i, PROCESS_VERSION + i,
                    stepNames, isHuman);
            processDefinitions.add(designProcessDefinition);
        }
        return processDefinitions;
    }

    public static DesignProcessDefinition buildProcessDefinitionWithHumanAndAutomaticSteps(final String processName, final String processVersion,
            final List<String> stepNames, final List<Boolean> isHuman, final String actorName, final boolean addActorInitiator)
            throws InvalidProcessDefinitionException {
        return buildProcessDefinitionWithHumanAndAutomaticSteps(processName, processVersion, stepNames, isHuman, actorName, addActorInitiator, false);
    }

    public static DesignProcessDefinition buildProcessDefinitionWithHumanAndAutomaticSteps(final String processName, final String processVersion,
            final List<String> stepNames, final List<Boolean> isHuman) throws InvalidProcessDefinitionException {
        return buildProcessDefinitionWithHumanAndAutomaticSteps(processName, processVersion, stepNames, isHuman, ACTOR_NAME, false);
    }

    public static DesignProcessDefinition buildProcessDefinitionWithHumanAndAutomaticSteps(final List<String> stepNames, final List<Boolean> isHuman)
            throws InvalidProcessDefinitionException {
        return buildProcessDefinitionWithHumanAndAutomaticSteps(PROCESS_NAME, PROCESS_VERSION, stepNames, isHuman, ACTOR_NAME, false);
    }

    public static DesignProcessDefinition buildProcessDefinitionWithHumanAndAutomaticSteps(final String processName, final String processVersion,
            final List<String> stepNames, final List<Boolean> isHuman, final String actorName, final boolean addActorInitiator, final boolean parallelActivities)
            throws InvalidProcessDefinitionException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(processName, processVersion);
        if (!isHuman.isEmpty() && isHuman.contains(true)) {
            processBuilder.addActor(actorName);
            if (addActorInitiator) {
                processBuilder.setActorInitiator(actorName);
            }
        }
        for (int i = 0; i < stepNames.size(); i++) {
            final String stepName = stepNames.get(i);
            if (isHuman.get(i)) {
                processBuilder.addUserTask(stepName, actorName);
            } else {
                processBuilder.addAutomaticTask(stepName);
            }
        }
        if (!parallelActivities) {
            for (int i = 0; i < stepNames.size() - 1; i++) {
                processBuilder.addTransition(stepNames.get(i), stepNames.get(i + 1));
            }
        }
        return processBuilder.done();
    }

    public static byte[] generateContent(final Document doc) {
        return doc.getName().getBytes();
    }

    public static BarResource generateJarAndBuildBarResource(final Class<?> clazz, final String name) throws IOException {
        final byte[] data = IOUtil.generateJar(clazz);
        return new BarResource(name, data);
    }

    public static BarResource getContentAndBuildBarResource(final String name, final Class<? extends Connector> clazz) throws IOException {
        final InputStream stream = clazz.getResourceAsStream(name);
        assertNotNull(stream);
        final byte[] byteArray = IOUtils.toByteArray(stream);
        stream.close();
        return new BarResource(name, byteArray);
    }

}
