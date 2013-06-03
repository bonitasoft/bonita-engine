package org.bonitasoft.engine.bpm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.operation.model.SOperatorType;
import org.bonitasoft.engine.core.operation.model.builder.SOperationBuilders;
import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilder;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilders;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilder;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilders;
import org.bonitasoft.engine.events.model.FireEventException;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilder;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilders;
import org.bonitasoft.engine.operation.IncorrectOperationStructureException;
import org.bonitasoft.engine.operation.OperationExecutionException;
import org.bonitasoft.engine.transaction.SBadTransactionStateException;
import org.bonitasoft.engine.transaction.STransactionCommitException;
import org.bonitasoft.engine.transaction.STransactionCreationException;
import org.bonitasoft.engine.transaction.STransactionRollbackException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Test;

/**
 * @author Zhang Bole
 */
public class OperationServiceIntegrationTest extends CommonBPMServicesTest {

    private static BPMServicesBuilder servicesBuilder;

    private static TransactionService transactionService;

    private static OperationService operationService;

    private static DataInstanceService dataInstanceService;

    private static SDataDefinitionBuilders dataDefinitionBuilders;

    private static SDataInstanceBuilders dataInstanceBuilders;

    private static SExpressionBuilders sExpressionBuilders;

    private static SOperationBuilders sOperationBuilders;

    public OperationServiceIntegrationTest() {
        servicesBuilder = getServicesBuilder();
        transactionService = servicesBuilder.getTransactionService();
        dataInstanceService = servicesBuilder.getDataInstanceService();
        dataInstanceBuilders = servicesBuilder.getSDataInstanceBuilders();
        dataDefinitionBuilders = servicesBuilder.getSDataDefinitionBuilders();
        sExpressionBuilders = servicesBuilder.getInstanceOf(SExpressionBuilders.class);
        sOperationBuilders = servicesBuilder.getSOperationBuilders();
        operationService = servicesBuilder.getOperationService();
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
        final Map<String, Serializable> expressionContexts = new HashMap<String, Serializable>();
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
        final Map<String, Serializable> expressionContexts = new HashMap<String, Serializable>();
        expressionContexts.put("containerId", containerId);
        expressionContexts.put("containerType", containerType);
        operation = buildJavaMethodOperation(dataInstanceName, newConstantValue);
        executeOperation(operation, containerId, containerType, expressionContexts);

        dataInstance = getDataInstance(dataInstanceName, containerId, containerType);
        assertTrue(dataInstance.getValue() instanceof ArrayList<?>);
        final ArrayList<String> list = (ArrayList<String>) dataInstance.getValue();
        assertTrue(list.contains(newConstantValue));

        // clean
        deleteDataInstance(dataInstance);
    }

    private void createListDataInstance(final String dataInstanceName, final long containerId, final String containerType,
            final String defaultValueExpressionConstant, final Serializable defaultValue) throws SBonitaException, SDataInstanceException {
        final String description = null;
        final SDataInstance dataInstance = buildDataInstance(dataInstanceName, ArrayList.class.getName(), description, defaultValueExpressionConstant,
                containerId, containerType, false, SExpression.TYPE_READ_ONLY_SCRIPT, SExpression.GROOVY, defaultValue);
        insertDataInstance(dataInstance);
    }

    private SOperation buildJavaMethodOperation(final String dataInstanceName, final String newConstantValue) throws SInvalidExpressionException {
        final SLeftOperand leftOperand = sOperationBuilders.getSLeftOperandBuilder().createNewInstance().setName(dataInstanceName).done();
        final SExpression expression = sExpressionBuilders.getExpressionBuilder().createNewInstance().setContent(newConstantValue)
                .setExpressionType(ExpressionType.TYPE_CONSTANT.name()).setReturnType(String.class.getName()).done();
        final SOperation operation;
        operation = sOperationBuilders.getSOperationBuilder().createNewInstance().setOperator("add:" + Object.class.getName()).setLeftOperand(leftOperand)
                .setType(SOperatorType.JAVA_METHOD).setRightOperand(expression).done();
        return operation;
    }

