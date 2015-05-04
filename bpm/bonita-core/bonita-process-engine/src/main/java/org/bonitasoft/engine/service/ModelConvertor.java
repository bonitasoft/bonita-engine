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
 */
package org.bonitasoft.engine.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.SActorMember;
import org.bonitasoft.engine.api.impl.SessionInfos;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.actor.ActorMember;
import org.bonitasoft.engine.bpm.actor.impl.ActorInstanceImpl;
import org.bonitasoft.engine.bpm.actor.impl.ActorMemberImpl;
import org.bonitasoft.engine.bpm.category.Category;
import org.bonitasoft.engine.bpm.category.impl.CategoryImpl;
import org.bonitasoft.engine.bpm.comment.ArchivedComment;
import org.bonitasoft.engine.bpm.comment.Comment;
import org.bonitasoft.engine.bpm.comment.impl.ArchivedCommentImpl;
import org.bonitasoft.engine.bpm.comment.impl.CommentImpl;
import org.bonitasoft.engine.bpm.connector.ArchivedConnectorInstance;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinition;
import org.bonitasoft.engine.bpm.connector.ConnectorImplementationDescriptor;
import org.bonitasoft.engine.bpm.connector.ConnectorInstance;
import org.bonitasoft.engine.bpm.connector.ConnectorInstanceWithFailureInfo;
import org.bonitasoft.engine.bpm.connector.ConnectorState;
import org.bonitasoft.engine.bpm.connector.impl.ArchivedConnectorInstanceImpl;
import org.bonitasoft.engine.bpm.connector.impl.ConnectorDefinitionImpl;
import org.bonitasoft.engine.bpm.connector.impl.ConnectorInstanceImpl;
import org.bonitasoft.engine.bpm.connector.impl.ConnectorInstanceWithFailureInfoImpl;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.ConstraintDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.ContractDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.InputDefinitionImpl;
import org.bonitasoft.engine.bpm.data.ArchivedDataInstance;
import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.data.impl.ArchivedDataInstanceImpl;
import org.bonitasoft.engine.bpm.data.impl.BlobDataInstanceImpl;
import org.bonitasoft.engine.bpm.data.impl.BooleanDataInstanceImpl;
import org.bonitasoft.engine.bpm.data.impl.DataDefinitionImpl;
import org.bonitasoft.engine.bpm.data.impl.DataInstanceImpl;
import org.bonitasoft.engine.bpm.data.impl.DateDataInstanceImpl;
import org.bonitasoft.engine.bpm.data.impl.DoubleDataInstanceImpl;
import org.bonitasoft.engine.bpm.data.impl.FloatDataInstanceImpl;
import org.bonitasoft.engine.bpm.data.impl.IntegerDataInstanceImpl;
import org.bonitasoft.engine.bpm.data.impl.LongDataInstanceImpl;
import org.bonitasoft.engine.bpm.data.impl.ShortTextDataInstanceImpl;
import org.bonitasoft.engine.bpm.document.ArchivedDocument;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.impl.ArchivedDocumentImpl;
import org.bonitasoft.engine.bpm.document.impl.DocumentImpl;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityStates;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedAutomaticTaskInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedCallActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedGatewayInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedLoopActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedManualTaskInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedReceiveTaskInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedSendTaskInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedSubProcessActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedUserTaskInstance;
import org.bonitasoft.engine.bpm.flownode.BPMEventType;
import org.bonitasoft.engine.bpm.flownode.EventInstance;
import org.bonitasoft.engine.bpm.flownode.EventTriggerInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.GatewayInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.ManualTaskInstance;
import org.bonitasoft.engine.bpm.flownode.StateCategory;
import org.bonitasoft.engine.bpm.flownode.TaskPriority;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerInstance;
import org.bonitasoft.engine.bpm.flownode.UserTaskInstance;
import org.bonitasoft.engine.bpm.flownode.WaitingEvent;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ActivityInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ArchivedActivityInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ArchivedAutomaticTaskInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ArchivedCallActivityInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ArchivedFlowNodeInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ArchivedGatewayInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ArchivedHumanTaskInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ArchivedLoopActivityInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ArchivedManualTaskInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ArchivedMultiInstanceActivityInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ArchivedReceiveTaskInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ArchivedSendTaskInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ArchivedSubProcessActivityInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ArchivedUserTaskInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.AutomaticTaskInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.BoundaryEventInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.CallActivityInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.EndEventInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.EventInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowNodeInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.GatewayInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.HumanTaskInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.IntermediateCatchEventInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.IntermediateThrowEventInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.LoopActivityInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ManualTaskInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.MultiInstanceActivityInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ReceiveTaskInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.SendTaskInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.StartEventInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.SubProcessActivityInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.TimerEventTriggerInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.UserTaskInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.WaitingErrorEventImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.WaitingMessageEventImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.WaitingSignalEventImpl;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.bpm.process.impl.ProcessInstanceBuilder;
import org.bonitasoft.engine.bpm.process.impl.internal.ArchivedProcessInstanceImpl;
import org.bonitasoft.engine.bpm.process.impl.internal.ProcessDefinitionImpl;
import org.bonitasoft.engine.bpm.process.impl.internal.ProcessDeploymentInfoImpl;
import org.bonitasoft.engine.bpm.supervisor.ProcessSupervisor;
import org.bonitasoft.engine.bpm.supervisor.impl.ProcessSupervisorImpl;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.business.data.BusinessDataReference;
import org.bonitasoft.engine.business.data.impl.MultipleBusinessDataReferenceImpl;
import org.bonitasoft.engine.business.data.impl.SimpleBusinessDataReferenceImpl;
import org.bonitasoft.engine.command.CommandDescriptor;
import org.bonitasoft.engine.command.CommandDescriptorImpl;
import org.bonitasoft.engine.command.model.SCommand;
import org.bonitasoft.engine.core.category.model.SCategory;
import org.bonitasoft.engine.core.connector.parser.SConnectorImplementationDescriptor;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.document.model.archive.SAMappedDocument;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.operation.model.SOperatorType;
import org.bonitasoft.engine.core.operation.model.builder.SLeftOperandBuilderFactory;
import org.bonitasoft.engine.core.operation.model.builder.SOperationBuilderFactory;
import org.bonitasoft.engine.core.process.comment.model.SComment;
import org.bonitasoft.engine.core.process.comment.model.archive.SAComment;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SConstraintDefinition;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.core.process.definition.model.SType;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SAutomaticTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SCallActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstanceWithFailureInfo;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SLoopActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SManualTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SMultiInstanceActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SReceiveTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SSendTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SSubProcessActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SUserTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAAutomaticTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SACallActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SALoopActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAManualTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAMultiInstanceActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAReceiveTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SASendTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SASubProcessActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAUserTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SMultiRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SSimpleRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SBoundaryEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingErrorEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingSignalEvent;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SADataInstance;
import org.bonitasoft.engine.exception.UnknownElementType;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilder;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilderFactory;
import org.bonitasoft.engine.form.FormMapping;
import org.bonitasoft.engine.form.FormMappingTarget;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.identity.ContactData;
import org.bonitasoft.engine.identity.ContactDataCreator.ContactDataField;
import org.bonitasoft.engine.identity.ExportedUser;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.GroupCreator;
import org.bonitasoft.engine.identity.GroupCreator.GroupField;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.RoleCreator;
import org.bonitasoft.engine.identity.RoleCreator.RoleField;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserCreator;
import org.bonitasoft.engine.identity.UserCreator.UserField;
import org.bonitasoft.engine.identity.UserMembership;
import org.bonitasoft.engine.identity.impl.ContactDataImpl;
import org.bonitasoft.engine.identity.impl.CustomUserInfoDefinitionImpl;
import org.bonitasoft.engine.identity.impl.CustomUserInfoValueImpl;
import org.bonitasoft.engine.identity.impl.GroupImpl;
import org.bonitasoft.engine.identity.impl.RoleImpl;
import org.bonitasoft.engine.identity.impl.UserImpl;
import org.bonitasoft.engine.identity.impl.UserMembershipImpl;
import org.bonitasoft.engine.identity.model.SContactInfo;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserLogin;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.identity.model.builder.SContactInfoBuilder;
import org.bonitasoft.engine.identity.model.builder.SContactInfoBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SGroupBuilder;
import org.bonitasoft.engine.identity.model.builder.SGroupBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SRoleBuilder;
import org.bonitasoft.engine.identity.model.builder.SRoleBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SUserBuilder;
import org.bonitasoft.engine.identity.model.builder.SUserBuilderFactory;
import org.bonitasoft.engine.job.FailedJob;
import org.bonitasoft.engine.job.impl.FailedJobImpl;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.operation.impl.LeftOperandImpl;
import org.bonitasoft.engine.operation.impl.OperationImpl;
import org.bonitasoft.engine.page.PageURL;
import org.bonitasoft.engine.page.SPageMapping;
import org.bonitasoft.engine.page.SPageURL;
import org.bonitasoft.engine.platform.Platform;
import org.bonitasoft.engine.platform.command.model.SPlatformCommand;
import org.bonitasoft.engine.platform.impl.PlatformImpl;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileEntry;
import org.bonitasoft.engine.profile.ProfileMember;
import org.bonitasoft.engine.profile.ProfileMemberCreator;
import org.bonitasoft.engine.profile.ProfileMemberCreator.ProfileMemberField;
import org.bonitasoft.engine.profile.builder.SProfileMemberBuilder;
import org.bonitasoft.engine.profile.builder.SProfileMemberBuilderFactory;
import org.bonitasoft.engine.profile.impl.ProfileEntryImpl;
import org.bonitasoft.engine.profile.impl.ProfileImpl;
import org.bonitasoft.engine.profile.impl.ProfileMemberImpl;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.SProfileEntry;
import org.bonitasoft.engine.profile.model.SProfileMember;
import org.bonitasoft.engine.scheduler.model.SFailedJob;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.impl.APISessionImpl;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisor;
import org.bonitasoft.engine.theme.Theme;
import org.bonitasoft.engine.theme.ThemeType;
import org.bonitasoft.engine.theme.impl.ThemeImpl;
import org.bonitasoft.engine.theme.model.STheme;

/**
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 * @author Celine Souchet
 */
public class ModelConvertor {

    public static APISession toAPISession(final SSession session, final String tenant) {
        final long tenantId = session.getTenantId();
        final long id = session.getId();
        final long userId = session.getUserId();
        final String userName = session.getUserName();
        final Date creationDate = session.getCreationDate();
        final long duration = session.getDuration();
        final boolean technicalUser = session.isTechnicalUser();
        final APISessionImpl apiSession = new APISessionImpl(id, creationDate, duration, userName, userId, tenant, tenantId);
        apiSession.setTechnicalUser(technicalUser);
        return apiSession;
    }

    public static Platform toPlatform(final SPlatform sPlatform) {
        return new PlatformImpl(sPlatform.getVersion(), sPlatform.getPreviousVersion(), sPlatform.getInitialVersion(), sPlatform.getCreatedBy(),
                sPlatform.getCreated());
    }

    public static List<ActivityInstance> toActivityInstances(final List<SActivityInstance> sActivities, final FlowNodeStateManager flowNodeStateManager) {
        final List<ActivityInstance> activityInstances = new ArrayList<ActivityInstance>();
        for (final SActivityInstance sActivity : sActivities) {
            final ActivityInstance activityInstance = toActivityInstance(sActivity, flowNodeStateManager);
            activityInstances.add(activityInstance);
        }
        return activityInstances;
    }

    public static List<FlowNodeInstance> toFlowNodeInstances(final List<SFlowNodeInstance> sFlowNodes, final FlowNodeStateManager flowNodeStateManager) {
        final List<FlowNodeInstance> flowNodeInstances = new ArrayList<FlowNodeInstance>();
        for (final SFlowNodeInstance sFlowNode : sFlowNodes) {
            final FlowNodeInstance flowNodeInstance = toFlowNodeInstance(sFlowNode, flowNodeStateManager);
            flowNodeInstances.add(flowNodeInstance);
        }
        return flowNodeInstances;
    }

    private static void updateFlowNode(final FlowNodeInstanceImpl flowNode, final SFlowNodeInstance sflowNode, final String state) {
        flowNode.setId(sflowNode.getId());
        flowNode.setState(state);
        flowNode.setParentContainerId(sflowNode.getParentContainerId());
        flowNode.setRootContainerId(sflowNode.getRootContainerId());
        flowNode.setProcessDefinitionId(sflowNode.getLogicalGroup(0));
        flowNode.setParentProcessInstanceId(sflowNode.getLogicalGroup(3));
        flowNode.setDisplayName(sflowNode.getDisplayName());
        flowNode.setDisplayDescription(sflowNode.getDisplayDescription());
        flowNode.setDescription(sflowNode.getDescription());
        flowNode.setExecutedBy(sflowNode.getExecutedBy());
        flowNode.setExecutedBySubstitute(sflowNode.getExecutedBySubstitute());
        flowNode.setStateCategory(StateCategory.valueOf(sflowNode.getStateCategory().name()));
    }

    public static ActivityInstance toActivityInstance(final SActivityInstance sActivity, final FlowNodeStateManager flowNodeStateManager) {
        switch (sActivity.getType()) {
            case AUTOMATIC_TASK:
                return toAutomaticTask((SAutomaticTaskInstance) sActivity, flowNodeStateManager);
            case MANUAL_TASK:
                return toManualTask((SManualTaskInstance) sActivity, flowNodeStateManager);
            case USER_TASK:
                return toUserTaskInstance((SUserTaskInstance) sActivity, flowNodeStateManager);
            case RECEIVE_TASK:
                return toReceiveTaskInstance((SReceiveTaskInstance) sActivity, flowNodeStateManager);
            case SEND_TASK:
                return toSendTaskInstance((SSendTaskInstance) sActivity, flowNodeStateManager);
            case CALL_ACTIVITY:
                return toCallActivityInstance((SCallActivityInstance) sActivity, flowNodeStateManager);
            case SUB_PROCESS:
                return toSubProcessActivityInstance((SSubProcessActivityInstance) sActivity, flowNodeStateManager);
            case LOOP_ACTIVITY:
                return toLoopActivityInstance((SLoopActivityInstance) sActivity, flowNodeStateManager);
            case MULTI_INSTANCE_ACTIVITY:
                return toMultiInstanceActivityInstance((SMultiInstanceActivityInstance) sActivity, flowNodeStateManager);
            default:
                throw new UnknownElementType(sActivity.getType().name());
        }
    }

