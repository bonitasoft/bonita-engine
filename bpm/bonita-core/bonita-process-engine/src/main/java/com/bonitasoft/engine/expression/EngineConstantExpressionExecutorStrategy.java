/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package com.bonitasoft.engine.expression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.connector.EngineExecutionContext;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.bonitasoft.engine.expression.ExpressionConstantsResolver;
import org.bonitasoft.engine.expression.ExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;

import com.bonitasoft.engine.api.impl.APIAccessorExt;

/**
 * @author Matthieu Chaffotte
 */
public class EngineConstantExpressionExecutorStrategy implements ExpressionExecutorStrategy {

    private final ActivityInstanceService activityInstanceService;

    private final ProcessInstanceService processInstanceService;

    public EngineConstantExpressionExecutorStrategy(final ActivityInstanceService activityInstanceService, final ProcessInstanceService processInstanceService) {
        this.activityInstanceService = activityInstanceService;
        this.processInstanceService = processInstanceService;
    }

    @Override
    public Object evaluate(final SExpression expression, final Map<String, Object> dependencyValues, final Map<Integer, Object> resolvedExpressions)
            throws SExpressionEvaluationException, SExpressionDependencyMissingException {
        ExpressionConstants expressionConstant = ExpressionConstantsResolver.getExpressionConstantsFromName(expression.getContent());
        if (expressionConstant == null) {
            expressionConstant = ExpressionConstants.API_ACCESSOR;// just to make the expressionConstantsResolver load constants
            expressionConstant = ExpressionConstantsResolver.getExpressionConstantsFromName(expression.getContent());
        }
        switch (expressionConstant) {
            case API_ACCESSOR:
                return new APIAccessorExt();
            case ENGINE_EXECUTION_CONTEXT:
                return getFromContextOrEngineExecutionContext(expressionConstant, dependencyValues);
            case ACTIVITY_INSTANCE_ID:
                return getFromContextOrEngineExecutionContext(expressionConstant, dependencyValues);
            case PARENT_PROCESS_INSTANCE_ID:
                return getFromContextOrEngineExecutionContext(expressionConstant, dependencyValues);
            case PROCESS_DEFINITION_ID:
                return getFromContextOrEngineExecutionContext(expressionConstant, dependencyValues);
            case ROOT_PROCESS_INSTANCE_ID:
                return getFromContextOrEngineExecutionContext(expressionConstant, dependencyValues);
            default:
                return inContext(expressionConstant, dependencyValues);
        }
    }

    private Serializable getFromContextOrEngineExecutionContext(final ExpressionConstants expressionConstant, final Map<String, Object> dependencyValues)
            throws SExpressionEvaluationException {
        final Object object = dependencyValues.get(expressionConstant.getEngineName());
        if (object == null) {
            // try to get it from an already evaluated context
            final EngineExecutionContext context = (EngineExecutionContext) dependencyValues.get(ExpressionConstants.ENGINE_EXECUTION_CONTEXT);
            if (context != null) {
                return context.getExpressionConstant(expressionConstant);
            } else {
                // guess it
                final String containerType = (String) dependencyValues.get(SExpressionContext.containerTypeKey);
                final long containerId = (Long) dependencyValues.get(SExpressionContext.containerIdKey);
                if (ExpressionConstants.ENGINE_EXECUTION_CONTEXT.equals(expressionConstant)) {
                    return createContext(dependencyValues);
                } else if (dependencyValues.containsKey(SExpressionContext.containerTypeKey) && dependencyValues.containsKey(SExpressionContext.containerIdKey)) {
                    if (ExpressionConstants.PROCESS_DEFINITION_ID.equals(expressionConstant)) {
                        return (Serializable) dependencyValues.get(SExpressionContext.processDefinitionIdKey);
                    } else if (DataInstanceContainer.ACTIVITY_INSTANCE.toString().equals(containerType)) {
                        if (ExpressionConstants.ACTIVITY_INSTANCE_ID.equals(expressionConstant)) {
                            return containerId;
                        } else {
                            // get the activity and fill the others elements
                            try {
                                final SActivityInstance activityInstance = activityInstanceService.getActivityInstance(containerId);
                                dependencyValues.put(ExpressionConstants.PARENT_PROCESS_INSTANCE_ID.getEngineName(), activityInstance.getLogicalGroup(3));
                                dependencyValues.put(ExpressionConstants.ROOT_PROCESS_INSTANCE_ID.getEngineName(), activityInstance.getLogicalGroup(1));
                                final Serializable serializable = (Serializable) dependencyValues.get(expressionConstant.getEngineName());
                                return serializable == null ? -1 : serializable;
                            } catch (final SActivityReadException e) {
                                throw new SExpressionEvaluationException(
                                        "Error retrieving Activity instance while building EngineExecutionContext as EngineConstantExpression", e);
                            } catch (final SActivityInstanceNotFoundException e) {
                                throw new SExpressionEvaluationException(
                                        "Error retrieving Activity instance while building EngineExecutionContext as EngineConstantExpression", e);
                            }
                        }
                    } else {
                        if (ExpressionConstants.PARENT_PROCESS_INSTANCE_ID.equals(expressionConstant)) {
                            return containerId;
                        } else {
                            // get the process and fill the others elements
                            try {
                                final SProcessInstance processInstance = processInstanceService.getProcessInstance(containerId);
                                dependencyValues.put(ExpressionConstants.PARENT_PROCESS_INSTANCE_ID.getEngineName(), processInstance.getId());
                                dependencyValues.put(ExpressionConstants.ROOT_PROCESS_INSTANCE_ID.getEngineName(), processInstance.getRootProcessInstanceId());
                                final Serializable serializable = (Serializable) dependencyValues.get(expressionConstant.getEngineName());
                                return serializable == null ? -1 : serializable;
                            } catch (final SProcessInstanceNotFoundException e) {
                                throw new SExpressionEvaluationException(
                                        "Error retrieving process instance while building EngineExecutionContext as EngineConstantExpression", e);
                            } catch (final SProcessInstanceReadException e) {
                                throw new SExpressionEvaluationException(
                                        "Error retrieving process instance while building EngineExecutionContext as EngineConstantExpression", e);
                            }
                        }
                    }
                }
                return -1;// no container id and not processDefinition
            }
        } else {
            // we have it already evaluated
            return (Serializable) object;
        }
    }

