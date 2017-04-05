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
package org.bonitasoft.engine.core.filter.impl;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.connector.ConnectorExecutor;
import org.bonitasoft.engine.connector.exception.SConnectorException;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.filter.FilterResult;
import org.bonitasoft.engine.core.filter.UserFilterService;
import org.bonitasoft.engine.core.filter.exception.SUserFilterExecutionException;
import org.bonitasoft.engine.core.filter.exception.SUserFilterLoadingException;
import org.bonitasoft.engine.core.filter.model.UserFilterImplementationDescriptor;
import org.bonitasoft.engine.core.process.definition.model.SUserFilterDefinition;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.filter.UserFilter;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.resources.BARResourceType;
import org.bonitasoft.engine.resources.ProcessResourcesService;
import org.bonitasoft.engine.resources.SBARResource;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class UserFilterServiceImpl implements UserFilterService {

    private static final String FILTER_CACHE_NAME = "USER_FILTER";

    private final ConnectorExecutor connectorExecutor;
    private final CacheService cacheService;
    private final ExpressionResolverService expressionResolverService;
    private final TechnicalLoggerService logger;
    private final ProcessResourcesService processResourcesService;
    private final JAXBContext jaxbContext;
    private final Schema schema;

    public UserFilterServiceImpl(final ConnectorExecutor connectorExecutor, final CacheService cacheService,
            final ExpressionResolverService expressionResolverService, final TechnicalLoggerService logger,
            ProcessResourcesService processResourcesService) {
        super();
        this.connectorExecutor = connectorExecutor;
        this.cacheService = cacheService;
        this.expressionResolverService = expressionResolverService;
        this.logger = logger;
        this.processResourcesService = processResourcesService;
        try {
            jaxbContext = JAXBContext.newInstance(UserFilterImplementationDescriptor.class);
            URL schemaURL = ConnectorService.class.getResource("/connectors-impl.xsd");
            final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schema = sf.newSchema(schemaURL);
        } catch (final Exception e) {
            throw new BonitaRuntimeException("Unable to load unmarshaller for connector implementation descriptor", e);
        }
    }

    @Override
    public FilterResult executeFilter(final long processDefinitionId, final SUserFilterDefinition sUserFilterDefinition, final Map<String, SExpression> inputs,
            final ClassLoader classLoader, final SExpressionContext expressionContext, final String actorName) throws SUserFilterExecutionException {
        final FilterResult filterResult;
        String implementationClassName = "";
        UserFilterImplementationDescriptor descriptor = null;
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(this.getClass(), TechnicalLogSeverity.DEBUG,
                    Thread.currentThread().toString() + "-[" + Thread.currentThread().getId() + "," + Thread.currentThread().getState() + "]");
        }
        try {
            descriptor = getDescriptor(processDefinitionId, sUserFilterDefinition);
            if (descriptor == null) {
                loadUserFilters(processDefinitionId);
                descriptor = getDescriptor(processDefinitionId, sUserFilterDefinition);
                if (descriptor == null) {
                    throw new SUserFilterExecutionException("unable to load descriptor for filter " + sUserFilterDefinition.getUserFilterId());
                }
            }
            implementationClassName = descriptor.getImplementationClassName();
            filterResult = executeFilterInClassloader(implementationClassName, inputs, classLoader, expressionContext, actorName);
        } catch (final SConnectorException e) {
            String dbgInfo = "";
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                dbgInfo = buildDebugMessage(processDefinitionId, sUserFilterDefinition, inputs, classLoader, expressionContext, actorName,
                        implementationClassName, descriptor);
            }
            if (e.getCause() != null) {
                throw new SUserFilterExecutionException(dbgInfo, e.getCause());
            } else {
                throw new SUserFilterExecutionException("SConnectorException: " + e.getMessage() + dbgInfo, e);
            }
        } catch (final SUserFilterExecutionException e) {
            throw e;
        } catch (final Throwable e) {// catch throwable because we might have NoClassDefFound... see ENGINE-1333
            throw new SUserFilterExecutionException(e);
        }

        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
            String stb = "Executed userFilter [name: <" +
                    sUserFilterDefinition.getName() +
                    ">, user filter id: <" +
                    sUserFilterDefinition.getUserFilterId() +
                    ">, version: <" +
                    sUserFilterDefinition.getVersion() +
                    ">] on flow node instance with id: <" +
                    expressionContext.getContainerId() +
                    ">";

            logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, stb);
        }
        return filterResult;
    }

    protected String buildDebugMessage(long processDefinitionId, SUserFilterDefinition sUserFilterDefinition, Map<String, SExpression> inputs,
            ClassLoader classLoader, SExpressionContext expressionContext, String actorName, String implementationClassName,
            UserFilterImplementationDescriptor descriptor) {
        final StringBuilder stb = new StringBuilder();
        stb.append(" Flow node instance id: <");
        stb.append(expressionContext.getContainerId());
        stb.append(">");
        stb.append("\n Current Thread ID : <");
        stb.append(Thread.currentThread().getId());
        stb.append(">, Current Thread State : <");
        stb.append(Thread.currentThread().getState());
        stb.append(">,\n ProcessDefinitionID : <");
        stb.append(processDefinitionId);
        stb.append(">, SUserFilterDefinition : <");
        stb.append(sUserFilterDefinition);
        stb.append(">, Inputs : <");
        stb.append(inputs.toString());
        stb.append(">, ClassLoader : <");
        stb.append(classLoader.toString());
        stb.append(">, ExpressionContext : <");
        stb.append(expressionContext);
        stb.append(">, ActorName : <");
        stb.append(actorName);
        stb.append(">, UserFilterImplementationDescriptor : <");
        stb.append(descriptor);
        stb.append(">, ImplementationClassName : <");
        stb.append(implementationClassName);
        stb.append(">");
        return stb.toString();
    }

    private UserFilterImplementationDescriptor getDescriptor(final long processDefinitionId, final SUserFilterDefinition sUserFilterDefinition)
            throws SCacheException {
        return (UserFilterImplementationDescriptor) cacheService.get(FILTER_CACHE_NAME,
                getUserFilterImplementationIdInCache(processDefinitionId, sUserFilterDefinition.getUserFilterId(), sUserFilterDefinition.getVersion()));
    }

    private String getUserFilterImplementationIdInCache(final long processDefinitionId, final String userFilterId, final String version) {
        return String.valueOf(processDefinitionId) + ":" + userFilterId + "-" + version;
    }

    protected FilterResult executeFilterInClassloader(final String implementationClassName, final Map<String, SExpression> parameters,
            final ClassLoader classLoader, final SExpressionContext expressionContext, final String actorName) throws InstantiationException,
            IllegalAccessException, ClassNotFoundException, SUserFilterExecutionException, SExpressionTypeUnknownException, SExpressionEvaluationException,
            SExpressionDependencyMissingException, SInvalidExpressionException, SConnectorException {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            final UserFilter filter = (UserFilter) Class.forName(implementationClassName, true, classLoader).newInstance();
            if (filter == null) {
                throw new SUserFilterExecutionException("Can not instantiate UserFilter " + implementationClassName + ". It is null.");
            }
            final SConnectorUserFilterAdapter sConnectorAdapter = new SConnectorUserFilterAdapter(filter, actorName);
            final HashMap<String, Object> inputParameters = new HashMap<>(parameters.size());
            for (final Entry<String, SExpression> input : parameters.entrySet()) {
                if (expressionContext != null) {
                    inputParameters.put(input.getKey(), expressionResolverService.evaluate(input.getValue(), expressionContext));
                } else {
                    inputParameters.put(input.getKey(), expressionResolverService.evaluate(input.getValue()));
                }
            }
            connectorExecutor.execute(sConnectorAdapter, inputParameters, classLoader);
            return new FilterResultImpl(sConnectorAdapter.getUserIds(), sConnectorAdapter.shouldAutoAssignTaskIfSingleResult());
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    @Override
    public void removeUserFilters(final long processDefinitionId) throws SBonitaReadException, SRecorderException {
        processResourcesService.removeAll(processDefinitionId, BARResourceType.USER_FILTER);
    }

    @Override
    public boolean loadUserFilters(final long processDefinitionId) throws SUserFilterLoadingException {
        String name = null;
        try {
            final List<SBARResource> listFiles = processResourcesService.get(processDefinitionId, BARResourceType.USER_FILTER, 0, 1000);//FIXME
            final Pattern pattern = Pattern.compile("^.*\\" + IMPLEMENTATION_EXT + "$");
            for (final SBARResource file : listFiles) {
                name = file.getName();
                if (pattern.matcher(name).matches()) {
                    UserFilterImplementationDescriptor userFilterImplementationDescriptor;
                    final Object objectFromXML = convert(file.getContent());
                    userFilterImplementationDescriptor = (UserFilterImplementationDescriptor) objectFromXML;
                    if (userFilterImplementationDescriptor == null) {
                        throw new SUserFilterLoadingException("Can not parse ConnectorImplementation XML. The file name is " + name);
                    }
                    cacheService.store(
                            FILTER_CACHE_NAME,
                            getUserFilterImplementationIdInCache(processDefinitionId, userFilterImplementationDescriptor.getDefinitionId(),
                                    userFilterImplementationDescriptor.getDefinitionVersion()),
                            userFilterImplementationDescriptor);
                }
            }
            return true;
        } catch (final IOException e) {
            throw new SUserFilterLoadingException("Cannot load userFilterImplementationDescriptor XML. The file name is " + name, e);
        } catch (final SCacheException e) {
            throw new SUserFilterLoadingException("Unable to cache the user filter implementation" + name, e);
        } catch (SBonitaReadException e) {
            throw new SUserFilterLoadingException("Unable to list the user filter implementations", e);
        }
    }

    private UserFilterImplementationDescriptor convert(byte[] content) throws IOException {
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(schema);
            String connectorImplementationFileContent = new String(content);
            connectorImplementationFileContent = connectorImplementationFileContent.replace("<connectorImplementation>",
                    "<implementation:connectorImplementation xmlns:implementation=\"http://www.bonitasoft.org/ns/connector/implementation/6.0\">");
            connectorImplementationFileContent = connectorImplementationFileContent.replace("</connectorImplementation>",
                    "</implementation:connectorImplementation>");
            return (UserFilterImplementationDescriptor) unmarshaller.unmarshal(new StringReader(connectorImplementationFileContent));
        } catch (final JAXBException e) {
            throw new IOException(e);
        }
    }

}