    public static FlowNodeInstance toFlowNodeInstance(final SFlowNodeInstance sFlowNode, final FlowNodeStateManager flowNodeStateManager) {
        switch (sFlowNode.getType()) {
            case START_EVENT:
                return toEventInstance((SEventInstance) sFlowNode, flowNodeStateManager);
            case INTERMEDIATE_CATCH_EVENT:
                return toEventInstance((SEventInstance) sFlowNode, flowNodeStateManager);
            case BOUNDARY_EVENT:
                return toEventInstance((SEventInstance) sFlowNode, flowNodeStateManager);
            case INTERMEDIATE_THROW_EVENT:
                return toEventInstance((SEventInstance) sFlowNode, flowNodeStateManager);
            case END_EVENT:
                return toEventInstance((SEventInstance) sFlowNode, flowNodeStateManager);
            case GATEWAY:
                return toGatewayInstance((SGatewayInstance) sFlowNode, flowNodeStateManager);
            default:
                if (sFlowNode instanceof SActivityInstance) {
                    return toActivityInstance((SActivityInstance) sFlowNode, flowNodeStateManager);
                }
                throw new UnknownElementType(sFlowNode.getType().name());
        }
    }

    public static ActivityInstance toAutomaticTask(final SAutomaticTaskInstance sActivity, final FlowNodeStateManager flowNodeStateManager) {
        final AutomaticTaskInstanceImpl automaticTaskInstance = new AutomaticTaskInstanceImpl(sActivity.getName(), sActivity.getFlowNodeDefinitionId());
        updateActivityInstance(sActivity, flowNodeStateManager, automaticTaskInstance);
        return automaticTaskInstance;
    }

    public static ActivityInstance toCallActivityInstance(final SCallActivityInstance sActivity, final FlowNodeStateManager flowNodeStateManager) {
        final CallActivityInstanceImpl callActivityInstance = new CallActivityInstanceImpl(sActivity.getName(), sActivity.getFlowNodeDefinitionId());
        updateActivityInstance(sActivity, flowNodeStateManager, callActivityInstance);
        return callActivityInstance;
    }

    public static ActivityInstance toCallActivityInstance(final SSubProcessActivityInstance sActivity, final FlowNodeStateManager flowNodeStateManager) {
        final SubProcessActivityInstanceImpl subProcActivityInstance = new SubProcessActivityInstanceImpl(sActivity.getName(),
                sActivity.getFlowNodeDefinitionId(), sActivity.isTriggeredByEvent());
        updateActivityInstance(sActivity, flowNodeStateManager, subProcActivityInstance);
        return subProcActivityInstance;
    }

    public static ActivityInstance toSubProcessActivityInstance(final SSubProcessActivityInstance sActivity, final FlowNodeStateManager flowNodeStateManager) {
        final SubProcessActivityInstanceImpl subProcessActivityInstance = new SubProcessActivityInstanceImpl(sActivity.getName(),
                sActivity.getFlowNodeDefinitionId(), sActivity.isTriggeredByEvent());
        updateActivityInstance(sActivity, flowNodeStateManager, subProcessActivityInstance);
        return subProcessActivityInstance;
    }

    public static ActivityInstance toLoopActivityInstance(final SLoopActivityInstance sActivity, final FlowNodeStateManager flowNodeStateManager) {
        final LoopActivityInstanceImpl loopActivityInstance = new LoopActivityInstanceImpl(sActivity.getName(), sActivity.getFlowNodeDefinitionId(),
                sActivity.getLoopCounter());
        updateActivityInstance(sActivity, flowNodeStateManager, loopActivityInstance);
        return loopActivityInstance;
    }

    public static ActivityInstance toMultiInstanceActivityInstance(final SMultiInstanceActivityInstance sActivity,
            final FlowNodeStateManager flowNodeStateManager) {
        final MultiInstanceActivityInstanceImpl loopActivityInstance = new MultiInstanceActivityInstanceImpl(sActivity.getName(),
                sActivity.getFlowNodeDefinitionId(), sActivity.isSequential(), sActivity.getLoopDataInputRef(), sActivity.getLoopDataOutputRef(),
                sActivity.getDataInputItemRef(), sActivity.getDataOutputItemRef(), sActivity.getNumberOfActiveInstances(),
                sActivity.getNumberOfCompletedInstances(), sActivity.getNumberOfTerminatedInstances(), sActivity.getLoopCardinality());
        updateActivityInstance(sActivity, flowNodeStateManager, loopActivityInstance);
        return loopActivityInstance;
    }

    public static GatewayInstance toGatewayInstance(final SGatewayInstance sGatewayInstance, final FlowNodeStateManager flowNodeStateManager) {
        final GatewayInstanceImpl gatewayInstance = new GatewayInstanceImpl(sGatewayInstance.getName(), sGatewayInstance.getFlowNodeDefinitionId());
        final String state = flowNodeStateManager.getState(sGatewayInstance.getStateId()).getName();
        updateFlowNode(gatewayInstance, sGatewayInstance, state);
        return gatewayInstance;
    }

    public static ArchivedGatewayInstance toArchivedGatewayInstance(final SAGatewayInstance saGatewayInstance, final FlowNodeStateManager flowNodeStateManager) {
        final String name = saGatewayInstance.getName();
        final ArchivedGatewayInstanceImpl aGatewayInstance = new ArchivedGatewayInstanceImpl(name);
        final String state = flowNodeStateManager.getState(saGatewayInstance.getStateId()).getName();
        updateArchivedFlowNodeInstance(aGatewayInstance, saGatewayInstance, state);
        return aGatewayInstance;
    }

    private static void updateActivityInstance(final SActivityInstance sActivity, final FlowNodeStateManager flowNodeStateManager,
            final ActivityInstanceImpl activity) {
        final String state = flowNodeStateManager.getState(sActivity.getStateId()).getName();
        updateFlowNode(activity, sActivity, state);
        activity.setReachedSateDate(new Date(sActivity.getReachedStateDate()));
        activity.setLastUpdateDate(new Date(sActivity.getLastUpdateDate()));
    }

    public static List<UserTaskInstance> toUserTaskInstances(final List<SUserTaskInstance> sUserTasks, final FlowNodeStateManager flowNodeStateManager) {
        final List<UserTaskInstance> userTaskInstances = new ArrayList<UserTaskInstance>();
        for (final SUserTaskInstance sUserTask : sUserTasks) {
            final UserTaskInstance userTask = toUserTaskInstance(sUserTask, flowNodeStateManager);
            userTaskInstances.add(userTask);
        }
        return userTaskInstances;
    }

    public static UserTaskInstance toUserTaskInstance(final SUserTaskInstance sUserTask, final FlowNodeStateManager flowNodeStateManager) {
        final UserTaskInstanceImpl userTaskInstance = new UserTaskInstanceImpl(sUserTask.getName(), sUserTask.getFlowNodeDefinitionId(), sUserTask.getActorId());
        updateHumanTaskInstance(sUserTask, flowNodeStateManager, userTaskInstance);
        return userTaskInstance;
    }

    public static ActivityInstance toReceiveTaskInstance(final SReceiveTaskInstance sReceiveTask, final FlowNodeStateManager flowNodeStateManager) {
        final ReceiveTaskInstanceImpl receiveTaskInstance = new ReceiveTaskInstanceImpl(sReceiveTask.getName(), sReceiveTask.getFlowNodeDefinitionId());
        updateActivityInstance(sReceiveTask, flowNodeStateManager, receiveTaskInstance);
        return receiveTaskInstance;
    }

    public static ActivityInstance toSendTaskInstance(final SSendTaskInstance sSendTask, final FlowNodeStateManager flowNodeStateManager) {
        final SendTaskInstanceImpl sendTask = new SendTaskInstanceImpl(sSendTask.getName(), sSendTask.getFlowNodeDefinitionId());
        updateActivityInstance(sSendTask, flowNodeStateManager, sendTask);
        return sendTask;
    }

    private static void updateHumanTaskInstance(final SHumanTaskInstance sHumanTask, final FlowNodeStateManager flowNodeStateManager,
            final HumanTaskInstanceImpl humanTaskInstance) {
        updateActivityInstance(sHumanTask, flowNodeStateManager, humanTaskInstance);
        humanTaskInstance.setAssigneeId(sHumanTask.getAssigneeId());
        final long claimedDate = sHumanTask.getClaimedDate();
        if (claimedDate > 0) {
            humanTaskInstance.setClaimedDate(new Date(claimedDate));
        }
        humanTaskInstance.setPriority(TaskPriority.valueOf(sHumanTask.getPriority().name()));
        final long expectedEndDate = sHumanTask.getExpectedEndDate();
        if (expectedEndDate > 0) {
            humanTaskInstance.setExpectedEndDate(new Date(expectedEndDate));
        }
    }

    public static List<HumanTaskInstance> toHumanTaskInstances(final List<? extends SHumanTaskInstance> sHumanTasks,
            final FlowNodeStateManager flowNodeStateManager) {
        final List<HumanTaskInstance> humanTaskInstances = new ArrayList<HumanTaskInstance>(sHumanTasks.size());
        for (final SHumanTaskInstance sUserTask : sHumanTasks) {
            final HumanTaskInstance userTask = toHumanTaskInstance(sUserTask, flowNodeStateManager);
            humanTaskInstances.add(userTask);
        }
        return humanTaskInstances;
    }

    public static HumanTaskInstance toHumanTaskInstance(final SHumanTaskInstance sHumanTask, final FlowNodeStateManager flowNodeStateManager) {
        switch (sHumanTask.getType()) {
            case USER_TASK:
                return toUserTaskInstance((SUserTaskInstance) sHumanTask, flowNodeStateManager);
            case MANUAL_TASK:
                return toManualTask((SManualTaskInstance) sHumanTask, flowNodeStateManager);
            default:
                throw new UnknownElementType(sHumanTask.getType().name());
        }
    }

    public static ManualTaskInstance toManualTask(final SManualTaskInstance sHumanTask, final FlowNodeStateManager flowNodeStateManager) {
        final ManualTaskInstanceImpl manualTaskInstance = new ManualTaskInstanceImpl(sHumanTask.getName(), sHumanTask.getFlowNodeDefinitionId(),
                sHumanTask.getActorId());
        updateHumanTaskInstance(sHumanTask, flowNodeStateManager, manualTaskInstance);
        return manualTaskInstance;
    }

    public static ProcessDefinition toProcessDefinition(final SProcessDefinition sDefinition) {
        final ProcessDefinitionImpl processDefinitionImpl = new ProcessDefinitionImpl(sDefinition.getName(), sDefinition.getVersion());
        processDefinitionImpl.setId(sDefinition.getId());
        processDefinitionImpl.setDescription(sDefinition.getDescription());
        return processDefinitionImpl;
    }

    public static List<ProcessInstance> toProcessInstances(final List<SProcessInstance> sProcessInstances,
            final ProcessDefinitionService processDefinitionService) {
        final List<ProcessInstance> clientProcessInstances = new ArrayList<ProcessInstance>();
        if (sProcessInstances != null) {
            final Map<Long, SProcessDefinition> processDefinitions = new HashMap<Long, SProcessDefinition>();

            for (final SProcessInstance sProcessInstance : sProcessInstances) {
                final SProcessDefinition sProcessDefinition = getProcessDefinition(processDefinitionService, processDefinitions,
                        sProcessInstance.getProcessDefinitionId());
                clientProcessInstances.add(toProcessInstance(sProcessDefinition, sProcessInstance));
            }
        }
        return Collections.unmodifiableList(clientProcessInstances);
    }

    private static SProcessDefinition getProcessDefinition(final ProcessDefinitionService processDefinitionService,
            final Map<Long, SProcessDefinition> processDefinitions, final long processDefinitionId) {
        SProcessDefinition sProcessDefinition = processDefinitions.get(processDefinitionId);
        if (sProcessDefinition == null) {
            try {
                sProcessDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
                processDefinitions.put(sProcessDefinition.getId(), sProcessDefinition);
            } catch (final SProcessDefinitionNotFoundException e) {
                // ignore...
            } catch (final SProcessDefinitionReadException e) {
                // ignore...
            }
        }
        return sProcessDefinition;
    }

    public static ProcessInstance toProcessInstance(final SProcessDefinition definition, final SProcessInstance sInstance) {
        final ProcessInstanceBuilder clientProcessInstanceBuilder = ProcessInstanceBuilder.getInstance().createNewInstance(sInstance.getName());
        clientProcessInstanceBuilder.setId(sInstance.getId());

        clientProcessInstanceBuilder.setState(ProcessInstanceState.getFromId(sInstance.getStateId()).name().toLowerCase());
        if (sInstance.getStartDate() > 0) {
            clientProcessInstanceBuilder.setStartDate(sInstance.getStartDate());
        }
        clientProcessInstanceBuilder.setStartedBy(sInstance.getStartedBy());
        clientProcessInstanceBuilder.setStartedBySubstitute(sInstance.getStartedBySubstitute());
        if (sInstance.getEndDate() > 0) {
            clientProcessInstanceBuilder.setEndDate(sInstance.getEndDate());
        }
        clientProcessInstanceBuilder.setLastUpdate(sInstance.getLastUpdate());
        clientProcessInstanceBuilder.setProcessDefinitionId(sInstance.getProcessDefinitionId());
        clientProcessInstanceBuilder.setDescription(sInstance.getDescription());
        clientProcessInstanceBuilder.setRootProcessInstanceId(sInstance.getRootProcessInstanceId());
        clientProcessInstanceBuilder.setCallerId(sInstance.getCallerId());

        if (definition != null) {
            for (int i = 1; i <= 5; i++) {
                clientProcessInstanceBuilder.setStringIndexLabel(i, definition.getStringIndexLabel(i));
            }
        }
        clientProcessInstanceBuilder.setStringIndex1(sInstance.getStringIndex1());
        clientProcessInstanceBuilder.setStringIndex2(sInstance.getStringIndex2());
        clientProcessInstanceBuilder.setStringIndex3(sInstance.getStringIndex3());
        clientProcessInstanceBuilder.setStringIndex4(sInstance.getStringIndex4());
        clientProcessInstanceBuilder.setStringIndex5(sInstance.getStringIndex5());
        return clientProcessInstanceBuilder.done();
    }

