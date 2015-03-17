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
package org.bonitasoft.engine.expression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.impl.APIAccessorImpl;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.connector.ConnectorAPIAccessorImpl;
import org.bonitasoft.engine.connector.EngineExecutionContext;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SLoopActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class EngineConstantExpressionExecutorStrategy implements ExpressionExecutorStrategy {

    private final ActivityInstanceService activityInstanceService;

    private final ProcessInstanceService processInstanceService;

    private final SessionAccessor sessionAccessor;

    private final SessionService sessionService;

    public EngineConstantExpressionExecutorStrategy(final ActivityInstanceService activityInstanceService, final ProcessInstanceService processInstanceService,
            final SessionService sessionService, final SessionAccessor sessionAccessor) {
        this.activityInstanceService = activityInstanceService;
        this.processInstanceService = processInstanceService;
        this.sessionService = sessionService;
        this.sessionAccessor = sessionAccessor;
    }

    @Override
    public Serializable evaluate(final SExpression expression, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionEvaluationException {
        ExpressionConstants expressionConstant = ExpressionConstantsResolver.getExpressionConstantsFromName(expression.getContent());
        if (expressionConstant == null) {
            expressionConstant = ExpressionConstants.API_ACCESSOR;// just to make the expressionConstantsResolver load constants
            expressionConstant = ExpressionConstantsResolver.getExpressionConstantsFromName(expression.getContent());
        }
        final String expressionName = expression.getName();
        if (expressionConstant == null) {
            throw new SExpressionEvaluationException(expression.getContent() + " is not a valid Engine-provided variable", expressionName);
        }
        try {
            switch (expressionConstant) {
                case API_ACCESSOR:
                    return getApiAccessor();
                case CONNECTOR_API_ACCESSOR:
                    return getConnectorApiAccessor();
                case ENGINE_EXECUTION_CONTEXT:
                case ACTIVITY_INSTANCE_ID:
                case PROCESS_INSTANCE_ID:
                case ROOT_PROCESS_INSTANCE_ID:
                case PROCESS_DEFINITION_ID:
                case TASK_ASSIGNEE_ID:
                    return getFromContextOrEngineExecutionContext(expressionConstant, context, containerState);
                case LOGGED_USER_ID:
                    return getLoggedUserFromSession();
                case LOOP_COUNTER:
                    return getLoopCounter(context);
                default:
                    final Object object = context.get(expressionConstant.getEngineConstantName());
                    if (object == null) {
                        throw new SExpressionEvaluationException("EngineConstantExpression not supported for: " + expressionConstant.getEngineConstantName(),
                                expressionName);
                    }
                    return (Serializable) object;
            }
        } catch (final STenantIdNotSetException e) {
            throw new SExpressionEvaluationException(e, expressionName);
        } catch (final SSessionNotFoundException e) {
            throw new SExpressionEvaluationException(e, expressionName);
        } catch (final SProcessInstanceNotFoundException e) {
            throw new SExpressionEvaluationException("Error retrieving process instance while building EngineExecutionContext as EngineConstantExpression", e,
                    expressionName);
        } catch (final SProcessInstanceReadException e) {
            throw new SExpressionEvaluationException("Error retrieving process instance while building EngineExecutionContext as EngineConstantExpression", e,
                    expressionName);
        } catch (final SFlowNodeReadException e) {
            throw new SExpressionEvaluationException("Error retrieving flow node instance while building EngineExecutionContext as EngineConstantExpression",
                    e, expressionName);
        } catch (final SFlowNodeNotFoundException e) {
            throw new SExpressionEvaluationException("Error retrieving flow node instance while building EngineExecutionContext as EngineConstantExpression",
                    e, expressionName);
        } catch (final SActivityInstanceNotFoundException e) {
            throw new SExpressionEvaluationException("Error retrieving activity instance while building EngineExecutionContext as EngineConstantExpression", e,
                    expressionName);
        } catch (final SBonitaReadException e) {
            throw new SExpressionEvaluationException("Error while building EngineExecutionContext as EngineConstantExpression", e, expressionName);
        } catch (final SBonitaException e) {
            throw new SExpressionEvaluationException(e, expressionName);
        }
    }

    private Serializable getLoopCounter(Map<String, Object> context) throws SExpressionEvaluationException, SFlowNodeReadException, SFlowNodeNotFoundException {
        final String containerType = (String) context.get(SExpressionContext.CONTAINER_TYPE_KEY);
            final long containerId = (Long) context.get(SExpressionContext.CONTAINER_ID_KEY);
        if( DataInstanceContainer.ACTIVITY_INSTANCE.toString().equals(containerType)){
            SFlowNodeInstance flowNodeInstance = activityInstanceService.getFlowNodeInstance(containerId);
            if(flowNodeInstance instanceof SLoopActivityInstance){
                return flowNodeInstance.getLoopCounter();
            }
            SLoopActivityInstance loopActivityInstance = (SLoopActivityInstance) activityInstanceService.getFlowNodeInstance(flowNodeInstance.getParentActivityInstanceId());
            return loopActivityInstance.getLoopCounter();
        }
        throw new SExpressionEvaluationException("loopCounter is not available in this context","loopCounter");
    }

    protected APIAccessor getApiAccessor() {
        return new APIAccessorImpl();
    }

    protected APIAccessor getConnectorApiAccessor() throws STenantIdNotSetException {
        final long tenantId = sessionAccessor.getTenantId();
        return new ConnectorAPIAccessorImpl(tenantId);
    }

    private long getLoggedUserFromSession() throws SSessionNotFoundException {
        return sessionService.getLoggedUserFromSession(sessionAccessor);
    }

    private Serializable getFromContextOrEngineExecutionContext(final ExpressionConstants expressionConstant, final Map<String, Object> context,
            final ContainerState containerState) throws SBonitaException {
        final Object object = context.get(expressionConstant.getEngineConstantName());
        if (object == null) {
            // try to get it from an already evaluated context
            final EngineExecutionContext engineContext = (EngineExecutionContext) context.get(ExpressionConstants.ENGINE_EXECUTION_CONTEXT
                    .getEngineConstantName());
            if (engineContext != null) {
                return engineContext.getExpressionConstant(expressionConstant);
            }
            return evaluate(expressionConstant, context, containerState);
        }
        // we have it already evaluated
        return (Serializable) object;
    }

    private Serializable evaluate(final ExpressionConstants expressionConstant, final Map<String, Object> context,
            final ContainerState containerState) throws SBonitaException {
        // guess it
        if (ExpressionConstants.ENGINE_EXECUTION_CONTEXT.equals(expressionConstant)) {
            return createContext(context, containerState);
        } else if (context.containsKey(SExpressionContext.CONTAINER_TYPE_KEY) && context.containsKey(SExpressionContext.CONTAINER_ID_KEY)) {
            final String containerType = (String) context.get(SExpressionContext.CONTAINER_TYPE_KEY);
            final long containerId = (Long) context.get(SExpressionContext.CONTAINER_ID_KEY);
            if (ExpressionConstants.PROCESS_DEFINITION_ID.equals(expressionConstant)) {
                return (Serializable) context.get(SExpressionContext.PROCESS_DEFINITION_ID_KEY);
            } else if (DataInstanceContainer.ACTIVITY_INSTANCE.toString().equals(containerType)) {
                return evaluateUsingActivityInstanceContainer(expressionConstant, context, containerId);
            } else {
                return evaluateUsingProcessInstanceContainer(expressionConstant, context, containerId);
            }
        } else {
            return -1;// no container id and not processDefinition
        }
    }

    private Serializable evaluateUsingProcessInstanceContainer(final ExpressionConstants expressionConstant, final Map<String, Object> context,
            final long containerId) throws SProcessInstanceNotFoundException, SProcessInstanceReadException {
        if (ExpressionConstants.PROCESS_INSTANCE_ID.equals(expressionConstant)) {
            return containerId;
        } else if (ExpressionConstants.TASK_ASSIGNEE_ID.equals(expressionConstant)) {
            return -1; // the assignee is related to an user task
        } else {
            // get the process and fill the others elements
            fillDependenciesFromProcessInstance(context, containerId);
            return getNonNullLong(expressionConstant, context);
        }
    }

    private Serializable evaluateUsingActivityInstanceContainer(final ExpressionConstants expressionConstant, final Map<String, Object> context,
            final long containerId) throws SBonitaException {
        if (ExpressionConstants.ACTIVITY_INSTANCE_ID.equals(expressionConstant)) {
            return containerId;
        }
        // get the activity and fill the others elements
        fillDependenciesFromFlowNodeInstance(context, containerId);
        return getNonNullLong(expressionConstant, context);
    }

    private Serializable getNonNullLong(final ExpressionConstants expressionConstant, final Map<String, Object> context) {
        final Serializable serializable = (Serializable) context.get(expressionConstant.getEngineConstantName());
        return serializable == null ? -1L : serializable;
    }

    private void fillDependenciesFromProcessInstance(final Map<String, Object> context, final long processInstanceId) throws SProcessInstanceNotFoundException,
            SProcessInstanceReadException {
        final SProcessInstance processInstance = processInstanceService.getProcessInstance(processInstanceId);
        context.put(ExpressionConstants.PROCESS_INSTANCE_ID.getEngineConstantName(), processInstance.getId());
        context.put(ExpressionConstants.ROOT_PROCESS_INSTANCE_ID.getEngineConstantName(), processInstance.getRootProcessInstanceId());
    }

    void fillDependenciesFromFlowNodeInstance(final Map<String, Object> context, final long flowNodeInstanceId) throws SBonitaException {
        if (context.get("time") != null) {
            final SAActivityInstance aActivityInstance = activityInstanceService.getMostRecentArchivedActivityInstance(flowNodeInstanceId);
            context.put(ExpressionConstants.PROCESS_INSTANCE_ID.getEngineConstantName(), aActivityInstance.getLogicalGroup(3));
            context.put(ExpressionConstants.ROOT_PROCESS_INSTANCE_ID.getEngineConstantName(), aActivityInstance.getLogicalGroup(1));
            if (isHumanTask(aActivityInstance)) {
                final SAHumanTaskInstance saHumanTask = (SAHumanTaskInstance) aActivityInstance;
                context.put(ExpressionConstants.TASK_ASSIGNEE_ID.getEngineConstantName(), saHumanTask.getAssigneeId());
            }
        } else {
            final SFlowNodeInstance flowNodeInstance = activityInstanceService.getFlowNodeInstance(flowNodeInstanceId);
            context.put(ExpressionConstants.PROCESS_INSTANCE_ID.getEngineConstantName(), flowNodeInstance.getLogicalGroup(3));
            context.put(ExpressionConstants.ROOT_PROCESS_INSTANCE_ID.getEngineConstantName(), flowNodeInstance.getLogicalGroup(1));
            if (isHumanTask(flowNodeInstance)) {
                final SHumanTaskInstance taskInstance = (SHumanTaskInstance) flowNodeInstance;
                context.put(ExpressionConstants.TASK_ASSIGNEE_ID.getEngineConstantName(), taskInstance.getAssigneeId());
            }
        }
    }

    private boolean isHumanTask(final SAFlowNodeInstance aFlowNodeInstance) {
        return SFlowNodeType.USER_TASK.equals(aFlowNodeInstance.getType()) || SFlowNodeType.MANUAL_TASK.equals(aFlowNodeInstance.getType());
    }

    private boolean isHumanTask(final SFlowNodeInstance flowNodeInstance) {
        return SFlowNodeType.USER_TASK.equals(flowNodeInstance.getType()) || SFlowNodeType.MANUAL_TASK.equals(flowNodeInstance.getType());
    }

    private Serializable createContext(final Map<String, Object> context, final ContainerState containerState) throws SProcessInstanceNotFoundException,
            SProcessInstanceReadException, SActivityInstanceNotFoundException, SFlowNodeReadException, SBonitaReadException {
        final EngineExecutionContext ctx = new EngineExecutionContext();
        if (context.containsKey(SExpressionContext.CONTAINER_TYPE_KEY) && context.containsKey(SExpressionContext.CONTAINER_ID_KEY)) {
            final String containerType = (String) context.get(SExpressionContext.CONTAINER_TYPE_KEY);
            final long containerId = (Long) context.get(SExpressionContext.CONTAINER_ID_KEY);
            if (ContainerState.ARCHIVED.equals(containerState)) {
                if (DataInstanceContainer.ACTIVITY_INSTANCE.toString().equals(containerType)) {
                    updateContextFromArchivedActivityInstance(ctx, containerId);
                } else if (DataInstanceContainer.PROCESS_INSTANCE.toString().equals(containerType)) {
                    updateContextFromArchivedProcessInstance(ctx, containerId);
                }
            } else {
                if (DataInstanceContainer.ACTIVITY_INSTANCE.toString().equals(containerType)) {
                    updateContextFromActivityInstance(ctx, containerId);
                } else if (DataInstanceContainer.PROCESS_INSTANCE.toString().equals(containerType)) {
                    updateContextFromProcessInstance(ctx, containerId);
                }
            }
        }
        if (context.containsKey(SExpressionContext.PROCESS_DEFINITION_ID_KEY)) {
            ctx.setProcessDefinitionId((Long) context.get(SExpressionContext.PROCESS_DEFINITION_ID_KEY));
        }
        return ctx;
    }

    private void updateContextFromArchivedProcessInstance(final EngineExecutionContext ctx, final long processInstanceId)
            throws SBonitaReadException {
        final SAProcessInstance processInstance = processInstanceService.getLastArchivedProcessInstance(processInstanceId);
        if (processInstance != null) {
            ctx.setProcessInstanceId(processInstance.getSourceObjectId());
            ctx.setRootProcessInstanceId(processInstance.getRootProcessInstanceId());
        }
    }

    private void updateContextFromArchivedActivityInstance(final EngineExecutionContext ctx, final long activityInstanceId) throws SBonitaReadException {
        final SAActivityInstance activityInstance = activityInstanceService.getLastArchivedFlowNodeInstance(SAActivityInstance.class, activityInstanceId);
        if (activityInstance != null) {
            ctx.setActivityInstanceId(activityInstance.getSourceObjectId());
            ctx.setProcessInstanceId(activityInstance.getParentProcessInstanceId());
            ctx.setRootProcessInstanceId(activityInstance.getRootProcessInstanceId());
            if (isHumanTask(activityInstance)) {
                ctx.setTaskAssigneeId(((SAHumanTaskInstance) activityInstance).getAssigneeId());
            }
        }
    }

    private void updateContextFromProcessInstance(final EngineExecutionContext ctx, final long processInstanceId) throws SProcessInstanceNotFoundException,
            SProcessInstanceReadException {
        final SProcessInstance processInstance = processInstanceService.getProcessInstance(processInstanceId);
        ctx.setProcessInstanceId(processInstance.getId());
        ctx.setRootProcessInstanceId(processInstance.getRootProcessInstanceId());
    }

    private void updateContextFromActivityInstance(final EngineExecutionContext ctx, final long activityInstanceId) throws SActivityReadException,
            SActivityInstanceNotFoundException {
        ctx.setActivityInstanceId(activityInstanceId);
        final SActivityInstance activityInstance = activityInstanceService.getActivityInstance(activityInstanceId);
        ctx.setProcessInstanceId(activityInstance.getParentProcessInstanceId());
        ctx.setRootProcessInstanceId(activityInstance.getRootProcessInstanceId());
        if (isHumanTask(activityInstance)) {
            ctx.setTaskAssigneeId(((SHumanTaskInstance) activityInstance).getAssigneeId());
        }
    }

    @Override
    public void validate(final SExpression expression) throws SInvalidExpressionException {
        if (ExpressionConstantsResolver.getExpressionConstantsFromName(expression.getContent()) == null) {
            throw new SInvalidExpressionException("Unable to get Engine Constant '" + expression.getContent() + "' in expression: " + expression,
                    expression.getName());
        }
    }

    @Override
    public ExpressionKind getExpressionKind() {
        return KIND_ENGINE_CONSTANT;
    }

    @Override
    public List<Object> evaluate(final List<SExpression> expressions, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionEvaluationException {
        final List<Object> results = new ArrayList<Object>();
        for (final SExpression sExpression : expressions) {
            results.add(evaluate(sExpression, context, resolvedExpressions, containerState));
        }
        return results;
    }

    @Override
    public boolean mustPutEvaluatedExpressionInContext() {
        return true;
    }

    public SessionAccessor getSessionAccessor() {
        return sessionAccessor;
    }

    public SessionService getSessionService() {
        return sessionService;
    }

}
