/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.external.web.forms;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.impl.ProcessStarter;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinitionWithInputValues;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceCreationException;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.external.web.forms.ExecuteActionsBaseEntry;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Ruiheng Fan
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class ExecuteActionsAndStartInstanceExt extends ExecuteActionsBaseEntry {

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        final List<Operation> operations = getParameter(parameters, OPERATIONS_LIST_KEY, "Mandatory parameter " + OPERATIONS_LIST_KEY
                + " is missing or not convertible to List.");
        final Map<String, Serializable> operationsInputValues = getParameter(parameters, OPERATIONS_INPUT_KEY, "Mandatory parameter " + OPERATIONS_INPUT_KEY
                + " is missing or not convertible to Map.");
        final List<ConnectorDefinitionWithInputValues> connectorsWithInput = getParameter(parameters, CONNECTORS_LIST_KEY, "Mandatory parameter "
                + CONNECTORS_LIST_KEY + " is missing or not convertible to List.");
        final Long sProcessDefinitionID = getParameter(parameters, PROCESS_DEFINITION_ID_KEY, "Mandatory parameter " + PROCESS_DEFINITION_ID_KEY
                + " is missing or not convertible to long.");

        final Long userId = getParameter(parameters, USER_ID_KEY, "Mandatory parameter " + USER_ID_KEY + " is missing or not convertible to String.");

        try {
            final ClassLoaderService classLoaderService = serviceAccessor.getClassLoaderService();
            final ClassLoader processClassloader = classLoaderService.getLocalClassLoader(ScopeType.PROCESS.name(), sProcessDefinitionID);
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(processClassloader);

                return startProcess(sProcessDefinitionID, userId, operations, operationsInputValues, connectorsWithInput).getId();
            } finally {
                Thread.currentThread().setContextClassLoader(contextClassLoader);
            }
        } catch (final SBonitaException e) {
            throw new SCommandExecutionException(
                    "Error executing command 'Map<String, Serializable> ExecuteActionsAndStartInstanceExt(Map<Operation, Map<String, Serializable>> operationsMap, long processDefinitionID)'",
                    e);
        }
    }

    private ProcessInstance startProcess(final long processDefinitionId, final long userId, final List<Operation> operations,
            final Map<String, Serializable> context, final List<ConnectorDefinitionWithInputValues> connectorsWithInput)
            throws SProcessDefinitionException, SProcessDefinitionReadException, SProcessInstanceCreationException {
        final ProcessStarter starter = new ProcessStarter(userId, processDefinitionId, operations, context);
        return starter.start(connectorsWithInput);
    }

}