    public static List<ProcessDeploymentInfo> toProcessDeploymentInfo(final List<SProcessDefinitionDeployInfo> processDefinitionDIs) {
        final List<ProcessDeploymentInfo> deploymentInfos = new ArrayList<ProcessDeploymentInfo>();
        for (final SProcessDefinitionDeployInfo processDefinitionDI : processDefinitionDIs) {
            final ProcessDeploymentInfo deploymentInfo = toProcessDeploymentInfo(processDefinitionDI);
            deploymentInfos.add(deploymentInfo);
        }
        return deploymentInfos;
    }

    public static ProcessDeploymentInfo toProcessDeploymentInfo(final SProcessDefinitionDeployInfo processDefinitionDI) {
        return new ProcessDeploymentInfoImpl(processDefinitionDI.getId(), processDefinitionDI.getProcessId(), processDefinitionDI.getName(),
                processDefinitionDI.getVersion(), processDefinitionDI.getDescription(), new Date(processDefinitionDI.getDeploymentDate()),
                processDefinitionDI.getDeployedBy(), ActivationState.valueOf(processDefinitionDI.getActivationState()),
                ConfigurationState.valueOf(processDefinitionDI.getConfigurationState()), processDefinitionDI.getDisplayName(), new Date(
                        processDefinitionDI.getLastUpdateDate()), processDefinitionDI.getIconPath(), processDefinitionDI.getDisplayDescription());
    }

    public static Map<Long, ProcessDeploymentInfo> toProcessDeploymentInfos(final Map<Long, SProcessDefinitionDeployInfo> sProcessDeploymentInfos) {
        if (sProcessDeploymentInfos != null && !sProcessDeploymentInfos.isEmpty()) {
            final Map<Long, ProcessDeploymentInfo> processDeploymentInfos = new HashMap<Long, ProcessDeploymentInfo>();
            final Set<Entry<Long, SProcessDefinitionDeployInfo>> entries = sProcessDeploymentInfos.entrySet();
            for (final Entry<Long, SProcessDefinitionDeployInfo> entry : entries) {
                processDeploymentInfos.put(entry.getKey(), toProcessDeploymentInfo(entry.getValue()));
            }
            return processDeploymentInfos;
        }
        return Collections.emptyMap();
    }

    public static ArchivedUserTaskInstance toArchivedUserTaskInstance(final SAUserTaskInstance sInstance, final FlowNodeStateManager flowNodeStateManager) {
        final ArchivedUserTaskInstanceImpl archivedUserTaskInstanceImpl = new ArchivedUserTaskInstanceImpl(sInstance.getName());
        updateArchivedHumanTaskInstance(archivedUserTaskInstanceImpl, flowNodeStateManager, sInstance);
        return archivedUserTaskInstanceImpl;
    }

    public static ArchivedReceiveTaskInstance toArchivedReceiveTaskInstance(final SAReceiveTaskInstance sInstance,
            final FlowNodeStateManager flowNodeStateManager) {
        final ArchivedReceiveTaskInstanceImpl archivedReceiveTaskInstanceImpl = new ArchivedReceiveTaskInstanceImpl(sInstance.getName());
        updateArchivedReceiveTaskInstance(archivedReceiveTaskInstanceImpl, flowNodeStateManager, sInstance);
        return archivedReceiveTaskInstanceImpl;
    }

    public static ArchivedSendTaskInstance toArchivedSendTaskInstance(final SASendTaskInstance sInstance, final FlowNodeStateManager flowNodeStateManager) {
        final ArchivedSendTaskInstanceImpl archivedSendTaskInstanceImpl = new ArchivedSendTaskInstanceImpl(sInstance.getName());
        updateArchivedSendTaskInstance(archivedSendTaskInstanceImpl, flowNodeStateManager, sInstance);
        return archivedSendTaskInstanceImpl;
    }

    /**
     * Update the fields of ArchivedHumanTaskInstance from a SAHumanTaskInstance
     */
    private static void updateArchivedHumanTaskInstance(final ArchivedHumanTaskInstanceImpl activity, final FlowNodeStateManager flowNodeStateManager,
            final SAHumanTaskInstance saHumanTask) {
        updateArchivedActivityInstance(activity, flowNodeStateManager, saHumanTask);
        activity.setAssigneeId(saHumanTask.getAssigneeId());
        activity.setPriority(TaskPriority.valueOf(saHumanTask.getPriority().name()));
        activity.setActorId(saHumanTask.getActorId());
        if (saHumanTask.getExpectedEndDate() > 0) {
            activity.setExpectedEndDate(new Date(saHumanTask.getExpectedEndDate()));
        }
        if (saHumanTask.getClaimedDate() > 0) {
            activity.setClaimedDate(new Date(saHumanTask.getClaimedDate()));
        }
    }

    /**
     * Update the fields of ArchivednTaskInstance from a SAActivityInstance
     */
    private static void updateArchivedReceiveTaskInstance(final ArchivedHumanTaskInstanceImpl activity, final FlowNodeStateManager flowNodeStateManager,
            final SAReceiveTaskInstance sActivity) {
        final String state = flowNodeStateManager.getState(sActivity.getStateId()).getName();
        updateArchivedFlowNodeInstance(activity, sActivity, state);
        activity.setReachedStateDate(new Date(sActivity.getReachedStateDate()));
        activity.setLastUpdateDate(new Date(sActivity.getLastUpdateDate()));
    }

    /**
     * Update the fields of ArchivednTaskInstance from a SAActivityInstance
     */
    private static void updateArchivedSendTaskInstance(final ArchivedHumanTaskInstanceImpl activity, final FlowNodeStateManager flowNodeStateManager,
            final SASendTaskInstance sActivity) {
        final String state = flowNodeStateManager.getState(sActivity.getStateId()).getName();
        updateArchivedFlowNodeInstance(activity, sActivity, state);
        activity.setReachedStateDate(new Date(sActivity.getReachedStateDate()));
        activity.setLastUpdateDate(new Date(sActivity.getLastUpdateDate()));
    }

    /**
     * Update the fields of ArchivedActivityInstance from a SAActivityInstance
     */
    private static void updateArchivedActivityInstance(final ArchivedActivityInstanceImpl activity, final FlowNodeStateManager flowNodeStateManager,
            final SAActivityInstance sActivity) {
        final String state = flowNodeStateManager.getState(sActivity.getStateId()).getName();
        updateArchivedFlowNodeInstance(activity, sActivity, state);
        activity.setReachedStateDate(new Date(sActivity.getReachedStateDate()));
        activity.setLastUpdateDate(new Date(sActivity.getLastUpdateDate()));
    }

    private static void updateArchivedFlowNodeInstance(final ArchivedFlowNodeInstanceImpl aFlowNode, final SAFlowNodeInstance saFlowNode, final String state) {
        aFlowNode.setId(saFlowNode.getId());
        aFlowNode.setState(state);
        aFlowNode.setParentContainerId(saFlowNode.getParentContainerId());
        aFlowNode.setRootContainerId(saFlowNode.getRootContainerId());
        aFlowNode.setSourceObjectId(saFlowNode.getSourceObjectId());
        aFlowNode.setProcessDefinitionId(saFlowNode.getProcessDefinitionId());
        aFlowNode.setProcessInstanceId(saFlowNode.getParentProcessInstanceId());
        aFlowNode.setParentActivityInstanceId(saFlowNode.getParentActivityInstanceId());
        aFlowNode.setDescription(saFlowNode.getDescription());
        aFlowNode.setDisplayName(saFlowNode.getDisplayName());
        aFlowNode.setDisplayDescription(saFlowNode.getDisplayDescription());
        if (saFlowNode.getArchiveDate() > 0) {
            aFlowNode.setArchiveDate(new Date(saFlowNode.getArchiveDate()));
        }
        aFlowNode.setExecutedBy(saFlowNode.getExecutedBy());
        aFlowNode.setExecutedBySubstitute(saFlowNode.getExecutedBySubstitute());
        aFlowNode.setFlownodeDefinitionId(saFlowNode.getFlowNodeDefinitionId());
        aFlowNode.setTerminal(saFlowNode.isTerminal());
    }

    public static List<ArchivedUserTaskInstance> toArchivedUserTaskInstances(final List<SAUserTaskInstance> sInstances,
            final FlowNodeStateManager flowNodeStateManager) {
        final List<ArchivedUserTaskInstance> archivedUserTaskInstances = new ArrayList<ArchivedUserTaskInstance>();
        for (final SAUserTaskInstance sAUserTaskInstance : sInstances) {
            final ArchivedUserTaskInstance archivedUserTaskInstance = toArchivedUserTaskInstance(sAUserTaskInstance, flowNodeStateManager);
            archivedUserTaskInstances.add(archivedUserTaskInstance);
        }
        return archivedUserTaskInstances;
    }

    public static List<ArchivedReceiveTaskInstance> toArchivedReceiveTaskInstances(final List<SAReceiveTaskInstance> sInstances,
            final FlowNodeStateManager flowNodeStateManager) {
        final List<ArchivedReceiveTaskInstance> archivedReceiveTaskInstances = new ArrayList<ArchivedReceiveTaskInstance>();
        for (final SAReceiveTaskInstance sAReceiveTaskInstance : sInstances) {
            final ArchivedReceiveTaskInstance archivedReceiveTaskInstance = toArchivedReceiveTaskInstance(sAReceiveTaskInstance, flowNodeStateManager);
            archivedReceiveTaskInstances.add(archivedReceiveTaskInstance);
        }
        return archivedReceiveTaskInstances;
    }

    public static List<ArchivedHumanTaskInstance> toArchivedHumanTaskInstances(final List<? extends SAHumanTaskInstance> sInstances,
            final FlowNodeStateManager flowNodeStateManager) {
        final List<ArchivedHumanTaskInstance> archivedUserTaskInstances = new ArrayList<ArchivedHumanTaskInstance>();
        for (final SAHumanTaskInstance sInstance : sInstances) {
            final ArchivedHumanTaskInstance archivedUserTaskInstance = toArchivedHumanTaskInstance(sInstance, flowNodeStateManager);
            archivedUserTaskInstances.add(archivedUserTaskInstance);
        }
        return archivedUserTaskInstances;
    }

    public static ArchivedHumanTaskInstance toArchivedHumanTaskInstance(final SAHumanTaskInstance sInstance, final FlowNodeStateManager flowNodeStateManager) {
        switch (sInstance.getType()) {
            case MANUAL_TASK:
                return toArchivedManualTaskInstance((SAManualTaskInstance) sInstance, flowNodeStateManager);
            case USER_TASK:
                return toArchivedUserTaskInstance((SAUserTaskInstance) sInstance, flowNodeStateManager);
            default:
                throw new UnknownElementType(sInstance.getType().name());
        }
    }

    public static ArchivedActivityInstance toArchivedActivityInstance(final SAActivityInstance sInstance, final FlowNodeStateManager flowNodeStateManager) {
        switch (sInstance.getType()) {
            case AUTOMATIC_TASK:
                return toArchivedAutomaticTaskInstance(sInstance, flowNodeStateManager);
            case MANUAL_TASK:
                return toArchivedManualTaskInstance((SAManualTaskInstance) sInstance, flowNodeStateManager);
            case USER_TASK:
                return toArchivedUserTaskInstance((SAUserTaskInstance) sInstance, flowNodeStateManager);
            case RECEIVE_TASK:
                return toArchivedReceiveTaskInstance((SAReceiveTaskInstance) sInstance, flowNodeStateManager);
            case SEND_TASK:
                return toArchivedSendTaskInstance((SASendTaskInstance) sInstance, flowNodeStateManager);
            case LOOP_ACTIVITY:
                return toArchivedLoopActivityInstance((SALoopActivityInstance) sInstance, flowNodeStateManager);
            case CALL_ACTIVITY:
                return toArchivedCallActivityInstance((SACallActivityInstance) sInstance, flowNodeStateManager);
            case SUB_PROCESS:
                return toArchivedSubProcessActivityInstance((SASubProcessActivityInstance) sInstance, flowNodeStateManager);
            case MULTI_INSTANCE_ACTIVITY:
                return toArchivedMultiInstanceActivityInstance((SAMultiInstanceActivityInstance) sInstance, flowNodeStateManager);
            case BOUNDARY_EVENT:
            case END_EVENT:
            case GATEWAY:
            case INTERMEDIATE_CATCH_EVENT:
            case INTERMEDIATE_THROW_EVENT:
            case START_EVENT:
                throw new UnknownElementType("Events are not yet archived");
            default:
                throw new UnknownElementType(sInstance.getType().name());
        }
    }

    private static ArchivedLoopActivityInstance toArchivedLoopActivityInstance(final SALoopActivityInstance sInstance,
            final FlowNodeStateManager flowNodeStateManager) {
        final ArchivedLoopActivityInstanceImpl archivedloopActivityInstanceImpl = new ArchivedLoopActivityInstanceImpl(sInstance.getName());
        archivedloopActivityInstanceImpl.setLoopCounter(sInstance.getLoopCounter());
        archivedloopActivityInstanceImpl.setLoopMax(sInstance.getLoopMax());
        updateArchivedActivityInstance(archivedloopActivityInstanceImpl, flowNodeStateManager, sInstance);
        return archivedloopActivityInstanceImpl;
    }

