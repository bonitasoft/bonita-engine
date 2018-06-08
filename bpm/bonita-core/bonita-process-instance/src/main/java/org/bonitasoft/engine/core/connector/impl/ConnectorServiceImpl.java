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
package org.bonitasoft.engine.core.connector.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.connector.ConnectorExecutor;
import org.bonitasoft.engine.core.connector.ConnectorResult;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.connector.exception.SConnectorException;
import org.bonitasoft.engine.core.connector.exception.SInvalidConnectorImplementationException;
import org.bonitasoft.engine.core.connector.parser.SConnectorImplementationDescriptor;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.resources.BARResourceType;
import org.bonitasoft.engine.resources.ProcessResourcesService;
import org.bonitasoft.engine.resources.SBARResource;
import org.bonitasoft.engine.tracking.TimeTracker;
import org.bonitasoft.engine.tracking.TimeTrackerRecords;

/**
 * @author Baptiste Mesta
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class ConnectorServiceImpl implements ConnectorService {

    protected static final String CONNECTOR_CACHE_NAME = "CONNECTOR";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String IMPLEMENTATION_EXT = ".impl";
    private final CacheService cacheService;
    private final ConnectorExecutor connectorExecutor;
    private final ExpressionResolverService expressionResolverService;
    private final OperationService operationService;
    private final DependencyService dependencyService;
    private final TechnicalLoggerService logger;
    private final TimeTracker timeTracker;
    private final ProcessResourcesService processResourcesService;

    private final JAXBContext jaxbContext;
    private final Schema schema;

    public ConnectorServiceImpl(final CacheService cacheService, final ConnectorExecutor connectorExecutor,
            final ExpressionResolverService expressionResolverService, final OperationService operationService,
            final DependencyService dependencyService, final TechnicalLoggerService logger, final TimeTracker timeTracker,
            ProcessResourcesService processResourcesService) {
        this.cacheService = cacheService;
        this.connectorExecutor = connectorExecutor;
        this.expressionResolverService = expressionResolverService;
        this.processResourcesService = processResourcesService;
        this.operationService = operationService;
        this.dependencyService = dependencyService;
        this.logger = logger;
        this.timeTracker = timeTracker;
        try {
            jaxbContext = JAXBContext.newInstance(SConnectorImplementationDescriptor.class);
            URL schemaURL = ConnectorServiceImpl.class.getResource("/connectors-impl.xsd");
            final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schema = sf.newSchema(schemaURL);
        } catch (final Exception e) {
            throw new BonitaRuntimeException("Unable to load unmarshaller for connector implementation descriptor", e);
        }
    }

    /**
     * Build the log message using the connector instance's context (name, version, connector id, connector instance id, container type, container id)
     *
     * @param connectorInstance
     * @return the log message built using the connector instance's context
     */
    private static String buildConnectorContextMessage(final SConnectorInstance connectorInstance) {
        return " [name: <" + connectorInstance.getName() + ">, version: <" + connectorInstance.getVersion() + ">, connector id: <"
                + connectorInstance.getConnectorId() + ">, connector instance id: <" + connectorInstance.getId() + ">, container type: <"
                + connectorInstance.getContainerType() + ">, container id: <" + connectorInstance.getContainerId() + ">, activation event: <"
                + connectorInstance.getActivationEvent() + ">]";
    }

    private static String buildConnectorInputMessage(final Map<String, Object> inputParameters) {
        final StringBuilder stb = new StringBuilder();
        if (inputParameters != null && !inputParameters.isEmpty()) {
            stb.append(LINE_SEPARATOR);
            stb.append("Inputs: ");
            stb.append(LINE_SEPARATOR);
            final Set<String> inputNames = inputParameters.keySet();
            for (final String inputName : inputNames) {
                stb.append("    <").append(inputName).append("> : <").append(inputParameters.get(inputName)).append(">");
                stb.append(LINE_SEPARATOR);
            }
        }
        return stb.toString();
    }

    @Override
    public ConnectorResult executeConnector(final long processDefinitionId, final SConnectorInstance sConnectorInstance,
            SConnectorImplementationDescriptor connectorImplementationDescriptor, final ClassLoader classLoader,
            final Map<String, Object> inputParameters) throws SConnectorException {
        final String implementationClassName = connectorImplementationDescriptor.getImplementationClassName();
        final ConnectorResult connectorResult = executeConnectorInClassloader(implementationClassName, classLoader, inputParameters);
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
            final String message = "Executed connector " + buildConnectorContextMessage(sConnectorInstance)
                    + buildConnectorInputMessage(inputParameters);
            logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, message);
        }
        return connectorResult;
    }

    @Override
    public SConnectorImplementationDescriptor getConnectorImplementationDescriptor(long processDefinitionId, String connectorId, String version)
            throws SConnectorException {
        try {
            SConnectorImplementationDescriptor descriptor = getImplementation(processDefinitionId, connectorId, version);
            if (descriptor == null) {
                throw new SConnectorException("There is no implementation found for the connector " + connectorId
                        + " with version " + version);
            }
            return descriptor;
        } catch (final SCacheException e) {
            throw new SConnectorException(e);
        }
    }

    @Override
    public void executeOutputOperation(final List<SOperation> outputs, final SExpressionContext expressionContext, final ConnectorResult result)
            throws SConnectorException {
        final long startTime = System.currentTimeMillis();
        try {
            expressionContext.putAllInputValues(result.getResult());
            operationService.execute(outputs, expressionContext.getContainerId(), expressionContext.getContainerType(), expressionContext);// data is in
            disconnect(result);
        } catch (final SOperationExecutionException e) {
            throw new SConnectorException(e);
        } finally {
            if (timeTracker.isTrackable(TimeTrackerRecords.EXECUTE_CONNECTOR_OUTPUT_OPERATIONS)) {
                final long endTime = System.currentTimeMillis();
                String desc = "ConnectorResult: " + result;
                timeTracker.track(TimeTrackerRecords.EXECUTE_CONNECTOR_OUTPUT_OPERATIONS, desc, endTime - startTime);
            }
        }
    }

    @Override
    public void disconnect(final ConnectorResult result) throws SConnectorException {
        final long startTime = System.currentTimeMillis();
        try {
            connectorExecutor.disconnect(new SConnectorAdapter(result.getConnector()));
        } catch (final org.bonitasoft.engine.connector.exception.SConnectorException e) {
            throw new SConnectorException(e);
        } finally {
            if (timeTracker.isTrackable(TimeTrackerRecords.EXECUTE_CONNECTOR_DISCONNECT)) {
                final long endTime = System.currentTimeMillis();
                final StringBuilder desc = new StringBuilder();
                desc.append("ConnectorResult: ");
                desc.append(result);
                timeTracker.track(TimeTrackerRecords.EXECUTE_CONNECTOR_DISCONNECT, desc.toString(), endTime - startTime);
            }
        }
    }

    private SConnectorImplementationDescriptor getImplementation(final long rootDefinitionId, final String connectorId,
            final String version) throws SConnectorException, SCacheException {
        SConnectorImplementationDescriptor descriptor;
        try {
            final String key = buildConnectorImplementationKey(rootDefinitionId, connectorId, version);

            descriptor = (SConnectorImplementationDescriptor) cacheService.get(CONNECTOR_CACHE_NAME, key);
            if (descriptor == null) {
                // No value in cache : reload connector to ensure the cache stores all connectors for the current process
                loadConnectors(rootDefinitionId);
                descriptor = (SConnectorImplementationDescriptor) cacheService.get(CONNECTOR_CACHE_NAME, key);
            }
        } catch (final NumberFormatException e) {
            throw new SConnectorException(e);
        } catch (final SCacheException e) {
            throw e;
        }
        return descriptor;
    }

    private void cache(final long processDefinitionId, final SConnectorImplementationDescriptor connectorImplementation) throws SCacheException {
        final String key = buildConnectorImplementationKey(processDefinitionId, connectorImplementation.getDefinitionId(),
                connectorImplementation.getDefinitionVersion());
        cacheService.store(CONNECTOR_CACHE_NAME, key, connectorImplementation);
    }

    protected String buildConnectorImplementationKey(final long rootDefinitionId, final String connectorId, final String version) {
        return String.valueOf(rootDefinitionId) + ":" + connectorId + "-" + version;
    }

    @Override
    public ConnectorResult executeMultipleEvaluation(final long processDefinitionId, final String connectorDefinitionId,
            final String connectorDefinitionVersion, final Map<String, SExpression> connectorInputParameters,
            final Map<String, Map<String, Serializable>> inputValues, final ClassLoader classLoader, final SExpressionContext expressionContext)
            throws SConnectorException {
        final String implementationClassName = getConnectorImplementationDescriptor(processDefinitionId, connectorDefinitionId, connectorDefinitionVersion)
                .getImplementationClassName();
        final Map<String, Object> inputParameters;
        try {
            inputParameters = evaluateInputParameters(connectorDefinitionId, connectorInputParameters, expressionContext,
                    inputValues);
        } catch (final SBonitaException e) {
            throw new SConnectorException(e);
        }
        final ConnectorResult connectorResult = executeConnectorInClassloader(implementationClassName, classLoader, inputParameters);
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "Executed connector <" + implementationClassName
                    + "> with definition id <" + connectorDefinitionId + ">, version <" + connectorDefinitionVersion
                    + ">, " + buildConnectorInputMessage(inputParameters));
        }
        return connectorResult;
    }

    private ConnectorResult executeConnectorInClassloader(final String implementationClassName, final ClassLoader classLoader,
            final Map<String, Object> inputParameters) throws SConnectorException {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            Connector connector = (Connector) Class.forName(implementationClassName, true, classLoader).newInstance();
            final SConnectorAdapter sConnectorAdapter = new SConnectorAdapter(connector);
            return new ConnectorResult(connector, connectorExecutor.execute(sConnectorAdapter, inputParameters, classLoader));
        } catch (final ClassNotFoundException e) {
            throw new SConnectorException(implementationClassName + " can not be found.", e);
        } catch (final InstantiationException e) {
            throw new SConnectorException(implementationClassName + " can not be instantiated.", e);
        } catch (Throwable e) {
            throw new SConnectorException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    @Override
    public Map<String, Object> evaluateInputParameters(final String connectorId, final Map<String, SExpression> parameters,
            final SExpressionContext sExpressionContext,
            final Map<String, Map<String, Serializable>> inputValues) throws SExpressionTypeUnknownException, SExpressionEvaluationException,
            SExpressionDependencyMissingException, SInvalidExpressionException {
        final long startTime = System.currentTimeMillis();
        final Map<String, Object> inputParameters = new HashMap<>(parameters.size());
        try {
            for (final Entry<String, SExpression> input : parameters.entrySet()) {
                if (sExpressionContext != null) {
                    final String key = input.getKey();
                    if (inputValues != null && !inputValues.isEmpty() && inputValues.containsKey(key)) {
                        sExpressionContext.setSerializableInputValues(inputValues.get(key));
                    }
                    inputParameters.put(input.getKey(), expressionResolverService.evaluate(input.getValue(), sExpressionContext));
                } else {
                    inputParameters.put(input.getKey(), expressionResolverService.evaluate(input.getValue()));
                }
            }
        } finally {
            if (timeTracker.isTrackable(TimeTrackerRecords.EXECUTE_CONNECTOR_INPUT_EXPRESSIONS)) {
                final long endTime = System.currentTimeMillis();
                final StringBuilder desc = new StringBuilder();
                desc.append("Connector ID: ");
                desc.append(connectorId);
                desc.append(" - input parameters: ");
                desc.append(inputParameters);
                timeTracker.track(TimeTrackerRecords.EXECUTE_CONNECTOR_INPUT_EXPRESSIONS, desc.toString(), endTime - startTime);
            }
        }
        return inputParameters;
    }

    @Override
    public boolean loadConnectors(final SProcessDefinition sDefinition) throws SConnectorException {
        return loadConnectors(sDefinition.getId());
    }

    protected boolean loadConnectors(final long processDefinitionId) throws SConnectorException {
        String name = null;
        try {
            final List<SBARResource> connectorImplementations = getConnectorImplementations(processDefinitionId, 0, Integer.MAX_VALUE);
            for (SBARResource connectorImplementationFile : connectorImplementations) {
                name = connectorImplementationFile.getName();
                cache(processDefinitionId, convert(connectorImplementationFile.getContent()));
            }
        } catch (final IOException e) {
            throw new SConnectorException("Can not load ConnectorImplementation XML. The file name is <" + name + ">.", e);
        } catch (final SCacheException e) {
            throw new SConnectorException("Unable to cache the connector implementation " + name + ".", e);
        } catch (SBonitaReadException e) {
            throw new SConnectorException("Unable to list the connector implementations", e);
        }
        return true;
    }

    private SConnectorImplementationDescriptor convert(byte[] content) throws IOException {
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(schema);
            String connectorImplementationFileContent = new String(content);
            connectorImplementationFileContent = connectorImplementationFileContent.replace("<connectorImplementation>",
                    "<implementation:connectorImplementation xmlns:implementation=\"http://www.bonitasoft.org/ns/connector/implementation/6.0\">");
            connectorImplementationFileContent = connectorImplementationFileContent.replace("</connectorImplementation>",
                    "</implementation:connectorImplementation>");
            return (SConnectorImplementationDescriptor) unmarshaller.unmarshal(new StringReader(connectorImplementationFileContent));
        } catch (final JAXBException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void setConnectorImplementation(final SProcessDefinition sProcessDefinition, final String connectorId,
            final String connectorVersion, final byte[] connectorImplementationArchive) throws SConnectorException, SInvalidConnectorImplementationException {
        ConnectorArchive connectorArchive = extractConnectorImplementation(connectorImplementationArchive);
        replaceConnectorImpl(sProcessDefinition, connectorId, connectorVersion, connectorArchive);
        reLoadConnectors(sProcessDefinition, connectorId, connectorVersion);
    }

    private void replaceConnectorImpl(final SProcessDefinition sDefinition,
            final String connectorId, final String connectorVersion, ConnectorArchive connectorArchive)
            throws SConnectorException, SInvalidConnectorImplementationException {
        try {
            checkConnectorImplementationIsValid(parseConnectorImplementation(connectorArchive.getConnectorImplContent()), connectorId, connectorVersion);
            SBARResource connectorImplementationFile = getConnectorImplementationResource(sDefinition.getId(), connectorId, connectorVersion);
            SConnectorImplementationDescriptor connectorImplementationDescriptorToReplace = null;
            if (connectorImplementationFile != null) {
                connectorImplementationDescriptorToReplace = parseConnectorImplementation(
                        connectorImplementationFile.getContent());
            }
            updateJarDependencies(sDefinition, connectorArchive, connectorImplementationDescriptorToReplace);
            updateConnectorImplementationFile(sDefinition, connectorArchive, connectorImplementationFile);
            dependencyService.refreshClassLoaderAfterUpdate(ScopeType.PROCESS, sDefinition.getId());
        } catch (final SRecorderException | SDependencyException | SBonitaReadException e) {
            throw new SConnectorException("Problem replacing connector implementation of connector " + connectorId + " of process " + sDefinition.getId(), e);
        }
    }

    private void updateConnectorImplementationFile(SProcessDefinition sDefinition, ConnectorArchive connectorArchive, SBARResource connectorResourceToReplace)
            throws SRecorderException {
        if (connectorResourceToReplace != null && connectorResourceToReplace.getName().equals(connectorArchive.getConnectorImplName())) {
            processResourcesService.update(connectorResourceToReplace, connectorArchive.getConnectorImplContent());
        } else {
            if (connectorResourceToReplace != null) {
                processResourcesService.remove(connectorResourceToReplace);
            }
            processResourcesService.add(sDefinition.getId(), connectorArchive.getConnectorImplName(), BARResourceType.CONNECTOR,
                    connectorArchive.getConnectorImplContent());
        }
    }

    private void updateJarDependencies(SProcessDefinition sDefinition, ConnectorArchive connectorArchive,
            SConnectorImplementationDescriptor connectorImplementationDescriptorToReplace)
            throws SBonitaReadException, SDependencyException {
        List<String> jarFileNames = connectorImplementationDescriptorToReplace == null ? Collections.emptyList()
                : connectorImplementationDescriptorToReplace.getJarDependencies();
        Set<String> dependenciesToUpdate = new HashSet<>();
        if (jarFileNames != null) {
            for (String jarFileName : jarFileNames) {
                SDependency dependencyOfArtifact = dependencyService.getDependencyOfArtifact(sDefinition.getId(), ScopeType.PROCESS, jarFileName);
                if (dependencyOfArtifact == null) {
                    // For compatibility with older versions that may still have the wrong name in database:
                    dependencyOfArtifact = dependencyService.getDependencyOfArtifact(sDefinition.getId(), ScopeType.PROCESS,
                            sDefinition.getId() + "_" + jarFileName);
                }
                if (dependencyOfArtifact != null) {
                    if (connectorArchive.getDependencies().keySet().contains(jarFileName)) {
                        dependenciesToUpdate.add(jarFileName);
                    } else {
                        dependencyService.deleteDependency(dependencyOfArtifact);
                    }
                }
            }
        }
        final long processDefinitionId = sDefinition.getId();
        for (final Entry<String, byte[]> file : connectorArchive.getDependencies().entrySet()) {
            if (dependenciesToUpdate.contains(file.getKey())) {
                dependencyService.updateDependencyOfArtifact(file.getKey(), file.getValue(), file.getKey(), processDefinitionId, ScopeType.PROCESS);
            } else {
                final SDependency existingDependency = dependencyService.getDependencyOfArtifact(sDefinition.getId(), ScopeType.PROCESS, file.getKey());
                if (existingDependency != null) {
                    //a dependency with this name did exists event if it was not declared as a dependency of the connector inside the connector impl file
                    if (connectorImplementationDescriptorToReplace != null) {
                        logger.log(this.getClass(), TechnicalLogSeverity.WARNING,
                                "Updating a dependency of the connector " + connectorImplementationDescriptorToReplace.getDefinitionId() + " in version "
                                        + connectorImplementationDescriptorToReplace.getDefinitionVersion() +
                                        " of process definition " + processDefinitionId + ". The jar file " + file.getKey()
                                        + " was not declared in the previous connector implementation but is in the dependencies of the process. The jar is still updated but this can lead to inconsistencies.");
                    }
                    dependencyService.updateDependencyOfArtifact(file.getKey(), file.getValue(), file.getKey(), processDefinitionId, ScopeType.PROCESS);
                } else {
                    dependencyService.createMappedDependency(file.getKey(), file.getValue(), file.getKey(), processDefinitionId, ScopeType.PROCESS);
                }
            }
        }
    }

    protected void checkConnectorImplementationIsValid(final SConnectorImplementationDescriptor connectorImplementationDescriptor, final String connectorId,
            final String connectorVersion)
            throws SConnectorException, SInvalidConnectorImplementationException {
        if (!connectorImplementationDescriptor.getDefinitionId().equals(connectorId)
                || !connectorImplementationDescriptor.getDefinitionVersion().equals(connectorVersion)) {
            throw new SInvalidConnectorImplementationException("The connector must implement the connectorDefinition with id = <" + connectorId
                    + "> and version = <" + connectorVersion + ">.", connectorImplementationDescriptor);
        }
    }

    ConnectorArchive extractConnectorImplementation(final byte[] connectorImplementationArchive) throws SInvalidConnectorImplementationException {
        ConnectorArchive connectorArchive = new ConnectorArchive();
        try (ZipInputStream zipInputstream = new ZipInputStream(new ByteArrayInputStream(connectorImplementationArchive))) {

            ZipEntry zipEntry;
            while ((zipEntry = zipInputstream.getNextEntry()) != null) {
                String fileName = getFileName(zipEntry.getName());
                if (!fileName.endsWith(".jar") && !fileName.endsWith(".impl")) {
                    continue;
                }
                final byte[] fileContent = IOUtil.getBytes(zipInputstream);
                if (fileName.endsWith(".jar")) {
                    connectorArchive.addDependency(fileName, fileContent);
                } else {
                    connectorArchive.setConnectorImpl(fileName, fileContent);
                }
                zipInputstream.closeEntry();
            }
        } catch (final IOException e) {
            throw new SInvalidConnectorImplementationException(e);
        }
        if (connectorArchive.getConnectorImplContent() == null) {
            throw new SInvalidConnectorImplementationException("The connector archive do not contains a connector implementation");
        }
        return connectorArchive;
    }

    private String getFileName(String name) {
        return name.substring(name.lastIndexOf('/') + 1);
    }

    private SConnectorImplementationDescriptor parseConnectorImplementation(final byte[] bytes) throws SInvalidConnectorImplementationException {
        try {
            return convert(bytes);
        } catch (final IOException e) {
            throw new SInvalidConnectorImplementationException("Can not load ConnectorImplementation XML.", e);
        }
    }

    private SBARResource getConnectorImplementationResource(long processId, String connectorId, String connectorVersion)
            throws SBonitaReadException, SInvalidConnectorImplementationException {
        final List<SBARResource> listFiles = processResourcesService.get(processId, BARResourceType.CONNECTOR, 0, 1000);
        final Pattern pattern = Pattern.compile("^.*\\" + IMPLEMENTATION_EXT + "$");
        SBARResource connectorToReplace = null;
        for (final SBARResource resource : listFiles) {
            final String name = resource.getName();
            if (pattern.matcher(name).matches()) {
                final SConnectorImplementationDescriptor connectorImplementation = parseConnectorImplementation(resource.getContent());
                if (connectorId.equals(connectorImplementation.getDefinitionId()) && connectorVersion.equals(connectorImplementation.getDefinitionVersion())) {
                    connectorToReplace = resource;
                    break;
                }
            }
        }
        return connectorToReplace;
    }

    private void reLoadConnectors(final SProcessDefinition sProcessDefinition, final String connectorId, final String connectorVersion)
            throws SConnectorException {
        final String connectorKey = buildConnectorImplementationKey(sProcessDefinition.getId(), connectorId, connectorVersion);
        try {
            cacheService.remove(CONNECTOR_CACHE_NAME, connectorKey);
            // re_load connectors
            loadConnectors(sProcessDefinition);
        } catch (final SCacheException e) {
            throw new SConnectorException(e);
        }
    }

    @Override
    public Long getNumberOfConnectorImplementations(final long processDefinitionId) throws SConnectorException {
        try {
            return processResourcesService.count(processDefinitionId, BARResourceType.CONNECTOR);
        } catch (SBonitaReadException e) {
            throw new SConnectorException(e);
        }
    }

    @Override
    public List<SConnectorImplementationDescriptor> getConnectorImplementations(final long processDefinitionId, final int fromIndex,
            final int numberPerPage, final String field, final OrderByType order) throws SConnectorException {
        final List<SConnectorImplementationDescriptor> sConnectorImplementationDescriptors = getAllConnectorImplementations(processDefinitionId);
        if (sConnectorImplementationDescriptors != null && sConnectorImplementationDescriptors.size() > 0) {
            // pagination
            if (sConnectorImplementationDescriptors.size() <= fromIndex) {
                throw new SConnectorException("page out of range excepton. Total size is <" + sConnectorImplementationDescriptors.size()
                        + ">, but from index is <" + fromIndex + ">");
            }
            // set the comparison field
            SConnectorImplementationDescriptor.comparedField = field;
            // sorted: sort with ASC order first
            Collections.sort(sConnectorImplementationDescriptors);
            if (order != null && order == OrderByType.DESC) {
                Collections.reverse(sConnectorImplementationDescriptors);
            }
            // sub list
            int endIndex = fromIndex + numberPerPage;
            if (endIndex >= sConnectorImplementationDescriptors.size()) {
                endIndex = sConnectorImplementationDescriptors.size();
            }
            return sConnectorImplementationDescriptors.subList(fromIndex, endIndex);
        }
        return Collections.emptyList();
    }

    /**
     * @param processDefinitionId
     * @return
     * @throws SConnectorException
     */
    private List<SConnectorImplementationDescriptor> getAllConnectorImplementations(final long processDefinitionId)
            throws SConnectorException {
        // get all connector implementations for processDefinitionId
        List<SConnectorImplementationDescriptor> sConnectorImplementationDescriptors = null;
        try {
            final int size = cacheService.getCacheSize(CONNECTOR_CACHE_NAME);
            // reload connectors if connector cache size is 0;
            if (size == 0) {
                this.loadConnectors(processDefinitionId);
            }
            sConnectorImplementationDescriptors = getConnectorImplementationsFromCacheService(processDefinitionId);
            if (sConnectorImplementationDescriptors.isEmpty()) {
                // reload connectors if cache is not filed, e.g. server restart
                this.loadConnectors(processDefinitionId);
                sConnectorImplementationDescriptors = getConnectorImplementationsFromCacheService(processDefinitionId);
            }
        } catch (final SCacheException e) {
            // If cache name not found, ignore it.
        }
        return sConnectorImplementationDescriptors;
    }

    private List<SConnectorImplementationDescriptor> getConnectorImplementationsFromCacheService(final long processDefinitionId)
            throws SCacheException, SConnectorException {
        List<SConnectorImplementationDescriptor> sConnectorImplementationDescriptors;
        sConnectorImplementationDescriptors = new ArrayList<>();
        final List<?> cacheKeys = cacheService.getKeys(CONNECTOR_CACHE_NAME);
        if (cacheKeys.size() > 0) {
            for (final Object cacheKey : cacheKeys) {
                if (String.valueOf(cacheKey).startsWith(String.valueOf(processDefinitionId))) { // Is it needed?
                    SConnectorImplementationDescriptor connectorImplementationDescriptor = (SConnectorImplementationDescriptor) cacheService.get(
                            CONNECTOR_CACHE_NAME, cacheKey);
                    if (!isGoodImplementation(connectorImplementationDescriptor)) {
                        this.loadConnectors(processDefinitionId);
                        connectorImplementationDescriptor = (SConnectorImplementationDescriptor) cacheService.get(CONNECTOR_CACHE_NAME, cacheKey);
                    }
                    sConnectorImplementationDescriptors.add(connectorImplementationDescriptor);
                }
            }
        }
        return sConnectorImplementationDescriptors;
    }

    /**
     * @param connectorImplementationDescriptor check the implementation has all required properties or not
     * @return
     */
    private boolean isGoodImplementation(final SConnectorImplementationDescriptor connectorImplementationDescriptor) {
        return connectorImplementationDescriptor != null && connectorImplementationDescriptor.getImplementationClassName() != null
                && connectorImplementationDescriptor.getId() != null && connectorImplementationDescriptor.getVersion() != null
                && connectorImplementationDescriptor.getDefinitionId() != null && connectorImplementationDescriptor.getDefinitionVersion() != null;
    }

    @Override
    public SConnectorImplementationDescriptor getConnectorImplementation(final long processDefinitionId, final String connectorId,
            final String connectorVersion) throws SConnectorException {
        SConnectorImplementationDescriptor connectorImplementationDescriptor;
        try {
            final String connectorImplementationNameInCache = buildConnectorImplementationKey(processDefinitionId, connectorId, connectorVersion);
            connectorImplementationDescriptor = (SConnectorImplementationDescriptor) cacheService.get(CONNECTOR_CACHE_NAME, connectorImplementationNameInCache);
            if (connectorImplementationDescriptor == null) {
                /*
                 * Maybe connector was out of cache
                 * We try to reload connector before throwing an exception
                 */
                loadConnectors(processDefinitionId);
                connectorImplementationDescriptor = (SConnectorImplementationDescriptor) cacheService.get(CONNECTOR_CACHE_NAME,
                        connectorImplementationNameInCache);
                if (connectorImplementationDescriptor == null) {
                    throw new SConnectorException("Connector implementation not found with id = " + connectorId + " and version = " + connectorVersion
                            + " in process + " + processDefinitionId);
                }
            }
        } catch (final SCacheException e) {
            throw new SConnectorException(e);
        }
        return connectorImplementationDescriptor;
    }

    @Override
    public List<SBARResource> getConnectorImplementations(long processDefinitionId, int from, int numberOfElements) throws SBonitaReadException {
        return processResourcesService.get(processDefinitionId, BARResourceType.CONNECTOR, from, numberOfElements);
    }

    @Override
    public void addConnectorImplementation(Long processDefinitionId, String name, byte[] content) throws SRecorderException {
        processResourcesService.add(processDefinitionId, name, BARResourceType.CONNECTOR, content);
    }

    @Override
    public void removeConnectorImplementations(long processDefinitionId) throws SBonitaReadException, SRecorderException {
        processResourcesService.removeAll(processDefinitionId, BARResourceType.CONNECTOR);
    }
}
