/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.SActorNotFoundException;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.SActorMember;
import org.bonitasoft.engine.actor.mapping.model.SActorUpdateBuilder;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.impl.resolver.ProcessDependencyResolver;
import org.bonitasoft.engine.api.impl.transaction.activity.GetActivities;
import org.bonitasoft.engine.api.impl.transaction.activity.GetActivityInstance;
import org.bonitasoft.engine.api.impl.transaction.activity.GetArchivedActivityInstance;
import org.bonitasoft.engine.api.impl.transaction.activity.GetArchivedActivityInstances;
import org.bonitasoft.engine.api.impl.transaction.activity.GetNumberOfActivityInstance;
import org.bonitasoft.engine.api.impl.transaction.actor.ExportActorMapping;
import org.bonitasoft.engine.api.impl.transaction.actor.GetActor;
import org.bonitasoft.engine.api.impl.transaction.actor.GetActorMembers;
import org.bonitasoft.engine.api.impl.transaction.actor.GetActorsByActorIds;
import org.bonitasoft.engine.api.impl.transaction.actor.GetActorsByPagination;
import org.bonitasoft.engine.api.impl.transaction.actor.GetNumberOfActorMembers;
import org.bonitasoft.engine.api.impl.transaction.actor.GetNumberOfActors;
import org.bonitasoft.engine.api.impl.transaction.actor.GetNumberOfGroupsOfActor;
import org.bonitasoft.engine.api.impl.transaction.actor.GetNumberOfMembershipsOfActor;
import org.bonitasoft.engine.api.impl.transaction.actor.GetNumberOfRolesOfActor;
import org.bonitasoft.engine.api.impl.transaction.actor.GetNumberOfUsersOfActor;
import org.bonitasoft.engine.api.impl.transaction.actor.ImportActorMapping;
import org.bonitasoft.engine.api.impl.transaction.actor.RemoveActorMember;
import org.bonitasoft.engine.api.impl.transaction.category.CreateCategory;
import org.bonitasoft.engine.api.impl.transaction.category.DeleteSCategory;
import org.bonitasoft.engine.api.impl.transaction.category.GetCategories;
import org.bonitasoft.engine.api.impl.transaction.category.GetCategory;
import org.bonitasoft.engine.api.impl.transaction.category.GetNumberOfCategories;
import org.bonitasoft.engine.api.impl.transaction.category.GetNumberOfCategoriesOfProcess;
import org.bonitasoft.engine.api.impl.transaction.category.RemoveCategoriesFromProcessDefinition;
import org.bonitasoft.engine.api.impl.transaction.category.RemoveProcessDefinitionsOfCategory;
import org.bonitasoft.engine.api.impl.transaction.category.UpdateCategory;
import org.bonitasoft.engine.api.impl.transaction.command.expression.EvaluateExpressionsDefinitionLevel;
import org.bonitasoft.engine.api.impl.transaction.command.expression.EvaluateExpressionsInstanceLevel;
import org.bonitasoft.engine.api.impl.transaction.command.expression.EvaluateExpressionsInstanceLevelAndArchived;
import org.bonitasoft.engine.api.impl.transaction.comment.AddComment;
import org.bonitasoft.engine.api.impl.transaction.comment.GetComments;
import org.bonitasoft.engine.api.impl.transaction.connector.GetConnectorImplementation;
import org.bonitasoft.engine.api.impl.transaction.connector.GetConnectorImplementations;
import org.bonitasoft.engine.api.impl.transaction.connector.GetNumberOfConnectorImplementations;
import org.bonitasoft.engine.api.impl.transaction.data.GetNumberOfDataInstanceForContainer;
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
import org.bonitasoft.engine.api.impl.transaction.event.GetEventInstances;
import org.bonitasoft.engine.api.impl.transaction.flownode.GetFlowNodeInstance;
import org.bonitasoft.engine.api.impl.transaction.flownode.HideTasks;
import org.bonitasoft.engine.api.impl.transaction.flownode.IsTaskHidden;
import org.bonitasoft.engine.api.impl.transaction.flownode.SetExpectedEndDate;
import org.bonitasoft.engine.api.impl.transaction.flownode.UnhideTasks;
import org.bonitasoft.engine.api.impl.transaction.identity.GetSUser;
import org.bonitasoft.engine.api.impl.transaction.process.AddProcessDefinitionToCategory;
import org.bonitasoft.engine.api.impl.transaction.process.DeleteArchivedProcessInstances;
import org.bonitasoft.engine.api.impl.transaction.process.DeleteProcess;
import org.bonitasoft.engine.api.impl.transaction.process.DisableProcess;
import org.bonitasoft.engine.api.impl.transaction.process.EnableProcess;
import org.bonitasoft.engine.api.impl.transaction.process.GetArchivedProcessInstanceList;
import org.bonitasoft.engine.api.impl.transaction.process.GetLastArchivedProcessInstance;
import org.bonitasoft.engine.api.impl.transaction.process.GetLatestProcessDefinitionId;
import org.bonitasoft.engine.api.impl.transaction.process.GetNumberOfArchivedProcessInstance;
import org.bonitasoft.engine.api.impl.transaction.process.GetNumberOfProcessDeploymentInfos;
import org.bonitasoft.engine.api.impl.transaction.process.GetNumberOfProcessDeploymentInfosUnrelatedToCategory;
import org.bonitasoft.engine.api.impl.transaction.process.GetNumberOfProcessInstance;
import org.bonitasoft.engine.api.impl.transaction.process.GetProcessDefinition;
import org.bonitasoft.engine.api.impl.transaction.process.GetProcessDefinitionDeployInfo;
import org.bonitasoft.engine.api.impl.transaction.process.GetProcessDefinitionDeployInfoFromArchivedProcessInstanceIds;
import org.bonitasoft.engine.api.impl.transaction.process.GetProcessDefinitionDeployInfoFromProcessInstanceIds;
import org.bonitasoft.engine.api.impl.transaction.process.GetProcessDefinitionDeployInfos;
import org.bonitasoft.engine.api.impl.transaction.process.GetProcessDefinitionDeployInfosWithActorOnlyForGroup;
import org.bonitasoft.engine.api.impl.transaction.process.GetProcessDefinitionDeployInfosWithActorOnlyForGroups;
import org.bonitasoft.engine.api.impl.transaction.process.GetProcessDefinitionDeployInfosWithActorOnlyForRole;
import org.bonitasoft.engine.api.impl.transaction.process.GetProcessDefinitionDeployInfosWithActorOnlyForRoles;
import org.bonitasoft.engine.api.impl.transaction.process.GetProcessDefinitionDeployInfosWithActorOnlyForUser;
import org.bonitasoft.engine.api.impl.transaction.process.GetProcessDefinitionDeployInfosWithActorOnlyForUsers;
import org.bonitasoft.engine.api.impl.transaction.process.GetProcessDefinitionIDByNameAndVersion;
import org.bonitasoft.engine.api.impl.transaction.process.GetProcessDefinitionIdsOfCategory;
import org.bonitasoft.engine.api.impl.transaction.process.GetProcessDeploymentInfosFromIds;
import org.bonitasoft.engine.api.impl.transaction.process.GetProcessInstance;
import org.bonitasoft.engine.api.impl.transaction.process.GetStartedArchivedProcessInstance;
import org.bonitasoft.engine.api.impl.transaction.process.GetStartedProcessInstance;
import org.bonitasoft.engine.api.impl.transaction.process.SetProcessInstanceState;
import org.bonitasoft.engine.api.impl.transaction.process.UpdateProcessDeploymentInfo;
import org.bonitasoft.engine.api.impl.transaction.task.AssignOrUnassignUserTask;
import org.bonitasoft.engine.api.impl.transaction.task.GetAssignedTasks;
import org.bonitasoft.engine.api.impl.transaction.task.GetHumanTaskInstance;
import org.bonitasoft.engine.api.impl.transaction.task.GetNumberOfAssignedUserTaskInstances;
import org.bonitasoft.engine.api.impl.transaction.task.GetNumberOfOpenTasksForUsers;
import org.bonitasoft.engine.api.impl.transaction.task.GetNumberOfOverdueOpenTasksForUsers;
import org.bonitasoft.engine.api.impl.transaction.task.SetTaskPriority;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.bpm.actor.ActorCriterion;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.actor.ActorMappingExportException;
import org.bonitasoft.engine.bpm.actor.ActorMappingImportException;
import org.bonitasoft.engine.bpm.actor.ActorMember;
import org.bonitasoft.engine.bpm.actor.ActorNotFoundException;
import org.bonitasoft.engine.bpm.actor.ActorUpdater;
import org.bonitasoft.engine.bpm.actor.ActorUpdater.ActorField;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.category.Category;
import org.bonitasoft.engine.bpm.category.CategoryCriterion;
import org.bonitasoft.engine.bpm.category.CategoryNotFoundException;
import org.bonitasoft.engine.bpm.category.CategoryUpdater;
import org.bonitasoft.engine.bpm.category.CategoryUpdater.CategoryField;
import org.bonitasoft.engine.bpm.comment.ArchivedComment;
import org.bonitasoft.engine.bpm.comment.Comment;
import org.bonitasoft.engine.bpm.connector.ArchivedConnectorInstance;
import org.bonitasoft.engine.bpm.connector.ConnectorCriterion;
import org.bonitasoft.engine.bpm.connector.ConnectorExecutionException;
import org.bonitasoft.engine.bpm.connector.ConnectorImplementationDescriptor;
import org.bonitasoft.engine.bpm.connector.ConnectorInstance;
import org.bonitasoft.engine.bpm.connector.ConnectorNotFoundException;
import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.bpm.document.ArchivedDocument;
import org.bonitasoft.engine.bpm.document.ArchivedDocumentNotFoundException;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentAttachmentException;
import org.bonitasoft.engine.bpm.document.DocumentCriterion;
import org.bonitasoft.engine.bpm.document.DocumentException;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ActivityDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ActivityExecutionException;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ActivityStates;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstanceNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.EventCriterion;
import org.bonitasoft.engine.bpm.flownode.EventInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeExecutionException;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstanceNotFoundException;
import org.bonitasoft.engine.bpm.flownode.FlowNodeType;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.SendEventException;
import org.bonitasoft.engine.bpm.flownode.TaskPriority;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.bpm.process.ProcessActivationException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDeployException;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoUpdater;
import org.bonitasoft.engine.bpm.process.ProcessEnablementException;
import org.bonitasoft.engine.bpm.process.ProcessExecutionException;
import org.bonitasoft.engine.bpm.process.ProcessExportException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.bpm.process.impl.ProcessDeploymentInfoImpl;
import org.bonitasoft.engine.bpm.supervisor.ProcessSupervisor;
import org.bonitasoft.engine.bpm.supervisor.ProcessSupervisorSearchDescriptor;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.category.CategoryService;
import org.bonitasoft.engine.core.category.exception.SCategoryAlreadyExistsException;
import org.bonitasoft.engine.core.category.exception.SCategoryInProcessAlreadyExistsException;
import org.bonitasoft.engine.core.category.exception.SCategoryNotFoundException;
import org.bonitasoft.engine.core.category.model.SCategory;
import org.bonitasoft.engine.core.category.model.builder.SCategoryBuilderAccessor;
import org.bonitasoft.engine.core.category.model.builder.SCategoryUpdateBuilder;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.ConnectorResult;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.connector.exception.SConnectorException;
import org.bonitasoft.engine.core.connector.parser.SConnectorImplementationDescriptor;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.login.SLoginException;
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
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SActorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
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
import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingBuilderAccessor;
import org.bonitasoft.engine.core.process.document.model.SAProcessDocument;
import org.bonitasoft.engine.core.process.document.model.SProcessDocument;
import org.bonitasoft.engine.core.process.document.model.builder.SProcessDocumentBuilder;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityInterruptedException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceHierarchicalDeletionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SAutomaticTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SManualTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SReceiveTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.SUserTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAAutomaticTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAManualTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAReceiveTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SASendTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAUserTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAProcessInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.builder.SAutomaticTaskInstanceBuilder;
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
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.NotSerializableException;
import org.bonitasoft.engine.exception.ProcessInstanceHierarchicalDeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.execution.FlowNodeExecutor;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.execution.SUnreleasableTaskException;
import org.bonitasoft.engine.execution.TransactionalProcessInstanceInterruptor;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilders;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.MemberType;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.builder.IdentityModelBuilder;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.lock.SLockException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.operation.LeftOperand;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.persistence.OrderAndField;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.activity.SearchActivityInstances;
import org.bonitasoft.engine.search.activity.SearchArchivedActivityInstances;
import org.bonitasoft.engine.search.comment.SearchArchivedComments;
import org.bonitasoft.engine.search.comment.SearchComments;
import org.bonitasoft.engine.search.comment.SearchCommentsInvolvingUser;
import org.bonitasoft.engine.search.comment.SearchCommentsManagedBy;
import org.bonitasoft.engine.search.connector.SearchArchivedConnectorInstance;
import org.bonitasoft.engine.search.connector.SearchConnectorInstances;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchProcessDefinitionsDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchProcessSupervisorDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchUserDescriptor;
import org.bonitasoft.engine.search.document.SearchArchivedDocuments;
import org.bonitasoft.engine.search.document.SearchArchivedDocumentsSupervisedBy;
import org.bonitasoft.engine.search.document.SearchDocuments;
import org.bonitasoft.engine.search.document.SearchDocumentsSupervisedBy;
import org.bonitasoft.engine.search.flownode.SearchArchivedFlowNodeInstances;
import org.bonitasoft.engine.search.flownode.SearchFlowNodeInstances;
import org.bonitasoft.engine.search.identity.SearchUsersWhoCanStartProcessDeploymentInfo;
import org.bonitasoft.engine.search.impl.SearchFilter;
import org.bonitasoft.engine.search.impl.SearchResultImpl;
import org.bonitasoft.engine.search.process.SearchArchivedProcessInstances;
import org.bonitasoft.engine.search.process.SearchArchivedProcessInstancesInvolvingUser;
import org.bonitasoft.engine.search.process.SearchArchivedProcessInstancesSupervisedBy;
import org.bonitasoft.engine.search.process.SearchArchivedProcessInstancesWithoutSubProcess;
import org.bonitasoft.engine.search.process.SearchOpenProcessInstancesInvolvingUser;
import org.bonitasoft.engine.search.process.SearchOpenProcessInstancesInvolvingUsersManagedBy;
import org.bonitasoft.engine.search.process.SearchOpenProcessInstancesSupervisedBy;
import org.bonitasoft.engine.search.process.SearchProcessDeploymentInfos;
import org.bonitasoft.engine.search.process.SearchProcessDeploymentInfosStartedBy;
import org.bonitasoft.engine.search.process.SearchProcessDeploymentInfosUserCanStart;
import org.bonitasoft.engine.search.process.SearchProcessDeploymentInfosUsersManagedByCanStart;
import org.bonitasoft.engine.search.process.SearchProcessInstances;
import org.bonitasoft.engine.search.process.SearchUncategorizedProcessDeploymentInfos;
import org.bonitasoft.engine.search.process.SearchUncategorizedProcessDeploymentInfosSupervisedBy;
import org.bonitasoft.engine.search.process.SearchUncategorizedProcessDeploymentInfosUserCanStart;
import org.bonitasoft.engine.search.supervisor.SearchArchivedTasksSupervisedBy;
import org.bonitasoft.engine.search.supervisor.SearchAssignedTasksSupervisedBy;
import org.bonitasoft.engine.search.supervisor.SearchProcessDeploymentInfosSupervised;
import org.bonitasoft.engine.search.supervisor.SearchSupervisors;
import org.bonitasoft.engine.search.task.SearchArchivedTasks;
import org.bonitasoft.engine.search.task.SearchArchivedTasksManagedBy;
import org.bonitasoft.engine.search.task.SearchAssignedTaskManagedBy;
import org.bonitasoft.engine.search.task.SearchHumanTaskInstances;
import org.bonitasoft.engine.search.task.SearchPendingHiddenTasks;
import org.bonitasoft.engine.search.task.SearchPendingTasksForUser;
import org.bonitasoft.engine.search.task.SearchPendingTasksManagedBy;
import org.bonitasoft.engine.search.task.SearchPendingTasksSupervisedBy;
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
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisor;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisorBuilder;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisorBuilders;
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.xml.Parser;
import org.bonitasoft.engine.xml.XMLWriter;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 * @author Zhao Na
 * @author Zhang Bole
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 * @author Arthur Freycon
 */
public class ProcessAPIImpl implements ProcessAPI {

    private static final String CONTAINER_TYPE_PROCESS_INSTANCE = "PROCESS_INSTANCE";

    private static final String CONTAINER_TYPE_ACTIVITY_INSTANCE = "ACTIVITY_INSTANCE";