    private static ArchivedMultiInstanceActivityInstanceImpl toArchivedMultiInstanceActivityInstance(final SAMultiInstanceActivityInstance sInstance,
            final FlowNodeStateManager flowNodeStateManager) {
        final ArchivedMultiInstanceActivityInstanceImpl archivedMultiInstanceActivityInstanceImpl = new ArchivedMultiInstanceActivityInstanceImpl(
                sInstance.getName(), sInstance.getFlowNodeDefinitionId(), sInstance.isSequential(), sInstance.getLoopDataInputRef(),
                sInstance.getLoopDataOutputRef(), sInstance.getDataInputItemRef(), sInstance.getDataOutputItemRef(), sInstance.getNumberOfActiveInstances(),
                sInstance.getNumberOfCompletedInstances(), sInstance.getNumberOfTerminatedInstances(), sInstance.getLoopCardinality());
        updateArchivedActivityInstance(archivedMultiInstanceActivityInstanceImpl, flowNodeStateManager, sInstance);
        return archivedMultiInstanceActivityInstanceImpl;
    }

    public static ArchivedManualTaskInstance toArchivedManualTaskInstance(final SAManualTaskInstance sInstance, final FlowNodeStateManager flowNodeStateManager) {
        final ArchivedManualTaskInstanceImpl archivedUserTaskInstanceImpl = new ArchivedManualTaskInstanceImpl(sInstance.getName());
        updateArchivedHumanTaskInstance(archivedUserTaskInstanceImpl, flowNodeStateManager, sInstance);
        return archivedUserTaskInstanceImpl;
    }

    public static ArchivedCallActivityInstance toArchivedCallActivityInstance(final SACallActivityInstance sInstance,
            final FlowNodeStateManager flowNodeStateManager) {
        final ArchivedCallActivityInstanceImpl archivedCallActivityInstanceImpl = new ArchivedCallActivityInstanceImpl(sInstance.getName());
        updateArchivedActivityInstance(archivedCallActivityInstanceImpl, flowNodeStateManager, sInstance);
        return archivedCallActivityInstanceImpl;
    }

    public static ArchivedSubProcessActivityInstance toArchivedSubProcessActivityInstance(final SASubProcessActivityInstance sInstance,
            final FlowNodeStateManager flowNodeStateManager) {
        final ArchivedSubProcessActivityInstanceImpl archivedSubProcActivityInstanceImpl = new ArchivedSubProcessActivityInstanceImpl(sInstance.getName(),
                sInstance.isTriggeredByEvent());
        updateArchivedActivityInstance(archivedSubProcActivityInstanceImpl, flowNodeStateManager, sInstance);
        return archivedSubProcActivityInstanceImpl;
    }

    public static ArchivedAutomaticTaskInstance toArchivedAutomaticTaskInstance(final SAActivityInstance sInstance,
            final FlowNodeStateManager flowNodeStateManager) {
        final ArchivedAutomaticTaskInstanceImpl archivedUserTaskInstanceImpl = new ArchivedAutomaticTaskInstanceImpl(sInstance.getName());
        updateArchivedActivityInstance(archivedUserTaskInstanceImpl, flowNodeStateManager, sInstance);
        return archivedUserTaskInstanceImpl;
    }

    public static List<ArchivedActivityInstance> toArchivedActivityInstances(final List<SAActivityInstance> saActivityInstances,
            final FlowNodeStateManager flowNodeStateManager) {
        final List<ArchivedActivityInstance> archivedActivityInstances = new ArrayList<ArchivedActivityInstance>();
        for (final SAActivityInstance saActivityInstance : saActivityInstances) {
            final ArchivedActivityInstance archivedActivityInstance = toArchivedActivityInstance(saActivityInstance, flowNodeStateManager);
            archivedActivityInstances.add(archivedActivityInstance);
        }
        return archivedActivityInstances;
    }

    public static List<ArchivedProcessInstance> toArchivedProcessInstances(final List<SAProcessInstance> saProcessInstances,
            final ProcessDefinitionService processDefinitionService) {
        if (saProcessInstances != null) {
            final List<ArchivedProcessInstance> clientProcessInstances = new ArrayList<ArchivedProcessInstance>(saProcessInstances.size());
            final Map<Long, SProcessDefinition> processDefinitions = new HashMap<Long, SProcessDefinition>(saProcessInstances.size());

            for (final SAProcessInstance saProcessInstance : saProcessInstances) {
                final SProcessDefinition sProcessDefinition = getProcessDefinition(processDefinitionService, processDefinitions,
                        saProcessInstance.getProcessDefinitionId());
                clientProcessInstances.add(toArchivedProcessInstance(saProcessInstance, sProcessDefinition));
            }
            return Collections.unmodifiableList(clientProcessInstances);
        }
        return Collections.unmodifiableList(new ArrayList<ArchivedProcessInstance>(1));
    }

    public static List<ArchivedProcessInstance> toArchivedProcessInstances(final List<SAProcessInstance> sProcessInstances,
            final SProcessDefinition sProcessDefinition) {
        final List<ArchivedProcessInstance> clientProcessInstances = new ArrayList<ArchivedProcessInstance>(sProcessInstances.size());
        for (final SAProcessInstance sProcessInstance : sProcessInstances) {
            clientProcessInstances.add(toArchivedProcessInstance(sProcessInstance, sProcessDefinition));
        }
        return Collections.unmodifiableList(clientProcessInstances);
    }

    public static ArchivedProcessInstance toArchivedProcessInstance(final SAProcessInstance sInstance, final SProcessDefinition sProcessDefinition) {
        final ArchivedProcessInstanceImpl archivedInstance = new ArchivedProcessInstanceImpl(sInstance.getName());
        archivedInstance.setId(sInstance.getId());
        final int stateId = sInstance.getStateId();
        archivedInstance.setStateId(stateId);
        archivedInstance.setState(ProcessInstanceState.getFromId(stateId).name().toLowerCase());
        if (sInstance.getStartDate() > 0) {
            archivedInstance.setStartDate(new Date(sInstance.getStartDate()));
        }
        archivedInstance.setStartedBy(sInstance.getStartedBy());
        archivedInstance.setStartedBySubstitute(sInstance.getStartedBySubstitute());
        if (sInstance.getEndDate() > 0) {
            archivedInstance.setEndDate(new Date(sInstance.getEndDate()));
        }
        if (sInstance.getArchiveDate() > 0) {
            archivedInstance.setArchiveDate(new Date(sInstance.getArchiveDate()));
        }
        if (sInstance.getLastUpdate() > 0) {
            archivedInstance.setLastUpdate(new Date(sInstance.getLastUpdate()));
        }
        archivedInstance.setProcessDefinitionId(sInstance.getProcessDefinitionId());
        archivedInstance.setDescription(sInstance.getDescription());
        archivedInstance.setSourceObjectId(sInstance.getSourceObjectId());
        archivedInstance.setRootProcessInstanceId(sInstance.getRootProcessInstanceId());
        archivedInstance.setCallerId(sInstance.getCallerId());

        if (sProcessDefinition != null) {
            for (int i = 1; i <= 5; i++) {
                archivedInstance.setStringIndexLabel(i, sProcessDefinition.getStringIndexLabel(i));
            }
        }
        archivedInstance.setStringIndexValue(1, sInstance.getStringIndex1());
        archivedInstance.setStringIndexValue(2, sInstance.getStringIndex2());
        archivedInstance.setStringIndexValue(3, sInstance.getStringIndex3());
        archivedInstance.setStringIndexValue(4, sInstance.getStringIndex4());
        archivedInstance.setStringIndexValue(5, sInstance.getStringIndex5());
        return archivedInstance;
    }

    public static List<Group> toGroups(final List<SGroup> sGroups) {
        final List<Group> clientGroups = new ArrayList<Group>();
        if (sGroups != null) {
            for (final SGroup sGroup : sGroups) {
                clientGroups.add(toGroup(sGroup));
            }
        }
        return Collections.unmodifiableList(clientGroups);
    }

    public static Group toGroup(final SGroup sGroup) {
        final GroupImpl group = new GroupImpl(sGroup.getId(), sGroup.getName());
        group.setParentPath(sGroup.getParentPath());
        group.setCreatedBy(sGroup.getCreatedBy());
        group.setCreationDate(new Date(sGroup.getCreationDate()));
        group.setDescription(sGroup.getDescription());
        group.setDisplayName(sGroup.getDisplayName());
        group.setIconName(sGroup.getIconName());
        group.setIconPath(sGroup.getIconPath());
        group.setLastUpdate(new Date(sGroup.getLastUpdate()));
        return group;
    }

    public static User toUser(final SUser sUser) {
        return toUser(sUser, null);
    }

    public static User toUser(final SUser sUser, final Map<Long, SUser> userIdToUser) {
        final UserImpl user = new UserImpl(sUser.getId(), sUser.getUserName(), "");
        user.setFirstName(sUser.getFirstName());
        user.setLastName(sUser.getLastName());
        user.setTitle(sUser.getTitle());
        user.setJobTitle(sUser.getJobTitle());
        user.setCreatedBy(sUser.getCreatedBy());
        user.setCreationDate(new Date(sUser.getCreationDate()));
        user.setIconName(sUser.getIconName());
        user.setIconPath(sUser.getIconPath());
        user.setLastUpdate(new Date(sUser.getLastUpdate()));
        user.setEnabled(sUser.isEnabled());
        final long managerUserId = sUser.getManagerUserId();
        user.setManagerUserId(managerUserId);
        if (managerUserId > 0 && userIdToUser != null) {
            user.setManagerUserName(userIdToUser.get(managerUserId).getUserName());
        }
        final SUserLogin sUserLogin = sUser.getSUserLogin();
        if (sUserLogin != null && sUserLogin.getLastConnection() != null) {
            user.setLastConnection(new Date(sUserLogin.getLastConnection()));
        }
        return user;
    }

    public static ContactData toUserContactData(final SContactInfo sContactData) {
        final ContactDataImpl contactData = new ContactDataImpl(sContactData.getUserId());
        contactData.setAddress(sContactData.getAddress());
        contactData.setBuilding(sContactData.getBuilding());
        contactData.setCity(sContactData.getCity());
        contactData.setCountry(sContactData.getCountry());
        contactData.setEmail(sContactData.getEmail());
        contactData.setFaxNumber(sContactData.getFaxNumber());
        contactData.setMobileNumber(sContactData.getMobileNumber());
        contactData.setPersonal(sContactData.isPersonal());
        contactData.setPhoneNumber(sContactData.getPhoneNumber());
        contactData.setRoom(sContactData.getRoom());
        contactData.setState(sContactData.getState());
        contactData.setWebsite(sContactData.getWebsite());
        contactData.setZipCode(sContactData.getZipCode());
        return contactData;
    }

    public static List<User> toUsers(final List<SUser> sUsers, final Map<Long, SUser> userIdToUser) {
        final List<User> users = new ArrayList<User>();
        if (sUsers != null) {
            for (final SUser sUser : sUsers) {
                final User user = ModelConvertor.toUser(sUser, userIdToUser);
                users.add(user);
            }
        }
        return Collections.unmodifiableList(users);
    }

    public static List<User> toUsers(final List<SUser> sUsers) {
        return toUsers(sUsers, null);
    }

    public static Role toRole(final SRole sRole) {
        final RoleImpl role = new RoleImpl(sRole.getId(), sRole.getName());
        role.setDisplayName(sRole.getDisplayName());
        role.setDescription(sRole.getDescription());
        role.setIconName(sRole.getIconName());
        role.setIconPath(sRole.getIconPath());
        role.setCreatedBy(sRole.getCreatedBy());
        role.setCreationDate(new Date(sRole.getCreationDate()));
        role.setLastUpdate(new Date(sRole.getLastUpdate()));
        return role;
    }

    public static List<Role> toRoles(final List<SRole> sRoles) {
        final List<Role> lightRoles = new ArrayList<Role>();
        if (sRoles != null) {
            for (final SRole sRole : sRoles) {
                final Role role = toRole(sRole);
                lightRoles.add(role);
            }
        }
        return Collections.unmodifiableList(lightRoles);
    }

    public static UserMembership toUserMembership(final SUserMembership sUserMembership) {
        final UserMembershipImpl userMembership = new UserMembershipImpl(sUserMembership.getId(), sUserMembership.getUserId(), sUserMembership.getGroupId(),
                sUserMembership.getRoleId());
        userMembership.setAssignedBy(sUserMembership.getAssignedBy());
        userMembership.setAssignedDate(new Date(sUserMembership.getAssignedDate()));
        userMembership.setGroupName(sUserMembership.getGroupName());
        userMembership.setRoleName(sUserMembership.getRoleName());
        userMembership.setUsername(sUserMembership.getUsername());
        return userMembership;
    }

    public static List<UserMembership> toUserMembership(final List<SUserMembership> sUserMemberships) {
        final List<UserMembership> userMemberships = new ArrayList<UserMembership>();
        if (sUserMemberships != null) {
            for (final SUserMembership sMembership : sUserMemberships) {
                final UserMembership userMembership = toUserMembership(sMembership);
                userMemberships.add(userMembership);
            }
        }
        return Collections.unmodifiableList(userMemberships);
    }

    public static List<UserMembership> toUserMembership(final List<SUserMembership> sUserMemberships, final Map<Long, String> userNames,
            final Map<Long, String> groupIdToGroup) {
        final List<UserMembership> userMemberships = new ArrayList<UserMembership>();
        if (sUserMemberships != null) {
            for (final SUserMembership sMembership : sUserMemberships) {
                final UserMembership userMembership = toUserMembership(sMembership, userNames, groupIdToGroup);
                userMemberships.add(userMembership);
            }
        }
        return Collections.unmodifiableList(userMemberships);
    }

