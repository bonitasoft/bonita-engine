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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.connector.ConnectorExecutor;
import org.bonitasoft.engine.connector.exception.SConnectorException;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.filter.FilterResult;
import org.bonitasoft.engine.core.filter.UserFilterImplementationDescriptor;
import org.bonitasoft.engine.core.filter.UserFilterService;
import org.bonitasoft.engine.core.filter.exception.SUserFilterExecutionException;
import org.bonitasoft.engine.core.filter.exception.SUserFilterLoadingException;
import org.bonitasoft.engine.core.process.definition.model.SUserFilterDefinition;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.filter.UserFilter;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.xml.ElementBinding;
import org.bonitasoft.engine.xml.Parser;
import org.bonitasoft.engine.xml.ParserFactory;
import org.bonitasoft.engine.xml.SXMLParseException;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class UserFilterServiceImpl implements UserFilterService {

    private static final String FILTER_CACHE_NAME = "USER_FILTER";

    private final ConnectorExecutor connectorExecutor;

    private final CacheService cacheService;

    private final ReadSessionAccessor sessionAccessor;

    private final ExpressionResolverService expressionResolverService;

    private final Parser parser;

    private final TechnicalLoggerService logger;

    public UserFilterServiceImpl(final ConnectorExecutor connectorExecutor, final CacheService cacheService, final ReadSessionAccessor sessionAccessor,
            final ExpressionResolverService expressionResolverService, final ParserFactory parserFactory, final TechnicalLoggerService logger) {
        super();
        this.connectorExecutor = connectorExecutor;
        this.cacheService = cacheService;
        this.sessionAccessor = sessionAccessor;
        this.expressionResolverService = expressionResolverService;
        this.logger = logger;
        final List<Class<? extends ElementBinding>> bindings = new ArrayList<Class<? extends ElementBinding>>(2);
        bindings.add(JarDependenciesBinding.class);
        bindings.add(UserFilterImplementationBinding.class);
        parser = parserFactory.createParser(bindings);
    }

    @Override
    public FilterResult executeFilter(final long processDefinitionId, final SUserFilterDefinition sUserFilterDefinition, final Map<String, SExpression> inputs,
            final ClassLoader classLoader, final SExpressionContext expressionContext, final String actorName) throws SUserFilterExecutionException {
        final FilterResult filterResult;
        try {
            UserFilterImplementationDescriptor descriptor = getDescriptor(processDefinitionId, sUserFilterDefinition);
            if (descriptor == null) {
                final String tenantId = String.valueOf(sessionAccessor.getTenantId());
                loadUserFilters(processDefinitionId, Long.valueOf(tenantId));
                descriptor = getDescriptor(processDefinitionId, sUserFilterDefinition);
                if (descriptor == null) {
                    throw new SUserFilterExecutionException("unable to load descriptor for filter " + sUserFilterDefinition.getUserFilterId());
                }
            }
            final String implementationClassName = descriptor.getImplementationClassName();
            filterResult = executeFilterInClassloader(implementationClassName, inputs, classLoader, expressionContext, actorName);
        } catch (final SConnectorException e) {
            throw new SUserFilterExecutionException(e.getCause());// Usergit chec FilterException wrapped in a connectorException
        } catch (final SUserFilterExecutionException e) {
            throw e;
        } catch (final Throwable e) {// catch throwable because we might have NoClassDefFound... see ENGINE-1333
            throw new SUserFilterExecutionException(e);
        }

        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
            final StringBuilder stb = new StringBuilder();
            stb.append("Executed userFilter [name: <");
            stb.append(sUserFilterDefinition.getName());
            stb.append(">, user filter id: <");
            stb.append(sUserFilterDefinition.getUserFilterId());
            stb.append(">, version: <");
            stb.append(sUserFilterDefinition.getVersion());
            stb.append(">] on flow node instance with id: <");
            stb.append(expressionContext.getContainerId());
            stb.append(">");

            logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, stb.toString());
        }
        return filterResult;
    }

    private UserFilterImplementationDescriptor getDescriptor(final long processDefinitionId, final SUserFilterDefinition sUserFilterDefinition)
            throws SCacheException {
        return (UserFilterImplementationDescriptor) cacheService.get(FILTER_CACHE_NAME,
                getUserFilterImplementationIdInCache(processDefinitionId, sUserFilterDefinition.getUserFilterId(), sUserFilterDefinition.getVersion()));
    }

    private String getUserFilterImplementationIdInCache(final long processDefinitionId, final String userFilterId, final String version) {
        return processDefinitionId + ':' + userFilterId + '-' + version;
    }

    private FilterResult executeFilterInClassloader(final String implementationClassName, final Map<String, SExpression> parameters,
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
            final HashMap<String, Object> inputParameters = new HashMap<String, Object>(parameters.size());
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
    public boolean loadUserFilters(final long processDefinitionId, final long tenantId) throws SUserFilterLoadingException {
        boolean resolved = true;// should return false if there is nothing loaded + something in the definition
        try {
            final File[] listFiles = BonitaHomeServer.getInstance().getUserFiltersFiles(tenantId, processDefinitionId);
            final Pattern pattern = Pattern.compile("^.*\\" + IMPLEMENTATION_EXT + "$");
            for (final File file : listFiles) {
                final String name = file.getName();
                if (pattern.matcher(name).matches()) {
                    UserFilterImplementationDescriptor userFilterImplementationDescriptor = null;
                    try {
                        final Object objectFromXML = parser.getObjectFromXML(file);
                        userFilterImplementationDescriptor = (UserFilterImplementationDescriptor) objectFromXML;
                        if (userFilterImplementationDescriptor == null) {
                            throw new SUserFilterLoadingException("Can not parse ConnectorImplementation XML. The file name is " + name);
                        }
                        cacheService.store(
                                FILTER_CACHE_NAME,
                                getUserFilterImplementationIdInCache(processDefinitionId, userFilterImplementationDescriptor.getDefinitionId(),
                                        userFilterImplementationDescriptor.getDefinitionVersion()), userFilterImplementationDescriptor);
                    } catch (final IOException e) {
                        throw new SUserFilterLoadingException("Can not load userFilterImplementationDescriptor XML. The file name is " + name, e);
                    } catch (final SXMLParseException e) {
                        throw new SUserFilterLoadingException("Can not load userFilterImplementationDescriptor XML. The file name is " + name, e);
                    } catch (final SCacheException e) {
                        throw new SUserFilterLoadingException("Unable to cache the user filter implementation" + name, e);
                    }
                    resolved = true;
                }
            }
        } catch (final BonitaHomeNotSetException e) {
            throw new BonitaRuntimeException("Bonita home is not set.", e);
        } catch (IOException e) {
            throw new BonitaRuntimeException(e);
        }
        return resolved;
    }

}