    private static TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static PlatformServiceAccessor getPlatformServiceAccessor() {
        try {
            return ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SearchResult<HumanTaskInstance> searchHumanTaskInstances(final SearchOptions searchOptions) throws SearchException {
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
    public void deleteProcess(final long processDefinitionId) throws DeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        // multiple tx here because we must lock instances when deleting them
        // but if the second tx crash we can relaunch deleteprocess without issues
        try {
            deleteProcessInstancesFromProcessDefinition(processDefinitionId, tenantAccessor);
        } catch (final SProcessInstanceHierarchicalDeletionException e) {
            throw new ProcessInstanceHierarchicalDeletionException(e.getMessage(), e.getProcessInstanceId());
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
        } catch (final SearchException e) {
            throw new DeletionException(e);
        }

        // 1 tx for the rest
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
                final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
                final DeleteProcess deleteProcess = new DeleteProcess(processDefinitionService, processDefinition, processInstanceService,
                        tenantAccessor.getArchiveService(), actorMappingService);
                transactionExecutor.execute(deleteProcess);
                final String processesFolder = BonitaHomeServer.getInstance().getProcessesFolder(tenantAccessor.getTenantId());
                final File file = new File(processesFolder);
                if (!file.exists()) {
                    file.mkdir();
                }

                final File processFolder = new File(file, String.valueOf(processDefinition.getId()));
                IOUtil.deleteDir(processFolder);
            } catch (final BonitaHomeNotSetException e) {
                transactionExecutor.setTransactionRollback();
                throw new DeletionException(e);
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new DeletionException(e);
            } catch (final IOException e) {
                transactionExecutor.setTransactionRollback();
                throw new DeletionException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new DeletionException(e);
        }
    }

    protected void deleteProcessInstancesFromProcessDefinition(final long processDefinitionId, final TenantServiceAccessor tenantAccessor)
            throws SBonitaException, SearchException, SProcessInstanceHierarchicalDeletionException {
        List<ProcessInstance> processInstances;
        final int maxResults = 100;
        do {
            processInstances = searchProcessInstancesFromProcessDefinition(tenantAccessor, processDefinitionId, maxResults);
            final List<Long> processInstanceIds = new ArrayList<Long>(processInstances.size());
            for (final ProcessInstance processInstance : processInstances) {
                processInstanceIds.add(processInstance.getId());
            }
            if (processInstanceIds.size() > 0) {
                deleteProcessInstancesInsideLocks(tenantAccessor, true, processInstanceIds.toArray(new Long[processInstanceIds.size()]));
            }
        } while (!processInstances.isEmpty());
    }

    private void deleteProcessInstancesInsideLocks(final TenantServiceAccessor tenantAccessor, final boolean ignoreProcessInstanceNotFound,
            final Long... processInstanceIds) throws SBonitaException, SProcessInstanceHierarchicalDeletionException {
        final LockService lockService = tenantAccessor.getLockService();
        final String objectType = SFlowElementsContainerType.PROCESS.name();
        final List<Long> lockedProcesses = new ArrayList<Long>();
        try {
            createLocks(lockService, objectType, lockedProcesses, processInstanceIds);
            deleteProcessInstancesInTransaction(tenantAccessor, ignoreProcessInstanceNotFound, processInstanceIds);
        } finally {
            final List<Long> unReleasedLocks = releaseLocks(tenantAccessor, lockService, objectType, lockedProcesses);
            if (!unReleasedLocks.isEmpty()) {
                throw new SLoginException("Some locks were not released. Object type: " + objectType + ", ids: " + unReleasedLocks);
            }
        }
    }

    private List<Long> releaseLocks(final TenantServiceAccessor tenantAccessor, final LockService lockService, final String objectType,
            final List<Long> lockedProcesses) {
        final List<Long> unReleasedLocks = new ArrayList<Long>(1);
        for (final Long lockedProcessId : lockedProcesses) {
            try {
                lockService.releaseExclusiveLockAccess(lockedProcessId, objectType);
            } catch (final SLockException e) {
                unReleasedLocks.add(lockedProcessId);
                log(tenantAccessor, e);
            }
        }
        return unReleasedLocks;
    }

    private void createLocks(final LockService lockService, final String objectType, final List<Long> lockedProcesses, final Long... processInstanceIds)
            throws SLockException {
        for (final Long processInstanceId : processInstanceIds) {
            lockService.createExclusiveLockAccess(processInstanceId, objectType);
            lockedProcesses.add(processInstanceId);
        }
    }

    private void deleteProcessInstancesInTransaction(final TenantServiceAccessor tenantAccessor, final boolean ignoreProcessInstanceNotFound,
            final Long... processInstanceIds) throws SBonitaException, SProcessInstanceHierarchicalDeletionException {
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final TechnicalLoggerService logger = tenantAccessor.getTechnicalLoggerService();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();

        final boolean txOpened = transactionExecutor.openTransaction();
        try {
            deleteProcessInstances(processInstanceService, logger, archiveService, ignoreProcessInstanceNotFound, activityInstanceService, processInstanceIds);
        } catch (final SBonitaException e) {
            transactionExecutor.setTransactionRollback();
            throw e;
        } finally {
            transactionExecutor.completeTransaction(txOpened);
        }
    }

    private void deleteProcessInstances(final ProcessInstanceService processInstanceService, final TechnicalLoggerService logger,
            final ArchiveService archiveService, final boolean ignoreProcessInstanceNotFound, final ActivityInstanceService activityInstanceService,
            final Long... processInstanceIds) throws SBonitaException, SProcessInstanceHierarchicalDeletionException {
        for (final Long processInstanceId : processInstanceIds) {
            if (ignoreProcessInstanceNotFound) {
                try {
                    deleteProcessInstance(processInstanceService, processInstanceId, archiveService, activityInstanceService);
                } catch (final SProcessInstanceNotFoundException e) {
                    if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.WARNING)) {
                        logger.log(this.getClass(), TechnicalLogSeverity.WARNING, e.getMessage() + ". It has probably completed.");
                    }
                }
            } else {
                deleteProcessInstance(processInstanceService, processInstanceId, archiveService, activityInstanceService);
            }
        }
    }

    private void deleteProcessInstance(final ProcessInstanceService processInstanceService, final Long processInstanceId, final ArchiveService archiveService,
            final ActivityInstanceService activityInstanceService) throws SBonitaException, SProcessInstanceHierarchicalDeletionException {
        final ReadPersistenceService archivePersistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final SProcessInstance processInstance = processInstanceService.getProcessInstance(processInstanceId);
        final long callerId = processInstance.getCallerId();
        if (callerId > 0) {
            try {
                final SFlowNodeInstance flowNodeInstance = activityInstanceService.getFlowNodeInstance(callerId);
                throw new SProcessInstanceHierarchicalDeletionException("Unable to delete the process instance because the parent is still active: activity "
                        + flowNodeInstance.getName() + " with id " + flowNodeInstance.getId(), flowNodeInstance.getRootProcessInstanceId());
            } catch (final SFlowNodeNotFoundException e) {
                // ok the activity that called this process do not exists anymore
            }
        }
        if (archivePersistenceService != null) {
            processInstanceService.deleteArchivedProcessInstanceElements(processInstanceId, processInstance.getProcessDefinitionId());
        }
        processInstanceService.deleteProcessInstance(processInstance);
    }

    private List<ProcessInstance> searchProcessInstancesFromProcessDefinition(final TenantServiceAccessor tenantAccessor, final long processDefinitionId,
            final int maxResults) throws SProcessInstanceReadException, STransactionException, SearchException {
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, maxResults);
        searchOptionsBuilder.filter(ProcessInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDefinitionId);
        // Order by caller id ASC because we need to have parent process deleted before their sub processes
        searchOptionsBuilder.sort(ProcessInstanceSearchDescriptor.CALLER_ID, Order.ASC);
        return searchProcessInstances(tenantAccessor, searchOptionsBuilder.done()).getResult();
    }

    @Override
    public void deleteProcesses(final List<Long> processIdList) throws DeletionException {
        for (final Long processId : processIdList) {
            deleteProcess(processId);
        }
    }

    @Override
    public ProcessDefinition deployAndEnableProcess(final DesignProcessDefinition designProcessDefinition) throws ProcessDeployException,
            ProcessEnablementException, AlreadyExistsException, InvalidProcessDefinitionException {
        BusinessArchive businessArchive;
        try {
            businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        } catch (final InvalidBusinessArchiveFormatException ibafe) {
            throw new InvalidProcessDefinitionException(ibafe.getMessage());
        }
        return deployAndEnableProcess(businessArchive);
    }

    @Override
    public ProcessDefinition deployAndEnableProcess(final BusinessArchive businessArchive) throws ProcessDeployException, ProcessEnablementException,
            AlreadyExistsException {
        final ProcessDefinition processDefinition = deploy(businessArchive);
        try {
            enableProcess(processDefinition.getId());
        } catch (final ProcessDefinitionNotFoundException pdnfe) {
            throw new ProcessEnablementException(pdnfe.getMessage());
        }
        return processDefinition;
    }

    @Override
    public ProcessDefinition deploy(final DesignProcessDefinition designProcessDefinition) throws AlreadyExistsException, ProcessDeployException {
        try {
            final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition)
                    .done();
            return deploy(businessArchive);
        } catch (final InvalidBusinessArchiveFormatException ibafe) {
            throw new ProcessDeployException(ibafe);
        }
    }

    @Override
    public ProcessDefinition deploy(final BusinessArchive businessArchive) throws ProcessDeployException, AlreadyExistsException {
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
        final SProcessDefinition sDefinition = bpmDefinitionBuilders.getProcessDefinitionBuilder()
                .createNewInstance(processDefinition, sExpressionBuilders, sDataDefinitionBuilders, sOperationBuilders).done();

        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                try {
                    processDefinitionService.getProcessDefinitionId(processDefinition.getName(), processDefinition.getVersion());
                    throw new AlreadyExistsException("The process " + processDefinition.getName() + " in version " + processDefinition.getVersion()
                            + " already exists.");
                } catch (final SProcessDefinitionReadException e) {
                    // ok
                }
                processDefinitionService.store(sDefinition, processDefinition.getDisplayName(), processDefinition.getDisplayDescription());
                unzipBar(businessArchive, sDefinition, tenantAccessor.getTenantId());// TODO first unzip in temp folder
                // TODO refactor this to avoid using transaction executor inside
                final boolean isResolved = tenantAccessor.getDependencyResolver().resolveDependencies(this, businessArchive, tenantAccessor, sDefinition);
                if (isResolved) {
                    tenantAccessor.getDependencyResolver().resolveAndCreateDependencies(businessArchive, processDefinitionService, dependencyService,
                            dependencyBuilderAccessor, sDefinition);
                }
            } catch (final BonitaHomeNotSetException e) {
                transactionExecutor.setTransactionRollback();
                throw new ProcessDeployException(e);
            } catch (final IOException e) {
                transactionExecutor.setTransactionRollback();
                throw new ProcessDeployException(e);
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new ProcessDeployException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new ProcessDeployException(e);
        }
        return ModelConvertor.toProcessDefinition(sDefinition);
    }

    @Override
    public void importActorMapping(final long pDefinitionId, final byte[] actorMappingXML) throws ActorMappingImportException {
        if (actorMappingXML != null) {
            final String actorMapping = new String(actorMappingXML, Charset.forName("UTF-8"));
            importActorMapping(pDefinitionId, actorMapping);
        }
    }

    @Override
    // TODO delete files after use/if an exception occurs
    public byte[] exportBarProcessContentUnderHome(final long processDefinitionId) throws ProcessExportException {
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

        // export actormapping
        try {

            final File actormappF = new File(processFolder.getPath(), "actorMapping.xml");
            if (!actormappF.exists()) {
                actormappF.createNewFile();
            }
            String xmlcontent = "";
            try {
                xmlcontent = exportActorMapping(processDefinitionId);
            } catch (final ActorMappingExportException e) {
                throw new ProcessExportException(e);
            }
            IOUtil.write(actormappF, xmlcontent);

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ZipOutputStream zos = new ZipOutputStream(baos);
            try {
                IOUtil.zipDir(processFolder.getPath(), zos, processFolder.getPath());
                return baos.toByteArray();
            } finally {
                zos.close();
            }
        } catch (final IOException e) {
            throw new ProcessExportException(e);
        }
    }

    protected void unzipBar(final BusinessArchive businessArchive, final SProcessDefinition sDefinition, final long tenantId) throws BonitaHomeNotSetException,
            IOException {
        final String processesFolder = BonitaHomeServer.getInstance().getProcessesFolder(tenantId);
        final File file = new File(processesFolder);
        if (!file.exists()) {
            file.mkdirs();
        }
        final File processFolder = new File(file, String.valueOf(sDefinition.getId()));
        BusinessArchiveFactory.writeBusinessArchiveToFolder(businessArchive, processFolder);
    }

    @Override
    public void disableAndDelete(final long processDefinitionId) throws ProcessDefinitionNotFoundException, ProcessActivationException, DeletionException {
        disableProcess(processDefinitionId);
        deleteProcess(processDefinitionId);
    }

    @Override
    public void disableProcess(final long processId) throws ProcessDefinitionNotFoundException, ProcessActivationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final PlatformServiceAccessor platformServiceAccessor = getPlatformServiceAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final EventInstanceService eventInstanceService = tenantAccessor.getEventInstanceService();
        final SchedulerService schedulerService = platformServiceAccessor.getSchedulerService();
        final TransactionContent transactionContent = new DisableProcess(processDefinitionService, processId, eventInstanceService, schedulerService);
        try {
            transactionExecutor.execute(transactionContent);
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SBonitaException e) {
            throw new ProcessActivationException(e);
        }
    }

    @Override
    public void enableProcess(final long processId) throws ProcessDefinitionNotFoundException, ProcessEnablementException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final EventsHandler eventsHandler = tenantAccessor.getEventsHandler();
        try {
            final EnableProcess enableProcess = new EnableProcess(processDefinitionService, processId, eventsHandler);
            transactionExecutor.execute(enableProcess);
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SBonitaException sbe) {
            throw new ProcessEnablementException(sbe);
        } catch (final Exception e) {
            throw new ProcessEnablementException(e);
        }
    }

    @Override
    public void executeFlowNode(final long flownodeInstanceId) throws FlowNodeExecutionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessExecutor processExecutor = tenantAccessor.getProcessExecutor();
        try {
            processExecutor.executeActivity(flownodeInstanceId, getUserIdFromSession());
        } catch (final SFlowNodeExecutionException e) {
            throw new FlowNodeExecutionException(e);
        } catch (final SActivityInterruptedException e) {
            throw new ActivityExecutionException(e);
        } catch (final SActivityReadException e) {
            throw new ActivityExecutionException(e);
        }
    }

    @Override
    public List<ActivityInstance> getActivities(final long processInstanceId, final int startIndex, final int maxResults) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final GetActivities getActivityInstances = new GetActivities(processInstanceId, startIndex, maxResults, activityInstanceService);
        try {
            transactionExecutor.execute(getActivityInstances);
            final List<SActivityInstance> result = getActivityInstances.getResult();
            return ModelConvertor.toActivityInstances(result, flowNodeStateManager);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public long getNumberOfProcessDeploymentInfos() {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final TransactionContentWithResult<Long> transactionContentWithResult = new GetNumberOfProcessDeploymentInfos(processDefinitionService);
        try {
            transactionExecutor.execute(transactionContentWithResult);
            return transactionContentWithResult.getResult();
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public ProcessDefinition getProcessDefinition(final long processId) throws ProcessDefinitionNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final SProcessDefinition sProcessDefinition = processDefinitionService.getProcessDefinition(processId);
                return ModelConvertor.toProcessDefinition(sProcessDefinition);
            } catch (final SProcessDefinitionNotFoundException e) {
                throw new ProcessDefinitionNotFoundException(e);
            } catch (final SProcessDefinitionReadException e) {
                throw new RetrieveException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public ProcessDeploymentInfo getProcessDeploymentInfo(final long processDefinitionId) throws ProcessDefinitionNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final TransactionContentWithResult<SProcessDefinitionDeployInfo> transactionContentWithResult = new GetProcessDefinitionDeployInfo(
                    processDefinitionId, processDefinitionService);
            transactionExecutor.execute(transactionContentWithResult);
            return ModelConvertor.toProcessDeploymentInfo(transactionContentWithResult.getResult());
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    private void log(final TenantServiceAccessor tenantAccessor, final Exception e) {
        final TechnicalLoggerService logger = tenantAccessor.getTechnicalLoggerService();
        logger.log(this.getClass(), TechnicalLogSeverity.ERROR, e);
    }

    @Override
    public ProcessInstance getProcessInstance(final long processInstanceId) throws ProcessInstanceNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SearchProcessInstanceDescriptor searchProcessInstanceDescriptor = tenantAccessor.getSearchEntitiesDescriptor().getProcessInstanceDescriptor();

        try {
            final GetProcessInstance getProcessInstance = new GetProcessInstance(processInstanceService, processDefinitionService,
                    searchProcessInstanceDescriptor, processInstanceId);
            transactionExecutor.execute(getProcessInstance);
            return getProcessInstance.getResult();
        } catch (final SProcessInstanceNotFoundException notFound) {
            throw new ProcessInstanceNotFoundException(notFound);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public List<ArchivedProcessInstance> getArchivedProcessInstances(final long processInstanceId, final int startIndex, final int maxResults) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ReadPersistenceService persistenceService = tenantAccessor.getArchiveService().getDefinitiveArchiveReadPersistenceService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final GetArchivedProcessInstanceList getProcessInstanceList = new GetArchivedProcessInstanceList(processInstanceService, persistenceService,
                searchEntitiesDescriptor, processInstanceId, startIndex, maxResults);
        try {
            transactionExecutor.execute(getProcessInstanceList);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new RetrieveException(e);
        }
        return getProcessInstanceList.getResult();
    }

    @Override
    public ArchivedProcessInstance getArchivedProcessInstance(final long id) throws ArchivedProcessInstanceNotFoundException, RetrieveException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final SAProcessInstance archivedProcessInstance = processInstanceService.getArchivedProcessInstance(id, persistenceService);
                return ModelConvertor.toArchivedProcessInstance(archivedProcessInstance);
            } catch (final SProcessInstanceNotFoundException e) {
                throw new ArchivedProcessInstanceNotFoundException(e);
            } catch (final SBonitaException e) {
                throw new RetrieveException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e1) {
            throw new RetrieveException(e1);
        }
    }

    @Override
    public ArchivedProcessInstance getFinalArchivedProcessInstance(final long processInstanceId) throws ArchivedProcessInstanceNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ReadPersistenceService readPersistenceService = tenantAccessor.getArchiveService().getDefinitiveArchiveReadPersistenceService();

        final GetLastArchivedProcessInstance getProcessInstance = new GetLastArchivedProcessInstance(processInstanceService, processInstanceId,
                readPersistenceService, tenantAccessor.getSearchEntitiesDescriptor());
        try {
            transactionExecutor.execute(getProcessInstance);
        } catch (final SProcessInstanceNotFoundException e) {
            log(tenantAccessor, e);
            throw new ArchivedProcessInstanceNotFoundException(e);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new RetrieveException(e);
        }
        return getProcessInstance.getResult();
    }

    @Override
    public ProcessInstance startProcess(final long processDefinitionId) throws ProcessDefinitionNotFoundException, ProcessActivationException,
            ProcessExecutionException {
        try {
            return startProcess(getUserIdFromSession(), processDefinitionId);
        } catch (final UserNotFoundException e) {
            throw new ProcessExecutionException(e);
        } catch (final ProcessDefinitionNotFoundException e) {
            throw new ProcessExecutionException(e);
        }
    }

    @Override
    public ProcessInstance startProcess(final long userId, final long processDefinitionId) throws UserNotFoundException, ProcessDefinitionNotFoundException,
            ProcessExecutionException, ProcessActivationException {
        return startProcess(userId, processDefinitionId, null, null);
    }

    @Override
    public int getNumberOfActors(final long processDefinitionId) throws ProcessDefinitionNotFoundException {
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
    public List<ActorInstance> getActors(final long processDefinitionId, final int startIndex, final int maxResults, final ActorCriterion sort) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        try {
            final GetActorsByPagination getActorsByPaging = new GetActorsByPagination(actorMappingService, processDefinitionId, startIndex, maxResults, sort);
            transactionExecutor.execute(getActorsByPaging);
            return ModelConvertor.toActors(getActorsByPaging.getResult());
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public List<ActorMember> getActorMembers(final long actorId, final int startIndex, final int maxResults) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final GetActorMembers getActorMembers = new GetActorMembers(actorMappingService, actorId, startIndex, maxResults);
                getActorMembers.execute();
                return ModelConvertor.toActorMembers(getActorMembers.getResult());
            } catch (final SBonitaException e) {
                // no rollback, read only tx
                throw new RetrieveException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new RetrieveException(e);// TODO refactor exceptions
        }

    }

    @Override
    public long getNumberOfActorMembers(final long actorId) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final GetNumberOfActorMembers numberOfActorMembers = new GetNumberOfActorMembers(actorMappingService, actorId);
        try {
            transactionExecutor.execute(numberOfActorMembers);
            return numberOfActorMembers.getResult();
        } catch (final SBonitaException sbe) {
            return 0; // FIXME throw retrieve exception
        }
    }

    @Override
    public long getNumberOfUsersOfActor(final long actorId) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final GetNumberOfUsersOfActor numberOfUsersOfActor = new GetNumberOfUsersOfActor(actorMappingService, actorId);
        try {
            transactionExecutor.execute(numberOfUsersOfActor);
            return numberOfUsersOfActor.getResult();
        } catch (final SBonitaException sbe) {
            return 0; // FIXME throw retrieve exception
        }
    }

    @Override
    public long getNumberOfRolesOfActor(final long actorId) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final GetNumberOfRolesOfActor numberOfRolesOfActor = new GetNumberOfRolesOfActor(actorMappingService, actorId);
        try {
            transactionExecutor.execute(numberOfRolesOfActor);
            return numberOfRolesOfActor.getResult();
        } catch (final SBonitaException sbe) {
            return 0; // FIXME throw retrieve exception
        }
    }

    @Override
    public long getNumberOfGroupsOfActor(final long actorId) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final GetNumberOfGroupsOfActor numberOfGroupsOfActor = new GetNumberOfGroupsOfActor(actorMappingService, actorId);
        try {
            transactionExecutor.execute(numberOfGroupsOfActor);
            return numberOfGroupsOfActor.getResult();
        } catch (final SBonitaException sbe) {
            return 0; // FIXME throw retrieve exception
        }
    }

    @Override
    public long getNumberOfMembershipsOfActor(final long actorId) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final GetNumberOfMembershipsOfActor getNumber = new GetNumberOfMembershipsOfActor(actorMappingService, actorId);
        try {
            transactionExecutor.execute(getNumber);
            return getNumber.getResult();
        } catch (final SBonitaException sbe) {
            return 0; // FIXME throw retrieve exception
        }
    }

    @Override
    public ActorInstance updateActor(final long actorId, final ActorUpdater descriptor) throws ActorNotFoundException, UpdateException {
        if (descriptor == null || descriptor.getFields().isEmpty()) {
            throw new UpdateException("The update descriptor does not contain field updates");
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final SActorUpdateBuilder actorUpdateBuilder = tenantAccessor.getSActorBuilders().getSActorUpdateBuilder();
        final Map<ActorField, Serializable> fields = descriptor.getFields();
        for (final Entry<ActorField, Serializable> field : fields.entrySet()) {
            switch (field.getKey()) {
                case DISPLAY_NAME:
                    actorUpdateBuilder.updateDisplayName((String) field.getValue());
                    break;
                case DESCRIPTION:
                    actorUpdateBuilder.updateDescription((String) field.getValue());
                    break;
                default:
                    break;
            }
        }
        final EntityUpdateDescriptor updateDescriptor = actorUpdateBuilder.done();
        SActor updateActor;
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                updateActor = actorMappingService.updateActor(actorId, updateDescriptor);
                return ModelConvertor.toActorInstance(updateActor);
            } catch (final SActorNotFoundException e) {
                transactionExecutor.setTransactionRollback();
                throw new ActorNotFoundException(e);
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new UpdateException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new UpdateException(e);
        }
    }

    @Override
    public ActorMember addUserToActor(final long actorId, final long userId) throws CreationException, AlreadyExistsException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        ActorMember clientActorMember;
        long processDefinitionId;
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final SActorMember actorMember = actorMappingService.addUserToActor(actorId, userId);
                processDefinitionId = actorMappingService.getActor(actorId).getScopeId();
                clientActorMember = ModelConvertor.toActorMember(actorMember);
            } catch (final SBonitaException sbe) {
                throw new CreationException(sbe);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new CreationException(e);
        }
        try {
            tenantAccessor.getDependencyResolver().resolveDependencies(processDefinitionId, tenantAccessor);
        } catch (final SBonitaException e) {
            throw new CreationException(e);
        }
        return clientActorMember;
    }

    @Override
    public ActorMember addUserToActor(final String actorName, final ProcessDefinition processDefinition, final long userId) throws CreationException,
            AlreadyExistsException, ActorNotFoundException {
        final List<ActorInstance> actors = getActors(processDefinition.getId(), 0, Integer.MAX_VALUE, ActorCriterion.NAME_ASC);
        for (final ActorInstance ai : actors) {
            if (actorName.equals(ai.getName())) {
                return addUserToActor(ai.getId(), userId);
            }
        }
        throw new ActorNotFoundException("Actor " + actorName + " not found in process definition " + processDefinition.getName());
    }

    @Override
    public ActorMember addGroupToActor(final long actorId, final long groupId) throws CreationException, AlreadyExistsException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        ActorMember clientActorMember;
        long processDefinitionId;
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final SActorMember actorMember = actorMappingService.addGroupToActor(actorId, groupId);
                processDefinitionId = actorMappingService.getActor(actorId).getScopeId();
                clientActorMember = ModelConvertor.toActorMember(actorMember);
                transactionExecutor.completeTransaction(txOpened);
            } catch (final SBonitaException sbe) {
                transactionExecutor.setTransactionRollback();
                transactionExecutor.completeTransaction(txOpened);
                throw checkAlreadyExistingGroupMapping(actorId, groupId, transactionExecutor, actorMappingService, sbe);
            }
        } catch (final STransactionException e) {
            // Here, there has been an error on creating the mapping:
            throw checkAlreadyExistingGroupMapping(actorId, groupId, transactionExecutor, actorMappingService, e);
        }
        try {
            tenantAccessor.getDependencyResolver().resolveDependencies(processDefinitionId, tenantAccessor);
        } catch (final SBonitaException e4) {
            throw new CreationException(e4);
        }
        return clientActorMember;
    }

    @Override
    public ActorMember addGroupToActor(final String actorName, final long groupId, final ProcessDefinition processDefinition) throws CreationException,
            AlreadyExistsException, ActorNotFoundException {
        final List<ActorInstance> actors = getActors(processDefinition.getId(), 0, Integer.MAX_VALUE, ActorCriterion.NAME_ASC);
        for (final ActorInstance ai : actors) {
            if (actorName.equals(ai.getName())) {
                return addGroupToActor(ai.getId(), groupId);
            }
        }
        throw new ActorNotFoundException("Actor " + actorName + " not found in process definition " + processDefinition.getName());
    }

    private CreationException checkAlreadyExistingGroupMapping(final long actorId, final long groupId, final TransactionExecutor transactionExecutor,
            final ActorMappingService actorMappingService, final SBonitaException e) throws AlreadyExistsException, CreationException {
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                List<SActorMember> actorMembersOfGroup;
                int startIndex = 0;
                do {
                    actorMembersOfGroup = actorMappingService.getActorMembers(actorId, startIndex, 50);
                    for (final SActorMember sActorMember : actorMembersOfGroup) {
                        if (sActorMember.getGroupId() == groupId && sActorMember.getRoleId() == -1 && sActorMember.getUserId() == -1) {
                            throw new AlreadyExistsException("This group / actor mapping already exists");
                        }
                    }
                    startIndex += 50;
                } while (actorMembersOfGroup.size() > 0);
            } catch (final SBonitaReadException e2) {
                transactionExecutor.setTransactionRollback();
                throw new CreationException("Read problem when checking for duplicate actor member", e2);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
            return new CreationException(e);
        } catch (final STransactionException e3) {
            throw new CreationException(e);
        }
    }

    @Override
    public ActorMember addRoleToActor(final long actorId, final long roleId) throws CreationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        ActorMember clientActorMember;
        long processDefinitionId;
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final SActorMember actorMember = actorMappingService.addRoleToActor(actorId, roleId);
                processDefinitionId = actorMappingService.getActor(actorId).getScopeId();
                clientActorMember = ModelConvertor.toActorMember(actorMember);
            } catch (final SBonitaException sbe) {
                throw new CreationException(sbe);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new CreationException(e);
        }
        try {
            tenantAccessor.getDependencyResolver().resolveDependencies(processDefinitionId, tenantAccessor);
        } catch (final SBonitaException e) {
            throw new CreationException(e);
        }
        return clientActorMember;
    }

    @Override
    public ActorMember addRoleToActor(final String actorName, final ProcessDefinition processDefinition, final long roleId) throws ActorNotFoundException,
            CreationException {
        final List<ActorInstance> actors = getActors(processDefinition.getId(), 0, Integer.MAX_VALUE, ActorCriterion.NAME_ASC);
        for (final ActorInstance ai : actors) {
            if (actorName.equals(ai.getName())) {
                return addRoleToActor(ai.getId(), roleId);
            }
        }
        throw new ActorNotFoundException("Actor " + actorName + " not found in process definition " + processDefinition.getName());
    }

    @Override
    public ActorMember addRoleAndGroupToActor(final String actorName, final ProcessDefinition processDefinition, final long roleId, final long groupId)
            throws ActorNotFoundException, CreationException {
        final List<ActorInstance> actors = getActors(processDefinition.getId(), 0, Integer.MAX_VALUE, ActorCriterion.NAME_ASC);
        for (final ActorInstance ai : actors) {
            if (actorName.equals(ai.getName())) {
                return addRoleAndGroupToActor(ai.getId(), roleId, groupId);
            }
        }
        throw new ActorNotFoundException("Actor " + actorName + " not found in process definition " + processDefinition.getName());
    }

    @Override
    public ActorMember addRoleAndGroupToActor(final long actorId, final long roleId, final long groupId) throws CreationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        ActorMember clientActorMember;
        long processDefinitionId;
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final SActorMember actorMember = actorMappingService.addRoleAndGroupToActor(actorId, roleId, groupId);
                processDefinitionId = actorMappingService.getActor(actorId).getScopeId();
                clientActorMember = ModelConvertor.toActorMember(actorMember);
            } catch (final SBonitaException sbe) {
                throw new CreationException(sbe);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new CreationException(e);
        }
        try {
            tenantAccessor.getDependencyResolver().resolveDependencies(processDefinitionId, tenantAccessor);
        } catch (final SBonitaException e) {
            throw new CreationException(e);
        }
        return clientActorMember;
    }

    @Override
    public void removeActorMember(final long actorMemberId) throws DeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final RemoveActorMember removeActorMember = new RemoveActorMember(actorMappingService, actorMemberId);
        // FIXME remove an actor member when process is running!
        try {
            transactionExecutor.execute(removeActorMember);
            final SActorMember actorMember = removeActorMember.getResult();
            final long processDefinitionId = getActor(actorMember.getActorId()).getProcessDefinitionId();
            tenantAccessor.getDependencyResolver().resolveDependencies(processDefinitionId, tenantAccessor);
        } catch (final SBonitaException sbe) {
            throw new DeletionException(sbe);
        } catch (final ActorNotFoundException e) {
            throw new DeletionException(e);
        }
    }

    @Override
    public ActorInstance getActor(final long actorId) throws ActorNotFoundException {
        try {
            final TenantServiceAccessor tenantAccessor = getTenantAccessor();
            final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
            final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
            final GetActor getActor = new GetActor(actorMappingService, actorId);
            transactionExecutor.execute(getActor);
            return ModelConvertor.toActorInstance(getActor.getResult());
        } catch (final SBonitaException e) {
            throw new ActorNotFoundException(e);
        }
    }

    @Override
    public ActivityInstance getActivityInstance(final long activityInstanceId) throws ActivityInstanceNotFoundException {
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
            throw new RetrieveException(e);
        }
        return ModelConvertor.toActivityInstance(getActivityInstance.getResult(), flowNodeStateManager);
    }

    @Override
    public FlowNodeInstance getFlowNodeInstance(final long flowNodeInstanceId) throws FlowNodeInstanceNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final GetFlowNodeInstance getFlowNodeInstance = new GetFlowNodeInstance(activityInstanceService, flowNodeInstanceId);
        try {
            transactionExecutor.execute(getFlowNodeInstance);
        } catch (final SActivityInstanceNotFoundException e) {
            throw new FlowNodeInstanceNotFoundException(e);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
        return ModelConvertor.toFlowNodeInstance(getFlowNodeInstance.getResult(), flowNodeStateManager);
    }

    @Override
    public List<HumanTaskInstance> getAssignedHumanTaskInstances(final long userId, final int startIndex, final int maxResults,
            final ActivityInstanceCriterion pagingCriterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final SUserTaskInstanceBuilder modelBuilder = tenantAccessor.getBPMInstanceBuilders().getUserTaskInstanceBuilder();
        final OrderAndField orderAndField = OrderAndFields.getOrderAndFieldForActivityInstance(pagingCriterion, modelBuilder);
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActivityInstanceService instanceService = tenantAccessor.getActivityInstanceService();
        try {
            final GetAssignedTasks getAssignedTasks = new GetAssignedTasks(instanceService, userId, startIndex, maxResults, orderAndField.getField(),
                    orderAndField.getOrder());
            transactionExecutor.execute(getAssignedTasks);
            final List<SHumanTaskInstance> assignedTasks = getAssignedTasks.getResult();
            return ModelConvertor.toHumanTaskInstances(assignedTasks, flowNodeStateManager);
        } catch (final SUserNotFoundException e) {
            return Collections.emptyList();
        } catch (final SBonitaException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<HumanTaskInstance> getPendingHumanTaskInstances(final long userId, final int startIndex, final int maxResults,
            final ActivityInstanceCriterion pagingCriterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final SUserTaskInstanceBuilder modelBuilder = tenantAccessor.getBPMInstanceBuilders().getUserTaskInstanceBuilder();
        final OrderAndField orderAndField = OrderAndFields.getOrderAndFieldForActivityInstance(pagingCriterion, modelBuilder);
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService definitionService = tenantAccessor.getProcessDefinitionService();
        final ActivityInstanceService instanceService = tenantAccessor.getActivityInstanceService();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final Set<Long> actorIds = getActorsForUser(userId, actorMappingService, definitionService);
                final List<SHumanTaskInstance> pendingTasks = instanceService.getPendingTasks(userId, actorIds, startIndex, maxResults,
                        orderAndField.getField(), orderAndField.getOrder());
                return ModelConvertor.toHumanTaskInstances(pendingTasks, flowNodeStateManager);
            } catch (final SBonitaException e) {
                return Collections.emptyList();
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            return Collections.emptyList();
        }
    }

    private Set<Long> getActorsForUser(final long userId, final ActorMappingService actorMappingService, final ProcessDefinitionService definitionService)
            throws SBonitaReadException, SProcessDefinitionReadException {
        final long numberOfProcesses = definitionService.getNumberOfProcessDeploymentInfo(ActivationState.ENABLED);
        final List<Long> processDefIds = definitionService.getProcessDefinitionIds(ActivationState.ENABLED, 0, numberOfProcesses);// FIXME dirty....
        final HashSet<Long> processDefinitionIds = new HashSet<Long>(processDefIds);
        if (processDefinitionIds.isEmpty()) {
            return Collections.emptySet();
        }
        final List<SActor> actors = actorMappingService.getActors(processDefinitionIds, userId);
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
    public ArchivedActivityInstance getArchivedActivityInstance(final long activityInstanceId) throws ActivityInstanceNotFoundException {
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
            throw new ActivityInstanceNotFoundException(activityInstanceId, e);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
        return ModelConvertor.toArchivedActivityInstance(getActivityInstance.getResult(), flowNodeStateManager);
    }

    @Override
    public ArchivedFlowNodeInstance getArchivedFlowNodeInstance(final long archivedFlowNodeInstanceId) throws ArchivedFlowNodeInstanceNotFoundException,
            RetrieveException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();

        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final SAFlowNodeInstance archivedFlowNodeInstance = activityInstanceService.getArchivedFlowNodeInstance(archivedFlowNodeInstanceId,
                        persistenceService);
                return ModelConvertor.toArchivedFlowNodeInstance(archivedFlowNodeInstance, flowNodeStateManager);
            } catch (final SFlowNodeNotFoundException e) {
                throw new ArchivedFlowNodeInstanceNotFoundException(archivedFlowNodeInstanceId);
            } catch (final SFlowNodeReadException e) {
                throw new RetrieveException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public List<ProcessInstance> getProcessInstances(final int startIndex, final int maxResults, final ProcessInstanceCriterion criterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();

        final SAProcessInstanceBuilder modelBuilder = tenantAccessor.getBPMInstanceBuilders().getSAProcessInstanceBuilder();
        final OrderAndField orderAndField = OrderAndFields.getOrderAndFieldForProcessInstance(criterion, modelBuilder);
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(startIndex, maxResults);
        searchOptionsBuilder.sort(orderAndField.getField(), Order.valueOf(orderAndField.getOrder().name()));
        List<ProcessInstance> result;
        try {
            result = searchProcessInstances(tenantAccessor, searchOptionsBuilder.done()).getResult();
        } catch (final SearchException e) {
            result = Collections.emptyList();
        }
        return result;
    }

    @Override
    public long getNumberOfProcessInstances() {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();

        try {
            final TransactionContentWithResult<Long> transactionContent = new GetNumberOfProcessInstance(processInstanceService, processDefinitionService,
                    searchEntitiesDescriptor);
            transactionExecutor.execute(transactionContent);
            return transactionContent.getResult();
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    protected SearchResult<ProcessInstance> searchProcessInstances(final TenantServiceAccessor tenantAccessor, final SearchOptions searchOptions)
            throws SearchException {
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();

        try {
            final SearchProcessInstances searchProcessInstances = new SearchProcessInstances(processInstanceService,
                    searchEntitiesDescriptor.getProcessInstanceDescriptor(), searchOptions, processDefinitionService);
            transactionExecutor.execute(searchProcessInstances);
            return searchProcessInstances.getResult();
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
    }

    @Override
    public List<ArchivedProcessInstance> getArchivedProcessInstances(final int startIndex, final int maxResults, final ProcessInstanceCriterion criterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SAProcessInstanceBuilder modelBuilder = tenantAccessor.getBPMInstanceBuilders().getSAProcessInstanceBuilder();
        final OrderAndField orderAndField = OrderAndFields.getOrderAndFieldForProcessInstance(criterion, modelBuilder);

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(startIndex, maxResults);
        searchOptionsBuilder.sort(orderAndField.getField(), Order.valueOf(orderAndField.getOrder().name()));
        searchOptionsBuilder.filter(ArchivedProcessInstancesSearchDescriptor.STATE_ID, ProcessInstanceState.COMPLETED.getId());
        searchOptionsBuilder.filter(ArchivedProcessInstancesSearchDescriptor.CALLER_ID, -1);
        final SearchArchivedProcessInstances searchArchivedProcessInstances = searchArchivedProcessInstances(tenantAccessor, searchOptionsBuilder.done());
        try {
            transactionExecutor.execute(searchArchivedProcessInstances);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }

        return searchArchivedProcessInstances.getResult().getResult();
    }

    private SearchArchivedProcessInstances searchArchivedProcessInstances(final TenantServiceAccessor tenantAccessor, final SearchOptions searchOptions)
            throws RetrieveException {
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService readPersistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        return new SearchArchivedProcessInstances(processInstanceService, searchEntitiesDescriptor.getArchivedProcessInstancesDescriptor(), searchOptions,
                readPersistenceService);
    }

    @Override
    public long getNumberOfArchivedProcessInstances() {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService readPersistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SAProcessInstanceBuilder saProcessInstanceBuilder = tenantAccessor.getBPMInstanceBuilders().getSAProcessInstanceBuilder();

        try {
            final TransactionContentWithResult<Long> transactionContent = new GetNumberOfArchivedProcessInstance(processInstanceService,
                    readPersistenceService, searchEntitiesDescriptor, saProcessInstanceBuilder);
            transactionExecutor.execute(transactionContent);
            return transactionContent.getResult();
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new RetrieveException(e);
        }
    }

    @Override
    public SearchResult<ArchivedProcessInstance> searchArchivedProcessInstances(final SearchOptions searchOptions) throws RetrieveException, SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SearchArchivedProcessInstancesWithoutSubProcess searchArchivedProcessInstances = new SearchArchivedProcessInstancesWithoutSubProcess(
                processInstanceService, searchEntitiesDescriptor.getArchivedProcessInstancesDescriptor(), searchOptions, persistenceService);
        try {
            transactionExecutor.execute(searchArchivedProcessInstances);
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
        return searchArchivedProcessInstances.getResult();
    }

    @Override
    public List<ActivityInstance> getOpenActivityInstances(final long processInstanceId, final int startIndex, final int maxResults,
            final ActivityInstanceCriterion criterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SUserTaskInstanceBuilder modelBuilder = tenantAccessor.getBPMInstanceBuilders().getSUserTaskInstanceBuilder();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final int totalNumber = activityInstanceService.getNumberOfOpenActivityInstances(processInstanceId);
                // If there are no instances, return an empty list:
                if (totalNumber == 0) {
                    return Collections.<ActivityInstance> emptyList();
                }
                final OrderAndField orderAndField = OrderAndFields.getOrderAndFieldForActivityInstance(criterion, modelBuilder);
                return ModelConvertor.toActivityInstances(
                        activityInstanceService.getOpenActivityInstances(processInstanceId, startIndex, maxResults, orderAndField.getField(),
                                orderAndField.getOrder()), flowNodeStateManager);
            } catch (final SBonitaException e) {
                // no rollback, read only tx
                throw new RetrieveException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public List<ArchivedActivityInstance> getArchivedActivityInstances(final long processInstanceId, final int startIndex, final int maxResults,
            final ActivityInstanceCriterion criterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final List<ArchivedActivityInstance> archivedActivityInstances = getArchivedActivityInstances(processInstanceId, startIndex, maxResults, criterion,
                tenantAccessor, persistenceService);
        return archivedActivityInstances;
    }

    private List<ArchivedActivityInstance> getArchivedActivityInstances(final long processInstanceId, final int pageIndex, final int numberPerPage,
            final ActivityInstanceCriterion pagingCriterion, final TenantServiceAccessor tenantAccessor, final ReadPersistenceService persistenceService)
            throws RetrieveException {
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final SUserTaskInstanceBuilder modelBuilder = getTenantAccessor().getBPMInstanceBuilders().getSUserTaskInstanceBuilder();
        final OrderAndField orderAndField = OrderAndFields.getOrderAndFieldForActivityInstance(pagingCriterion, modelBuilder);
        final GetArchivedActivityInstances getActivityInstances = new GetArchivedActivityInstances(activityInstanceService, processInstanceId,
                persistenceService, pageIndex, numberPerPage, orderAndField.getField(), orderAndField.getOrder());
        try {
            transactionExecutor.execute(getActivityInstances);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new RetrieveException(e);
        }
        return ModelConvertor.toArchivedActivityInstances(getActivityInstances.getResult(), flowNodeStateManager);
    }

    @Override
    public int getNumberOfOpenedActivityInstances(final long processInstanceId) throws ProcessInstanceNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final TransactionContentWithResult<Integer> transactionContentWithResult = new GetNumberOfActivityInstance(processInstanceId, activityInstanceService);
        try {
            transactionExecutor.execute(transactionContentWithResult);
            return transactionContentWithResult.getResult();
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public Category createCategory(final String name, final String description) throws AlreadyExistsException, CreationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final CategoryService categoryService = tenantAccessor.getCategoryService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final CreateCategory createCategory = new CreateCategory(name, description, categoryService);
            transactionExecutor.execute(createCategory);
            return ModelConvertor.toCategory(createCategory.getResult());
        } catch (final SCategoryAlreadyExistsException scaee) {
            throw new AlreadyExistsException(scaee);
        } catch (final SBonitaException e) {
            throw new CreationException("Category create exception!", e);
        }
    }

    @Override
    public Category getCategory(final long categoryId) throws CategoryNotFoundException {
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
    public long getNumberOfCategories() {
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
    public List<Category> getCategories(final int startIndex, final int maxResults, final CategoryCriterion sortCriterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final SCategoryBuilderAccessor modelBuilderAccessor = tenantAccessor.getCategoryModelBuilderAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final CategoryService categoryService = tenantAccessor.getCategoryService();
        String field = null;
        OrderByType order = null;
        switch (sortCriterion) {
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
            final GetCategories getCategories = new GetCategories(startIndex, maxResults, field, categoryService, order);
            transactionExecutor.execute(getCategories);
            return ModelConvertor.toCategories(getCategories.getResult());
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public void addCategoriesToProcess(final long processDefinitionId, final List<Long> categoryIds) throws AlreadyExistsException, CreationException {
        try {
            final CategoryService categoryService = getTenantAccessor().getCategoryService();
            final TransactionExecutor transactionExecutor = getTenantAccessor().getTransactionExecutor();
            final ProcessDefinitionService processDefinitionService = getTenantAccessor().getProcessDefinitionService();
            final List<TransactionContent> transactionContents = new ArrayList<TransactionContent>(categoryIds.size());
            for (final Long categoryId : categoryIds) {
                transactionContents.add(new AddProcessDefinitionToCategory(categoryId, processDefinitionId, categoryService, processDefinitionService));
            }
            transactionExecutor.execute(transactionContents);
        } catch (final SCategoryInProcessAlreadyExistsException scipaee) {
            throw new AlreadyExistsException(scipaee);
        } catch (final SBonitaException sbe) {
            throw new CreationException(sbe);
        }
    }

    @Override
    public void removeCategoriesFromProcess(final long processDefinitionId, final List<Long> categoryIds) throws DeletionException {
        try {
            final CategoryService categoryService = getTenantAccessor().getCategoryService();
            final TransactionExecutor transactionExecutor = getTenantAccessor().getTransactionExecutor();
            final TransactionContent transactionContent = new RemoveCategoriesFromProcessDefinition(processDefinitionId, categoryIds, categoryService);
            transactionExecutor.execute(transactionContent);
        } catch (final SBonitaException sbe) {
            throw new DeletionException(sbe);
        }
    }

    @Override
    public void addProcessDefinitionToCategory(final long categoryId, final long processDefinitionId) throws AlreadyExistsException, CreationException {
        try {
            final CategoryService categoryService = getTenantAccessor().getCategoryService();
            final TransactionExecutor transactionExecutor = getTenantAccessor().getTransactionExecutor();
            final ProcessDefinitionService processDefinitionService = getTenantAccessor().getProcessDefinitionService();
            final TransactionContent transactionContent = new AddProcessDefinitionToCategory(categoryId, processDefinitionId, categoryService,
                    processDefinitionService);
            transactionExecutor.execute(transactionContent);
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new CreationException(e);
        } catch (final SCategoryNotFoundException scnfe) {
            throw new CreationException(scnfe);
        } catch (final SCategoryInProcessAlreadyExistsException cipaee) {
            throw new AlreadyExistsException(cipaee);
        } catch (final SBonitaException sbe) {
            throw new CreationException(sbe);
        }
    }

    @Override
    public void addProcessDefinitionsToCategory(final long categoryId, final List<Long> processDefinitionIds) throws AlreadyExistsException, CreationException {
        try {
            final CategoryService categoryService = getTenantAccessor().getCategoryService();
            final TransactionExecutor transactionExecutor = getTenantAccessor().getTransactionExecutor();
            final ProcessDefinitionService processDefinitionService = getTenantAccessor().getProcessDefinitionService();
            final ArrayList<TransactionContent> transactionContents = new ArrayList<TransactionContent>(processDefinitionIds.size());
            for (final Long processDefinitionId : processDefinitionIds) {
                transactionContents.add(new AddProcessDefinitionToCategory(categoryId, processDefinitionId, categoryService, processDefinitionService));
            }
            transactionExecutor.execute(transactionContents);
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new CreationException(e);
        } catch (final SCategoryNotFoundException scnfe) {
            throw new CreationException(scnfe);
        } catch (final SCategoryInProcessAlreadyExistsException cipaee) {
            throw new AlreadyExistsException(cipaee);
        } catch (final SBonitaException sbe) {
            throw new CreationException(sbe);
        }
    }

    @Override
    public long getNumberOfCategories(final long processDefinitionId) {
        try {
            final TenantServiceAccessor tenantAccessor = getTenantAccessor();
            final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
            final CategoryService categoryService = tenantAccessor.getCategoryService();
            final GetNumberOfCategoriesOfProcess getNumberOfCategoriesOfProcess = new GetNumberOfCategoriesOfProcess(categoryService, processDefinitionId);
            transactionExecutor.execute(getNumberOfCategoriesOfProcess);
            return getNumberOfCategoriesOfProcess.getResult();
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    @Override
    public long getNumberOfProcessDefinitionsOfCategory(final long categoryId) {
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
            throw new RetrieveException(e);
        }
        return 0;
    }

    @Override
    public List<ProcessDeploymentInfo> getProcessDeploymentInfosOfCategory(final long categoryId, final int startIndex, final int maxResults,
            final ProcessDeploymentInfoCriterion sortCriterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final CategoryService categoryService = tenantAccessor.getCategoryService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final OrderByType order = getOrderByType(sortCriterion);
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            List<Long> processDefinitionIds;
            try {
                processDefinitionIds = categoryService.getProcessDefinitionIdsOfCategory(categoryId);
                if (processDefinitionIds != null && processDefinitionIds.size() > 0) {
                    final List<SProcessDefinitionDeployInfo> processDefinitionDeployInfoList = processDefinitionService
                            .getProcessDeploymentInfos(processDefinitionIds);
                    if (processDefinitionDeployInfoList != null) {
                        Collections.sort(processDefinitionDeployInfoList, new ProcessDefinitionDeployInfoComparator());
                        if (order != null && order == OrderByType.DESC) {
                            Collections.reverse(processDefinitionDeployInfoList);
                        }
                        if (startIndex >= processDefinitionDeployInfoList.size()) {
                            return Collections.emptyList();
                        }
                        final int toIndex = Math.min(processDefinitionDeployInfoList.size(), startIndex + maxResults);
                        return ModelConvertor.toProcessDeploymentInfo(new ArrayList<SProcessDefinitionDeployInfo>(processDefinitionDeployInfoList.subList(
                                startIndex, toIndex)));
                    }
                }
                return Collections.emptyList();
            } catch (final SBonitaException e) {
                // no rollback, read only tx
                throw new RetrieveException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new RetrieveException(e);
        }
    }

    private OrderByType getOrderByType(final ProcessDeploymentInfoCriterion sortCriterion) {
        OrderByType order = null;
        if (sortCriterion != null) {
            switch (sortCriterion) {
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
        return order;
    }

    @Override
    public List<Category> getCategoriesOfProcessDefinition(final long processDefinitionId, final int startIndex, final int maxResults,
            final CategoryCriterion pagingCriterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final CategoryService categoryService = tenantAccessor.getCategoryService();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                OrderByType order = null;
                switch (pagingCriterion) {
                    case NAME_ASC:
                        order = OrderByType.ASC;
                        break;
                    case NAME_DESC:
                        order = OrderByType.DESC;
                        break;
                }
                return ModelConvertor.toCategories(categoryService.getCategoriesOfProcessDefinition(processDefinitionId, startIndex, maxResults, order));
            } catch (final SBonitaException sbe) {
                throw new RetrieveException(sbe);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public List<Category> getCategoriesUnrelatedToProcessDefinition(final long processDefinitionId, final int startIndex, final int maxResults,
            final CategoryCriterion sortingCriterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final CategoryService categoryService = tenantAccessor.getCategoryService();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                OrderByType order = null;
                switch (sortingCriterion) {
                    case NAME_ASC:
                        order = OrderByType.ASC;
                        break;
                    case NAME_DESC:
                        order = OrderByType.DESC;
                        break;
                }
                return ModelConvertor.toCategories(categoryService
                        .getCategoriesUnrelatedToProcessDefinition(processDefinitionId, startIndex, maxResults, order));
            } catch (final SBonitaException sbe) {
                // no rollback, read only tx
                throw new RetrieveException(sbe);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public void updateCategory(final long categoryId, final CategoryUpdater updater) throws CategoryNotFoundException, UpdateException {
        if (updater == null || updater.getFields().isEmpty()) {
            throw new UpdateException("The update descriptor does not contain field updates");
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final CategoryService categoryService = tenantAccessor.getCategoryService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final SCategoryUpdateBuilder categoryUpdateBuilder = tenantAccessor.getCategoryModelBuilderAccessor().getCategoryUpdateBuilder();
            final EntityUpdateDescriptor updateDescriptor = getCategoryUpdateDescriptor(categoryUpdateBuilder, updater);
            final UpdateCategory updateCategory = new UpdateCategory(categoryService, categoryId, updateDescriptor);
            transactionExecutor.execute(updateCategory);
        } catch (final SCategoryNotFoundException scnfe) {
            throw new CategoryNotFoundException(scnfe);
        } catch (final SBonitaException sbe) {
            throw new UpdateException(sbe);
        }
    }

    private EntityUpdateDescriptor getCategoryUpdateDescriptor(final SCategoryUpdateBuilder descriptorBuilder, final CategoryUpdater updater) {
        final Map<CategoryField, Serializable> fields = updater.getFields();
        final String name = (String) fields.get(CategoryField.NAME);
        if (name != null) {
            descriptorBuilder.updateName(name);
        }
        final String description = (String) fields.get(CategoryField.DESCRIPTION);
        if (description != null) {
            descriptorBuilder.updateDescription(description);
        }
        return descriptorBuilder.done();
    }

    @Override
    public void deleteCategory(final long categoryId) throws DeletionException {
        if (categoryId <= 0) {
            throw new DeletionException("Category id can not be less than 0!");
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final CategoryService categoryService = tenantAccessor.getCategoryService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final DeleteSCategory deleteSCategory = new DeleteSCategory(categoryService, categoryId);
        try {
            transactionExecutor.execute(deleteSCategory);
        } catch (final SBonitaException sbe) {
            throw new DeletionException(sbe);
        }
    }

    @Override
    public void removeAllProcessDefinitionsFromCategory(final long categoryId) throws DeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final CategoryService categoryService = tenantAccessor.getCategoryService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final RemoveProcessDefinitionsOfCategory remove = new RemoveProcessDefinitionsOfCategory(categoryService, categoryId);
        try {
            transactionExecutor.execute(remove);
        } catch (final SBonitaException sbe) {
            throw new DeletionException(sbe);
        }
    }

    @Override
    public long getNumberOfUncategorizedProcessDefinitions() {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final CategoryService categoryService = tenantAccessor.getCategoryService();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final long numberOfProcessDefinitions = processDefinitionService.getNumberOfProcessDeploymentInfos();
                final List<Long> processDefinitionIds = processDefinitionService.getProcessDefinitionIds(0, numberOfProcessDefinitions);
                long number;
                if (processDefinitionIds.isEmpty()) {
                    number = 0;
                } else {
                    number = processDefinitionIds.size() - categoryService.getNumberOfCategorizedProcessIds(processDefinitionIds);
                }
                return number;
            } catch (final SBonitaException e) {
                throw new BonitaRuntimeException(e);// TODO refactor exceptions
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new BonitaRuntimeException(e);// TODO refactor exceptions
        }
    }

    @Override
    public List<ProcessDeploymentInfo> getUncategorizedProcessDeploymentInfos(final int startIndex, final int maxResults,
            final ProcessDeploymentInfoCriterion sortCriterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final CategoryService categoryService = tenantAccessor.getCategoryService();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final long numberOfProcessDefinitions = processDefinitionService.getNumberOfProcessDeploymentInfos();
                final List<Long> processDefinitionIds = processDefinitionService.getProcessDefinitionIds(0, numberOfProcessDefinitions);
                processDefinitionIds.removeAll(categoryService.getCategorizedProcessIds(processDefinitionIds));
                OrderByType order = null;
                switch (sortCriterion) {
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
                final List<SProcessDefinitionDeployInfo> processDefinitionDeployInfos = processDefinitionService.getProcessDeploymentInfos(
                        processDefinitionIds, startIndex, maxResults, "name", order);
                return ModelConvertor.toProcessDeploymentInfo(processDefinitionDeployInfos);
            } catch (final SBonitaException sbe) {
                // no rollback, read only tx
                throw new RetrieveException(sbe);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public long getNumberOfProcessDeploymentInfosUnrelatedToCategory(final long categoryId) {
        try {
            final TenantServiceAccessor tenantAccessor = getTenantAccessor();
            final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
            final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
            final GetNumberOfProcessDeploymentInfosUnrelatedToCategory transactionContentWithResult = new GetNumberOfProcessDeploymentInfosUnrelatedToCategory(
                    categoryId, processDefinitionService);
            transactionExecutor.execute(transactionContentWithResult);
            return transactionContentWithResult.getResult();
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    @Override
    public List<ProcessDeploymentInfo> getProcessDeploymentInfosUnrelatedToCategory(final long categoryId, final int startIndex, final int maxResults,
            final ProcessDeploymentInfoCriterion sortingCriterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                return ModelConvertor.toProcessDeploymentInfo(processDefinitionService.getProcessDeploymentInfosUnrelatedToCategory(categoryId, startIndex,
                        maxResults, sortingCriterion));
            } catch (final SBonitaException e) {
                // no rollback, read only tx
                throw new RetrieveException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public void removeAllCategoriesFromProcessDefinition(final long processDefinitionId) throws DeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final CategoryService categoryService = tenantAccessor.getCategoryService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final TransactionContent transactionContent = new RemoveProcessDefinitionsOfCategory(processDefinitionId, categoryService);
        try {
            transactionExecutor.execute(transactionContent);
        } catch (final SBonitaException sbe) {
            throw new DeletionException(sbe);
        }
    }

    @Override
    public List<EventInstance> getEventInstances(final long rootContainerId, final int startIndex, final int maxResults, final EventCriterion criterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final EventInstanceService eventInstanceService = tenantAccessor.getEventInstanceService();
        final SEndEventInstanceBuilder eventInstanceBuilder = tenantAccessor.getBPMInstanceBuilders().getSEndEventInstanceBuilder();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final OrderAndField orderAndField = OrderAndFields.getOrderAndFieldForEvent(criterion, eventInstanceBuilder);
        final GetEventInstances getEventInstances = new GetEventInstances(eventInstanceService, rootContainerId, startIndex, maxResults,
                orderAndField.getField(), orderAndField.getOrder());
        try {
            transactionExecutor.execute(getEventInstances);
            final List<SEventInstance> result = getEventInstances.getResult();
            return ModelConvertor.toEventInstances(result, flowNodeStateManager);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public void assignUserTask(final long userTaskId, final long userId) throws UpdateException {
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
            throw new UpdateException(sunfe);
        } catch (final SActivityInstanceNotFoundException sainfe) {
            throw new UpdateException(sainfe);
        } catch (final SBonitaException sbe) {
            throw new UpdateException(sbe);
        }
    }

    @Override
    public List<DataDefinition> getActivityDataDefinitions(final long processDefinitionId, final String activityName, final int startIndex, final int maxResults)
            throws ActivityDefinitionNotFoundException, ProcessDefinitionNotFoundException {
        List<DataDefinition> subDataDefinitionList = Collections.emptyList();
        List<SDataDefinition> sdataDefinitionList = Collections.emptyList();
        final TenantServiceAccessor tenantAccessor;
        tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final GetProcessDefinition getProcessDefinition = new GetProcessDefinition(processDefinitionId, processDefinitionService);
        try {
            transactionExecutor.execute(getProcessDefinition);
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
        final SProcessDefinition sProcessDefinition = getProcessDefinition.getResult();
        if (sProcessDefinition != null) {
            boolean activityFound = false;
            final SFlowElementContainerDefinition processContainer = sProcessDefinition.getProcessContainer();
            final Set<SActivityDefinition> activityDefList = processContainer.getActivities();
            for (final SActivityDefinition sActivityDefinition : activityDefList) {
                if (activityName.equals(sActivityDefinition.getName())) {
                    sdataDefinitionList = sActivityDefinition.getSDataDefinitions();
                    activityFound = true;
                    break;
                }
            }
            if (!activityFound) {
                throw new ActivityDefinitionNotFoundException(activityName);
            }
            final List<DataDefinition> dataDefinitionList = ModelConvertor.toDataDefinitions(sdataDefinitionList);
            if (startIndex >= dataDefinitionList.size()) {
                return Collections.emptyList();
            }
            final int toIndex = Math.min(dataDefinitionList.size(), startIndex + maxResults);
            subDataDefinitionList = new ArrayList<DataDefinition>(dataDefinitionList.subList(startIndex, toIndex));
        }
        return subDataDefinitionList;
    }

    @Override
    public List<DataDefinition> getProcessDataDefinitions(final long processDefinitionId, final int startIndex, final int maxResults)
            throws ProcessDefinitionNotFoundException {
        List<DataDefinition> subDataDefinitionList = Collections.emptyList();
        final TenantServiceAccessor tenantAccessor;
        tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final GetProcessDefinition getProcessDefinition = new GetProcessDefinition(processDefinitionId, processDefinitionService);
        try {
            transactionExecutor.execute(getProcessDefinition);
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }

        final SProcessDefinition sProcessDefinition = getProcessDefinition.getResult();
        if (sProcessDefinition != null) {
            final SFlowElementContainerDefinition processContainer = sProcessDefinition.getProcessContainer();
            final List<SDataDefinition> sdataDefinitionList = processContainer.getDataDefinitions();
            final List<DataDefinition> dataDefinitionList = ModelConvertor.toDataDefinitions(sdataDefinitionList);
            if (startIndex >= dataDefinitionList.size()) {
                return Collections.emptyList();
            }
            final int toIndex = Math.min(dataDefinitionList.size(), startIndex + maxResults);
            subDataDefinitionList = new ArrayList<DataDefinition>(dataDefinitionList.subList(startIndex, toIndex));
        }
        return subDataDefinitionList;
    }

    @Override
    public HumanTaskInstance getHumanTaskInstance(final long activityInstanceId) throws ActivityInstanceNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final GetHumanTaskInstance getHumanTaskInstance = new GetHumanTaskInstance(activityInstanceService, activityInstanceId);
        try {
            transactionExecutor.execute(getHumanTaskInstance);
        } catch (final SActivityInstanceNotFoundException e) {
            throw new ActivityInstanceNotFoundException(activityInstanceId, e);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
        return ModelConvertor.toHumanTaskInstance(getHumanTaskInstance.getResult(), flowNodeStateManager);
    }

    @Override
    public long getNumberOfAssignedHumanTaskInstances(final long userId) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final TransactionContentWithResult<Long> transactionContent = new GetNumberOfAssignedUserTaskInstances(userId, activityInstanceService);
            transactionExecutor.execute(transactionContent);
            return transactionContent.getResult();
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public Map<Long, Long> getNumberOfOpenTasks(final List<Long> userIds) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final GetNumberOfOpenTasksForUsers transactionContent = new GetNumberOfOpenTasksForUsers(userIds, activityInstanceService);
            transactionExecutor.execute(transactionContent);
            return transactionContent.getResult();
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public long getNumberOfPendingHumanTaskInstances(final long userId) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefService = tenantAccessor.getProcessDefinitionService();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final Set<Long> actorIds = getActorsForUser(userId, actorMappingService, processDefService);
                if (actorIds.isEmpty()) {
                    return 0L;
                }
                return activityInstanceService.getNumberOfPendingTasksForUser(userId, QueryOptions.defaultQueryOptions());
            } catch (final SBonitaException e) {
                // no rollback, read only tx
                throw new RetrieveException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public Map<String, byte[]> getProcessResources(final long processDefinitionId, final String filenamesPattern) throws RetrieveException {
        String processesFolder;
        TenantServiceAccessor tenantAccessor;
        try {
            tenantAccessor = getTenantAccessor();
            processesFolder = BonitaHomeServer.getInstance().getProcessesFolder(tenantAccessor.getTenantId());
        } catch (final BonitaHomeNotSetException e) {
            throw new RetrieveException("Problem accessing basic Bonita Home server resources", e);
        }
        processesFolder = processesFolder.replaceAll("\\\\", "/");
        if (!processesFolder.endsWith("/")) {
            processesFolder = processesFolder + "/";
        }
        processesFolder = processesFolder + processDefinitionId + "/";
        final Collection<File> files = FileUtils.listFiles(new File(processesFolder), new DeepRegexFileFilter(processesFolder + filenamesPattern),
                DirectoryFileFilter.DIRECTORY);
        final Map<String, byte[]> res = new HashMap<String, byte[]>(files.size());
        try {
            for (final File f : files) {
                res.put(f.getAbsolutePath().replaceAll("\\\\", "/").replaceFirst(processesFolder, ""), IOUtil.getAllContentFrom(f));
            }
        } catch (final IOException e) {
            throw new RetrieveException("Problem accessing resources " + filenamesPattern + " for processDefinitionId: " + processDefinitionId, e);
        }
        return res;
    }

    @Override
    public long getLatestProcessDefinitionId(final String processName) throws ProcessDefinitionNotFoundException {
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
    public List<DataInstance> getProcessDataInstances(final long processInstanceId, final int startIndex, final int maxResults) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final DataInstanceService dataInstanceService = tenantAccessor.getDataInstanceService();
        final ClassLoaderService classLoaderService = tenantAccessor.getClassLoaderService();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            final boolean openTransaction = transactionExecutor.openTransaction();
            try {
                final long processDefinitionId = processInstanceService.getProcessInstance(processInstanceId).getProcessDefinitionId();
                final ClassLoader processClassLoader = classLoaderService.getLocalClassLoader("process", processDefinitionId);
                Thread.currentThread().setContextClassLoader(processClassLoader);
                final List<SDataInstance> dataInstances = dataInstanceService.getDataInstances(processInstanceId,
                        DataInstanceContainer.PROCESS_INSTANCE.name(), startIndex, maxResults);
                return ModelConvertor.toDataInstances(dataInstances);
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new RetrieveException(e);
            } finally {
                Thread.currentThread().setContextClassLoader(contextClassLoader);
                transactionExecutor.completeTransaction(openTransaction);
            }
        } catch (final STransactionException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public DataInstance getProcessDataInstance(final String dataName, final long processInstanceId) throws DataNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final DataInstanceService dataInstanceService = tenantAccessor.getDataInstanceService();
        final ClassLoaderService classLoaderService = tenantAccessor.getClassLoaderService();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            final boolean openTransaction = transactionExecutor.openTransaction();
            try {
                final long processDefinitionId = processInstanceService.getProcessInstance(processInstanceId).getProcessDefinitionId();
                final ClassLoader processClassLoader = classLoaderService.getLocalClassLoader("process", processDefinitionId);
                Thread.currentThread().setContextClassLoader(processClassLoader);
                final SDataInstance sDataInstance = dataInstanceService.getDataInstance(dataName, processInstanceId,
                        DataInstanceContainer.PROCESS_INSTANCE.toString());
                return ModelConvertor.toDataInstance(sDataInstance);
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new DataNotFoundException(e);
            } finally {
                Thread.currentThread().setContextClassLoader(contextClassLoader);
                transactionExecutor.completeTransaction(openTransaction);
            }
        } catch (final STransactionException e) {
            throw new DataNotFoundException(e);
        }
    }

    @Override
    public void updateProcessDataInstance(final String dataName, final long processInstanceId, final Serializable dataValue) throws UpdateException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final DataInstanceService dataInstanceService = tenantAccessor.getDataInstanceService();
        final ClassLoaderService classLoaderService = tenantAccessor.getClassLoaderService();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            final boolean openTransaction = transactionExecutor.openTransaction();
            try {
                final long processDefinitionId = processInstanceService.getProcessInstance(processInstanceId).getProcessDefinitionId();
                final ClassLoader processClassLoader = classLoaderService.getLocalClassLoader("process", processDefinitionId);
                Thread.currentThread().setContextClassLoader(processClassLoader);
                final SDataInstance sDataInstance = dataInstanceService.getDataInstance(dataName, processInstanceId,
                        DataInstanceContainer.PROCESS_INSTANCE.toString());
                final EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
                entityUpdateDescriptor.addField("value", dataValue);
                dataInstanceService.updateDataInstance(sDataInstance, entityUpdateDescriptor);
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new UpdateException(e);
            } finally {
                Thread.currentThread().setContextClassLoader(contextClassLoader);
                transactionExecutor.completeTransaction(openTransaction);
            }
        } catch (final STransactionException e) {
            throw new UpdateException(e);
        }
    }

    @Override
    public List<DataInstance> getActivityDataInstances(final long activityInstanceId, final int startIndex, final int maxResults) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final DataInstanceService dataInstanceService = tenantAccessor.getDataInstanceService();
        final ClassLoaderService classLoaderService = tenantAccessor.getClassLoaderService();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final int processDefinitionIndex = tenantAccessor.getBPMInstanceBuilders().getSAutomaticTaskInstanceBuilder().getProcessDefinitionIndex();
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            final boolean openTransaction = transactionExecutor.openTransaction();
            try {
                final long parentProcessInstanceId = activityInstanceService.getFlowNodeInstance(activityInstanceId).getLogicalGroup(processDefinitionIndex);
                final ClassLoader processClassLoader = classLoaderService.getLocalClassLoader("process", parentProcessInstanceId);
                Thread.currentThread().setContextClassLoader(processClassLoader);
                final List<SDataInstance> dataInstances = dataInstanceService.getDataInstances(activityInstanceId,
                        DataInstanceContainer.ACTIVITY_INSTANCE.name(), startIndex, maxResults);
                return ModelConvertor.toDataInstances(dataInstances);
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new RetrieveException(e);
            } finally {
                Thread.currentThread().setContextClassLoader(contextClassLoader);
                transactionExecutor.completeTransaction(openTransaction);
            }
        } catch (final STransactionException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public DataInstance getActivityDataInstance(final String dataName, final long activityInstanceId) throws DataNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final DataInstanceService dataInstanceService = tenantAccessor.getDataInstanceService();
        final ClassLoaderService classLoaderService = tenantAccessor.getClassLoaderService();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final int processDefinitionIndex = tenantAccessor.getBPMInstanceBuilders().getSAutomaticTaskInstanceBuilder().getProcessDefinitionIndex();
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            final boolean openTransaction = transactionExecutor.openTransaction();
            try {
                final long parentProcessInstanceId = activityInstanceService.getFlowNodeInstance(activityInstanceId).getLogicalGroup(processDefinitionIndex);
                final ClassLoader processClassLoader = classLoaderService.getLocalClassLoader("process", parentProcessInstanceId);
                Thread.currentThread().setContextClassLoader(processClassLoader);
                final SDataInstance sDataInstance = dataInstanceService.getDataInstance(dataName, activityInstanceId,
                        DataInstanceContainer.ACTIVITY_INSTANCE.toString());
                return ModelConvertor.toDataInstance(sDataInstance);
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new DataNotFoundException(e);
            } finally {
                Thread.currentThread().setContextClassLoader(contextClassLoader);
                transactionExecutor.completeTransaction(openTransaction);
            }
        } catch (final STransactionException e) {
            throw new DataNotFoundException(e);
        }
    }

    @Override
    public void updateActivityDataInstance(final String dataName, final long activityInstanceId, final Serializable dataValue) throws UpdateException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final DataInstanceService dataInstanceService = tenantAccessor.getDataInstanceService();
        final ClassLoaderService classLoaderService = tenantAccessor.getClassLoaderService();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final int processDefinitionIndex = tenantAccessor.getBPMInstanceBuilders().getSAutomaticTaskInstanceBuilder().getProcessDefinitionIndex();
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            final boolean openTransaction = transactionExecutor.openTransaction();
            try {
                final long parentProcessInstanceId = activityInstanceService.getFlowNodeInstance(activityInstanceId).getLogicalGroup(processDefinitionIndex);
                final ClassLoader processClassLoader = classLoaderService.getLocalClassLoader("process", parentProcessInstanceId);
                Thread.currentThread().setContextClassLoader(processClassLoader);
                final SDataInstance sDataInstance = dataInstanceService.getDataInstance(dataName, activityInstanceId,
                        DataInstanceContainer.ACTIVITY_INSTANCE.toString());
                final EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
                entityUpdateDescriptor.addField("value", dataValue);
                dataInstanceService.updateDataInstance(sDataInstance, entityUpdateDescriptor);
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new UpdateException(e);
            } finally {
                Thread.currentThread().setContextClassLoader(contextClassLoader);
                transactionExecutor.completeTransaction(openTransaction);
            }
        } catch (final STransactionException e) {
            throw new UpdateException(e);
        }
    }

    @Override
    public void importActorMapping(final long processDefinitionId, final String xmlContent) throws ActorMappingImportException {
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
    public String exportActorMapping(final long processDefinitionId) throws ActorMappingExportException {
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
    public boolean isInvolvedInProcessInstance(final long userId, final long processInstanceId) throws ProcessInstanceNotFoundException, UserNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();

        final IdentityService identityService = tenantAccessor.getIdentityService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final int totalNumber = activityInstanceService.getNumberOfActivityInstances(processInstanceId);
                final List<SActivityInstance> activityInstances = activityInstanceService.getActivityInstances(processInstanceId, 0, totalNumber, null, null);
                for (final SActivityInstance activityInstance : activityInstances) {
                    if (activityInstance instanceof SUserTaskInstance) {
                        final SUserTaskInstance userTaskInstance = (SUserTaskInstance) activityInstance;
                        if (userId == userTaskInstance.getAssigneeId()) {
                            return true;
                        }
                        final long actorId = userTaskInstance.getActorId();
                        final int numOfActorMembers = (int) actorMappingService.getNumberOfActorMembers(actorId);
                        final int numberPerPage = 100;
                        for (int i = 0; i < numOfActorMembers % numberPerPage; i++) {
                            final GetActorMembers getActorMembers = new GetActorMembers(actorMappingService, actorId, i, numberPerPage);
                            getActorMembers.execute();
                            for (final SActorMember actorMember : getActorMembers.getResult()) {
                                if (actorMember.getUserId() == userId) {
                                    return true;
                                }
                                // if userId is as id of a user manager, return true
                                final SUser user = identityService.getUser(actorMember.getUserId());
                                if (userId == user.getManagerUserId()) {
                                    return true;
                                }
                            }
                        }
                    }
                }
                if (activityInstances.isEmpty()) {
                    // check if the process exists in case of there is no results
                    try {
                        tenantAccessor.getProcessInstanceService().getProcessInstance(processInstanceId);
                    } catch (final SProcessInstanceNotFoundException e) {
                        throw new ProcessInstanceNotFoundException(processInstanceId);
                    }
                }
                return false;
            } catch (final SBonitaException e) {
                // no rollback, read only method
                throw new BonitaRuntimeException(e);// TODO refactor Exceptions!!!!!!!!!!!!!!!!!!!
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new BonitaRuntimeException(e);// TODO refactor Exceptions!!!!!!!!!!!!!!!!!!!
        }
    }

    @Override
    public long getProcessInstanceIdFromActivityInstanceId(final long activityInstanceId) throws ProcessInstanceNotFoundException {
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
    public long getProcessDefinitionIdFromActivityInstanceId(final long activityInstanceId) throws ProcessDefinitionNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final SActivityInstance activity = activityInstanceService.getActivityInstance(activityInstanceId);
                return processInstanceService.getProcessInstance(activity.getRootContainerId()).getProcessDefinitionId();
            } catch (final SBonitaException e) {
                throw new ProcessDefinitionNotFoundException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new ProcessDefinitionNotFoundException(e);
        }
    }

    @Override
    public long getProcessDefinitionIdFromProcessInstanceId(final long processInstanceId) throws ProcessDefinitionNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SearchProcessInstanceDescriptor searchProcessInstanceDescriptor = tenantAccessor.getSearchEntitiesDescriptor().getProcessInstanceDescriptor();

        try {
            final GetProcessInstance getProcessInstance = new GetProcessInstance(processInstanceService, processDefinitionService,
                    searchProcessInstanceDescriptor, processInstanceId);
            transactionExecutor.execute(getProcessInstance);
            final ProcessInstance processInstance = getProcessInstance.getResult();
            return processInstance.getProcessDefinitionId();
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ProcessDefinitionNotFoundException(e.getMessage());
        }
    }

    @Override
    public Date getActivityReachedStateDate(final long activityInstanceId, final String stateName) {
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
            throw new RetrieveException(e);
        }
    }

    @Override
    public Set<String> getSupportedStates(final FlowNodeType nodeType) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        return flowNodeStateManager.getSupportedState(nodeType);
    }

    @Override
    public void updateActivityInstanceVariables(final long activityInstanceId, final Map<String, Serializable> variables) throws UpdateException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final DataInstanceService dataInstanceService = tenantAccessor.getDataInstanceService();
        final ClassLoaderService classLoaderService = tenantAccessor.getClassLoaderService();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final int processDefinitionIndex = tenantAccessor.getBPMInstanceBuilders().getSAutomaticTaskInstanceBuilder().getProcessDefinitionIndex();
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            final boolean openTransaction = transactionExecutor.openTransaction();
            try {
                final long parentProcessInstanceId = activityInstanceService.getFlowNodeInstance(activityInstanceId).getLogicalGroup(processDefinitionIndex);
                final ClassLoader processClassLoader = classLoaderService.getLocalClassLoader("process", parentProcessInstanceId);
                Thread.currentThread().setContextClassLoader(processClassLoader);
                for (final Entry<String, Serializable> variable : variables.entrySet()) {
                    final SDataInstance sDataInstance = dataInstanceService.getDataInstance(variable.getKey(), activityInstanceId,
                            DataInstanceContainer.ACTIVITY_INSTANCE.toString());
                    final EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
                    entityUpdateDescriptor.addField("value", variable.getValue());
                    dataInstanceService.updateDataInstance(sDataInstance, entityUpdateDescriptor);
                }
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new UpdateException(e);
            } finally {
                Thread.currentThread().setContextClassLoader(contextClassLoader);
                transactionExecutor.completeTransaction(openTransaction);
            }
        } catch (final STransactionException e) {
            throw new UpdateException(e);
        }
    }

    @Override
    public void updateActivityInstanceVariables(final List<Operation> operations, final long activityInstanceId,
            final Map<String, Serializable> expressionContexts) throws UpdateException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final OperationService operationService = tenantAccessor.getOperationService();
        final SOperationBuilders sOperationBuilders = tenantAccessor.getSOperationBuilders();
        final SExpressionBuilders sExpressionBuilders = tenantAccessor.getSExpressionBuilders();
        final DataInstanceService dataInstanceService = tenantAccessor.getDataInstanceService();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final int processDefinitionIndex = tenantAccessor.getBPMInstanceBuilders().getSAutomaticTaskInstanceBuilder().getProcessDefinitionIndex();
        final List<String> dataNames = new ArrayList<String>(operations.size());
        for (final Operation operation : operations) {
            dataNames.add(operation.getLeftOperand().getName());
        }
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final List<SDataInstance> dataInstances = dataInstanceService.getDataInstances(dataNames, activityInstanceId,
                        DataInstanceContainer.ACTIVITY_INSTANCE.toString());
                final SActivityInstance activityInstance = activityInstanceService.getActivityInstance(activityInstanceId);
                for (int i = 0; i < dataInstances.size(); i++) {
                    // data instances and operation are in the same order
                    final SDataInstance dataInstance = dataInstances.get(i);
                    final Operation operation = operations.get(i);
                    final SOperation sOperation = toSOperation(operation, sOperationBuilders, sExpressionBuilders);
                    final SExpressionContext sExpressionContext = new SExpressionContext(activityInstanceId,
                            DataInstanceContainer.ACTIVITY_INSTANCE.toString(), activityInstance.getLogicalGroup(processDefinitionIndex));
                    sExpressionContext.setSerializableInputValues(expressionContexts);
                    operationService.execute(sOperation, dataInstance.getContainerId(), dataInstance.getContainerType(), sExpressionContext);
                }
            } catch (final SDataInstanceException e) {
                throw new UpdateException(e);
            } catch (final SBonitaException e) {
                throw new UpdateException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new UpdateException(e);
        }
    }

    @Override
    public long getOneAssignedUserTaskInstanceOfProcessInstance(final long processInstanceId, final long userId) throws UserNotFoundException,
            RetrieveException {
        // FIXME: write specific query that should be more efficient:
        final int assignedUserTaskInstanceNumber = (int) getNumberOfAssignedHumanTaskInstances(userId);
        final List<HumanTaskInstance> userTaskInstances = getAssignedHumanTaskInstances(userId, 0, assignedUserTaskInstanceNumber,
                ActivityInstanceCriterion.DEFAULT);
        String stateName;
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
    public long getOneAssignedUserTaskInstanceOfProcessDefinition(final long processDefinitionId, final long userId) throws UserNotFoundException {
        // TODO change this method to do that in one request
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SearchProcessInstanceDescriptor searchProcessInstanceDescriptor = tenantAccessor.getSearchEntitiesDescriptor().getProcessInstanceDescriptor();

        final int assignedUserTaskInstanceNumber = (int) getNumberOfAssignedHumanTaskInstances(userId);
        final List<HumanTaskInstance> userTaskInstances = getAssignedHumanTaskInstances(userId, 0, assignedUserTaskInstanceNumber,
                ActivityInstanceCriterion.DEFAULT);
        String stateName;
        if (userTaskInstances.size() != 0) {
            for (final HumanTaskInstance userTaskInstance : userTaskInstances) {
                stateName = userTaskInstance.getState();
                ProcessInstance processInstance;
                try {
                    final GetProcessInstance getProcessInstance = new GetProcessInstance(processInstanceService, processDefinitionService,
                            searchProcessInstanceDescriptor, userTaskInstance.getRootContainerId());
                    transactionExecutor.execute(getProcessInstance);
                    processInstance = getProcessInstance.getResult();
                } catch (final SBonitaException e) {
                    throw new RetrieveException(e);
                }
                if (stateName.equals(ActivityStates.READY_STATE) && processInstance.getProcessDefinitionId() == processDefinitionId) {
                    return userTaskInstance.getId();
                }
            }
        }
        return -1;
    }

    @Override
    public String getActivityInstanceState(final long activityInstanceId) throws ActivityInstanceNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        try {
            final GetActivityInstance getActivityInstance = new GetActivityInstance(activityInstanceService, activityInstanceId);
            transactionExecutor.execute(getActivityInstance);
            final SActivityInstance sActivity = getActivityInstance.getResult();
            final ActivityInstance activityInstance = ModelConvertor.toActivityInstance(sActivity, flowNodeStateManager);
            return activityInstance.getState();
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ActivityInstanceNotFoundException(activityInstanceId);
        }
    }

    @Override
    public boolean canExecuteTask(final long activityInstanceId, final long userId) throws ActivityInstanceNotFoundException, UserNotFoundException,
            RetrieveException {
        final HumanTaskInstance userTaskInstance = getHumanTaskInstance(activityInstanceId);
        return userTaskInstance.getState().equalsIgnoreCase(ActivityStates.READY_STATE) && userTaskInstance.getAssigneeId() == userId;
    }

    @Override
    public long getProcessDefinitionId(final String name, final String version) throws ProcessDefinitionNotFoundException {
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
    public void releaseUserTask(final long userTaskId) throws ActivityInstanceNotFoundException, UpdateException {
        final TenantServiceAccessor tenantAccessor;
        tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        try {
            final AssignOrUnassignUserTask assignUserTask = new AssignOrUnassignUserTask(0, userTaskId, activityInstanceService, null, null);
            transactionExecutor.execute(assignUserTask);
        } catch (final SUnreleasableTaskException e) {
            throw new UpdateException(e);
        } catch (final SActivityInstanceNotFoundException e) {
            throw new ActivityInstanceNotFoundException(e);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new UpdateException(e);
        }
    }

    @Override
    public void updateProcessDeploymentInfo(final long processId, final ProcessDeploymentInfoUpdater processDeploymentInfoUpdater)
            throws ProcessDefinitionNotFoundException, UpdateException {
        if (processDeploymentInfoUpdater == null || processDeploymentInfoUpdater.getFields().isEmpty()) {
            throw new UpdateException("The update descriptor does not contain field updates");
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SProcessDefinitionDeployInfoUpdateBuilder processDeploymentInfoUpdateBuilder = tenantAccessor.getBPMDefinitionBuilders()
                .getProcessDefinitionDeployInfoUpdateBuilder();
        final UpdateProcessDeploymentInfo updateProcessDeploymentInfo = new UpdateProcessDeploymentInfo(processDefinitionService,
                processDeploymentInfoUpdateBuilder, processId, processDeploymentInfoUpdater);
        try {
            transactionExecutor.execute(updateProcessDeploymentInfo);
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SBonitaException sbe) {
            throw new UpdateException(sbe);
        }
    }

    @Override
    public List<ProcessDeploymentInfo> getStartableProcessDeploymentInfosForActors(final Set<Long> actorIds, final int startIndex, final int maxResults,
            final ProcessDeploymentInfoCriterion sortingCriterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final List<SActor> actors = actorMappingService.getActors(new ArrayList<Long>(actorIds));
                final HashSet<Long> processDefIds = new HashSet<Long>(actors.size());
                for (final SActor sActor : actors) {
                    if (sActor.isInitiator()) {
                        processDefIds.add(sActor.getScopeId());
                    }
                }
                final List<SProcessDefinitionDeployInfo> processDefinitionDeployInfos = processDefinitionService.getProcessDeploymentInfos(new ArrayList<Long>(
                        processDefIds));
                return ModelConvertor.toProcessDeploymentInfo(processDefinitionDeployInfos);
            } catch (final SBonitaException e) {
                // no rollback only read db
                throw new RetrieveException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new RetrieveException(e);
        }

    }

    @Override
    public boolean isAllowedToStartProcess(final long processId, final Set<Long> actorIds) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final GetActorsByActorIds getActorsByActorIds = new GetActorsByActorIds(actorMappingService, new ArrayList<Long>(actorIds));
        try {
            transactionExecutor.execute(getActorsByActorIds);
            final List<SActor> actors = getActorsByActorIds.getResult();
            boolean isAllowedToStartProcess = true;
            final Iterator<SActor> iterator = actors.iterator();
            while (isAllowedToStartProcess && iterator.hasNext()) {
                final SActor actor = iterator.next();
                if (actor.getScopeId() != processId || !actor.isInitiator()) {
                    isAllowedToStartProcess = false;
                }
            }
            return isAllowedToStartProcess;
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public ActorInstance getActorInitiator(final long processDefinitionId) throws ActorNotFoundException, ProcessDefinitionNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        ActorInstance actorInstance = null;
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final SProcessDefinition definition = processDefinitionService.getProcessDefinition(processDefinitionId);
                final SActorDefinition sActorDefinition = definition.getActorInitiator();
                if (sActorDefinition == null) {
                    throw new ActorNotFoundException("No actor initiator defined on the process");
                }
                final String name = sActorDefinition.getName();
                final SActor sActor = actorMappingService.getActor(name, processDefinitionId);
                actorInstance = ModelConvertor.toActorInstance(sActor);
            } catch (final SProcessDefinitionNotFoundException e) {
                // no rollback need, we only read
                throw new ProcessDefinitionNotFoundException(e);
            } catch (final SBonitaException e) {
                // no rollback need, we only read
                throw new RetrieveException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new RetrieveException(e);
        }
        return actorInstance;
    }

    @Override
    public int getNumberOfActivityDataDefinitions(final long processDefinitionId, final String activityName) throws ProcessDefinitionNotFoundException,
            ActivityDefinitionNotFoundException {
        List<SDataDefinition> sdataDefinitionList = Collections.emptyList();
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final GetProcessDefinition getProcessDefinition = new GetProcessDefinition(processDefinitionId, processDefinitionService);
        try {
            transactionExecutor.execute(getProcessDefinition);
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
        final SProcessDefinition sProcessDefinition = getProcessDefinition.getResult();
        if (sProcessDefinition != null) {
            boolean found = false;
            final SFlowElementContainerDefinition processContainer = sProcessDefinition.getProcessContainer();
            final Set<SActivityDefinition> activityDefList = processContainer.getActivities();
            for (final SActivityDefinition sActivityDefinition : activityDefList) {
                if (activityName.equals(sActivityDefinition.getName())) {
                    sdataDefinitionList = sActivityDefinition.getSDataDefinitions();
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new ActivityDefinitionNotFoundException(activityName);
            }
            return sdataDefinitionList.size();
        }
        return 0;
    }

    @Override
    public int getNumberOfProcessDataDefinitions(final long processDefinitionId) throws ProcessDefinitionNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();

        final GetProcessDefinition getProcessDefinition = new GetProcessDefinition(processDefinitionId, processDefinitionService);
        try {
            transactionExecutor.execute(getProcessDefinition);
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
        final SProcessDefinition sProcessDefinition = getProcessDefinition.getResult();
        final SFlowElementContainerDefinition processContainer = sProcessDefinition.getProcessContainer();
        return processContainer.getDataDefinitions().size();
    }

    @Override
    public ProcessInstance startProcess(final long processDefinitionId, final List<Operation> operations, final Map<String, Serializable> context)
            throws ProcessExecutionException, ProcessDefinitionNotFoundException, ProcessActivationException {
        try {
            return startProcess(0, processDefinitionId, operations, context);
        } catch (final RetrieveException e) {
            throw new ProcessExecutionException(e);
        } catch (final UserNotFoundException e) {
            throw new ProcessExecutionException(e);
        }
    }

    private long getUserIdFromSession() {
        SessionAccessor sessionAccessor;
        long userId;
        try {
            sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long sessionId = sessionAccessor.getSessionId();
            final PlatformServiceAccessor platformServiceAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
            userId = platformServiceAccessor.getSessionService().getSession(sessionId).getUserId();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
        return userId;
    }

    @Override
    public ProcessInstance startProcess(final long userId, final long processDefinitionId, final List<Operation> operations,
            final Map<String, Serializable> context)
            throws ProcessDefinitionNotFoundException, UserNotFoundException, ProcessActivationException, ProcessExecutionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final ProcessExecutor processExecutor = tenantAccessor.getProcessExecutor();
        final SOperationBuilders sOperationBuilders = tenantAccessor.getSOperationBuilders();
        final SExpressionBuilders sExpressionBuilders = tenantAccessor.getSExpressionBuilders();
        final long starterId;
        if (userId == 0) {
            starterId = getUserIdFromSession();
        } else {
            starterId = userId;
        }
        // Retrieval of the process definition:
        SProcessDefinition sDefinition;
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final SProcessDefinitionDeployInfo deployInfo = processDefinitionService.getProcessDeploymentInfo(processDefinitionId);
                if (ActivationState.DISABLED.name().equals(deployInfo.getActivationState())) {
                    throw new ProcessActivationException("Process disabled");
                }
                sDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
            } catch (final SProcessDefinitionNotFoundException e) {
                throw new ProcessDefinitionNotFoundException(e);
            } catch (final SBonitaException e) {
                // no rollback, read only tx
                throw new RetrieveException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new RetrieveException(e);
        }
        SProcessInstance startedInstance;
        try {
            final List<SOperation> sOperations = toSOperation(operations, sOperationBuilders, sExpressionBuilders);
            Map<String, Object> operationContext;
            if (context != null) {
                operationContext = new HashMap<String, Object>(context);
            } else {
                operationContext = Collections.emptyMap();
            }
            startedInstance = processExecutor.start(sDefinition, starterId, getUserIdFromSession(), sOperations, operationContext);
        } catch (final SBonitaException e) {
            throw new ProcessExecutionException(e);
        }// FIXME in case process instance creation exception -> put it in failed
        return ModelConvertor.toProcessInstance(sDefinition, startedInstance);
    }

    private List<SOperation> toSOperation(final List<Operation> operations, final SOperationBuilders sOperationBuilders,
            final SExpressionBuilders sExpressionBuilders) {
        if (operations == null) {
            return null;
        }
        if (operations.isEmpty()) {
            return Collections.emptyList();
        }
        final List<SOperation> sOperations = new ArrayList<SOperation>(operations.size());
        for (final Operation operation : operations) {
            final SOperation sOperation = toSOperation(operation, sOperationBuilders, sExpressionBuilders);
            sOperations.add(sOperation);
        }
        return sOperations;
    }

    private SOperation toSOperation(final Operation operation, final SOperationBuilders sOperationBuilders, final SExpressionBuilders sExpressionBuilders) {
        final SExpression rightOperand = ModelConvertor.constructSExpression(operation.getRightOperand(), sExpressionBuilders.getExpressionBuilder());
        final SOperatorType operatorType = SOperatorType.valueOf(operation.getType().name());
        final SLeftOperand sLeftOperand = toSLeftOperand(operation.getLeftOperand(), sOperationBuilders);
        return sOperationBuilders.getSOperationBuilder().createNewInstance().setOperator(operation.getOperator()).setRightOperand(rightOperand)
                .setType(operatorType).setLeftOperand(sLeftOperand).done();
    }

    private SLeftOperand toSLeftOperand(final LeftOperand variableToSet, final SOperationBuilders sOperationBuilders) {
        return sOperationBuilders.getSLeftOperandBuilder().createNewInstance().setName(variableToSet.getName()).done();
    }

    @Override
    public long getNumberOfActivityDataInstances(final long activityInstanceId) throws ActivityInstanceNotFoundException {
        try {
            return getNumberOfDataInstancesOfContainer(activityInstanceId, DataInstanceContainer.ACTIVITY_INSTANCE);
        } catch (final SBonitaException e) {
            throw new ActivityInstanceNotFoundException(activityInstanceId);
        }
    }

    private long getNumberOfDataInstancesOfContainer(final long activityInstanceId, final DataInstanceContainer containerType) throws SBonitaException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final DataInstanceService dataInstanceService = tenantAccessor.getDataInstanceService();
        final GetNumberOfDataInstanceForContainer getNumberOfDataInstance = new GetNumberOfDataInstanceForContainer(activityInstanceId, containerType,
                dataInstanceService);
        transactionExecutor.execute(getNumberOfDataInstance);
        return getNumberOfDataInstance.getResult();
    }

    @Override
    public long getNumberOfProcessDataInstances(final long processInstanceId) throws ProcessInstanceNotFoundException {
        try {
            return getNumberOfDataInstancesOfContainer(processInstanceId, DataInstanceContainer.PROCESS_INSTANCE);
        } catch (final SBonitaException e) {
            throw new ProcessInstanceNotFoundException(e);
        }
    }

    protected Map<String, Serializable> executeOperations(final ConnectorResult connectorResult, final List<Operation> operations,
            final Map<String, Serializable> operationInputValues, final SExpressionContext expressionContext, final ClassLoader classLoader,
            final TenantServiceAccessor tenantAccessor) throws SBonitaException {
        final ConnectorService connectorService = tenantAccessor.getConnectorService();
        final OperationService operationService = tenantAccessor.getOperationService();
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            final Map<String, Serializable> externalDataValue = new HashMap<String, Serializable>(operations.size());
            for (final Operation operation : operations) {
                // convert the client operation to server operation
                final SOperation sOperation = ModelConvertor.constructSOperation(operation, tenantAccessor);
                // set input values of expression with connector result + provided input for this operation
                final HashMap<String, Object> inputValues = new HashMap<String, Object>(operationInputValues);
                inputValues.putAll(connectorResult.getResult());
                expressionContext.setInputValues(inputValues);
                // execute
                final Long containerId = expressionContext.getContainerId();
                operationService.execute(sOperation, containerId == null ? -1 : containerId, expressionContext.getContainerType(), expressionContext);
                // return the value of the data if it's an external data
                final LeftOperand leftOperand = operation.getLeftOperand();
                if (leftOperand.isExternal()) {
                    externalDataValue.put(leftOperand.getName(), (Serializable) expressionContext.getInputValues().get(leftOperand.getName()));
                }
            }
            // we finally disconnect the connector
            connectorService.disconnect(connectorResult);
            return externalDataValue;
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    private Map<String, Serializable> toSerializableMap(final Map<String, Object> map, final String connectorDefinitionId,
            final String connectorDefinitionVersion) throws NotSerializableException {
        final HashMap<String, Serializable> resMap = new HashMap<String, Serializable>(map.size());
        for (final Entry<String, Object> entry : map.entrySet()) {
            try {
                resMap.put(entry.getKey(), (Serializable) entry.getValue());
            } catch (final ClassCastException e) {
                throw new NotSerializableException(connectorDefinitionId, connectorDefinitionVersion, entry.getKey(), entry.getValue());
            }
        }
        return resMap;
    }

    @Override
    public Map<String, Serializable> executeConnectorOnProcessDefinition(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final long processDefinitionId)
            throws ConnectorExecutionException, ConnectorNotFoundException {
        return executeConnectorOnProcessDefinitionWithOrWithoutOperations(connectorDefinitionId, connectorDefinitionVersion, connectorInputParameters,
                inputValues, null, null, processDefinitionId);
    }

    @Override
    public Map<String, Serializable> executeConnectorOnProcessDefinition(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final List<Operation> operations,
            final Map<String, Serializable> operationInputValues, final long processDefinitionId) throws ConnectorExecutionException,
            ConnectorNotFoundException {
        return executeConnectorOnProcessDefinitionWithOrWithoutOperations(connectorDefinitionId, connectorDefinitionVersion, connectorInputParameters,
                inputValues, operations, operationInputValues, processDefinitionId);
    }

    /**
     * execute the connector and return connector output if there is no operation or operation output if there is operation
     * 
     * @param operations
     * @param operationInputValues
     */
    private Map<String, Serializable> executeConnectorOnProcessDefinitionWithOrWithoutOperations(final String connectorDefinitionId,
            final String connectorDefinitionVersion, final Map<String, Expression> connectorInputParameters,
            final Map<String, Map<String, Serializable>> inputValues, final List<Operation> operations, final Map<String, Serializable> operationInputValues,
            final long processDefinitionId) throws ConnectorExecutionException {
        checkConnectorParameters(connectorInputParameters, inputValues);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SExpressionBuilders sExpressionBuilders = tenantAccessor.getSExpressionBuilders();
        final ConnectorService connectorService = tenantAccessor.getConnectorService();
        final ClassLoaderService classLoaderService = tenantAccessor.getClassLoaderService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final ClassLoader classLoader = classLoaderService.getLocalClassLoader("process", processDefinitionId);

                final Map<String, SExpression> connectorsExps = ModelConvertor.constructExpressions(sExpressionBuilders, connectorInputParameters);
                final SExpressionContext expcontext = new SExpressionContext();
                expcontext.setProcessDefinitionId(processDefinitionId);
                final SProcessDefinition processDef = processDefinitionService.getProcessDefinition(processDefinitionId);
                if (processDef != null) {
                    expcontext.setProcessDefinition(processDef);
                }
                final ConnectorResult connectorResult = connectorService.executeMutipleEvaluation(processDefinitionId, connectorDefinitionId,
                        connectorDefinitionVersion, connectorsExps, inputValues, classLoader, expcontext);
                if (operations != null) {
                    // execute operations
                    return executeOperations(connectorResult, operations, operationInputValues, expcontext, classLoader, tenantAccessor);
                } else {
                    return getSerializableResultOfConnector(connectorDefinitionVersion, connectorResult, connectorService);
                }
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new ConnectorExecutionException(e);
            } catch (final NotSerializableException e) {
                transactionExecutor.setTransactionRollback();
                throw new ConnectorExecutionException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new ConnectorExecutionException(e);
        }
    }

    protected Map<String, Serializable> getSerializableResultOfConnector(final String connectorDefinitionVersion, final ConnectorResult connectorResult,
            final ConnectorService connectorService) throws NotSerializableException, SConnectorException {
        connectorService.disconnect(connectorResult);
        return toSerializableMap(connectorResult.getResult(), connectorDefinitionVersion, connectorDefinitionVersion);
    }

    protected void checkConnectorParameters(final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues)
            throws ConnectorExecutionException {
        if (connectorInputParameters.size() != inputValues.size()) {
            throw new ConnectorExecutionException("The number of input parameters is not consistent with the number of input values. Input parameters: "
                    + connectorInputParameters.size() + ", number of input values: " + inputValues.size());
        }
    }

    @Override
    public void setActivityStateByName(final long activityInstanceId, final String state) throws UpdateException {
        setActivityStateById(activityInstanceId, ModelConvertor.getServerActivityStateId(state));
    }

    @Override
    public void setActivityStateById(final long activityInstanceId, final int stateId) throws UpdateException {
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
            throw new UpdateException(e);
        }
    }

    @Override
    public void setTaskPriority(final long humanTaskInstanceId, final TaskPriority priority) throws UpdateException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final SetTaskPriority transactionContent = new SetTaskPriority(activityInstanceService, humanTaskInstanceId, STaskPriority.valueOf(priority.name()));
            transactionExecutor.execute(transactionContent);
        } catch (final SBonitaException e) {
            throw new UpdateException(e);
        }
    }

    @Override
    public void deleteProcessInstances(final long processDefinitionId) throws DeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            deleteProcessInstancesFromProcessDefinition(processDefinitionId, tenantAccessor);
            final DeleteArchivedProcessInstances transactionContent = new DeleteArchivedProcessInstances(processInstanceService, processDefinitionId,
                    tenantAccessor.getArchiveService());
            transactionExecutor.execute(transactionContent);
        } catch (final SProcessInstanceHierarchicalDeletionException e) {
            throw new ProcessInstanceHierarchicalDeletionException(e.getMessage(), e.getProcessInstanceId());
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
        } catch (final SearchException e) {
            throw new DeletionException(e);
        }
    }

    @Override
    public void deleteProcessInstance(final long processInstanceId) throws DeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        try {
            deleteProcessInstancesInsideLocks(tenantAccessor, false, processInstanceId);
        } catch (final SProcessInstanceHierarchicalDeletionException e) {
            throw new ProcessInstanceHierarchicalDeletionException(e.getMessage(), e.getProcessInstanceId());
        } catch (final SProcessInstanceNotFoundException e) {
            throw new DeletionException(e);
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
        }
    }

    @Override
    public SearchResult<ProcessInstance> searchOpenProcessInstances(final SearchOptions searchOptions) throws SearchException {
        // To select all process instances completed, without subprocess
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(searchOptions);
        searchOptionsBuilder.differentFrom(ProcessInstanceSearchDescriptor.STATE_ID, ProcessInstanceState.COMPLETED.getId());
        searchOptionsBuilder.filter(ProcessInstanceSearchDescriptor.CALLER_ID, -1);
        return searchProcessInstances(getTenantAccessor(), searchOptionsBuilder.done());
    }

    @Override
    public SearchResult<ProcessInstance> searchOpenProcessInstancesSupervisedBy(final long userId, final SearchOptions searchOptions) throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final GetSUser getUser = new GetSUser(identityService, userId);
        try {
            transactionExecutor.execute(getUser);
        } catch (final SBonitaException e) {
            return new SearchResultImpl<ProcessInstance>(0, Collections.<ProcessInstance> emptyList());
        }
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SearchOpenProcessInstancesSupervisedBy searchOpenProcessInstances = new SearchOpenProcessInstancesSupervisedBy(processInstanceService,
                searchEntitiesDescriptor.getProcessInstanceDescriptor(), userId, searchOptions, processDefinitionService);
        try {
            // TODO 2tx... do something with search
            transactionExecutor.execute(searchOpenProcessInstances);
            return searchOpenProcessInstances.getResult();
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
    }

    @Override
    public SearchResult<ProcessDeploymentInfo> searchProcessDeploymentInfosStartedBy(final long userId, final SearchOptions searchOptions)
            throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SearchProcessDefinitionsDescriptor searchDescriptor = searchEntitiesDescriptor.getProcessDefinitionsDescriptor();
        final SearchProcessDeploymentInfosStartedBy searcher = new SearchProcessDeploymentInfosStartedBy(processDefinitionService, searchDescriptor, userId,
                searchOptions);
        try {
            transactionExecutor.execute(searcher);
        } catch (final SBonitaException e) {
            throw new SearchException("Can't get ProcessDeploymentInfo startedBy userid " + userId, e);
        }
        return searcher.getResult();
    }

    @Override
    public SearchResult<ProcessDeploymentInfo> searchProcessDeploymentInfos(final SearchOptions searchOptions) throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SearchProcessDefinitionsDescriptor searchDescriptor = searchEntitiesDescriptor.getProcessDefinitionsDescriptor();
        final SearchProcessDeploymentInfos transactionSearch = new SearchProcessDeploymentInfos(processDefinitionService, searchDescriptor, searchOptions);
        try {
            transactionExecutor.execute(transactionSearch);
        } catch (final SBonitaException e) {
            throw new SearchException("Can't get processDefinition's executing searchProcessDefinitions()", e);
        }
        return transactionSearch.getResult();
    }

    @Override
    public SearchResult<ProcessDeploymentInfo> searchProcessDeploymentInfos(final long userId, final SearchOptions searchOptions) throws RetrieveException,
            SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SearchProcessDefinitionsDescriptor searchDescriptor = searchEntitiesDescriptor.getProcessDefinitionsDescriptor();
        final SearchProcessDeploymentInfosUserCanStart transactionSearch = new SearchProcessDeploymentInfosUserCanStart(processDefinitionService,
                searchDescriptor, searchOptions, userId);
        try {
            transactionExecutor.execute(transactionSearch);
        } catch (final SBonitaException e) {
            throw new SearchException("Error while retrieving process definitions: " + e.getMessage(), e);
        }
        return transactionSearch.getResult();

    }

    @Override
    public SearchResult<ProcessDeploymentInfo> searchProcessDeploymentInfosUsersManagedByCanStart(final long managerUserId, final SearchOptions searchOptions)
            throws SearchException {
        final TenantServiceAccessor serviceAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = serviceAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = serviceAccessor.getProcessDefinitionService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = serviceAccessor.getSearchEntitiesDescriptor();
        final SearchProcessDefinitionsDescriptor searchDescriptor = searchEntitiesDescriptor.getProcessDefinitionsDescriptor();
        final SearchProcessDeploymentInfosUsersManagedByCanStart transactionSearch = new SearchProcessDeploymentInfosUsersManagedByCanStart(
                processDefinitionService, searchDescriptor, searchOptions, managerUserId);
        try {
            transactionExecutor.execute(transactionSearch);
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
        return transactionSearch.getResult();
    }

    @Override
    public SearchResult<ProcessDeploymentInfo> searchProcessDeploymentInfosSupervisedBy(final long userId, final SearchOptions searchOptions)
            throws SearchException {
        final TenantServiceAccessor serviceAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = serviceAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = serviceAccessor.getProcessDefinitionService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = serviceAccessor.getSearchEntitiesDescriptor();
        final SearchProcessDefinitionsDescriptor searchDescriptor = searchEntitiesDescriptor.getProcessDefinitionsDescriptor();
        final SearchProcessDeploymentInfosSupervised searcher = new SearchProcessDeploymentInfosSupervised(processDefinitionService, searchDescriptor,
                searchOptions, userId);
        try {
            transactionExecutor.execute(searcher);
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
        return searcher.getResult();
    }

    @Override
    public SearchResult<HumanTaskInstance> searchAssignedTasksSupervisedBy(final long supervisorId, final SearchOptions searchOptions) throws SearchException {
        final TenantServiceAccessor serviceAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = serviceAccessor.getTransactionExecutor();
        final FlowNodeStateManager flowNodeStateManager = serviceAccessor.getFlowNodeStateManager();
        final ActivityInstanceService activityInstanceService = serviceAccessor.getActivityInstanceService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = serviceAccessor.getSearchEntitiesDescriptor();
        final SearchAssignedTasksSupervisedBy searchedTasksTransaction = new SearchAssignedTasksSupervisedBy(supervisorId, activityInstanceService,
                flowNodeStateManager, searchEntitiesDescriptor.getHumanTaskInstanceDescriptor(), searchOptions);
        try {
            transactionExecutor.execute(searchedTasksTransaction);
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
        return searchedTasksTransaction.getResult();

    }

    @Override
    public SearchResult<ArchivedHumanTaskInstance> searchArchivedHumanTasksSupervisedBy(final long supervisorId, final SearchOptions searchOptions)
            throws SearchException {
        final TenantServiceAccessor serviceAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = serviceAccessor.getTransactionExecutor();
        final FlowNodeStateManager flowNodeStateManager = serviceAccessor.getFlowNodeStateManager();
        final ActivityInstanceService activityInstanceService = serviceAccessor.getActivityInstanceService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = serviceAccessor.getSearchEntitiesDescriptor();
        final SearchArchivedTasksSupervisedBy searchedTasksTransaction = new SearchArchivedTasksSupervisedBy(supervisorId, activityInstanceService,
                flowNodeStateManager, searchEntitiesDescriptor.getArchivedHumanTaskInstanceDescriptor(), searchOptions);

        try {
            transactionExecutor.execute(searchedTasksTransaction);
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
        return searchedTasksTransaction.getResult();
    }

    @Override
    public SearchResult<ProcessSupervisor> searchProcessSupervisors(final SearchOptions searchOptions) throws SearchException {
        final TenantServiceAccessor serviceAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = serviceAccessor.getTransactionExecutor();
        final SupervisorMappingService supervisorService = serviceAccessor.getSupervisorService();
        final SProcessSupervisorBuilders supervisorBuilders = serviceAccessor.getSSupervisorBuilders();
        final IdentityModelBuilder identityModelBuilder = serviceAccessor.getIdentityModelBuilder();

        final SearchProcessSupervisorDescriptor searchDescriptor = new SearchProcessSupervisorDescriptor(supervisorBuilders, identityModelBuilder);
        final SearchSupervisors searchSupervisorsTransaction = new SearchSupervisors(supervisorService, searchDescriptor, searchOptions);
        try {
            transactionExecutor.execute(searchSupervisorsTransaction);
            return searchSupervisorsTransaction.getResult();
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
    }

    @Override
    public boolean isUserProcessSupervisor(final long processDefinitionId, final long userId) {
        final TenantServiceAccessor serviceAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = serviceAccessor.getTransactionExecutor();
        final SupervisorMappingService supervisorService = serviceAccessor.getSupervisorService();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                return supervisorService.isProcessSupervisor(processDefinitionId, userId);
            } catch (final SBonitaReadException e) {
                transactionExecutor.setTransactionRollback();
                throw new RetrieveException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public void deleteSupervisor(final long supervisorId) throws DeletionException {
        final TenantServiceAccessor serviceAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = serviceAccessor.getTransactionExecutor();
        final SupervisorMappingService supervisorService = serviceAccessor.getSupervisorService();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                supervisorService.deleteSupervisor(supervisorId);
            } catch (final SSupervisorNotFoundException e) {
                transactionExecutor.setTransactionRollback();
                throw new DeletionException("supervisor not found with id " + supervisorId);
            } catch (final SSupervisorDeletionException e) {
                transactionExecutor.setTransactionRollback();
                throw new DeletionException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new DeletionException(e);
        }
    }

    @Override
    public void deleteSupervisor(final Long processId, final Long userId, final Long roleId, final Long groupId) throws DeletionException {
        final TenantServiceAccessor serviceAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = serviceAccessor.getTransactionExecutor();
        final SupervisorMappingService supervisorService = serviceAccessor.getSupervisorService();
        final SProcessSupervisorBuilders supervisorBuilders = serviceAccessor.getSSupervisorBuilders();
        final IdentityModelBuilder identityModelBuilder = serviceAccessor.getIdentityModelBuilder();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();

            // Prepare search options
            final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 1);
            searchOptionsBuilder.sort(ProcessSupervisorSearchDescriptor.ID, Order.ASC);
            searchOptionsBuilder.filter(ProcessSupervisorSearchDescriptor.PROCESS_DEFINITION_ID, processId == null ? -1 : processId);
            searchOptionsBuilder.filter(ProcessSupervisorSearchDescriptor.USER_ID, userId == null ? -1 : userId);
            searchOptionsBuilder.filter(ProcessSupervisorSearchDescriptor.ROLE_ID, roleId == null ? -1 : roleId);
            searchOptionsBuilder.filter(ProcessSupervisorSearchDescriptor.GROUP_ID, groupId == null ? -1 : groupId);
            final SearchProcessSupervisorDescriptor searchDescriptor = new SearchProcessSupervisorDescriptor(supervisorBuilders, identityModelBuilder);
            final SearchSupervisors searchSupervisorsTransaction = new SearchSupervisors(supervisorService, searchDescriptor, searchOptionsBuilder.done());

            try {
                // Search the supervisor corresponding to criteria
                searchSupervisorsTransaction.execute();
                final SearchResult<ProcessSupervisor> result = searchSupervisorsTransaction.getResult();

                if (result.getCount() > 0) {
                    // Then, delete it
                    final List<ProcessSupervisor> processSupervisors = result.getResult();
                    supervisorService.deleteSupervisor(processSupervisors.get(0).getSupervisorId());
                } else {
                    throw new SSupervisorNotFoundException("No supervisor was found with userId = " + userId + ", roleId = " + roleId + ", groupId = "
                            + groupId + ", processId = " + processId);
                }
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new DeletionException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new DeletionException(e);
        }
    }

    @Override
    public ProcessSupervisor createProcessSupervisorForUser(final long processDefinitionId, final long userId) throws CreationException, AlreadyExistsException {
        return createSupervisor(processDefinitionId, userId, null, null, MemberType.USER);
    }

    @Override
    public ProcessSupervisor createProcessSupervisorForRole(final long processDefinitionId, final long roleId) throws CreationException, AlreadyExistsException {
        return createSupervisor(processDefinitionId, null, null, roleId, MemberType.ROLE);
    }

    @Override
    public ProcessSupervisor createProcessSupervisorForGroup(final long processDefinitionId, final long groupId) throws CreationException,
            AlreadyExistsException {
        return createSupervisor(processDefinitionId, null, groupId, null, MemberType.GROUP);
    }

    @Override
    public ProcessSupervisor createProcessSupervisorForMembership(final long processDefinitionId, final long groupId, final long roleId)
            throws CreationException, AlreadyExistsException {
        return createSupervisor(processDefinitionId, null, groupId, roleId, MemberType.MEMBERSHIP);
    }

    private ProcessSupervisor createSupervisor(final long processDefinitionId, final Long userId, final Long groupId, final Long roleId,
            final MemberType memberType) throws CreationException, AlreadyExistsException {
        SProcessSupervisor supervisor;
        final TenantServiceAccessor serviceAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = serviceAccessor.getTransactionExecutor();
        final SupervisorMappingService supervisorService = serviceAccessor.getSupervisorService();
        final SProcessSupervisorBuilder supervisorBuilder = serviceAccessor.getSSupervisorBuilders().getSSupervisorBuilder();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                supervisorBuilder.createNewInstance(processDefinitionId);
                switch (memberType) {
                    case USER:
                        supervisorBuilder.setUserId(userId);
                        break;

                    case GROUP:
                        supervisorBuilder.setGroupId(groupId);
                        break;

                    case ROLE:
                        supervisorBuilder.setRoleId(roleId);
                        break;

                    case MEMBERSHIP:
                        supervisorBuilder.setGroupId(groupId);
                        supervisorBuilder.setRoleId(roleId);
                        break;
                }

                supervisor = supervisorBuilder.done();
                supervisor = supervisorService.createSupervisor(supervisor);
                return ModelConvertor.toProcessSupervisor(supervisor);
            } catch (final SSupervisorAlreadyExistsException e) {
                transactionExecutor.setTransactionRollback();
                throw new AlreadyExistsException("This supervisor already exists");
            } catch (final SSupervisorCreationException e) {
                transactionExecutor.setTransactionRollback();
                throw new CreationException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new CreationException(e);
        }
    }

    @Override
    public SearchResult<ProcessDeploymentInfo> searchUncategorizedProcessDeploymentInfosUserCanStart(final long userId, final SearchOptions searchOptions)
            throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SearchProcessDefinitionsDescriptor searchDescriptor = searchEntitiesDescriptor.getProcessDefinitionsDescriptor();
        final SearchUncategorizedProcessDeploymentInfosUserCanStart transactionSearch = new SearchUncategorizedProcessDeploymentInfosUserCanStart(
                processDefinitionService, searchDescriptor, searchOptions, userId);
        try {
            transactionExecutor.execute(transactionSearch);
        } catch (final SBonitaException e) {
            throw new SearchException("Error while retrieving process definitions", e);
        }
        return transactionSearch.getResult();
    }

    @Override
    public SearchResult<ArchivedHumanTaskInstance> searchArchivedHumanTasksManagedBy(final long managerUserId, final SearchOptions searchOptions)
            throws SearchException {
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
            throw new SearchException(e);
        }
        return searchTransaction.getResult();
    }

    @Override
    public SearchResult<ProcessInstance> searchOpenProcessInstancesInvolvingUser(final long userId, final SearchOptions searchOptions) throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SearchOpenProcessInstancesInvolvingUser searchOpenProcessInstances = new SearchOpenProcessInstancesInvolvingUser(processInstanceService,
                searchEntitiesDescriptor.getProcessInstanceDescriptor(), userId, searchOptions, processDefinitionService);
        try {
            transactionExecutor.execute(searchOpenProcessInstances);
            return searchOpenProcessInstances.getResult();
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
    }

    @Override
    public SearchResult<ProcessInstance> searchOpenProcessInstancesInvolvingUsersManagedBy(final long managerUserId, final SearchOptions searchOptions)
            throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SearchOpenProcessInstancesInvolvingUsersManagedBy searchOpenProcessInstances = new SearchOpenProcessInstancesInvolvingUsersManagedBy(
                processInstanceService, searchEntitiesDescriptor.getProcessInstanceDescriptor(), managerUserId, searchOptions, processDefinitionService);
        try {
            transactionExecutor.execute(searchOpenProcessInstances);
            return searchOpenProcessInstances.getResult();
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
    }

    @Override
    public SearchResult<ArchivedHumanTaskInstance> searchArchivedHumanTasks(final SearchOptions searchOptions) throws SearchException {
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
            throw new SearchException(sbe);
        }
        return searchArchivedTasks.getResult();
    }

    @Override
    public SearchResult<HumanTaskInstance> searchAssignedTasksManagedBy(final long managerUserId, final SearchOptions searchOptions) throws SearchException {
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
            throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final SearchArchivedProcessInstancesSupervisedBy searchArchivedProcessInstances = new SearchArchivedProcessInstancesSupervisedBy(userId,
                processInstanceService, searchEntitiesDescriptor.getArchivedProcessInstancesDescriptor(), searchOptions, persistenceService);
        try {
            transactionExecutor.execute(searchArchivedProcessInstances);
            return searchArchivedProcessInstances.getResult();
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
    }

    @Override
    public SearchResult<ArchivedProcessInstance> searchArchivedProcessInstancesInvolvingUser(final long userId, final SearchOptions searchOptions)
            throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final SearchArchivedProcessInstancesInvolvingUser searchArchivedProcessInstances = new SearchArchivedProcessInstancesInvolvingUser(userId,
                processInstanceService, searchEntitiesDescriptor.getArchivedProcessInstancesDescriptor(), searchOptions, persistenceService);
        try {
            transactionExecutor.execute(searchArchivedProcessInstances);
            return searchArchivedProcessInstances.getResult();
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
    }

    @Override
    public SearchResult<HumanTaskInstance> searchPendingTasksForUser(final long userId, final SearchOptions searchOptions) throws SearchException {
        return searchTasksForUser(userId, searchOptions, false);
    }

    /**
     * @param orAssignedToUser
     *            do we also want to retrieve tasks directly assigned to this user ?
     * @throws SearchException
     */
    private SearchResult<HumanTaskInstance> searchTasksForUser(final long userId, final SearchOptions searchOptions, final boolean orAssignedToUser)
            throws SearchException {
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
            throw new SearchException(sbe);
        }
    }

    @Override
    public SearchResult<HumanTaskInstance> searchMyAvailableHumanTasks(final long userId, final SearchOptions searchOptions) throws SearchException {
        return searchTasksForUser(userId, searchOptions, true);
    }

    @Override
    public SearchResult<HumanTaskInstance> searchPendingTasksSupervisedBy(final long userId, final SearchOptions searchOptions) throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SearchPendingTasksSupervisedBy searchPendingTasksSupervisedBy = new SearchPendingTasksSupervisedBy(activityInstanceService, flowNodeStateManager,
                searchEntitiesDescriptor.getHumanTaskInstanceDescriptor(), userId, searchOptions);
        try {
            // TODO 2 transaction... must do something about search
            transactionExecutor.execute(searchPendingTasksSupervisedBy);
            return searchPendingTasksSupervisedBy.getResult();
        } catch (final SBonitaException sbe) {
            throw new BonitaRuntimeException(sbe);
        }
    }

    @Override
    public SearchResult<Comment> searchComments(final SearchOptions searchOptions) throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SCommentService commentService = tenantAccessor.getCommentService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SearchComments searchComments = new SearchComments(searchEntitiesDescriptor.getCommentDescriptor(), searchOptions, commentService);
        try {
            transactionExecutor.execute(searchComments);
            return searchComments.getResult();
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
    }

    @Override
    public Comment addComment(final long processInstanceId, final String comment) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SCommentService commentService = tenantAccessor.getCommentService();
        final AddComment addComment = new AddComment(commentService, processInstanceId, comment);
        try {
            transactionExecutor.execute(addComment);
            final SComment sComment = addComment.getResult();
            return ModelConvertor.toComment(sComment);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public List<Comment> getComments(final long processInstanceId) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SCommentService commentService = tenantAccessor.getCommentService();
        final GetComments getComments = new GetComments(commentService, processInstanceId);
        try {
            transactionExecutor.execute(getComments);
            final List<SComment> sComments = getComments.getResult();
            return ModelConvertor.toComments(sComments);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public Document attachDocument(final long processInstanceId, final String documentName, final String fileName, final String mimeType, final String url)
            throws ProcessInstanceNotFoundException, DocumentAttachmentException {
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
        return attachDocumentTransationContent.getResult();
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
            final byte[] documentContent) throws ProcessInstanceNotFoundException, DocumentAttachmentException {
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
        return attachDocumentTransationContent.getResult();
    }

    @Override
    public Document attachNewDocumentVersion(final long processInstanceId, final String documentName, final String fileName, final String mimeType,
            final String url) throws DocumentAttachmentException {
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
            final String contentMimeType, final byte[] documentContent) throws DocumentAttachmentException {
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
    public Document getDocument(final long documentId) throws DocumentNotFoundException {
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
    public List<Document> getLastVersionOfDocuments(final long processInstanceId, final int pageIndex, final int numberPerPage,
            final DocumentCriterion pagingCriterion) throws ProcessInstanceNotFoundException, DocumentException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDocumentService processDocumentService = tenantAccessor.getProcessDocumentService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SDocumentMappingBuilderAccessor builder = tenantAccessor.getDocumentMappingBuilderAccessor();
        final OrderAndField orderAndField = OrderAndFields.getOrderAndFieldForDocument(pagingCriterion, builder.getSDocumentMappingBuilder());
        try {
            final GetDocumentsOfProcessInstance transationContent = new GetDocumentsOfProcessInstance(processDocumentService, processInstanceId, pageIndex,
                    numberPerPage, orderAndField.getField(), orderAndField.getOrder());
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
    public byte[] getDocumentContent(final String documentStorageId) throws DocumentNotFoundException {
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
    public Document getLastDocument(final long processInstaneId, final String documentName) throws DocumentNotFoundException {
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
    public long getNumberOfDocuments(final long processInstanceId) throws DocumentException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDocumentService processDocumentService = tenantAccessor.getProcessDocumentService();
        final TransactionExecutor transactionExecutor = getTenantAccessor().getTransactionExecutor();
        try {
            final GetNumberOfDocumentsOfProcessInstance transationContent = new GetNumberOfDocumentsOfProcessInstance(processDocumentService, processInstanceId);
            transactionExecutor.execute(transationContent);
            return transationContent.getResult();

        } catch (final SBonitaException sbe) {
            throw new DocumentException(sbe);
        }
    }

    @Override
    public Document getDocumentAtProcessInstantiation(final long processInstanceId, final String documentName) throws DocumentNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDocumentService processDocumentService = tenantAccessor.getProcessDocumentService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final SAProcessInstanceBuilder saProcessInstanceBuilder = tenantAccessor.getBPMInstanceBuilders().getSAProcessInstanceBuilder();
        final ReadPersistenceService persistenceService = tenantAccessor.getArchiveService().getDefinitiveArchiveReadPersistenceService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();

        try {
            final GetDocumentByNameAtProcessInstantiation transationContent = new GetDocumentByNameAtProcessInstantiation(processDocumentService,
                    persistenceService, processInstanceService, saProcessInstanceBuilder, searchEntitiesDescriptor, processInstanceId, documentName);
            transactionExecutor.execute(transationContent);
            final SProcessDocument attachment = transationContent.getResult();
            return ModelConvertor.toDocument(attachment);
        } catch (final SBonitaException sbe) {
            throw new DocumentNotFoundException(sbe);
        }
    }

    @Override
    public Document getDocumentAtActivityInstanceCompletion(final long activityInstanceId, final String documentName) throws DocumentNotFoundException {
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
    public SearchResult<HumanTaskInstance> searchPendingTasksManagedBy(final long managerUserId, final SearchOptions searchOptions) throws SearchException {
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
    public Map<Long, Long> getNumberOfOverdueOpenTasks(final List<Long> userIds) throws RetrieveException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final GetNumberOfOverdueOpenTasksForUsers transactionContent = new GetNumberOfOverdueOpenTasksForUsers(userIds, activityInstanceService);
            transactionExecutor.execute(transactionContent);
            return transactionContent.getResult();
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new RetrieveException(e.getMessage());
        }
    }

    @Override
    public SearchResult<ProcessDeploymentInfo> searchUncategorizedProcessDeploymentInfos(final SearchOptions searchOptions) throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SearchProcessDefinitionsDescriptor searchDescriptor = searchEntitiesDescriptor.getProcessDefinitionsDescriptor();
        final SearchUncategorizedProcessDeploymentInfos transactionSearch = new SearchUncategorizedProcessDeploymentInfos(processDefinitionService,
                searchDescriptor, searchOptions);
        try {
            transactionExecutor.execute(transactionSearch);
        } catch (final SBonitaException e) {
            throw new SearchException("Problem encountered while searching for Uncategorized Process Definitions", e);
        }
        return transactionSearch.getResult();
    }

    @Override
    public SearchResult<Comment> searchCommentsManagedBy(final long managerUserId, final SearchOptions searchOptions) throws SearchException {
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
            throw new SearchException(sbe);
        }
    }

    @Override
    public SearchResult<Comment> searchCommentsInvolvingUser(final long userId, final SearchOptions searchOptions) throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SCommentService commentService = tenantAccessor.getCommentService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SearchCommentsInvolvingUser searchComments = new SearchCommentsInvolvingUser(searchEntitiesDescriptor.getCommentDescriptor(), searchOptions,
                commentService, userId);
        try {
            transactionExecutor.execute(searchComments);
            return searchComments.getResult();
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
    }

    @Override
    public List<Long> getChildrenInstanceIdsOfProcessInstance(final long processInstanceId, final int startIndex, final int maxResults,
            final ProcessInstanceCriterion criterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SAProcessInstanceBuilder modelBuilder = tenantAccessor.getBPMInstanceBuilders().getSAProcessInstanceBuilder();
        long totalNumber;
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                totalNumber = processInstanceService.getNumberOfChildInstancesOfProcessInstance(processInstanceId);
                if (totalNumber == 0) {
                    return Collections.emptyList();
                }
                final OrderAndField orderAndField = OrderAndFields.getOrderAndFieldForProcessInstance(criterion, modelBuilder);
                return processInstanceService.getChildInstanceIdsOfProcessInstance(processInstanceId, startIndex, maxResults, orderAndField.getField(),
                        orderAndField.getOrder());
            } catch (final SProcessInstanceReadException e) {
                // no rollback, read only tx
                throw new RetrieveException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public SearchResult<ProcessDeploymentInfo> searchUncategorizedProcessDeploymentInfosSupervisedBy(final long userId, final SearchOptions searchOptions)
            throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SearchProcessDefinitionsDescriptor searchDescriptor = searchEntitiesDescriptor.getProcessDefinitionsDescriptor();
        final SearchUncategorizedProcessDeploymentInfosSupervisedBy transactionSearch = new SearchUncategorizedProcessDeploymentInfosSupervisedBy(
                processDefinitionService, searchDescriptor, searchOptions, userId);
        try {
            transactionExecutor.execute(transactionSearch);
        } catch (final SBonitaException e) {
            throw new SearchException("Problem encountered while searching for Uncategorized Process Definitions for a supervisor", e);
        }
        return transactionSearch.getResult();
    }

    @Override
    public Map<Long, ProcessDeploymentInfo> getProcessDeploymentInfosFromIds(final List<Long> processDefinitionIds) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final GetProcessDeploymentInfosFromIds processDefinitions = new GetProcessDeploymentInfosFromIds(processDefinitionIds, processDefinitionService);
            transactionExecutor.execute(processDefinitions);
            final List<ProcessDeploymentInfo> processDeploymentInfos = ModelConvertor.toProcessDeploymentInfo(processDefinitions.getResult());
            final Map<Long, ProcessDeploymentInfo> mProcessDefinitions = new HashMap<Long, ProcessDeploymentInfo>();
            for (final ProcessDeploymentInfo p : processDeploymentInfos) {
                mProcessDefinitions.put(p.getProcessId(), p);
            }
            return mProcessDefinitions;
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public List<ConnectorImplementationDescriptor> getConnectorImplementations(final long processDefinitionId, final int startIndex, final int maxsResults,
            final ConnectorCriterion sortingCriterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ConnectorService connectorService = tenantAccessor.getConnectorService();
        final OrderAndField orderAndField = OrderAndFields.getOrderAndFieldForConnectorImplementation(sortingCriterion);
        final GetConnectorImplementations transactionContent = new GetConnectorImplementations(connectorService, processDefinitionId,
                tenantAccessor.getTenantId(), startIndex, maxsResults, orderAndField.getField(), orderAndField.getOrder());
        try {
            transactionExecutor.execute(transactionContent);
            final List<SConnectorImplementationDescriptor> sConnectorImplementationDescriptors = transactionContent.getResult();
            return ModelConvertor.toConnectorImplementationDescriptors(sConnectorImplementationDescriptors);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public long getNumberOfConnectorImplementations(final long processDefinitionId) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ConnectorService connectorService = tenantAccessor.getConnectorService();
        final GetNumberOfConnectorImplementations transactionContent = new GetNumberOfConnectorImplementations(connectorService, processDefinitionId,
                tenantAccessor.getTenantId());
        try {
            transactionExecutor.execute(transactionContent);
            return transactionContent.getResult();
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public SearchResult<ActivityInstance> searchActivities(final SearchOptions searchOptions) throws SearchException {
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
    public SearchResult<ArchivedFlowNodeInstance> searchArchivedFlowNodeInstances(final SearchOptions searchOptions) throws SearchException {
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
    public SearchResult<FlowNodeInstance> searchFlowNodeInstances(final SearchOptions searchOptions) throws SearchException {
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
                    case RECEIVE_TASK:
                        entityClass = SReceiveTaskInstance.class;
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
    public SearchResult<ArchivedActivityInstance> searchArchivedActivities(final SearchOptions searchOptions) throws SearchException {
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
                    case RECEIVE_TASK:
                        entityClass = SAReceiveTaskInstance.class;
                        break;
                    case SEND_TASK:
                        entityClass = SASendTaskInstance.class;
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
    public ConnectorImplementationDescriptor getConnectorImplementation(final long processDefinitionId, final String connectorId, final String connectorVersion)
            throws ConnectorNotFoundException {
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
    public void cancelProcessInstance(final long processInstanceId) throws ProcessInstanceNotFoundException, RetrieveException, UpdateException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final BPMInstanceBuilders bpmInstanceBuilders = tenantAccessor.getBPMInstanceBuilders();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final ProcessExecutor processExecutor = tenantAccessor.getProcessExecutor();
        final LockService lockService = tenantAccessor.getLockService();

        final TransactionalProcessInstanceInterruptor processInstanceInterruptor = new TransactionalProcessInstanceInterruptor(bpmInstanceBuilders,
                processInstanceService, activityInstanceService, transactionExecutor, processExecutor, lockService, tenantAccessor.getTechnicalLoggerService());

        try {
            processInstanceInterruptor.interruptProcessInstance(processInstanceId, SStateCategory.CANCELLING, getUserIdFromSession());
        } catch (final SBonitaException e) {
            throw new UpdateException(e);
        }
    }

    @Override
    public void setProcessInstanceState(final ProcessInstance processInstance, final String state) throws UpdateException {
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
            throw new UpdateException(e.getMessage());
        }
    }

    @Override
    public Map<Long, ProcessDeploymentInfo> getProcessDeploymentInfosFromProcessInstanceIds(final List<Long> processInstantsIds) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final GetProcessDefinitionDeployInfoFromProcessInstanceIds processDefinitions = new GetProcessDefinitionDeployInfoFromProcessInstanceIds(
                processInstantsIds, processDefinitionService);
        try {
            transactionExecutor.execute(processDefinitions);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
        final List<Map<String, String>> sProcessDeploymentInfos = processDefinitions.getResult();
        return getProcessDeploymentInfosFromMap(sProcessDeploymentInfos);

    }

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
        ActivationState activationState = null;
        ConfigurationState configurationState = null;
        String displayName = "";
        long lastUpdateDate = 0;
        String iconPath = "";
        String displayDescription = "";
        for (final Map<String, String> m : sProcessDeploymentInfos) {
            for (final Entry<String, String> entry : m.entrySet()) {
                final String key = entry.getKey();
                final Object value = entry.getValue();
                if ("processInstanceId".equals(key)) {
                    processInstanceId = Long.parseLong(value.toString());
                } else if ("id".equals(key)) {
                    id = Long.parseLong(value.toString());
                } else if ("processId".equals(key)) {
                    processId = Long.parseLong(value.toString());
                } else if ("name".equals(key)) {
                    name = m.get(key);
                } else if ("version".equals(key)) {
                    version = m.get(key);
                } else if ("description".equals(key)) {
                    description = String.valueOf(m.get(key));
                } else if ("deploymentDate".equals(key)) {
                    deploymentDate = Long.parseLong(value.toString());
                } else if ("deployedBy".equals(key)) {
                    deployedBy = Long.parseLong(value.toString());
                } else if ("activationState".equals(key)) {
                    activationState = ActivationState.valueOf(m.get(key));
                } else if ("configurationState".equals(key)) {
                    configurationState = ConfigurationState.valueOf(m.get(key));
                } else if ("displayName".equals(key)) {
                    displayName = m.get(key);
                } else if ("lastUpdateDate".equals(key)) {
                    lastUpdateDate = Long.parseLong(value.toString());
                } else if ("iconPath".equals(key)) {
                    iconPath = m.get(key);
                } else if ("displayDescription".equals(key)) {
                    displayDescription = String.valueOf(m.get(key));
                }
            }
            final ProcessDeploymentInfoImpl pDeplInfoImpl = new ProcessDeploymentInfoImpl(id, processId, name, version, description, new Date(deploymentDate),
                    deployedBy, activationState, configurationState, displayName, new Date(lastUpdateDate), iconPath, displayDescription);
            mProcessDeploymentInfos.put(processInstanceId, pDeplInfoImpl);
        }
        return mProcessDeploymentInfos;
    }

    @Override
    public SearchResult<Document> searchDocuments(final SearchOptions searchOptions) throws SearchException {
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
    public SearchResult<Document> searchDocumentsSupervisedBy(final long userId, final SearchOptions searchOptions) throws UserNotFoundException,
            SearchException {
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
    public SearchResult<ArchivedDocument> searchArchivedDocuments(final SearchOptions searchOptions) throws SearchException {
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
            throws UserNotFoundException, SearchException {
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
    public void retryTask(final long activityInstanceId) throws ActivityInstanceNotFoundException, ActivityExecutionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final FlowNodeExecutor flowNodeExecutor = tenantAccessor.getFlowNodeExecutor();
        final ProcessExecutor processExecutor = tenantAccessor.getProcessExecutor();
        final SAutomaticTaskInstanceBuilder keyProvider = tenantAccessor.getBPMInstanceBuilders().getSAutomaticTaskInstanceBuilder();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        int stateId = -1;
        FlowNodeState state = null;
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            final SFlowNodeInstance activity;
            final FlowNodeState flowNodeState;
            final SProcessDefinition processDefinition;
            try {
                activity = activityInstanceService.getFlowNodeInstance(activityInstanceId);
                processDefinition = processDefinitionService.getProcessDefinition(activity.getLogicalGroup(keyProvider.getProcessDefinitionIndex()));
                flowNodeState = flowNodeStateManager.getState(activity.getStateId());
                stateId = activity.getPreviousStateId();
                state = flowNodeStateManager.getState(stateId);
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new ActivityExecutionException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
            if (!ActivityStates.FAILED_STATE.equals(flowNodeState.getName())) {
                throw new ActivityExecutionException("Unable to retry a task that is not failed - task name=" + activity.getName() + " id="
                        + activityInstanceId + " that was in state " + flowNodeState);
            }
            // this should not open a new transaction (it's ok now because transaction executor don't open a new one)
            try {
                flowNodeExecutor.setStateByStateId(processDefinition, activity, stateId);
                // execute the flow node only if it is not the final state
                if (!state.isTerminal()) {
                    processExecutor.executeActivity(activityInstanceId, getUserIdFromSession());
                }
            } catch (final SBonitaException e) {
                throw new ActivityExecutionException(e);
            }
        } catch (final STransactionException e) {
            throw new ActivityExecutionException(e);
        }
    }

    @Override
    public ArchivedDocument getArchivedVersionOfProcessDocument(final long sourceObjectId) throws ArchivedDocumentNotFoundException {
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
    public ArchivedDocument getArchivedProcessDocument(final long archivedProcessDocumentId) throws ArchivedDocumentNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDocumentService processDocumentService = tenantAccessor.getProcessDocumentService();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final SAProcessDocument archivedDocument = processDocumentService.getArchivedDocument(archivedProcessDocumentId, persistenceService);
                return ModelConvertor.toArchivedDocument(archivedDocument);
            } catch (final SDocumentNotFoundException e) {
                transactionExecutor.setTransactionRollback();
                throw new ArchivedDocumentNotFoundException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e1) {
            throw new ArchivedDocumentNotFoundException(e1);
        }
    }

    @Override
    public SearchResult<ArchivedComment> searchArchivedComments(final SearchOptions searchOptions) throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SCommentService sCommentService = tenantAccessor.getCommentService();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final SearchArchivedComments searchArchivedComments = new SearchArchivedComments(sCommentService,
                searchEntitiesDescriptor.getArchivedCommentsDescriptor(), searchOptions, persistenceService);
        try {
            transactionExecutor.execute(searchArchivedComments);
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
        return searchArchivedComments.getResult();
    }

    @Override
    public ArchivedComment getArchivedComment(final long archivedCommentId) throws RetrieveException, NotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SCommentService sCommentService = tenantAccessor.getCommentService();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final SAComment archivedComment = sCommentService.getArchivedComment(archivedCommentId, persistenceService);
                return ModelConvertor.toArchivedComment(archivedComment);
            } catch (final SCommentNotFoundException e) {
                transactionExecutor.setTransactionRollback();
                throw new NotFoundException(e);
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new RetrieveException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e1) {
            throw new RetrieveException(e1);
        }
    }

    @Override
    public Map<Long, ActorInstance> getActorsFromActorIds(final List<Long> actorIds) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final Map<Long, ActorInstance> res = new HashMap<Long, ActorInstance>();
        final ActorMappingService actormappingService = tenantAccessor.getActorMappingService();
        final GetActorsByActorIds getActorsByActorIds = new GetActorsByActorIds(actormappingService, actorIds);
        try {
            transactionExecutor.execute(getActorsByActorIds);
        } catch (final SBonitaException e1) {
            throw new RetrieveException(e1);
        }
        final List<SActor> actors = getActorsByActorIds.getResult();
        for (final SActor actor : actors) {
            res.put(actor.getId(), ModelConvertor.toActorInstance(actor));
        }
        return res;
    }

    @Override
    public Map<Long, ProcessDeploymentInfo> getProcessDeploymentInfosFromArchivedProcessInstanceIds(final List<Long> archivedProcessInstantsIds) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final GetProcessDefinitionDeployInfoFromArchivedProcessInstanceIds getProcessDeploymentInfoFromArchivedProcessInstanceIds = new GetProcessDefinitionDeployInfoFromArchivedProcessInstanceIds(
                archivedProcessInstantsIds, processDefinitionService);
        try {
            transactionExecutor.execute(getProcessDeploymentInfoFromArchivedProcessInstanceIds);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
        final Map<Long, SProcessDefinitionDeployInfo> sProcessDeploymentInfos = getProcessDeploymentInfoFromArchivedProcessInstanceIds.getResult();
        if (sProcessDeploymentInfos != null && !sProcessDeploymentInfos.isEmpty()) {
            final Map<Long, ProcessDeploymentInfo> processDeploymentInfos = new HashMap<Long, ProcessDeploymentInfo>();
            final Set<Entry<Long, SProcessDefinitionDeployInfo>> entries = sProcessDeploymentInfos.entrySet();
            for (final Entry<Long, SProcessDefinitionDeployInfo> entry : entries) {
                processDeploymentInfos.put(entry.getKey(), ModelConvertor.toProcessDeploymentInfo(entry.getValue()));
            }
            return processDeploymentInfos;
        }
        return Collections.emptyMap();
    }

    @Override
    public SearchResult<HumanTaskInstance> searchPendingHiddenTasks(final long userId, final SearchOptions searchOptions) throws SearchException {
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
    public void hideTasks(final long userId, final Long... activityInstanceId) throws UpdateException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final TransactionContent hideTasksTx = new HideTasks(tenantAccessor.getActivityInstanceService(), userId, activityInstanceId);
        try {
            transactionExecutor.execute(hideTasksTx);
        } catch (final SBonitaException e) {
            throw new UpdateException("Error while trying to hide tasks: " + Arrays.toString(activityInstanceId) + " from user with ID " + userId, e);
        }
    }

    @Override
    public void unhideTasks(final long userId, final Long... activityInstanceId) throws UpdateException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final TransactionContent unhideTasksTx = new UnhideTasks(tenantAccessor.getActivityInstanceService(), userId, activityInstanceId);
        try {
            transactionExecutor.execute(unhideTasksTx);
        } catch (final SBonitaException e) {
            throw new UpdateException("Error while trying to un-hide tasks: " + Arrays.toString(activityInstanceId) + " from user with ID " + userId, e);
        }
    }

    @Override
    public Serializable evaluateExpressionOnProcessDefinition(final Expression expression, final Map<String, Serializable> context,
            final long processDefinitionId) throws ExpressionEvaluationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ExpressionResolverService expressionResolverService = tenantAccessor.getExpressionResolverService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SExpressionBuilders sExpressionBuilders = tenantAccessor.getSExpressionBuilders();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SExpression sExpression = ServerModelConvertor.convertExpression(sExpressionBuilders, expression);
        final SExpressionContext expcontext = new SExpressionContext();
        expcontext.setProcessDefinitionId(processDefinitionId);
        SProcessDefinition processDef;
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                processDef = processDefinitionService.getProcessDefinition(processDefinitionId);
                if (processDef != null) {
                    expcontext.setProcessDefinition(processDef);
                }
                final HashMap<String, Object> hashMap = new HashMap<String, Object>(context);
                expcontext.setInputValues(hashMap);
                return (Serializable) expressionResolverService.evaluate(sExpression, expcontext);
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new ExpressionEvaluationException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new ExpressionEvaluationException(e);
        }
    }

    @Override
    public void updateDueDateOfTask(final long userTaskId, final Date dueDate) throws UpdateException {
        if (dueDate == null) {
            throw new UpdateException("Unable to update a due date to null");
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        try {
            final SetExpectedEndDate updateProcessInstance = new SetExpectedEndDate(activityInstanceService, userTaskId, dueDate);
            transactionExecutor.execute(updateProcessInstance);
        } catch (final SFlowNodeNotFoundException e) {
            throw new UpdateException(e);
        } catch (final SBonitaException e) {
            throw new UpdateException(e);
        }
    }

    @Override
    public boolean isTaskHidden(final long userTaskId, final long userId) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final IsTaskHidden hideTasksTx = new IsTaskHidden(tenantAccessor.getActivityInstanceService(), userId, userTaskId);
        try {
            transactionExecutor.execute(hideTasksTx);
            return hideTasksTx.getResult();
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public long countComments(final SearchOptions searchOptions) throws SearchException {
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 0).setFilters(searchOptions.getFilters()).searchTerm(
                searchOptions.getSearchTerm());
        final SearchResult<Comment> searchResult = searchComments(searchOptionsBuilder.done());
        return searchResult.getCount();
    }

    @Override
    public long countAttachments(final SearchOptions searchOptions) throws SearchException {
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 0).setFilters(searchOptions.getFilters()).searchTerm(
                searchOptions.getSearchTerm());
        final SearchResult<Document> searchResult = searchDocuments(searchOptionsBuilder.done());
        return searchResult.getCount();
    }

    @Override
    public void sendSignal(final String signalName) throws SendEventException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final EventsHandler eventsHandler = tenantAccessor.getEventsHandler();
        final SThrowSignalEventTriggerDefinitionBuilder signalEventTriggerDefinitionBuilder = tenantAccessor.getBPMDefinitionBuilders()
                .getThrowSignalEventTriggerDefinitionBuilder();
        final SThrowSignalEventTriggerDefinition signalEventTriggerDefinition = signalEventTriggerDefinitionBuilder.createNewInstance(signalName).done();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                eventsHandler.handleThrowEvent(signalEventTriggerDefinition);
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new SendEventException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }

        } catch (final STransactionException e) {
            throw new SendEventException(e);
        }
    }

    @Override
    public void sendMessage(final String messageName, final Expression targetProcess, final Expression targetFlowNode,
            final Map<Expression, Expression> messageContent) throws SendEventException {
        sendMessage(messageName, targetProcess, targetFlowNode, messageContent, null);
    }

    @Override
    public void sendMessage(final String messageName, final Expression targetProcess, final Expression targetFlowNode,
            final Map<Expression, Expression> messageContent, final Map<Expression, Expression> correlations) throws SendEventException {
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
        SExpression targetFlowNodeNameExp = null;
        if (targetFlowNode != null) {
            targetFlowNodeNameExp = ServerModelConvertor.convertExpression(sExpressionBuilders, targetFlowNode);
        }
        messageEventTriggerDefinitionBuilder.createNewInstance(messageName, targetProcessNameExp, targetFlowNodeNameExp);
        if (correlations != null && !correlations.isEmpty()) {
            addMessageCorrelations(messageEventTriggerDefinitionBuilder, sExpressionBuilders, correlations);
        }
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
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
                transactionExecutor.completeTransaction(txOpened);
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

    @Override
    public List<Problem> getProcessResolutionProblems(final long processId) throws ProcessDefinitionNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final List<ProcessDependencyResolver> resolvers = tenantAccessor.getDependencyResolver().getResolvers();
                SProcessDefinition processDefinition;
                try {
                    processDefinition = processDefinitionService.getProcessDefinition(processId);
                } catch (final SProcessDefinitionNotFoundException e) {
                    throw new ProcessDefinitionNotFoundException(e);
                } catch (final SProcessDefinitionReadException e) {
                    throw new ProcessDefinitionNotFoundException(e);
                }
                final ArrayList<Problem> problems = new ArrayList<Problem>();
                for (final ProcessDependencyResolver resolver : resolvers) {
                    final List<Problem> problem = resolver.checkResolution(tenantAccessor, processDefinition);
                    if (problem != null) {
                        problems.addAll(problem);
                    }
                }
                return problems;

            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException ste) {
            throw new RetrieveException(ste);
        }
    }

    @Override
    public List<ProcessDeploymentInfo> getProcessDeploymentInfos(final int startIndex, final int maxResults,
            final ProcessDeploymentInfoCriterion pagingCriterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchProcessDefinitionsDescriptor processDefinitionsDescriptor = tenantAccessor.getSearchEntitiesDescriptor().getProcessDefinitionsDescriptor();
        final GetProcessDefinitionDeployInfos transactionContentWithResult = new GetProcessDefinitionDeployInfos(processDefinitionService,
                processDefinitionsDescriptor, startIndex, maxResults, pagingCriterion);
        try {
            transactionExecutor.execute(transactionContentWithResult);
            return transactionContentWithResult.getResult();
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public List<ProcessDeploymentInfo> getProcessDeploymentInfosWithActorOnlyForGroup(final long groupId, final int startIndex, final int maxResults,
            final ProcessDeploymentInfoCriterion sortingCriterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchProcessDefinitionsDescriptor processDefinitionsDescriptor = tenantAccessor.getSearchEntitiesDescriptor().getProcessDefinitionsDescriptor();
        final GetProcessDefinitionDeployInfosWithActorOnlyForGroup transactionContentWithResult = new GetProcessDefinitionDeployInfosWithActorOnlyForGroup(
                processDefinitionService, processDefinitionsDescriptor, startIndex, maxResults, sortingCriterion, groupId);
        try {
            transactionExecutor.execute(transactionContentWithResult);
            return transactionContentWithResult.getResult();
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public List<ProcessDeploymentInfo> getProcessDeploymentInfosWithActorOnlyForGroups(final List<Long> groupIds, final int startIndex, final int maxResults,
            final ProcessDeploymentInfoCriterion sortingCriterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchProcessDefinitionsDescriptor processDefinitionsDescriptor = tenantAccessor.getSearchEntitiesDescriptor().getProcessDefinitionsDescriptor();
        final GetProcessDefinitionDeployInfosWithActorOnlyForGroups transactionContentWithResult = new GetProcessDefinitionDeployInfosWithActorOnlyForGroups(
                processDefinitionService, processDefinitionsDescriptor, startIndex, maxResults, sortingCriterion, groupIds);
        try {
            transactionExecutor.execute(transactionContentWithResult);
            return transactionContentWithResult.getResult();
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public List<ProcessDeploymentInfo> getProcessDeploymentInfosWithActorOnlyForRole(final long roleId, final int startIndex, final int maxResults,
            final ProcessDeploymentInfoCriterion sortingCriterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchProcessDefinitionsDescriptor processDefinitionsDescriptor = tenantAccessor.getSearchEntitiesDescriptor().getProcessDefinitionsDescriptor();
        final GetProcessDefinitionDeployInfosWithActorOnlyForRole transactionContentWithResult = new GetProcessDefinitionDeployInfosWithActorOnlyForRole(
                processDefinitionService, processDefinitionsDescriptor, startIndex, maxResults, sortingCriterion, roleId);
        try {

            transactionExecutor.execute(transactionContentWithResult);
            return transactionContentWithResult.getResult();
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public List<ProcessDeploymentInfo> getProcessDeploymentInfosWithActorOnlyForRoles(final List<Long> roleIds, final int startIndex, final int maxResults,
            final ProcessDeploymentInfoCriterion sortingCriterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchProcessDefinitionsDescriptor processDefinitionsDescriptor = tenantAccessor.getSearchEntitiesDescriptor().getProcessDefinitionsDescriptor();
        final GetProcessDefinitionDeployInfosWithActorOnlyForRoles transactionContentWithResult = new GetProcessDefinitionDeployInfosWithActorOnlyForRoles(
                processDefinitionService, processDefinitionsDescriptor, startIndex, maxResults, sortingCriterion, roleIds);
        try {
            transactionExecutor.execute(transactionContentWithResult);
            return transactionContentWithResult.getResult();
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public List<ProcessDeploymentInfo> getProcessDeploymentInfosWithActorOnlyForUser(final long userId, final int startIndex, final int maxResults,
            final ProcessDeploymentInfoCriterion sortingCriterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SearchProcessDefinitionsDescriptor processDefinitionsDescriptor = tenantAccessor.getSearchEntitiesDescriptor().getProcessDefinitionsDescriptor();
        final GetProcessDefinitionDeployInfosWithActorOnlyForUser transactionContentWithResult = new GetProcessDefinitionDeployInfosWithActorOnlyForUser(
                processDefinitionService, processDefinitionsDescriptor, startIndex, maxResults, sortingCriterion, userId);
        try {
            transactionExecutor.execute(transactionContentWithResult);
            return transactionContentWithResult.getResult();
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public List<ProcessDeploymentInfo> getProcessDeploymentInfosWithActorOnlyForUsers(final List<Long> userIds, final int startIndex, final int maxResults,
            final ProcessDeploymentInfoCriterion sortingCriterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SearchProcessDefinitionsDescriptor processDefinitionsDescriptor = tenantAccessor.getSearchEntitiesDescriptor().getProcessDefinitionsDescriptor();
        final GetProcessDefinitionDeployInfosWithActorOnlyForUsers transactionContentWithResult = new GetProcessDefinitionDeployInfosWithActorOnlyForUsers(
                processDefinitionService, processDefinitionsDescriptor, startIndex, maxResults, sortingCriterion, userIds);
        try {
            transactionExecutor.execute(transactionContentWithResult);
            return transactionContentWithResult.getResult();
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public SearchResult<ConnectorInstance> searchConnectorInstances(final SearchOptions searchOptions) throws RetrieveException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ConnectorInstanceService connectorInstanceService = tenantAccessor.getConnectorInstanceService();
        final SearchConnectorInstances searchConnector = new SearchConnectorInstances(connectorInstanceService,
                searchEntitiesDescriptor.getConnectorInstanceDescriptor(), searchOptions);
        try {
            transactionExecutor.execute(searchConnector);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
        return searchConnector.getResult();
    }

    @Override
    public SearchResult<ArchivedConnectorInstance> searchArchivedConnectorInstances(final SearchOptions searchOptions) throws RetrieveException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ConnectorInstanceService connectorInstanceService = tenantAccessor.getConnectorInstanceService();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final SearchArchivedConnectorInstance searchArchivedConnectorInstance = new SearchArchivedConnectorInstance(connectorInstanceService,
                searchEntitiesDescriptor.getArchivedConnectorInstanceDescriptor(), searchOptions, persistenceService);
        try {
            transactionExecutor.execute(searchArchivedConnectorInstance);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
        return searchArchivedConnectorInstance.getResult();
    }

    @Override
    public List<HumanTaskInstance> getHumanTaskInstances(final long processInstanceId, final String taskName, final int startIndex, final int maxResults) {
        try {
            return getHumanTaskInstances(processInstanceId, taskName, startIndex, maxResults, HumanTaskInstanceSearchDescriptor.PROCESS_INSTANCE_ID, Order.ASC);
        } catch (final NotFoundException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public HumanTaskInstance getLastStateHumanTaskInstance(final long processInstanceId, final String taskName) throws NotFoundException {
        return getHumanTaskInstances(processInstanceId, taskName, 0, 1, HumanTaskInstanceSearchDescriptor.REACHED_STATE_DATE, Order.DESC).get(0);
    }

    private List<HumanTaskInstance> getHumanTaskInstances(final long processInstanceId, final String taskName, final int startIndex, final int maxResults,
            final String field, final Order order) throws NotFoundException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(startIndex, maxResults);
        builder.filter(HumanTaskInstanceSearchDescriptor.PROCESS_INSTANCE_ID, processInstanceId).filter(HumanTaskInstanceSearchDescriptor.NAME, taskName);
        builder.sort(field, order);
        try {
            final SearchResult<HumanTaskInstance> searchHumanTasks = searchHumanTaskInstances(builder.done());
            if (searchHumanTasks.getCount() == 0) {
                throw new NotFoundException("Task '" + taskName + "' not found");
            }
            return searchHumanTasks.getResult();
        } catch (final SearchException se) {
            throw new RetrieveException(se);
        }
    }

    @Override
    public SearchResult<User> searchUsersWhoCanStartProcessDefinition(final long processDefinitionId, final SearchOptions searchOptions) throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SearchUserDescriptor searchDescriptor = searchEntitiesDescriptor.getUserDescriptor();
        final SearchUsersWhoCanStartProcessDeploymentInfo transactionSearch = new SearchUsersWhoCanStartProcessDeploymentInfo(processDefinitionService,
                searchDescriptor, processDefinitionId, searchOptions);
        try {
            transactionExecutor.execute(transactionSearch);
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
        return transactionSearch.getResult();
    }

    @Override
    public Map<String, Serializable> evaluateExpressionsAtProcessInstanciation(final long processInstanceId,
            final Map<Expression, Map<String, Serializable>> expressions) throws ExpressionEvaluationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();

        try {
            final boolean txOpened = transactionExecutor.openTransaction();

            try {
                final ProcessInstance processInstance = getStartedProcessInstance(processInstanceId);
                return evaluateExpressionsInstanceLevelAndArchived(expressions, processInstanceId, CONTAINER_TYPE_PROCESS_INSTANCE,
                        processInstance.getProcessDefinitionId(), processInstance.getStartDate().getTime());
            } catch (final SProcessInstanceNotFoundException spinfe) {
                final ArchivedProcessInstance archiveProcessInstance = getStartedArchivedProcessInstance(processInstanceId);
                final Map<String, Serializable> evaluateExpressionInArchiveProcessInstance = evaluateExpressionsInstanceLevelAndArchived(expressions,
                        processInstanceId, CONTAINER_TYPE_PROCESS_INSTANCE, archiveProcessInstance.getProcessDefinitionId(), archiveProcessInstance
                                .getStartDate().getTime());
                transactionExecutor.setTransactionRollback();
                return evaluateExpressionInArchiveProcessInstance;
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final SBonitaException e) {
            throw new ExpressionEvaluationException(e);
        }
    }

    @Override
    public Map<String, Serializable> evaluateExpressionOnCompletedProcessInstance(final long processInstanceId,
            final Map<Expression, Map<String, Serializable>> expressions) throws ExpressionEvaluationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();

        try {
            final boolean txOpened = transactionExecutor.openTransaction();

            try {
                final ArchivedProcessInstance lastArchivedProcessInstance = getLastArchivedProcessInstance(processInstanceId);
                return evaluateExpressionsInstanceLevelAndArchived(expressions, processInstanceId, CONTAINER_TYPE_PROCESS_INSTANCE,
                        lastArchivedProcessInstance.getProcessDefinitionId(), lastArchivedProcessInstance.getArchiveDate().getTime());
            } catch (final SProcessInstanceNotFoundException e) {
                transactionExecutor.setTransactionRollback();
                throw new ExpressionEvaluationException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final SBonitaException e) {
            throw new ExpressionEvaluationException(e);
        }
    }

    @Override
    public Map<String, Serializable> evaluateExpressionsOnProcessInstance(final long processInstanceId,
            final Map<Expression, Map<String, Serializable>> expressions) throws ExpressionEvaluationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();

        try {
            final boolean txOpened = transactionExecutor.openTransaction();

            try {
                return evaluateExpressionsInstanceLevel(expressions, processInstanceId, CONTAINER_TYPE_PROCESS_INSTANCE, getProcessInstance(processInstanceId)
                        .getProcessDefinitionId());
            } catch (final BonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new ExpressionEvaluationException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final SBonitaException e) {
            throw new ExpressionEvaluationException(e);
        }
    }

    @Override
    public Map<String, Serializable> evaluateExpressionsOnProcessDefinition(final long processDefinitionId,
            final Map<Expression, Map<String, Serializable>> expressions) throws ExpressionEvaluationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();

        try {
            final boolean txOpened = transactionExecutor.openTransaction();

            try {
                return evaluateExpressionsDefinitionLevel(expressions, processDefinitionId);
            } catch (final SProcessInstanceNotFoundException e) {
                transactionExecutor.setTransactionRollback();
                throw new ExpressionEvaluationException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final SBonitaException e) {
            throw new ExpressionEvaluationException(e);
        }
    }

    @Override
    public Map<String, Serializable> evaluateExpressionsOnActivityInstance(final long activityInstanceId,
            final Map<Expression, Map<String, Serializable>> expressions) throws ExpressionEvaluationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();

        try {
            final boolean txOpened = transactionExecutor.openTransaction();

            try {
                final ActivityInstance activityInstance = getActivityInstance(activityInstanceId);
                final ProcessInstance processInstance = getProcessInstance(activityInstance.getRootContainerId());

                return evaluateExpressionsInstanceLevel(expressions, activityInstanceId, CONTAINER_TYPE_ACTIVITY_INSTANCE,
                        processInstance.getProcessDefinitionId());
            } catch (final BonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new ExpressionEvaluationException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final SBonitaException e) {
            throw new ExpressionEvaluationException(e);
        }
    }

    @Override
    public Map<String, Serializable> evaluateExpressionsOnCompletedActivityInstance(final long activityInstanceId,
            final Map<Expression, Map<String, Serializable>> expressions) throws ExpressionEvaluationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();

        try {
            final boolean txOpened = transactionExecutor.openTransaction();

            try {
                final ArchivedActivityInstance activityInstance = getArchivedActivityInstance(activityInstanceId);
                // same archive time to process even if there're many activities in the process
                final ArchivedProcessInstance lastArchivedProcessInstance = getLastArchivedProcessInstance(activityInstance.getRootContainerId());

                return evaluateExpressionsInstanceLevelAndArchived(expressions, activityInstanceId, CONTAINER_TYPE_ACTIVITY_INSTANCE,
                        lastArchivedProcessInstance.getProcessDefinitionId(), activityInstance.getArchiveDate().getTime());
            } catch (final ActivityInstanceNotFoundException e) {
                transactionExecutor.setTransactionRollback();
                throw new ExpressionEvaluationException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final SBonitaException e) {
            throw new ExpressionEvaluationException(e);
        }
    }

    private Map<String, Serializable> evaluateExpressionsDefinitionLevel(final Map<Expression, Map<String, Serializable>> expressionsAndTheirPartialContext,
            final long processDefinitionId) throws SBonitaException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ExpressionResolverService expressionResolverService = tenantAccessor.getExpressionResolverService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SExpressionBuilders expBuilder = tenantAccessor.getSExpressionBuilders();
        final EvaluateExpressionsDefinitionLevel evaluations = new EvaluateExpressionsDefinitionLevel(expressionsAndTheirPartialContext, processDefinitionId,
                expressionResolverService, expBuilder, processDefinitionService);
        evaluations.execute();
        return evaluations.getResult();
    }

    private Map<String, Serializable> evaluateExpressionsInstanceLevel(final Map<Expression, Map<String, Serializable>> expressions, final long containerId,
            final String containerType, final long processDefinitionId) throws SBonitaException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ExpressionResolverService expressionService = tenantAccessor.getExpressionResolverService();
        final SExpressionBuilders expBuilder = tenantAccessor.getSExpressionBuilders();
        final EvaluateExpressionsInstanceLevel evaluations = new EvaluateExpressionsInstanceLevel(expressions, containerId, containerType, processDefinitionId,
                expressionService, expBuilder);
        evaluations.execute();
        return evaluations.getResult();
    }

    private Map<String, Serializable> evaluateExpressionsInstanceLevelAndArchived(final Map<Expression, Map<String, Serializable>> expressions,
            final long containerId, final String containerType, final long proceDefinitionId, final long time) throws SBonitaException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ExpressionResolverService expressionService = tenantAccessor.getExpressionResolverService();
        final SExpressionBuilders expBuilder = tenantAccessor.getSExpressionBuilders();
        final EvaluateExpressionsInstanceLevelAndArchived evaluations = new EvaluateExpressionsInstanceLevelAndArchived(expressions, containerId,
                containerType, proceDefinitionId, time, expressionService, expBuilder);
        evaluations.execute();
        return evaluations.getResult();
    }

    private ProcessInstance getStartedProcessInstance(final long processInstanceId) throws SBonitaException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final GetStartedProcessInstance getStartedProcessInstance = new GetStartedProcessInstance(processInstanceService, processDefinitionService,
                searchEntitiesDescriptor, processInstanceId);
        getStartedProcessInstance.execute();

        return getStartedProcessInstance.getResult();
    }

    private ArchivedProcessInstance getStartedArchivedProcessInstance(final long processInstanceId) throws SBonitaException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ReadPersistenceService readPersistenceService = getDefinitiveArchiveReadPersistenceService(tenantAccessor);
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();

        final GetStartedArchivedProcessInstance searchArchivedProcessInstances = new GetStartedArchivedProcessInstance(processInstanceService,
                readPersistenceService, searchEntitiesDescriptor, processInstanceId);
        searchArchivedProcessInstances.execute();
        return searchArchivedProcessInstances.getResult();
    }

    private ArchivedProcessInstance getLastArchivedProcessInstance(final long processInstanceId) throws SBonitaException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ReadPersistenceService readPersistenceService = getDefinitiveArchiveReadPersistenceService(tenantAccessor);
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final GetLastArchivedProcessInstance searchArchivedProcessInstances = new GetLastArchivedProcessInstance(processInstanceService, processInstanceId,
                readPersistenceService, searchEntitiesDescriptor);

        searchArchivedProcessInstances.execute();
        return searchArchivedProcessInstances.getResult();
    }

    private ReadPersistenceService getDefinitiveArchiveReadPersistenceService(final TenantServiceAccessor tenantAccessor) {
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        return archiveService.getDefinitiveArchiveReadPersistenceService();
    }
}
