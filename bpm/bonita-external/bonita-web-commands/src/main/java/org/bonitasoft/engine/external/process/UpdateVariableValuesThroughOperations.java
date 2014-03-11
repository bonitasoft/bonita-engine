/**
 * Copyright (C) 2012, 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.external.process;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.command.TenantCommand;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public abstract class UpdateVariableValuesThroughOperations extends TenantCommand {

    protected static final String OPERATIONS_LIST_KEY = "OPERATIONS_LIST_KEY";

    protected static final String OPERATIONS_INPUT_KEY = "OPERATIONS_INPUT_KEY";

    protected static final String CURRENT_VARIABLE_VALUES_MAP_KEY = "CURRENT_VARIABLE_VALUES_MAP_KEY";

    protected void updateVariablesThroughOperations(final List<Operation> operations, final Map<String, Serializable> operationsInputValues,
            final Map<String, Serializable> currentVariableValues, final long containerInstanceId) throws SBonitaException {
        final ClassLoaderService classLoaderService = getServiceAccessor().getClassLoaderService();
        final TransactionContent tc = new UpdateVariableValues(operations, operationsInputValues, currentVariableValues, containerInstanceId,
                classLoaderService);
        tc.execute();
    }

    /**
     * Transactional object to execute the update of the variable values through operations into one transaction.
     * 
     * @author Emmanuel Duchastenier
     */
    class UpdateVariableValues implements TransactionContent {

        private final Map<String, Serializable> currentVariableValues;

        private final long containerInstanceId;

        private final ClassLoaderService classLoaderService;

        private final List<Operation> operations;

        private final Map<String, Serializable> operationsInputValues;

        public UpdateVariableValues(final List<Operation> operations, final Map<String, Serializable> operationsInputValues,
                final Map<String, Serializable> currentVariableValues, final long containerInstanceId, final ClassLoaderService classLoaderService) {
            this.operations = operations;
            this.operationsInputValues = operationsInputValues;
            this.currentVariableValues = currentVariableValues;
            this.containerInstanceId = containerInstanceId;
            this.classLoaderService = classLoaderService;
        }

        @Override
        public void execute() throws SBonitaException {
            // logic
            final ClassLoader processClassloader = classLoaderService
                    .getLocalClassLoader(ScopeType.PROCESS.name(), getProcessDefinitionId(containerInstanceId));
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(processClassloader);
                for (final Operation operation : operations) {
                    // check in currentVariableValues and update variable
                    final String dataName = operation.getLeftOperand().getName();
                    for (final Entry<String, Serializable> variableEntry : currentVariableValues.entrySet()) {
                        if (variableEntry.getKey().equals(dataName)) {
                            // do update
                            executeOperation(operation, operationsInputValues, currentVariableValues, containerInstanceId);
                        }
                    }
                }
            } finally {
                Thread.currentThread().setContextClassLoader(contextClassLoader);
            }
        }

    }

    protected abstract void executeOperation(Operation operation, Map<String, Serializable> operationsInputValues,
            Map<String, Serializable> currentVariableValues, long containerId) throws SBonitaException;

    public abstract long getProcessDefinitionId(long containerInstanceId) throws SBonitaException;

    public abstract TenantServiceAccessor getServiceAccessor();

}
