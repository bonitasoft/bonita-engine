package com.bonitasoft.engine.bpm.process.impl;

import java.util.List;

import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
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

}
