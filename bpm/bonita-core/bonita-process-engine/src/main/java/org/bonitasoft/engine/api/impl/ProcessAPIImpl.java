/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package org.bonitasoft.engine.api.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.bonitasoft.engine.actor.ActorMappingExportException;
import org.bonitasoft.engine.actor.ActorMappingImportException;
import org.bonitasoft.engine.actor.ActorMemberCreationException;
import org.bonitasoft.engine.actor.ActorMemberDeletionException;
import org.bonitasoft.engine.actor.ActorUpdateDescriptor;
import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.SActorNotFoundException;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.SActorMember;
import org.bonitasoft.engine.actor.mapping.model.SActorUpdateBuilder;
import org.bonitasoft.engine.actor.privilege.api.ActorPrivilegeService;
import org.bonitasoft.engine.actor.privilege.model.SActorPrivilege;
import org.bonitasoft.engine.api.ActorMemberSorting;
import org.bonitasoft.engine.api.ActorSorting;
import org.bonitasoft.engine.api.EventSorting;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProcessInstanceCriterion;
import org.bonitasoft.engine.api.impl.resolver.ActorProcessDependencyResolver;
import org.bonitasoft.engine.api.impl.resolver.ConnectorProcessDependencyResolver;
import org.bonitasoft.engine.api.impl.resolver.ProcessDependencyResolver;
import org.bonitasoft.engine.api.impl.resolver.UserFilterProcessDependencyResolver;
import org.bonitasoft.engine.api.impl.transaction.*;
import org.bonitasoft.engine.api.impl.transaction.document.AttachDocument;
import org.bonitasoft.engine.api.impl.transaction.document.AttachDocumentAndStoreContent;
import org.bonitasoft.engine.api.impl.transaction.document.AttachDocumentVersion;
import org.bonitasoft.engine.api.impl.transaction.document.AttachDocumentVersionAndStoreContent;
import org.bonitasoft.engine.api.impl.transaction.document.GetArchivedDocument;
import org.bonitasoft.engine.api.impl.transaction.document.GetDocument;
import org.bonitasoft.engine.api.impl.transaction.document.GetDocumentByName;
import org.bonitasoft.engine.api.impl.transaction.document.GetDocumentByNameAtActivityCompletion;
import org.bonitasoft.engine.api.impl.transaction.document.GetDocumentByNameAtProcessInstantiation;
import org.bonitasoft.engine.api.impl.transaction.document.GetDocumentContent;
import org.bonitasoft.engine.api.impl.transaction.document.GetDocumentsOfProcessInstance;
import org.bonitasoft.engine.api.impl.transaction.document.GetNumberOfDocumentsOfProcessInstance;
import org.bonitasoft.engine.api.impl.transaction.flownode.HideTasks;
import org.bonitasoft.engine.api.impl.transaction.flownode.IsTaskHidden;
import org.bonitasoft.engine.api.impl.transaction.flownode.SetExpectedEndDate;
import org.bonitasoft.engine.api.impl.transaction.flownode.UnhideTasks;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.model.ActivityInstance;
import org.bonitasoft.engine.bpm.model.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.model.ActivityStates;
import org.bonitasoft.engine.bpm.model.ActorInstance;
import org.bonitasoft.engine.bpm.model.ActorMember;
import org.bonitasoft.engine.bpm.model.Category;
import org.bonitasoft.engine.bpm.model.CategoryCriterion;
import org.bonitasoft.engine.bpm.model.Comment;
import org.bonitasoft.engine.bpm.model.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.model.FlowNodeInstance;
import org.bonitasoft.engine.bpm.model.FlowNodeType;
import org.bonitasoft.engine.bpm.model.HumanTaskInstance;
import org.bonitasoft.engine.bpm.model.Index;
import org.bonitasoft.engine.bpm.model.ManualTaskInstance;
import org.bonitasoft.engine.bpm.model.MemberType;
import org.bonitasoft.engine.bpm.model.ProcessDefinition;
import org.bonitasoft.engine.bpm.model.ProcessDefinitionCriterion;
import org.bonitasoft.engine.bpm.model.ProcessDefinitionStates;
import org.bonitasoft.engine.bpm.model.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.model.ProcessDeploymentInfoUpdateDescriptor;
import org.bonitasoft.engine.bpm.model.ProcessInstance;
import org.bonitasoft.engine.bpm.model.ProcessInstanceUpdateDescriptor;
import org.bonitasoft.engine.bpm.model.TaskPriority;
import org.bonitasoft.engine.bpm.model.archive.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.model.archive.ArchivedComment;
import org.bonitasoft.engine.bpm.model.archive.ArchivedDocument;
import org.bonitasoft.engine.bpm.model.archive.ArchivedFlowElementInstance;
import org.bonitasoft.engine.bpm.model.archive.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.model.archive.ArchivedHumanTaskInstance;
import org.bonitasoft.engine.bpm.model.archive.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.model.archive.impl.ArchivedTransitionInstanceImpl;
import org.bonitasoft.engine.bpm.model.breakpoint.Breakpoint;
import org.bonitasoft.engine.bpm.model.breakpoint.BreakpointCriterion;
import org.bonitasoft.engine.bpm.model.data.DataDefinition;
import org.bonitasoft.engine.bpm.model.data.DataInstance;
import org.bonitasoft.engine.bpm.model.document.Document;
import org.bonitasoft.engine.bpm.model.event.EventInstance;
import org.bonitasoft.engine.bpm.model.impl.ProcessDeploymentInfoImpl;
import org.bonitasoft.engine.bpm.model.privilege.ActorPrivilege;
import org.bonitasoft.engine.bpm.model.privilege.LevelRight;
import org.bonitasoft.engine.bpm.model.privilege.Privilege;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.connector.ConnectorCriterion;
import org.bonitasoft.engine.connector.ConnectorImplementationDescriptor;
import org.bonitasoft.engine.core.category.CategoryService;
import org.bonitasoft.engine.core.category.exception.SCategoryAlreadyExistsException;
import org.bonitasoft.engine.core.category.exception.SCategoryException;
import org.bonitasoft.engine.core.category.exception.SCategoryNotFoundException;
import org.bonitasoft.engine.core.category.model.SCategory;
import org.bonitasoft.engine.core.category.model.builder.SCategoryBuilder;
import org.bonitasoft.engine.core.category.model.builder.SCategoryBuilderAccessor;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.connector.parser.SConnectorImplementationDescriptor;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.LeftOperand;
import org.bonitasoft.engine.core.operation.Operation;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.operation.model.SOperatorType;
import org.bonitasoft.engine.core.operation.model.builder.SOperationBuilders;
import org.bonitasoft.engine.core.process.comment.api.SCommentNotFoundException;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.comment.model.SComment;
import org.bonitasoft.engine.core.process.comment.model.archive.SAComment;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SDeletingEnabledProcessException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SActorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SParameterDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.core.process.definition.model.builder.BPMDefinitionBuilders;
import org.bonitasoft.engine.core.process.definition.model.builder.SProcessDefinitionDeployInfoUpdateBuilder;
import org.bonitasoft.engine.core.process.definition.model.builder.ServerModelConvertor;
import org.bonitasoft.engine.core.process.definition.model.builder.event.trigger.SThrowMessageEventTriggerDefinitionBuilder;
import org.bonitasoft.engine.core.process.definition.model.builder.event.trigger.SThrowSignalEventTriggerDefinitionBuilder;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SThrowMessageEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SThrowSignalEventTriggerDefinition;
import org.bonitasoft.engine.core.process.document.api.ProcessDocumentService;
import org.bonitasoft.engine.core.process.document.model.SAProcessDocument;
import org.bonitasoft.engine.core.process.document.model.SProcessDocument;
import org.bonitasoft.engine.core.process.document.model.builder.SProcessDocumentBuilder;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.BreakpointService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityInterruptedException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SBreakpointNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.api.states.ProcessInstanceState;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SAutomaticTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SManualTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.SUserTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAAutomaticTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAManualTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAUserTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAProcessInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAUserTaskInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.breakpoint.SBreakpoint;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.builder.SBreakpointBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceUpdateBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SEndEventInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.event.SEventInstance;
import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilder;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilders;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.builder.DependencyBuilderAccessor;
import org.bonitasoft.engine.document.SDocumentNotFoundException;
import org.bonitasoft.engine.exception.ActivityCreationException;
import org.bonitasoft.engine.exception.ActivityExecutionErrorException;
import org.bonitasoft.engine.exception.ActivityExecutionFailedException;
import org.bonitasoft.engine.exception.ActivityInstanceModificationException;
import org.bonitasoft.engine.exception.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.exception.ActivityInstanceReadException;
import org.bonitasoft.engine.exception.ActivityInterruptedException;
import org.bonitasoft.engine.exception.ActivityNotFoundException;
import org.bonitasoft.engine.exception.ActorMemberNotFoundException;
import org.bonitasoft.engine.exception.ActorNotFoundException;
import org.bonitasoft.engine.exception.ActorPrivilegeInsertException;
import org.bonitasoft.engine.exception.ActorPrivilegeNotFoundException;
import org.bonitasoft.engine.exception.ActorPrivilegeRemoveException;
import org.bonitasoft.engine.exception.ActorPrivilegeUpdateException;
import org.bonitasoft.engine.exception.ActorUpdateException;
import org.bonitasoft.engine.exception.ArchivedActivityInstanceNotFoundException;
import org.bonitasoft.engine.exception.ArchivedDocumentNotFoundException;
import org.bonitasoft.engine.exception.ArchivedProcessInstanceNotFoundException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.BonitaReadException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.BreakpointCreationException;
import org.bonitasoft.engine.exception.BreakpointDeletionException;
import org.bonitasoft.engine.exception.BreakpointNotFoundException;
import org.bonitasoft.engine.exception.CategoryAlreadyExistException;
import org.bonitasoft.engine.exception.CategoryCreationException;
import org.bonitasoft.engine.exception.CategoryDeletionException;
import org.bonitasoft.engine.exception.CategoryGettingException;
import org.bonitasoft.engine.exception.CategoryMappingException;
import org.bonitasoft.engine.exception.CategoryNotFoundException;
import org.bonitasoft.engine.exception.CategoryUpdateException;
import org.bonitasoft.engine.exception.ClassLoaderException;
import org.bonitasoft.engine.exception.CommentAddException;
import org.bonitasoft.engine.exception.CommentReadException;
import org.bonitasoft.engine.exception.ConnectorException;
import org.bonitasoft.engine.exception.ConnectorNotFoundException;
import org.bonitasoft.engine.exception.DataNotFoundException;
import org.bonitasoft.engine.exception.DataUpdateException;
import org.bonitasoft.engine.exception.DeletingEnabledProcessException;
import org.bonitasoft.engine.exception.DocumentException;
import org.bonitasoft.engine.exception.DocumentNotFoundException;
import org.bonitasoft.engine.exception.EventInstanceReadException;
import org.bonitasoft.engine.exception.ExpressionEvaluationException;
import org.bonitasoft.engine.exception.GroupNotFoundException;
import org.bonitasoft.engine.exception.InvalidEvaluationConnectorCondition;
import org.bonitasoft.engine.exception.InvalidProcessDefinitionException;
import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.NoSuchActivityDefinitionException;
import org.bonitasoft.engine.exception.ObjectAlreadyExistsException;
import org.bonitasoft.engine.exception.ObjectCreationException;
import org.bonitasoft.engine.exception.ObjectDeletionException;
import org.bonitasoft.engine.exception.ObjectNotFoundException;
import org.bonitasoft.engine.exception.ObjectReadException;
import org.bonitasoft.engine.exception.OperationExecutionException;
import org.bonitasoft.engine.exception.PageOutOfRangeException;
import org.bonitasoft.engine.exception.PrivilegeInsertException;
import org.bonitasoft.engine.exception.PrivilegeNotFoundException;
import org.bonitasoft.engine.exception.PrivilegeRemoveException;
import org.bonitasoft.engine.exception.ProcessDefinitionNotEnabledException;
import org.bonitasoft.engine.exception.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.exception.ProcessDefinitionReadException;
import org.bonitasoft.engine.exception.ProcessDeletionException;
import org.bonitasoft.engine.exception.ProcessDeployException;
import org.bonitasoft.engine.exception.ProcessDeploymentInfoUpdateException;
import org.bonitasoft.engine.exception.ProcessDisablementException;
import org.bonitasoft.engine.exception.ProcessEnablementException;
import org.bonitasoft.engine.exception.ProcessInstanceCreationException;
import org.bonitasoft.engine.exception.ProcessInstanceDeletionException;
import org.bonitasoft.engine.exception.ProcessInstanceModificationException;
import org.bonitasoft.engine.exception.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.exception.ProcessInstanceReadException;
import org.bonitasoft.engine.exception.ProcessResourceException;
import org.bonitasoft.engine.exception.RetryTaskException;
import org.bonitasoft.engine.exception.RoleNotFoundException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.SendEventException;
import org.bonitasoft.engine.exception.TaskReleaseException;
import org.bonitasoft.engine.exception.UnreleasableTaskException;
import org.bonitasoft.engine.exception.UserNotFoundException;
import org.bonitasoft.engine.exception.UserTaskNotFoundException;
import org.bonitasoft.engine.exception.UserTaskSetPriorityException;
import org.bonitasoft.engine.exception.document.DocumentAttachmentException;
import org.bonitasoft.engine.exception.flownode.TaskHidingException;
import org.bonitasoft.engine.execution.FlowNodeExecutor;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.execution.SUnreleasableTaskException;
import org.bonitasoft.engine.execution.TransactionalProcessInstanceInterruptor;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.execution.state.ProcessInstanceStateManager;
import org.bonitasoft.engine.execution.transaction.AddActivityInstanceTokenCount;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilders;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SGroupNotFoundException;
import org.bonitasoft.engine.identity.SRoleNotFoundException;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.builder.IdentityModelBuilder;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.parameter.OrderBy;
import org.bonitasoft.engine.parameter.ParameterService;
import org.bonitasoft.engine.parameter.SOutOfBoundException;
import org.bonitasoft.engine.parameter.SParameter;
import org.bonitasoft.engine.parameter.SParameterProcessNotFoundException;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.privilege.api.PrivilegeService;
import org.bonitasoft.engine.privilege.model.SPrivilege;
import org.bonitasoft.engine.privilege.model.buidler.PrivilegeBuilder;
import org.bonitasoft.engine.process.supervisor.ProcessSupervisor;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.model.SProfileMember;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.search.ActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.search.ArchivedActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.search.SearchActivityInstances;
import org.bonitasoft.engine.search.SearchActorPrivileges;
import org.bonitasoft.engine.search.SearchArchivedActivityInstances;
import org.bonitasoft.engine.search.SearchArchivedComments;
import org.bonitasoft.engine.search.SearchArchivedDocuments;
import org.bonitasoft.engine.search.SearchArchivedDocumentsSupervisedBy;
import org.bonitasoft.engine.search.SearchArchivedProcessInstances;
import org.bonitasoft.engine.search.SearchArchivedProcessInstancesInvolvingUser;
import org.bonitasoft.engine.search.SearchArchivedProcessInstancesSupervisedBy;
import org.bonitasoft.engine.search.SearchArchivedTasks;
import org.bonitasoft.engine.search.SearchArchivedTasksManagedBy;
import org.bonitasoft.engine.search.SearchAssignedTaskManagedBy;
import org.bonitasoft.engine.search.SearchComments;
import org.bonitasoft.engine.search.SearchCommentsInvolvingUser;
import org.bonitasoft.engine.search.SearchCommentsManagedBy;
import org.bonitasoft.engine.search.SearchDocuments;
import org.bonitasoft.engine.search.SearchDocumentsSupervisedBy;
import org.bonitasoft.engine.search.SearchEntitiesDescriptor;
import org.bonitasoft.engine.search.SearchEntityDescriptor;
import org.bonitasoft.engine.search.SearchHumanTaskInstances;
import org.bonitasoft.engine.search.SearchOpenProcessInstancesInvolvingUser;
import org.bonitasoft.engine.search.SearchOpenProcessInstancesSupervisedBy;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchPendingHiddenTasks;
import org.bonitasoft.engine.search.SearchPendingTasksManagedBy;
import org.bonitasoft.engine.search.SearchPendingTasksSupervisedBy;
import org.bonitasoft.engine.search.SearchPrivileges;
import org.bonitasoft.engine.search.SearchProceDefWithRecentProceInstancesStarted;
import org.bonitasoft.engine.search.SearchProcessDefinitions;
import org.bonitasoft.engine.search.SearchProcessDefinitionsDescriptor;
import org.bonitasoft.engine.search.SearchProcessDefinitionsUserCanStart;
import org.bonitasoft.engine.search.SearchProcessInstances;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.SearchUncategorizedProcessDefinitions;
import org.bonitasoft.engine.search.SearchUncategorizedProcessDefinitionsSupervisedBy;
import org.bonitasoft.engine.search.SearchUncategorizedProcessDefinitionsUserCanStart;
import org.bonitasoft.engine.search.flownode.SearchArchivedFlowNodeInstances;
import org.bonitasoft.engine.search.flownode.SearchFlowNodeInstances;
import org.bonitasoft.engine.search.humantask.SearchPendingTasksForUser;
import org.bonitasoft.engine.search.impl.SearchFilter;
import org.bonitasoft.engine.search.impl.SearchOptionsImpl;
import org.bonitasoft.engine.search.impl.SearchResultImpl;
import org.bonitasoft.engine.search.supervisor.SearchArchivedTasksSupervisedByTransaction;
import org.bonitasoft.engine.search.supervisor.SearchAssignedTasksSupervisedByTransaction;
import org.bonitasoft.engine.search.supervisor.SearchProcessDefinitionsSupervised;
import org.bonitasoft.engine.search.supervisor.SearchProcessSupervisorGroupDescriptor;
import org.bonitasoft.engine.search.supervisor.SearchProcessSupervisorRoleAndGroupDescriptor;
import org.bonitasoft.engine.search.supervisor.SearchProcessSupervisorRoleDescriptor;
import org.bonitasoft.engine.search.supervisor.SearchProcessSupervisorUserDescriptor;
import org.bonitasoft.engine.search.supervisor.SearchSupervisorsTransaction;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.supervisor.mapping.SSupervisorAlreadyExistsException;
import org.bonitasoft.engine.supervisor.mapping.SSupervisorCreationException;
import org.bonitasoft.engine.supervisor.mapping.SSupervisorDeletionException;
import org.bonitasoft.engine.supervisor.mapping.SSupervisorNotFoundException;
import org.bonitasoft.engine.supervisor.mapping.SupervisorMappingService;
import org.bonitasoft.engine.supervisor.mapping.model.SSupervisor;
import org.bonitasoft.engine.supervisor.mapping.model.SSupervisorBuilder;
import org.bonitasoft.engine.supervisor.mapping.model.SSupervisorBuilders;
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.util.FileUtil;
import org.bonitasoft.engine.util.IOUtil;
import org.bonitasoft.engine.xml.Parser;
import org.bonitasoft.engine.xml.XMLWriter;

import com.bonitasoft.engine.api.ParameterSorting;
import com.bonitasoft.engine.api.impl.resolver.ParameterProcessDependencyResolver;
import com.bonitasoft.engine.bpm.model.ParameterInstance;
import com.bonitasoft.engine.bpm.model.impl.ParameterImpl;
import com.bonitasoft.engine.exception.InvalidParameterValueException;
import com.bonitasoft.engine.exception.ParameterNotFoundException;

/**
 * @author Matthieu Chaffotte
 */
public class ProcessAPIImpl implements ProcessAPI {