    private static UserMembership toUserMembership(final SUserMembership sUserMembership, final Map<Long, String> userNames,
            final Map<Long, String> groupIdToGroup) {
        final UserMembershipImpl userMembership = new UserMembershipImpl(sUserMembership.getId(), sUserMembership.getUserId(), sUserMembership.getGroupId(),
                sUserMembership.getRoleId());
        userMembership.setGroupName(sUserMembership.getGroupName());
        userMembership.setGroupParentPath(groupIdToGroup.get(sUserMembership.getGroupId()));
        userMembership.setRoleName(sUserMembership.getRoleName());
        userMembership.setUsername(sUserMembership.getUsername());
        final long assignedBy = sUserMembership.getAssignedBy();
        userMembership.setAssignedBy(assignedBy);
        if (assignedBy > 0) {
            userMembership.setAssignedByName(userNames.get(assignedBy));
        }
        userMembership.setAssignedDate(new Date(sUserMembership.getAssignedDate()));
        return userMembership;
    }

    public static Category toCategory(final SCategory sCategory) {
        final CategoryImpl category = new CategoryImpl(sCategory.getId(), sCategory.getName());
        category.setDescription(sCategory.getDescription());
        category.setCreator(sCategory.getCreator());
        category.setCreationDate(new Date(sCategory.getCreationDate()));
        category.setLastUpdate(new Date(sCategory.getLastUpdateDate()));
        return category;
    }

    public static CommandDescriptor toCommandDescriptor(final SCommand command) {
        final CommandDescriptorImpl commandDescriptor = new CommandDescriptorImpl(command.getName(), command.getDescription(), command.getImplementation());
        commandDescriptor.setId(command.getId());
        commandDescriptor.setSystem(command.getSystem());
        return commandDescriptor;
    }

    public static CommandDescriptor toCommandDescriptor(final SPlatformCommand platformCommand) {
        final CommandDescriptorImpl commandDescriptor = new CommandDescriptorImpl(platformCommand.getName(), platformCommand.getDescription(),
                platformCommand.getImplementation());
        commandDescriptor.setId(platformCommand.getId());
        return commandDescriptor;
    }

    public static List<CommandDescriptor> toCommandDescriptors(final List<SCommand> sCommands) {
        if (sCommands != null) {
            final List<CommandDescriptor> commandList = new ArrayList<CommandDescriptor>();
            for (final SCommand sCommand : sCommands) {
                commandList.add(toCommandDescriptor(sCommand));
            }
            return Collections.unmodifiableList(commandList);
        }
        return Collections.emptyList();
    }

    public static List<CommandDescriptor> toPlatformCommandDescriptors(final List<SPlatformCommand> sPlatformCommands) {
        if (sPlatformCommands != null) {
            final List<CommandDescriptor> platformCommandList = new ArrayList<CommandDescriptor>();
            for (final SPlatformCommand sCommand : sPlatformCommands) {
                platformCommandList.add(toCommandDescriptor(sCommand));
            }
            return Collections.unmodifiableList(platformCommandList);
        }
        return Collections.emptyList();
    }

    public static List<Category> toCategories(final List<SCategory> sCategories) {
        if (sCategories != null) {
            final List<Category> categoryList = new ArrayList<Category>();
            for (final SCategory sCategory : sCategories) {
                categoryList.add(toCategory(sCategory));
            }
            return Collections.unmodifiableList(categoryList);
        }
        return Collections.emptyList();
    }

    public static List<EventInstance> toEventInstances(final Collection<SEventInstance> sEvents, final FlowNodeStateManager flowNodeStateManager) {
        final List<EventInstance> eventInstances = new ArrayList<EventInstance>();
        for (final SEventInstance sEvent : sEvents) {
            final EventInstance eventInstance = toEventInstance(sEvent, flowNodeStateManager);
            eventInstances.add(eventInstance);
        }
        return eventInstances;
    }

    public static EventInstance toEventInstance(final SEventInstance sEvent, final FlowNodeStateManager flowNodeStateManager) {
        final EventInstanceImpl eventInstance = getEventInstance(sEvent);
        updateFlowNode(eventInstance, sEvent, flowNodeStateManager.getState(sEvent.getStateId()).getName());
        return eventInstance;
    }

    public static List<EventTriggerInstance> toEventTriggerInstances(final List<SEventTriggerInstance> sEventTriggerInstances) {
        final List<EventTriggerInstance> eventTriggerInstances = new ArrayList<EventTriggerInstance>();
        for (final SEventTriggerInstance sEventTriggerInstance : sEventTriggerInstances) {
            final EventTriggerInstance eventTriggerInstance = toEventTriggerInstance(sEventTriggerInstance);
            if (eventTriggerInstance != null) {
                eventTriggerInstances.add(eventTriggerInstance);
            }
        }
        return eventTriggerInstances;
    }

    public static List<TimerEventTriggerInstance> toTimerEventTriggerInstances(final List<STimerEventTriggerInstance> sEventTriggerInstances) {
        final List<TimerEventTriggerInstance> eventTriggerInstances = new ArrayList<TimerEventTriggerInstance>();
        for (final STimerEventTriggerInstance sEventTriggerInstance : sEventTriggerInstances) {
            final TimerEventTriggerInstance eventTriggerInstance = toTimerEventTriggerInstance(sEventTriggerInstance);
            if (eventTriggerInstance != null) {
                eventTriggerInstances.add(eventTriggerInstance);
            }
        }
        return eventTriggerInstances;
    }

    public static EventTriggerInstance toEventTriggerInstance(final SEventTriggerInstance sEventTriggerInstance) {
        EventTriggerInstance eventTriggerInstance = null;
        switch (sEventTriggerInstance.getEventTriggerType()) {
            case ERROR:
                // Not support for now
                break;
            case TIMER:
                eventTriggerInstance = toTimerEventTriggerInstance((STimerEventTriggerInstance) sEventTriggerInstance);
                break;
            case SIGNAL:
                // Not support for now
                break;
            case MESSAGE:
                // Not support for now
                break;
            case TERMINATE:
                // Not support for now
                break;
            default:
                throw new UnknownElementType(sEventTriggerInstance.getClass().getName());

        }
        return eventTriggerInstance;
    }

    public static TimerEventTriggerInstance toTimerEventTriggerInstance(final STimerEventTriggerInstance sTimerEventTriggerInstance) {
        return new TimerEventTriggerInstanceImpl(sTimerEventTriggerInstance.getId(), sTimerEventTriggerInstance.getEventInstanceId(),
                sTimerEventTriggerInstance.getEventInstanceName(), new Date(sTimerEventTriggerInstance.getExecutionDate()));
    }

    public static WaitingEvent toWaitingEvent(final SWaitingEvent sWaitingEvent) {
        WaitingEvent waitingEvent;
        final BPMEventType bpmEventType = BPMEventType.valueOf(sWaitingEvent.getEventType().name());
        final long processDefinitionId = sWaitingEvent.getProcessDefinitionId();
        final String processName = sWaitingEvent.getProcessName();
        final long flowNodeDefinitionId = sWaitingEvent.getFlowNodeDefinitionId();
        switch (sWaitingEvent.getEventTriggerType()) {
            case ERROR:
                final SWaitingErrorEvent sWaitingErrorEvent = (SWaitingErrorEvent) sWaitingEvent;
                waitingEvent = new WaitingErrorEventImpl(bpmEventType, processDefinitionId, processName, flowNodeDefinitionId,
                        sWaitingErrorEvent.getErrorCode());
                break;
            case MESSAGE:
                final SWaitingMessageEvent sWaitingMessageEvent = (SWaitingMessageEvent) sWaitingEvent;
                waitingEvent = new WaitingMessageEventImpl(bpmEventType, processDefinitionId, processName, flowNodeDefinitionId,
                        sWaitingMessageEvent.getMessageName());
                break;
            case SIGNAL:
                final SWaitingSignalEvent sWaitingSignalEvent = (SWaitingSignalEvent) sWaitingEvent;
                waitingEvent = new WaitingSignalEventImpl(bpmEventType, processDefinitionId, processName, flowNodeDefinitionId,
                        sWaitingSignalEvent.getSignalName());
                break;
            default:
                throw new UnknownElementType(sWaitingEvent.getClass().getName());
        }
        return waitingEvent;
    }

    public static List<WaitingEvent> toWaitingEvents(final List<SWaitingEvent> sWaitingEvents) {
        final List<WaitingEvent> waitingEvents = new ArrayList<WaitingEvent>(sWaitingEvents.size());
        for (final SWaitingEvent sWaitingEvent : sWaitingEvents) {
            waitingEvents.add(toWaitingEvent(sWaitingEvent));
        }
        return Collections.unmodifiableList(waitingEvents);
    }

    private static EventInstanceImpl getEventInstance(final SEventInstance sEvent) {
        switch (sEvent.getType()) {
            case END_EVENT:
                return new EndEventInstanceImpl(sEvent.getName(), sEvent.getFlowNodeDefinitionId());
            case INTERMEDIATE_CATCH_EVENT:
                return new IntermediateCatchEventInstanceImpl(sEvent.getName(), sEvent.getFlowNodeDefinitionId());
            case INTERMEDIATE_THROW_EVENT:
                return new IntermediateThrowEventInstanceImpl(sEvent.getName(), sEvent.getFlowNodeDefinitionId());
            case BOUNDARY_EVENT:
                return new BoundaryEventInstanceImpl(sEvent.getName(), sEvent.getFlowNodeDefinitionId(),
                        ((SBoundaryEventInstance) sEvent).getActivityInstanceId());
            case START_EVENT:
                return new StartEventInstanceImpl(sEvent.getName(), sEvent.getFlowNodeDefinitionId());
            default:
                throw new UnknownElementType(sEvent.getType().name());
        }
    }

    public static List<DataInstance> toDataInstances(final List<SDataInstance> sDataInstances) {
        if (sDataInstances != null) {
            final List<DataInstance> dataInstanceList = new ArrayList<DataInstance>();
            for (final SDataInstance sDataInstance : sDataInstances) {
                dataInstanceList.add(toDataInstance(sDataInstance));
            }
            return Collections.unmodifiableList(dataInstanceList);
        }
        return Collections.emptyList();
    }

    public static List<DataDefinition> toDataDefinitions(final List<SDataDefinition> sDataDefinitions) {
        if (sDataDefinitions != null) {
            final List<DataDefinition> dataDefinitionList = new ArrayList<DataDefinition>();
            for (final SDataDefinition sDataDefinition : sDataDefinitions) {
                dataDefinitionList.add(toDataDefinition(sDataDefinition));
            }
            return Collections.unmodifiableList(dataDefinitionList);
        }
        return Collections.emptyList();
    }

    public static DataDefinition toDataDefinition(final SDataDefinition sDataDefinition) {
        DataDefinitionImpl dataDefinitionImpl = null;
        if (sDataDefinition != null) {
            dataDefinitionImpl = new DataDefinitionImpl(sDataDefinition.getName(), toExpression(sDataDefinition.getDefaultValueExpression()));
            dataDefinitionImpl.setClassName(sDataDefinition.getClassName());
            dataDefinitionImpl.setDescription(sDataDefinition.getDescription());
            dataDefinitionImpl.setTransientData(sDataDefinition.isTransientData());
        }
        return dataDefinitionImpl;
    }

    public static List<Expression> toExpressions(final List<SExpression> sExpressions) {
        if (sExpressions != null && !sExpressions.isEmpty()) {
            final List<Expression> expList = new ArrayList<Expression>(sExpressions.size());
            for (final SExpression sexp : sExpressions) {
                expList.add(toExpression(sexp));
            }
            return expList;
        }
        return Collections.emptyList();
    }

    public static Expression toExpression(final SExpression sexp) {
        final ExpressionImpl exp = new ExpressionImpl();
        if (sexp != null) {
            exp.setContent(sexp.getContent());
            exp.setExpressionType(sexp.getExpressionType());
            exp.setInterpreter(sexp.getInterpreter());
            exp.setName(sexp.getName());
            exp.setReturnType(sexp.getReturnType());
            exp.setDependencies(toExpressions(sexp.getDependencies()));
        }
        return exp;
    }

    public static DataInstance toDataInstance(final SDataInstance sDataInstance) {
        DataInstanceImpl dataInstance;
        if (sDataInstance.getClassName().equals(Integer.class.getName())) {
            dataInstance = new IntegerDataInstanceImpl();
        } else if (sDataInstance.getClassName().equals(Long.class.getName())) {
            dataInstance = new LongDataInstanceImpl();
        } else if (sDataInstance.getClassName().equals(Boolean.class.getName())) {
            dataInstance = new BooleanDataInstanceImpl();
        } else if (sDataInstance.getClassName().equals(Date.class.getName())) {
            dataInstance = new DateDataInstanceImpl();
        } else if (sDataInstance.getClassName().equals(Double.class.getName())) {
            dataInstance = new DoubleDataInstanceImpl();
        } else if (sDataInstance.getClassName().equals(Float.class.getName())) {
            dataInstance = new FloatDataInstanceImpl();
        } else if (sDataInstance.getClassName().equals(String.class.getName())) {
            dataInstance = new ShortTextDataInstanceImpl();
        } else {
            dataInstance = new BlobDataInstanceImpl();
        }
        dataInstance.setTransientData(sDataInstance.isTransientData());
        dataInstance.setClassName(sDataInstance.getClassName());
        dataInstance.setContainerId(sDataInstance.getContainerId());
        dataInstance.setContainerType(sDataInstance.getContainerType());
        dataInstance.setDataTypeClassName(sDataInstance.getClassName());
        dataInstance.setDescription(sDataInstance.getDescription());
        dataInstance.setId(sDataInstance.getId());
        dataInstance.setName(sDataInstance.getName());
        dataInstance.setValue(sDataInstance.getValue());
        return dataInstance;
    }

