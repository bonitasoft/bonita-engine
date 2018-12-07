/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

package org.bonitasoft.engine.service.impl;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.api.impl.TenantConfiguration;
import org.bonitasoft.engine.api.impl.resolver.BusinessArchiveArtifactsManager;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.authentication.GenericAuthenticationService;
import org.bonitasoft.engine.authentication.GenericAuthenticationServiceAccessor;
import org.bonitasoft.engine.bar.BusinessArchiveService;
import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.data.BusinessDataModelRepository;
import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.business.data.BusinessDataService;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.command.CommandService;
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
import org.bonitasoft.engine.core.login.TechnicalUser;
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
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.FlowNodeExecutor;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.execution.ProcessInstanceInterruptor;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.execution.work.BPMWorkFactory;
import org.bonitasoft.engine.expression.ExpressionService;
import org.bonitasoft.engine.external.identity.mapping.ExternalIdentityMappingService;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.incident.IncidentService;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.message.MessagesHandlingService;
import org.bonitasoft.engine.page.PageMappingService;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.parameter.ParameterService;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.ProfilesExporter;
import org.bonitasoft.engine.profile.ProfilesImporter;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.resources.ProcessResourcesService;
import org.bonitasoft.engine.resources.TenantResourcesService;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.service.PermissionService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.supervisor.mapping.SupervisorMappingService;
import org.bonitasoft.engine.synchro.SynchroService;
import org.bonitasoft.engine.theme.ThemeService;
import org.bonitasoft.engine.tracking.TimeTracker;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.bonitasoft.engine.work.WorkExecutorService;
import org.bonitasoft.engine.work.WorkService;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class SpringTenantServiceAccessor implements TenantServiceAccessor {

    private final SpringBeanAccessor beanAccessor;
    private Long tenantId;

    public SpringTenantServiceAccessor(final SpringBeanAccessor beanAccessor, Long tenantId) {
        this.beanAccessor = beanAccessor;
        this.tenantId = tenantId;
    }

    @Override
    public ParentContainerResolver getParentContainerResolver() {
        return beanAccessor.getService(ParentContainerResolver.class);
    }

    @Override
    public TimeTracker getTimeTracker() {
        return beanAccessor.getService(TimeTracker.class);
    }

    @Override
    public SessionAccessor getSessionAccessor() {
        return beanAccessor.getService(SessionAccessor.class);
    }

    @Override
    public SessionService getSessionService() {
        return beanAccessor.getService(SessionService.class);
    }

    @Override
    public IdentityService getIdentityService() {
        return beanAccessor.getService(IdentityService.class);
    }

    @Override
    public LoginService getLoginService() {
        return beanAccessor.getService(LoginService.class);
    }

    @Override
    public QueriableLoggerService getQueriableLoggerService() {
        return beanAccessor.getService("queriableLoggerService", QueriableLoggerService.class);
    }

    @Override
    public TechnicalLoggerService getTechnicalLoggerService() {
        return beanAccessor.getService("tenantTechnicalLoggerService", TechnicalLoggerService.class);
    }

    private TransactionService getTransactionService() {
        return beanAccessor.getService(TransactionService.class);
    }

    @Override
    public UserTransactionService getUserTransactionService() {
        return getTransactionService();
    }

    @Override
    public ProcessDefinitionService getProcessDefinitionService() {
        return beanAccessor.getService(ProcessDefinitionService.class);
    }

    @Override
    public ProcessInstanceService getProcessInstanceService() {
        return beanAccessor.getService(ProcessInstanceService.class);
    }

    @Override
    public ActivityInstanceService getActivityInstanceService() {
        return beanAccessor.getService(ActivityInstanceService.class);
    }

    @Override
    public BPMInstancesCreator getBPMInstancesCreator() {
        return beanAccessor.getService(BPMInstancesCreator.class);
    }

    @Override
    public FlowNodeExecutor getFlowNodeExecutor() {
        return beanAccessor.getService(FlowNodeExecutor.class);
    }

    @Override
    public ProcessExecutor getProcessExecutor() {
        return beanAccessor.getService(ProcessExecutor.class);
    }

    @Override
    public FlowNodeStateManager getFlowNodeStateManager() {
        return beanAccessor.getService(FlowNodeStateManager.class);
    }

    @Override
    public ActorMappingService getActorMappingService() {
        return beanAccessor.getService(ActorMappingService.class);
    }

    @Override
    public ArchiveService getArchiveService() {
        return beanAccessor.getService(ArchiveService.class);
    }

    @Override
    public CategoryService getCategoryService() {
        return beanAccessor.getService(CategoryService.class);
    }

    @Override
    public CommandService getCommandService() {
        return beanAccessor.getService(CommandService.class);
    }

    @Override
    public ClassLoaderService getClassLoaderService() {
        return beanAccessor.getService("classLoaderService", ClassLoaderService.class);
    }

    @Override
    public DependencyService getDependencyService() {
        return beanAccessor.getService("dependencyService", DependencyService.class);
    }

    @Override
    public long getTenantId() {
        return tenantId;
    }

    @Override
    public EventInstanceService getEventInstanceService() {
        return beanAccessor.getService(EventInstanceService.class);
    }

    @Override
    public ConnectorService getConnectorService() {
        return beanAccessor.getService("connectorService", ConnectorService.class);
    }

    @Override
    public ConnectorInstanceService getConnectorInstanceService() {
        return beanAccessor.getService(ConnectorInstanceService.class);
    }

    @Override
    public ConnectorExecutor getConnectorExecutor() {
        return beanAccessor.getService(ConnectorExecutor.class);
    }

    @Override
    public ExpressionService getExpressionService() {
        return beanAccessor.getService(ExpressionService.class);
    }

    @Override
    public DocumentService getDocumentService() {
        return beanAccessor.getService(DocumentService.class);
    }

    @Override
    public ProfileService getProfileService() {
        return beanAccessor.getService(ProfileService.class);
    }

    @Override
    public ProfilesImporter getProfilesImporter() {
        return beanAccessor.getService(ProfilesImporter.class);
    }

    @Override
    public ProfilesExporter getProfilesExporter() {
        return beanAccessor.getService(ProfilesExporter.class);
    }

    @Override
    public DataInstanceService getDataInstanceService() {
        return beanAccessor.getService(DataInstanceService.class);
    }

    @Override
    public OperationService getOperationService() {
        return beanAccessor.getService(OperationService.class);
    }

    @Override
    public ExpressionResolverService getExpressionResolverService() {
        return beanAccessor.getService(ExpressionResolverService.class);
    }

    @Override
    public SupervisorMappingService getSupervisorService() {
        return beanAccessor.getService(SupervisorMappingService.class);
    }

    @Override
    public UserFilterService getUserFilterService() {
        return beanAccessor.getService("userFilterService", UserFilterService.class);
    }

    @Override
    public SearchEntitiesDescriptor getSearchEntitiesDescriptor() {
        return beanAccessor.getService(SearchEntitiesDescriptor.class);
    }

    @Override
    public SCommentService getCommentService() {
        return beanAccessor.getService(SCommentService.class);
    }

    @Override
    public ContainerRegistry getContainerRegistry() {
        return beanAccessor.getService(ContainerRegistry.class);
    }

    @Override
    public ExternalIdentityMappingService getExternalIdentityMappingService() {
        return beanAccessor.getService(ExternalIdentityMappingService.class);
    }

    @Override
    public LockService getLockService() {
        return beanAccessor.getService(LockService.class);
    }

    @Override
    public EventsHandler getEventsHandler() {
        return beanAccessor.getService(EventsHandler.class);
    }

    @Override
    public EventService getEventService() {
        return beanAccessor.getService(EventService.class);
    }

    public SpringBeanAccessor getBeanAccessor() {
        return beanAccessor;
    }

    @Override
    public CacheService getCacheService() {
        return beanAccessor.getService("cacheService", CacheService.class);
    }

    @Override
    public BusinessArchiveArtifactsManager getBusinessArchiveArtifactsManager() {
        return beanAccessor.getService(BusinessArchiveArtifactsManager.class);
    }

    @Override
    public WorkService getWorkService() {
        return beanAccessor.getService(WorkService.class);
    }

    @Override
    public WorkExecutorService getWorkExecutorService() {
        return beanAccessor.getService(WorkExecutorService.class);
    }

    @Override
    public SynchroService getSynchroService() {
        return beanAccessor.getService(SynchroService.class);
    }

    @Override
    public IncidentService getIncidentService() {
        return beanAccessor.getService(IncidentService.class);
    }

    @Override
    public SchedulerService getSchedulerService() {
        return beanAccessor.getService(SchedulerService.class);
    }

    @Override
    public JobService getJobService() {
        return beanAccessor.getService(JobService.class);
    }

    @Override
    public ThemeService getThemeService() {
        return beanAccessor.getService(ThemeService.class);
    }

    @Override
    public TransientDataService getTransientDataService() {
        return beanAccessor.getService(TransientDataService.class);
    }

    @Override
    public GatewayInstanceService getGatewayInstanceService() {
        return beanAccessor.getService(GatewayInstanceService.class);
    }

    @Override
    public void destroy() {
        beanAccessor.destroy();
    }

    @Override
    public TenantConfiguration getTenantConfiguration() {
        return beanAccessor.getService(TenantConfiguration.class);
    }

    @Override
    public <T> T lookup(final String serviceName) throws NotFoundException {
        try {

            return beanAccessor.getService(serviceName);
        } catch (NoSuchBeanDefinitionException e) {
            throw new NotFoundException(e);
        }
    }

    @Override
    public PermissionService getPermissionService() {
        return beanAccessor.getService(PermissionService.class);
    }

    @Override
    public ContractDataService getContractDataService() {
        return beanAccessor.getService(ContractDataService.class);
    }

    @Override
    public ParameterService getParameterService() {
        return beanAccessor.getService(ParameterService.class);
    }

    /**
     * might not be an available service
     */
    @Override
    public PageService getPageService() {
        return beanAccessor.getService(PageService.class);
    }

    @Override
    public ApplicationService getApplicationService() {
        return beanAccessor.getService(ApplicationService.class);
    }

    @Override
    public BusinessDataRepository getBusinessDataRepository() {
        return beanAccessor.getService(BusinessDataRepository.class);
    }

    @Override
    public BusinessDataModelRepository getBusinessDataModelRepository() {
        return beanAccessor.getService(BusinessDataModelRepository.class);
    }

    @Override
    public RefBusinessDataService getRefBusinessDataService() {
        return beanAccessor.getService(RefBusinessDataService.class);
    }

    @Override
    public GenericAuthenticationService getAuthenticationService() {
        return beanAccessor.getService(GenericAuthenticationServiceAccessor.class).getAuthenticationService();
    }

    @Override
    public ReadPersistenceService getReadPersistenceService() {
        return beanAccessor.getService("persistenceService");
    }

    @Override
    public Recorder getRecorder() {
        return beanAccessor.getService(Recorder.class);
    }

    @Override
    public BusinessArchiveService getBusinessArchiveService() {
        return beanAccessor.getService(BusinessArchiveService.class);
    }

    @Override
    public BusinessDataService getBusinessDataService() {
        return beanAccessor.getService(BusinessDataService.class);
    }

    @Override
    public FormMappingService getFormMappingService() {
        return beanAccessor.getService(FormMappingService.class);
    }

    @Override
    public PageMappingService getPageMappingService() {
        return beanAccessor.getService(PageMappingService.class);
    }

    public ProcessResourcesService getProcessResourcesService() {
        return beanAccessor.getService(ProcessResourcesService.class);
    }

    public TenantResourcesService getTenantResourcesService() {
        return beanAccessor.getService(TenantResourcesService.class);
    }

    public MessagesHandlingService getMessagesHandlingService() {
        return beanAccessor.getService(MessagesHandlingService.class);
    }

    @Override
    public ProcessInstanceInterruptor getProcessInstanceInterruptor() {
        return beanAccessor.getService(ProcessInstanceInterruptor.class);
    }

    public BPMWorkFactory getBPMWorkFactory() {
        return beanAccessor.getService(BPMWorkFactory.class);
    }

    public TechnicalUser getTechnicalUser() {
        return beanAccessor.getService(TechnicalUser.class);
    }

}
