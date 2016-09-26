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
 */
package org.bonitasoft.engine.bpm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.operation.model.SOperatorType;
import org.bonitasoft.engine.core.operation.model.builder.SLeftOperandBuilderFactory;
import org.bonitasoft.engine.core.operation.model.builder.SOperationBuilderFactory;
import org.bonitasoft.engine.data.ParentContainerResolverImpl;
import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilder;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilderFactory;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilder;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilderFactory;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilder;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilderFactory;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Zhang Bole
 */
public class OperationServiceIntegrationTest extends CommonBPMServicesTest {

    private UserTransactionService transactionService;

    private OperationService operationService;

    private DataInstanceService dataInstanceService;
    private ParentContainerResolverImpl parentContainerResolver;

    @Before
    public void setup() {
        transactionService = getTransactionService();
        dataInstanceService = getTenantAccessor().getDataInstanceService();
        operationService = getTenantAccessor().getOperationService();
        parentContainerResolver = (ParentContainerResolverImpl) getTenantAccessor().getParentContainerResolver();
        parentContainerResolver.setAllowUnknownContainer(true);
    }

    @After
    public void after() throws Exception {
        parentContainerResolver.setAllowUnknownContainer(false);
    }

    /**
     * Assign a new value to a String Variable. Using an expression which is a constant.
     * variableName = "afterUpdate"
     *
     * @throws Exception
     */
    @Test
    public void executeOperationUsingStringConstantExpression() throws Exception {
        final String dataInstanceName = "variableName";
        final long containerId = 11L;
        final String containerType = "miniTask";
        final String defaultValue = "beforeUpdate";
        final String newConstantValue = "afterUpdate";
        createStringDataInstance(dataInstanceName, containerId, containerType, defaultValue, defaultValue);
        SDataInstance dataInstance = getDataInstance(dataInstanceName, containerId, containerType);
        assertEquals(defaultValue, dataInstance.getValue());

        final SOperation operation;
        final Map<String, Serializable> expressionContexts = new HashMap<>();
        expressionContexts.put("containerId", containerId);
        expressionContexts.put("containerType", containerType);
        operation = buildAssignmentOperation(dataInstanceName, newConstantValue);
        executeOperation(operation, containerId, containerType, expressionContexts);

        dataInstance = getDataInstance(dataInstanceName, containerId, containerType);
        assertEquals(newConstantValue, dataInstance.getValue());
        deleteDataInstance(dataInstance);
    }