    public static List<ArchivedDataInstance> toArchivedDataInstances(final List<SADataInstance> sADataInstances) {
        final List<ArchivedDataInstance> dataInstances = new ArrayList<ArchivedDataInstance>();
        for (final SADataInstance sADataInstance : sADataInstances) {
            final ArchivedDataInstance dataInstance = toArchivedDataInstance(sADataInstance);
            dataInstances.add(dataInstance);
        }
        return dataInstances;
    }

    public static ArchivedDataInstance toArchivedDataInstance(final SADataInstance sDataInstance) {
        final ArchivedDataInstanceImpl dataInstance = new ArchivedDataInstanceImpl();
        dataInstance.setClassName(sDataInstance.getClassName());
        dataInstance.setContainerId(sDataInstance.getContainerId());
        dataInstance.setContainerType(sDataInstance.getContainerType());
        dataInstance.setDataTypeClassName(sDataInstance.getClassName());
        dataInstance.setDescription(sDataInstance.getDescription());
        dataInstance.setId(sDataInstance.getId());
        dataInstance.setName(sDataInstance.getName());
        dataInstance.setValue(sDataInstance.getValue());
        dataInstance.setArchiveDate(new Date(sDataInstance.getArchiveDate()));
        dataInstance.setSourceObjectId(sDataInstance.getSourceObjectId());
        return dataInstance;
    }

    public static ActorMember toActorMember(final SActorMember sActorMember) {
        return new ActorMemberImpl(sActorMember.getId(), sActorMember.getUserId(), sActorMember.getGroupId(), sActorMember.getRoleId());
    }

    public static List<ActorMember> toActorMembers(final List<SActorMember> sActorMembers) {
        final List<ActorMember> actorMembers = new ArrayList<ActorMember>();
        for (final SActorMember sActorMember : sActorMembers) {
            final ActorMember actorMember = toActorMember(sActorMember);
            actorMembers.add(actorMember);
        }
        return actorMembers;
    }

    public static ActorInstance toActorInstance(final SActor actor) {
        final String name = actor.getName();
        final String description = actor.getDescription();
        final long scopeId = actor.getScopeId();
        final String displayName = actor.getDisplayName();
        final boolean initiator = actor.isInitiator();
        final ActorInstanceImpl actorInstance = new ActorInstanceImpl(name, description, displayName, scopeId, initiator);
        actorInstance.setId(actor.getId());
        return actorInstance;
    }

    public static SUser constructSUser(final UserCreator creator) {
        final long now = System.currentTimeMillis();
        final SUserBuilder userBuilder = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance();
        final Map<UserField, Serializable> fields = creator.getFields();
        userBuilder.setUserName((String) fields.get(UserField.NAME));
        userBuilder.setPassword((String) fields.get(UserField.PASSWORD));
        final String firstName = (String) fields.get(UserField.FIRST_NAME);
        if (firstName != null) {
            userBuilder.setFirstName(firstName);
        }
        final String lastName = (String) fields.get(UserField.LAST_NAME);
        if (lastName != null) {
            userBuilder.setLastName(lastName);
        }
        final String iconName = (String) fields.get(UserField.ICON_NAME);
        if (iconName != null) {
            userBuilder.setIconName(iconName);
        }
        final String iconPath = (String) fields.get(UserField.ICON_PATH);
        if (iconPath != null) {
            userBuilder.setIconPath(iconPath);
        }
        final String jobTitle = (String) fields.get(UserField.JOB_TITLE);
        if (jobTitle != null) {
            userBuilder.setJobTitle(jobTitle);
        }
        final String title = (String) fields.get(UserField.TITLE);
        if (title != null) {
            userBuilder.setTitle(title);
        }
        userBuilder.setCreatedBy(SessionInfos.getUserIdFromSession());

        final Long managerUserId = (Long) fields.get(UserField.MANAGER_ID);
        if (managerUserId != null) {
            userBuilder.setManagerUserId(managerUserId);
        }

        final Boolean enabled = (Boolean) fields.get(UserField.ENABLED);
        if (enabled != null) {
            userBuilder.setEnabled(enabled);
        } else {
            userBuilder.setEnabled(Boolean.FALSE);
        }
        userBuilder.setCreationDate(now);
        userBuilder.setLastUpdate(now);
        return userBuilder.done();
    }

    public static SContactInfo constructSUserContactInfo(final UserCreator creator, final long userId, final boolean personal) {
        Map<ContactDataField, Serializable> fields;
        if (personal) {
            fields = creator.getPersoFields();
        } else {
            fields = creator.getProFields();
        }
        if (fields != null && !fields.isEmpty()) {
            final SContactInfoBuilder contactInfoBuilder = BuilderFactory.get(SContactInfoBuilderFactory.class).createNewInstance(userId, personal);
            final String address = (String) fields.get(ContactDataField.ADDRESS);
            if (address != null) {
                contactInfoBuilder.setAddress(address);
            }
            final String email = (String) fields.get(ContactDataField.EMAIL);
            if (email != null) {
                contactInfoBuilder.setEmail(email);
            }
            final String building = (String) fields.get(ContactDataField.BUILDING);
            if (building != null) {
                contactInfoBuilder.setBuilding(building);
            }
            final String city = (String) fields.get(ContactDataField.CITY);
            if (city != null) {
                contactInfoBuilder.setCity(city);
            }
            final String country = (String) fields.get(ContactDataField.COUNTRY);
            if (country != null) {
                contactInfoBuilder.setCountry(country);
            }
            final String fax = (String) fields.get(ContactDataField.FAX);
            if (fax != null) {
                contactInfoBuilder.setFaxNumber(fax);
            }
            final String mobile = (String) fields.get(ContactDataField.MOBILE);
            if (mobile != null) {
                contactInfoBuilder.setMobileNumber(mobile);
            }
            final String phone = (String) fields.get(ContactDataField.PHONE);
            if (phone != null) {
                contactInfoBuilder.setPhoneNumber(phone);
            }
            final String room = (String) fields.get(ContactDataField.ROOM);
            if (room != null) {
                contactInfoBuilder.setRoom(room);
            }
            final String state = (String) fields.get(ContactDataField.STATE);
            if (state != null) {
                contactInfoBuilder.setState(state);
            }
            final String website = (String) fields.get(ContactDataField.WEBSITE);
            if (website != null) {
                contactInfoBuilder.setWebsite(website);
            }
            final String zipCode = (String) fields.get(ContactDataField.ZIP_CODE);
            if (zipCode != null) {
                contactInfoBuilder.setZipCode(zipCode);
            }
            return contactInfoBuilder.done();
        }
        return null;
    }

    public static SContactInfo constructSUserContactInfo(final ExportedUser user, final boolean isPersonal, final long userId) {
        final SContactInfoBuilder contactInfoBuilder = BuilderFactory.get(SContactInfoBuilderFactory.class).createNewInstance(userId, isPersonal);
        if (isPersonal) {
            contactInfoBuilder.setAddress(user.getPersonalAddress());
            contactInfoBuilder.setBuilding(user.getPersonalBuilding());
            contactInfoBuilder.setCity(user.getPersonalCity());
            contactInfoBuilder.setCountry(user.getPersonalCountry());
            contactInfoBuilder.setEmail(user.getPersonalEmail());
            contactInfoBuilder.setFaxNumber(user.getPersonalFaxNumber());
            contactInfoBuilder.setMobileNumber(user.getPersonalMobileNumber());
            contactInfoBuilder.setPhoneNumber(user.getPersonalPhoneNumber());
            contactInfoBuilder.setRoom(user.getPersonalRoom());
            contactInfoBuilder.setState(user.getPersonalState());
            contactInfoBuilder.setWebsite(user.getPersonalWebsite());
            contactInfoBuilder.setZipCode(user.getPersonalZipCode());
        } else {
            contactInfoBuilder.setAddress(user.getProfessionalAddress());
            contactInfoBuilder.setBuilding(user.getProfessionalBuilding());
            contactInfoBuilder.setCity(user.getProfessionalCity());
            contactInfoBuilder.setCountry(user.getProfessionalCountry());
            contactInfoBuilder.setEmail(user.getProfessionalEmail());
            contactInfoBuilder.setFaxNumber(user.getProfessionalFaxNumber());
            contactInfoBuilder.setMobileNumber(user.getProfessionalMobileNumber());
            contactInfoBuilder.setPhoneNumber(user.getProfessionalPhoneNumber());
            contactInfoBuilder.setRoom(user.getProfessionalRoom());
            contactInfoBuilder.setState(user.getProfessionalState());
            contactInfoBuilder.setWebsite(user.getProfessionalWebsite());
            contactInfoBuilder.setZipCode(user.getProfessionalZipCode());
        }
        return contactInfoBuilder.done();
    }

    public static SRole constructSRole(final RoleCreator creator) {
        final long now = System.currentTimeMillis();
        final SRoleBuilder roleBuilder = BuilderFactory.get(SRoleBuilderFactory.class).createNewInstance();
        roleBuilder.setCreatedBy(SessionInfos.getUserIdFromSession());
        roleBuilder.setCreationDate(now).setLastUpdate(now);
        final Map<RoleField, Serializable> fields = creator.getFields();
        roleBuilder.setName((String) fields.get(RoleField.NAME));
        final String displayName = (String) fields.get(RoleField.DISPLAY_NAME);
        if (displayName != null) {
            roleBuilder.setDisplayName(displayName);
        }
        final String description = (String) fields.get(RoleField.DESCRIPTION);
        if (description != null) {
            roleBuilder.setDescription(description);
        }
        final String iconName = (String) fields.get(RoleField.ICON_NAME);
        if (iconName != null) {
            roleBuilder.setIconName(iconName);
        }
        final String iconPath = (String) fields.get(RoleField.ICON_PATH);
        if (iconPath != null) {
            roleBuilder.setIconPath(iconPath);
        }
        return roleBuilder.done();
    }

    public static SGroup constructSGroup(final GroupCreator creator) {
        final long now = System.currentTimeMillis();
        final SGroupBuilder groupBuilder = BuilderFactory.get(SGroupBuilderFactory.class).createNewInstance();
        groupBuilder.setCreatedBy(SessionInfos.getUserIdFromSession());
        groupBuilder.setCreationDate(now).setLastUpdate(now);
        final Map<GroupField, Serializable> fields = creator.getFields();
        groupBuilder.setName((String) fields.get(GroupField.NAME));
        final String parentPath = (String) fields.get(GroupField.PARENT_PATH);
        if (parentPath != null && !parentPath.isEmpty()) {
            groupBuilder.setParentPath(parentPath);
        }
        final String displayName = (String) fields.get(GroupField.DISPLAY_NAME);
        if (displayName != null) {
            groupBuilder.setDisplayName(displayName);
        }
        final String description = (String) fields.get(GroupField.DESCRIPTION);
        if (description != null) {
            groupBuilder.setDescription(description);
        }
        final String iconName = (String) fields.get(GroupField.ICON_NAME);
        if (iconName != null) {
            groupBuilder.setIconName(iconName);
        }
        final String iconPath = (String) fields.get(GroupField.ICON_PATH);
        if (iconPath != null) {
            groupBuilder.setIconPath(iconPath);
        }
        return groupBuilder.done();
    }

    public static List<ProcessSupervisor> toProcessSupervisors(final List<SProcessSupervisor> sSupervisors) {
        final List<ProcessSupervisor> processSupervisors = new ArrayList<ProcessSupervisor>();
        if (sSupervisors != null) {
            for (final SProcessSupervisor sSupervisor : sSupervisors) {
                processSupervisors.add(toProcessSupervisor(sSupervisor));
            }
        }
        return processSupervisors;
    }

    public static ProcessSupervisor toProcessSupervisor(final SProcessSupervisor sSupervisor) {
        final ProcessSupervisorImpl supervisor = new ProcessSupervisorImpl();
        supervisor.setId(sSupervisor.getId());
        supervisor.setProcessDefinitionId(sSupervisor.getProcessDefId());
        supervisor.setUserId(sSupervisor.getUserId());
        supervisor.setGroupId(sSupervisor.getGroupId());
        supervisor.setRoleId(sSupervisor.getRoleId());
        return supervisor;
    }

    public static List<Document> toDocuments(final Collection<SMappedDocument> mappedDocuments, final DocumentService documentService) {
        final List<Document> documents = new ArrayList<Document>();
        for (final SMappedDocument mappedDocument : mappedDocuments) {
            final Document document = toDocument(mappedDocument, documentService);
            documents.add(document);
        }
        return documents;
    }

    public static Document toDocument(final SMappedDocument mappedDocument, final DocumentService documentService) {

        final DocumentImpl documentImpl = new DocumentImpl();
        if (mappedDocument instanceof SAMappedDocument) {
            documentImpl.setId(((SAMappedDocument) mappedDocument).getSourceObjectId());
        } else {
            documentImpl.setId(mappedDocument.getId());
        }
        setDocumentFields(mappedDocument, documentService, documentImpl);
        return documentImpl;
    }

    private static void setDocumentFields(final SMappedDocument mappedDocument, final DocumentService documentService, final DocumentImpl documentImpl) {
        documentImpl.setProcessInstanceId(mappedDocument.getProcessInstanceId());
        documentImpl.setName(mappedDocument.getName());
        documentImpl.setDescription(mappedDocument.getDescription());
        documentImpl.setVersion(mappedDocument.getVersion());
        documentImpl.setAuthor(mappedDocument.getAuthor());
        documentImpl.setCreationDate(new Date(mappedDocument.getCreationDate()));
        documentImpl.setHasContent(mappedDocument.hasContent());
        documentImpl.setContentMimeType(mappedDocument.getMimeType());
        documentImpl.setFileName(mappedDocument.getFileName());
        documentImpl.setContentStorageId(String.valueOf(mappedDocument.getDocumentId()));
        documentImpl.setIndex(mappedDocument.getIndex());
        if (mappedDocument.hasContent()) {
            documentImpl.setUrl(documentService.generateDocumentURL(mappedDocument.getFileName(), String.valueOf(mappedDocument.getDocumentId())));
        } else {
            documentImpl.setUrl(mappedDocument.getUrl());
        }
    }

