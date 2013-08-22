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
package org.bonitasoft.engine.service;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.model.SActorBuilders;
import org.bonitasoft.engine.api.impl.resolver.DependencyResolver;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.command.CommandService;
import org.bonitasoft.engine.command.DefaultCommandProvider;
import org.bonitasoft.engine.command.model.SCommandBuilderAccessor;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.connector.ConnectorExecutor;
import org.bonitasoft.engine.core.category.CategoryService;
import org.bonitasoft.engine.core.category.model.builder.SCategoryBuilderAccessor;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.filter.UserFilterService;
import org.bonitasoft.engine.core.login.LoginService;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.model.builder.SOperationBuilders;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.comment.model.archive.builder.SACommentBuilder;
import org.bonitasoft.engine.core.process.comment.model.builder.SCommentBuilders;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.builder.BPMDefinitionBuilders;
import org.bonitasoft.engine.core.process.document.api.ProcessDocumentService;
import org.bonitasoft.engine.core.process.document.mapping.DocumentMappingService;
import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingBuilderAccessor;
import org.bonitasoft.engine.core.process.document.model.builder.SProcessDocumentBuilder;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.TokenService;
import org.bonitasoft.engine.core.process.instance.api.TransitionService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.data.DataService;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilders;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilders;
import org.bonitasoft.engine.data.model.builder.SDataSourceModelBuilder;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.builder.DependencyBuilderAccessor;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.FlowNodeExecutor;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.execution.TransactionalProcessInstanceInterruptor;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.expression.ExpressionService;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilders;
import org.bonitasoft.engine.external.identity.mapping.ExternalIdentityMappingService;
import org.bonitasoft.engine.external.identity.mapping.model.SExternalIdentityMappingBuilders;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.builder.IdentityModelBuilder;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.model.builder.STenantBuilder;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.builder.SProfileBuilderAccessor;
import org.bonitasoft.engine.queriablelogger.model.builder.SQueriableLogModelBuilder;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.supervisor.mapping.SupervisorMappingService;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisorBuilders;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.work.WorkService;
import org.bonitasoft.engine.xml.Parser;
import org.bonitasoft.engine.xml.ParserFactory;
import org.bonitasoft.engine.xml.XMLWriter;

/**
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 * @author Hongwen Zang
 * @author Celine Souchet
 */
public interface TenantServiceAccessor extends ServiceAccessor {

    long getTenantId();

    SessionService getSessionService();

    ReadSessionAccessor getReadSessionAccessor();

    IdentityModelBuilder getIdentityModelBuilder();

    IdentityService getIdentityService();

    LoginService getLoginService();

    QueriableLoggerService getQueriableLoggerService();

    SQueriableLogModelBuilder getSQueriableLogModelBuilder();

    TechnicalLoggerService getTechnicalLoggerService();

    STenantBuilder getSTenantBuilder();

    TransactionService getTransactionService();

    ProcessDefinitionService getProcessDefinitionService();

    ProcessInstanceService getProcessInstanceService();

    TokenService getTokenService();

    TransitionService getTransitionInstanceService();

    ActivityInstanceService getActivityInstanceService();

    BPMDefinitionBuilders getBPMDefinitionBuilders();

    BPMInstanceBuilders getBPMInstanceBuilders();

    BPMInstancesCreator getBPMInstancesCreator();

    FlowNodeExecutor getFlowNodeExecutor();

    ProcessExecutor getProcessExecutor();

    FlowNodeStateManager getFlowNodeStateManager();

    TransactionExecutor getTransactionExecutor();

    ActorMappingService getActorMappingService();

    SActorBuilders getSActorBuilders();

    ArchiveService getArchiveService();

    SCategoryBuilderAccessor getCategoryModelBuilderAccessor();

    CategoryService getCategoryService();

    SExpressionBuilders getSExpressionBuilders();

    ExpressionService getExpressionService();

    CommandService getCommandService();

    SCommandBuilderAccessor getSCommandBuilderAccessor();

    ClassLoaderService getClassLoaderService();

    DependencyService getDependencyService();

    DependencyBuilderAccessor getDependencyBuilderAccessor();

    EventInstanceService getEventInstanceService();

    ConnectorService getConnectorService();

    ConnectorInstanceService getConnectorInstanceService();

    DocumentMappingService getDocumentMappingService();

    SDocumentMappingBuilderAccessor getDocumentMappingBuilderAccessor();

    SProcessDocumentBuilder getProcessDocumentBuilder();

    ProcessDocumentService getProcessDocumentService();

    ProfileService getProfileService();

    SProfileBuilderAccessor getSProfileBuilderAccessor();

    DataInstanceService getDataInstanceService();

    SDataDefinitionBuilders getSDataDefinitionBuilders();

    SDataSourceModelBuilder getSDataSourceModelBuilder();

    SDataInstanceBuilders getSDataInstanceBuilders();

    DataService getDataService();

    ParserFactory getParserFactgory();

    Parser getActorMappingParser();

    XMLWriter getXMLWriter();

    ExpressionResolverService getExpressionResolverService();

    OperationService getOperationService();

    SupervisorMappingService getSupervisorService();

    ExternalIdentityMappingService getExternalIdentityMappingService();

    SExternalIdentityMappingBuilders getExternalIdentityMappingBuilders();

    SProcessSupervisorBuilders getSSupervisorBuilders();

    SOperationBuilders getSOperationBuilders();

    UserFilterService getUserFilterService();

    SearchEntitiesDescriptor getSearchEntitiesDescriptor();

    SCommentService getCommentService();

    SCommentBuilders getSCommentBuilders();

    ContainerRegistry getContainerRegistry();

    LockService getLockService();

    Parser getProfileParser();

    EventsHandler getEventsHandler();

    EventService getEventService();

    ConnectorExecutor getConnectorExecutor();

    CacheService getCacheService();

    DependencyResolver getDependencyResolver();

    DefaultCommandProvider getDefaultCommandProvider();

    WorkService getWorkService();

    TransactionalProcessInstanceInterruptor getTransactionalProcessInstanceInterruptor();

    SACommentBuilder getSACommentBuilders();

    SessionAccessor getSessionAccessor();

}
