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
package org.bonitasoft.engine.command;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.impl.ProcessStarter;
import org.bonitasoft.engine.bpm.process.ProcessActivationException;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessExecutionException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.command.system.CommandWithParameters;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.execution.AdvancedStartProcessValidator;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Elias Ricken de Medeiros
 */
public abstract class AbstractStartProcessCommand extends CommandWithParameters {
    public static final String STARTED_BY = "started_by";
    public static final String PROCESS_DEFINITION_ID = "process_definition_id";
    public static final String OPERATIONS = "operations";
    public static final String CONTEXT = "context";

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        // get parameters
        final long processDefinitionId = getProcessDefinitionId(parameters);
        final List<String> activityNames = getActivityNames(parameters);
        final long startedBy = getStartedBy(parameters);
        final Map<String, Serializable> context = getContext(parameters);
        final List<Operation> operations = getOperations(parameters);

        try {
            validateInputs(serviceAccessor, processDefinitionId, activityNames);

            return startProcess(processDefinitionId, activityNames, startedBy, context, operations);
        } catch (final SCommandExecutionException e) {
            throw e;
        } catch (final Exception e) {
            throw new SCommandExecutionException(e);
        }
    }

    private ProcessInstance startProcess(final long processDefinitionId, final List<String> activityNames, final long startedBy,
            final Map<String, Serializable> context, final List<Operation> operations) throws ProcessDefinitionNotFoundException, ProcessActivationException,
            ProcessExecutionException {
        final ProcessStarter starter = new ProcessStarter(startedBy, processDefinitionId, operations, context, activityNames);
        return starter.start();
    }

    private void validateInputs(final TenantServiceAccessor serviceAccessor, final long processDefinitionId, final List<String> activityNames)
            throws SBonitaException {
        final AdvancedStartProcessValidator validator = new AdvancedStartProcessValidator(serviceAccessor.getProcessDefinitionService(), processDefinitionId);
        final List<String> problems = validator.validate(activityNames);
        handleProblems(problems);
    }

    private void handleProblems(final List<String> problems) throws SCommandExecutionException {
        if (!problems.isEmpty()) {
            final StringBuilder stb = new StringBuilder();
            for (final String problem : problems) {
                stb.append(problem);
                stb.append("\n");
            }
            throw new SCommandExecutionException(stb.toString());
        }
    }

    private Long getStartedBy(final Map<String, Serializable> parameters) throws SCommandParameterizationException {
        return getLongMandadoryParameter(parameters, STARTED_BY);
    }

    private Long getProcessDefinitionId(final Map<String, Serializable> parameters) throws SCommandParameterizationException {
        return getLongMandadoryParameter(parameters, PROCESS_DEFINITION_ID);
    }

    private List<Operation> getOperations(final Map<String, Serializable> parameters) throws SCommandParameterizationException {
        return getParameter(parameters, OPERATIONS);
    }

    private Map<String, Serializable> getContext(final Map<String, Serializable> parameters) throws SCommandParameterizationException {
        return getParameter(parameters, CONTEXT);
    }

    protected abstract List<String> getActivityNames(Map<String, Serializable> parameters) throws SCommandParameterizationException;
}