    public static List<ArchivedDocument> toArchivedDocuments(final Collection<SAMappedDocument> mappedDocuments, final DocumentService documentService) {
        final List<ArchivedDocument> documents = new ArrayList<ArchivedDocument>();
        for (final SAMappedDocument mappedDocument : mappedDocuments) {
            final ArchivedDocument document = toArchivedDocument(mappedDocument, documentService);
            documents.add(document);
        }
        return documents;
    }

    public static ArchivedDocument toArchivedDocument(final SAMappedDocument mappedDocument, final DocumentService documentService) {
        final ArchivedDocumentImpl documentImpl = new ArchivedDocumentImpl(mappedDocument.getName());
        documentImpl.setId(mappedDocument.getId());
        setDocumentFields(mappedDocument, documentService, documentImpl);
        documentImpl.setArchiveDate(new Date(mappedDocument.getArchiveDate()));
        documentImpl.setSourceObjectId(mappedDocument.getSourceObjectId());
        return documentImpl;
    }

    public static int getServerActivityStateId(final String state) {
        int stateId = -1;
        if (state.equalsIgnoreCase(ActivityStates.READY_STATE)) {
            stateId = 4;
        } else if (state.equalsIgnoreCase(ActivityStates.COMPLETING_STATE)) {
            stateId = 9;
        } else if (state.equalsIgnoreCase(ActivityStates.COMPLETED_STATE)) {
            stateId = 2;
        } else if (state.equalsIgnoreCase(ActivityStates.EXECUTING_STATE)) {
            stateId = 1;
        } else if (state.equalsIgnoreCase(ActivityStates.INITIALIZING_STATE)) {
            stateId = 0;
        } else if (state.equalsIgnoreCase(ActivityStates.SKIPPED_STATE)) {
            stateId = 12;
        } else if (state.equalsIgnoreCase(ActivityStates.CANCELLING_SUBTASKS_STATE)) {
            stateId = 13;
        } else if (state.equalsIgnoreCase(ActivityStates.CANCELLED_STATE)) {
            stateId = 14;
        }
        return stateId;
    }

    public static ProcessInstanceState getProcessInstanceState(final String state) {
        if (state != null) {
            if (state.equalsIgnoreCase(ProcessInstanceState.ABORTED.toString())) {
                return ProcessInstanceState.ABORTED;
            } else if (state.equalsIgnoreCase(ProcessInstanceState.CANCELLED.toString())) {
                return ProcessInstanceState.CANCELLED;
            } else if (state.equalsIgnoreCase(ProcessInstanceState.COMPLETED.toString())) {
                return ProcessInstanceState.COMPLETED;
            } else if (state.equalsIgnoreCase(ProcessInstanceState.COMPLETING.toString())) {
                return ProcessInstanceState.COMPLETING;
            } else if (state.equalsIgnoreCase(ProcessInstanceState.ERROR.toString())) {
                return ProcessInstanceState.ERROR;
            } else if (state.equalsIgnoreCase(ProcessInstanceState.INITIALIZING.toString())) {
                return ProcessInstanceState.INITIALIZING;
            } else if (state.equalsIgnoreCase(ProcessInstanceState.STARTED.toString())) {
                return ProcessInstanceState.STARTED;
            } else if (state.equalsIgnoreCase(ProcessInstanceState.SUSPENDED.toString())) {
                return ProcessInstanceState.SUSPENDED;
            }
        }
        throw new IllegalArgumentException("Invalid process instance state: " + state);
    }

    public static Comment toComment(final SComment sComment) {
        final CommentImpl commentImpl = new CommentImpl();
        commentImpl.setTenantId(sComment.getTenantId());
        commentImpl.setId(sComment.getId());
        commentImpl.setUserId(sComment.getUserId());
        commentImpl.setProcessInstanceId(sComment.getProcessInstanceId());
        commentImpl.setPostDate(sComment.getPostDate());
        commentImpl.setContent(sComment.getContent());
        return commentImpl;
    }

    public static List<Comment> toComments(final List<SComment> sComments) {
        final List<Comment> comments = new ArrayList<Comment>();
        for (final SComment sComment : sComments) {
            comments.add(toComment(sComment));
        }
        return comments;
    }

    public static Map<String, SExpression> constructExpressions(final Map<String, Expression> inputs) {

        final Map<String, SExpression> result = new HashMap<String, SExpression>(inputs.size());
        for (final Entry<String, Expression> expression : inputs.entrySet()) {
            result.put(expression.getKey(), constructSExpression(expression.getValue()));
        }
        return result;
    }

    public static SExpression constructSExpression(final Expression model) {
        final ArrayList<SExpression> dependencies = new ArrayList<SExpression>();
        for (final Expression dep : model.getDependencies()) {
            dependencies.add(constructSExpression(dep));
        }
        final SExpressionBuilder expressionBuilder = BuilderFactory.get(SExpressionBuilderFactory.class).createNewInstance();
        expressionBuilder.setName(model.getName());
        expressionBuilder.setContent(model.getContent());
        expressionBuilder.setExpressionType(model.getExpressionType());
        expressionBuilder.setInterpreter(model.getInterpreter());
        expressionBuilder.setReturnType(model.getReturnType());
        expressionBuilder.setDependencies(dependencies);
        try {
            return expressionBuilder.done();
        } catch (final SInvalidExpressionException e) {
            throw new IllegalArgumentException("Error constructing SExpression");
        }
    }

    public static SOperation convertOperation(final Operation operation) {
        if (operation == null) {
            return null;
        }
        return BuilderFactory
                .get(SOperationBuilderFactory.class)
                .createNewInstance()
                .setOperator(operation.getOperator())
                .setType(SOperatorType.valueOf(operation.getType().name()))
                .setRightOperand(ModelConvertor.constructSExpression(operation.getRightOperand()))
                .setLeftOperand(
                        BuilderFactory.get(SLeftOperandBuilderFactory.class).createNewInstance().setName(operation.getLeftOperand().getName())
                                .setType(operation.getLeftOperand().getType()).done()).done();
    }

    public static List<SOperation> convertOperations(final List<Operation> operations) {
        if (operations == null) {
            return Collections.emptyList();
        }
        final List<SOperation> sOperations = new ArrayList<SOperation>(operations.size());
        for (final Operation operation : operations) {
            sOperations.add(convertOperation(operation));
        }
        return sOperations;
    }

    public static List<ConnectorImplementationDescriptor> toConnectorImplementationDescriptors(
            final List<SConnectorImplementationDescriptor> sConnectorImplementationDescriptors) {
        if (sConnectorImplementationDescriptors != null) {
            final List<ConnectorImplementationDescriptor> connectorImplementationDescriptors = new ArrayList<ConnectorImplementationDescriptor>(
                    sConnectorImplementationDescriptors.size());
            for (final SConnectorImplementationDescriptor sConnectorImplementationDescriptor : sConnectorImplementationDescriptors) {
                connectorImplementationDescriptors.add(toConnectorImplementationDescriptor(sConnectorImplementationDescriptor));
            }
            return connectorImplementationDescriptors;
        }
        return Collections.emptyList();
    }

    public static ConnectorImplementationDescriptor toConnectorImplementationDescriptor(
            final SConnectorImplementationDescriptor sConnectorImplementationDescriptor) {
        return new ConnectorImplementationDescriptor(sConnectorImplementationDescriptor.getImplementationClassName(),
                sConnectorImplementationDescriptor.getId(), sConnectorImplementationDescriptor.getVersion(),
                sConnectorImplementationDescriptor.getDefinitionId(), sConnectorImplementationDescriptor.getDefinitionVersion(),
                sConnectorImplementationDescriptor.getJarDependencies().getDependencies());
    }

    public static List<ArchivedComment> toArchivedComments(final List<SAComment> serverObjects) {
        final List<ArchivedComment> commments = new ArrayList<ArchivedComment>();
        for (final SAComment saComment : serverObjects) {
            final ArchivedComment comment = toArchivedComment(saComment);
            commments.add(comment);
        }
        return commments;
    }

    public static ArchivedComment toArchivedComment(final SAComment saComment) {
        final ArchivedCommentImpl commentImpl = new ArchivedCommentImpl(saComment.getContent());
        commentImpl.setId(saComment.getId());
        commentImpl.setProcessInstanceId(saComment.getProcessInstanceId());
        commentImpl.setArchiveDate(new Date(saComment.getArchiveDate()));
        commentImpl.setContent(saComment.getContent());
        commentImpl.setSourceObjectId(saComment.getSourceObjectId());
        commentImpl.setUserId(saComment.getUserId());
        commentImpl.setPostDate(new Date(saComment.getPostDate()));
        return commentImpl;
    }

    public static Operation toOperation(final SOperation operation) {
        final OperationImpl operationImpl = new OperationImpl();
        operationImpl.setRightOperand(toExpression(operation.getRightOperand()));
        operationImpl.setOperator(operation.getOperator());
        operationImpl.setType(toOperatorType(operation.getType()));
        final LeftOperandImpl leftOperand = new LeftOperandImpl();
        final SLeftOperand sLeftOperand = operation.getLeftOperand();
        leftOperand.setName(sLeftOperand.getName());
        leftOperand.setType(sLeftOperand.getType());
        operationImpl.setLeftOperand(leftOperand);
        return operationImpl;
    }

    private static OperatorType toOperatorType(final SOperatorType type) {
        OperatorType operatorType = null;
        if (SOperatorType.ASSIGNMENT.equals(type)) {
            operatorType = OperatorType.ASSIGNMENT;
        } else if (SOperatorType.JAVA_METHOD.equals(type)) {
            operatorType = OperatorType.JAVA_METHOD;
        } else if (SOperatorType.XPATH_UPDATE_QUERY.equals(type)) {
            operatorType = OperatorType.XPATH_UPDATE_QUERY;
        }
        return operatorType;
    }

    public static ConnectorDefinition toConnectorDefinition(final SConnectorDefinition connector) {
        final ConnectorDefinitionImpl connectorDefinitionImpl = new ConnectorDefinitionImpl(connector.getName(), connector.getConnectorId(),
                connector.getVersion(), connector.getActivationEvent());
        // connectorDefinitionImpl.setId(connector.getId());
        for (final Entry<String, SExpression> input : connector.getInputs().entrySet()) {
            connectorDefinitionImpl.addInput(input.getKey(), toExpression(input.getValue()));
        }
        for (final SOperation operation : connector.getOutputs()) {
            connectorDefinitionImpl.addOutput(toOperation(operation));
        }
        return connectorDefinitionImpl;
    }

    public static List<ActorInstance> toActors(final List<SActor> sActors) {
        final List<ActorInstance> actors = new ArrayList<ActorInstance>();
        for (final SActor sActor : sActors) {
            final ActorInstance actor = toActorInstance(sActor);
            actors.add(actor);
        }
        return actors;
    }

    public static List<ArchivedFlowNodeInstance> toArchivedFlowNodeInstances(final List<SAFlowNodeInstance> saFlowNodes,
            final FlowNodeStateManager flowNodeStateManager) {
        final List<ArchivedFlowNodeInstance> flowNodeInstances = new ArrayList<ArchivedFlowNodeInstance>();
        for (final SAFlowNodeInstance saFlowNode : saFlowNodes) {
            final ArchivedFlowNodeInstance flowNodeInstance = toArchivedFlowNodeInstance(saFlowNode, flowNodeStateManager);
            flowNodeInstances.add(flowNodeInstance);
        }
        return flowNodeInstances;
    }

    public static ArchivedFlowNodeInstance toArchivedFlowNodeInstance(final SAFlowNodeInstance saFlowNode, final FlowNodeStateManager flowNodeStateManager) {
        ArchivedFlowNodeInstance archiveFlowNodeInstance = null;
        switch (saFlowNode.getType()) {
            case AUTOMATIC_TASK:
                archiveFlowNodeInstance = toArchivedAutomaticTaskInstance((SAAutomaticTaskInstance) saFlowNode, flowNodeStateManager);
                break;
            case MANUAL_TASK:
                archiveFlowNodeInstance = toArchivedManualTaskInstance((SAManualTaskInstance) saFlowNode, flowNodeStateManager);
                break;
            case USER_TASK:
                archiveFlowNodeInstance = toArchivedUserTaskInstance((SAUserTaskInstance) saFlowNode, flowNodeStateManager);
                break;
            case RECEIVE_TASK:
                archiveFlowNodeInstance = toArchivedReceiveTaskInstance((SAReceiveTaskInstance) saFlowNode, flowNodeStateManager);
                break;
            case SEND_TASK:
                archiveFlowNodeInstance = toArchivedSendTaskInstance((SASendTaskInstance) saFlowNode, flowNodeStateManager);
                break;
            case CALL_ACTIVITY:
                archiveFlowNodeInstance = toArchivedCallActivityInstance((SACallActivityInstance) saFlowNode, flowNodeStateManager);
                break;
            case LOOP_ACTIVITY:
                archiveFlowNodeInstance = toArchivedLoopActivityInstance((SALoopActivityInstance) saFlowNode, flowNodeStateManager);
                break;
            case SUB_PROCESS:
                archiveFlowNodeInstance = toArchivedSubProcessActivityInstance((SASubProcessActivityInstance) saFlowNode, flowNodeStateManager);
                break;
            case GATEWAY:
                archiveFlowNodeInstance = toArchivedGatewayInstance((SAGatewayInstance) saFlowNode, flowNodeStateManager);
                break;
            case MULTI_INSTANCE_ACTIVITY:
                archiveFlowNodeInstance = toArchivedMultiInstanceActivityInstance((SAMultiInstanceActivityInstance) saFlowNode, flowNodeStateManager);
                break;
            case BOUNDARY_EVENT:
            case START_EVENT:
            case INTERMEDIATE_CATCH_EVENT:
            case INTERMEDIATE_THROW_EVENT:
            case END_EVENT:
                // archiveFlowNodeInstance = toArchivedEventInstance((SAEventInstance) saFlowNode, flowNodeStateManager);
                break;
            default:
                throw new UnknownElementType(saFlowNode.getType().name());
        }
        return archiveFlowNodeInstance;
    }