    private SOperation buildAssignmentOperation(final String dataInstanceName, final String newConstantValue) throws SInvalidExpressionException {
        final SLeftOperand leftOperand = sOperationBuilders.getSLeftOperandBuilder().createNewInstance().setName(dataInstanceName).done();
        final SExpression expression = sExpressionBuilders.getExpressionBuilder().createNewInstance().setContent(newConstantValue)
                .setReturnType(String.class.getName()).setExpressionType(ExpressionType.TYPE_CONSTANT.name()).setReturnType(String.class.getName()).done();
        final SOperation operation;
        operation = sOperationBuilders.getSOperationBuilder().createNewInstance().setOperator("=").setLeftOperand(leftOperand)
                .setType(SOperatorType.ASSIGNMENT).setRightOperand(expression).done();
        return operation;
    }

    private void executeOperation(final SOperation operation, final long containerId, final String containerType,
            final Map<String, Serializable> expressionContexts) throws IncorrectOperationStructureException, OperationExecutionException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException,
            SBonitaException {
        transactionService.begin();
        final SExpressionContext sExpressionContext = new SExpressionContext();
        sExpressionContext.setSerializableInputValues(expressionContexts);
        operationService.execute(operation, containerId, containerType, sExpressionContext);
        transactionService.complete();
    }

    private SDataInstance getDataInstance(final String dataInstanceName, final long containerId, final String containerType) throws SBonitaException {
        transactionService.begin();
        final SDataInstance dataInstance = dataInstanceService.getDataInstance(dataInstanceName, containerId, containerType);
        transactionService.complete();
        return dataInstance;
    }

    private void createStringDataInstance(final String instanceName, final long containerId, final String containerType,
            final String defaultValueExpressionContent, final Serializable currentDataInstanceValue) throws SBonitaException, SDataInstanceException {
        final SDataInstance dataInstance = buildDataInstance(instanceName, String.class.getName(), "testUpdate", defaultValueExpressionContent, containerId,
                containerType, false, SExpression.TYPE_CONSTANT, null, currentDataInstanceValue);
        insertDataInstance(dataInstance);
    }

    private SDataInstance buildDataInstance(final String instanceName, final String className, final String description,
            final String defaultValueExpressionContent, final long containerId, final String containerType, final boolean isTransient,
            final String expressionType, final String expressionInterpreter, final Serializable currentDataInstanceValue) throws SBonitaException {
        // create definition
        final SDataDefinitionBuilder dataDefinitionBuilder = dataDefinitionBuilders.getDataDefinitionBuilder();
        dataDefinitionBuilder.createNewInstance(instanceName, className);
        initializeBuilder(dataDefinitionBuilder, description, defaultValueExpressionContent, className, isTransient, expressionType, expressionInterpreter);
        final SDataDefinition dataDefinition = dataDefinitionBuilder.done();
        // create data instance
        final SDataInstanceBuilder dataInstanceBuilder = dataInstanceBuilders.getDataInstanceBuilder().createNewInstance(dataDefinition);
        final SDataInstance dataInstance = dataInstanceBuilder.setContainerId(containerId).setContainerType(containerType).setValue(currentDataInstanceValue)
                .done();
        return dataInstance;
    }

    private void insertDataInstance(final SDataInstance dataInstance) throws SBonitaException {
        transactionService.begin();
        dataInstanceService.createDataInstance(dataInstance);
        dataInstanceService.createDataContainer(dataInstance.getContainerId(), dataInstance.getContainerType());
        transactionService.complete();
    }

    private void initializeBuilder(final SDataDefinitionBuilder dataDefinitionBuilder, final String description, final String defaultValueExpressionContent,
            final String defaultValueExprReturnType, final boolean isTransient, final String expressionType, final String interpreter)
            throws SInvalidExpressionException {
        SExpression expression = null;
        if (defaultValueExpressionContent != null) {
            // create expression
            final SExpressionBuilder expreBuilder = sExpressionBuilders.getExpressionBuilder().createNewInstance();
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

    private void deleteDataInstance(final SDataInstance dataInstance) throws STransactionCommitException,
            SBadTransactionStateException, FireEventException, STransactionCreationException, SDataInstanceException, STransactionRollbackException {
        transactionService.begin();
        dataInstanceService.deleteDataInstance(dataInstance);
        transactionService.complete();
    }

}
