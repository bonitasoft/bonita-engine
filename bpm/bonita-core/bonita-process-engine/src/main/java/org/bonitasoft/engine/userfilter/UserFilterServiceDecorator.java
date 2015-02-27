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
package org.bonitasoft.engine.userfilter;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.filter.FilterResult;
import org.bonitasoft.engine.core.filter.UserFilterService;
import org.bonitasoft.engine.core.filter.exception.SUserFilterExecutionException;
import org.bonitasoft.engine.core.filter.exception.SUserFilterLoadingException;
import org.bonitasoft.engine.core.process.definition.model.SUserFilterDefinition;
import org.bonitasoft.engine.expression.EngineConstantExpressionBuilder;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilder;

/**
 * This {@link UserFilterService} implementation injects, in method {@link #executeFilter(long, SUserFilterDefinition, Map, ClassLoader)} a new expression to
 * access the {@link APIAccessor} for User filters.
 * This new expression is referenced under the name 'apiAccessor'.
 * 
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class UserFilterServiceDecorator implements UserFilterService {

    private final UserFilterService userFilterService;

    /**
     * @param userFilterService
     *            the UserFilterService class that this class is decorating.
     * @param expressionbuilder
     *            the {@link SExpressionBuilder} used to decorate.
     */
    public UserFilterServiceDecorator(final UserFilterService userFilterService) {
        super();
        this.userFilterService = userFilterService;
    }

    /**
     * {@inheritDoc}. This implementation injects a new expression to access the {@link APIAccessor} for User filters.
     * This new expression is referenced under the name 'apiAccessor'.
     * 
     * @param actors
     */
    @Override
    public FilterResult executeFilter(final long processDefinitionId, final SUserFilterDefinition sUserFilterDefinition, final Map<String, SExpression> inputs,
            final ClassLoader classLoader, final SExpressionContext expressionContext, final String actorName) throws SUserFilterExecutionException {
        SExpression apiAccessorExpression;
        SExpression engineExecutionContext;
        apiAccessorExpression = EngineConstantExpressionBuilder.getConnectorAPIAccessorExpression();
        engineExecutionContext = EngineConstantExpressionBuilder.getEngineExecutionContext();
        final Map<String, SExpression> enrichedInputs = new HashMap<String, SExpression>(inputs);
        enrichedInputs.put("connectorApiAccessor", apiAccessorExpression);
        enrichedInputs.put("engineExecutionContext", engineExecutionContext);
        return userFilterService.executeFilter(processDefinitionId, sUserFilterDefinition, enrichedInputs, classLoader, expressionContext, actorName);
    }

    @Override
    public boolean loadUserFilters(final long processDefinitionId, final long tenantId) throws SUserFilterLoadingException {
        return userFilterService.loadUserFilters(processDefinitionId, tenantId);
    }

}