    private static TenantServiceAccessor getTenantAccessor() throws InvalidSessionException {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new InvalidSessionException(e);
        }
    }

    @Override
    public SearchResult<HumanTaskInstance> searchHumanTaskInstances(final SearchOptions searchOptions) throws InvalidSessionException, SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();

        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final SearchHumanTaskInstances searchHumanTasksTransaction = new SearchHumanTaskInstances(activityInstanceService, flowNodeStateManager,
                searchEntitiesDescriptor.getHumanTaskInstanceDescriptor(), searchOptions);
        try {
            transactionExecutor.execute(searchHumanTasksTransaction);
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
        return searchHumanTasksTransaction.getResult();
    }

    @Override
    public SearchResult<ArchivedHumanTaskInstance> searchArchivedTasksManagedBy(final long managerUserId, final SearchOptions searchOptions)
            throws InvalidSessionException, UserNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ArchiveService archiveService = getTenantAccessor().getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SearchArchivedTasksManagedBy searchTransaction = new SearchArchivedTasksManagedBy(managerUserId, searchOptions, activityInstanceService,
                flowNodeStateManager, searchEntitiesDescriptor.getArchivedHumanTaskInstanceDescriptor(), persistenceService);
        try {
            transactionExecutor.execute(searchTransaction);
        } catch (final SBonitaException e) {
            throw new BonitaRuntimeException(e);
        }
        return searchTransaction.getResult();
    }

    @Override
    public void deleteProcess(final long processDefinitionUUID) throws InvalidSessionException, ProcessDefinitionNotFoundException, ProcessDeletionException,
            DeletingEnabledProcessException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final SProcessDefinition serverProcessDefinition = getServerProcessDefinition(transactionExecutor, processDefinitionUUID, processDefinitionService);
            final DeleteProcess deleteProcess = new DeleteProcess(processDefinitionService, serverProcessDefinition, processInstanceService,
                    tenantAccessor.getArchiveService(), tenantAccessor.getCommentService());
            transactionExecutor.execute(deleteProcess);
            final String processesFolder = BonitaHomeServer.getInstance().getProcessesFolder(tenantAccessor.getTenantId());
            final File file = new File(processesFolder);
            if (!file.exists()) {
                file.mkdir();
            }
            tenantAccessor.getParameterService().deleteAll(serverProcessDefinition.getId());
            final File processeFolder = new File(file, String.valueOf(serverProcessDefinition.getId()));
            FileUtil.deleteDir(processeFolder);

            // delete actorPrivileges
            final ActorPrivilegeService actorPrivilegeService = tenantAccessor.getActorPrivilegeService();
            final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
            final SearchOptions searchOptions = new SearchOptionsImpl(0, 10);
            final SearchActorPrivileges searchActorPrivileges = new SearchActorPrivileges(actorPrivilegeService,
                    searchEntitiesDescriptor.getActorPrivilegeDescriptor(), searchOptions);
            transactionExecutor.execute(searchActorPrivileges);
            final SearchResult<ActorPrivilege> actorPrisRes = searchActorPrivileges.getResult();
            if (actorPrisRes.getCount() > 0) {
                for (final ActorPrivilege actorPrivilege : actorPrisRes.getResult()) {
                    final RemoveActorPrivilegeById removeActorPrivilegeById = new RemoveActorPrivilegeById(actorPrivilege.getId(), actorPrivilegeService);
                    transactionExecutor.execute(removeActorPrivilegeById);
                }
            }

        } catch (final SProcessDefinitionNotFoundException e) {
            log(tenantAccessor, e);
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SDeletingEnabledProcessException e) {
            log(tenantAccessor, e);
            throw new DeletingEnabledProcessException(e);
        } catch (final SProcessDefinitionReadException e) {
            log(tenantAccessor, e);
            throw new ProcessDeletionException(e);
        } catch (final BonitaHomeNotSetException e) {
            log(tenantAccessor, e);
            throw new BonitaRuntimeException(e);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ProcessDeletionException(e);
        }
    }

    @Override
    public void deleteProcesses(final List<Long> processIdList) throws InvalidSessionException, ProcessDefinitionNotFoundException, ProcessDeletionException,
            DeletingEnabledProcessException {
        // FIXME batch method
        for (final Long processId : processIdList) {
            deleteProcess(processId);
        }
    }

    @Override
    public ProcessDefinition deploy(final BusinessArchive businessArchive) throws InvalidSessionException, ProcessDeployException,
            ProcessDefinitionNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final BPMDefinitionBuilders bpmDefinitionBuilders = tenantAccessor.getBPMDefinitionBuilders();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SExpressionBuilders sExpressionBuilders = tenantAccessor.getSExpressionBuilders();
        final SDataDefinitionBuilders sDataDefinitionBuilders = tenantAccessor.getSDataDefinitionBuilders();
        final SOperationBuilders sOperationBuilders = tenantAccessor.getSOperationBuilders();
        final DependencyService dependencyService = tenantAccessor.getDependencyService();
        final DependencyBuilderAccessor dependencyBuilderAccessor = tenantAccessor.getDependencyBuilderAccessor();
        final DesignProcessDefinition processDefinition = businessArchive.getProcessDefinition();
        // create the runtime process definition
        final SProcessDefinition sDefinition = createDefinition(tenantAccessor, processDefinitionService, bpmDefinitionBuilders, transactionExecutor,
                processDefinition, sExpressionBuilders, sDataDefinitionBuilders, sOperationBuilders);
        try {
            unzipBar(businessArchive, sDefinition, transactionExecutor, tenantAccessor.getTenantId());// TODO first unzip in temp folder
            final boolean isResolved = resolveDependencies(businessArchive, tenantAccessor, sDefinition);
            if (isResolved) {
                transactionExecutor.execute(new ResolveProcessAndCreateDependencies(processDefinitionService, sDefinition.getId(), dependencyService,
                        dependencyBuilderAccessor, businessArchive));
            }
        } catch (final BonitaHomeNotSetException e) {
            log(tenantAccessor, e);
            throw new ProcessDeployException(e.getMessage());
        } catch (final IOException e) {
            log(tenantAccessor, e);
            throw new ProcessDeployException(e.getMessage());
        } catch (final SBonitaException e) {
            throw new ProcessDeployException(e);
        }
        return ModelConvertor.toProcessDefinition(sDefinition);
    }

    private boolean resolveDependencies(final BusinessArchive businessArchive, final TenantServiceAccessor tenantAccessor, final SProcessDefinition sDefinition)
            throws InvalidSessionException, ProcessDeployException {
        final List<ProcessDependencyResolver> resolvers = Arrays.asList(new ActorProcessDependencyResolver(), new ParameterProcessDependencyResolver(),
                new ConnectorProcessDependencyResolver(), new UserFilterProcessDependencyResolver());
        ProcessDeployException pde = null;
        boolean resolved = true;
        for (final ProcessDependencyResolver resolver : resolvers) {
            try {
                resolved &= resolver.resolve(this, tenantAccessor, businessArchive, sDefinition);
            } catch (final BonitaException e) {
                if (pde == null) {
                    pde = new ProcessDeployException("Some dependencies are not resolved");
                    pde.setProcessDefinitionId(sDefinition.getId());
                }
                resolved = false;
                pde.addException(e);
            }
        }
        if (pde != null) {
            final TechnicalLoggerService technicalLoggerService = tenantAccessor.getTechnicalLoggerService();
            for (final BonitaException e : pde.getExceptions()) {
                technicalLoggerService.log(this.getClass(), TechnicalLogSeverity.ERROR, e);
            }
            throw pde;
        }
        return resolved;
    }

    @Override
    public void importActorMapping(final long pDefinitionId, final byte[] actorMappingXML) throws InvalidSessionException, ActorMappingImportException {
        if (actorMappingXML != null) {
            final String actorMapping = new String(actorMappingXML);
            importActorMapping(pDefinitionId, actorMapping);
        }
    }

    @Override
    public void importParameters(final long pDefinitionId, final byte[] parametersXML) throws InvalidSessionException, InvalidParameterValueException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        SProcessDefinition sDefinition = null;
        if (pDefinitionId > 0) {
            final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
            final GetProcessDefinition getProcessDefinition = new GetProcessDefinition(pDefinitionId, processDefinitionService);
            try {
                transactionExecutor.execute(getProcessDefinition);
            } catch (final SBonitaException e) {
                throw new InvalidParameterValueException(e);
            }
            sDefinition = getProcessDefinition.getResult();
        }

        final ParameterService parameterService = tenantAccessor.getParameterService();
        final Set<SParameterDefinition> parameters = sDefinition.getParameters();
        final Map<String, String> defaultParamterValues = new HashMap<String, String>();

        if (parametersXML != null) {
            final Properties property = new Properties();
            try {
                property.load(new ByteArrayInputStream(parametersXML));
            } catch (final IOException e1) {
                throw new InvalidParameterValueException(e1);
            }
            final Enumeration<String> names = (Enumeration<String>) property.propertyNames();
            while (names.hasMoreElements()) {
                final String name = names.nextElement();
                final String value = property.getProperty(name);
                defaultParamterValues.put(name, value);
            }
            /*
             * for(Entry entry : property.entrySet()){
             * defaultParamterValues.put((String)entry.getKey(), (String)entry.getValue());
             * }
             */
        }

        final Map<String, String> storedParameters = new HashMap<String, String>();
        for (final SParameterDefinition sParameterDefinition : parameters) {
            final String name = sParameterDefinition.getName();
            final String value = defaultParamterValues.get(name);
            if (value != null) {
                storedParameters.put(name, value);
            }
        }

        try {
            parameterService.addAll(sDefinition.getId(), storedParameters);
        } catch (final SParameterProcessNotFoundException e) {
            throw new InvalidParameterValueException(e);
        }
    }

    @Override
    // TODO delete files after use/if an exception occurs
    public byte[] exportBarProcessContentUnderHome(final long processDefinitionId) throws BonitaRuntimeException, IOException, InvalidSessionException {
        String processesFolder;
        try {
            final long tenantId = getTenantAccessor().getTenantId();
            processesFolder = BonitaHomeServer.getInstance().getProcessesFolder(tenantId);
        } catch (final BonitaHomeNotSetException e) {
            throw new BonitaRuntimeException(e);
        }
        final File file = new File(processesFolder);
        if (!file.exists()) {
            file.mkdirs();
        }
        final File processFolder = new File(file, String.valueOf(processDefinitionId));

        // copy current parameter to parameter file
        final File currentParasF = new File(processFolder.getPath(), "current-parameters.properties");
        if (currentParasF.exists()) {
            final File parasF = new File(processFolder.getPath(), "parameters.properties");
            if (!parasF.exists()) {
                parasF.createNewFile();
            }
            final String content = FileUtil.read(currentParasF);
            FileUtil.write(parasF, content);
        }

        // export actormapping
        final File actormappF = new File(processFolder.getPath(), "actorMapping.xml");
        if (!actormappF.exists()) {
            actormappF.createNewFile();
        }
        String xmlcontent = "";
        try {
            xmlcontent = exportActorMapping(processDefinitionId);
        } catch (final ActorMappingExportException e) {
            throw new BonitaRuntimeException(e);
        }
        FileUtil.write(actormappF, xmlcontent);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipOutputStream zos = new ZipOutputStream(baos);
        try {
            FileUtil.zipDir(processFolder.getPath(), zos, processFolder.getPath());
            return baos.toByteArray();
        } finally {
            zos.close();
        }
    }

    private void unzipBar(final BusinessArchive businessArchive, final SProcessDefinition sDefinition, final TransactionExecutor transactionExecutor,
            final long tenantId) throws BonitaHomeNotSetException, InvalidSessionException, ProcessDeployException, IOException {
        final String processesFolder = BonitaHomeServer.getInstance().getProcessesFolder(tenantId);
        final File file = new File(processesFolder);
        if (!file.exists()) {
            file.mkdirs();
        }
        final File processFolder = new File(file, String.valueOf(sDefinition.getId()));
        final TransactionContent transactionContent = new UnzipBusinessArchive(businessArchive, processFolder);
        try {
            transactionExecutor.execute(transactionContent);
        } catch (final SBonitaException e) {
            throw (IOException) e.getCause();
        }
    }

    private SProcessDefinition createDefinition(final TenantServiceAccessor tenantAccessor, final ProcessDefinitionService processDefinitionService,
            final BPMDefinitionBuilders bpmDefinitionBuilders, final TransactionExecutor transactionExecutor, final DesignProcessDefinition processDefinition,
            final SExpressionBuilders sExpressionBuilders, final SDataDefinitionBuilders sDataDefinitionBuilders, final SOperationBuilders sOperationBuilders)
            throws ProcessDeployException {
        final SProcessDefinition sDefinition = bpmDefinitionBuilders.getProcessDefinitionBuilder()
                .createNewInstance(processDefinition, sExpressionBuilders, sDataDefinitionBuilders, sOperationBuilders).done();
        try {
            final StoreProcess storeProcess = new StoreProcess(processDefinitionService, sDefinition, processDefinition.getDisplayName(),
                    processDefinition.getDisplayDescription());
            transactionExecutor.execute(storeProcess);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ProcessDeployException(e.getMessage());
        }
        return sDefinition;
    }

    @Override
    public void disableProcess(final long processId) throws InvalidSessionException, ProcessDefinitionNotFoundException, ProcessDisablementException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final EventInstanceService eventInstanceService = tenantAccessor.getEventInstanceService();
        final TransactionContent transactionContent = new DisableProcess(processDefinitionService, processId, eventInstanceService);
        try {
            transactionExecutor.execute(transactionContent);
        } catch (final SProcessDefinitionNotFoundException e) {
            log(tenantAccessor, e);
            throw new ProcessDefinitionNotFoundException(e.getMessage());
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ProcessDisablementException(e.getMessage());
        }
    }

    @Override
    public void enableProcess(final long processId) throws InvalidSessionException, ProcessDefinitionNotFoundException, ProcessEnablementException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final EventsHandler eventsHandler = tenantAccessor.getEventsHandler();
        try {
            final TransactionContent transactionContent = new EnableProcess(processDefinitionService, processId, eventsHandler);
            transactionExecutor.execute(transactionContent);
            getServerProcessDefinition(transactionExecutor, processId, processDefinitionService);
        } catch (final SProcessDefinitionNotFoundException e) {
            log(tenantAccessor, e);
            throw new ProcessDefinitionNotFoundException(e.getMessage());
        } catch (final SBonitaException sbe) {
            log(tenantAccessor, sbe);
            throw new ProcessEnablementException(sbe);
        } catch (final Exception e) {
            log(tenantAccessor, e);
            throw new ProcessEnablementException(e.getMessage());
        }
    }

    @Override
    public void executeActivity(final long flowNodeInstanceId) throws InvalidSessionException, ActivityInterruptedException, ActivityExecutionErrorException,
            ActivityInstanceNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessExecutor processExecutor = tenantAccessor.getProcessExecutor();
        try {
            processExecutor.executeActivity(flowNodeInstanceId, getUserIdFromSession());
        } catch (final SActivityExecutionException e) {
            throw new ActivityExecutionErrorException(e);
        } catch (final SActivityInterruptedException e) {
            log(tenantAccessor, e);
            throw new ActivityInterruptedException(e.getMessage());
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ActivityExecutionErrorException(e);
        }
    }

    /**
     * This method has issues:
     * It throw activity execution error when the activity is finishing (because it is deleted in the last state)
     */
    @Override
    public ActivityInstance executeActivityStepByStep(final long activityInstanceUUID) throws InvalidSessionException, ActivityExecutionFailedException,
            ActivityExecutionErrorException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final GetActivityInstance transactionContent = new GetActivityInstance(activityInstanceService, activityInstanceUUID);
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final FlowNodeExecutor flowNodeExecutor = tenantAccessor.getFlowNodeExecutor();
        try {
            flowNodeExecutor.stepForward(activityInstanceUUID, null, null, null);
            transactionExecutor.execute(transactionContent);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ActivityExecutionErrorException(e.getMessage());
        }
        return ModelConvertor.toActivityInstance(transactionContent.getResult(), flowNodeStateManager); // TODO
    }

    @Override
    public Set<ActivityInstance> getActivities(final long processInstanceId, final int pageIndex, final int numberPerPage) throws InvalidSessionException,
            ActivityInstanceReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final GetActivities getActivityInstances = new GetActivities(processInstanceId, pageIndex * numberPerPage, numberPerPage, activityInstanceService);
        try {
            transactionExecutor.execute(getActivityInstances);
            final List<SActivityInstance> result = getActivityInstances.getResult();
            final Set<ActivityInstance> activities = ModelConvertor.toActivityInstances(result, flowNodeStateManager);
            return activities;
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ActivityInstanceReadException(e.getMessage());
        }
    }

    @Override
    public List<ActivityInstance> getActivitiesOfProcess(final long userId, final long processId, final int pageIndex, final int numberPerPage,
            final ActivityInstanceCriterion pagingCriterion) throws InvalidSessionException, ProcessDefinitionNotFoundException, UserNotFoundException,
            PageOutOfRangeException {
        throw new ProcessDefinitionNotFoundException("NYI");
    }

    @Override
    public long getNumberOfProcesses() throws InvalidSessionException, ProcessDefinitionReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final TransactionContentWithResult<Long> transactionContentWithResult = new GetNumberOfProcesses(processDefinitionService);
        try {
            transactionExecutor.execute(transactionContentWithResult);
            return transactionContentWithResult.getResult();
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ProcessDefinitionReadException(e.getMessage());
        }
    }

    @Override
    public ProcessDefinition getProcessDefinition(final Long processDefinitionUUID) throws InvalidSessionException, ProcessDefinitionNotFoundException,
            ProcessDefinitionReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final SProcessDefinition sProcessDefinition = getServerProcessDefinition(transactionExecutor, processDefinitionUUID, processDefinitionService);
            return ModelConvertor.toProcessDefinition(sProcessDefinition);
        } catch (final SProcessDefinitionNotFoundException e) {
            log(tenantAccessor, e);
            throw new ProcessDefinitionNotFoundException(e.getMessage());
        } catch (final SProcessDefinitionReadException e) {
            log(tenantAccessor, e);
            throw new ProcessDefinitionReadException(e.getMessage());
        }
    }

    @Override
    public ProcessDeploymentInfo getProcessDeploymentInfo(final long processDefinitionUUID) throws InvalidSessionException, ProcessDefinitionNotFoundException,
            ProcessDefinitionReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final TransactionContentWithResult<SProcessDefinitionDeployInfo> transactionContentWithResult = new GetProcessDeploymentInfo(processDefinitionUUID,
                    processDefinitionService);
            transactionExecutor.execute(transactionContentWithResult);
            return ModelConvertor.toProcessDeploymentInfo(transactionContentWithResult.getResult());
        } catch (final SProcessDefinitionNotFoundException e) {
            log(tenantAccessor, e);
            throw new ProcessDefinitionNotFoundException(e.getMessage());
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ProcessDefinitionReadException(e.getMessage());
        }
    }

    @Override
    public List<ProcessDeploymentInfo> getProcesses(final int pageIndex, final int numberPerPage, final ProcessDefinitionCriterion pagingCriterion)
            throws InvalidSessionException, PageOutOfRangeException, ProcessDefinitionReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final GetNumberOfProcesses getNumberOfProcesses = new GetNumberOfProcesses(processDefinitionService);
        try {
            transactionExecutor.execute(getNumberOfProcesses);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ProcessDefinitionReadException(e.getMessage());
        }
        final long totalNumber = getNumberOfProcesses.getResult();
        PageIndexCheckingUtil.checkIfPageIsOutOfRange(totalNumber, pageIndex, numberPerPage);
        final GetProcessDeploymentInfos transactionContentWithResult = new GetProcessDeploymentInfos(pageIndex, processDefinitionService, pagingCriterion,
                numberPerPage);
        try {
            transactionExecutor.execute(transactionContentWithResult);
            final List<SProcessDefinitionDeployInfo> result = transactionContentWithResult.getResult();
            final List<ProcessDeploymentInfo> clientResult = ModelConvertor.toProcessDeploymentInfo(result);
            return clientResult;
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ProcessDefinitionReadException(e.getMessage());
        }
    }

    private void log(final TenantServiceAccessor tenantAccessor, final Exception e) {
        final TechnicalLoggerService logger = tenantAccessor.getTechnicalLoggerService();
        logger.log(this.getClass(), TechnicalLogSeverity.ERROR, e);
    }

    @Override
    public ProcessInstance getProcessInstance(final long processInstanceId) throws InvalidSessionException, ProcessInstanceNotFoundException,
            ProcessInstanceReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        SProcessInstance sProcessInstance;
        try {
            sProcessInstance = getSProcessInstance(processInstanceId, tenantAccessor);
        } catch (final SProcessInstanceNotFoundException spinfe) {
            throw new ProcessInstanceNotFoundException(spinfe);
        } catch (final SProcessInstanceReadException spire) {
            throw new ProcessInstanceReadException(spire);
        }
        final ProcessInstanceStateManager processInstanceStateManager = tenantAccessor.getProcessInstanceStateManager();
        return ModelConvertor.toProcessInstance(sProcessInstance, processInstanceStateManager);
    }

    private SProcessInstance getSProcessInstance(final long processInstanceId, final TenantServiceAccessor tenantAccessor) throws InvalidSessionException,
            SProcessInstanceNotFoundException, SProcessInstanceReadException {
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final GetProcessInstance getProcessInstance = new GetProcessInstance(processInstanceService, processInstanceId);
        try {
            transactionExecutor.execute(getProcessInstance);
            return getProcessInstance.getResult();
        } catch (final SProcessInstanceNotFoundException spine) {
            throw spine;
        } catch (final SBonitaException sbe) {
            log(tenantAccessor, sbe);
            throw new SProcessInstanceReadException(sbe);
        }
    }

    @Override
    public List<ArchivedProcessInstance> getArchivedProcessInstanceList(final long processInstanceId, final int pageIndex, final int numberPerPage)
            throws InvalidSessionException, ProcessInstanceNotFoundException, ProcessInstanceReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ProcessInstanceStateManager processInstanceStateManager = tenantAccessor.getProcessInstanceStateManager();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final GetArchivedProcessInstanceList getProcessInstanceList = new GetArchivedProcessInstanceList(processInstanceService, processInstanceId,
                persistenceService, pageIndex, numberPerPage);
        try {
            transactionExecutor.execute(getProcessInstanceList);
        } catch (final SProcessInstanceNotFoundException e) {
            log(tenantAccessor, e);
            throw new ProcessInstanceNotFoundException(e.getMessage());
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ProcessInstanceReadException(e.getMessage());
        }
        return ModelConvertor.toArchivedProcessInstances(getProcessInstanceList.getResult(), processInstanceStateManager);
    }

    @Override
    public ArchivedProcessInstance getArchivedProcessInstance(final long id) throws InvalidSessionException, ProcessInstanceNotFoundException,
            ProcessInstanceReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ProcessInstanceStateManager processInstanceStateManager = tenantAccessor.getProcessInstanceStateManager();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        try {
            transactionExecutor.openTransaction();
            try {
                final SAProcessInstance archivedProcessInstance = processInstanceService.getArchivedProcessInstance(id, persistenceService);
                return ModelConvertor.toArchivedProcessInstance(archivedProcessInstance, processInstanceStateManager);
            } catch (final SProcessInstanceNotFoundException e) {
                throw new ProcessInstanceNotFoundException(e);
            } catch (final SBonitaException e) {
                throw new ProcessInstanceReadException(e);
            } finally {
                transactionExecutor.completeTransaction();
            }
        } catch (final STransactionException e1) {
            throw new ProcessInstanceReadException(e1);
        }
    }

    @Override
    public ArchivedProcessInstance getFinalArchivedProcessInstance(final long processInstanceId) throws InvalidSessionException,
            ProcessInstanceNotFoundException, ProcessInstanceReadException {
        final ArchiveService archiveService = getTenantAccessor().getArchiveService();
        return getArchivedProcessInstance(processInstanceId, archiveService.getDefinitiveArchiveReadPersistenceService());
    }

    /**
     * Read archive process instance from the provided persistence service.
     * 
     * @param persistenceService
     *            the persistence service from which to read
     */
    private ArchivedProcessInstance getArchivedProcessInstance(final long processInstanceId, final ReadPersistenceService persistenceService)
            throws InvalidSessionException, ProcessInstanceNotFoundException, ProcessInstanceReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ProcessInstanceStateManager processInstanceStateManager = tenantAccessor.getProcessInstanceStateManager();
        final SAProcessInstanceBuilder saProcessInstanceBuilder = tenantAccessor.getBPMInstanceBuilders().getSAProcessInstanceBuilder();
        final GetLatestArchivedProcessInstance getProcessInstance = new GetLatestArchivedProcessInstance(processInstanceService, processInstanceId,
                persistenceService, saProcessInstanceBuilder);
        try {
            transactionExecutor.execute(getProcessInstance);
        } catch (final SProcessInstanceNotFoundException e) {
            log(tenantAccessor, e);
            throw new ProcessInstanceNotFoundException(e.getMessage());
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ProcessInstanceReadException(e.getMessage());
        }
        return ModelConvertor.toArchivedProcessInstance(getProcessInstance.getResult(), processInstanceStateManager);
    }

    private SProcessDefinition getServerProcessDefinition(final TransactionExecutor transactionExecutor, final long processDefinitionUUID,
            final ProcessDefinitionService processDefinitionService) throws SProcessDefinitionNotFoundException, SProcessDefinitionReadException {
        final TransactionContentWithResult<SProcessDefinition> transactionContentWithResult = new GetProcessDefinition(processDefinitionUUID,
                processDefinitionService);
        try {
            transactionExecutor.execute(transactionContentWithResult);
            return transactionContentWithResult.getResult();
        } catch (final SProcessDefinitionNotFoundException e) {
            throw e;
        } catch (final SProcessDefinitionReadException e) {
            throw e;
        } catch (final SBonitaException e) {
            throw new SProcessDefinitionNotFoundException(e);
        }
    }

    @Override
    public ProcessInstance startProcess(final long processDefinitionId) throws InvalidSessionException, ProcessDefinitionNotFoundException,
            ProcessInstanceCreationException, ProcessDefinitionReadException, ProcessDefinitionNotEnabledException {
        final long userId = getUserIdFromSession();
        return startProcess(userId, processDefinitionId);
    }

    @Override
    public ProcessInstance startProcess(final long userId, final long processDefinitionId) throws InvalidSessionException, ProcessDefinitionNotFoundException,
            ProcessInstanceCreationException, ProcessDefinitionReadException, ProcessDefinitionNotEnabledException {
        try {
            return startProcess(userId, processDefinitionId, null);
        } catch (final OperationExecutionException e) {
            throw new ProcessDefinitionNotFoundException(e);
        }
    }

    @Override
    public int getNumberOfParameterInstances(final long processDefinitionUUID) throws InvalidSessionException, ProcessDefinitionNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final SProcessDefinition sProcessDefinition = getServerProcessDefinition(transactionExecutor, processDefinitionUUID, processDefinitionService);
            return sProcessDefinition.getParameters().size();
        } catch (final SProcessDefinitionReadException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        }
    }

    @Override
    public ParameterInstance getParameterInstance(final long processDefinitionId, final String parameterName) throws InvalidSessionException,
            ProcessDefinitionNotFoundException, ParameterNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ParameterService parameterService = tenantAccessor.getParameterService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final SProcessDefinition sProcessDefinition = getServerProcessDefinition(transactionExecutor, processDefinitionId, processDefinitionService);
            final SParameter parameter = parameterService.get(processDefinitionId, parameterName);
            final String name = parameter.getName();
            final String value = parameter.getValue();
            final SParameterDefinition parameterDefinition = sProcessDefinition.getParameter(name);
            final String description = parameterDefinition.getDescription();
            final String type = parameterDefinition.getType();
            final ParameterInstance paramterInstance = new ParameterImpl(name, description, value, type);
            return paramterInstance;
        } catch (final SParameterProcessNotFoundException e) {
            throw new ParameterNotFoundException(processDefinitionId, parameterName);
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SProcessDefinitionReadException e) {
            throw new ProcessDefinitionNotFoundException(e);
        }
    }

    @Override
    public List<ParameterInstance> getParameterInstances(final long processDefinitionId, final int pageIndex, final int numberPerPage,
            final ParameterSorting sort) throws InvalidSessionException, ProcessDefinitionNotFoundException, PageOutOfRangeException {
        final int totalNumber = getNumberOfParameterInstances(processDefinitionId);
        PageIndexCheckingUtil.checkIfPageIsOutOfRange(totalNumber, pageIndex, numberPerPage);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ParameterService parameterService = tenantAccessor.getParameterService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            OrderBy order = null;
            switch (sort) {
                case NAME_DESC:
                    order = OrderBy.NAME_DESC;
                    break;
                default:
                    order = OrderBy.NAME_ASC;
                    break;
            }

            final SProcessDefinition sProcessDefinition = getServerProcessDefinition(transactionExecutor, processDefinitionId, processDefinitionService);
            final List<SParameter> parameters = parameterService.get(processDefinitionId, pageIndex * numberPerPage, numberPerPage, order);
            final List<ParameterInstance> paramterInstances = new ArrayList<ParameterInstance>();
            for (int i = 0; i < parameters.size(); i++) {
                final SParameter parameter = parameters.get(i);
                final String name = parameter.getName();
                final String value = parameter.getValue();
                final SParameterDefinition parameterDefinition = sProcessDefinition.getParameter(name);
                final String description = parameterDefinition.getDescription();
                final String type = parameterDefinition.getType();
                paramterInstances.add(new ParameterImpl(name, description, value, type));
            }
            return paramterInstances;
        } catch (final SParameterProcessNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SProcessDefinitionReadException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SOutOfBoundException e) {
            throw new PageOutOfRangeException(e);
        }
    }

    @Override
    public void updateParameterInstanceValue(final long processDefinitionId, final String parameterName, final String parameterValue)
            throws InvalidSessionException, ProcessDefinitionNotFoundException, ParameterNotFoundException, InvalidParameterValueException {
        if (parameterValue == null) {
            throw new InvalidParameterValueException("The parameter value cannot be null");
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ParameterService parameterService = tenantAccessor.getParameterService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final SProcessDefinition sProcessDefinition = getServerProcessDefinition(transactionExecutor, processDefinitionId, processDefinitionService);
            final SParameterDefinition parameter = sProcessDefinition.getParameter(parameterName);
            if (parameter == null) {
                throw new ParameterNotFoundException(processDefinitionId, parameterName);
            }
            parameterService.update(processDefinitionId, parameterName, parameterValue);
            resolvedDependencies(sProcessDefinition, tenantAccessor);
        } catch (final SParameterProcessNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SProcessDefinitionReadException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SBonitaException e) {
            throw new ProcessDefinitionNotFoundException(e);
        }
    }

    @Override
    public int getNumberOfActors(final long processDefinitionId) throws InvalidSessionException, ProcessDefinitionNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final GetNumberOfActors getNumberofActors = new GetNumberOfActors(processDefinitionService, processDefinitionId);
        try {
            transactionExecutor.execute(getNumberofActors);
        } catch (final SBonitaException e) {
            throw new ProcessDefinitionNotFoundException(e);
        }
        return getNumberofActors.getResult();
    }

    @Override
    public List<ActorInstance> getActors(final long processDefinitionId, final int pageIndex, final int numberPerPage, final ActorSorting sort)
            throws InvalidSessionException, ProcessDefinitionNotFoundException, PageOutOfRangeException {
        final long totalNumber = getNumberOfActors(processDefinitionId);
        PageIndexCheckingUtil.checkIfPageIsOutOfRange(totalNumber, pageIndex, numberPerPage);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        try {
            final GetActorsByPagination getActorsByPaging = new GetActorsByPagination(actorMappingService, processDefinitionId, pageIndex, numberPerPage, sort);
            transactionExecutor.execute(getActorsByPaging);
            return ModelConvertor.toActors(getActorsByPaging.getResult());
        } catch (final SBonitaException e) {
            throw new ProcessDefinitionNotFoundException(e);
        }
    }

    @Override
    public List<ActorMember> getActorMembers(final long actorId, final int pageIndex, final int numberPerPage, final ActorMemberSorting sort)
            throws InvalidSessionException, ActorNotFoundException, PageOutOfRangeException {
        final long totalNumber = getNumberOfActorMembers(actorId);
        PageIndexCheckingUtil.checkIfPageIsOutOfRange(totalNumber, pageIndex, numberPerPage);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final GetActorMembers getActorMembers = new GetActorMembers(actorMappingService, actorId, pageIndex, numberPerPage, sort);
            transactionExecutor.execute(getActorMembers);
            return ModelConvertor.toActorMembers(getActorMembers.getResult());
        } catch (final SActorNotFoundException sanfe) {
            throw new ActorNotFoundException(sanfe);
        } catch (final SBonitaException e) {
            throw new ActorNotFoundException(e);
        }
    }

    @Override
    public long getNumberOfActorMembers(final long actorId) throws InvalidSessionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final GetNumberOfActorMembers numberOfActorMembers = new GetNumberOfActorMembers(actorMappingService, actorId);
        try {
            transactionExecutor.execute(numberOfActorMembers);
            return numberOfActorMembers.getResult();
        } catch (final SBonitaException sbe) {
            throw new InvalidSessionException(sbe);
        }
    }

    @Override
    public ActorInstance updateActor(final long actorId, final ActorUpdateDescriptor descriptor) throws InvalidSessionException, ActorNotFoundException,
            ActorUpdateException {
        if (descriptor == null || descriptor.getFields().isEmpty()) {
            throw new ActorUpdateException("The update descriptor does not contain field updates");
        }
        final SActor actor = getSActor(actorId);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final SActorUpdateBuilder actorUpdateBuilder = tenantAccessor.getSActorBuilders().getSActorUpdateBuilder();
        final UpdateActor updateActor = new UpdateActor(actorMappingService, actorUpdateBuilder, actor, descriptor);
        try {
            transactionExecutor.execute(updateActor);
        } catch (final SBonitaException sbe) {
            throw new ActorUpdateException(sbe);
        }
        return getActor(actorId);
    }

    @Override
    public ActorMember addUserToActor(final long actorId, final long userId) throws InvalidSessionException, ActorNotFoundException, UserNotFoundException,
            ActorMemberCreationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final AddActorMember addActorMember = new AddActorMember(actorMappingService, actorId, userId, -1, -1, MemberType.USER);
        try {
            transactionExecutor.execute(addActorMember);
            final SActorMember actorMember = addActorMember.getActorMember();
            final SActor sActor = getSActor(actorId);
            final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
            final GetProcessDefinition getProcessDefinition = new GetProcessDefinition(sActor.getScopeId(), processDefinitionService);
            transactionExecutor.execute(getProcessDefinition);
            final SProcessDefinition processDefinition = getProcessDefinition.getResult();
            resolvedDependencies(processDefinition, tenantAccessor);
            return ModelConvertor.toActorMember(actorMember);
        } catch (final SBonitaException sbe) {
            throw new ActorMemberCreationException(sbe);
        }
    }

    @Override
    public ActorMember addGroupToActor(final long actorId, final long groupId) throws InvalidSessionException, ActorNotFoundException, GroupNotFoundException,
            ActorMemberCreationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final AddActorMember addActorMember = new AddActorMember(actorMappingService, actorId, -1, groupId, -1, MemberType.GROUP);
        try {
            transactionExecutor.execute(addActorMember);
            final SActorMember actorMember = addActorMember.getActorMember();
            final SActor sActor = getSActor(actorId);
            final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
            final GetProcessDefinition getProcessDefinition = new GetProcessDefinition(sActor.getScopeId(), processDefinitionService);
            transactionExecutor.execute(getProcessDefinition);
            final SProcessDefinition processDefinition = getProcessDefinition.getResult();
            resolvedDependencies(processDefinition, tenantAccessor);
            return ModelConvertor.toActorMember(actorMember);
        } catch (final SBonitaException sbe) {
            throw new ActorMemberCreationException(sbe);
        }
    }

    @Override
    public ActorMember addRoleToActor(final long actorId, final long roleId) throws InvalidSessionException, ActorNotFoundException, RoleNotFoundException,
            ActorMemberCreationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final AddActorMember addActorMember = new AddActorMember(actorMappingService, actorId, -1, -1, roleId, MemberType.ROLE);
        try {
            transactionExecutor.execute(addActorMember);
            final SActorMember actorMember = addActorMember.getActorMember();
            final SActor sActor = getSActor(actorId);
            final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
            final GetProcessDefinition getProcessDefinition = new GetProcessDefinition(sActor.getScopeId(), processDefinitionService);
            transactionExecutor.execute(getProcessDefinition);
            final SProcessDefinition processDefinition = getProcessDefinition.getResult();
            resolvedDependencies(processDefinition, tenantAccessor);
            return ModelConvertor.toActorMember(actorMember);
        } catch (final SBonitaException sbe) {
            throw new ActorMemberCreationException(sbe);
        }
    }

    @Override
    public ActorMember addRoleAndGroupToActor(final long actorId, final long roleId, final long groupId) throws InvalidSessionException,
            ActorNotFoundException, RoleNotFoundException, GroupNotFoundException, ActorMemberCreationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final AddActorMember addActorMember = new AddActorMember(actorMappingService, actorId, -1, groupId, roleId, MemberType.MEMBERSHIP);
        try {
            transactionExecutor.execute(addActorMember);
            final SActorMember actorMember = addActorMember.getActorMember();
            final SActor sActor = getSActor(actorId);
            final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
            final GetProcessDefinition getProcessDefinition = new GetProcessDefinition(sActor.getScopeId(), processDefinitionService);
            transactionExecutor.execute(getProcessDefinition);
            final SProcessDefinition processDefinition = getProcessDefinition.getResult();
            resolvedDependencies(processDefinition, tenantAccessor);
            return ModelConvertor.toActorMember(actorMember);
        } catch (final SBonitaException sbe) {
            throw new ActorMemberCreationException(sbe);
        }
    }

    @Override
    public void removeActorMember(final long actorMemberId) throws InvalidSessionException, ActorMemberNotFoundException, ActorMemberDeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final RemoveActorMember removeActorMember = new RemoveActorMember(actorMappingService, actorMemberId);
        // FIXME remove an actor member when process is running!
        try {
            transactionExecutor.execute(removeActorMember);
        } catch (final SBonitaException sbe) {
            throw new ActorMemberDeletionException(sbe);
        }
    }

    @Override
    public ActorInstance getActor(final long actorId) throws InvalidSessionException, ActorNotFoundException {
        final SActor actor = getSActor(actorId);
        return ModelConvertor.toActorInstance(actor);
    }

    private SActor getSActor(final long actorId) throws InvalidSessionException, ActorNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        try {
            final GetActor getActor = new GetActor(actorMappingService, actorId);
            transactionExecutor.execute(getActor);
            return getActor.getResult();
        } catch (final SBonitaException e) {
            throw new ActorNotFoundException(e);
        }
    }

    private void resolvedDependencies(final SProcessDefinition definition, final TenantServiceAccessor tenantAccessor) throws SBonitaException {
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ParameterService parameterService = tenantAccessor.getParameterService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final DependencyService dependencyService = tenantAccessor.getDependencyService();
        final DependencyBuilderAccessor dependencyBuilderAccessor = tenantAccessor.getDependencyBuilderAccessor();
        final GetProcessDeploymentInfo getProcessDeploymentInfo = new GetProcessDeploymentInfo(definition.getId(), processDefinitionService);
        transactionExecutor.execute(getProcessDeploymentInfo);
        final SProcessDefinitionDeployInfo processDefinitionDeployInfo = getProcessDeploymentInfo.getResult();
        final boolean containsNullParameterValues = parameterService.containsNullValues(definition.getId());
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final CheckActorMapping checkActorMapping = new CheckActorMapping(actorMappingService, definition.getId());
        transactionExecutor.execute(checkActorMapping);
        final Boolean actorMappingResolved = checkActorMapping.getResult();
        if (!containsNullParameterValues && actorMappingResolved && ProcessDefinitionStates.UNRESOLVED.equals(processDefinitionDeployInfo.getState())) {
            try {
                transactionExecutor.execute(new ResolveProcessAndCreateDependencies(processDefinitionService, definition.getId(), dependencyService,
                        dependencyBuilderAccessor, tenantAccessor.getTenantId()));
            } catch (final BonitaHomeNotSetException e) {
                throw new BonitaRuntimeException(e);
            }
        }
    }

    @Override
    public ActivityInstance getActivityInstance(final long activityInstanceId) throws InvalidSessionException, ActivityInstanceNotFoundException,
            ActivityInstanceReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final GetActivityInstance getActivityInstance = new GetActivityInstance(activityInstanceService, activityInstanceId);
        try {
            transactionExecutor.execute(getActivityInstance);
        } catch (final SActivityInstanceNotFoundException e) {
            throw new ActivityInstanceNotFoundException(activityInstanceId);
        } catch (final SBonitaException e) {
            throw new ActivityInstanceReadException(e);
        }
        return ModelConvertor.toActivityInstance(getActivityInstance.getResult(), flowNodeStateManager);
    }

    @Override
    public List<HumanTaskInstance> getAssignedHumanTaskInstances(final long userId, final int pageIndex, final int numberPerPage,
            final ActivityInstanceCriterion pagingCriterion) throws InvalidSessionException, UserNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final SUserTaskInstanceBuilder modelBuilder = tenantAccessor.getBPMInstanceBuilders().getUserTaskInstanceBuilder();
        String field = null;
        OrderByType order = null;
        ActivityInstanceCriterion criterion = pagingCriterion;
        if (criterion == null) {
            criterion = ActivityInstanceCriterion.DEFAULT;
        }
        switch (criterion) {
            case NAME_ASC:
                field = modelBuilder.getNameKey();
                order = OrderByType.ASC;
                break;
            case NAME_DESC:
                field = modelBuilder.getNameKey();
                order = OrderByType.DESC;
                break;
            case REACHED_STATE_DATE_ASC:
                field = modelBuilder.getReachStateDateKey();
                order = OrderByType.ASC;
                break;
            case REACHED_STATE_DATE_DESC:
                field = modelBuilder.getReachStateDateKey();
                order = OrderByType.DESC;
                break;
            case LAST_UPDATE_ASC:
                field = modelBuilder.getLastUpdateDateKey();
                order = OrderByType.ASC;
                break;
            case LAST_UPDATE_DESC:
                field = modelBuilder.getLastUpdateDateKey();
                order = OrderByType.DESC;
                break;
            case PRIORITY_ASC:
                field = modelBuilder.getPriorityKey();
                order = OrderByType.ASC;
                break;
            case PRIORITY_DESC:
            case DEFAULT:
            default:
                field = modelBuilder.getPriorityKey();
                order = OrderByType.DESC;
                break;
        }
        try {
            final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
            final GetSUser getUser = new GetSUser(identityService, userId);
            transactionExecutor.execute(getUser);
            final SUser user = getUser.getResult();
            final ActivityInstanceService instanceService = tenantAccessor.getActivityInstanceService();
            final GetAssignedTasks getAssignedTasks = new GetAssignedTasks(instanceService, user.getId(), pageIndex, numberPerPage, field, order);
            transactionExecutor.execute(getAssignedTasks);
            final List<SHumanTaskInstance> assignedTasks = getAssignedTasks.getResult();
            final List<HumanTaskInstance> tasks = ModelConvertor.toHumanTaskInstances(assignedTasks, flowNodeStateManager);
            return tasks;
        } catch (final SUserNotFoundException e) {
            throw new UserNotFoundException(e);
        } catch (final SBonitaException e) {
            throw new UserNotFoundException(e);
        }
    }

    @Override
    public List<HumanTaskInstance> getPendingHumanTaskInstances(final long userId, final int pageIndex, final int numberPerPage,
            final ActivityInstanceCriterion pagingCriterion) throws InvalidSessionException, UserNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final SUserTaskInstanceBuilder modelBuilder = tenantAccessor.getBPMInstanceBuilders().getUserTaskInstanceBuilder();
        String field = null;
        OrderByType order = null;
        ActivityInstanceCriterion criterion = pagingCriterion;
        if (criterion == null) {
            criterion = ActivityInstanceCriterion.DEFAULT;
        }
        switch (criterion) {
            case NAME_ASC:
                field = modelBuilder.getNameKey();
                order = OrderByType.ASC;
                break;
            case NAME_DESC:
                field = modelBuilder.getNameKey();
                order = OrderByType.DESC;
                break;
            case REACHED_STATE_DATE_ASC:
                field = modelBuilder.getReachStateDateKey();
                order = OrderByType.ASC;
                break;
            case REACHED_STATE_DATE_DESC:
                field = modelBuilder.getReachStateDateKey();
                order = OrderByType.DESC;
                break;
            case LAST_UPDATE_ASC:
                field = modelBuilder.getLastUpdateDateKey();
                order = OrderByType.ASC;
                break;
            case LAST_UPDATE_DESC:
                field = modelBuilder.getLastUpdateDateKey();
                order = OrderByType.DESC;
                break;
            case PRIORITY_ASC:
                field = modelBuilder.getPriorityKey();
                order = OrderByType.ASC;
                break;
            case PRIORITY_DESC:
            case DEFAULT:
            default:
                field = modelBuilder.getPriorityKey();
                order = OrderByType.DESC;
                break;
        }
        try {
            final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
            final ProcessDefinitionService definitionService = tenantAccessor.getProcessDefinitionService();
            final Set<Long> actorIds = getActorsForUser(userId, actorMappingService, identityService, transactionExecutor, definitionService);
            final ActivityInstanceService instanceService = tenantAccessor.getActivityInstanceService();
            final GetPendingTasks getPendingTasks = new GetPendingTasks(instanceService, userId, actorIds, pageIndex, numberPerPage, field, order);
            transactionExecutor.execute(getPendingTasks);
            final List<SHumanTaskInstance> pendingTasks = getPendingTasks.getResult();
            return ModelConvertor.toHumanTaskInstances(pendingTasks, flowNodeStateManager);
        } catch (final SUserNotFoundException e) {
            throw new UserNotFoundException(e);
        } catch (final SBonitaException e) {
            throw new UserNotFoundException(e); // FIXME
        }
    }

    private Set<Long> getActorsForUser(final long userId, final ActorMappingService actorMappingService, final IdentityService identityService,
            final TransactionExecutor transactionExecutor, final ProcessDefinitionService definitionService) throws SBonitaException {
        final GetAllProcessDefinitionIdsInState getAllProcessDefinitionIdsInState = new GetAllProcessDefinitionIdsInState(definitionService,
                ProcessDefinitionStates.ENABLED);
        transactionExecutor.execute(getAllProcessDefinitionIdsInState);
        final Set<Long> processDefinitionIds = getAllProcessDefinitionIdsInState.getResult();
        if (processDefinitionIds.isEmpty()) {
            return Collections.emptySet();
        }
        final GetActors getActors = new GetActors(actorMappingService, processDefinitionIds, userId);
        transactionExecutor.execute(getActors);
        final List<SActor> actors = getActors.getResult();
        if (actors.isEmpty()) {
            return Collections.emptySet();
        }
        final Set<Long> actorIds = new HashSet<Long>();
        for (final SActor sActor : actors) {
            actorIds.add(sActor.getId());
        }
        return actorIds;
    }

    @Override
    public ArchivedActivityInstance getArchivedActivityInstance(final long activityInstanceId) throws InvalidSessionException,
            ActivityInstanceNotFoundException, ActivityInstanceReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final GetArchivedActivityInstance getActivityInstance = new GetArchivedActivityInstance(activityInstanceService, activityInstanceId, persistenceService);
        try {
            transactionExecutor.execute(getActivityInstance);
        } catch (final SActivityInstanceNotFoundException e) {
            log(tenantAccessor, e);
            throw new ActivityInstanceNotFoundException(activityInstanceId);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ActivityInstanceReadException(e.getMessage());
        }
        return ModelConvertor.toArchivedActivityInstance(getActivityInstance.getResult(), flowNodeStateManager);
    }

    @Override
    public ArchivedFlowNodeInstance getArchivedFlowNodeInstance(final long archivedFlowNodeInstanceId) throws InvalidSessionException,
            ActivityInstanceNotFoundException, ActivityInstanceReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();

        try {
            transactionExecutor.openTransaction();
            try {
                final SAFlowNodeInstance archivedFlowNodeInstance = activityInstanceService.getArchivedFlowNodeInstance(archivedFlowNodeInstanceId,
                        persistenceService);
                return ModelConvertor.toArchivedFlowNodeInstance(archivedFlowNodeInstance, flowNodeStateManager);
            } catch (final SFlowNodeNotFoundException e) {
                throw new ActivityInstanceNotFoundException(archivedFlowNodeInstanceId);
            } catch (final SFlowNodeReadException e) {
                throw new ActivityInstanceReadException(e);
            } finally {
                transactionExecutor.completeTransaction();
            }
        } catch (final STransactionException e1) {
            throw new ActivityInstanceReadException(e1);
        }
    }

    @Override
    public List<ProcessInstance> getProcessInstances(final int pageIndex, final int numberPerPage, final ProcessInstanceCriterion pagingCriterion)
            throws PageOutOfRangeException, InvalidSessionException {

        final long totalNumber = getNumberOfProcessInstances();

        // If there are no instances, return an empty list:
        if (totalNumber == 0) {
            return new ArrayList<ProcessInstance>();
        }

        PageIndexCheckingUtil.checkIfPageIsOutOfRange(totalNumber, pageIndex, numberPerPage);

        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SProcessInstanceBuilder modelBuilder = tenantAccessor.getBPMInstanceBuilders().getSProcessInstanceBuilder();
        final ProcessInstanceStateManager processInstanceStateManager = tenantAccessor.getProcessInstanceStateManager();
        String field = null;
        OrderByType order = null;
        switch (pagingCriterion) {
            case STATE_ASC:
                field = modelBuilder.getStateIdKey();
                order = OrderByType.ASC;
                break;
            case STATE_DESC:
                field = modelBuilder.getStateIdKey();
                order = OrderByType.DESC;
                break;
            case LAST_UPDATE_DESC:
                field = modelBuilder.getLastUpdateKey();
                order = OrderByType.DESC;
                break;
            case LAST_UPDATE_ASC:
                field = modelBuilder.getLastUpdateKey();
                order = OrderByType.ASC;
                break;
            case CREATION_DATE_ASC:
                field = modelBuilder.getStartDateKey();
                order = OrderByType.ASC;
                break;
            case CREATION_DATE_DESC:
            case DEFAULT:
                field = modelBuilder.getStartDateKey();
                order = OrderByType.DESC;
                break;
            case NAME_ASC:
                field = modelBuilder.getNameKey();
                order = OrderByType.ASC;
                break;
            case NAME_DESC:
                field = modelBuilder.getNameKey();
                order = OrderByType.DESC;
                break;
        }
        try {
            final String fieldContent = field;
            final OrderByType orderContent = order;
            final TransactionContentWithResult<List<SProcessInstance>> transactionContent = new GetProcessInstanceWithOrder(numberPerPage, orderContent,
                    processInstanceService, fieldContent, pageIndex);
            transactionExecutor.execute(transactionContent);
            return ModelConvertor.toProcessInstances(transactionContent.getResult(), processInstanceStateManager);
        } catch (final SBonitaException e) {
            throw new PageOutOfRangeException(e);
        }

    }

    @Override
    public long getNumberOfProcessInstances() throws InvalidSessionException {
        final ProcessInstanceService processInstanceService = getTenantAccessor().getProcessInstanceService();
        final TransactionExecutor transactionExecutor = getTenantAccessor().getTransactionExecutor();
        try {
            final TransactionContentWithResult<Long> transactionContent = new GetNumberOfProcessInstance(processInstanceService);
            transactionExecutor.execute(transactionContent);
            return transactionContent.getResult();
        } catch (final SBonitaException e) {
            throw new BonitaRuntimeException(e);// FIXME throw exceptions
        }
    }

    @Override
    public List<ArchivedProcessInstance> getArchivedProcessInstances(final int pageIndex, final int numberPerPage,
            final ProcessInstanceCriterion pagingCriterion) throws PageOutOfRangeException, InvalidSessionException, ProcessInstanceReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final long totalNumber = getNumberOfArchivedProcessInstances();

        return getArchivedProcessInstanceInPersistenceService(persistenceService, pageIndex, numberPerPage, pagingCriterion, totalNumber);
    }

    private List<ArchivedProcessInstance> getArchivedProcessInstanceInPersistenceService(final ReadPersistenceService persistenceService, final int pageIndex,
            final int numberPerPage, final ProcessInstanceCriterion pagingCriterion, final long totalNumber) throws PageOutOfRangeException,
            InvalidSessionException {
        // If there are no instances, return an empty list:
        if (totalNumber == 0) {
            return new ArrayList<ArchivedProcessInstance>(0);
        }

        PageIndexCheckingUtil.checkIfPageIsOutOfRange(totalNumber, pageIndex, numberPerPage);

        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessInstanceStateManager processInstanceStateManager = tenantAccessor.getProcessInstanceStateManager();
        final SAProcessInstanceBuilder modelBuilder = tenantAccessor.getBPMInstanceBuilders().getSAProcessInstanceBuilder();
        String field = null;
        OrderByType order = null;
        switch (pagingCriterion) {
            case STATE_ASC:
                field = modelBuilder.getStateIdKey();
                order = OrderByType.ASC;
                break;
            case STATE_DESC:
                field = modelBuilder.getStateIdKey();
                order = OrderByType.DESC;
                break;
            case ARCHIVE_DATE_ASC:
                field = modelBuilder.getArchiveDateKey();
                order = OrderByType.ASC;
                break;
            case ARCHIVE_DATE_DESC:
                field = modelBuilder.getArchiveDateKey();
                order = OrderByType.DESC;
                break;
            case LAST_UPDATE_DESC:
                field = modelBuilder.getLastUpdateKey();
                order = OrderByType.DESC;
                break;
            case LAST_UPDATE_ASC:
                field = modelBuilder.getLastUpdateKey();
                order = OrderByType.ASC;
                break;
            case CREATION_DATE_ASC:
                field = modelBuilder.getStartDateKey();
                order = OrderByType.ASC;
                break;
            case CREATION_DATE_DESC:
            case DEFAULT:
                field = modelBuilder.getStartDateKey();
                order = OrderByType.DESC;
                break;
        }
        try {
            final String fieldContent = field;
            final OrderByType orderContent = order;
            final TransactionContentWithResult<List<SAProcessInstance>> transactionContent = new GetArchivedProcessInstanceWithOrder(persistenceService,
                    numberPerPage, orderContent, processInstanceService, fieldContent, pageIndex);
            transactionExecutor.execute(transactionContent);
            return ModelConvertor.toArchivedProcessInstances(transactionContent.getResult(), processInstanceStateManager);
        } catch (final SBonitaException e) {
            throw new PageOutOfRangeException(e);
        }
    }

    @Override
    public long getNumberOfArchivedProcessInstances() throws InvalidSessionException, ProcessInstanceReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final TransactionContentWithResult<Long> transactionContent = new GetNumberOfArchivedProcessInstance(processInstanceService, persistenceService);
            transactionExecutor.execute(transactionContent);
            return transactionContent.getResult();
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ProcessInstanceReadException(e.getMessage());
        }
    }

    @Override
    public List<ActivityInstance> getOpenedActivityInstances(final long processInstanceId, final int pageIndex, final int numberPerPage,
            final ActivityInstanceCriterion pagingCriterion) throws InvalidSessionException, ProcessInstanceNotFoundException, PageOutOfRangeException,
            ActivityInstanceReadException {

        final int totalNumber = getNumberOfOpenedActivityInstances(processInstanceId);

        // If there are no instances, return an empty list:
        if (totalNumber == 0) {
            return new ArrayList<ActivityInstance>();
        }

        PageIndexCheckingUtil.checkIfPageIsOutOfRange(totalNumber, pageIndex, numberPerPage);

        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SAUserTaskInstanceBuilder modelBuilder = tenantAccessor.getBPMInstanceBuilders().getSAUserTaskInstanceBuilder();
        String field = null;
        OrderByType order = null;
        switch (pagingCriterion) {
            case NAME_ASC:
                field = modelBuilder.getNameKey();
                order = OrderByType.ASC;
                break;
            case NAME_DESC:
                field = modelBuilder.getNameKey();
                order = OrderByType.DESC;
                break;
            case REACHED_STATE_DATE_ASC:
                field = modelBuilder.getReachedStateDateKey();
                order = OrderByType.ASC;
                break;
            case REACHED_STATE_DATE_DESC:
                field = modelBuilder.getReachedStateDateKey();
                order = OrderByType.DESC;
                break;
            case LAST_UPDATE_ASC:
                field = modelBuilder.getLastUpdateKey();
                order = OrderByType.ASC;
                break;
            case LAST_UPDATE_DESC:
                field = modelBuilder.getLastUpdateKey();
                order = OrderByType.DESC;
                break;
            case PRIORITY_ASC:
                field = modelBuilder.getPriorityKey();
                order = OrderByType.ASC;
                break;
            case PRIORITY_DESC:
            case DEFAULT:
                field = modelBuilder.getPriorityKey();
                order = OrderByType.DESC;
                break;
        }
        try {
            final TransactionContentWithResult<List<SActivityInstance>> transactionContent = new GetActivityInstances(activityInstanceService,
                    processInstanceId, pageIndex, numberPerPage, field, order);
            transactionExecutor.execute(transactionContent);
            return ModelConvertor.toActivityInstanceList(transactionContent.getResult(), flowNodeStateManager);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ActivityInstanceReadException(e);
        }
    }

    @Override
    public List<ArchivedActivityInstance> getArchivedActivityInstances(final long processInstanceId, final int pageIndex, final int numberPerPage,
            final ActivityInstanceCriterion pagingCriterion) throws InvalidSessionException, PageOutOfRangeException, ActivityInstanceReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final int numberOfArchivedActivityInstances = getNumberOfArchivedActivityInstances(processInstanceId);
        return getArchivedActivityInstancesFromPersistence(processInstanceId, pageIndex, numberPerPage, pagingCriterion, tenantAccessor, persistenceService,
                numberOfArchivedActivityInstances);
    }

    private List<ArchivedActivityInstance> getArchivedActivityInstancesFromPersistence(final long processInstanceId, final int pageIndex,
            final int numberPerPage, final ActivityInstanceCriterion pagingCriterion, final TenantServiceAccessor tenantAccessor,
            final ReadPersistenceService persistenceService, final int numberOfArchivedActivityInstances) throws PageOutOfRangeException,
            InvalidSessionException, ActivityInstanceReadException {
        PageIndexCheckingUtil.checkIfPageIsOutOfRange(numberOfArchivedActivityInstances, pageIndex, numberPerPage);
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final SAUserTaskInstanceBuilder modelBuilder = getTenantAccessor().getBPMInstanceBuilders().getSAUserTaskInstanceBuilder();
        String field = null;
        OrderByType order = null;
        switch (pagingCriterion) {
            case DEFAULT:
                field = modelBuilder.getNameKey();
                order = OrderByType.DESC;
                break;
            case LAST_UPDATE_ASC:
                field = modelBuilder.getLastUpdateKey();
                order = OrderByType.ASC;
                break;
            case LAST_UPDATE_DESC:
                field = modelBuilder.getLastUpdateKey();
                order = OrderByType.DESC;
                break;
            case NAME_ASC:
                field = modelBuilder.getNameKey();
                order = OrderByType.ASC;
                break;
            case NAME_DESC:
                field = modelBuilder.getNameKey();
                order = OrderByType.DESC;
                break;
            case PRIORITY_ASC:
                field = modelBuilder.getPriorityKey();
                order = OrderByType.ASC;
                break;
            case PRIORITY_DESC:
                field = modelBuilder.getPriorityKey();
                order = OrderByType.DESC;
                break;
            case REACHED_STATE_DATE_ASC:
                field = modelBuilder.getReachedStateDateKey();
                order = OrderByType.ASC;
                break;
            case REACHED_STATE_DATE_DESC:
                field = modelBuilder.getReachedStateDateKey();
                order = OrderByType.DESC;
                break;
        }
        final GetArchivedActivityInstances getActivityInstances = new GetArchivedActivityInstances(activityInstanceService, processInstanceId,
                persistenceService, pageIndex, numberPerPage, field, order);
        try {
            transactionExecutor.execute(getActivityInstances);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ActivityInstanceReadException(e);
        }
        return ModelConvertor.toArchivedActivityInstances(getActivityInstances.getResult(), flowNodeStateManager);
    }

    @Override
    public int getNumberOfOpenedActivityInstances(final long processInstanceId) throws InvalidSessionException, ActivityInstanceReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final TransactionContentWithResult<Integer> transactionContentWithResult = new GetNumberOfActivityInstance("getNumberOfOpenActivityInstances",
                processInstanceId, activityInstanceService);
        try {
            transactionExecutor.execute(transactionContentWithResult);
            return transactionContentWithResult.getResult();
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ActivityInstanceReadException(e.getMessage());
        }
    }

    @Override
    public int getNumberOfArchivedActivityInstances(final long processInstanceId) throws InvalidSessionException {
        // TODO implement me!
        return 0;
    }

    @Override
    public Category createCategory(final String name, final String description) throws InvalidSessionException, CategoryAlreadyExistException,
            CategoryCreationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final CategoryService categoryService = tenantAccessor.getCategoryService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final CreateCategory createCategory = new CreateCategory(name, description, categoryService);
            transactionExecutor.execute(createCategory);
            return ModelConvertor.toCategory(createCategory.getResult());
        } catch (final SCategoryAlreadyExistsException scaee) {
            throw new CategoryAlreadyExistException(scaee);
        } catch (final SBonitaException e) {
            throw new CategoryCreationException("Category create exception!", e);
        }
    }

    @Override
    public Category getCategory(final long categoryId) throws InvalidSessionException, CategoryNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final CategoryService categoryService = tenantAccessor.getCategoryService();
        try {
            final GetCategory getCategory = new GetCategory(categoryService, categoryId);
            transactionExecutor.execute(getCategory);
            final SCategory sCategory = getCategory.getResult();
            return ModelConvertor.toCategory(sCategory);
        } catch (final SBonitaException sbe) {
            throw new CategoryNotFoundException(sbe);
        }
    }

    @Override
    public long getNumberOfCategories() throws InvalidSessionException {
        final TransactionExecutor transactionExecutor = getTenantAccessor().getTransactionExecutor();
        final CategoryService categoryService = getTenantAccessor().getCategoryService();
        try {
            final GetNumberOfCategories getNumberOfCategories = new GetNumberOfCategories(categoryService);
            transactionExecutor.execute(getNumberOfCategories);
            return getNumberOfCategories.getResult();
        } catch (final SBonitaException e) {
            return 0;
        }
    }

    @Override
    public List<Category> getCategories(final int pageIndex, final int numberPerPage, final CategoryCriterion pagingCriterion) throws InvalidSessionException,
            PageOutOfRangeException {
        final long totalNumber = this.getNumberOfCategories();
        PageIndexCheckingUtil.checkIfPageIsOutOfRange(totalNumber, pageIndex, numberPerPage);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final SCategoryBuilderAccessor modelBuilderAccessor = tenantAccessor.getCategoryModelBuilderAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();;
        final CategoryService categoryService = tenantAccessor.getCategoryService();
        String field = null;
        OrderByType order = null;
        switch (pagingCriterion) {
            case NAME_ASC:
                field = modelBuilderAccessor.getCategoryBuilder().getNameKey();
                order = OrderByType.ASC;
                break;
            case NAME_DESC:
                field = modelBuilderAccessor.getCategoryBuilder().getNameKey();
                order = OrderByType.DESC;
                break;
        }
        try {
            final GetCategories getCategories = new GetCategories(pageIndex, field, categoryService, numberPerPage, order);
            transactionExecutor.execute(getCategories);
            return ModelConvertor.toCateogryList(getCategories.getResult());
        } catch (final SBonitaException e) {
            throw new PageOutOfRangeException(e);
        }
    }

    @Override
    public void addCategoriesToProcess(final long processDefinitionId, final List<Long> categoryIds) throws InvalidSessionException,
            ProcessDefinitionNotFoundException, CategoryMappingException, CategoryNotFoundException {
        for (final Long categoryId : categoryIds) {
            addProcessDefinitionToCategory(categoryId, processDefinitionId);
        }
    }

    @Override
    public void removeCategoriesToProcess(final long processDefinitionId, final List<Long> categoryIds) throws InvalidSessionException,
            CategoryMappingException {
        try {
            final CategoryService categoryService = getTenantAccessor().getCategoryService();
            final TransactionExecutor transactionExecutor = getTenantAccessor().getTransactionExecutor();
            final TransactionContent transactionContent = new RemoveCategoriesFromProcessDefinition(processDefinitionId, categoryIds, categoryService);
            transactionExecutor.execute(transactionContent);
        } catch (final SCategoryException scnfe) {
            throw new CategoryMappingException(scnfe);
        } catch (final SBonitaException sbe) {
            throw new CategoryMappingException(sbe);
        }
    }

    @Override
    public void addProcessDefinitionToCategory(final long categoryId, final long processDefinitionId) throws InvalidSessionException,
            CategoryNotFoundException, ProcessDefinitionNotFoundException, CategoryMappingException {
        try {
            getProcessDefinition(processDefinitionId);
            final CategoryService categoryService = getTenantAccessor().getCategoryService();
            final TransactionExecutor transactionExecutor = getTenantAccessor().getTransactionExecutor();
            final TransactionContent transactionContent = new AddProcessDefinitionToCategory(categoryId, processDefinitionId, categoryService);
            transactionExecutor.execute(transactionContent);
        } catch (final ProcessDefinitionReadException pdre) {
            throw new ProcessDefinitionNotFoundException(pdre);
        } catch (final SCategoryNotFoundException scnfe) {
            throw new CategoryNotFoundException(scnfe);
        } catch (final SBonitaException sbe) {
            throw new CategoryMappingException(sbe);
        }
    }

    @Override
    public void addProcessDefinitionsToCategory(final long categoryId, final List<Long> processDefinitionIds) throws InvalidSessionException,
            CategoryNotFoundException, ProcessDefinitionNotFoundException, CategoryMappingException {
        for (final Long processDefinitionId : processDefinitionIds) {
            addProcessDefinitionToCategory(categoryId, processDefinitionId);
        }
    }

    @Override
    public long getNumberOfCategories(final long processDefinitionId) throws InvalidSessionException, ProcessDefinitionNotFoundException,
            CategoryGettingException {
        try {
            getProcessDefinition(processDefinitionId);
            final TenantServiceAccessor tenantAccessor = getTenantAccessor();
            final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
            final CategoryService categoryService = tenantAccessor.getCategoryService();
            final GetNumberOfCategoriesOfProcess getNumberOfCategoriesOfProcess = new GetNumberOfCategoriesOfProcess(categoryService, processDefinitionId);
            transactionExecutor.execute(getNumberOfCategoriesOfProcess);
            return getNumberOfCategoriesOfProcess.getResult();
        } catch (final ProcessDefinitionReadException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SBonitaException sbe) {
            throw new CategoryGettingException(sbe);
        }
    }

    @Override
    public long getNumberOfProcessesInCategory(final long categoryId) throws InvalidSessionException, CategoryNotFoundException {
        try {
            final CategoryService categoryService = getTenantAccessor().getCategoryService();
            final TransactionExecutor transactionExecutor = getTenantAccessor().getTransactionExecutor();
            final GetProcessDefinitionIdsOfCategory transactionContentWithResult = new GetProcessDefinitionIdsOfCategory(categoryId, categoryService);
            transactionExecutor.execute(transactionContentWithResult);
            final List<Long> ids = transactionContentWithResult.getResult();
            if (ids != null) {
                return ids.size();
            }
        } catch (final SBonitaException e) {
            throw new CategoryNotFoundException(e);
        } catch (final InvalidSessionException e) {
            throw new CategoryNotFoundException(e);
        }
        return 0;
    }

    @Override
    public List<ProcessDeploymentInfo> getProcessDeploymentInfosOfCategory(final long categoryId, final int pageIndex, final int numberPerPage,
            final ProcessDefinitionCriterion pagingCriterion) throws InvalidSessionException, CategoryNotFoundException, PageOutOfRangeException {
        final CategoryService categoryService = getTenantAccessor().getCategoryService();
        final TransactionExecutor transactionExecutor = getTenantAccessor().getTransactionExecutor();
        final GetProcessDefinitionIdsOfCategory transactionContentWithResult = new GetProcessDefinitionIdsOfCategory(categoryId, categoryService);
        // PageIndexCheckingUtil.checkIfPageIsOutOfRange(totalNumber, pageIndex, numberPerPage);

        OrderByType order = null;
        if (pagingCriterion != null) {
            switch (pagingCriterion) {
                case NAME_ASC:
                    order = OrderByType.ASC;
                    break;
                case NAME_DESC:
                    order = OrderByType.DESC;
                    break;
                case DEFAULT:
                    order = OrderByType.ASC;
                    break;
            }
        }

        try {
            transactionExecutor.execute(transactionContentWithResult);
        } catch (final SCategoryNotFoundException e) {
            throw new CategoryNotFoundException(e);
        } catch (final SBonitaException e) {
            throw new CategoryNotFoundException(e);
        }

        final List<Long> processDefinitionIds = transactionContentWithResult.getResult();
        if (processDefinitionIds != null && processDefinitionIds.size() > 0) {
            final List<ProcessDeploymentInfo> processDefinitionDeployInfoList = new ArrayList<ProcessDeploymentInfo>();
            try {
                for (final Long processDefinitionId : processDefinitionIds) {
                    processDefinitionDeployInfoList.add(getProcessDeploymentInfo(processDefinitionId));
                }
            } catch (final ProcessDefinitionNotFoundException e) {
                // TODO
            } catch (final ProcessDefinitionReadException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // sort
            if (processDefinitionDeployInfoList != null) {
                Collections.sort(processDefinitionDeployInfoList, new ProcessDefinitionDeployInfoComparator());
                if (order != null && order == OrderByType.DESC) {
                    Collections.reverse(processDefinitionDeployInfoList);
                }
                final int fromIndex = pageIndex * numberPerPage;

                int toIndex = fromIndex + numberPerPage;
                final int size = processDefinitionDeployInfoList.size();
                if (fromIndex >= size) {
                    throw new PageOutOfRangeException("fromIndex " + fromIndex + " is out of range the size of " + size + "!");
                }
                if (toIndex > size) {
                    toIndex = size;
                }
                return new ArrayList<ProcessDeploymentInfo>(processDefinitionDeployInfoList.subList(fromIndex, toIndex));
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<Category> getCategoriesOfProcessDefinition(final long processDefinitionId, final int pageIndex, final int numberPerPage,
            final CategoryCriterion pagingCriterion) throws InvalidSessionException, ProcessDefinitionNotFoundException, PageOutOfRangeException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final CategoryService categoryService = tenantAccessor.getCategoryService();
        try {
            getProcessDefinition(processDefinitionId);
            final GetNumberOfCategoriesOfProcess getNumberOfCategoriesOfProcess = new GetNumberOfCategoriesOfProcess(categoryService, processDefinitionId);
            transactionExecutor.execute(getNumberOfCategoriesOfProcess);
            final Long totalNumber = getNumberOfCategoriesOfProcess.getResult();
            PageIndexCheckingUtil.checkIfPageIsOutOfRange(totalNumber, pageIndex, numberPerPage);
            OrderByType order = null;
            switch (pagingCriterion) {
                case NAME_ASC:
                    order = OrderByType.ASC;
                    break;
                case NAME_DESC:
                    order = OrderByType.DESC;
                    break;
            }
            final GetSCategoriesOfProcessDefinition getCategories = new GetSCategoriesOfProcessDefinition(processDefinitionId, pageIndex, numberPerPage, order,
                    categoryService);
            transactionExecutor.execute(getCategories);
            return ModelConvertor.toCateogryList(getCategories.getResult());
        } catch (final ProcessDefinitionReadException pdre) {
            throw new ProcessDefinitionNotFoundException(pdre);
        } catch (final SBonitaException sbe) {
            throw new ProcessDefinitionNotFoundException(sbe);
        }
    }

    @Override
    public void updateCategory(final long categoryId, final Category category) throws InvalidSessionException, CategoryNotFoundException,
            CategoryUpdateException {
        if (category == null) {
            throw new CategoryUpdateException("Category can not be null!");
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final CategoryService categoryService = tenantAccessor.getCategoryService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final SCategory sCategory = buildSCategory(category);
            final UpdateCategory updateCategory = new UpdateCategory(categoryId, sCategory, categoryService);
            transactionExecutor.execute(updateCategory);
        } catch (final SCategoryNotFoundException scnfe) {
            throw new CategoryNotFoundException(scnfe);
        } catch (final SBonitaException sbe) {
            throw new CategoryUpdateException(sbe);
        }
    }

    @Override
    public void deleteCategory(final long categoryId) throws InvalidSessionException, CategoryNotFoundException, CategoryDeletionException {
        if (categoryId <= 0) {
            throw new CategoryDeletionException("Category id can not be less than 0!");
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final CategoryService categoryService = tenantAccessor.getCategoryService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final DeleteSCategory deleteSCategory = new DeleteSCategory(categoryService, categoryId);
        try {
            transactionExecutor.execute(deleteSCategory);
        } catch (final SCategoryNotFoundException scnfe) {
            throw new CategoryNotFoundException(scnfe);
        } catch (final SBonitaException sbe) {
            throw new CategoryDeletionException(sbe);
        }
    }

    @Override
    public void removeProcessDefinitionsOfCategory(final long categoryId) throws InvalidSessionException, CategoryNotFoundException, CategoryMappingException {
        if (categoryId <= 0) {
            throw new CategoryNotFoundException("Category id can not be less than 0!");
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final CategoryService categoryService = tenantAccessor.getCategoryService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final RemoveProcessDefinitionsOfCategory remove = new RemoveProcessDefinitionsOfCategory(categoryService, categoryId);
        try {
            transactionExecutor.execute(remove);
        } catch (final SCategoryNotFoundException scnfe) {
            throw new CategoryNotFoundException(scnfe);
        } catch (final SBonitaException sbe) {
            throw new CategoryMappingException(sbe);
        }
    }

    @Override
    public long getNumberOfUncategorizedProcessDefinitions() throws InvalidSessionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final GetAllProcessDefinitionsIds allProcessDefinitionsIds = new GetAllProcessDefinitionsIds(processDefinitionService);
        final CategoryService categoryService = tenantAccessor.getCategoryService();
        try {
            transactionExecutor.execute(allProcessDefinitionsIds);
            final List<Long> processDefinitionIds = allProcessDefinitionsIds.getResult();
            final GetNumberOfUncategorizedProcessIds getNumber = new GetNumberOfUncategorizedProcessIds(categoryService, processDefinitionIds);
            transactionExecutor.execute(getNumber);
            return getNumber.getResult();
        } catch (final SBonitaException sbe) {
            return 0;
        }
    }

    @Override
    public List<ProcessDeploymentInfo> getUncategorizedProcessDeploymentInfos(final int pageIndex, final int numberPerPage,
            final ProcessDefinitionCriterion pagingCriterion) throws InvalidSessionException, PageOutOfRangeException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final GetAllProcessDefinitionsIds allProcessDefinitionsIds = new GetAllProcessDefinitionsIds(processDefinitionService);
        final CategoryService categoryService = tenantAccessor.getCategoryService();
        try {
            transactionExecutor.execute(allProcessDefinitionsIds);
            final List<Long> processDefinitionIds = allProcessDefinitionsIds.getResult();
            final GetUncategorizedProcessIds getUncategorizedProcessIds = new GetUncategorizedProcessIds(categoryService, processDefinitionIds);
            transactionExecutor.execute(getUncategorizedProcessIds);
            final List<Long> uncategorizedProcessIds = getUncategorizedProcessIds.getResult();
            PageIndexCheckingUtil.checkIfPageIsOutOfRange(uncategorizedProcessIds.size(), pageIndex, numberPerPage);
            OrderByType order = null;
            switch (pagingCriterion) {
                case NAME_ASC:
                    order = OrderByType.ASC;
                    break;
                case NAME_DESC:
                    order = OrderByType.DESC;
                    break;
                case DEFAULT:
                    order = OrderByType.ASC;
                    break;
            }
            final GetSProcessDefinitionDeployInfos getSProcessDefinitionDeployInfos = new GetSProcessDefinitionDeployInfos(processDefinitionService,
                    uncategorizedProcessIds, pageIndex * numberPerPage, numberPerPage, "name", order);
            transactionExecutor.execute(getSProcessDefinitionDeployInfos);
            final List<SProcessDefinitionDeployInfo> processDefinitionDeployInfos = getSProcessDefinitionDeployInfos.getResult();
            return ModelConvertor.toProcessDeploymentInfo(processDefinitionDeployInfos);
        } catch (final SBonitaException sbe) {
            throw new PageOutOfRangeException(sbe);
        }
    }

    @Override
    public void removeProcessDefinitionFromCategory(final long processDefinitionId) throws InvalidSessionException, ProcessDefinitionNotFoundException {
        try {
            getProcessDefinition(processDefinitionId);
        } catch (final ProcessDefinitionReadException e) {
            throw new ProcessDefinitionNotFoundException(e);
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final CategoryService categoryService = tenantAccessor.getCategoryService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final TransactionContent transactionContent = new RemoveProcessDefinitionsOfCategory(processDefinitionId, categoryService);
        try {
            transactionExecutor.execute(transactionContent);
        } catch (final SBonitaException sbe) {
            throw new ProcessDefinitionNotFoundException(sbe);
        }
    }

    private SCategory buildSCategory(final Category category) throws InvalidSessionException {
        final SCategoryBuilderAccessor modelBuilderAccessor = getTenantAccessor().getCategoryModelBuilderAccessor();
        final SCategoryBuilder categoryUpdateBuilder = modelBuilderAccessor.getCategoryBuilder();
        categoryUpdateBuilder.createNewInstance(category.getName(), category.getCreator()).setDescription(category.getDescription());
        return categoryUpdateBuilder.done();
    }

    class ProcessDefinitionDeployInfoComparator implements Comparator<ProcessDeploymentInfo> {

        @Override
        public int compare(final ProcessDeploymentInfo o1, final ProcessDeploymentInfo o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    @Override
    public List<EventInstance> getEventInstances(final long rootContainerId, final int pageIndex, final int numberPerPage, final EventSorting sortingType)
            throws InvalidSessionException, EventInstanceReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final EventInstanceService eventInstanceService = tenantAccessor.getEventInstanceService();
        final SEndEventInstanceBuilder eventInstanceBuilder = tenantAccessor.getBPMInstanceBuilders().getSEndEventInstanceBuilder();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();

        OrderByType orderByType = null;
        String fieldName = null;
        switch (sortingType) {
            case NAME_DESC:
                orderByType = OrderByType.DESC;
                fieldName = eventInstanceBuilder.getNameKey();
                break;

            default:
                orderByType = OrderByType.ASC;
                fieldName = eventInstanceBuilder.getNameKey();
                break;
        }

        final GetEventInstances getEventInstances = new GetEventInstances(eventInstanceService, rootContainerId, pageIndex * numberPerPage, numberPerPage,
                fieldName, orderByType);
        try {
            transactionExecutor.execute(getEventInstances);
            final List<SEventInstance> result = getEventInstances.getResult();
            final List<EventInstance> events = ModelConvertor.toEventInstanceList(result, flowNodeStateManager);
            return events;
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new EventInstanceReadException(e.getMessage());
        }
    }

    @Override
    public ManualTaskInstance addManualUserTask(final long humanTaskId, final String taskName, final String displayName, final long assignTo,
            final String description, final Date dueDate, final TaskPriority priority) throws InvalidSessionException, ActivityInterruptedException,
            ActivityExecutionErrorException, ActivityCreationException, ActivityNotFoundException {
        TenantServiceAccessor tenantAccessor = null;
        final TaskPriority prio = priority != null ? priority : TaskPriority.NORMAL;
        try {
            final String userName = getUserNameFromSession();
            tenantAccessor = getTenantAccessor();
            final IdentityService identityService = tenantAccessor.getIdentityService();
            final TransactionExecutor transactionExecutor = getTenantAccessor().getTransactionExecutor();
            final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
            final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
            final GetSUser getSUser = new GetSUser(identityService, userName);
            transactionExecutor.execute(getSUser);
            final long userId = getSUser.getResult().getId();

            final GetActivityInstance getActivityInstance = new GetActivityInstance(activityInstanceService, humanTaskId);
            transactionExecutor.execute(getActivityInstance);
            final SActivityInstance activityInstance = getActivityInstance.getResult();
            if (!(activityInstance instanceof SHumanTaskInstance)) {
                throw new ActivityNotFoundException("The parent activity is not a Human task", humanTaskId);
            }
            if (((SHumanTaskInstance) activityInstance).getAssigneeId() != userId) {
                throw new ActivityCreationException("Unable to create a child task from this task, it's not assigned to you!");
            }
            final TransactionContentWithResult<SManualTaskInstance> createManualUserTask = new CreateManualUserTask(activityInstanceService, taskName,
                    displayName, humanTaskId, assignTo, description, dueDate, STaskPriority.valueOf(prio.name()));
            transactionExecutor.execute(createManualUserTask);
            final long id = createManualUserTask.getResult().getId();
            executeActivity(id);// put it in ready
            final AddActivityInstanceTokenCount addActivityInstanceTokenCount = new AddActivityInstanceTokenCount(activityInstanceService, humanTaskId, 1);
            transactionExecutor.execute(addActivityInstanceTokenCount);
            return ModelConvertor.toManualTask(createManualUserTask.getResult(), flowNodeStateManager);
        } catch (final SActivityInstanceNotFoundException e) {
            log(tenantAccessor, e);
            throw new ActivityNotFoundException(e.getMessage(), humanTaskId);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ActivityCreationException(e.getMessage());
        } catch (final InvalidSessionException e) {
            throw e;
        } catch (final ActivityInterruptedException e) {
            throw e;
        } catch (final ActivityExecutionErrorException e) {
            throw e;
        } catch (final Exception e) {
            log(tenantAccessor, e);
            throw new ActivityExecutionErrorException(e.getMessage());
        }
    }

    @Override
    public void assignUserTask(final long userTaskId, final long userId) throws InvalidSessionException, ActivityInstanceNotFoundException,
            ActivityInstanceReadException, UserNotFoundException {
        final TenantServiceAccessor tenantAccessor;
        tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final SCommentService scommentService = tenantAccessor.getCommentService();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        try {
            final AssignOrUnassignUserTask assignUserTask = new AssignOrUnassignUserTask(userId, userTaskId, activityInstanceService, scommentService,
                    identityService);
            transactionExecutor.execute(assignUserTask);
        } catch (final SUserNotFoundException sunfe) {
            log(tenantAccessor, sunfe);
            throw new UserNotFoundException(sunfe);
        } catch (final SActivityInstanceNotFoundException sainfe) {
            log(tenantAccessor, sainfe);
            throw new ActivityInstanceNotFoundException(userTaskId);
        } catch (final SBonitaException sbe) {
            log(tenantAccessor, sbe);
            throw new ActivityInstanceReadException(sbe);
        }
    }

    @Override
    public List<DataDefinition> getActivityDataDefinitions(final long processDefinitionId, final String activityName, final int pageIndex,
            final int numberPerPage) throws InvalidSessionException, NoSuchActivityDefinitionException, DataNotFoundException,
            ProcessDefinitionNotFoundException {
        List<DataDefinition> subDataDefinitionList = Collections.emptyList();
        List<DataDefinition> dataDefinitionList = Collections.emptyList();
        List<SDataDefinition> sdataDefinitionList = Collections.emptyList();
        final TenantServiceAccessor tenantAccessor;
        tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final GetProcessDefinition getProcessDefinition = new GetProcessDefinition(processDefinitionId, processDefinitionService);
        try {
            transactionExecutor.execute(getProcessDefinition);
        } catch (final SBonitaException e) {
            throw new ProcessDefinitionNotFoundException(e);
        }
        final SProcessDefinition sProcessDefinition = getProcessDefinition.getResult();
        if (sProcessDefinition != null) {
            boolean isHave = false;
            final SFlowElementContainerDefinition processContainer = sProcessDefinition.getProcessContainer();
            final Set<SActivityDefinition> activityDefList = processContainer.getActivities();
            final Iterator<SActivityDefinition> it = activityDefList.iterator();
            while (it.hasNext()) {
                final SActivityDefinition sActivityDefinition = it.next();
                if (sActivityDefinition != null && activityName.equals(sActivityDefinition.getName())) {
                    sdataDefinitionList = sActivityDefinition.getSDataDefinitions();
                    isHave = true;
                    break;
                }
            }
            if (!isHave) {
                throw new NoSuchActivityDefinitionException(activityName);
            }
            dataDefinitionList = ModelConvertor.toDataDefinitionList(sdataDefinitionList);
            final int toIndex = Math.min(dataDefinitionList.size(), (pageIndex + 1) * numberPerPage);
            subDataDefinitionList = new ArrayList<DataDefinition>(dataDefinitionList.subList(pageIndex * numberPerPage, toIndex));
        }
        return subDataDefinitionList;
    }

    @Override
    public List<DataDefinition> getProcessDataDefinitions(final long processDefinitionId, final int pageIndex, final int numberPerPage)
            throws InvalidSessionException, ProcessDefinitionNotFoundException, DataNotFoundException {
        List<DataDefinition> subDataDefinitionList = Collections.emptyList();
        List<DataDefinition> dataDefinitionList = Collections.emptyList();
        List<SDataDefinition> sdataDefinitionList = Collections.emptyList();
        final TenantServiceAccessor tenantAccessor;
        tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final GetProcessDefinition getProcessDefinition = new GetProcessDefinition(processDefinitionId, processDefinitionService);
        try {
            transactionExecutor.execute(getProcessDefinition);
        } catch (final SBonitaException e) {
            throw new ProcessDefinitionNotFoundException(e);
        }
        final SProcessDefinition sProcessDefinition = getProcessDefinition.getResult();
        if (sProcessDefinition != null) {
            final SFlowElementContainerDefinition processContainer = sProcessDefinition.getProcessContainer();
            sdataDefinitionList = processContainer.getDataDefinitions();
            dataDefinitionList = ModelConvertor.toDataDefinitionList(sdataDefinitionList);
            final int toIndex = Math.min(dataDefinitionList.size(), (pageIndex + 1) * numberPerPage);
            subDataDefinitionList = new ArrayList<DataDefinition>(dataDefinitionList.subList(pageIndex * numberPerPage, toIndex));
        }
        return subDataDefinitionList;
    }

    @Override
    public long getNumberOfAssignedTasksSupervisedBy(final long managerUserId) throws InvalidSessionException, UserNotFoundException {
        return 0;
    }

    @Override
    public HumanTaskInstance getHumanTaskInstance(final long activityInstanceId) throws InvalidSessionException, ActivityInstanceNotFoundException,
            ActivityInstanceReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final GetHumanTaskInstance getHumanTaskInstance = new GetHumanTaskInstance(activityInstanceService, activityInstanceId);
        try {
            transactionExecutor.execute(getHumanTaskInstance);
        } catch (final SActivityInstanceNotFoundException e) {
            log(tenantAccessor, e);
            throw new ActivityInstanceNotFoundException(activityInstanceId);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ActivityInstanceReadException(e.getMessage());
        }
        return ModelConvertor.toHumanTaskInstance(getHumanTaskInstance.getResult(), flowNodeStateManager);
    }

    @Override
    public long getNumberOfAssignedHumanTaskInstances(final long userId) throws InvalidSessionException, ActivityInstanceReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final TransactionContentWithResult<Long> transactionContent = new GetNumberOfAssignedUserTaskInstances(userId, activityInstanceService);
            transactionExecutor.execute(transactionContent);
            return transactionContent.getResult();
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ActivityInstanceReadException(e.getMessage());
        }
    }

    @Override
    public Map<Long, Long> getNumberOfOpenTasks(final List<Long> userIds) throws InvalidSessionException, ActivityInstanceReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final GetNumberOfOpenTasksForUsers transactionContent = new GetNumberOfOpenTasksForUsers(userIds, activityInstanceService);
            transactionExecutor.execute(transactionContent);
            return transactionContent.getResult();
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ActivityInstanceReadException(e.getMessage());
        }
    }

    @Override
    public long getNumberOfPendingHumanTaskInstances(final long userId) throws InvalidSessionException, ActivityInstanceReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        try {
            final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
            final ProcessDefinitionService processDefService = tenantAccessor.getProcessDefinitionService();
            final Set<Long> actorIds = getActorsForUser(userId, actorMappingService, identityService, transactionExecutor, processDefService);
            if (actorIds.isEmpty()) {
                return 0L;
            }
            final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
            final TransactionContentWithResult<Long> transactionContent = new GetNumberOfPendingHumanTaskInstances(userId, actorIds, activityInstanceService);
            transactionExecutor.execute(transactionContent);
            return transactionContent.getResult();
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ActivityInstanceReadException(e.getMessage());
        }
    }

    @Override
    public Map<String, byte[]> getProcessResources(final long processDefinitionId, final String filenamesPattern) throws InvalidSessionException,
            ProcessResourceException {

        String processesFolder;
        TenantServiceAccessor tenantAccessor = null;
        try {
            tenantAccessor = getTenantAccessor();
            processesFolder = BonitaHomeServer.getInstance().getProcessesFolder(tenantAccessor.getTenantId());
        } catch (final BonitaHomeNotSetException e) {
            log(tenantAccessor, e);
            throw new ProcessResourceException("Problem accessing basic Bonita Home server resources", e);
        }
        processesFolder = processesFolder.replaceAll("\\\\", "/");
        if (!processesFolder.endsWith("/")) {
            processesFolder = processesFolder + "/";
        }
        processesFolder = processesFolder + processDefinitionId + "/";
        @SuppressWarnings("unchecked")
        final Collection<File> files = FileUtils.listFiles(new File(processesFolder), new DeepRegexFileFilter(processesFolder + filenamesPattern),
                DirectoryFileFilter.DIRECTORY);
        final Map<String, byte[]> res = new HashMap<String, byte[]>(files.size());
        try {
            for (final File f : files) {
                res.put(f.getAbsolutePath().replaceAll("\\\\", "/").replaceFirst(processesFolder, ""), IOUtil.getAllContentFrom(f));
            }
        } catch (final IOException e) {
            log(tenantAccessor, e);
            throw new ProcessResourceException("Problem accessing resources " + filenamesPattern + " for processDefinitionId: " + processDefinitionId);
        }
        return res;
    }

    @Override
    public long getLatestProcessDefinitionId(final String processName) throws ProcessDefinitionNotFoundException, InvalidSessionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final TransactionContentWithResult<Long> transactionContent = new GetLatestProcessDefinitionId(processDefinitionService, processName);
        try {
            transactionExecutor.execute(transactionContent);
        } catch (final SBonitaException e) {
            throw new ProcessDefinitionNotFoundException(e);
        }
        return transactionContent.getResult();
    }

    @Override
    public List<DataInstance> getProcessDataInstances(final long processInstanceId, final int pageIndex, final int numberPerPage)
            throws InvalidSessionException, DataNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final DataInstanceService dataInstanceService = tenantAccessor.getDataInstanceService();
        final GetDataInstances getDataInstances = new GetDataInstances(processInstanceId, DataInstanceContainer.PROCESS_INSTANCE.toString(), pageIndex
                * numberPerPage, numberPerPage, dataInstanceService);
        try {
            transactionExecutor.execute(getDataInstances);
            final List<SDataInstance> result = getDataInstances.getResult();
            return ModelConvertor.toDataInstanceList(result);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            // FIXME should not throw data not found here
            throw new DataNotFoundException(e.getMessage());
        }
    }

    @Override
    public DataInstance getProcessDataInstance(final String dataName, final long containerId) throws InvalidSessionException, DataNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final DataInstanceService dataInstanceService = tenantAccessor.getDataInstanceService();
        final GetDataInstance getDataInstance = new GetDataInstance(dataName, containerId, DataInstanceContainer.PROCESS_INSTANCE.toString(),
                dataInstanceService);
        try {
            transactionExecutor.execute(getDataInstance);
            final SDataInstance result = getDataInstance.getResult();
            return ModelConvertor.toDataInstance(result);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new DataNotFoundException(e.getMessage());
        }
    }

    @Override
    public void updateProcessDataInstance(final String dataName, final long containerId, final Serializable dataValue) throws InvalidSessionException,
            DataUpdateException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final DataInstanceService dataInstanceService = tenantAccessor.getDataInstanceService();
        final UpdateDataInstance updateDataInstance = new UpdateDataInstance(dataName, containerId, DataInstanceContainer.PROCESS_INSTANCE.toString(),
                dataValue, dataInstanceService);
        try {
            transactionExecutor.execute(updateDataInstance);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new DataUpdateException(e.getMessage());
        }
    }

    @Override
    public List<DataInstance> getActivityDataInstances(final long activityInstanceId, final int pageIndex, final int numberPerPage)
            throws InvalidSessionException, DataNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final DataInstanceService dataInstanceService = tenantAccessor.getDataInstanceService();
        final GetDataInstances getDataInstances = new GetDataInstances(activityInstanceId, DataInstanceContainer.ACTIVITY_INSTANCE.toString(), pageIndex
                * numberPerPage, numberPerPage, dataInstanceService);
        try {
            transactionExecutor.execute(getDataInstances);
            final List<SDataInstance> result = getDataInstances.getResult();
            return ModelConvertor.toDataInstanceList(result);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new DataNotFoundException(e.getMessage());
        }
    }

    @Override
    public DataInstance getActivityDataInstance(final String dataName, final long activityInstanceId) throws InvalidSessionException, DataNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final DataInstanceService dataInstanceService = tenantAccessor.getDataInstanceService();
        final GetDataInstance getDataInstance = new GetDataInstance(dataName, activityInstanceId, DataInstanceContainer.ACTIVITY_INSTANCE.toString(),
                dataInstanceService);
        try {
            transactionExecutor.execute(getDataInstance);
            final SDataInstance result = getDataInstance.getResult();
            return ModelConvertor.toDataInstance(result);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new DataNotFoundException(e.getMessage());
        }
    }

    @Override
    public void updateActivityDataInstance(final String dataName, final long activityInstanceId, final Serializable dataValue) throws InvalidSessionException,
            DataUpdateException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final DataInstanceService dataInstanceService = tenantAccessor.getDataInstanceService();
        final UpdateDataInstance updateDataInstance = new UpdateDataInstance(dataName, activityInstanceId, DataInstanceContainer.ACTIVITY_INSTANCE.toString(),
                dataValue, dataInstanceService);
        try {
            transactionExecutor.execute(updateDataInstance);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new DataUpdateException(e.getMessage());
        }
    }

    @Override
    public void importActorMapping(final long processDefinitionId, final String xmlContent) throws InvalidSessionException, ActorMappingImportException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final Parser parser = tenantAccessor.getActorMappingParser();
        final ImportActorMapping importActorMapping = new ImportActorMapping(actorMappingService, identityService, parser, processDefinitionId, xmlContent);
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            transactionExecutor.execute(importActorMapping);
        } catch (final SBonitaException sbe) {
            throw new ActorMappingImportException(sbe);
        }
    }

    @Override
    public String exportActorMapping(final long processDefinitionId) throws InvalidSessionException, ActorMappingExportException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final XMLWriter writer = tenantAccessor.getXMLWriter();
        final ExportActorMapping exportActorMapping = new ExportActorMapping(actorMappingService, identityService, writer, processDefinitionId);
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            transactionExecutor.execute(exportActorMapping);
            return exportActorMapping.getResult();
        } catch (final SBonitaException sbe) {
            throw new ActorMappingExportException(sbe);
        }
    }

    @Override
    public List<Long> getChildrenInstanceIdsOfProcessInstance(final long processInstanceId, final int pageIndex, final int numberPerPage,
            final ProcessInstanceCriterion criterion) throws ProcessInstanceNotFoundException, InvalidSessionException, PageOutOfRangeException {

        final long totalNumber = getNumberOfChildProcessInstances(processInstanceId);

        if (totalNumber == 0) {
            return new ArrayList<Long>();
        }

        PageIndexCheckingUtil.checkIfPageIsOutOfRange(totalNumber, pageIndex, numberPerPage);

        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SProcessInstanceBuilder modelBuilder = tenantAccessor.getBPMInstanceBuilders().getSProcessInstanceBuilder();
        String field = null;
        OrderByType order = null;
        switch (criterion) {
            case NAME_ASC:
                field = modelBuilder.getNameKey();
                order = OrderByType.ASC;
                break;
            case NAME_DESC:
                field = modelBuilder.getNameKey();
                order = OrderByType.DESC;
                break;
            case CREATION_DATE_ASC: // creation date can be seen as start date
                field = modelBuilder.getStartDateKey();
                order = OrderByType.ASC;
                break;
            case CREATION_DATE_DESC:
                field = modelBuilder.getStartDateKey();
                order = OrderByType.DESC;
                break;
            case LAST_UPDATE_ASC:
                field = modelBuilder.getLastUpdateKey();
                order = OrderByType.ASC;
                break;
            case LAST_UPDATE_DESC:
                field = modelBuilder.getLastUpdateKey();
                order = OrderByType.DESC;
                break;
            case DEFAULT:
                field = modelBuilder.getNameKey();
                order = OrderByType.DESC;
                break;
        }
        try {
            final TransactionContentWithResult<List<Long>> transactionContent = new GetChildInstanceIdsOfProcessInstance(processInstanceService,
                    processInstanceId, pageIndex, numberPerPage, field, order);
            transactionExecutor.execute(transactionContent);
            return transactionContent.getResult();
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
        }

        return Collections.emptyList();
    }

    private long getNumberOfChildProcessInstances(final long processInstanceId) throws ProcessInstanceNotFoundException, InvalidSessionException {
        try {
            getProcessInstance(processInstanceId);
            final TenantServiceAccessor tenantAccessor = getTenantAccessor();
            final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
            final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
            final TransactionContentWithResult<Long> transactionContent = new GetNumberOfChildInstancesOfProcessInstance(processInstanceService,
                    processInstanceId);
            transactionExecutor.execute(transactionContent);
            return transactionContent.getResult();
        } catch (final ProcessInstanceReadException e) {
            throw new ProcessInstanceNotFoundException(e);
        } catch (final SBonitaException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean isInvolvedInProcessInstance(final long userId, final long processInstanceId) throws ProcessInstanceNotFoundException,
            InvalidSessionException, UserNotFoundException {
        // check processInstance existence
        try {
            getProcessInstance(processInstanceId);
        } catch (final ProcessInstanceReadException e) {
            throw new ProcessInstanceNotFoundException(e);
        }
        // check user existence
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();

        final IdentityService identityService = tenantAccessor.getIdentityService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final GetActivities getActivityInstances = new GetActivities(processInstanceId, 0, 0, activityInstanceService);
        try {
            transactionExecutor.execute(getActivityInstances);
            final List<SActivityInstance> result = getActivityInstances.getResult();
            for (final SActivityInstance activityInstance : result) {
                if (activityInstance instanceof SUserTaskInstance) {
                    final SUserTaskInstance userTaskInstance = (SUserTaskInstance) activityInstance;
                    if (userId == userTaskInstance.getAssigneeId()) {
                        return true;
                    }
                    final long actorId = userTaskInstance.getActorId();
                    final int numOfActorMembers = (int) getNumberOfActorMembers(actorId);
                    final int numberPerPage = 100;
                    for (int i = 0; i < numOfActorMembers % numberPerPage; i++) {
                        final List<ActorMember> actorMembers = getActorMembers(actorId, i, numberPerPage, ActorMemberSorting.NAME_ASC);
                        for (final ActorMember actorMember : actorMembers) {
                            if (actorMember.getUserId() == userId) {
                                return true;
                            }
                            // if userId is as id of a user manager, return true
                            final GetSUser getUser = new GetSUser(identityService, actorMember.getUserId());
                            try {
                                transactionExecutor.execute(getUser);
                                final SUser user = getUser.getResult();
                                if (userId == user.getManagerUserId()) {
                                    return true;
                                }
                            } catch (final SBonitaException e) {
                                throw new UserNotFoundException(e);
                            }
                        }
                    }
                }
            }
        } catch (final SBonitaException e) {
            throw new InvalidSessionException(e);// TODO FIXME
        } catch (final ActorNotFoundException e) {
            throw new InvalidSessionException(e);
        } catch (final PageOutOfRangeException e) {
            throw new InvalidSessionException(e);
        }
        return false;
    }

    @Override
    public long getProcessInstanceIdFromActivityInstanceId(final long activityInstanceId) throws InvalidSessionException, ProcessInstanceNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final GetActivityInstance getActivityInstance = new GetActivityInstance(activityInstanceService, activityInstanceId);
        try {
            transactionExecutor.execute(getActivityInstance);
            final SActivityInstance activity = getActivityInstance.getResult();
            return activity.getRootContainerId();
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ProcessInstanceNotFoundException(e.getMessage());
        }
    }

    @Override
    public long getProcessDefinitionIdFromActivityInstanceId(final long activityInstanceId) throws InvalidSessionException, ProcessDefinitionNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final GetActivityInstance getActivityInstance = new GetActivityInstance(activityInstanceService, activityInstanceId);
        try {
            transactionExecutor.execute(getActivityInstance);
            final SActivityInstance activity = getActivityInstance.getResult();
            final GetProcessInstance getProcessInstance = new GetProcessInstance(processInstanceService, activity.getRootContainerId());
            transactionExecutor.execute(getProcessInstance);
            final SProcessInstance processInstance = getProcessInstance.getResult();
            return processInstance.getProcessDefinitionId();
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ProcessDefinitionNotFoundException(e.getMessage());
        }
    }

    @Override
    public long getProcessDefinitionIdFromProcessInstanceId(final long processInstanceId) throws InvalidSessionException, ProcessDefinitionNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        try {
            final GetProcessInstance getProcessInstance = new GetProcessInstance(processInstanceService, processInstanceId);
            transactionExecutor.execute(getProcessInstance);
            final SProcessInstance processInstance = getProcessInstance.getResult();
            return processInstance.getProcessDefinitionId();
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ProcessDefinitionNotFoundException(e.getMessage());
        }
    }

    @Override
    public Date getActivityReachedStateDate(final long activityInstanceId, final String stateName) throws InvalidSessionException,
            ActivityInstanceReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final int stateId = ModelConvertor.getServerActivityStateId(stateName);
        final GetArchivedActivityInstance getArchivedActivityInstance = new GetArchivedActivityInstance(activityInstanceId, stateId, activityInstanceService,
                persistenceService);
        try {
            transactionExecutor.execute(getArchivedActivityInstance);
            final long reachedDate = getArchivedActivityInstance.getResult().getReachedStateDate();
            return new Date(reachedDate);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ActivityInstanceReadException(e);
        }
    }

    @Override
    public Set<String> getSupportedStates(final FlowNodeType nodeType) throws InvalidSessionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        return flowNodeStateManager.getSupportedState(nodeType);
    }

    @Override
    public void updateActivityInstanceVariables(final long activityInstanceId, final Map<String, Serializable> variables) throws InvalidSessionException,
            DataNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final DataInstanceService dataInstanceService = tenantAccessor.getDataInstanceService();
        for (final Entry<String, Serializable> variable : variables.entrySet()) {
            try {
                final UpdateDataInstance updateDataInstance = new UpdateDataInstance(variable.getKey(), activityInstanceId,
                        DataInstanceContainer.ACTIVITY_INSTANCE.toString(), variable.getValue(), dataInstanceService);
                transactionExecutor.execute(updateDataInstance);
            } catch (final SDataInstanceException sdie) {
                throw new DataNotFoundException(sdie);
            } catch (final SBonitaException sbe) {
                throw new DataNotFoundException(sbe);
            }
        }
    }

    @Override
    public void updateActivityInstanceVariables(final List<Operation> operations, final long activityInstanceId,
            final Map<String, Serializable> expressionContexts) throws InvalidSessionException, OperationExecutionException, DataNotFoundException,
            ActivityInstanceNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final OperationService operationService = tenantAccessor.getOperationService();
        final SOperationBuilders sOperationBuilders = tenantAccessor.getSOperationBuilders();
        final SExpressionBuilders sExpressionBuilders = tenantAccessor.getSExpressionBuilders();

        final long num = getNumberOfActivityDataInstances(activityInstanceId);
        final int activityDataInstanceNum = (int) num;
        final List<DataInstance> dataInstances = getActivityDataInstances(activityInstanceId, 0, activityDataInstanceNum);
        for (final DataInstance dataInstance : dataInstances) {
            for (final Operation operation : operations) {
                if (operation.getVariableToSet().getDataName().equals(dataInstance.getName())) {
                    final SOperation sOperation = toSOperation(operation, sOperationBuilders, sExpressionBuilders);
                    final SExpressionContext sExpressionContext = new SExpressionContext();
                    sExpressionContext.setSerializableInputValues(expressionContexts);
                    final ExecuteOperation executeOperation = new ExecuteOperation(operationService, dataInstance.getContainerId(),
                            dataInstance.getContainerType(), sOperation, sExpressionContext);
                    try {
                        transactionExecutor.execute(executeOperation);
                    } catch (final SBonitaException e) {
                        log(tenantAccessor, e);
                        throw new OperationExecutionException(e);
                    }

                }
            }
        }
    }

    @Override
    public long getOneAssignedUserTaskInstanceOfProcessInstance(final long processInstanceId, final long userId) throws InvalidSessionException,
            UserNotFoundException, ActivityInstanceReadException {
        // FIXME: write specific query that should be more efficient:
        final int assignedUserTaskInstanceNumber = (int) getNumberOfAssignedHumanTaskInstances(userId);
        final List<HumanTaskInstance> userTaskInstances = getAssignedHumanTaskInstances(userId, 0, assignedUserTaskInstanceNumber,
                ActivityInstanceCriterion.DEFAULT);
        String stateName = null;
        if (userTaskInstances.size() != 0) {
            for (final HumanTaskInstance userTaskInstance : userTaskInstances) {
                stateName = userTaskInstance.getState();
                final long userTaskInstanceId = userTaskInstance.getId();
                if (stateName.equals(ActivityStates.READY_STATE) && userTaskInstance.getParentContainerId() == processInstanceId) {
                    return userTaskInstanceId;
                }
            }
        }
        return -1;
    }

    @Override
    public long getOneAssignedUserTaskInstanceOfProcessDefinition(final long processDefinitionId, final long userId) throws InvalidSessionException,
            UserNotFoundException, ProcessInstanceNotFoundException, ProcessInstanceReadException, ActivityInstanceReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final int assignedUserTaskInstanceNumber = (int) getNumberOfAssignedHumanTaskInstances(userId);
        final List<HumanTaskInstance> userTaskInstances = getAssignedHumanTaskInstances(userId, 0, assignedUserTaskInstanceNumber,
                ActivityInstanceCriterion.DEFAULT);
        String stateName = null;
        if (userTaskInstances.size() != 0) {
            for (final HumanTaskInstance userTaskInstance : userTaskInstances) {
                stateName = userTaskInstance.getState();
                SProcessInstance sProcessInstance = null;
                try {
                    final GetProcessInstance getProcessInstance = new GetProcessInstance(processInstanceService, userTaskInstance.getRootContainerId());
                    transactionExecutor.execute(getProcessInstance);
                    sProcessInstance = getProcessInstance.getResult();
                } catch (final SBonitaException e) {
                    throw new ProcessInstanceNotFoundException(e.getMessage());
                }
                if (stateName.equals(ActivityStates.READY_STATE) && sProcessInstance.getProcessDefinitionId() == processDefinitionId) {
                    return userTaskInstance.getId();
                }
            }
        }
        return -1;
    }

    @Override
    public String getActivityInstanceState(final long activityInstanceId) throws InvalidSessionException, ActivityInstanceNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        try {
            final GetActivityInstance getActivityInstance = new GetActivityInstance(activityInstanceService, activityInstanceId);
            transactionExecutor.execute(getActivityInstance);
            final SActivityInstance sActivity = getActivityInstance.getResult();
            final ActivityInstance activityInstance = ModelConvertor.toActivityInstance(sActivity, flowNodeStateManager);
            final String stateName = activityInstance.getState();
            return stateName;
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ActivityInstanceNotFoundException(activityInstanceId);
        }
    }

    @Override
    public boolean canExecuteTask(final long activityInstanceId, final long userId) throws InvalidSessionException, ActivityInstanceNotFoundException,
            UserNotFoundException, ActivityInstanceReadException {
        final TenantServiceAccessor tenantAccessor;
        tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final IdentityService identityService = tenantAccessor.getIdentityService();

        final GetSUser getSUser = new GetSUser(identityService, userId);
        try {
            transactionExecutor.execute(getSUser);
        } catch (final SBonitaException e) {
            throw new UserNotFoundException(e.getMessage());
        }
        final HumanTaskInstance userTaskInstance = getHumanTaskInstance(activityInstanceId);
        final String stateName = getActivityInstanceState(activityInstanceId);
        if (stateName.equalsIgnoreCase(ActivityStates.READY_STATE) && userTaskInstance.getAssigneeId() == userId) {
            return true;
        }
        return false;
    }

    @Override
    public long getProcessDefinitionId(final String name, final String version) throws ProcessDefinitionNotFoundException, InvalidSessionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final TransactionContentWithResult<Long> transactionContent = new GetProcessDefinitionIDByNameAndVersion(processDefinitionService, name, version);
        try {
            transactionExecutor.execute(transactionContent);
        } catch (final SBonitaException e) {
            throw new ProcessDefinitionNotFoundException(e);
        }
        return transactionContent.getResult();
    }

    @Override
    public void releaseUserTask(final long userTaskId) throws InvalidSessionException, ActivityNotFoundException, TaskReleaseException,
            UnreleasableTaskException {
        final TenantServiceAccessor tenantAccessor;
        tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        try {
            final AssignOrUnassignUserTask assignUserTask = new AssignOrUnassignUserTask(0, userTaskId, activityInstanceService, null, null);
            transactionExecutor.execute(assignUserTask);
        } catch (final SUnreleasableTaskException e) {
            throw new UnreleasableTaskException(e.getMessage());
        } catch (final SActivityInstanceNotFoundException e) {
            throw new ActivityNotFoundException(e.getMessage(), userTaskId);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new TaskReleaseException(e.getMessage());
        }
    }

    @Override
    public void updateProcessDeploymentInfo(final long processDefinitionId, final ProcessDeploymentInfoUpdateDescriptor processDeploymentInfoUpdateDescriptor)
            throws InvalidSessionException, ProcessDefinitionNotFoundException, ProcessDeploymentInfoUpdateException {
        if (processDeploymentInfoUpdateDescriptor == null || processDeploymentInfoUpdateDescriptor.getFields().isEmpty()) {
            throw new ProcessDeploymentInfoUpdateException("The update descriptor does not contain field updates");
        }
        ProcessDefinition processDefinition;
        try {
            processDefinition = getProcessDefinition(processDefinitionId);
        } catch (final ProcessDefinitionReadException e) {
            throw new ProcessDefinitionNotFoundException(e);
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SProcessDefinitionDeployInfoUpdateBuilder processDeploymentInfoUpdateBuilder = tenantAccessor.getBPMDefinitionBuilders()
                .getProcessDefinitionDeployInfoUpdateBuilder();
        final UpdateProcessDeploymentInfo updateProcessDeploymentInfo = new UpdateProcessDeploymentInfo(processDefinitionService,
                processDeploymentInfoUpdateBuilder, processDefinition.getId(), processDeploymentInfoUpdateDescriptor);
        try {
            transactionExecutor.execute(updateProcessDeploymentInfo);
        } catch (final SBonitaException sbe) {
            throw new ProcessDeploymentInfoUpdateException(sbe);
        }
    }

    @Override
    public List<ProcessDeploymentInfo> getStartableProcessesForActors(final Set<Long> actorIds, final int pageIndex, final int numberPerPage)
            throws InvalidSessionException, ActorNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();

        final GetActorInitiators getActorsForInitiator = new GetActorInitiators(actorMappingService, actorIds);
        final List<ProcessDeploymentInfo> processDeploymentInfos = new ArrayList<ProcessDeploymentInfo>(10);
        try {
            transactionExecutor.execute(getActorsForInitiator);
            final List<SActor> sActors = getActorsForInitiator.getResult();

            for (final SActor sActor : sActors) {
                processDeploymentInfos.add(getProcessDeploymentInfo(sActor.getScopeId()));
            }
        } catch (final SBonitaException e) {
            throw new ActorNotFoundException(e);
        } catch (final ProcessDefinitionNotFoundException e) {
            throw new ActorNotFoundException(e);
        } catch (final ProcessDefinitionReadException e) {
            throw new ActorNotFoundException(e);
        }

        return processDeploymentInfos;
    }

    @Override
    public boolean isAllowedToStartProcess(final long processDefinitionId, final Set<Long> actorIds) throws InvalidSessionException, ActorNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();

        final GetActorInitiators getActorsForInitiator = new GetActorInitiators(actorMappingService, actorIds);
        boolean isAllowedToStartProcess = false;
        try {
            transactionExecutor.execute(getActorsForInitiator);
            final List<SActor> sActors = getActorsForInitiator.getResult();

            for (final SActor sActor : sActors) {
                if (sActor.getScopeId() == processDefinitionId) {
                    isAllowedToStartProcess = true;
                }
                break;
            }
        } catch (final SBonitaException e) {
            throw new ActorNotFoundException(e);
        }

        return isAllowedToStartProcess;
    }

    @Override
    public ActorInstance getActorInitiator(final long processDefinitionId) throws InvalidSessionException, ActorNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final GetProcessDefinition getProcessDefinition = new GetProcessDefinition(processDefinitionId, processDefinitionService);
        ActorInstance actorInstance = null;
        try {
            transactionExecutor.execute(getProcessDefinition);
            final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
            final SProcessDefinition definition = getProcessDefinition.getResult();
            final SActorDefinition sActorDefinition = definition.getActorInitiator();
            if (sActorDefinition == null) {
                throw new ActorNotFoundException("No actor initiator defined on the process");
            }
            final String name = sActorDefinition.getName();
            final GetActor getActor = new GetActor(actorMappingService, name, processDefinitionId);
            transactionExecutor.execute(getActor);
            final SActor sActor = getActor.getResult();
            actorInstance = ModelConvertor.toActorInstance(sActor);
        } catch (final SBonitaException e) {
            throw new ActorNotFoundException("Actor no found.", e);
        }
        if (actorInstance == null) {
            throw new ActorNotFoundException("Actor no found.");
        }
        return actorInstance;
    }

    @Override
    public int getNumberOfActivityDataDefinitions(final long processDefinitionId, final String activityName) throws InvalidSessionException,
            ProcessDefinitionNotFoundException {
        List<SDataDefinition> sdataDefinitionList = Collections.emptyList();
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final GetProcessDefinition getProcessDefinition = new GetProcessDefinition(processDefinitionId, processDefinitionService);
        try {
            transactionExecutor.execute(getProcessDefinition);
        } catch (final SBonitaException e) {
            throw new ProcessDefinitionNotFoundException(e);
        }
        final SProcessDefinition sProcessDefinition = getProcessDefinition.getResult();
        if (sProcessDefinition != null) {
            boolean isHave = false;
            final SFlowElementContainerDefinition processContainer = sProcessDefinition.getProcessContainer();
            final Set<SActivityDefinition> activityDefList = processContainer.getActivities();
            final Iterator<SActivityDefinition> it = activityDefList.iterator();
            while (it.hasNext()) {
                final SActivityDefinition sActivityDefinition = it.next();
                if (sActivityDefinition != null && activityName.equals(sActivityDefinition.getName())) {
                    sdataDefinitionList = sActivityDefinition.getSDataDefinitions();
                    isHave = true;
                    break;
                }
            }
            if (!isHave) {
                throw new ProcessDefinitionNotFoundException(activityName);
            }
            return sdataDefinitionList.size();
        }
        return 0;
    }

    @Override
    public int getNumberOfProcessDataDefinitions(final long processDefinitionId) throws InvalidSessionException, ProcessDefinitionNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();

        final GetProcessDefinition getProcessDefinition = new GetProcessDefinition(processDefinitionId, processDefinitionService);
        try {
            transactionExecutor.execute(getProcessDefinition);
        } catch (final SBonitaException e) {
            throw new ProcessDefinitionNotFoundException(e);
        }
        final SProcessDefinition sProcessDefinition = getProcessDefinition.getResult();
        final SFlowElementContainerDefinition processContainer = sProcessDefinition.getProcessContainer();
        return processContainer.getDataDefinitions().size();
    }

    @Override
    public ProcessInstance startProcess(final long processDefinitionId, final Map<Operation, Map<String, Serializable>> operations)
            throws InvalidSessionException, ProcessDefinitionNotFoundException, ProcessDefinitionNotEnabledException, ProcessInstanceCreationException,
            OperationExecutionException {
        try {
            return this.startProcess(0, processDefinitionId, operations);
        } catch (final ProcessDefinitionReadException e) {
            throw new ProcessDefinitionNotFoundException(e);
        }
    }

    private String getUserNameFromSession() throws InvalidSessionException {
        SessionAccessor sessionAccessor = null;
        String userName;
        try {
            sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long sessionId = sessionAccessor.getSessionId();
            final PlatformServiceAccessor platformServiceAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
            userName = platformServiceAccessor.getSessionService().getSession(sessionId).getUserName();
        } catch (final Exception e) {
            throw new InvalidSessionException(e);
        }
        return userName;
    }

    private long getUserIdFromSession() throws InvalidSessionException {
        SessionAccessor sessionAccessor = null;
        long userId;
        try {
            sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long sessionId = sessionAccessor.getSessionId();
            final PlatformServiceAccessor platformServiceAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
            userId = platformServiceAccessor.getSessionService().getSession(sessionId).getUserId();
        } catch (final Exception e) {
            throw new InvalidSessionException(e);
        }
        return userId;
    }

    @Override
    public ProcessInstance startProcess(long userId, final long processDefinitionId, final Map<Operation, Map<String, Serializable>> operations)
            throws InvalidSessionException, ProcessDefinitionNotFoundException, ProcessInstanceCreationException, ProcessDefinitionReadException,
            ProcessDefinitionNotEnabledException, OperationExecutionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final ProcessExecutor processExecutor = tenantAccessor.getProcessExecutor();
        final ProcessInstanceStateManager processInstanceStateManager = tenantAccessor.getProcessInstanceStateManager();
        final SOperationBuilders sOperationBuilders = tenantAccessor.getSOperationBuilders();
        final SExpressionBuilders sExpressionBuilders = tenantAccessor.getSExpressionBuilders();
        if (userId == 0) {
            userId = getUserIdFromSession();
        }
        // Retrieval of the process definition:
        SProcessDefinition sDefinition;
        try {
            final GetProcessDeploymentInfo transactionContentWithResult = new GetProcessDeploymentInfo(processDefinitionId, processDefinitionService);
            transactionExecutor.execute(transactionContentWithResult);
            final SProcessDefinitionDeployInfo deployInfo = transactionContentWithResult.getResult();
            if (!ProcessDefinitionStates.ENABLED.equals(deployInfo.getState())) {
                throw new ProcessDefinitionNotEnabledException(deployInfo.getName(), deployInfo.getVersion(), deployInfo.getProcessId());
            }
            sDefinition = getServerProcessDefinition(transactionExecutor, processDefinitionId, processDefinitionService);
        } catch (final SProcessDefinitionNotFoundException e) {
            log(tenantAccessor, e);
            throw new ProcessDefinitionNotFoundException(e.getMessage());
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ProcessDefinitionReadException(e.getMessage());
        }

        SProcessInstance startedInstance;
        try {
            final Map<SOperation, Map<String, Serializable>> sOperations = toSOperation(operations, sOperationBuilders, sExpressionBuilders);
            startedInstance = processExecutor.start(userId, sDefinition, sOperations);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ProcessInstanceCreationException(e);
        }// FIXME in case process instance creation exception -> put it in failed
        return ModelConvertor.toProcessInstance(startedInstance, processInstanceStateManager);
    }

    private Map<SOperation, Map<String, Serializable>> toSOperation(final Map<Operation, Map<String, Serializable>> operations,
            final SOperationBuilders sOperationBuilders, final SExpressionBuilders sExpressionBuilders) {
        if (operations == null) {
            return null;
        }
        if (operations.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<SOperation, Map<String, Serializable>> sOperations = new HashMap<SOperation, Map<String, Serializable>>();
        for (final Operation operation : operations.keySet()) {
            final SOperation sOperation = toSOperation(operation, sOperationBuilders, sExpressionBuilders);
            sOperations.put(sOperation, operations.get(operation));
        }
        return sOperations;
    }

    private SOperation toSOperation(final Operation operation, final SOperationBuilders sOperationBuilders, final SExpressionBuilders sExpressionBuilders) {
        final SExpression rightOperand = ModelConvertor.constructSExpression(operation.getRightOperand(), sExpressionBuilders.getExpressionBuilder());
        final SOperatorType operatorType = SOperatorType.valueOf(operation.getType().name());
        final SLeftOperand sLeftOperand = toSLeftOperand(operation.getVariableToSet(), sOperationBuilders);
        final SOperation sOperation = sOperationBuilders.getSOperationBuilder().createNewInstance().setOperator(operation.getOperator())
                .setRightOperand(rightOperand).setType(operatorType).setVariableToSet(sLeftOperand).done();
        return sOperation;
    }

    private SLeftOperand toSLeftOperand(final LeftOperand variableToSet, final SOperationBuilders sOperationBuilders) {
        return sOperationBuilders.getSLeftOperandBuilder().createNewInstance().setDataName(variableToSet.getDataName()).done();
    }

    @Override
    public long getNumberOfActivityDataInstances(final long activityInstanceId) throws InvalidSessionException, ActivityInstanceNotFoundException {
        try {
            return getNumberOfDataInstancesOfContainer(activityInstanceId, DataInstanceContainer.ACTIVITY_INSTANCE);
        } catch (final SBonitaException e) {
            throw new ActivityInstanceNotFoundException(activityInstanceId);
        }
    }

    private long getNumberOfDataInstancesOfContainer(final long activityInstanceId, final DataInstanceContainer containerType) throws InvalidSessionException,
            SBonitaException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final DataInstanceService dataInstanceService = tenantAccessor.getDataInstanceService();
        final GetNumberOfDataInstanceForContainer getNumberOfDataInstance = new GetNumberOfDataInstanceForContainer(activityInstanceId, containerType,
                dataInstanceService);
        transactionExecutor.execute(getNumberOfDataInstance);
        final long result = getNumberOfDataInstance.getResult();
        return result;
    }

    @Override
    public long getNumberOfProcessDataInstances(final long processInstanceId) throws InvalidSessionException, ProcessInstanceNotFoundException {
        try {
            return getNumberOfDataInstancesOfContainer(processInstanceId, DataInstanceContainer.PROCESS_INSTANCE);
        } catch (final SBonitaException e) {
            throw new ProcessInstanceNotFoundException(e);
        }
    }

    @Override
    @Deprecated
    public Privilege createAddPrivilege(final String scope, final String level) throws InvalidSessionException, PrivilegeInsertException,
            PrivilegeNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final PrivilegeService privilegeService = tenantAccessor.getPrivilegeService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final PrivilegeBuilder pbuild = tenantAccessor.getPrivilegeBuilders().getPrivilegeBuilder().createNewInstance().setLevel(level).setScope(scope);
        final SPrivilege privi = pbuild.done();
        Privilege p = null;
        final AddPrivilege addPrivilege = new AddPrivilege(privi, privilegeService);
        try {
            transactionExecutor.execute(addPrivilege);
        } catch (final SBonitaException e) {
            throw new PrivilegeInsertException(e);
        }
        final GetPrivilege getPrivilege = new GetPrivilege(scope, level, privilegeService);
        try {
            transactionExecutor.execute(getPrivilege);
            final SPrivilege sprivilege = getPrivilege.getResult();
            if (sprivilege != null) {
                p = ModelConvertor.toPrivilege(sprivilege);
            }
        } catch (final SBonitaException e) {
            throw new PrivilegeNotFoundException(e);
        }
        return p;
    }

    @Override
    @Deprecated
    public ActorPrivilege createAddActorPrivilege(final long actorId, final long privilegeId, final int type) throws InvalidSessionException,
            ActorPrivilegeInsertException, ActorPrivilegeNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActorPrivilegeService privilegeService = tenantAccessor.getActorPrivilegeService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        ActorPrivilege p = null;
        final GrantAllPrivilegesToActor grantAllPrivilegesToActor = new GrantAllPrivilegesToActor(privilegeService, Arrays.asList(actorId),
                Arrays.asList(privilegeId), type);
        try {
            transactionExecutor.execute(grantAllPrivilegesToActor);
        } catch (final SBonitaException e) {
            throw new ActorPrivilegeInsertException(e);
        }
        final GetActorPrivilege getActorPrivilege = new GetActorPrivilege(actorId, privilegeId, privilegeService);
        try {
            transactionExecutor.execute(getActorPrivilege);
            final SActorPrivilege sactorprivilege = getActorPrivilege.getResult();
            if (sactorprivilege != null) {
                p = ModelConvertor.toActorPrivilege(sactorprivilege);
            }
        } catch (final SBonitaException e) {
            throw new ActorPrivilegeNotFoundException(e);
        }
        return p;
    }

    @Override
    public void removeActorPrivileges(final List<Long> actorPrivilegeIds) throws InvalidSessionException, ActorPrivilegeRemoveException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActorPrivilegeService actorprivilegeService = tenantAccessor.getActorPrivilegeService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        for (final Long actorPrivilegeId : actorPrivilegeIds) {
            final RemoveActorPrivilegeById removeActorPrivilegeById = new RemoveActorPrivilegeById(actorPrivilegeId, actorprivilegeService);
            try {
                transactionExecutor.execute(removeActorPrivilegeById);
            } catch (final SBonitaException e) {
                throw new ActorPrivilegeRemoveException(actorPrivilegeId, e);
            }
        }
    }

    @Override
    @Deprecated
    public void removePrivileges(final List<Long> privilegeIds) throws InvalidSessionException, PrivilegeRemoveException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final PrivilegeService privilegeService = tenantAccessor.getPrivilegeService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        for (final Long privilegeId : privilegeIds) {
            final RemovePrivilege RemovePrivilege = new RemovePrivilege(privilegeId, privilegeService);
            try {
                transactionExecutor.execute(RemovePrivilege);
            } catch (final SBonitaException e) {
                throw new PrivilegeRemoveException(privilegeId, e);
            }
        }

    }

    private SActivityInstance getActivityInstance(final TenantServiceAccessor tenantAccessor, final long activityInstanceId)
            throws ActivityInstanceNotFoundException {
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final GetActivityInstance getActivityInstance = new GetActivityInstance(activityInstanceService, activityInstanceId);
        try {
            transactionExecutor.execute(getActivityInstance);
        } catch (final SBonitaException e) {
            throw new ActivityInstanceNotFoundException(activityInstanceId);
        }
        return getActivityInstance.getResult();
    }

    private SProcessInstance getProcessInstance(final TenantServiceAccessor tenantAccessor, final long processInstanceId)
            throws ProcessInstanceNotFoundException {
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final GetProcessInstance getProcessInstance = new GetProcessInstance(processInstanceService, processInstanceId);
        try {
            transactionExecutor.execute(getProcessInstance);
        } catch (final SBonitaException e) {
            throw new ProcessInstanceNotFoundException(processInstanceId);
        }
        return getProcessInstance.getResult();
    }

    private ClassLoader getLocalClassLoader(final TenantServiceAccessor tenantAccessor, final long processDefinitionId) throws ClassLoaderException {
        final ClassLoaderService classLoaderService = tenantAccessor.getClassLoaderService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final GetLocalClassLoader getLocalClassLoader = new GetLocalClassLoader(classLoaderService, "process", processDefinitionId);
        try {
            transactionExecutor.execute(getLocalClassLoader);
        } catch (final SBonitaException e) {
            throw new ClassLoaderException("Get process default classloader failed.", e);
        }
        return getLocalClassLoader.getResult();
    }

    private Map<String, Serializable> executeConnector(final TenantServiceAccessor tenantAccessor, final long processDefinitionId,
            final String connectorDefinitionId, final String connectorDefinitionVersion, final Map<String, SExpression> connectorsExps,
            final Map<String, Map<String, Serializable>> inputValues, final ClassLoader clazzLoader, final SExpressionContext expcontext)
            throws ConnectorException {
        final ConnectorService connectService = tenantAccessor.getConnectorService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ExecuteConnector executeConnector = new ExecuteConnector(connectService, processDefinitionId, connectorDefinitionId, connectorDefinitionVersion,
                connectorsExps, inputValues, clazzLoader, expcontext);
        try {
            transactionExecutor.execute(executeConnector);
        } catch (final SBonitaException e) {
            throw new ConnectorException("Execution of connector " + connectorDefinitionId + " failed.", e);
        }
        return executeConnector.getResult();
    }

    @Override
    public Map<String, Serializable> executeConnectorOnActivityInstance(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final long activityInstanceId)
            throws InvalidSessionException, ActivityInstanceNotFoundException, ProcessInstanceNotFoundException, ClassLoaderException, ConnectorException,
            InvalidEvaluationConnectorCondition {
        final String containerType = "ACTIVITY_INSTANCE";
        if (connectorInputParameters.size() == inputValues.size()) {
            final TenantServiceAccessor tenantAccessor = getTenantAccessor();
            final SActivityInstance activity = getActivityInstance(tenantAccessor, activityInstanceId);
            final SProcessInstance processInstance = getProcessInstance(tenantAccessor, activity.getRootContainerId());
            final long processDefinitionId = processInstance.getProcessDefinitionId();
            final ClassLoader clazzLoader = getLocalClassLoader(tenantAccessor, processDefinitionId);
            final SExpressionBuilders sExpressionBuilders = tenantAccessor.getSExpressionBuilders();
            final Map<String, SExpression> connectorsExps = ModelConvertor.constructExpressions(sExpressionBuilders, connectorInputParameters);
            final SExpressionContext expcontext = new SExpressionContext();
            expcontext.setContainerId(activityInstanceId);
            expcontext.setContainerType(containerType);
            expcontext.setProcessDefinitionId(processDefinitionId);
            return executeConnector(tenantAccessor, processDefinitionId, connectorDefinitionId, connectorDefinitionVersion, connectorsExps, inputValues,
                    clazzLoader, expcontext);
        } else {
            throw new InvalidEvaluationConnectorCondition(connectorInputParameters.size(), inputValues.size());
        }
    }

    @Override
    public Map<String, Serializable> executeConnectorOnProcessDefinition(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final long processDefinitionId)
            throws InvalidSessionException, ClassLoaderException, ConnectorException, InvalidEvaluationConnectorCondition, InvalidProcessDefinitionException {
        if (connectorInputParameters.size() == inputValues.size()) {
            final TenantServiceAccessor tenantAccessor = getTenantAccessor();
            final ClassLoader classLoader = getLocalClassLoader(tenantAccessor, processDefinitionId);
            final SExpressionBuilders sExpressionBuilders = tenantAccessor.getSExpressionBuilders();
            final Map<String, SExpression> connectorsExps = ModelConvertor.constructExpressions(sExpressionBuilders, connectorInputParameters);
            final SExpressionContext expcontext = new SExpressionContext();
            expcontext.setProcessDefinitionId(processDefinitionId);
            final SProcessDefinition processDef = getProcessDefinition(tenantAccessor, processDefinitionId);
            if (processDef != null) {
                expcontext.setProcessDefinition(processDef);
            }
            return executeConnector(tenantAccessor, processDefinitionId, connectorDefinitionId, connectorDefinitionVersion, connectorsExps, inputValues,
                    classLoader, expcontext);
        } else {
            throw new InvalidEvaluationConnectorCondition(connectorInputParameters.size(), inputValues.size());
        }
    }

    private SProcessDefinition getProcessDefinition(final TenantServiceAccessor tenantAccessor, final long processDefinitionId)
            throws InvalidProcessDefinitionException {
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final GetProcessDefinition getProcessDefinition = new GetProcessDefinition(processDefinitionId, processDefinitionService);
        try {
            transactionExecutor.execute(getProcessDefinition);
        } catch (final SBonitaException e) {
            throw new InvalidProcessDefinitionException("invalid processDefinition with id:" + processDefinitionId);
        }
        return getProcessDefinition.getResult();
    }

    @Override
    public Map<String, Serializable> executeConnectorOnProcessInstance(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final long processInstanceId)
            throws ClassLoaderException, InvalidSessionException, ProcessInstanceNotFoundException, ConnectorException, InvalidEvaluationConnectorCondition {
        final String containerType = "PROCESS_INSTANCE";
        if (connectorInputParameters.size() == inputValues.size()) {
            final TenantServiceAccessor tenantAccessor = getTenantAccessor();
            final SProcessInstance processInstance = getProcessInstance(tenantAccessor, processInstanceId);
            final long processDefinitionId = processInstance.getProcessDefinitionId();
            final ClassLoader clazzLoader = getLocalClassLoader(tenantAccessor, processDefinitionId);
            final SExpressionBuilders sExpressionBuilders = tenantAccessor.getSExpressionBuilders();
            final Map<String, SExpression> connectorsExps = ModelConvertor.constructExpressions(sExpressionBuilders, connectorInputParameters);
            final SExpressionContext expcontext = new SExpressionContext();
            expcontext.setContainerId(processInstanceId);
            expcontext.setContainerType(containerType);
            expcontext.setProcessDefinitionId(processDefinitionId);
            return executeConnector(tenantAccessor, processDefinitionId, connectorDefinitionId, connectorDefinitionVersion, connectorsExps, inputValues,
                    clazzLoader, expcontext);
        } else {
            throw new InvalidEvaluationConnectorCondition(connectorInputParameters.size(), inputValues.size());
        }
    }

    private SAActivityInstance getArchivedActivityInstance(final TenantServiceAccessor tenantAccessor, final long activityInstanceId)
            throws ArchivedActivityInstanceNotFoundException {
        final ActivityInstanceService activityService = tenantAccessor.getActivityInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final GetArchivedActivityInstance getArchivedActivityInstance = new GetArchivedActivityInstance(activityService, activityInstanceId, persistenceService);
        try {
            transactionExecutor.execute(getArchivedActivityInstance);
        } catch (final SBonitaException e) {
            throw new ArchivedActivityInstanceNotFoundException(activityInstanceId);
        }
        return getArchivedActivityInstance.getResult();
    }

    @Override
    public Map<String, Serializable> executeConnectorOnCompletedActivityInstance(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final long activityInstanceId)
            throws InvalidSessionException, ArchivedActivityInstanceNotFoundException, ProcessInstanceNotFoundException, ClassLoaderException,
            ConnectorException, InvalidEvaluationConnectorCondition {
        final String containerType = "ACTIVITY_INSTANCE";
        if (connectorInputParameters.size() == inputValues.size()) {
            final TenantServiceAccessor tenantAccessor = getTenantAccessor();
            final SAActivityInstance aactivityInstance = getArchivedActivityInstance(tenantAccessor, activityInstanceId);
            final long processInstanceId = aactivityInstance.getRootContainerId();
            final SProcessInstance processInstance = getProcessInstance(tenantAccessor, processInstanceId);
            final long processDefinitionId = processInstance.getProcessDefinitionId();
            final ClassLoader clazzLoader = getLocalClassLoader(tenantAccessor, processDefinitionId);
            final SExpressionBuilders sExpressionBuilders = tenantAccessor.getSExpressionBuilders();
            final Map<String, SExpression> connectorsExps = ModelConvertor.constructExpressions(sExpressionBuilders, connectorInputParameters);
            final SExpressionContext expcontext = new SExpressionContext();
            expcontext.setContainerId(activityInstanceId);
            expcontext.setContainerType(containerType);
            expcontext.setProcessDefinitionId(processDefinitionId);
            expcontext.setTime(aactivityInstance.getArchiveDate() + 500);
            return executeConnector(tenantAccessor, processDefinitionId, connectorDefinitionId, connectorDefinitionVersion, connectorsExps, inputValues,
                    clazzLoader, expcontext);
        } else {
            throw new InvalidEvaluationConnectorCondition(connectorInputParameters.size(), inputValues.size());
        }
    }

    private SAProcessInstance getArchivedProcessInstance(final TenantServiceAccessor tenantAccessor, final long processInstanceId)
            throws ArchivedProcessInstanceNotFoundException {
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final SAProcessInstanceBuilder saProcessInstanceBuilder = tenantAccessor.getBPMInstanceBuilders().getSAProcessInstanceBuilder();
        final GetLatestArchivedProcessInstance getArchivedProcessInstance = new GetLatestArchivedProcessInstance(processInstanceService, processInstanceId,
                persistenceService, saProcessInstanceBuilder);
        try {
            transactionExecutor.execute(getArchivedProcessInstance);
        } catch (final SBonitaException e) {
            throw new ArchivedProcessInstanceNotFoundException(processInstanceId);
        }
        return getArchivedProcessInstance.getResult();
    }

    @Override
    public Map<String, Serializable> executeConnectorOnCompletedProcessInstance(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final long processInstanceId)
            throws InvalidSessionException, ArchivedProcessInstanceNotFoundException, ClassLoaderException, ConnectorException,
            InvalidEvaluationConnectorCondition {
        final String containerType = "PROCESS_INSTANCE";
        if (connectorInputParameters.size() == inputValues.size()) {
            final TenantServiceAccessor tenantAccessor = getTenantAccessor();
            final SAProcessInstance saprocessInstance = getArchivedProcessInstance(tenantAccessor, processInstanceId);
            final long processDefinitionId = saprocessInstance.getProcessDefinitionId();
            final ClassLoader clazzLoader = getLocalClassLoader(tenantAccessor, processDefinitionId);
            final SExpressionBuilders sExpressionBuilders = tenantAccessor.getSExpressionBuilders();
            final Map<String, SExpression> connectorsExps = ModelConvertor.constructExpressions(sExpressionBuilders, connectorInputParameters);
            final SExpressionContext expcontext = new SExpressionContext();
            expcontext.setContainerId(processInstanceId);
            expcontext.setContainerType(containerType);
            expcontext.setProcessDefinitionId(processDefinitionId);
            expcontext.setTime(saprocessInstance.getArchiveDate() + 500);
            return executeConnector(tenantAccessor, processDefinitionId, connectorDefinitionId, connectorDefinitionVersion, connectorsExps, inputValues,
                    clazzLoader, expcontext);
        } else {
            throw new InvalidEvaluationConnectorCondition(connectorInputParameters.size(), inputValues.size());
        }
    }

    @Override
    public Map<String, Serializable> executeConnectorAtProcessInstanciation(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final long processInstanceId)
            throws InvalidSessionException, ArchivedProcessInstanceNotFoundException, ClassLoaderException, ConnectorException,
            InvalidEvaluationConnectorCondition {
        final String containerType = "PROCESS_INSTANCE";
        if (connectorInputParameters.size() == inputValues.size()) {
            final TenantServiceAccessor tenantAccessor = getTenantAccessor();
            final SAProcessInstance saprocessInstance = getArchivedProcessInstance(tenantAccessor, processInstanceId);
            final long processDefinitionId = saprocessInstance.getProcessDefinitionId();
            final ClassLoader clazzLoader = getLocalClassLoader(tenantAccessor, processDefinitionId);
            final SExpressionBuilders sExpressionBuilders = tenantAccessor.getSExpressionBuilders();
            final Map<String, SExpression> connectorsExps = ModelConvertor.constructExpressions(sExpressionBuilders, connectorInputParameters);
            final SExpressionContext expcontext = new SExpressionContext();
            expcontext.setContainerId(processInstanceId);
            expcontext.setContainerType(containerType);
            expcontext.setProcessDefinitionId(processDefinitionId);
            // should use getStartDate() but not implemented
            expcontext.setTime(saprocessInstance.getArchiveDate() + 500);
            return executeConnector(tenantAccessor, processDefinitionId, connectorDefinitionId, connectorDefinitionVersion, connectorsExps, inputValues,
                    clazzLoader, expcontext);
        } else {
            throw new InvalidEvaluationConnectorCondition(connectorInputParameters.size(), inputValues.size());
        }
    }

    @Override
    public void setStateByStateName(final long activityInstanceId, final String state) throws InvalidSessionException, UserTaskNotFoundException,
            ActivityExecutionFailedException {
        setStateByStateId(activityInstanceId, ModelConvertor.getServerActivityStateId(state));
    }

    @Override
    public void setStateByStateId(final long activityInstanceId, final int stateId) throws InvalidSessionException, UserTaskNotFoundException,
            ActivityExecutionFailedException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final FlowNodeExecutor flowNodeExecutor = tenantAccessor.getFlowNodeExecutor();
        try {
            final GetActivityInstance getActivityInstance = new GetActivityInstance(activityInstanceService, activityInstanceId);
            transactionExecutor.execute(getActivityInstance);
            final SActivityInstance activityInstance = getActivityInstance.getResult();
            final GetProcessDefinition getProcessDefinition = new GetProcessDefinition(activityInstance.getLogicalGroup(0), processDefinitionService);
            transactionExecutor.execute(getProcessDefinition);
            // set state
            flowNodeExecutor.setStateByStateId(getProcessDefinition.getResult(), activityInstance, stateId);
        } catch (final SBonitaException e) {
            throw new UserTaskNotFoundException(e.getMessage());
        }
    }

    @Override
    public void setTaskPriority(final long humanTaskInstanceId, final TaskPriority priority) throws InvalidSessionException, UserTaskSetPriorityException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final SetTaskPriority transactionContent = new SetTaskPriority(activityInstanceService, humanTaskInstanceId, STaskPriority.valueOf(priority.name()));
            transactionExecutor.execute(transactionContent);
        } catch (final SBonitaException e) {
            throw new UserTaskSetPriorityException(e);
        }
    }

    @Override
    public void deleteProcessInstances(final long processDefinitionId) throws InvalidSessionException, ProcessDefinitionNotFoundException,
            ProcessDeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final DeleteProcessInstances transactionContent = new DeleteProcessInstances(processInstanceService, tenantAccessor.getCommentService(),
                    processDefinitionId, tenantAccessor.getArchiveService());
            transactionExecutor.execute(transactionContent);
        } catch (final SBonitaException e) {
            throw new ProcessDeletionException(e);
        }
    }

    @Override
    public void deleteProcessInstance(final long processInstanceId) throws InvalidSessionException, ProcessInstanceNotFoundException,
            ProcessInstanceDeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final DeleteProcessInstance transactionContent = new DeleteProcessInstance(processInstanceService, processInstanceId);
            transactionExecutor.execute(transactionContent);
        } catch (final SProcessInstanceNotFoundException e) {
            throw new ProcessInstanceNotFoundException(processInstanceId);
        } catch (final SBonitaException e) {
            throw new ProcessInstanceDeletionException(e);
        }
    }

    @Override
    public SearchResult<ProcessInstance> searchOpenProcessInstances(final SearchOptions searchOptions) throws InvalidSessionException,
            ProcessInstanceReadException, PageOutOfRangeException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ProcessInstanceStateManager processInstanceStateManager = tenantAccessor.getProcessInstanceStateManager();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SearchProcessInstances searchProcessInstances = new SearchProcessInstances(processInstanceService, processInstanceStateManager,
                searchEntitiesDescriptor.getProcessInstanceDescriptor(), searchOptions);
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            transactionExecutor.execute(searchProcessInstances);
            return searchProcessInstances.getResult();
        } catch (final SBonitaException sbe) {
            throw new BonitaRuntimeException(sbe);
        }
    }

    @Override
    public SearchResult<ProcessInstance> searchOpenProcessInstancesSupervisedBy(final long userId, final SearchOptions searchOptions)
            throws InvalidSessionException, UserNotFoundException, PageOutOfRangeException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final GetSUser getUser = new GetSUser(identityService, userId);
        try {
            transactionExecutor.execute(getUser);
            getUser.getResult();
        } catch (final SBonitaException e) {
            throw new UserNotFoundException(e);
        }
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ProcessInstanceStateManager processInstanceStateManager = tenantAccessor.getProcessInstanceStateManager();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SearchOpenProcessInstancesSupervisedBy searchOpenProcessInstances = new SearchOpenProcessInstancesSupervisedBy(processInstanceService,
                processInstanceStateManager, searchEntitiesDescriptor.getProcessInstanceDescriptor(), userId, searchOptions);
        try {
            transactionExecutor.execute(searchOpenProcessInstances);
            return searchOpenProcessInstances.getResult();
        } catch (final SBonitaException sbe) {
            throw new BonitaRuntimeException(sbe);
        }
    }

    @Override
    public SearchResult<ProcessDeploymentInfo> searchRecentlyStartedProcessDefinitions(final long userId, final SearchOptions searchOptions)
            throws InvalidSessionException, ProcessDefinitionReadException, PageOutOfRangeException, UserNotFoundException {
        SUser user = null;
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final GetSUser getUser = new GetSUser(identityService, userId);
        try {
            transactionExecutor.execute(getUser);
            user = getUser.getResult();
        } catch (final SBonitaException e) {
            throw new UserNotFoundException(e);
        }
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SearchProcessDefinitionsDescriptor searchDescriptor = searchEntitiesDescriptor.getSearchProcessDefinitionsDescriptor();
        final SearchProceDefWithRecentProceInstancesStarted searcher = new SearchProceDefWithRecentProceInstancesStarted(processDefinitionService,
                searchDescriptor, user.getId(), searchOptions);
        try {
            transactionExecutor.execute(searcher);
        } catch (final SBonitaException e) {
            throw new ProcessDefinitionReadException("Can't get processDefinition executing searchRecentlyStartedProcessDefinitions method with userid "
                    + userId + ".");
        }
        return searcher.getResult();
    }

    @Override
    public SearchResult<ProcessDeploymentInfo> searchProcessDefinitions(final SearchOptions searchOptions) throws InvalidSessionException,
            ProcessDefinitionReadException, PageOutOfRangeException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SearchProcessDefinitionsDescriptor searchDescriptor = searchEntitiesDescriptor.getSearchProcessDefinitionsDescriptor();
        final SearchProcessDefinitions transactionSearch = new SearchProcessDefinitions(processDefinitionService, searchDescriptor, searchOptions);
        try {
            transactionExecutor.execute(transactionSearch);
        } catch (final SBonitaException e) {
            throw new ProcessDefinitionReadException("Can't get processDefinition's executing searchProcessDefinitions() due to " + e.getMessage());
        }
        return transactionSearch.getResult();
    }

    @Override
    public SearchResult<ProcessDeploymentInfo> searchProcessDefinitions(final long userId, final SearchOptions searchOptions) throws InvalidSessionException,
            ProcessDefinitionReadException, PageOutOfRangeException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SearchProcessDefinitionsDescriptor searchDescriptor = searchEntitiesDescriptor.getSearchProcessDefinitionsDescriptor();
        final SearchProcessDefinitionsUserCanStart transactionSearch = new SearchProcessDefinitionsUserCanStart(processDefinitionService, searchDescriptor,
                searchOptions, userId);
        try {
            transactionExecutor.execute(transactionSearch);
        } catch (final SBonitaException e) {
            throw new ProcessDefinitionReadException("Error while retrieving process definitions: " + e.getMessage());
        }
        return transactionSearch.getResult();

    }

    @Override
    public SearchResult<ProcessDeploymentInfo> searchProcessDefinitionsSupervisedBy(final long userId, final SearchOptions searchOptions)
            throws InvalidSessionException, SearchException, PageOutOfRangeException {
        final TenantServiceAccessor serviceAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = serviceAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = serviceAccessor.getProcessDefinitionService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = serviceAccessor.getSearchEntitiesDescriptor();
        final SearchProcessDefinitionsDescriptor searchDescriptor = searchEntitiesDescriptor.getSearchProcessDefinitionsDescriptor();
        final SearchProcessDefinitionsSupervised searcher = new SearchProcessDefinitionsSupervised(processDefinitionService, searchDescriptor, searchOptions,
                userId);
        try {
            transactionExecutor.execute(searcher);
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
        final SearchResult<ProcessDeploymentInfo> proceDefs = searcher.getResult();
        return proceDefs;

    }

    @Override
    public SearchResult<HumanTaskInstance> searchAssignedTasksSupervisedBy(final long supervisorId, final SearchOptions searchOptions)
            throws InvalidSessionException, SearchException, PageOutOfRangeException {
        final TenantServiceAccessor serviceAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = serviceAccessor.getTransactionExecutor();
        final FlowNodeStateManager flowNodeStateManager = serviceAccessor.getFlowNodeStateManager();
        final ActivityInstanceService activityInstanceService = serviceAccessor.getActivityInstanceService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = serviceAccessor.getSearchEntitiesDescriptor();
        final SearchAssignedTasksSupervisedByTransaction searchedTasksTransaction = new SearchAssignedTasksSupervisedByTransaction(supervisorId,
                activityInstanceService, flowNodeStateManager, searchEntitiesDescriptor.getHumanTaskInstanceDescriptor(), searchOptions);
        try {
            transactionExecutor.execute(searchedTasksTransaction);
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
        return searchedTasksTransaction.getResult();

    }

    @Override
    public SearchResult<ArchivedHumanTaskInstance> searchArchivedTasksSupervisedBy(final long supervisorId, final SearchOptions searchOptions)
            throws InvalidSessionException, SearchException, PageOutOfRangeException {
        final TenantServiceAccessor serviceAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = serviceAccessor.getTransactionExecutor();
        final FlowNodeStateManager flowNodeStateManager = serviceAccessor.getFlowNodeStateManager();
        final ActivityInstanceService activityInstanceService = serviceAccessor.getActivityInstanceService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = serviceAccessor.getSearchEntitiesDescriptor();
        final SearchArchivedTasksSupervisedByTransaction searchedTasksTransaction = new SearchArchivedTasksSupervisedByTransaction(supervisorId,
                activityInstanceService, flowNodeStateManager, searchEntitiesDescriptor.getArchivedHumanTaskInstanceDescriptor(), searchOptions);

        try {
            transactionExecutor.execute(searchedTasksTransaction);
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }

        return searchedTasksTransaction.getResult();

    }

    @Override
    public SearchResult<ProcessSupervisor> searchProcessSupervisors(final MemberType memberType, final SearchOptions searchOptions)
            throws InvalidSessionException, SearchException, PageOutOfRangeException {
        final TenantServiceAccessor serviceAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = serviceAccessor.getTransactionExecutor();
        final SupervisorMappingService supervisorService = serviceAccessor.getSupervisorService();
        final SSupervisorBuilders supervisorBuilders = serviceAccessor.getSSupervisorBuilders();
        final IdentityModelBuilder identityModelBuilder = serviceAccessor.getIdentityModelBuilder();
        SearchEntityDescriptor searchDescriptor = null;
        String suffix = null;
        switch (memberType) {
            case USER:
                searchDescriptor = new SearchProcessSupervisorUserDescriptor(supervisorBuilders, identityModelBuilder);
                suffix = "ForUser";
                break;

            case GROUP:
                searchDescriptor = new SearchProcessSupervisorGroupDescriptor(supervisorBuilders, identityModelBuilder);
                suffix = "ForGroup";
                break;

            case ROLE:
                searchDescriptor = new SearchProcessSupervisorRoleDescriptor(supervisorBuilders, identityModelBuilder);
                suffix = "ForRole";
                break;

            case MEMBERSHIP:
                searchDescriptor = new SearchProcessSupervisorRoleAndGroupDescriptor(supervisorBuilders, identityModelBuilder);
                suffix = "ForRoleAndGroup";
                break;

        }
        final SearchSupervisorsTransaction searchSupervisorsTransaction = new SearchSupervisorsTransaction(supervisorService, searchDescriptor, searchOptions,
                suffix);
        try {
            transactionExecutor.execute(searchSupervisorsTransaction);
            return searchSupervisorsTransaction.getResult();
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
    }

    @Override
    public ProcessSupervisor getSupervisor(final long supervisorId, final MemberType memberType) throws InvalidSessionException, ObjectReadException,
            ObjectNotFoundException {
        final TenantServiceAccessor serviceAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = serviceAccessor.getTransactionExecutor();
        try {
            transactionExecutor.openTransaction();
            try {

                final SupervisorMappingService supervisorService = serviceAccessor.getSupervisorService();
                SSupervisor supervisor = null;
                switch (memberType) {
                    case USER:
                        supervisor = supervisorService.getSupervisorForUser(supervisorId);
                        break;

                    case GROUP:
                        supervisor = supervisorService.getSupervisorForGroup(supervisorId);
                        break;

                    case ROLE:
                        supervisor = supervisorService.getSupervisorForRole(supervisorId);
                        break;

                    case MEMBERSHIP:
                        supervisor = supervisorService.getSupervisorForRoleAndGroup(supervisorId);// this method should throw a read exception
                        break;
                }
                return ModelConvertor.toProcessSupervisor(supervisor);
            } catch (final SSupervisorNotFoundException e) {
                throw new ObjectNotFoundException("supervisor with type " + memberType + " and supervisor id " + supervisorId + " not found");
            } finally {
                transactionExecutor.completeTransaction();
            }
        } catch (final STransactionException e) {
            throw new ObjectReadException(e);
        }
    }

    @Override
    public boolean isUserProcessSupervisor(final long processDefinitionId, final long userId) throws InvalidSessionException, ObjectReadException {
        final TenantServiceAccessor serviceAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = serviceAccessor.getTransactionExecutor();
        final SupervisorMappingService supervisorService = serviceAccessor.getSupervisorService();
        try {
            transactionExecutor.openTransaction();
            try {
                return supervisorService.isProcessSupervisor(processDefinitionId, userId);
            } catch (final SBonitaReadException e) {
                transactionExecutor.setTransactionRollback();
                throw new ObjectReadException(e);
            } finally {
                transactionExecutor.completeTransaction();
            }
        } catch (final STransactionException e) {
            throw new ObjectReadException(e);
        }
    }

    @Override
    public void deleteSupervisor(final long id) throws InvalidSessionException, ObjectNotFoundException, ObjectDeletionException {
        final TenantServiceAccessor serviceAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = serviceAccessor.getTransactionExecutor();
        final SupervisorMappingService supervisorService = serviceAccessor.getSupervisorService();
        try {
            transactionExecutor.openTransaction();
            try {
                supervisorService.deleteSupervisor(id);
            } catch (final SSupervisorNotFoundException e) {
                transactionExecutor.setTransactionRollback();
                throw new ObjectNotFoundException("supervisor not found with id " + id);
            } catch (final SSupervisorDeletionException e) {
                transactionExecutor.setTransactionRollback();
                throw new ObjectDeletionException(e);
            } finally {
                transactionExecutor.completeTransaction();
            }
        } catch (final STransactionException e) {
            throw new ObjectDeletionException(e);
        }
    }

    @Override
    public ProcessSupervisor createProcessSupervisorForUser(final long processDefinitionId, final long userId) throws InvalidSessionException,
            ObjectCreationException, ObjectNotFoundException {
        return createSupervisor(processDefinitionId, userId, null, null, MemberType.USER);
    }

    @Override
    public ProcessSupervisor createProcessSupervisorForRole(final long processDefinitionId, final long roleId) throws InvalidSessionException,
            ObjectCreationException, ObjectNotFoundException {
        return createSupervisor(processDefinitionId, null, null, roleId, MemberType.ROLE);
    }

    @Override
    public ProcessSupervisor createProcessSupervisorForGroup(final long processDefinitionId, final long groupId) throws InvalidSessionException,
            ObjectCreationException, ObjectNotFoundException {
        return createSupervisor(processDefinitionId, null, groupId, null, MemberType.GROUP);
    }

    @Override
    public ProcessSupervisor createProcessSupervisorForMembership(final long processDefinitionId, final long groupId, final long roleId)
            throws InvalidSessionException, ObjectCreationException, ObjectNotFoundException {
        return createSupervisor(processDefinitionId, null, groupId, roleId, MemberType.MEMBERSHIP);
    }

    private ProcessSupervisor createSupervisor(final long processDefinitionId, final Long userId, final Long groupId, final Long roleId,
            final MemberType memberType) throws InvalidSessionException, ObjectCreationException, ObjectNotFoundException {
        SSupervisor supervisor = null;
        final TenantServiceAccessor serviceAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = serviceAccessor.getTransactionExecutor();
        final SupervisorMappingService supervisorService = serviceAccessor.getSupervisorService();
        final IdentityService identityService = serviceAccessor.getIdentityService();
        final SSupervisorBuilder supervisorBuilder = serviceAccessor.getSSupervisorBuilders().getSSupervisorBuilder();
        try {
            try {
                transactionExecutor.openTransaction();
                supervisorBuilder.createNewInstance(processDefinitionId);
                switch (memberType) {
                    case USER:
                        final SUser user = identityService.getUser(userId);
                        supervisorBuilder.setUserId(userId);
                        supervisorBuilder.setDisplayNamePart1(user.getFirstName());
                        supervisorBuilder.setDisplayNamePart2(user.getLastName());
                        supervisorBuilder.setDisplayNamePart3(user.getUserName());
                        break;

                    case GROUP:
                        SGroup group = identityService.getGroup(groupId);
                        supervisorBuilder.setGroupId(groupId);
                        supervisorBuilder.setDisplayNamePart1(group.getName());
                        supervisorBuilder.setDisplayNamePart2(group.getParentPath());
                        supervisorBuilder.setDisplayNamePart3("");
                        break;

                    case ROLE:
                        SRole role = identityService.getRole(roleId);
                        supervisorBuilder.setRoleId(roleId);
                        supervisorBuilder.setDisplayNamePart1(role.getName());
                        supervisorBuilder.setDisplayNamePart2("");
                        supervisorBuilder.setDisplayNamePart3("");
                        break;

                    case MEMBERSHIP:
                        group = identityService.getGroup(groupId);
                        role = identityService.getRole(roleId);
                        supervisorBuilder.setGroupId(groupId);
                        supervisorBuilder.setRoleId(roleId);
                        supervisorBuilder.setDisplayNamePart1(role.getName());
                        supervisorBuilder.setDisplayNamePart2(group.getName());
                        supervisorBuilder.setDisplayNamePart3(group.getParentPath());
                        break;

                }

                supervisor = supervisorBuilder.done();
                supervisor = supervisorService.createSupervisor(supervisor);
                return ModelConvertor.toProcessSupervisor(supervisor);
            } catch (final SSupervisorAlreadyExistsException e) {
                transactionExecutor.setTransactionRollback();
                throw new ObjectAlreadyExistsException(supervisor);
            } catch (final SSupervisorCreationException e) {
                transactionExecutor.setTransactionRollback();
                throw new ObjectCreationException(e);
            } catch (final SGroupNotFoundException e) {
                transactionExecutor.setTransactionRollback();
                throw new ObjectNotFoundException("group not found with id: " + groupId);
            } catch (final SRoleNotFoundException e) {
                transactionExecutor.setTransactionRollback();
                throw new ObjectNotFoundException("role not found with id: " + roleId);
            } catch (final SUserNotFoundException e) {
                transactionExecutor.setTransactionRollback();
                throw new ObjectNotFoundException("user not found with id: " + userId);
            } finally {
                transactionExecutor.completeTransaction();
            }
        } catch (final STransactionException e) {
            throw new ObjectCreationException(e);
        }
    }

    @Override
    public SearchResult<ProcessDeploymentInfo> searchUncategorizedProcessDefinitionsUserCanStart(final long userId, final SearchOptions searchOptions)
            throws InvalidSessionException, ProcessDefinitionReadException, PageOutOfRangeException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SearchProcessDefinitionsDescriptor searchDescriptor = searchEntitiesDescriptor.getSearchProcessDefinitionsDescriptor();
        final SearchUncategorizedProcessDefinitionsUserCanStart transactionSearch = new SearchUncategorizedProcessDefinitionsUserCanStart(
                processDefinitionService, searchDescriptor, searchOptions, userId);
        try {
            transactionExecutor.execute(transactionSearch);
        } catch (final SBonitaException e) {
            throw new ProcessDefinitionReadException("Error while retrieving process definitions: " + e.getMessage());
        }
        return transactionSearch.getResult();
    }

    @Override
    public SearchResult<Privilege> searchPrivileges(final SearchOptions searchOptions) throws InvalidSessionException, PageOutOfRangeException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final PrivilegeService privilegeService = tenantAccessor.getPrivilegeService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SearchPrivileges searchPrivileges = new SearchPrivileges(privilegeService, searchEntitiesDescriptor.getPrivilegeDescriptor(), searchOptions);
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            transactionExecutor.execute(searchPrivileges);
            return searchPrivileges.getResult();
        } catch (final SBonitaException sbe) {
            throw new BonitaRuntimeException(sbe);
        }
    }

    @Override
    public SearchResult<ActorPrivilege> searchActorPrivileges(final SearchOptions searchOptions) throws InvalidSessionException, ActorNotFoundException,
            PageOutOfRangeException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActorPrivilegeService actorPrivilegeService = tenantAccessor.getActorPrivilegeService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SearchActorPrivileges searchActorPrivileges = new SearchActorPrivileges(actorPrivilegeService,
                searchEntitiesDescriptor.getActorPrivilegeDescriptor(), searchOptions);
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            transactionExecutor.execute(searchActorPrivileges);
            return searchActorPrivileges.getResult();
        } catch (final SBonitaException sbe) {
            throw new BonitaRuntimeException(sbe);
        }
    }

    @Override
    public ActorPrivilege getActorPrivilege(final long actorPrivilegeId) throws InvalidSessionException, ActorPrivilegeNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActorPrivilegeService actorPrivilegeService = tenantAccessor.getActorPrivilegeService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final GetActorPrivilegeById GetActorPrivilegeById = new GetActorPrivilegeById(actorPrivilegeId, actorPrivilegeService);
        try {
            transactionExecutor.execute(GetActorPrivilegeById);
        } catch (final SBonitaException e) {
            throw new ActorPrivilegeNotFoundException(actorPrivilegeId, e);
        }
        ActorPrivilege actorPri = null;
        if (GetActorPrivilegeById.getResult() != null) {
            actorPri = ModelConvertor.toActorPrivilege(GetActorPrivilegeById.getResult());
        }
        return actorPri;
    }

    @Override
    public void updateActorPrivilege(final Map<Long, String> map) throws InvalidSessionException, ActorPrivilegeNotFoundException,
            ActorPrivilegeUpdateException {
        if (map != null && !map.isEmpty()) {
            final TenantServiceAccessor tenantAccessor = getTenantAccessor();
            final ActorPrivilegeService actorPrivilegeService = tenantAccessor.getActorPrivilegeService();
            final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();

            for (final Entry<Long, String> m : map.entrySet()) {
                final EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
                entityUpdateDescriptor.addField("levelValue", m.getValue());
                final Map<Long, EntityUpdateDescriptor> updateMap = new HashMap<Long, EntityUpdateDescriptor>();
                updateMap.put(m.getKey(), entityUpdateDescriptor);
                final UpdateActorPrivilege updateActorPrivilege = new UpdateActorPrivilege(actorPrivilegeService, updateMap);
                try {
                    transactionExecutor.execute(updateActorPrivilege);
                } catch (final SBonitaException e) {
                    throw new ActorPrivilegeUpdateException("Update actorPrivilege " + m.getKey() + "with " + m.getValue() + " failed.", e);
                }
            }
        } else {
            throw new ActorPrivilegeUpdateException("The input map is empty when updating actorPrivilege.");
        }
    }

    @Override
    @Deprecated
    public boolean isAllowed(final long actorId, final long privilegeId, final LevelRight value) throws InvalidSessionException,
            ActorPrivilegeNotFoundException, PrivilegeNotFoundException, ActorNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActorPrivilegeService actorPrivilegeService = tenantAccessor.getActorPrivilegeService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final CheckActorPrivilege checkActorPrivilege = new CheckActorPrivilege(actorId, privilegeId, value.toString(), actorPrivilegeService);
        try {
            transactionExecutor.execute(checkActorPrivilege);
        } catch (final SBonitaException e) {
            throw new ActorPrivilegeNotFoundException(actorId, privilegeId, value.toString(), e);
        }
        return checkActorPrivilege.getResult();
    }

    @Override
    public LevelRight getActorPrivilegeValue(final long actorId, final long privilegeId) throws InvalidSessionException, ActorPrivilegeNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActorPrivilegeService actorPrivilegeService = tenantAccessor.getActorPrivilegeService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final GetActorPrivilege getActorPrivilege = new GetActorPrivilege(actorId, privilegeId, actorPrivilegeService);
        try {
            transactionExecutor.execute(getActorPrivilege);
        } catch (final SBonitaException e) {
            throw new ActorPrivilegeNotFoundException("Can't find actorPrivilege using actorId " + actorId + " and  privilegeId " + privilegeId, e);
        }
        final SActorPrivilege actorPri = getActorPrivilege.getResult();
        return LevelRight.valueOf(actorPri.getLevelValue());
    }

    @Override
    public List<Privilege> getPrivileges(final List<Long> privilegeIds) throws InvalidSessionException, PrivilegeNotFoundException {
        final List<Privilege> privileges = new ArrayList<Privilege>();
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final PrivilegeService privilegeService = tenantAccessor.getPrivilegeService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        for (final Long priviId : privilegeIds) {
            final GetPrivilegeById getPrivilegeById = new GetPrivilegeById(priviId, privilegeService);
            try {
                transactionExecutor.execute(getPrivilegeById);
            } catch (final SBonitaException e) {
                throw new PrivilegeNotFoundException(priviId, e);
            }
            Privilege pri = null;
            if (getPrivilegeById.getResult() != null) {
                pri = ModelConvertor.toPrivilege(getPrivilegeById.getResult());
                privileges.add(pri);
            }
        }
        return privileges;
    }

    @Override
    public void grantAllDefaultToActors(final long processDefinitionId) throws InvalidSessionException, ActorNotFoundException, PrivilegeNotFoundException,
            ActorPrivilegeInsertException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final GetActorsByScopeId getActorsByScopeId = new GetActorsByScopeId(actorMappingService, processDefinitionId);
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            transactionExecutor.execute(getActorsByScopeId);
        } catch (final SBonitaException e) {
            throw new ActorNotFoundException("Can't find an actor in processDefinition: " + processDefinitionId, e);
        }
        final Set<SActor> actors = getActorsByScopeId.getResult();
        final List<Long> actorIds = new ArrayList<Long>();
        for (final SActor sActor : actors) {
            actorIds.add(sActor.getId());
        }
        final SearchOptions searchOptions = new SearchOptionsImpl(0, 10);
        SearchResult<Privilege> privileges = null;
        try {
            privileges = searchPrivileges(searchOptions);
        } catch (final PageOutOfRangeException e) {
            throw new PrivilegeNotFoundException("No privileges found.", e);
        }
        if (privileges.getCount() > 0) {
            final List<Long> allPriIds = new ArrayList<Long>();
            for (final Privilege pri : privileges.getResult()) {
                allPriIds.add(pri.getId());
            }
            final ActorPrivilegeService actorPrivilegeService = tenantAccessor.getActorPrivilegeService();
            final GrantAllPrivilegesToActor grantAllPrivilegesToActor = new GrantAllPrivilegesToActor(actorPrivilegeService, actorIds, allPriIds, 1);
            try {
                transactionExecutor.execute(grantAllPrivilegesToActor);
            } catch (final SBonitaException e) {
                throw new ActorPrivilegeInsertException("Granting all privileges failed.", e);
            }
        }
    }

    @Override
    public SearchResult<ProcessInstance> searchOpenProcessInstancesInvolvingUser(final long userId, final SearchOptions searchOptions)
            throws InvalidSessionException, UserNotFoundException, PageOutOfRangeException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final GetSUser getUser = new GetSUser(identityService, userId);
        try {
            transactionExecutor.execute(getUser);
            getUser.getResult();
        } catch (final SBonitaException e) {
            throw new UserNotFoundException(e);
        }
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ProcessInstanceStateManager processInstanceStateManager = tenantAccessor.getProcessInstanceStateManager();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SearchOpenProcessInstancesInvolvingUser searchOpenProcessInstances = new SearchOpenProcessInstancesInvolvingUser(processInstanceService,
                processInstanceStateManager, searchEntitiesDescriptor.getProcessInstanceDescriptor(), userId, searchOptions);
        try {
            transactionExecutor.execute(searchOpenProcessInstances);
            return searchOpenProcessInstances.getResult();
        } catch (final SBonitaException sbe) {
            throw new BonitaRuntimeException(sbe);
        }
    }

    @Override
    public SearchResult<ArchivedHumanTaskInstance> searchArchivedTasks(final SearchOptions searchOptions) throws InvalidSessionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();

        final SearchArchivedTasks searchArchivedTasks = new SearchArchivedTasks(activityInstanceService, flowNodeStateManager,
                searchEntitiesDescriptor.getArchivedHumanTaskInstanceDescriptor(), searchOptions, persistenceService);
        try {
            transactionExecutor.execute(searchArchivedTasks);
        } catch (final SBonitaException sbe) {
            throw new BonitaRuntimeException(sbe);
        }
        return searchArchivedTasks.getResult();
    }

    @Override
    public SearchResult<ArchivedProcessInstance> searchArchivedProcessInstances(final SearchOptions searchOptions) throws InvalidSessionException,
            ProcessInstanceReadException, PageOutOfRangeException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ProcessInstanceStateManager processInstanceStateManager = tenantAccessor.getProcessInstanceStateManager();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SearchArchivedProcessInstances searchArchivedProcessInstances = new SearchArchivedProcessInstances(processInstanceService,
                processInstanceStateManager, searchEntitiesDescriptor.getArchivedProcessInstanceDescriptor(), searchOptions, persistenceService);
        try {
            transactionExecutor.execute(searchArchivedProcessInstances);
        } catch (final SBonitaException e) {
            throw new BonitaRuntimeException(e);
        }
        return searchArchivedProcessInstances.getResult();
    }

    @Override
    public SearchResult<HumanTaskInstance> searchAssignedTasksManagedBy(final long managerUserId, final SearchOptions searchOptions)
            throws InvalidSessionException, UserNotFoundException, SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();

        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final SearchAssignedTaskManagedBy searchAssignedTaskManagedBy = new SearchAssignedTaskManagedBy(activityInstanceService, flowNodeStateManager,
                searchEntitiesDescriptor.getHumanTaskInstanceDescriptor(), managerUserId, searchOptions);
        try {
            transactionExecutor.execute(searchAssignedTaskManagedBy);
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
        return searchAssignedTaskManagedBy.getResult();
    }

    @Override
    public SearchResult<ArchivedProcessInstance> searchArchivedProcessInstancesSupervisedBy(final long userId, final SearchOptions searchOptions)
            throws InvalidSessionException, UserNotFoundException, PageOutOfRangeException {
        // check user
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final GetSUser getUser = new GetSUser(identityService, userId);
        try {
            transactionExecutor.execute(getUser);
            getUser.getResult();
        } catch (final SBonitaException e) {
            throw new UserNotFoundException(e);
        }
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ProcessInstanceStateManager processInstanceStateManager = tenantAccessor.getProcessInstanceStateManager();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final SearchArchivedProcessInstancesSupervisedBy searchArchivedProcessInstances = new SearchArchivedProcessInstancesSupervisedBy(userId,
                processInstanceService, processInstanceStateManager, searchEntitiesDescriptor.getArchivedProcessInstanceDescriptor(), searchOptions,
                persistenceService);
        try {
            transactionExecutor.execute(searchArchivedProcessInstances);
            return searchArchivedProcessInstances.getResult();
        } catch (final SBonitaException sbe) {
            throw new BonitaRuntimeException(sbe);
        }
    }

    @Override
    public SearchResult<ArchivedProcessInstance> searchArchivedProcessInstancesInvolvingUser(final long userId, final SearchOptions searchOptions)
            throws InvalidSessionException, UserNotFoundException, PageOutOfRangeException {
        // check user
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final GetSUser getUser = new GetSUser(identityService, userId);
        SUser user;
        try {
            transactionExecutor.execute(getUser);
            user = getUser.getResult();
        } catch (final SBonitaException e) {
            throw new UserNotFoundException(e);
        }
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ProcessInstanceStateManager processInstanceStateManager = tenantAccessor.getProcessInstanceStateManager();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final SearchArchivedProcessInstancesInvolvingUser searchArchivedProcessInstances = new SearchArchivedProcessInstancesInvolvingUser(user,
                processInstanceService, processInstanceStateManager, searchEntitiesDescriptor.getArchivedProcessInstanceDescriptor(), searchOptions,
                persistenceService);
        try {
            transactionExecutor.execute(searchArchivedProcessInstances);
            return searchArchivedProcessInstances.getResult();
        } catch (final SBonitaException sbe) {
            throw new BonitaRuntimeException(sbe);
        }
    }

    @Override
    public SearchResult<HumanTaskInstance> searchPendingTasksForUser(final long userId, final SearchOptions searchOptions) throws InvalidSessionException,
            UserNotFoundException {
        return searchTasksForUser(userId, searchOptions, false);
    }

    /**
     * @param orAssignedToUser
     *            do we also want to retrieve tasks directly assigned to this user ?
     */
    private SearchResult<HumanTaskInstance> searchTasksForUser(final long userId, final SearchOptions searchOptions, final boolean orAssignedToUser)
            throws InvalidSessionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SearchPendingTasksForUser searchPendingTasksForUser = new SearchPendingTasksForUser(activityInstanceService, flowNodeStateManager,
                searchEntitiesDescriptor.getHumanTaskInstanceDescriptor(), userId, searchOptions, orAssignedToUser);
        try {
            transactionExecutor.execute(searchPendingTasksForUser);
            return searchPendingTasksForUser.getResult();
        } catch (final SBonitaException sbe) {
            throw new BonitaRuntimeException(sbe);
        }
    }

    @Override
    public SearchResult<HumanTaskInstance> searchMyAvailableHumanTasks(final long userId, final SearchOptions searchOptions) throws InvalidSessionException,
            SearchException {
        return searchTasksForUser(userId, searchOptions, true);
    }

    @Override
    public SearchResult<HumanTaskInstance> searchPendingTasksSupervisedBy(final long userId, final SearchOptions searchOptions) throws InvalidSessionException,
            UserNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final GetSUser getUser = new GetSUser(identityService, userId);
        try {
            transactionExecutor.execute(getUser);
            getUser.getResult();
        } catch (final SBonitaException e) {
            throw new UserNotFoundException(e);
        }
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SearchPendingTasksSupervisedBy searchPendingTasksSupervisedBy = new SearchPendingTasksSupervisedBy(activityInstanceService, flowNodeStateManager,
                searchEntitiesDescriptor.getHumanTaskInstanceDescriptor(), userId, searchOptions);
        try {
            transactionExecutor.execute(searchPendingTasksSupervisedBy);
            return searchPendingTasksSupervisedBy.getResult();
        } catch (final SBonitaException sbe) {
            throw new BonitaRuntimeException(sbe);
        }
    }

    @Override
    public SearchResult<Comment> searchComments(final SearchOptions searchOptions) throws InvalidSessionException, ProcessInstanceNotFoundException,
            PageOutOfRangeException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SCommentService commentService = tenantAccessor.getCommentService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SearchComments searchComments = new SearchComments(searchEntitiesDescriptor.getCommentDescriptor(), searchOptions, commentService);
        try {
            transactionExecutor.execute(searchComments);
            return searchComments.getResult();
        } catch (final SBonitaException sbe) {
            throw new BonitaRuntimeException(sbe);
        }
    }

    @Override
    public Comment addComment(final long processInstanceId, final String comment) throws InvalidSessionException, CommentAddException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SCommentService commentService = tenantAccessor.getCommentService();
        final AddComment addComment = new AddComment(commentService, processInstanceId, comment);
        try {
            transactionExecutor.execute(addComment);
            final SComment sComment = addComment.getResult();
            return ModelConvertor.toComment(sComment);
        } catch (final SBonitaException e) {
            throw new CommentAddException(e);
        }
    }

    @Override
    public List<Comment> getComments(final long processInstanceId) throws InvalidSessionException, CommentAddException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SCommentService commentService = tenantAccessor.getCommentService();
        final GetComments getComments = new GetComments(commentService, processInstanceId);
        try {
            transactionExecutor.execute(getComments);
            final List<SComment> sComments = getComments.getResult();
            return ModelConvertor.toComments(sComments);
        } catch (final SBonitaException e) {
            throw new CommentAddException(e);
        }
    }

    @Override
    public Set<String> getDocumentNames(final long processDefinitionID) throws InvalidSessionException, DocumentNotFoundException {
        // FIXME: implement me!
        final Set<String> result = new HashSet<String>();
        result.add("myPdf.pdf");
        return result;
    }

    @Override
    public Document attachDocument(final long processInstanceId, final String documentName, final String fileName, final String mimeType, final String url)
            throws ProcessInstanceNotFoundException, InvalidSessionException, DocumentAttachmentException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDocumentService processDocumentService = tenantAccessor.getProcessDocumentService();
        final TransactionExecutor transactionExecutor = getTenantAccessor().getTransactionExecutor();
        final SProcessDocumentBuilder documentBuilder = tenantAccessor.getProcessDocumentBuilder();
        final long author = getUserIdFromSession();
        try {
            final SProcessDocument document = attachDocument(processInstanceId, documentName, fileName, mimeType, url, processDocumentService,
                    transactionExecutor, documentBuilder, author);
            return ModelConvertor.toDocument(document);
        } catch (final SBonitaException sbe) {
            throw new DocumentAttachmentException(sbe);
        }
    }

    protected SProcessDocument attachDocument(final long processInstanceId, final String documentName, final String fileName, final String mimeType,
            final String url, final ProcessDocumentService processDocumentService, final TransactionExecutor transactionExecutor,
            final SProcessDocumentBuilder documentBuilder, final long authorId) throws SBonitaException {
        final SProcessDocument attachment = buildExternalProcessDocumentReference(documentBuilder, processInstanceId, documentName, fileName, mimeType,
                authorId, url);
        final AttachDocument attachDocumentTransationContent = new AttachDocument(processDocumentService, attachment);
        transactionExecutor.execute(attachDocumentTransationContent);
        final SProcessDocument document = attachDocumentTransationContent.getResult();
        return document;
    }

    private SProcessDocument buildExternalProcessDocumentReference(final SProcessDocumentBuilder documentBuilder, final long processInstanceId,
            final String documentName, final String fileName, final String mimeType, final long authorId, final String url) {
        initDocumentBuilder(documentBuilder, processInstanceId, documentName, fileName, mimeType, authorId);
        documentBuilder.setURL(url);
        documentBuilder.setHasContent(false);
        return documentBuilder.done();
    }

    private SProcessDocument buildProcessDocument(final SProcessDocumentBuilder documentBuilder, final long processInstanceId, final String documentName,
            final String fileName, final String mimetype, final long authorId) {
        initDocumentBuilder(documentBuilder, processInstanceId, documentName, fileName, mimetype, authorId);
        documentBuilder.setHasContent(true);
        return documentBuilder.done();
    }

    private void initDocumentBuilder(final SProcessDocumentBuilder documentBuilder, final long processInstanceId, final String documentName,
            final String fileName, final String mimetype, final long authorId) {
        documentBuilder.createNewInstance();
        documentBuilder.setName(documentName);
        documentBuilder.setFileName(fileName);
        documentBuilder.setContentMimeType(mimetype);
        documentBuilder.setProcessInstanceId(processInstanceId);
        documentBuilder.setAuthor(authorId);
        documentBuilder.setCreationDate(System.currentTimeMillis());
    }

    @Override
    public Document attachDocument(final long processInstanceId, final String documentName, final String fileName, final String mimeType,
            final byte[] documentContent) throws ProcessInstanceNotFoundException, InvalidSessionException, DocumentAttachmentException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDocumentService processDocumentService = tenantAccessor.getProcessDocumentService();
        final TransactionExecutor transactionExecutor = getTenantAccessor().getTransactionExecutor();
        final SProcessDocumentBuilder documentBuilder = tenantAccessor.getProcessDocumentBuilder();
        final long authorId = getUserIdFromSession();
        try {
            final SProcessDocument document = attachDocument(processInstanceId, documentName, fileName, mimeType, documentContent, processDocumentService,
                    transactionExecutor, documentBuilder, authorId);
            return ModelConvertor.toDocument(document);
        } catch (final SBonitaException sbe) {
            throw new DocumentAttachmentException(sbe);
        }
    }

    protected SProcessDocument attachDocument(final long processInstanceId, final String documentName, final String fileName, final String mimeType,
            final byte[] documentContent, final ProcessDocumentService processDocumentService, final TransactionExecutor transactionExecutor,
            final SProcessDocumentBuilder documentBuilder, final long authorId) throws SBonitaException {
        final SProcessDocument attachment = buildProcessDocument(documentBuilder, processInstanceId, documentName, fileName, mimeType, authorId);
        final AttachDocumentAndStoreContent attachDocumentTransationContent = new AttachDocumentAndStoreContent(processDocumentService, attachment,
                documentContent);
        transactionExecutor.execute(attachDocumentTransationContent);
        final SProcessDocument document = attachDocumentTransationContent.getResult();
        return document;
    }

    @Override
    public Document attachNewDocumentVersion(final long processInstanceId, final String documentName, final String fileName, final String mimeType,
            final String url) throws InvalidSessionException, DocumentAttachmentException {
        getTenantAccessor();
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDocumentService processDocumentService = tenantAccessor.getProcessDocumentService();
        final TransactionExecutor transactionExecutor = getTenantAccessor().getTransactionExecutor();
        final SProcessDocumentBuilder documentBuilder = tenantAccessor.getProcessDocumentBuilder();
        final long authorId = getUserIdFromSession();
        try {
            final SProcessDocument attachment = buildExternalProcessDocumentReference(documentBuilder, processInstanceId, documentName, fileName, mimeType,
                    authorId, url);
            final AttachDocumentVersion attachDocumentTransationContent = new AttachDocumentVersion(processDocumentService, attachment);
            transactionExecutor.execute(attachDocumentTransationContent);
            return ModelConvertor.toDocument(attachDocumentTransationContent.getResult());
        } catch (final SBonitaException sbe) {
            throw new DocumentAttachmentException(sbe);
        }
    }

    @Override
    public Document attachNewDocumentVersion(final long processInstanceId, final String documentName, final String contentFileName,
            final String contentMimeType, final byte[] documentContent) throws InvalidSessionException, DocumentAttachmentException {
        getTenantAccessor();
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDocumentService processDocumentService = tenantAccessor.getProcessDocumentService();
        final TransactionExecutor transactionExecutor = getTenantAccessor().getTransactionExecutor();
        final SProcessDocumentBuilder documentBuilder = tenantAccessor.getProcessDocumentBuilder();
        final long authorId = getUserIdFromSession();
        try {
            final SProcessDocument attachment = buildProcessDocument(documentBuilder, processInstanceId, documentName, contentFileName, contentMimeType,
                    authorId);
            final AttachDocumentVersionAndStoreContent attachDocumentTransationContent = new AttachDocumentVersionAndStoreContent(processDocumentService,
                    attachment, documentContent);
            transactionExecutor.execute(attachDocumentTransationContent);
            return ModelConvertor.toDocument(attachDocumentTransationContent.getResult());
        } catch (final SBonitaException sbe) {
            throw new DocumentAttachmentException(sbe);
        }
    }

    @Override
    public Document getDocument(final long documentId) throws DocumentNotFoundException, InvalidSessionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDocumentService processDocumentService = tenantAccessor.getProcessDocumentService();
        final TransactionExecutor transactionExecutor = getTenantAccessor().getTransactionExecutor();
        try {
            final GetDocument attachDocumentTransationContent = new GetDocument(processDocumentService, documentId);
            transactionExecutor.execute(attachDocumentTransationContent);
            return ModelConvertor.toDocument(attachDocumentTransationContent.getResult());
        } catch (final SBonitaException sbe) {
            throw new DocumentNotFoundException(sbe);
        }
    }

    @Override
    public List<Document> getLastVersionOfDocuments(final long processInstanceId, final int pageIndex, final int numberPerPage)
            throws ProcessInstanceNotFoundException, InvalidSessionException, DocumentException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDocumentService processDocumentService = tenantAccessor.getProcessDocumentService();
        final TransactionExecutor transactionExecutor = getTenantAccessor().getTransactionExecutor();
        try {
            final GetDocumentsOfProcessInstance transationContent = new GetDocumentsOfProcessInstance(processDocumentService, processInstanceId, pageIndex,
                    numberPerPage);
            transactionExecutor.execute(transationContent);
            final List<SProcessDocument> attachments = transationContent.getResult();
            if (attachments != null && !attachments.isEmpty()) {
                final List<Document> result = new ArrayList<Document>(attachments.size());
                for (final SProcessDocument attachment : attachments) {
                    result.add(ModelConvertor.toDocument(attachment));
                }
                return result;
            } else {
                return Collections.emptyList();
            }
        } catch (final SBonitaException sbe) {
            throw new DocumentException(sbe);
        }
    }

    @Override
    public byte[] getDocumentContent(final String documentStorageId) throws DocumentNotFoundException, InvalidSessionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDocumentService processDocumentService = tenantAccessor.getProcessDocumentService();
        final TransactionExecutor transactionExecutor = getTenantAccessor().getTransactionExecutor();
        try {
            final GetDocumentContent transationContent = new GetDocumentContent(processDocumentService, documentStorageId);
            transactionExecutor.execute(transationContent);
            return transationContent.getResult();
        } catch (final SBonitaException sbe) {
            throw new DocumentNotFoundException(sbe);
        }
    }

    @Override
    public Document getLastDocument(final long processInstaneId, final String documentName) throws InvalidSessionException, DocumentNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDocumentService processDocumentService = tenantAccessor.getProcessDocumentService();
        final TransactionExecutor transactionExecutor = getTenantAccessor().getTransactionExecutor();
        try {
            final GetDocumentByName transationContent = new GetDocumentByName(processDocumentService, processInstaneId, documentName);
            transactionExecutor.execute(transationContent);
            final SProcessDocument attachment = transationContent.getResult();
            return ModelConvertor.toDocument(attachment);
        } catch (final SBonitaException sbe) {
            throw new DocumentNotFoundException(sbe);
        }
    }

    @Override
    public long getNumberOfDocuments(final long processInstanceId) throws InvalidSessionException, DocumentException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDocumentService processDocumentService = tenantAccessor.getProcessDocumentService();
        final TransactionExecutor transactionExecutor = getTenantAccessor().getTransactionExecutor();
        try {
            final GetNumberOfDocumentsOfProcessInstance transationContent = new GetNumberOfDocumentsOfProcessInstance(processDocumentService, processInstanceId);
            transactionExecutor.execute(transationContent);
            final long numberOfAttachment = transationContent.getResult();
            return numberOfAttachment;

        } catch (final SBonitaException sbe) {
            throw new DocumentException(sbe);
        }
    }

    @Override
    public Document getDocumentAtProcessInstantiation(final long processInstanceId, final String documentName) throws InvalidSessionException,
            DocumentNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDocumentService processDocumentService = tenantAccessor.getProcessDocumentService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final SProcessInstanceBuilder sProcessInstanceBuilder = tenantAccessor.getBPMInstanceBuilders().getSProcessInstanceBuilder();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        try {
            final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
            final GetDocumentByNameAtProcessInstantiation transationContent = new GetDocumentByNameAtProcessInstantiation(processDocumentService,
                    processInstanceId, documentName, persistenceService, processInstanceService, sProcessInstanceBuilder);
            transactionExecutor.execute(transationContent);
            final SProcessDocument attachment = transationContent.getResult();
            return ModelConvertor.toDocument(attachment);
        } catch (final SBonitaException sbe) {
            throw new DocumentNotFoundException(sbe);
        }
    }

    @Override
    public Document getDocumentAtActivityInstanceCompletion(final long activityInstanceId, final String documentName) throws InvalidSessionException,
            DocumentNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDocumentService processDocumentService = tenantAccessor.getProcessDocumentService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        try {
            final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
            final GetDocumentByNameAtActivityCompletion transationContent = new GetDocumentByNameAtActivityCompletion(processDocumentService,
                    activityInstanceId, documentName, persistenceService, activityInstanceService);
            transactionExecutor.execute(transationContent);
            final SProcessDocument attachment = transationContent.getResult();
            return ModelConvertor.toDocument(attachment);
        } catch (final SBonitaException sbe) {
            throw new DocumentNotFoundException(sbe);
        }
    }

    @Override
    public SearchResult<HumanTaskInstance> searchPendingTasksManagedBy(final long managerUserId, final SearchOptions searchOptions)
            throws InvalidSessionException, UserNotFoundException, SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final SearchPendingTasksManagedBy searchPendingTasksManagedBy = new SearchPendingTasksManagedBy(activityInstanceService, flowNodeStateManager,
                searchEntitiesDescriptor.getHumanTaskInstanceDescriptor(), managerUserId, searchOptions);
        try {
            transactionExecutor.execute(searchPendingTasksManagedBy);
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
        return searchPendingTasksManagedBy.getResult();
    }

    @Override
    public Map<Long, Long> getNumberOfOverdueOpenTasks(final List<Long> userIds) throws InvalidSessionException, ActivityInstanceReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final GetNumberOfOverdueOpenTasksForUsers transactionContent = new GetNumberOfOverdueOpenTasksForUsers(userIds, activityInstanceService);
            transactionExecutor.execute(transactionContent);
            return transactionContent.getResult();
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ActivityInstanceReadException(e.getMessage());
        }
    }

    @Override
    public SearchResult<ProcessDeploymentInfo> searchUncategorizedProcessDefinitions(final SearchOptions searchOptions) throws InvalidSessionException,
            ProcessDefinitionReadException, PageOutOfRangeException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SearchProcessDefinitionsDescriptor searchDescriptor = searchEntitiesDescriptor.getSearchProcessDefinitionsDescriptor();
        final SearchUncategorizedProcessDefinitions transactionSearch = new SearchUncategorizedProcessDefinitions(processDefinitionService, searchDescriptor,
                searchOptions);
        try {
            transactionExecutor.execute(transactionSearch);
        } catch (final SBonitaException e) {
            throw new ProcessDefinitionReadException("Can't get processDefinition's executing searchProcessDefinitions()");
        }
        return transactionSearch.getResult();
    }

    @Override
    public SearchResult<Comment> searchCommentsManagedBy(final long managerUserId, final SearchOptions searchOptions) throws InvalidSessionException,
            CommentReadException, PageOutOfRangeException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SCommentService commentService = tenantAccessor.getCommentService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SearchCommentsManagedBy searchComments = new SearchCommentsManagedBy(searchEntitiesDescriptor.getCommentDescriptor(), searchOptions,
                commentService, managerUserId);
        try {
            transactionExecutor.execute(searchComments);
            return searchComments.getResult();
        } catch (final SBonitaException sbe) {
            throw new BonitaRuntimeException(sbe);
        }
    }

    @Override
    public SearchResult<Comment> searchCommentsInvolvingUser(final long userId, final SearchOptions searchOptions) throws InvalidSessionException,
            UserNotFoundException, CommentReadException, PageOutOfRangeException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final GetSUser getUser = new GetSUser(identityService, userId);
        try {
            transactionExecutor.execute(getUser);
        } catch (final SBonitaException e) {
            throw new UserNotFoundException(e);
        }
        final SCommentService commentService = tenantAccessor.getCommentService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SearchCommentsInvolvingUser searchComments = new SearchCommentsInvolvingUser(searchEntitiesDescriptor.getCommentDescriptor(), searchOptions,
                commentService, userId);
        try {
            transactionExecutor.execute(searchComments);
            return searchComments.getResult();
        } catch (final SBonitaException sbe) {
            throw new BonitaRuntimeException(sbe);
        }
    }

    @Override
    public SearchResult<ProcessDeploymentInfo> searchUncategorizedProcessDefinitionsSupervisedBy(final long userId, final SearchOptions searchOptions)
            throws InvalidSessionException, ProcessDefinitionReadException, PageOutOfRangeException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SearchProcessDefinitionsDescriptor searchDescriptor = searchEntitiesDescriptor.getSearchProcessDefinitionsDescriptor();
        final SearchUncategorizedProcessDefinitionsSupervisedBy transactionSearch = new SearchUncategorizedProcessDefinitionsSupervisedBy(
                processDefinitionService, searchDescriptor, searchOptions, userId);
        try {
            transactionExecutor.execute(transactionSearch);
        } catch (final SBonitaException e) {
            throw new ProcessDefinitionReadException("Can't get processDefinition's executing searchProcessDefinitions()");
        }
        return transactionSearch.getResult();
    }

    /**
     * byte[] is a zip file exported from studio
     * clear: remove the old .impl file; put the new .impl file in the connector directory
     * reload the cache, connectorId and connectorVersion are used here.
     */
    @Override
    public void setConnectorImplementation(final long processDefinitionId, final String connectorId, final String connectorVersion,
            final byte[] connectorImplementationArchive) throws InvalidSessionException, ConnectorException {

        try {
            this.getProcessDefinition(processDefinitionId);
        } catch (final ProcessDefinitionNotFoundException e) {
            throw new ConnectorException(e);
        } catch (final ProcessDefinitionReadException e) {
            throw new ConnectorException(e);
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ConnectorService connectorService = tenantAccessor.getConnectorService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionContent transactionContent = new SetConnectorImplementation(processDefinitionId, connectorImplementationArchive, connectorId,
                connectorVersion, processDefinitionService, connectorService, tenantAccessor.getTenantId());
        try {
            transactionExecutor.execute(transactionContent);
        } catch (final SBonitaException e) {
            throw new ConnectorException(e);
        }
    }

    @Override
    public ProcessInstance updateProcessInstance(final long processInstanceId, final ProcessInstanceUpdateDescriptor updateDescriptor)
            throws InvalidSessionException, ProcessInstanceNotFoundException, ProcessInstanceModificationException {
        if (updateDescriptor == null || updateDescriptor.getFields().isEmpty()) {
            throw new ProcessInstanceModificationException("The update descriptor does not contain field updates");
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final BPMInstanceBuilders bpmInstanceBuilders = tenantAccessor.getBPMInstanceBuilders();
        final SProcessInstanceUpdateBuilder updateBuilder = bpmInstanceBuilders.getProcessInstanceUpdateBuilder();
        try {
            final UpdateProcessInstance updateProcessInstance = new UpdateProcessInstance(processInstanceService, updateDescriptor, updateBuilder,
                    processInstanceId);
            transactionExecutor.execute(updateProcessInstance);
            return getProcessInstance(processInstanceId);
        } catch (final SProcessInstanceNotFoundException spinfe) {
            throw new ProcessInstanceNotFoundException(spinfe);
        } catch (final SBonitaException sbe) {
            throw new ProcessInstanceModificationException(sbe);
        } catch (final ProcessInstanceReadException pire) {
            throw new ProcessInstanceModificationException(pire);
        }
    }

    @Override
    public ProcessInstance updateProcessInstanceIndex(final long processInstanceId, final Index index, final String value) throws InvalidSessionException,
            ProcessInstanceNotFoundException, ProcessInstanceModificationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final BPMInstanceBuilders bpmInstanceBuilders = tenantAccessor.getBPMInstanceBuilders();
        final SProcessInstanceUpdateBuilder updateBuilder = bpmInstanceBuilders.getProcessInstanceUpdateBuilder();
        try {
            final UpdateProcessInstance updateProcessInstance = new UpdateProcessInstance(processInstanceService, updateBuilder, processInstanceId, index,
                    value);
            transactionExecutor.execute(updateProcessInstance);
            return getProcessInstance(processInstanceId);
        } catch (final SProcessInstanceNotFoundException spinfe) {
            throw new ProcessInstanceNotFoundException(spinfe);
        } catch (final SBonitaException sbe) {
            throw new ProcessInstanceModificationException(sbe);
        } catch (final ProcessInstanceReadException pire) {
            throw new ProcessInstanceModificationException(pire);
        }
    }

    @Override
    public Map<Long, ProcessDeploymentInfo> getProcessDefinitionsFromIds(final List<Long> processDefinitionIds) throws InvalidSessionException,
            ProcessDefinitionNotFoundException, ProcessDefinitionReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final GetProcessDefinitionsFromIds processDefinitions = new GetProcessDefinitionsFromIds(processDefinitionIds, processDefinitionService);
            transactionExecutor.execute(processDefinitions);
            final List<ProcessDeploymentInfo> processDeploymentInfos = ModelConvertor.toProcessDeploymentInfo(processDefinitions.getResult());
            final Map<Long, ProcessDeploymentInfo> mProcessDefinitions = new HashMap<Long, ProcessDeploymentInfo>();
            for (final ProcessDeploymentInfo p : processDeploymentInfos) {
                mProcessDefinitions.put(p.getProcessId(), p);
            }
            return mProcessDefinitions;
        } catch (final SProcessDefinitionNotFoundException e) {
            log(tenantAccessor, e);
            throw new ProcessDefinitionNotFoundException(e.getMessage());
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ProcessDefinitionReadException(e.getMessage());
        }
    }

    @Override
    public List<ConnectorImplementationDescriptor> getConnectorImplementations(final long processDefinitionId, final int pageIndex, final int numberPerPage,
            final ConnectorCriterion pagingCriterion) throws InvalidSessionException, ProcessDefinitionNotFoundException, PageOutOfRangeException,
            ConnectorException {
        try {
            this.getProcessDefinition(processDefinitionId);
        } catch (final ProcessDefinitionNotFoundException e) {
            throw e;
        } catch (final ProcessDefinitionReadException e) {
            throw new ProcessDefinitionNotFoundException(e);
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ConnectorService connectorService = tenantAccessor.getConnectorService();
        String filed = null;
        OrderByType orderBy = null;
        ConnectorCriterion criterion = pagingCriterion;
        if (criterion == null) {
            criterion = ConnectorCriterion.DEFAULT;
        }
        switch (criterion) {
            case DEFINITION_ID_ASC:
                filed = ConnectorImplementationDescriptor.DEFINITION_ID;
                orderBy = OrderByType.ASC;
                break;
            case DEFINITION_ID_DESC:
                filed = ConnectorImplementationDescriptor.DEFINITION_ID;
                orderBy = OrderByType.DESC;
                break;
            case DEFINITION_VERSION_ASC:
                filed = ConnectorImplementationDescriptor.DEFINITION_VERSION;
                orderBy = OrderByType.ASC;
                break;
            case DEFINITION_VERSION_DESC:
                filed = ConnectorImplementationDescriptor.DEFINITION_VERSION;
                orderBy = OrderByType.DESC;
                break;
            case IMPLEMENTATION_VERSION_ASC:
                filed = ConnectorImplementationDescriptor.VERSIOIN;
                orderBy = OrderByType.ASC;
                break;
            case IMPLEMENTATIONN_VERSION_DESC:
                filed = ConnectorImplementationDescriptor.VERSIOIN;
                orderBy = OrderByType.DESC;
                break;
            case IMPLEMENTATIONN_CLASS_NAME_ACS:
                filed = ConnectorImplementationDescriptor.IMPLEMENTATION_CLASS_NAME;
                orderBy = OrderByType.ASC;
                break;
            case IMPLEMENTATIONN_CLASS_NAME_DESC:
                filed = ConnectorImplementationDescriptor.IMPLEMENTATION_CLASS_NAME;
                orderBy = OrderByType.DESC;
                break;
            case IMPLEMENTATION_ID_DESC:
                filed = ConnectorImplementationDescriptor.ID;
                orderBy = OrderByType.DESC;
                break;
            case IMPLEMENTATION_ID_ASC:
            case DEFAULT:
                filed = ConnectorImplementationDescriptor.ID;
                orderBy = OrderByType.ASC;
                break;
        }
        final GetConnectorImplementations transactionContent = new GetConnectorImplementations(connectorService, processDefinitionId,
                tenantAccessor.getTenantId(), pageIndex, numberPerPage, filed, orderBy);
        try {
            transactionExecutor.execute(transactionContent);
            final List<SConnectorImplementationDescriptor> sConnectorImplementationDescriptors = transactionContent.getResult();
            return ModelConvertor.toConnectorImplementationDescriptors(sConnectorImplementationDescriptors);
        } catch (final SBonitaException e) {
            throw new ConnectorException(e);
        }
    }

    @Override
    public SearchResult<ActivityInstance> searchActivities(final SearchOptions searchOptions) throws InvalidSessionException, SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();

        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final Class<? extends PersistentObject> entityClass = getEntityClass(searchOptions);
        final SearchActivityInstances searchActivityInstancesTransaction = new SearchActivityInstances(activityInstanceService, flowNodeStateManager,
                searchEntitiesDescriptor.getActivityInstanceDescriptor(), searchOptions, entityClass);
        try {
            transactionExecutor.execute(searchActivityInstancesTransaction);
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
        return searchActivityInstancesTransaction.getResult();
    }

    @Override
    public SearchResult<ArchivedFlowElementInstance> searchArchivedFlowElementInstances(final SearchOptions searchOptions) throws InvalidSessionException,
            SearchException {
        // TODO Implement me!
        return new SearchResultImpl<ArchivedFlowElementInstance>(1, Arrays.asList((ArchivedFlowElementInstance) new ArchivedTransitionInstanceImpl(
                "manu's empty implem for searchArchivedFlowElementInstances() method ;-)")));
    }

    @Override
    public SearchResult<ArchivedFlowNodeInstance> searchArchivedFlowNodeInstances(final SearchOptions searchOptions) throws InvalidSessionException,
            SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final SearchArchivedFlowNodeInstances searchTransaction = new SearchArchivedFlowNodeInstances(activityInstanceService, flowNodeStateManager,
                searchEntitiesDescriptor.getArchivedFlowNodeInstanceDescriptor(), searchOptions);
        try {
            transactionExecutor.execute(searchTransaction);
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
        return searchTransaction.getResult();
    }

    @Override
    public SearchResult<FlowNodeInstance> searchFlowNodeInstances(final SearchOptions searchOptions) throws InvalidSessionException, SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();

        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final SearchFlowNodeInstances searchFlowNodeInstancesTransaction = new SearchFlowNodeInstances(activityInstanceService, flowNodeStateManager,
                searchEntitiesDescriptor.getFlowNodeInstanceDescriptor(), searchOptions);
        try {
            transactionExecutor.execute(searchFlowNodeInstancesTransaction);
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
        return searchFlowNodeInstancesTransaction.getResult();
    }

    /**
     * Get which entity should be retrieved;
     * remove activityType filter if possible because it does not correspond exactly to a value of a column in the database.
     * 
     * @param searchOptions
     * @return
     */
    private Class<? extends PersistentObject> getEntityClass(final SearchOptions searchOptions) {
        Class<? extends PersistentObject> entityClass = SActivityInstance.class;
        final SearchFilter searchFilter = getSearchFilter(searchOptions, ActivityInstanceSearchDescriptor.ACTIVITY_TYPE);
        if (searchFilter != null) {
            final FlowNodeType activityType = (FlowNodeType) searchFilter.getValue();
            if (activityType != null) {
                switch (activityType) {
                    case AUTOMATIC_TASK:
                        entityClass = SAutomaticTaskInstance.class;
                        break;
                    case MANUAL_TASK:
                        entityClass = SManualTaskInstance.class;
                        break;
                    case USER_TASK:
                        entityClass = SUserTaskInstance.class;
                        break;
                    case HUMAN_TASK:
                        entityClass = SHumanTaskInstance.class;
                        break;
                    default:
                        entityClass = SActivityInstance.class;
                        break;
                }
                searchOptions.getFilters().remove(searchFilter);
            }
        }
        return entityClass;
    }

    private SearchFilter getSearchFilter(final SearchOptions searchOptions, final String searchedKey) {
        for (final SearchFilter searchFilter : searchOptions.getFilters()) {
            if (searchedKey.equals(searchFilter.getField())) {
                return searchFilter;
            }
        }
        return null;
    }

    @Override
    public SearchResult<ArchivedActivityInstance> searchArchivedActivities(final SearchOptions searchOptions) throws InvalidSessionException, SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();

        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final Class<? extends PersistentObject> entityClass = getArchivedEntityClass(searchOptions);
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final SearchArchivedActivityInstances searchActivityInstancesTransaction = new SearchArchivedActivityInstances(activityInstanceService,
                flowNodeStateManager, searchEntitiesDescriptor.getArchivedActivityInstanceDescriptor(), searchOptions, entityClass, persistenceService);
        try {
            transactionExecutor.execute(searchActivityInstancesTransaction);
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
        return searchActivityInstancesTransaction.getResult();
    }

    private Class<? extends PersistentObject> getArchivedEntityClass(final SearchOptions searchOptions) {
        Class<? extends PersistentObject> entityClass = SAActivityInstance.class;
        final SearchFilter searchFilter = getSearchFilter(searchOptions, ArchivedActivityInstanceSearchDescriptor.ACTIVITY_TYPE);
        if (searchFilter != null) {
            final FlowNodeType activityType = (FlowNodeType) searchFilter.getValue();
            if (activityType != null) {
                switch (activityType) {
                    case AUTOMATIC_TASK:
                        entityClass = SAAutomaticTaskInstance.class;
                        break;
                    case MANUAL_TASK:
                        entityClass = SAManualTaskInstance.class;
                        break;
                    case USER_TASK:
                        entityClass = SAUserTaskInstance.class;
                        break;
                    case HUMAN_TASK:
                        entityClass = SAHumanTaskInstance.class;
                        break;
                    default:
                        entityClass = SAActivityInstance.class;
                        break;
                }
                searchOptions.getFilters().remove(searchFilter);
            }
        }
        return entityClass;
    }

    @Override
    public Map<Long, Long> getNumberOfProfileMembers(final List<Long> profileIds) throws InvalidSessionException, SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProfileService profileService = tenantAccessor.getProfileService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final GetNumberOfProfileMembers numberOfProfileMembers = new GetNumberOfProfileMembers(profileIds, profileService);
        try {
            transactionExecutor.execute(numberOfProfileMembers);
            final List<SProfileMember> listOfProfileMembers = numberOfProfileMembers.getResult();
            final Map<Long, SProfileMember> profileMembers = new HashMap<Long, SProfileMember>();
            final Map<Long, Long> result = new HashMap<Long, Long>();
            for (final SProfileMember p : listOfProfileMembers) {
                profileMembers.put(p.getProfileId(), p);
            }

            for (final Long obj : profileMembers.keySet()) {
                long number = 0;
                final long profileId = obj;
                for (int j = 0; j < listOfProfileMembers.size(); j++) {
                    final long tempProfileId = listOfProfileMembers.get(j).getProfileId();
                    if (profileId == tempProfileId) {
                        number++;
                    }
                }
                result.put(profileId, number);
            }
            return result;
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
    }

    @Override
    public ConnectorImplementationDescriptor getConnectorImplementation(final long processDefinitionId, final String connectorId, final String connectorVersion)
            throws InvalidSessionException, ProcessDefinitionNotFoundException, ConnectorNotFoundException {
        try {
            this.getProcessDefinition(processDefinitionId);
        } catch (final ProcessDefinitionNotFoundException e) {
            throw e;
        } catch (final ProcessDefinitionReadException e) {
            throw new ProcessDefinitionNotFoundException(e);
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ConnectorService connectorService = tenantAccessor.getConnectorService();
        final GetConnectorImplementation transactionContent = new GetConnectorImplementation(connectorService, processDefinitionId, connectorId,
                connectorVersion, tenantAccessor.getTenantId());
        try {
            transactionExecutor.execute(transactionContent);
            final SConnectorImplementationDescriptor sConnectorImplementationDescriptor = transactionContent.getResult();
            return ModelConvertor.toConnectorImplementationDescriptor(sConnectorImplementationDescriptor);
        } catch (final SBonitaException e) {
            throw new ConnectorNotFoundException(e);
        }
    }

    @Override
    public void cancelProcessInstance(final long processInstanceId) throws InvalidSessionException, ProcessInstanceNotFoundException,
            ProcessInstanceReadException, ProcessInstanceModificationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final BPMInstanceBuilders bpmInstanceBuilders = tenantAccessor.getBPMInstanceBuilders();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final ProcessExecutor processExecutor = tenantAccessor.getProcessExecutor();
        final LockService lockService = tenantAccessor.getLockService();

        final TransactionalProcessInstanceInterruptor processInstanceInterruptor = new TransactionalProcessInstanceInterruptor(bpmInstanceBuilders,
                processInstanceService, activityInstanceService, transactionExecutor, processExecutor, lockService, true);

        try {
            processInstanceInterruptor.interruptProcessInstance(processInstanceId, SStateCategory.CANCELLING, getUserIdFromSession());
        } catch (final SBonitaException e) {
            throw new ProcessInstanceModificationException(e);
        }
    }

    @Override
    public void setProcessInstanceState(final ProcessInstance processInstance, final String state) throws InvalidSessionException,
            ProcessInstanceModificationException {
        // NOW, is only available for COMPLETED, ABORTED, CANCELLED, STARTED
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        try {
            final ProcessInstanceState processInstanceState = ModelConvertor.getProcessInstanceState(state);
            final SetProcessInstanceState transactionContent = new SetProcessInstanceState(processInstanceService, processInstance.getId(),
                    processInstanceState);
            transactionExecutor.execute(transactionContent);
        } catch (final SBonitaException e) {
            throw new ProcessInstanceModificationException(e.getMessage());
        }
    }

    @Override
    public Map<Long, ProcessDeploymentInfo> getProcessDeploymentInfoFromProcessInstanceIds(final List<Long> processInstantsIds) throws InvalidSessionException,
            SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final GetProcessDeploymentInfoFromProcessInstanceIds processDefinitions = new GetProcessDeploymentInfoFromProcessInstanceIds(processInstantsIds,
                processDefinitionService);
        try {
            transactionExecutor.execute(processDefinitions);
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
        final List<Map<String, String>> sProcessDeploymentInfos = processDefinitions.getResult();
        Map<Long, ProcessDeploymentInfo> mProcessDeploymentInfos = new HashMap<Long, ProcessDeploymentInfo>();
        mProcessDeploymentInfos = getProcessDeploymentInfosFromMap(sProcessDeploymentInfos);
        return mProcessDeploymentInfos;

    }

    /**
     * get processDeploymentInfos from result
     * 
     * @param sProcessDeploymentInfos
     * @return map
     */
    private Map<Long, ProcessDeploymentInfo> getProcessDeploymentInfosFromMap(final List<Map<String, String>> sProcessDeploymentInfos) {
        final Map<Long, ProcessDeploymentInfo> mProcessDeploymentInfos = new HashMap<Long, ProcessDeploymentInfo>();
        long processInstanceId = 0;
        long id = 0;
        long processId = 0;
        String name = "";
        String version = "";
        String description = "";
        long deploymentDate = 0;
        long deployedBy = 0;
        String state = "";
        String displayName = "";
        long lastUpdateDate = 0;
        String iconPath = "";
        String displayDescription = "";
        for (final Map<String, String> m : sProcessDeploymentInfos) {
            ProcessDeploymentInfoImpl PDInfoImpl = null;
            final Iterator<String> keys = m.keySet().iterator();
            while (keys.hasNext()) {
                final String key = keys.next();
                final Object value = m.get(key);
                if (key.equals("processInstanceId")) {
                    processInstanceId = Long.parseLong(value.toString());
                }
                if (key.equals("id")) {
                    id = Long.parseLong(value.toString());
                }
                if (key.equals("processId")) {
                    processId = Long.parseLong(value.toString());
                }
                if (key.equals("name")) {
                    name = m.get(key);
                }
                if (key.equals("version")) {
                    version = m.get(key);
                }
                if (key.equals("description")) {
                    description = String.valueOf(m.get(key));
                }
                if (key.equals("deploymentDate")) {
                    deploymentDate = Long.parseLong(value.toString());
                }
                if (key.equals("deployedBy")) {
                    deployedBy = Long.parseLong(value.toString());
                }
                if (key.equals("state")) {
                    state = m.get(key);
                }
                if (key.equals("displayName")) {
                    displayName = m.get(key);
                }
                if (key.equals("lastUpdateDate")) {
                    lastUpdateDate = Long.parseLong(value.toString());
                }
                if (key.equals("iconPath")) {
                    iconPath = m.get(key);
                }
                if (key.equals("displayDescription")) {
                    displayDescription = String.valueOf(m.get(key));
                }
            }
            PDInfoImpl = new ProcessDeploymentInfoImpl(id, processId, name, version, description, new Date(deploymentDate), deployedBy, state, displayName,
                    new Date(lastUpdateDate), iconPath, displayDescription);
            mProcessDeploymentInfos.put(processInstanceId, PDInfoImpl);
        }
        return mProcessDeploymentInfos;
    }

    @Override
    public SearchResult<Document> searchDocuments(final SearchOptions searchOptions) throws InvalidSessionException, SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ProcessDocumentService processDocumentService = tenantAccessor.getProcessDocumentService();

        final SearchDocuments searchDocuments = new SearchDocuments(processDocumentService, searchEntitiesDescriptor.getDocumentDescriptor(), searchOptions);
        try {
            transactionExecutor.execute(searchDocuments);
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
        return searchDocuments.getResult();
    }

    @Override
    public SearchResult<Document> searchDocumentsSupervisedBy(final long userId, final SearchOptions searchOptions) throws InvalidSessionException,
            UserNotFoundException, SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ProcessDocumentService processDocumentService = tenantAccessor.getProcessDocumentService();

        final SearchDocumentsSupervisedBy searchDocuments = new SearchDocumentsSupervisedBy(processDocumentService,
                searchEntitiesDescriptor.getDocumentDescriptor(), searchOptions, userId);
        try {
            transactionExecutor.execute(searchDocuments);
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
        return searchDocuments.getResult();
    }

    @Override
    public SearchResult<ArchivedDocument> searchArchivedDocuments(final SearchOptions searchOptions) throws InvalidSessionException, SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ProcessDocumentService processDocumentService = tenantAccessor.getProcessDocumentService();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final SearchArchivedDocuments searchDocuments = new SearchArchivedDocuments(processDocumentService,
                searchEntitiesDescriptor.getArchivedDocumentDescriptor(), searchOptions, persistenceService);
        try {
            transactionExecutor.execute(searchDocuments);
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
        return searchDocuments.getResult();
    }

    @Override
    public SearchResult<ArchivedDocument> searchArchivedDocumentsSupervisedBy(final long userId, final SearchOptions searchOptions)
            throws InvalidSessionException, UserNotFoundException, SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ProcessDocumentService processDocumentService = tenantAccessor.getProcessDocumentService();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final SearchArchivedDocumentsSupervisedBy searchDocuments = new SearchArchivedDocumentsSupervisedBy(userId, processDocumentService,
                searchEntitiesDescriptor.getArchivedDocumentDescriptor(), searchOptions, persistenceService);
        try {
            transactionExecutor.execute(searchDocuments);
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
        return searchDocuments.getResult();
    }

    @Override
    public void retryTask(final long activityInstanceId) throws InvalidSessionException, ActivityNotFoundException, RetryTaskException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final GetActivityInstance transactionContent = new GetActivityInstance(activityInstanceService, activityInstanceId);
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final ProcessExecutor processExecutor = tenantAccessor.getProcessExecutor();
        try {
            transactionExecutor.execute(transactionContent);
            final SActivityInstance activity = transactionContent.getResult();
            final GetProcessInstance getProcessInstance = new GetProcessInstance(processInstanceService, activity.getRootContainerId());
            transactionExecutor.execute(getProcessInstance);
            final GetProcessDefinition getProcessDefinition = new GetProcessDefinition(getProcessInstance.getResult().getProcessDefinitionId(),
                    processDefinitionService);
            transactionExecutor.execute(getProcessDefinition);
            final FlowNodeState flowNodeState = flowNodeStateManager.getState(activity.getStateId());
            if (ActivityStates.FAILED_STATE.equals(flowNodeState.getName())) {
                final GetPreviousState getPreviousState = new GetPreviousState(activityInstanceService, activity.getId());
                transactionExecutor.execute(getPreviousState);
                final SAActivityInstance activityInstance = getPreviousState.getResult();
                final int stateId = activityInstance.getStateId();
                setStateByStateId(activityInstanceId, stateId);
                processExecutor.executeActivity(activityInstanceId, getUserIdFromSession());
            } else {
                throw new RetryTaskException("Unable to retry a task that is not faild, tried to retry task name=" + activity.getName() + " id="
                        + activityInstanceId + " that was in state " + flowNodeState);
            }
        } catch (final SBonitaException e) {
            throw new RetryTaskException(e);
        } catch (final UserTaskNotFoundException e) {
            throw new RetryTaskException(e);
        } catch (final ActivityExecutionFailedException e) {
            throw new RetryTaskException(e);
        }
    }

    @Override
    public ArchivedDocument getArchivedVersionOfProcessDocument(final long sourceObjectId) throws InvalidSessionException, ArchivedDocumentNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDocumentService processDocumentService = tenantAccessor.getProcessDocumentService();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final GetArchivedDocument getArchivedDocument = new GetArchivedDocument(processDocumentService, sourceObjectId, persistenceService);
        try {
            transactionExecutor.execute(getArchivedDocument);
        } catch (final SBonitaException e) {
            throw new ArchivedDocumentNotFoundException(e);
        }
        return ModelConvertor.toArchivedDocument(getArchivedDocument.getResult());
    }

    @Override
    public ArchivedDocument getArchivedProcessDocument(final long archivedProcessDocumentId) throws InvalidSessionException, ArchivedDocumentNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDocumentService processDocumentService = tenantAccessor.getProcessDocumentService();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        try {
            transactionExecutor.openTransaction();
            try {
                final SAProcessDocument archivedDocument = processDocumentService.getArchivedDocument(archivedProcessDocumentId, persistenceService);
                return ModelConvertor.toArchivedDocument(archivedDocument);
            } catch (final SDocumentNotFoundException e) {
                throw new ArchivedDocumentNotFoundException(e);
            } finally {
                transactionExecutor.completeTransaction();
            }
        } catch (final STransactionException e1) {
            throw new ArchivedDocumentNotFoundException(e1);
        }
    }

    @Override
    public SearchResult<ArchivedComment> searchArchivedComments(final SearchOptions searchOptions) throws InvalidSessionException, SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SCommentService sCommentService = tenantAccessor.getCommentService();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final SearchArchivedComments searchArchivedComments = new SearchArchivedComments(sCommentService,
                searchEntitiesDescriptor.getSearchArchivedCommentsDescriptor(), searchOptions, persistenceService);
        try {
            transactionExecutor.execute(searchArchivedComments);
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
        return searchArchivedComments.getResult();
    }

    @Override
    public ArchivedComment getArchivedComment(final long archivedCommentId) throws InvalidSessionException, CommentReadException, ObjectNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SCommentService sCommentService = tenantAccessor.getCommentService();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        try {
            transactionExecutor.openTransaction();
            try {
                final SAComment archivedComment = sCommentService.getArchivedComment(archivedCommentId, persistenceService);
                return ModelConvertor.toArchivedComment(archivedComment);
            } catch (final SCommentNotFoundException e) {
                throw new ObjectNotFoundException(e);
            } catch (final SBonitaException e) {
                throw new CommentReadException(e);
            } finally {
                transactionExecutor.completeTransaction();
            }
        } catch (final STransactionException e1) {
            throw new CommentReadException(e1);
        }
    }

    @Override
    public long addBreakpoint(final long definitionId, final long instanceId, final String elementName, final int idOfTheStateToInterrupt,
            final int idOfTheInterruptingState) throws InvalidSessionException, BreakpointCreationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final BreakpointService breakpointService = tenantAccessor.getBreakpointService();
        final BPMInstanceBuilders breakpointBuilder = tenantAccessor.getBPMInstanceBuilders();
        final AddBreakpoint transactionContent = new AddBreakpoint(breakpointService, breakpointBuilder, definitionId, instanceId, elementName,
                idOfTheStateToInterrupt, idOfTheInterruptingState);
        try {
            transactionExecutor.execute(transactionContent);
        } catch (final SBonitaException e) {
            throw new BreakpointCreationException(e);
        }
        return transactionContent.getResult().getId();
    }

    @Override
    public long addBreakpoint(final long definitionId, final String elementName, final int idOfTheStateToInterrupt, final int idOfTheInterruptingState)
            throws InvalidSessionException, BreakpointCreationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final BreakpointService breakpointService = tenantAccessor.getBreakpointService();
        final BPMInstanceBuilders breakpointBuilder = tenantAccessor.getBPMInstanceBuilders();
        final AddBreakpoint transactionContent = new AddBreakpoint(breakpointService, breakpointBuilder, definitionId, elementName, idOfTheStateToInterrupt,
                idOfTheInterruptingState);
        try {
            transactionExecutor.execute(transactionContent);
        } catch (final SBonitaException e) {
            throw new BreakpointCreationException(e);
        }
        return transactionContent.getResult().getId();
    }

    @Override
    public void removeBreakpoint(final long id) throws InvalidSessionException, BreakpointDeletionException, BreakpointNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final BreakpointService breakpointService = tenantAccessor.getBreakpointService();
        final RemoveBreakpoint transactionContent = new RemoveBreakpoint(breakpointService, id);
        try {
            transactionExecutor.execute(transactionContent);
        } catch (final SBreakpointNotFoundException e) {
            throw new BreakpointNotFoundException(id);
        } catch (final SBonitaException e) {
            throw new BreakpointDeletionException(e);
        }
    }

    @Override
    public List<Breakpoint> getBreakpoints(final int pageNumber, final int numberPerPage, final BreakpointCriterion sort) throws InvalidSessionException,
            BonitaReadException {
        try {
            final TenantServiceAccessor tenantAccessor = getTenantAccessor();
            final BreakpointService breakpointService = tenantAccessor.getBreakpointService();
            final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
            final SBreakpointBuilder sBreakpointBuilder = tenantAccessor.getBPMInstanceBuilders().getSBreakpointBuilder();
            final GetNumberOfBreakpoints getNumberOfProcesses = new GetNumberOfBreakpoints(breakpointService);
            transactionExecutor.execute(getNumberOfProcesses);
            final long totalNumber = getNumberOfProcesses.getResult();
            PageIndexCheckingUtil.checkIfPageIsOutOfRange(totalNumber, pageNumber, numberPerPage);
            final GetBreakpoints transactionContentWithResult = new GetBreakpoints(pageNumber, breakpointService, sort, numberPerPage, sBreakpointBuilder);
            transactionExecutor.execute(transactionContentWithResult);
            final List<SBreakpoint> result = transactionContentWithResult.getResult();
            final List<Breakpoint> clientResult = ModelConvertor.toBreakpoints(result);
            return clientResult;
        } catch (final SBonitaException e) {
            throw new BonitaReadException(e);
        } catch (final PageOutOfRangeException e) {
            throw new BonitaReadException(e);
        }
    }

    @Override
    public Map<Long, ActorInstance> getActorsFromActorIds(final List<Long> actorIds) throws InvalidSessionException, BonitaReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final Map<Long, ActorInstance> res = new HashMap<Long, ActorInstance>();
        final ActorMappingService actormappingService = tenantAccessor.getActorMappingService();
        final GetActorsByActorIds getActorsByActorIds = new GetActorsByActorIds(actormappingService, actorIds);
        try {
            transactionExecutor.execute(getActorsByActorIds);
        } catch (final SBonitaException e1) {
            throw new BonitaReadException(e1);
        }
        final List<SActor> actors = getActorsByActorIds.getResult();
        for (final SActor actor : actors) {
            res.put(actor.getId(), ModelConvertor.toActorInstance(actor));
        }
        return res;
    }

    @Override
    public Map<Long, ActorPrivilege> getActorPrivilegesFromActorPrivilegeIds(final List<Long> actorPrivilegeIds) throws InvalidSessionException,
            BonitaReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActorPrivilegeService actorpriService = tenantAccessor.getActorPrivilegeService();
        final Map<Long, ActorPrivilege> res = new HashMap<Long, ActorPrivilege>();
        final GetActorPrivilegesByIds getActorPrivilegesByIds = new GetActorPrivilegesByIds(actorpriService, actorPrivilegeIds);
        try {
            transactionExecutor.execute(getActorPrivilegesByIds);
        } catch (final SBonitaException e) {
            throw new BonitaReadException(e);
        }
        final List<SActorPrivilege> actrPrieges = getActorPrivilegesByIds.getResult();
        for (final SActorPrivilege sActorPrivilege : actrPrieges) {
            res.put(sActorPrivilege.getId(), ModelConvertor.toActorPrivilege(sActorPrivilege));
        }
        return res;
    }

    @Override
    public Map<Long, ProcessDeploymentInfo> getProcessDeploymentInfoFromArchivedProcessInstanceIds(final List<Long> archivedProcessInstantsIds)
            throws InvalidSessionException, SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final GetProcessDeploymentInfoFromArchivedProcessInstanceIds getProcessDeploymentInfoFromArchivedProcessInstanceIds = new GetProcessDeploymentInfoFromArchivedProcessInstanceIds(
                archivedProcessInstantsIds, processDefinitionService);
        try {
            transactionExecutor.execute(getProcessDeploymentInfoFromArchivedProcessInstanceIds);
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
        final Map<Long, SProcessDefinitionDeployInfo> sProcessDeploymentInfos = getProcessDeploymentInfoFromArchivedProcessInstanceIds.getResult();
        if (sProcessDeploymentInfos != null && !sProcessDeploymentInfos.isEmpty()) {
            final Map<Long, ProcessDeploymentInfo> processDeploymentInfos = new HashMap<Long, ProcessDeploymentInfo>();
            final Set<Entry<Long, SProcessDefinitionDeployInfo>> entries = sProcessDeploymentInfos.entrySet();
            final Iterator<Entry<Long, SProcessDefinitionDeployInfo>> iterator = entries.iterator();
            while (iterator.hasNext()) {
                final Entry<Long, SProcessDefinitionDeployInfo> entry = iterator.next();
                processDeploymentInfos.put(entry.getKey(), ModelConvertor.toProcessDeploymentInfo(entry.getValue()));
            }
            return processDeploymentInfos;
        }
        return Collections.emptyMap();
    }

    @Override
    public SearchResult<HumanTaskInstance> searchPendingHiddenTasks(final long userId, final SearchOptions searchOptions) throws InvalidSessionException,
            SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final SearchPendingHiddenTasks searchHiddenTasksTx = new SearchPendingHiddenTasks(activityInstanceService, flowNodeStateManager,
                searchEntitiesDescriptor.getHumanTaskInstanceDescriptor(), userId, searchOptions);
        try {
            transactionExecutor.execute(searchHiddenTasksTx);
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
        return searchHiddenTasksTx.getResult();
    }

    @Override
    public void hideTasks(final long userId, final Long... activityInstanceId) throws InvalidSessionException, TaskHidingException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final TransactionContent hideTasksTx = new HideTasks(tenantAccessor.getActivityInstanceService(), userId, activityInstanceId);
        try {
            transactionExecutor.execute(hideTasksTx);
        } catch (final SBonitaException e) {
            throw new TaskHidingException("Error while trying to hide tasks: " + activityInstanceId + " from user with ID " + userId, e);
        }
    }

    @Override
    public void unhideTasks(final long userId, final Long... activityInstanceId) throws InvalidSessionException, TaskHidingException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final TransactionContent unhideTasksTx = new UnhideTasks(tenantAccessor.getActivityInstanceService(), userId, activityInstanceId);
        try {
            transactionExecutor.execute(unhideTasksTx);
        } catch (final SBonitaException e) {
            throw new TaskHidingException("Error while trying to un-hide tasks: " + activityInstanceId + " from user with ID " + userId, e);
        }
    }

    @Override
    public Serializable evaluateExpressionOnProcessDefinition(final Expression expression, final Map<String, Serializable> context,
            final long processDefinitionId) throws InvalidSessionException, ExpressionEvaluationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ExpressionResolverService expressionResolverService = tenantAccessor.getExpressionResolverService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SExpressionBuilders sExpressionBuilders = tenantAccessor.getSExpressionBuilders();
        final SExpression sExpression = ServerModelConvertor.convertExpression(sExpressionBuilders, expression);
        final SExpressionContext expcontext = new SExpressionContext();
        expcontext.setProcessDefinitionId(processDefinitionId);
        SProcessDefinition processDef;
        try {
            processDef = getProcessDefinition(tenantAccessor, processDefinitionId);
            if (processDef != null) {
                expcontext.setProcessDefinition(processDef);
            }
            final HashMap<String, Object> hashMap = new HashMap<String, Object>(context);
            expcontext.setInputValues(hashMap);
            final TransactionContentWithResult<Serializable> transactionContent = new TransactionContentWithResult<Serializable>() {

                private Serializable evaluate;

                @Override
                public void execute() throws SBonitaException {
                    evaluate = expressionResolverService.evaluate(sExpression, expcontext);
                }

                @Override
                public Serializable getResult() {
                    return evaluate;
                }
            };
            transactionExecutor.execute(transactionContent);
            return transactionContent.getResult();
        } catch (final InvalidProcessDefinitionException e) {
            throw new ExpressionEvaluationException(e);
        } catch (final SExpressionTypeUnknownException e) {
            throw new ExpressionEvaluationException(e);
        } catch (final SExpressionEvaluationException e) {
            throw new ExpressionEvaluationException(e);
        } catch (final SExpressionDependencyMissingException e) {
            throw new ExpressionEvaluationException(e);
        } catch (final SInvalidExpressionException e) {
            throw new ExpressionEvaluationException(e);
        } catch (final SBonitaException e) {
            throw new ExpressionEvaluationException(e);
        }
    }

    @Override
    public void updateDueDateOfTask(final long userTaskId, final Date dueDate) throws InvalidSessionException, ActivityInstanceModificationException,
            ActivityInstanceNotFoundException {
        if (dueDate == null) {
            throw new ActivityInstanceModificationException("Unable to update a due date to null");
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        try {
            final SetExpectedEndDate updateProcessInstance = new SetExpectedEndDate(activityInstanceService, userTaskId, dueDate);
            transactionExecutor.execute(updateProcessInstance);
        } catch (final SFlowNodeNotFoundException e) {
            throw new ActivityInstanceNotFoundException(userTaskId);
        } catch (final SBonitaException e) {
            throw new ActivityInstanceModificationException(e);
        }
    }

    @Override
    public boolean isTaskHidden(final long userTaskId, final long userId) throws InvalidSessionException, ActivityInstanceReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final IsTaskHidden hideTasksTx = new IsTaskHidden(tenantAccessor.getActivityInstanceService(), userId, userTaskId);
        try {
            transactionExecutor.execute(hideTasksTx);
            return hideTasksTx.getResult();
        } catch (final SBonitaException e) {
            throw new ActivityInstanceReadException(e);
        }
    }

    @Override
    public long countComments(final SearchOptions searchOptions) throws PageOutOfRangeException, InvalidSessionException, ProcessInstanceNotFoundException {
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 0).setFilters(searchOptions.getFilters()).searchTerm(
                searchOptions.getSearchTerm());

        final SearchResult<Comment> searchResult = searchComments(searchOptionsBuilder.done());

        return searchResult.getCount();
    }

    @Override
    public long countAttachments(final SearchOptions searchOptions) throws InvalidSessionException, SearchException {
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 0).setFilters(searchOptions.getFilters()).searchTerm(
                searchOptions.getSearchTerm());

        final SearchResult<Document> searchResult = searchDocuments(searchOptionsBuilder.done());

        return searchResult.getCount();
    }

    @Override
    public void sendSignal(final String signalName) throws InvalidSessionException, SendEventException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final EventsHandler eventsHandler = tenantAccessor.getEventsHandler();
        final SThrowSignalEventTriggerDefinitionBuilder signalEventTriggerDefinitionBuilder = tenantAccessor.getBPMDefinitionBuilders()
                .getThrowSignalEventTriggerDefinitionBuilder();
        final SThrowSignalEventTriggerDefinition signalEventTriggerDefinition = signalEventTriggerDefinitionBuilder.createNewInstance(signalName).done();
        try {
            transactionExecutor.openTransaction();
            try {
                eventsHandler.handleThrowEvent(signalEventTriggerDefinition);
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new SendEventException(e);
            } finally {
                transactionExecutor.completeTransaction();
            }

        } catch (final STransactionException e) {
            throw new SendEventException(e);
        }
    }

    @Override
    public void sendMessage(final String messageName, final Expression targetProcess, final Expression targetFlowNode,
            final Map<Expression, Expression> messageContent) throws InvalidSessionException, SendEventException {
        sendMessage(messageName, targetProcess, targetFlowNode, messageContent, null);
    }

    @Override
    public void sendMessage(final String messageName, final Expression targetProcess, final Expression targetFlowNode,
            final Map<Expression, Expression> messageContent, final Map<Expression, Expression> correlations) throws InvalidSessionException,
            SendEventException {
        if (correlations != null && correlations.size() > 5) {
            throw new SendEventException("Too many correlations: a message can not have more than 5 correlations.");
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final EventsHandler eventsHandler = tenantAccessor.getEventsHandler();
        final SExpressionBuilders sExpressionBuilders = tenantAccessor.getSExpressionBuilders();
        final SDataDefinitionBuilders sDataDefinitionBuilders = tenantAccessor.getSDataDefinitionBuilders();
        final ExpressionResolverService expressionResolverService = tenantAccessor.getExpressionResolverService();

        final SThrowMessageEventTriggerDefinitionBuilder messageEventTriggerDefinitionBuilder = tenantAccessor.getBPMDefinitionBuilders()
                .getThrowMessageEventTriggerDefinitionBuilder();
        final SExpression targetProcessNameExp = ServerModelConvertor.convertExpression(sExpressionBuilders, targetProcess);
        final SExpression targetFlowNodeNameExp = ServerModelConvertor.convertExpression(sExpressionBuilders, targetFlowNode);
        messageEventTriggerDefinitionBuilder.createNewInstance(messageName, targetProcessNameExp, targetFlowNodeNameExp);
        if (correlations != null && !correlations.isEmpty()) {
            addMessageCorrelations(messageEventTriggerDefinitionBuilder, sExpressionBuilders, correlations);
        }
        try {
            transactionExecutor.openTransaction();
            try {
                if (messageContent != null && !messageContent.isEmpty()) {
                    addMessageContent(messageEventTriggerDefinitionBuilder, sExpressionBuilders, sDataDefinitionBuilders, expressionResolverService,
                            messageContent);
                }
                final SThrowMessageEventTriggerDefinition messageEventTriggerDefinition = messageEventTriggerDefinitionBuilder.done();
                eventsHandler.handleThrowEvent(messageEventTriggerDefinition);
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new SendEventException(e);
            } finally {
                transactionExecutor.completeTransaction();
            }

        } catch (final STransactionException e) {
            throw new SendEventException(e);
        }

    }

    private void addMessageContent(final SThrowMessageEventTriggerDefinitionBuilder messageEventTriggerDefinitionBuilder,
            final SExpressionBuilders sExpressionBuilders, final SDataDefinitionBuilders sDataDefinitionBuilders,
            final ExpressionResolverService expressionResolverService, final Map<Expression, Expression> messageContent) throws SBonitaException {
        for (final Entry<Expression, Expression> entry : messageContent.entrySet()) {
            expressionResolverService.evaluate(ServerModelConvertor.convertExpression(sExpressionBuilders, entry.getKey()));
            final SDataDefinitionBuilder dataDefinitionBuilder = sDataDefinitionBuilders.getDataDefinitionBuilder().createNewInstance(
                    entry.getKey().getContent(), entry.getValue().getReturnType());
            dataDefinitionBuilder.setDefaultValue(ServerModelConvertor.convertExpression(sExpressionBuilders, entry.getValue()));
            messageEventTriggerDefinitionBuilder.addData(dataDefinitionBuilder.done());
        }

    }

    private void addMessageCorrelations(final SThrowMessageEventTriggerDefinitionBuilder messageEventTriggerDefinitionBuilder,
            final SExpressionBuilders sExpressionBuilders, final Map<Expression, Expression> messageCorrelations) {
        for (final Entry<Expression, Expression> entry : messageCorrelations.entrySet()) {
            messageEventTriggerDefinitionBuilder.addCorrelation(ServerModelConvertor.convertExpression(sExpressionBuilders, entry.getKey()),
                    ServerModelConvertor.convertExpression(sExpressionBuilders, entry.getValue()));
        }

    }
}
