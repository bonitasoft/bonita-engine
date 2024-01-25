/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.core.connector;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.bonitasoft.engine.core.connector.exception.SConnectorException;
import org.bonitasoft.engine.core.connector.exception.SInvalidConnectorImplementationException;
import org.bonitasoft.engine.core.connector.parser.SConnectorImplementationDescriptor;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.resources.SBARResource;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @since 6.0
 */
public interface ConnectorService {

    String TO_BE_EXECUTED = "TO_BE_EXECUTED";

    String DONE = "DONE";

    String FAILED = "FAILED";

    String SKIPPED = "SKIPPED";

    String TO_RE_EXECUTE = "TO_RE_EXECUTE";

    /**
     * Execute a connector instance by given connectorDefinitionId and connectorDefinitionVersion
     *
     * @param processDefinitionId
     *        The identifier of process definition
     * @param connectorDefinitionId
     *        The identifier of connector definition
     * @param connectorDefinitionVersion
     *        The version of connector definition
     * @param connectorInputParameters
     *        The input of connector
     * @param inputValues
     *        The input values of connector
     * @param classLoader
     *        The class loader used to load and run connector
     * @param sexpContext
     *        The expression context
     * @return The output after connector executing
     * @throws SConnectorException
     *         Error thrown if has exceptions during the connector executing
     */
    ConnectorResult executeMultipleEvaluation(long processDefinitionId, String connectorDefinitionId,
            String connectorDefinitionVersion,
            Map<String, SExpression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues,
            ClassLoader classLoader,
            SExpressionContext sexpContext) throws SConnectorException;

    /**
     * Load connectors for given process definition and tenant, connectors will be stored in cache after loading
     *
     * @param sDefinition
     *        The process definition
     * @return true if all connectors found have all them dependencies resolved and are correctly loaded
     * @throws SConnectorException
     *         Error thrown if has exceptions during the connector loading
     */
    boolean loadConnectors(SProcessDefinition sDefinition) throws SConnectorException;

    /**
     * Set connector implementation for id and version specified connector.
     * Store all connector related files if they are not existed and replace the old implementation with the new one in
     * file system.
     * Delete former and load current connectors in cache.
     *
     * @param sProcessDefinition
     *        The process definition which the connector belongs to
     * @param connectorId
     *        Id of connector definition.
     * @param connectorVersion
     *        Version of connector definition
     * @param connectorImplementationArchive
     *        zip byte array containing the connector implementation information
     * @throws SConnectorException
     *         Error thrown if has exceptions during the connector implementation setting
     * @throws SInvalidConnectorImplementationException
     */
    void setConnectorImplementation(SProcessDefinition sProcessDefinition, String connectorId, String connectorVersion,
            byte[] connectorImplementationArchive) throws SConnectorException, SInvalidConnectorImplementationException;

    /**
     * Get a list of connector implementation descriptors for id specified process definition, the returned list is
     * paginated
     *
     * @param processDefinitionId
     *        Identifier of process definition
     * @param fromIndex
     *        Start index of connector record
     * @param numberPerPage
     *        Number of connectors we want to get. Maximum number of connectors returned.
     * @param field
     *        The field that the result ordered by
     * @param order
     *        The order, ACS or DESC
     * @return A list of all satisfied connector implementation descriptor objects
     * @throws SConnectorException
     *         Error thrown if has exceptions during the connector implementations retrieve
     */
    List<SConnectorImplementationDescriptor> getConnectorImplementations(long processDefinitionId, int fromIndex,
            int numberPerPage,
            String field, OrderByType order) throws SConnectorException;

    /**
     * Get connector implementation descriptor for specified connector in a process definition.
     *
     * @param processDefinitionId
     *        Identifier of process definition
     * @param connectorId
     *        id of connector definition
     * @param connectorVersion
     *        version of connector definition
     * @return connector implementation descriptor object
     * @throws SConnectorException
     *         Error thrown if has exceptions during the connector implementation get
     */
    SConnectorImplementationDescriptor getConnectorImplementation(long processDefinitionId, String connectorId,
            String connectorVersion)
            throws SConnectorException;

    /**
     * @param parameters
     * @param sExpressionContext
     * @param inputValues
     * @return
     * @throws SExpressionTypeUnknownException
     * @throws SExpressionEvaluationException
     * @throws SExpressionDependencyMissingException
     * @throws SInvalidExpressionException
     */
    Map<String, Object> evaluateInputParameters(String connectorId, Map<String, SExpression> parameters,
            SExpressionContext sExpressionContext,
            Map<String, Map<String, Serializable>> inputValues)
            throws SExpressionTypeUnknownException, SExpressionEvaluationException,
            SExpressionDependencyMissingException, SInvalidExpressionException;

    SConnectorImplementationDescriptor getConnectorImplementationDescriptor(long processDefinitionId,
            String connectorId, String version)
            throws SConnectorException;

    /**
     * @param outputs
     * @param expressionContext
     * @param result
     * @throws SConnectorException
     */
    void executeOutputOperation(List<SOperation> outputs, SExpressionContext expressionContext, ConnectorResult result)
            throws SConnectorException;

    /**
     * @param processDefinitionId
     * @param sConnectorInstance
     * @param connectorImplementationDescriptor
     * @param classLoader
     * @param inputParameters
     * @return the result of the connector execution
     * @throws SConnectorException
     */
    CompletableFuture<ConnectorResult> executeConnector(long processDefinitionId, SConnectorInstance sConnectorInstance,
            SConnectorImplementationDescriptor connectorImplementationDescriptor, ClassLoader classLoader,
            Map<String, Object> inputParameters)
            throws SConnectorException;

    /**
     * @param result
     * @throws SConnectorException
     */
    void disconnect(ConnectorResult result) throws SConnectorException;

    /**
     * @param processDefinitionId
     *        the id of the process definition
     * @return
     *         the number of connector implementation for this process definition
     * @throws SConnectorException
     */
    Long getNumberOfConnectorImplementations(long processDefinitionId) throws SConnectorException;

    List<SBARResource> getConnectorImplementations(long processDefinitionId, int from, int numberOfElements)
            throws SBonitaReadException;

    void addConnectorImplementation(Long processDefinitionId, String name, byte[] content) throws SRecorderException;

    void removeConnectorImplementations(long processDefinitionId) throws SBonitaReadException, SRecorderException;
}
