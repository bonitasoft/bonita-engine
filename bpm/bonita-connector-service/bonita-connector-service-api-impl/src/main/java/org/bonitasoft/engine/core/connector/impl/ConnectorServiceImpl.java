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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.bonitasoft.engine.builder.BuilderFactory;
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
import org.bonitasoft.engine.core.connector.parser.ConnectorImplementationBinding;
import org.bonitasoft.engine.core.connector.parser.JarDependenciesBinding;
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
import org.bonitasoft.engine.dependency.model.builder.SDependencyBuilderFactory;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.bonitasoft.engine.tracking.TimeTracker;
import org.bonitasoft.engine.tracking.TimeTrackerRecords;
import org.bonitasoft.engine.xml.ElementBinding;
import org.bonitasoft.engine.xml.Parser;
import org.bonitasoft.engine.xml.ParserFactory;
import org.bonitasoft.engine.xml.SXMLParseException;

/**
 * @author Baptiste Mesta
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class ConnectorServiceImpl implements ConnectorService {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static final String IMPLEMENTATION_EXT = ".impl";

    protected static final String CONNECTOR_CACHE_NAME = "CONNECTOR";

    private final Parser parser;

    private final CacheService cacheService;

    private final ConnectorExecutor connectorExecutor;

    private final ReadSessionAccessor sessionAccessor;

    private final ExpressionResolverService expressionResolverService;

    private final OperationService operationService;

    private final DependencyService dependencyService;

    private final TechnicalLoggerService logger;

    private final TimeTracker timeTracker;

    public ConnectorServiceImpl(final CacheService cacheService, final ConnectorExecutor connectorExecutor, final ParserFactory parserFactory,
            final ReadSessionAccessor sessionAccessor, final ExpressionResolverService expressionResolverService, final OperationService operationService,
            final DependencyService dependencyService, final TechnicalLoggerService logger, final TimeTracker timeTracker) {
        this.cacheService = cacheService;
        this.connectorExecutor = connectorExecutor;
        this.sessionAccessor = sessionAccessor;
        this.expressionResolverService = expressionResolverService;
        final List<Class<? extends ElementBinding>> bindings = new ArrayList<Class<? extends ElementBinding>>();
        bindings.add(ConnectorImplementationBinding.class);
        bindings.add(JarDependenciesBinding.class);
        parser = parserFactory.createParser(bindings);
        this.operationService = operationService;
        this.dependencyService = dependencyService;
        this.logger = logger;
        this.timeTracker = timeTracker;
    }

    @Override
    public ConnectorResult executeConnector(final long rootDefinitionId, final SConnectorInstance sConnectorInstance, final ClassLoader classLoader,
            final Map<String, Object> inputParameters) throws SConnectorException {
        final ConnectorResult connectorResult;
        try {
            final String tenantId = String.valueOf(sessionAccessor.getTenantId());
            SConnectorImplementationDescriptor descriptor = getImplementation(rootDefinitionId, tenantId, sConnectorInstance.getConnectorId(),
                    sConnectorInstance.getVersion());
            if (descriptor == null) {
                loadConnectors(rootDefinitionId, Integer.valueOf(tenantId));
                descriptor = getImplementation(rootDefinitionId, tenantId, sConnectorInstance.getConnectorId(), sConnectorInstance.getVersion());
                if (descriptor == null) {
                    throw new SConnectorException("There is no implementation found for the connector " + sConnectorInstance.getConnectorId()
                            + " with version " + sConnectorInstance.getVersion());
                }
            }
            final String implementationClassName = descriptor.getImplementationClassName();
            connectorResult = executeConnectorInClassloader(implementationClassName, classLoader, inputParameters);
        } catch (final SCacheException e) {
            throw new SConnectorException(e);
        } catch (final STenantIdNotSetException e) {
            throw new SConnectorException(e);
        }
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
            final String message = "Executed connector " + buildConnectorContextMessage(sConnectorInstance)
                    + buildConnectorInputMessage(inputParameters);
            logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, message);
        }
        return connectorResult;
    }

    /**
     * Build the log message using the connector instance's context (name, version, connector id, connector instance id, container type, container id)
     *
     * @param conectorInstance
     * @return the log message built using the connector instance's context
     */
    private static String buildConnectorContextMessage(final SConnectorInstance conectorInstance) {
        final StringBuilder stb = new StringBuilder();
        stb.append(" [name: <");
        stb.append(conectorInstance.getName());
        stb.append(">, version: <");
        stb.append(conectorInstance.getVersion());
        stb.append(">, connector id: <");
        stb.append(conectorInstance.getConnectorId());
        stb.append(">, connector instance id: <");
        stb.append(conectorInstance.getId());
        stb.append(">, container type: <");
        stb.append(conectorInstance.getContainerType());
        stb.append(">, container id: <");
        stb.append(conectorInstance.getContainerId());
        stb.append(">, activation event: <");
        stb.append(conectorInstance.getActivationEvent());
        stb.append(">]");
        return stb.toString();
    }

    private static String buildConnectorInputMessage(final Map<String, Object> inputParameters) {
        final StringBuilder stb = new StringBuilder();
        if (inputParameters != null && !inputParameters.isEmpty()) {
            stb.append(LINE_SEPARATOR);
            stb.append("Inputs: ");
            stb.append(LINE_SEPARATOR);
            final Set<String> inputNames = inputParameters.keySet();
            for (final String inputName : inputNames) {
                stb.append("    <" + inputName + "> : <" + inputParameters.get(inputName) + ">");
                stb.append(LINE_SEPARATOR);
            }
        }
        return stb.toString();
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
                final StringBuilder desc = new StringBuilder();
                desc.append("ConnectorResult: ");
                desc.append(result);
                timeTracker.track(TimeTrackerRecords.EXECUTE_CONNECTOR_OUTPUT_OPERATIONS, desc.toString(), endTime - startTime);
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

    private SConnectorImplementationDescriptor getImplementation(final long rootDefinitionId, final String tenantId, final String connectorId,
            final String version) throws SConnectorException, SCacheException {
        SConnectorImplementationDescriptor descriptor;
        try {
            final String key = buildConnectorImplementationKey(rootDefinitionId, connectorId, version);

            descriptor = (SConnectorImplementationDescriptor) cacheService.get(CONNECTOR_CACHE_NAME, key);
            if (descriptor == null) {
                // No value in cache : reload connector to ensure the cache stores all connectors for the current process
                loadConnectors(rootDefinitionId, Long.parseLong(tenantId));
                descriptor = (SConnectorImplementationDescriptor) cacheService.get(CONNECTOR_CACHE_NAME, key);
            }
        } catch (final NumberFormatException e) {
            throw new SConnectorException(e);
        } catch (final SCacheException e) {
            throw e;
        }
        return descriptor;
    }

    private void storeImplementation(final long processDefinitionId, final SConnectorImplementationDescriptor connectorImplementation) throws SCacheException {
        final String key = buildConnectorImplementationKey(processDefinitionId, connectorImplementation.getDefinitionId(),
                connectorImplementation.getDefinitionVersion());
        cacheService.store(CONNECTOR_CACHE_NAME, key, connectorImplementation);
    }

    protected String buildConnectorImplementationKey(final long rootDefinitionId, final String connectorId, final String version) {
        return new StringBuilder()
        .append(rootDefinitionId)
        .append(":")
        .append(connectorId)
        .append("-")
        .append(version)
        .toString();
    }

    @Override
    public ConnectorResult executeMutipleEvaluation(final long processDefinitionId, final String connectorDefinitionId,
            final String connectorDefinitionVersion, final Map<String, SExpression> connectorInputParameters,
            final Map<String, Map<String, Serializable>> inputValues, final ClassLoader classLoader, final SExpressionContext sexpContext)
                    throws SConnectorException {
        try {
            final SConnectorImplementationDescriptor implementation = getImplementation(processDefinitionId, String.valueOf(sessionAccessor.getTenantId()),
                    connectorDefinitionId, connectorDefinitionVersion);
            if (implementation == null) {
                throw new SConnectorException("Can not find implementation for connector(definitionId = " + connectorDefinitionId + ", definitionVersion = "
                        + connectorDefinitionVersion + ") for process:" + processDefinitionId);
            }
            final String implementationClassName = implementation.getImplementationClassName();

            final Map<String, Object> inputParameters = evaluateInputParameters(implementation.getDefinitionId(), connectorInputParameters, sexpContext,
                    inputValues);
            final ConnectorResult connectorResult = executeConnectorInClassloader(implementationClassName, classLoader, inputParameters);

            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "Executed connector <" + implementation.getImplementationClassName()
                        + "> with definition id <" + implementation.getDefinitionId() + ">, version <" + implementation.getDefinitionVersion()
                        + ">, and inputs :");
                if (inputParameters != null) {
                    final Set<String> inputNames = inputParameters.keySet();
                    for (final String inputName : inputNames) {
                        logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "    <" + inputName + "> : <" + inputParameters.get(inputName) + ">");
                    }
                }
            }
            return connectorResult;
        } catch (final SConnectorException e) {
            throw e;
        } catch (final SBonitaException e) {
            throw new SConnectorException(e);
        }
    }

    private ConnectorResult executeConnectorInClassloader(final String implementationClassName, final ClassLoader classLoader,
            final Map<String, Object> inputParameters) throws SConnectorException {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            final Connector connector = (Connector) Class.forName(implementationClassName, true, classLoader).newInstance();
            final SConnectorAdapter sConnectorAdapter = new SConnectorAdapter(connector);
            return new ConnectorResult(connector, connectorExecutor.execute(sConnectorAdapter, inputParameters, classLoader));
        } catch (final ClassNotFoundException e) {
            throw new SConnectorException(implementationClassName + " can not be found.", e);
        } catch (final InstantiationException e) {
            throw new SConnectorException(implementationClassName + " can not be instantiated.", e);
        } catch (final IllegalAccessException e) {
            throw new SConnectorException(e);
        } catch (final org.bonitasoft.engine.connector.exception.SConnectorException e) {
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
        final Map<String, Object> inputParameters = new HashMap<String, Object>(parameters.size());
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
    public boolean loadConnectors(final SProcessDefinition sDefinition, final long tenantId) throws SConnectorException {
        return loadConnectors(sDefinition.getId(), tenantId);
    }

    protected boolean loadConnectors(final long processDefinitionId, final long tenantId) throws SConnectorException {
        boolean resolved = true;
        try {
            final Map<String, byte[]> connectorFiles = BonitaHomeServer.getInstance().getConnectorFiles(tenantId, processDefinitionId);

            if (connectorFiles != null && connectorFiles.size() > 0) {
                final Pattern pattern = Pattern.compile("^.*\\" + IMPLEMENTATION_EXT + "$");
                for (final Map.Entry<String, byte[]> resource : connectorFiles.entrySet()) {
                    final String name = resource.getKey();
                    if (pattern.matcher(name).matches()) {
                        SConnectorImplementationDescriptor connectorImplementation;
                        try {
                            final Object objectFromXML = parser.getObjectFromXML(resource.getValue());
                            if (objectFromXML == null) {
                                throw new SConnectorException("Can not parse ConnectorImplementation XML. The file name is <" + name + ">.");
                            }
                            // check dependencies in the bar
                            connectorImplementation = (SConnectorImplementationDescriptor) objectFromXML;
                            storeImplementation(processDefinitionId, connectorImplementation);
                        } catch (final IOException e) {
                            throw new SConnectorException("Can not load ConnectorImplementation XML. The file name is <" + name + ">.", e);
                        } catch (final SXMLParseException e) {
                            throw new SConnectorException("Can not load ConnectorImplementation XML. The file name is <" + name + ">.", e);
                        } catch (final SCacheException e) {
                            throw new SConnectorException("Unable to cache the connector implementation " + name + ".", e);
                        }
                        // TODO parse the definitions to ensure all connectors are loaded
                        resolved = true;
                    }
                }
            }
        } catch (final BonitaHomeNotSetException e) {
            throw new SConnectorException("Bonita home is not set !!", e);
        } catch (IOException e) {
            throw new SConnectorException(e);
        }
        return resolved;
    }

    @Override
    public void setConnectorImplementation(final SProcessDefinition sProcessDefinition, final long tenantId, final String connectorId,
            final String connectorVersion, final byte[] connectorImplementationArchive) throws SConnectorException, SInvalidConnectorImplementationException {
        // store file in file system.
        replaceConnectorImpl(sProcessDefinition, tenantId, connectorImplementationArchive, connectorId, connectorVersion);
        // reload cache
        reLoadConnectors(sProcessDefinition, tenantId, connectorId, connectorVersion);
    }

    private void replaceConnectorImpl(final SProcessDefinition sDefinition, final long tenantId, final byte[] connectorImplementationArchive,
            final String connectorId, final String connectorVersion) throws SConnectorException, SInvalidConnectorImplementationException {
        checkConnectorImplementationIsValid(connectorImplementationArchive, connectorId, connectorVersion);
        unzipNewImplementation(sDefinition, tenantId, connectorImplementationArchive, connectorId, connectorVersion);
        try {
            deployNewDependencies(sDefinition.getId(), tenantId);
        } catch (final SDependencyException e) {
            throw new SConnectorException("Problem recording connector dependencies.", e);
        } catch (final BonitaHomeNotSetException e) {
            throw new SConnectorException(e);
        } catch (final IOException e) {
            throw new SConnectorException("Problem reading connector dependency jar files.", e);
        }
    }

    private void deployNewDependencies(final long processDefinitionId, final long tenantId) throws SDependencyException, IOException, BonitaHomeNotSetException {
        // deploy new ones from the filesystem (bonita-home):
        final Map<String, byte[]> classpath = BonitaHomeServer.getInstance().getProcessClasspath(tenantId, processDefinitionId);
        final ArrayList<SDependency> dependencies = new ArrayList<SDependency>();
        for (final Map.Entry<String, byte[]> file : classpath.entrySet()) {
            final String name = file.getKey();
            final SDependency sDependency = BuilderFactory.get(SDependencyBuilderFactory.class)
                        .createNewInstance(name, processDefinitionId, ScopeType.PROCESS, name + ".jar", file.getValue()).done();
            dependencies.add(sDependency);
        }
        dependencyService.updateDependenciesOfArtifact(processDefinitionId, ScopeType.PROCESS, dependencies);
    }

    protected void checkConnectorImplementationIsValid(final byte[] connectorImplementationArchive, final String connectorId, final String connectorVersion)
            throws SConnectorException, SInvalidConnectorImplementationException {
        ZipInputStream zipInputstream = null;
        boolean isClosed = false;
        try {
            zipInputstream = new ZipInputStream(new ByteArrayInputStream(connectorImplementationArchive));
            ZipEntry zipEntry = zipInputstream.getNextEntry();
            if (zipEntry == null) {
                throw new SInvalidConnectorImplementationException("the zip is empty or is not a valid zip file");
            }
            while (zipEntry != null) {
                final String entryName = zipEntry.getName();
                if (entryName.endsWith(".impl")) {
                    final SConnectorImplementationDescriptor connectorImplementationDescriptor = getConnectorImplementationDescriptor(IOUtil.getBytes(zipInputstream));
                    if (!connectorImplementationDescriptor.getDefinitionId().equals(connectorId)
                            || !connectorImplementationDescriptor.getDefinitionVersion().equals(connectorVersion)) {
                        throw new SInvalidConnectorImplementationException("The connector must implement the connectorDefinition with id = <" + connectorId
                                + "> and version = <" + connectorVersion + ">.", connectorImplementationDescriptor);
                    }
                    isClosed = true;
                    // stream already closed by the parser
                    return;
                }
                zipInputstream.closeEntry();
                zipEntry = zipInputstream.getNextEntry();
            }
            throw new SInvalidConnectorImplementationException("There no Implementation file is the zip");
        } catch (final IOException e) {
            throw new SConnectorException(e);
        } finally {
            try {
                if (zipInputstream != null && !isClosed) {
                    zipInputstream.close();
                }
            } catch (final IOException e) {
                throw new SConnectorException(e);
            }
        }
    }

    private void unzipNewImplementation(final SProcessDefinition sDefinition, final long tenantId, final byte[] connectorImplementationArchive,
            final String connectorId, final String connectorVersion) throws SInvalidConnectorImplementationException {
        ZipInputStream zipInputstream = null;
        try {

            // First delete the old implementation before trying to unzip the new one:
            deleteOldImplementation(tenantId, sDefinition.getId(), connectorId, connectorVersion);

            zipInputstream = new ZipInputStream(new ByteArrayInputStream(connectorImplementationArchive));
            ZipEntry zipEntry = zipInputstream.getNextEntry();
            while (zipEntry != null) {
                String entryName = zipEntry.getName();
                if (entryName.endsWith(".jar")) {
                    final int startIndex = Math.max(0, entryName.lastIndexOf('/'));
                    entryName = entryName.substring(startIndex);
                } else {
                    entryName = entryName.replace('/', File.separatorChar);
                    entryName = entryName.replace('\\', File.separatorChar);
                }
                final File newFile = new File(entryName);
                // if File already exists and is any other file than .impl, then skip it, because we already deleted the old implementation jars, so better not
                // overwrite existing common global process jar files:
                if (newFile.exists() && !entryName.endsWith(".impl")) {
                    zipEntry = zipInputstream.getNextEntry();
                    continue;
                }
                if (zipEntry.isDirectory()) {
                    if (!newFile.mkdirs()) {
                        break;
                    }
                    zipEntry = zipInputstream.getNextEntry();
                    continue;
                }
                final byte[] fileContent = IOUtil.getBytes(zipInputstream);
                if (entryName.endsWith(".jar")) {
                    BonitaHomeServer.getInstance().storeClasspathFile(tenantId, sDefinition.getId(), entryName, fileContent);
                } else {
                    BonitaHomeServer.getInstance().storeConnectorFile(tenantId, sDefinition.getId(), entryName, fileContent);
                }

                zipInputstream.closeEntry();
                zipEntry = zipInputstream.getNextEntry();
            }
        } catch (final IOException e) {
            throw new SInvalidConnectorImplementationException(e);
        } catch (final BonitaHomeNotSetException e) {
            throw new SInvalidConnectorImplementationException(e);
        } finally {
            try {
                if (zipInputstream != null) {
                    zipInputstream.close();
                }
            } catch (final IOException e) {
                throw new SInvalidConnectorImplementationException(e);
            }
        }
    }

    private SConnectorImplementationDescriptor getConnectorImplementationDescriptor(final byte[] bytes) throws SInvalidConnectorImplementationException {
        try {
            final Object objectFromXML = parser.getObjectFromXML(bytes);
            final SConnectorImplementationDescriptor connectorImplementation = (SConnectorImplementationDescriptor) objectFromXML;
            if (connectorImplementation == null) {
                throw new SInvalidConnectorImplementationException("Can not parse ConnectorImplementation XML.");
            }
            return connectorImplementation;
        } catch (final IOException e) {
            throw new SInvalidConnectorImplementationException("Can not load ConnectorImplementation XML.", e);
        } catch (final SXMLParseException e) {
            throw new SInvalidConnectorImplementationException("Can not load ConnectorImplementation XML.", e);
        }
    }

    private void deleteOldImplementation(final long tenantId, final long processId, final String connectorId, final String connectorVersion)
            throws SInvalidConnectorImplementationException, BonitaHomeNotSetException, IOException {
        final Map<String, byte[]> listFiles = BonitaHomeServer.getInstance().getConnectorFiles(tenantId, processId);
        final Pattern pattern = Pattern.compile("^.*\\" + IMPLEMENTATION_EXT + "$");
        List<String> jarFileNames = null;
        // delete .impl file for the specified connector
        for (final Entry<String, byte[]> resource : listFiles.entrySet()) {
            final String name = resource.getKey();
            if (pattern.matcher(name).matches()) {
                final SConnectorImplementationDescriptor connectorImplementation = getConnectorImplementationDescriptor(resource.getValue());
                if (connectorId.equals(connectorImplementation.getDefinitionId()) && connectorVersion.equals(connectorImplementation.getDefinitionVersion())) {
                    BonitaHomeServer.getInstance().deleteConnectorFile(tenantId, processId, name);
                    jarFileNames = connectorImplementation.getJarDependencies().getDependencies();
                    break;
                }
            }
        }

        // delete the .jar files for the specified connector
        if (jarFileNames != null) {
            BonitaHomeServer.getInstance().deleteClasspathFiles(tenantId, processId, jarFileNames.toArray(new String[jarFileNames.size()]));
        }
    }

    private void reLoadConnectors(final SProcessDefinition sProcessDefinition, final long tenantId, final String connectorId, final String connectorVersion)
            throws SConnectorException {
        final String connectorKey = buildConnectorImplementationKey(sProcessDefinition.getId(), connectorId, connectorVersion);
        try {
            cacheService.remove(CONNECTOR_CACHE_NAME, connectorKey);
            // re_load connectors
            loadConnectors(sProcessDefinition, tenantId);
        } catch (final SCacheException e) {
            throw new SConnectorException(e);
        }
    }

    @Override
    public Long getNumberOfConnectorImplementations(final long processDefinitionId, final long tenantId) throws SConnectorException {
        return Long.valueOf(getAllConnectorImplementations(processDefinitionId, tenantId).size());
    }

    @Override
    public List<SConnectorImplementationDescriptor> getConnectorImplementations(final long processDefinitionId, final long tenantId, final int fromIndex,
            final int numberPerPage, final String field, final OrderByType order) throws SConnectorException {
        final List<SConnectorImplementationDescriptor> sConnectorImplementationDescriptors = getAllConnectorImplementations(processDefinitionId, tenantId);
        if (sConnectorImplementationDescriptors != null && sConnectorImplementationDescriptors.size() > 0) {
            // pagination
            if (sConnectorImplementationDescriptors.size() <= fromIndex) {
                throw new SConnectorException("page out of range excepton. Total size is <" + sConnectorImplementationDescriptors.size()
                        + ">, but from index is <" + fromIndex + ">");
            }
            // set the comparison field
            SConnectorImplementationDescriptor.comparedFiled = field;
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
     * @param tenantId
     * @return
     * @throws SConnectorException
     */
    private List<SConnectorImplementationDescriptor> getAllConnectorImplementations(final long processDefinitionId, final long tenantId)
            throws SConnectorException {
        // get all connector implementations for processDefinitionId
        List<SConnectorImplementationDescriptor> sConnectorImplementationDescriptors = null;
        try {
            final int size = cacheService.getCacheSize(CONNECTOR_CACHE_NAME);
            // reload connectors if connector cache size is 0;
            if (size == 0) {
                this.loadConnectors(processDefinitionId, tenantId);
            }
            sConnectorImplementationDescriptors = getConnectorImplementationsFromCacheService(processDefinitionId, tenantId);
            if (sConnectorImplementationDescriptors.isEmpty()) {
                // reload connectors if cache is not filed, e.g. server restart
                this.loadConnectors(processDefinitionId, tenantId);
                sConnectorImplementationDescriptors = getConnectorImplementationsFromCacheService(processDefinitionId, tenantId);
            }
        } catch (final SCacheException e) {
            // If cache name not found, ignore it.
        }
        return sConnectorImplementationDescriptors;
    }

    private List<SConnectorImplementationDescriptor> getConnectorImplementationsFromCacheService(final long processDefinitionId, final long tenantId)
            throws SCacheException, SConnectorException {
        List<SConnectorImplementationDescriptor> sConnectorImplementationDescriptors;
        sConnectorImplementationDescriptors = new ArrayList<SConnectorImplementationDescriptor>();
        final List<?> cacheKeys = cacheService.getKeys(CONNECTOR_CACHE_NAME);
        if (cacheKeys.size() > 0) {
            for (final Object cacheKey : cacheKeys) {
                if (String.valueOf(cacheKey).startsWith(String.valueOf(processDefinitionId))) { // Is it needed?
                    SConnectorImplementationDescriptor connectorImplementationDescriptor = (SConnectorImplementationDescriptor) cacheService.get(
                            CONNECTOR_CACHE_NAME, cacheKey);
                    if (!isGoodImplementation(connectorImplementationDescriptor)) {
                        this.loadConnectors(processDefinitionId, tenantId);
                        connectorImplementationDescriptor = (SConnectorImplementationDescriptor) cacheService.get(CONNECTOR_CACHE_NAME, cacheKey);
                    }
                    sConnectorImplementationDescriptors.add(connectorImplementationDescriptor);
                }
            }
        }
        return sConnectorImplementationDescriptors;
    }

    /**
     * @param connectorImplementationDescriptor
     *            check the implementation has all required properties or not
     * @return
     */
    private boolean isGoodImplementation(final SConnectorImplementationDescriptor connectorImplementationDescriptor) {
        return connectorImplementationDescriptor != null && connectorImplementationDescriptor.getImplementationClassName() != null
                && connectorImplementationDescriptor.getId() != null && connectorImplementationDescriptor.getVersion() != null
                && connectorImplementationDescriptor.getDefinitionId() != null && connectorImplementationDescriptor.getDefinitionVersion() != null;
    }

    @Override
    public SConnectorImplementationDescriptor getConnectorImplementation(final long processDefinitionId, final String connectorId,
            final String connectorVersion, final long tenantId) throws SConnectorException {
        SConnectorImplementationDescriptor connectorImplementationDescriptor;
        try {
            final String connectorImplementationNameInCache = buildConnectorImplementationKey(processDefinitionId, connectorId, connectorVersion);
            connectorImplementationDescriptor = (SConnectorImplementationDescriptor) cacheService.get(CONNECTOR_CACHE_NAME, connectorImplementationNameInCache);
            if (connectorImplementationDescriptor == null) {
                /*
                 * Maybe connector was out of cache
                 * We try to reload connector before throwing an exception
                 */
                loadConnectors(processDefinitionId, tenantId);
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

}