    /**
     * Assign a new value to a List Variable. Using an expression which is a constant.
     * variableName.add("afterUpdate");
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void executeOperationUsingJavaMethodConstantExpression() throws Exception {
        final String dataInstanceName = "variableName";
        final long containerId = 12L;
        final String containerType = "miniTask";
        final String defaultValueExpressionContent = "new ArrayList<String>();";
        final String newConstantValue = "stringAddedIntoTheList";
        createListDataInstance(dataInstanceName, containerId, containerType, defaultValueExpressionContent, new ArrayList<String>());
        SDataInstance dataInstance = getDataInstance(dataInstanceName, containerId, containerType);
        assertTrue(dataInstance.getValue() instanceof ArrayList<?>);

        final SOperation operation;
        final Map<String, Serializable> expressionContexts = new HashMap<>();
        expressionContexts.put("containerId", containerId);
        expressionContexts.put("containerType", containerType);
        operation = buildJavaMethodOperation(dataInstanceName, newConstantValue);
        executeOperation(operation, containerId, containerType, expressionContexts);

        dataInstance = getDataInstance(dataInstanceName, containerId, containerType);
        assertTrue(dataInstance.getValue() instanceof ArrayList<?>);
        final List<String> list = (ArrayList<String>) dataInstance.getValue();
        assertTrue(list.contains(newConstantValue));

        // clean
        deleteDataInstance(dataInstance);
    }

    private void createListDataInstance(final String dataInstanceName, final long containerId, final String containerType,
            final String defaultValueExpressionConstant, final Serializable defaultValue) throws Exception {
        final String description = null;
        final SDataInstance dataInstance = buildDataInstance(dataInstanceName, ArrayList.class.getName(), description, defaultValueExpressionConstant,
                containerId, containerType, false, SExpression.TYPE_READ_ONLY_SCRIPT, SExpression.GROOVY, defaultValue);
        insertDataInstance(dataInstance);
    }

    private SOperation buildJavaMethodOperation(final String dataInstanceName, final String newConstantValue) throws SInvalidExpressionException {
        final SLeftOperand leftOperand = BuilderFactory.get(SLeftOperandBuilderFactory.class).createNewInstance().setName(dataInstanceName).done();
        final SExpression expression = BuilderFactory.get(SExpressionBuilderFactory.class).createNewInstance().setContent(newConstantValue)
                .setExpressionType(ExpressionType.TYPE_CONSTANT.name()).setReturnType(String.class.getName()).done();
        return BuilderFactory.get(SOperationBuilderFactory.class).createNewInstance().setOperator("add:" + Object.class.getName()).setLeftOperand(leftOperand)
                .setType(SOperatorType.JAVA_METHOD).setRightOperand(expression).done();
    }

    private SOperation buildAssignmentOperation(final String dataInstanceName, final String newConstantValue) throws SInvalidExpressionException {
        final SLeftOperand leftOperand = BuilderFactory.get(SLeftOperandBuilderFactory.class).createNewInstance().setName(dataInstanceName).done();
        final SExpression expression = BuilderFactory.get(SExpressionBuilderFactory.class).createNewInstance().setContent(newConstantValue)
                .setReturnType(String.class.getName()).setExpressionType(ExpressionType.TYPE_CONSTANT.name()).setReturnType(String.class.getName()).done();
        return BuilderFactory.get(SOperationBuilderFactory.class).createNewInstance().setOperator("=").setLeftOperand(leftOperand)
                .setType(SOperatorType.ASSIGNMENT)
                .setRightOperand(expression).done();
    }

    private void executeOperation(final SOperation operation, final long containerId, final String containerType,
            final Map<String, Serializable> expressionContexts) throws Exception {
        transactionService.executeInTransaction(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                final SExpressionContext sExpressionContext = new SExpressionContext();
                sExpressionContext.setSerializableInputValues(expressionContexts);
                sExpressionContext.setContainerId(containerId);
                sExpressionContext.setContainerType(containerType);
                operationService.execute(operation, containerId, containerType, sExpressionContext);
                return null;
            }
        });
    }

    private SDataInstance getDataInstance(final String dataInstanceName, final long containerId, final String containerType) throws Exception {
        return transactionService.executeInTransaction(new Callable<SDataInstance>() {

            @Override
            public SDataInstance call() throws Exception {
                return dataInstanceService.getDataInstance(dataInstanceName, containerId, containerType, parentContainerResolver);
            }
        });
    }

    private void createStringDataInstance(final String instanceName, final long containerId, final String containerType,
            final String defaultValueExpressionContent, final Serializable currentDataInstanceValue) throws Exception {
        final SDataInstance dataInstance = buildDataInstance(instanceName, String.class.getName(), "testUpdate", defaultValueExpressionContent, containerId,
                containerType, false, SExpression.TYPE_CONSTANT, null, currentDataInstanceValue);
        insertDataInstance(dataInstance);
    }

    private SDataInstance buildDataInstance(final String instanceName, final String className, final String description,
            final String defaultValueExpressionContent, final long containerId, final String containerType, final boolean isTransient,
            final String expressionType, final String expressionInterpreter, final Serializable currentDataInstanceValue) throws SBonitaException {
        // create definition
        final SDataDefinitionBuilder dataDefinitionBuilder = BuilderFactory.get(SDataDefinitionBuilderFactory.class).createNewInstance(instanceName, className);
        initializeBuilder(dataDefinitionBuilder, description, defaultValueExpressionContent, className, isTransient, expressionType, expressionInterpreter);
        final SDataDefinition dataDefinition = dataDefinitionBuilder.done();
        // create data instance
        final SDataInstanceBuilder dataInstanceBuilder = BuilderFactory.get(SDataInstanceBuilderFactory.class).createNewInstance(dataDefinition);
        return dataInstanceBuilder.setContainerId(containerId).setContainerType(containerType).setValue(currentDataInstanceValue).done();
    }

    private void insertDataInstance(final SDataInstance dataInstance) throws Exception {
        transactionService.executeInTransaction(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                dataInstanceService.createDataInstance(dataInstance);
                return null;
            }
        });
    }

    private void initializeBuilder(final SDataDefinitionBuilder dataDefinitionBuilder, final String description, final String defaultValueExpressionContent,
            final String defaultValueExprReturnType, final boolean isTransient, final String expressionType, final String interpreter)
            throws SInvalidExpressionException {
        SExpression expression = null;
        if (defaultValueExpressionContent != null) {
            // create expression
            final SExpressionBuilder expreBuilder = BuilderFactory.get(SExpressionBuilderFactory.class).createNewInstance();
            // this discrimination'll be changed.
            expreBuilder.setContent(defaultValueExpressionContent).setExpressionType(expressionType).setReturnType(defaultValueExprReturnType);
            if (interpreter != null) {
                expreBuilder.setInterpreter(interpreter);
            }
            expression = expreBuilder.done();
        }
        dataDefinitionBuilder.setDescription(description);
        dataDefinitionBuilder.setTransient(isTransient);
        dataDefinitionBuilder.setDefaultValue(expression);
    }

    private void deleteDataInstance(final SDataInstance dataInstance) throws Exception {
        transactionService.executeInTransaction(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                dataInstanceService.deleteDataInstance(dataInstance);
                return null;
            }
        });
    }

}
