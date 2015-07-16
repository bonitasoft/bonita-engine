
/*******************************************************************************
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
 ******************************************************************************/

package org.bonitasoft.engine.service;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.api.impl.TenantConfiguration;
import org.bonitasoft.engine.api.impl.resolver.DependencyResolver;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.authentication.GenericAuthenticationService;
import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.data.BusinessDataModelRepository;
import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.business.data.BusinessDataService;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.command.CommandService;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.connector.ConnectorExecutor;
import org.bonitasoft.engine.core.category.CategoryService;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.contract.data.ContractDataService;
import org.bonitasoft.engine.core.data.instance.TransientDataService;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.filter.UserFilterService;
import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.core.login.LoginService;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.GatewayInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.api.ParentContainerResolver;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.FlowNodeExecutor;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.execution.TransactionalProcessInstanceInterruptor;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.expression.ExpressionService;
import org.bonitasoft.engine.external.identity.mapping.ExternalIdentityMappingService;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.incident.IncidentService;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.page.PageMappingService;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.parameter.ParameterService;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.supervisor.mapping.SupervisorMappingService;
import org.bonitasoft.engine.synchro.SynchroService;
import org.bonitasoft.engine.theme.ThemeService;
import org.bonitasoft.engine.tracking.TimeTracker;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.bonitasoft.engine.work.WorkService;
import org.bonitasoft.engine.xml.Parser;
import org.bonitasoft.engine.xml.ParserFactory;
import org.bonitasoft.engine.xml.XMLWriter;

/**
 * Accessor for tenant level engine services.
 * <p>
 * All server side services of a tenant can be accessed using this class. Using server side services instead of an API might cause unexpected behaviors and
 * damage your data.
 *
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 * @author Hongwen Zang
 * @author Celine Souchet
 */
public interface TenantServiceAccessor extends ServiceAccessor {

    long getTenantId();

    ParentContainerResolver getParentContainerResolver();

    SessionService getSessionService();

    IdentityService getIdentityService();

    LoginService getLoginService();

    QueriableLoggerService getQueriableLoggerService();

    TechnicalLoggerService getTechnicalLoggerService();

    UserTransactionService getUserTransactionService();

    ProcessDefinitionService getProcessDefinitionService();

    ProcessInstanceService getProcessInstanceService();

    ActivityInstanceService getActivityInstanceService();

    BPMInstancesCreator getBPMInstancesCreator();

    FlowNodeExecutor getFlowNodeExecutor();

    ProcessExecutor getProcessExecutor();

    FlowNodeStateManager getFlowNodeStateManager();

    TransactionExecutor getTransactionExecutor();

    ActorMappingService getActorMappingService();

    ArchiveService getArchiveService();

    CategoryService getCategoryService();

    ExpressionService getExpressionService();

    CommandService getCommandService();

    ClassLoaderService getClassLoaderService();

    DependencyService getDependencyService();

    EventInstanceService getEventInstanceService();

    ConnectorService getConnectorService();

    ConnectorInstanceService getConnectorInstanceService();

    DocumentService getDocumentService();

    ProfileService getProfileService();

    DataInstanceService getDataInstanceService();

    TransientDataService getTransientDataService();

    ParserFactory getParserFactgory();

    XMLWriter getXMLWriter();

    ExpressionResolverService getExpressionResolverService();

    OperationService getOperationService();

    SupervisorMappingService getSupervisorService();

    ExternalIdentityMappingService getExternalIdentityMappingService();

    UserFilterService getUserFilterService();

    SearchEntitiesDescriptor getSearchEntitiesDescriptor();

    SCommentService getCommentService();

    ContainerRegistry getContainerRegistry();

    LockService getLockService();

    Parser getProfileParser();

    EventsHandler getEventsHandler();

    EventService getEventService();

    ConnectorExecutor getConnectorExecutor();

    CacheService getCacheService();

    DependencyResolver getDependencyResolver();

    WorkService getWorkService();

    TransactionalProcessInstanceInterruptor getTransactionalProcessInstanceInterruptor();

    SessionAccessor getSessionAccessor();

    SynchroService getSynchroService();

    IncidentService getIncidentService();

    SchedulerService getSchedulerService();

    JobService getJobService();

    ThemeService getThemeService();

    TenantConfiguration getTenantConfiguration();

    <T> T lookup(String serviceName);

    GatewayInstanceService getGatewayInstanceService();

    void destroy();

    TimeTracker getTimeTracker();

    PermissionService getPermissionService();

    ContractDataService getContractDataService();

    ParameterService getParameterService();

    PageService getPageService();

    ApplicationService getApplicationService();

    FormMappingService getFormMappingService();

    BusinessDataRepository getBusinessDataRepository();

    BusinessDataService getBusinessDataService();

    BusinessDataModelRepository getBusinessDataModelRepository();

    RefBusinessDataService getRefBusinessDataService();

    PageMappingService getPageMappingService();

    GenericAuthenticationService getAuthenticationService();

    ReadPersistenceService getReadPersistenceService();

    Recorder getRecorder();
}