    public static List<ConnectorInstance> toConnectorInstances(final List<SConnectorInstance> sConnectorInstances) {
        final ArrayList<ConnectorInstance> connectorInstances = new ArrayList<ConnectorInstance>(sConnectorInstances.size());
        for (final SConnectorInstance sConnectorInstance : sConnectorInstances) {
            connectorInstances.add(toConnectorInstance(sConnectorInstance));
        }
        return connectorInstances;
    }

    private static ConnectorInstance toConnectorInstance(final SConnectorInstance sConnectorInstance) {
        final ConnectorInstanceImpl connectorInstanceImpl = new ConnectorInstanceImpl(sConnectorInstance.getName(), sConnectorInstance.getContainerId(),
                sConnectorInstance.getContainerType(), sConnectorInstance.getConnectorId(), sConnectorInstance.getVersion(),
                ConnectorState.valueOf(sConnectorInstance.getState()), sConnectorInstance.getActivationEvent());
        connectorInstanceImpl.setId(sConnectorInstance.getId());
        return connectorInstanceImpl;
    }

    public static ConnectorInstanceWithFailureInfo toConnectorInstanceWithFailureInfo(final SConnectorInstanceWithFailureInfo sConnectorInstanceWithFailureInfo) {
        final ConnectorInstanceWithFailureInfoImpl connectorInstanceImpl = new ConnectorInstanceWithFailureInfoImpl(
                sConnectorInstanceWithFailureInfo.getName(), sConnectorInstanceWithFailureInfo.getContainerId(),
                sConnectorInstanceWithFailureInfo.getContainerType(), sConnectorInstanceWithFailureInfo.getConnectorId(),
                sConnectorInstanceWithFailureInfo.getVersion(), ConnectorState.valueOf(sConnectorInstanceWithFailureInfo.getState()),
                sConnectorInstanceWithFailureInfo.getActivationEvent(), sConnectorInstanceWithFailureInfo.getExceptionMessage(),
                sConnectorInstanceWithFailureInfo.getStackTrace());
        connectorInstanceImpl.setId(sConnectorInstanceWithFailureInfo.getId());
        return connectorInstanceImpl;
    }

    public static ArchivedConnectorInstance toArchivedConnectorInstance(final SAConnectorInstance sAConnectorInstance) {
        final ArchivedConnectorInstanceImpl connectorInstanceImpl = new ArchivedConnectorInstanceImpl(sAConnectorInstance.getName(), new Date(
                sAConnectorInstance.getArchiveDate()), sAConnectorInstance.getContainerId(), sAConnectorInstance.getContainerType(),
                sAConnectorInstance.getConnectorId(), sAConnectorInstance.getVersion(), sAConnectorInstance.getActivationEvent(),
                ConnectorState.valueOf(sAConnectorInstance.getState()), sAConnectorInstance.getSourceObjectId());
        connectorInstanceImpl.setId(sAConnectorInstance.getId());
        return connectorInstanceImpl;
    }

    public static List<ArchivedConnectorInstance> toArchivedConnectorInstances(final List<SAConnectorInstance> serverObjects) {
        final List<ArchivedConnectorInstance> commments = new ArrayList<ArchivedConnectorInstance>();
        for (final SAConnectorInstance saConnectorInstance : serverObjects) {
            final ArchivedConnectorInstance archivedConnectorInstance = toArchivedConnectorInstance(saConnectorInstance);
            commments.add(archivedConnectorInstance);
        }
        return commments;
    }

    public static List<Profile> toProfiles(final List<SProfile> sProfiles) {
        final List<Profile> profiles = new ArrayList<Profile>(sProfiles.size());
        for (final SProfile sProfile : sProfiles) {
            final Profile profile = toProfile(sProfile);
            profiles.add(profile);
        }
        return profiles;
    }

    public static Profile toProfile(final SProfile sProfile) {
        final ProfileImpl profileImpl = new ProfileImpl(sProfile.getName());
        profileImpl.setId(sProfile.getId());
        profileImpl.setDefault(sProfile.isDefault());
        profileImpl.setDescription(sProfile.getDescription());
        profileImpl.setCreationDate(new Date(sProfile.getCreationDate()));
        profileImpl.setCreatedBy(sProfile.getCreatedBy());
        profileImpl.setLastUpdateDate(new Date(sProfile.getLastUpdateDate()));
        profileImpl.setLastUpdatedBy(sProfile.getLastUpdatedBy());
        return profileImpl;
    }

    public static List<ProfileEntry> toProfileEntries(final List<SProfileEntry> sProfileEntries) {
        final List<ProfileEntry> profiles = new ArrayList<ProfileEntry>(sProfileEntries.size());
        for (final SProfileEntry sProfileEntry : sProfileEntries) {
            final ProfileEntry profile = toProfileEntry(sProfileEntry);
            profiles.add(profile);
        }
        return profiles;
    }

    public static ProfileEntry toProfileEntry(final SProfileEntry sProfileEntry) {
        final ProfileEntryImpl profileEntryImpl = new ProfileEntryImpl(sProfileEntry.getName(), sProfileEntry.getProfileId());
        profileEntryImpl.setId(sProfileEntry.getId());
        profileEntryImpl.setDescription(sProfileEntry.getDescription());
        profileEntryImpl.setIndex(sProfileEntry.getIndex());
        profileEntryImpl.setPage(sProfileEntry.getPage());
        profileEntryImpl.setParentId(sProfileEntry.getParentId());
        profileEntryImpl.setType(sProfileEntry.getType());
        profileEntryImpl.setCustom(sProfileEntry.isCustom());
        return profileEntryImpl;
    }

    public static List<ProfileMember> toProfileMembers(final List<SProfileMember> sProfileMembers) {
        final List<ProfileMember> profiles = new ArrayList<ProfileMember>(sProfileMembers.size());
        for (final SProfileMember sProfileMember : sProfileMembers) {
            final ProfileMember profile = toProfileMember(sProfileMember);
            profiles.add(profile);
        }
        return profiles;
    }

    public static ProfileMember toProfileMember(final SProfileMember sProfileMember) {
        final ProfileMemberImpl profileMemberImpl = new ProfileMemberImpl();
        profileMemberImpl.setId(sProfileMember.getId());
        profileMemberImpl.setDisplayNamePart1(sProfileMember.getDisplayNamePart1());
        profileMemberImpl.setDisplayNamePart2(sProfileMember.getDisplayNamePart2());
        profileMemberImpl.setDisplayNamePart3(sProfileMember.getDisplayNamePart3());
        profileMemberImpl.setGroupId(sProfileMember.getGroupId());
        profileMemberImpl.setProfileId(sProfileMember.getProfileId());
        profileMemberImpl.setRoleId(sProfileMember.getRoleId());
        profileMemberImpl.setUserId(sProfileMember.getUserId());
        return profileMemberImpl;
    }

    public static SProfileMember constructSProfileMember(final ProfileMemberCreator creator) {
        final Map<ProfileMemberField, Serializable> fields = creator.getFields();
        final SProfileMemberBuilder newSProfileMemberBuilder = BuilderFactory.get(SProfileMemberBuilderFactory.class).createNewInstance(
                (Long) fields.get(ProfileMemberField.PROFILE_ID));
        final Long groupeId = (Long) fields.get(ProfileMemberField.GROUP_ID);
        if (groupeId != null) {
            newSProfileMemberBuilder.setGroupId(groupeId);
        }
        final Long roleId = (Long) fields.get(ProfileMemberField.ROLE_ID);
        if (roleId != null) {
            newSProfileMemberBuilder.setRoleId(roleId);
        }
        final Long userId = (Long) fields.get(ProfileMemberField.USER_ID);
        if (userId != null) {
            newSProfileMemberBuilder.setUserId(userId);
        }
        return newSProfileMemberBuilder.done();
    }

    public static List<FailedJob> toFailedJobs(final List<SFailedJob> sFailedJobs) {
        final List<FailedJob> failedJobs = new ArrayList<FailedJob>(sFailedJobs.size());
        for (final SFailedJob sFailedJob : sFailedJobs) {
            failedJobs.add(toFailedJob(sFailedJob));
        }
        return failedJobs;
    }

    public static FailedJob toFailedJob(final SFailedJob sFailedJob) {
        final FailedJobImpl failedJob = new FailedJobImpl(sFailedJob.getJobDescriptorId(), sFailedJob.getJobName());
        failedJob.setDescription(sFailedJob.getDescription());
        failedJob.setLastMessage(sFailedJob.getLastMessage());
        failedJob.setRetryNumber(sFailedJob.getRetryNumber());
        failedJob.setLastUpdateDate(new Date(sFailedJob.getLastUpdateDate()));
        return failedJob;
    }

    public static List<Theme> toThemes(final List<STheme> sThemes) {
        final List<Theme> themes = new ArrayList<Theme>(sThemes.size());
        for (final STheme sTheme : sThemes) {
            final Theme theme = toTheme(sTheme);
            themes.add(theme);
        }
        return themes;
    }

    public static Theme toTheme(final STheme sTheme) {
        final ThemeType type = ThemeType.valueOf(sTheme.getType().name());
        final Date lastUpdateDate = new Date(sTheme.getLastUpdateDate());
        return new ThemeImpl(sTheme.getContent(), sTheme.getCssContent(), sTheme.isDefault(), type, lastUpdateDate);
    }

    public static CustomUserInfoDefinitionImpl convert(final SCustomUserInfoDefinition sDefinition) {
        final CustomUserInfoDefinitionImpl definition = new CustomUserInfoDefinitionImpl();
        definition.setId(sDefinition.getId());
        definition.setName(sDefinition.getName());
        definition.setDescription(sDefinition.getDescription());
        return definition;
    }

    public static CustomUserInfoValueImpl convert(final SCustomUserInfoValue sValue) {
        if (sValue == null) {
            return null;
        }
        final CustomUserInfoValueImpl value = new CustomUserInfoValueImpl();
        value.setDefinitionId(sValue.getDefinitionId());
        value.setUserId(sValue.getUserId());
        value.setValue(sValue.getValue());
        return value;
    }

    public static FormMapping toFormMapping(final SFormMapping sFormMapping) {
        if (sFormMapping == null) {
            return null;
        }
        final FormMapping formMapping = new FormMapping();
        formMapping.setId(sFormMapping.getId());
        formMapping.setTask(sFormMapping.getTask());
        final SPageMapping pageMapping = sFormMapping.getPageMapping();
        if (pageMapping != null) {
            formMapping.setPageMappingKey(pageMapping.getKey());
            formMapping.setPageId(pageMapping.getPageId());
            formMapping.setPageURL(pageMapping.getUrl());
        }
        formMapping.setType(FormMappingType.getTypeFromId(sFormMapping.getType()));
        formMapping.setTarget(sFormMapping.getTarget() == null ? null : FormMappingTarget.valueOf(sFormMapping.getTarget()));
        formMapping.setProcessDefinitionId(sFormMapping.getProcessDefinitionId());
        final long lastUpdateDate = sFormMapping.getLastUpdateDate();
        formMapping.setLastUpdateDate(lastUpdateDate > 0 ? new Date(lastUpdateDate) : null);
        formMapping.setLastUpdatedBy(sFormMapping.getLastUpdatedBy());
        return formMapping;
    }

    public static List<FormMapping> toFormMappings(final List<SFormMapping> serverObjects) {
        final List<FormMapping> clientObjects = new ArrayList<>(serverObjects.size());
        for (final SFormMapping serverObject : serverObjects) {
            clientObjects.add(toFormMapping(serverObject));
        }
        return clientObjects;
    }

    public static BusinessDataReference toBusinessDataReference(final SRefBusinessDataInstance sRefBusinessDataInstance) {
        if (sRefBusinessDataInstance == null) {
            return null;
        }
        if (sRefBusinessDataInstance instanceof SMultiRefBusinessDataInstance) {
            final SMultiRefBusinessDataInstance multi = (SMultiRefBusinessDataInstance) sRefBusinessDataInstance;
            return new MultipleBusinessDataReferenceImpl(multi.getName(), multi.getDataClassName(), multi.getDataIds());
        }
        final SSimpleRefBusinessDataInstance simple = (SSimpleRefBusinessDataInstance) sRefBusinessDataInstance;
        return new SimpleBusinessDataReferenceImpl(simple.getName(), simple.getDataClassName(), simple.getDataId());

    }

    public static ContractDefinition toContract(final SContractDefinition sContract) {
        final ContractDefinitionImpl contract = new ContractDefinitionImpl();
        for (final SInputDefinition input : sContract.getInputDefinitions()) {
            contract.addInput(toInput(input));
        }
        for (final SConstraintDefinition sConstraintDefinition : sContract.getConstraints()) {
            final ConstraintDefinitionImpl constraint = new ConstraintDefinitionImpl(sConstraintDefinition.getName(), sConstraintDefinition.getExpression(),
                    sConstraintDefinition.getExplanation());
            for (final String inputName : sConstraintDefinition.getInputNames()) {
                constraint.addInputName(inputName);
            }
            contract.addConstraint(constraint);
        }
        return contract;
    }

    private static InputDefinition toInput(final SInputDefinition input) {
        final List<InputDefinition> inputDefinitions = new ArrayList<InputDefinition>();
        for (final SInputDefinition sInputDefinition : input.getInputDefinitions()) {
            inputDefinitions.add(toInput(sInputDefinition));
        }
        final SType type = input.getType();
        final InputDefinitionImpl inputDefinition = new InputDefinitionImpl(input.getName(), type == null ? null : Type.valueOf(type.toString()),
                input.getDescription(), input.isMultiple());
        inputDefinition.getInputs().addAll(inputDefinitions);
        return inputDefinition;

    }

    public static PageURL toPageURL(final SPageURL sPageURL) {
        return new PageURL(sPageURL.getUrl(), sPageURL.getPageId());
    }
}