    private Serializable inContext(final ExpressionConstants expressionConstant, final Map<String, Object> dependencyValues)
            throws SExpressionEvaluationException {
        final Object object = dependencyValues.get(expressionConstant.getEngineName());
        if (object == null) {
            throw new SExpressionEvaluationException("EngineConstantExpression not supported for: " + expressionConstant.getEngineName());
        } else {
            return (Serializable) object;
        }
    }

    private Serializable createContext(final Map<String, Object> dependencyValues) throws SExpressionEvaluationException {
        final EngineExecutionContext ctx = new EngineExecutionContext();
        if (dependencyValues.containsKey(SExpressionContext.containerTypeKey) && dependencyValues.containsKey(SExpressionContext.containerIdKey)) {
            final String containerType = (String) dependencyValues.get(SExpressionContext.containerTypeKey);
            final long containerId = (Long) dependencyValues.get(SExpressionContext.containerIdKey);
            if (DataInstanceContainer.ACTIVITY_INSTANCE.toString().equals(containerType)) {
                ctx.setActivityInstanceId(containerId);
                try {
                    final SActivityInstance activityInstance = activityInstanceService.getActivityInstance(containerId);
                    ctx.setParentProcessInstanceId(activityInstance.getLogicalGroup(3));
                    ctx.setRootProcessInstanceId(activityInstance.getLogicalGroup(1));
                } catch (final SActivityReadException e) {
                    throw new SExpressionEvaluationException(
                            "Error retrieving Activity instance while building EngineExecutionContext as EngineConstantExpression", e);
                } catch (final SActivityInstanceNotFoundException e) {
                    throw new SExpressionEvaluationException(
                            "Error retrieving Activity instance while building EngineExecutionContext as EngineConstantExpression", e);
                }
            } else if (DataInstanceContainer.PROCESS_INSTANCE.toString().equals(containerType)) {
                try {
                    final SProcessInstance processInstance = processInstanceService.getProcessInstance(containerId);
                    ctx.setParentProcessInstanceId(processInstance.getId());
                    ctx.setRootProcessInstanceId(processInstance.getRootProcessInstanceId());
                } catch (final SProcessInstanceNotFoundException e) {
                    throw new SExpressionEvaluationException(
                            "Error retrieving process instance while building EngineExecutionContext as EngineConstantExpression", e);
                } catch (final SProcessInstanceReadException e) {
                    throw new SExpressionEvaluationException(
                            "Error retrieving process instance while building EngineExecutionContext as EngineConstantExpression", e);
                }
            }
        }
        if (dependencyValues.containsKey(SExpressionContext.processDefinitionIdKey)) {
            ctx.setProcessDefinitionId((Long) dependencyValues.get(SExpressionContext.processDefinitionIdKey));
        }
        return ctx;
    }

    @Override
    public boolean validate(final String expressionContent) {
        return ExpressionConstantsResolver.getExpressionConstantsFromName(expressionContent) != null;
    }

    @Override
    public ExpressionKind getExpressionKind() {
        return KIND_ENGINE_CONSTANT;
    }

    @Override
    public List<Object> evaluate(final List<SExpression> expressions, final Map<String, Object> dependencyValues,
            final Map<Integer, Object> resolvedExpressions) throws SExpressionEvaluationException, SExpressionDependencyMissingException {
        final List<Object> results = new ArrayList<Object>();
        for (final SExpression sExpression : expressions) {
            results.add(evaluate(sExpression, dependencyValues, resolvedExpressions));
        }
        return results;
    }

    @Override
    public boolean mustPutEvaluatedExpressionInContext() {
        return true;
    }

}
