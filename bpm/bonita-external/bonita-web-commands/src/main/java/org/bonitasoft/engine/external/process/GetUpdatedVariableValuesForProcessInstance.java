/**
 * Copyright (C) 2012, 2014 Bonitasoft S.A.
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
package org.bonitasoft.engine.external.process;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.model.builder.ServerModelConvertor;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class GetUpdatedVariableValuesForProcessInstance extends UpdateVariableValuesThroughOperations {

    private static final String PROCESS_INSTANCE_ID_KEY = "PROCESS_INSTANCE_ID_KEY";

    TenantServiceAccessor serviceAccessor;

    @SuppressWarnings({ "unchecked" })
    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        this.serviceAccessor = serviceAccessor;
        // get specified parameters
        final Map<String, Serializable> currentVariableValues;
        final long processInstanceId;
        List<Operation> operations;
        Map<String, Serializable> operationsInputValues;
        try {
            operations = (List<Operation>) parameters.get(OPERATIONS_LIST_KEY);
            operationsInputValues = (Map<String, Serializable>) parameters.get(OPERATIONS_INPUT_KEY);
            currentVariableValues = (Map<String, Serializable>) parameters.get(CURRENT_VARIABLE_VALUES_MAP_KEY);
            processInstanceId = (Long) parameters.get(PROCESS_INSTANCE_ID_KEY);
        } catch (final Exception e) {
            throw new SCommandParameterizationException("Mandatory parameter " + OPERATIONS_LIST_KEY + "/" + OPERATIONS_INPUT_KEY + "/"
                    + CURRENT_VARIABLE_VALUES_MAP_KEY + "/" + PROCESS_INSTANCE_ID_KEY + " is missing.");
        }
        if (operations == null || operationsInputValues == null || currentVariableValues == null) {
            throw new SCommandParameterizationException("Mandatory parameter " + OPERATIONS_LIST_KEY + "/" + OPERATIONS_INPUT_KEY + "/"
                    + CURRENT_VARIABLE_VALUES_MAP_KEY + "/" + PROCESS_INSTANCE_ID_KEY + " is not convertible.");
        }
        try {
            updateVariablesThroughOperations(operations, operationsInputValues, currentVariableValues, processInstanceId);
        } catch (final SCommandExecutionException e) {
            throw e;
        } catch (final SBonitaException e) {
            throw new SCommandExecutionException(e);
        }
        return (Serializable) currentVariableValues;
    }

    @Override
    protected void executeOperation(final Operation operation, final Map<String, Serializable> operationsInputValues,
            final Map<String, Serializable> currentVariableValues, final long processInstanceId) throws SBonitaException {
        if (currentVariableValues != null) {
            final SOperation sOperation = ServerModelConvertor.convertOperation(operation);
            final Map<String, Serializable> inputValues = operationsInputValues;
            inputValues.putAll(currentVariableValues);
            final SExpressionContext sec = new SExpressionContext();
            sec.setSerializableInputValues(inputValues);
            sec.setContainerId(processInstanceId);
            sec.setContainerType(DataInstanceContainer.PROCESS_INSTANCE.name());
            final OperationService operationService = serviceAccessor.getOperationService();
            // containerId + containerType are not necessary as we don't need to update data in DB:
            operationService.execute(sOperation, -1L, null, sec);
            // Let's update the values with the new ones:
            for (final Entry<String, Serializable> variable : currentVariableValues.entrySet()) {
                final Object updatedValue = sec.getInputValues().get(variable.getKey());
                variable.setValue((Serializable) updatedValue);
            }
        }
    }

    @Override
    public TenantServiceAccessor getServiceAccessor() {
        return serviceAccessor;
    }

    @Override
    public long getProcessDefinitionId(final long containerInstanceId) throws SProcessInstanceNotFoundException, SProcessInstanceReadException {
        final SProcessInstance processInstance = serviceAccessor.getProcessInstanceService().getProcessInstance(containerInstanceId);
        return processInstance.getProcessDefinitionId();
    }
}
