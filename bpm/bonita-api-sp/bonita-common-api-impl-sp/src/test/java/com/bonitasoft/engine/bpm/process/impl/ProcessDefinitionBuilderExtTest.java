/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bpm.process.impl;

import java.util.List;

import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.junit.Test;

public class ProcessDefinitionBuilderExtTest {

    public static final String ACTOR_NAME = "Employee actor";

    private static final String EMPLOYEE_QUALIF_CLASSNAME = "org.bonita.pojo.Employee";

    @Test
    public void validMultiBusinessDataUsedInExpressions() throws InvalidExpressionException, InvalidProcessDefinitionException {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployees", "import " + EMPLOYEE_QUALIF_CLASSNAME
                + "; Employee john = new Employee(); john.firstName = 'John'; john.lastName = 'Doe';"
                + " Employee jane = new Employee(); jane.firstName = 'Jane'; jane.lastName = 'Doe'; return [jane, john];", List.class.getName());

        final Expression jackExpression = new ExpressionBuilder().createGroovyScriptExpression("createJack", "import " + EMPLOYEE_QUALIF_CLASSNAME
                + "; Employee jack = new Employee(); jack.firstName = 'Jack'; jack.lastName = 'Doe'; return jack;", EMPLOYEE_QUALIF_CLASSNAME);

        final ProcessDefinitionBuilderExt builder = new ProcessDefinitionBuilderExt().createNewInstance("test", "1.2-alpha");
        builder.addBusinessData("myEmployees", EMPLOYEE_QUALIF_CLASSNAME, employeeExpression).setMultiple(true);
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step1", ACTOR_NAME)
                .addOperation(new OperationBuilder().createBusinessDataSetAttributeOperation("myEmployees", "add", Object.class.getName(), jackExpression));
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addTransition("step1", "step2");
        builder.done();
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    public void invalidProcessDueToAMismatchConfigurationBetweenMultiBusinessDataAndInitExpression() throws InvalidExpressionException,
            InvalidProcessDefinitionException {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("create", "jane", String.class.getName());

        final ProcessDefinitionBuilderExt builder = new ProcessDefinitionBuilderExt().createNewInstance("test", "0.0.1");
        builder.addBusinessData("myEmployees", EMPLOYEE_QUALIF_CLASSNAME, employeeExpression).setMultiple(true);
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step1", ACTOR_NAME);
        builder.done();
    }

    @Test
    public void validMultiBusinessDataUsedWithoutADefaultExpression() throws InvalidProcessDefinitionException {
        final ProcessDefinitionBuilderExt builder = new ProcessDefinitionBuilderExt().createNewInstance("test", "0.0.1");
        builder.addBusinessData("myEmployees", EMPLOYEE_QUALIF_CLASSNAME, null).setMultiple(true);
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step1", ACTOR_NAME);
        builder.done();
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    public void invalidProcessDueToACallActivityMappingUsingASimpleBusinessData() throws Exception {
        final ProcessDefinitionBuilderExt builder = new ProcessDefinitionBuilderExt().createNewInstance("MBIMI", "1.2-beta");
        builder.addBusinessData("myEmployee", EMPLOYEE_QUALIF_CLASSNAME, null);
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step", ACTOR_NAME).addMultiInstance(true, "myEmployee").addDataInputItemRef("employee");
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addTransition("step1", "step2");
        builder.done();
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    public void invalidProcessBecauseOfTheReferenceToTheLoopDataInputItemDoesNotReferToADataOfTheActivity() throws Exception {
        final ProcessDefinitionBuilderExt builder = new ProcessDefinitionBuilderExt().createNewInstance("MBIMI", "1.2-beta");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step", ACTOR_NAME).addMultiInstance(true, "myEmployee").addDataInputItemRef("employee");
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addTransition("step1", "step2");
        builder.done();
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    public void invalidProcessBecauseOfTheReferenceToTheDataOutputItemDoesNotReferToADataOfTheActivity() throws Exception {
        final ProcessDefinitionBuilderExt builder = new ProcessDefinitionBuilderExt().createNewInstance("test", "1.0");
        builder.addBusinessData("myEmployees", EMPLOYEE_QUALIF_CLASSNAME, null).setMultiple(true);
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step1", ACTOR_NAME)
        .addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(2)).addDataOutputItemRef("newEmployee")
        .addLoopDataOutputRef("myEmployees");
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addTransition("step1", "step2");
        builder.done();
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    public void invalidProcessBecauseOfTheReferenceToTheLoopDataOutputDoesNotReferToADataOfTheProcess() throws Exception {
        final ProcessDefinitionBuilderExt builder = new ProcessDefinitionBuilderExt().createNewInstance("test", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step1", ACTOR_NAME)
        .addBusinessData("newEmployee", EMPLOYEE_QUALIF_CLASSNAME)
        .addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(2)).addDataOutputItemRef("newEmployee")
        .addLoopDataOutputRef("myEmployees");
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addTransition("step1", "step2");
        builder.done();
    }

    @Test
    public void validProcessWhenAllReferencesAreWellSet() throws Exception {
        final ProcessDefinitionBuilderExt builder = new ProcessDefinitionBuilderExt().createNewInstance("test", "1.0");
        builder.addBusinessData("myEmployees", EMPLOYEE_QUALIF_CLASSNAME, null).setMultiple(true);
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step1", ACTOR_NAME)
        .addBusinessData("newEmployee", EMPLOYEE_QUALIF_CLASSNAME)
        .addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(2)).addDataOutputItemRef("newEmployee")
        .addLoopDataOutputRef("myEmployees");
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addTransition("step1", "step2");
        builder.done();
    }

    @Test
    public void validProcessUsingAMultiInstanceActivityWithRefToActivityAndProcessVariables() throws Exception {
        final ProcessDefinitionBuilderExt builder = new ProcessDefinitionBuilderExt().createNewInstance("test", "1.0");
        builder.addData("loopDataInput", List.class.getName(), null);
        builder.addData("loopDataOutputRef", List.class.getName(), null);
        final UserTaskDefinitionBuilder userTaskBuilder = builder.addUserTask("task1", ACTOR_NAME);
        userTaskBuilder.addShortTextData("dataInputItemRef", null);
        userTaskBuilder.addShortTextData("dataOutputItemRef", null);
        userTaskBuilder.addMultiInstance(false, "loopDataInput").addDataInputItemRef("dataInputItemRef").addDataOutputItemRef("dataOutputItemRef")
        .addLoopDataOutputRef("loopDataOutputRef");
        builder.done();
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    public void invalidProcessBecauseOfTheActivityContainsABusinessDataButNotTheMultiInstanceBehaviour() throws Exception {
        final ProcessDefinitionBuilderExt builder = new ProcessDefinitionBuilderExt().createNewInstance("MBIMI", "1.2-beta");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step", ACTOR_NAME).addBusinessData("employee", EMPLOYEE_QUALIF_CLASSNAME);
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addTransition("step1", "step2");
        builder.done();
    }

}
