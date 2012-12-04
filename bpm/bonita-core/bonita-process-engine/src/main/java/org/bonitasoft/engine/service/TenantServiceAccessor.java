/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package org.bonitasoft.engine.service;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.model.SActorBuilders;
import org.bonitasoft.engine.actor.privilege.api.ActorPrivilegeService;
import org.bonitasoft.engine.actor.privilege.model.builder.ActorPrivilegeBuilders;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.businesslogger.model.builder.SBusinessLogModelBuilder;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.command.CommandService;
import org.bonitasoft.engine.command.model.SCommandBuilderAccessor;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.connector.ConnectorExecutor;
import org.bonitasoft.engine.core.category.CategoryService;
import org.bonitasoft.engine.core.category.model.builder.SCategoryBuilderAccessor;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.filter.UserFilterService;
import org.bonitasoft.engine.core.login.LoginService;
import org.bonitasoft.engine.core.migration.MigrationPlanService;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.model.builder.SOperationBuilders;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.comment.model.builder.SCommentBuilders;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.builder.BPMDefinitionBuilders;
import org.bonitasoft.engine.core.process.document.api.ProcessDocumentService;
import org.bonitasoft.engine.core.process.document.mapping.DocumentMappingService;
import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingBuilderAccessor;
import org.bonitasoft.engine.core.process.document.model.builder.SProcessDocumentBuilder;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.BreakpointService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.TransitionService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.data.DataService;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilders;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.model.builder.SDataSourceModelBuilder;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.builder.DependencyBuilderAccessor;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.FlowNodeExecutor;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.execution.state.ProcessInstanceStateManager;
import org.bonitasoft.engine.expression.ExpressionService;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilders;
import org.bonitasoft.engine.external.identity.mapping.ExternalIdentityMappingService;
import org.bonitasoft.engine.external.identity.mapping.model.SExternalIdentityMappingBuilders;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.builder.IdentityModelBuilder;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.monitoring.TenantMonitoringService;
import org.bonitasoft.engine.parameter.ParameterService;
import org.bonitasoft.engine.platform.model.builder.STenantBuilder;
import org.bonitasoft.engine.privilege.api.PrivilegeService;
import org.bonitasoft.engine.privilege.model.buidler.PrivilegeBuilders;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.model.SProfileBuilderAccessor;
import org.bonitasoft.engine.search.SearchEntitiesDescriptor;
import org.bonitasoft.engine.services.BusinessLoggerService;
import org.bonitasoft.engine.supervisor.mapping.SupervisorMappingService;
import org.bonitasoft.engine.supervisor.mapping.model.SSupervisorBuilders;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.xml.Parser;
import org.bonitasoft.engine.xml.ParserFactory;
import org.bonitasoft.engine.xml.XMLWriter;

/**
 * @author Matthieu Chaffotte
 */
public interface TenantServiceAccessor extends ServiceAccessor {

    long getTenantId();

    IdentityModelBuilder getIdentityModelBuilder();

    IdentityService getIdentityService();

    LoginService getLoginService();

    BusinessLoggerService getBusinessLoggerService();

    SBusinessLogModelBuilder getSBusinessLogModelBuilder();

    TechnicalLoggerService getTechnicalLoggerService();

    STenantBuilder getSTenantBuilder();

    TransactionService getTransactionService();

    ProcessDefinitionService getProcessDefinitionService();

    ProcessInstanceService getProcessInstanceService();

    TransitionService getTransitionInstanceService();

    ActivityInstanceService getActivityInstanceService();

    BPMDefinitionBuilders getBPMDefinitionBuilders();

    BPMInstanceBuilders getBPMInstanceBuilders();

    FlowNodeExecutor getFlowNodeExecutor();

    ProcessExecutor getProcessExecutor();

    FlowNodeStateManager getFlowNodeStateManager();

    TransactionExecutor getTransactionExecutor();

    TenantMonitoringService getTenantMonitoringService();

    ParameterService getParameterService();

    ActorMappingService getActorMappingService();

    SActorBuilders getSActorBuilders();

    ArchiveService getArchiveService();

    SCategoryBuilderAccessor getCategoryModelBuilderAccessor();

    CategoryService getCategoryService();

    ProcessInstanceStateManager getProcessInstanceStateManager();

    SExpressionBuilders getSExpressionBuilders();

    ExpressionService getExpressionService();

    CommandService getCommandService();

    SCommandBuilderAccessor getSCommandBuilderAccessor();

    ClassLoaderService getClassLoaderService();

    DependencyService getDependencyService();

    DependencyBuilderAccessor getDependencyBuilderAccessor();

    EventInstanceService getEventInstanceService();

    ConnectorService getConnectorService();

    DocumentMappingService getDocumentMappingService();

    SDocumentMappingBuilderAccessor getDocumentMappingBuilderAccessor();

    SProcessDocumentBuilder getProcessDocumentBuilder();

    ProcessDocumentService getProcessDocumentService();

    ProfileService getProfileService();

    SProfileBuilderAccessor getSProfileBuilderAccessor();

    DataInstanceService getDataInstanceService();

    SDataDefinitionBuilders getSDataDefinitionBuilders();

    SDataSourceModelBuilder getSDataSourceModelBuilder();

    DataService getDataService();

    ParserFactory getParserFactgory();

    Parser getActorMappingParser();

    XMLWriter getXMLWriter();

    ExpressionResolverService getExpressionResolverService();

    OperationService getOperationService();

    SupervisorMappingService getSupervisorService();

    ExternalIdentityMappingService getExternalIdentityMappingService();

    SExternalIdentityMappingBuilders getExternalIdentityMappingBuilders();

    SSupervisorBuilders getSSupervisorBuilders();

    SOperationBuilders getSOperationBuilders();

    ActorPrivilegeService getActorPrivilegeService();

    ActorPrivilegeBuilders getActorPrivilegeBuilders();

    UserFilterService getUserFilterService();

    PrivilegeService getPrivilegeService();

    PrivilegeBuilders getPrivilegeBuilders();

    SearchEntitiesDescriptor getSearchEntitiesDescriptor();

    SCommentService getCommentService();

    SCommentBuilders getSCommentBuilders();

    ContainerRegistry getContainerRegistry();

    BreakpointService getBreakpointService();

    LockService getLockService();

    MigrationPlanService getMigrationPlanService();

    Parser getProfileParser();

    EventsHandler getEventsHandler();

    EventService getEventService();

    ConnectorExecutor getConnectorExecutor();
}
