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
package org.bonitasoft.engine.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.xml.ActorBinding;
import org.bonitasoft.engine.actor.xml.ActorMappingBinding;
import org.bonitasoft.engine.actor.xml.ActorMembershipBinding;
import org.bonitasoft.engine.actor.xml.GroupPathsBinding;
import org.bonitasoft.engine.actor.xml.RoleNamesBinding;
import org.bonitasoft.engine.actor.xml.UserNamesBinding;
import org.bonitasoft.engine.api.impl.TenantConfiguration;
import org.bonitasoft.engine.api.impl.resolver.DependencyResolver;
import org.bonitasoft.engine.api.impl.transaction.actor.ImportActorMapping;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.authentication.GenericAuthenticationService;
import org.bonitasoft.engine.authentication.GenericAuthenticationServiceAccessor;
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
import org.bonitasoft.engine.core.process.instance.api.TransitionService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.api.ParentContainerResolver;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
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
import org.bonitasoft.engine.parameter.ParameterService;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.xml.ChildrenEntriesBinding;
import org.bonitasoft.engine.profile.xml.MembershipBinding;
import org.bonitasoft.engine.profile.xml.MembershipsBinding;
import org.bonitasoft.engine.profile.xml.ParentProfileEntryBinding;
import org.bonitasoft.engine.profile.xml.ProfileBinding;
import org.bonitasoft.engine.profile.xml.ProfileEntriesBinding;
import org.bonitasoft.engine.profile.xml.ProfileEntryBinding;
import org.bonitasoft.engine.profile.xml.ProfileMappingBinding;
import org.bonitasoft.engine.profile.xml.ProfilesBinding;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.service.PermissionService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.supervisor.mapping.SupervisorMappingService;
import org.bonitasoft.engine.synchro.SynchroService;
import org.bonitasoft.engine.theme.ThemeService;
import org.bonitasoft.engine.tracking.TimeTracker;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.bonitasoft.engine.work.WorkService;
import org.bonitasoft.engine.xml.ElementBinding;
import org.bonitasoft.engine.xml.Parser;
import org.bonitasoft.engine.xml.ParserFactory;
import org.bonitasoft.engine.xml.SInvalidSchemaException;
import org.bonitasoft.engine.xml.XMLWriter;

/**
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class SpringTenantServiceAccessor implements TenantServiceAccessor {

    private final SpringTenantFileSystemBeanAccessor beanAccessor;

    private IdentityService identityService;

    private LoginService loginService;

    private QueriableLoggerService queriableLoggerService;

    private TechnicalLoggerService technicalLoggerService;

    private TransactionService transactionService;

    private ProcessDefinitionService processDefinitionService;

    private ActivityInstanceService activityInstanceService;

    private ProcessInstanceService processInstanceService;

    private FlowNodeExecutor flowNodeExecutor;

    private ProcessExecutor processExecutor;

    private FlowNodeStateManager flowNodeStateManager;

    private TransactionExecutor transactionExecutor;

    private BPMInstancesCreator bpmInstancesCreator;

    private ActorMappingService actorMappingService;

    private ArchiveService archiveService;

    private CategoryService categoryService;

    private ExpressionService expressionService;

    private CommandService commandService;

    private TransitionService transitionService;

    private ClassLoaderService classLoaderService;

    private DependencyService dependencyService;

    private EventInstanceService eventInstanceService;

    private final long tenantId;

    private ConnectorService connectorService;

    private ConnectorInstanceService connectorInstanceService;

    private DocumentService documentService;

    private ProfileService profileService;

    private DataInstanceService dataInstanceService;

    private ParserFactory parserFactory;

    private OperationService operationService;

    private ExpressionResolverService expressionResolverService;

    private SupervisorMappingService supervisorService;

    private UserFilterService userFilterService;

    private SearchEntitiesDescriptor searchEntitiesDescriptor;

    private SCommentService commentService;

    private ContainerRegistry containerRegistry;

    private ExternalIdentityMappingService externalIdentityMappingService;

    private LockService lockService;

    private EventsHandler eventsHandler;

    private EventService eventService;

    private ConnectorExecutor connectorExecutor;

    private CacheService cacheService;

    private DependencyResolver dependencyResolver;

    private WorkService workService;

    private SessionService sessionService;

    private ReadSessionAccessor readSessionAccessor;

    private TransactionalProcessInstanceInterruptor transactionalProcessInstanceInterruptor;

    private SessionAccessor sessionAccessor;

    private SynchroService synchroService;

    private IncidentService incidentService;

    private SchedulerService schedulerService;

    private JobService jobService;

    private ThemeService themeService;

    private TenantConfiguration tenantConfiguration;

    private GatewayInstanceService gatewayInstanceService;

    private TransientDataService transientDataService;

    private TimeTracker timeTracker;

    private PermissionService permissionService;

    private ParentContainerResolver parentContainerResolver;

    private ContractDataService contractDataService;

    private ParameterService parameterService;

    private PageService pageService;

    private ApplicationService applicationService;

    private FormMappingService formMappingService;

    private BusinessDataRepository businessDataRespository;

    private RefBusinessDataService refBusinessDataService;

    private BusinessDataModelRepository businessDataModelRespository;

    private BusinessDataService businessDataService;
    private PageMappingService pageMappingService;
    private GenericAuthenticationService genericAuthenticationService;
    private ReadPersistenceService readPersistenceService;
    private Recorder recorder;


    @Override
    public ParentContainerResolver getParentContainerResolver() {
        if (parentContainerResolver == null) {
            parentContainerResolver = beanAccessor.getService(ParentContainerResolver.class);
        }
        return parentContainerResolver;
    }

    public SpringTenantServiceAccessor(final Long tenantId) {
        beanAccessor = SpringFileSystemBeanAccessorFactory.getTenantAccessor(tenantId);
        this.tenantId = tenantId;
    }

    @Override
    public TimeTracker getTimeTracker() {
        if (timeTracker == null) {
            timeTracker = beanAccessor.getService(TimeTracker.class);
        }
        return timeTracker;
    }

    @Override
    public SessionAccessor getSessionAccessor() {
        if (sessionAccessor == null) {
            sessionAccessor = beanAccessor.getService(SessionAccessor.class);
        }
        return sessionAccessor;
    }

    @Override
    public SessionService getSessionService() {
        if (sessionService == null) {
            sessionService = beanAccessor.getService(SessionService.class);
        }
        return sessionService;
    }

    @Override
    public IdentityService getIdentityService() {
        if (identityService == null) {
            identityService = beanAccessor.getService(IdentityService.class);
        }
        return identityService;
    }

    @Override
    public LoginService getLoginService() {
        if (loginService == null) {
            loginService = beanAccessor.getService(LoginService.class);
        }
        return loginService;
    }

    @Override
    public QueriableLoggerService getQueriableLoggerService() {
        if (queriableLoggerService == null) {
            queriableLoggerService = beanAccessor.getService("syncQueriableLoggerService", QueriableLoggerService.class);
        }
        return queriableLoggerService;
    }

    @Override
    public TechnicalLoggerService getTechnicalLoggerService() {
        if (technicalLoggerService == null) {
            technicalLoggerService = beanAccessor.getService("tenantTechnicalLoggerService", TechnicalLoggerService.class);
        }
        return technicalLoggerService;
    }

    private TransactionService getTransactionService() {
        if (transactionService == null) {
            transactionService = beanAccessor.getService(TransactionService.class);
        }
        return transactionService;
    }

    @Override
    public UserTransactionService getUserTransactionService() {
        return getTransactionService();
    }

    @Override
    public ProcessDefinitionService getProcessDefinitionService() {
        if (processDefinitionService == null) {
            processDefinitionService = beanAccessor.getService(ProcessDefinitionService.class);
        }
        return processDefinitionService;
    }

    @Override
    public ProcessInstanceService getProcessInstanceService() {
        if (processInstanceService == null) {
            processInstanceService = beanAccessor.getService(ProcessInstanceService.class);
        }
        return processInstanceService;
    }

    @Override
    public ActivityInstanceService getActivityInstanceService() {
        if (activityInstanceService == null) {
            activityInstanceService = beanAccessor.getService(ActivityInstanceService.class);
        }
        return activityInstanceService;
    }

    @Override
    public BPMInstancesCreator getBPMInstancesCreator() {
        if (bpmInstancesCreator == null) {
            bpmInstancesCreator = beanAccessor.getService(BPMInstancesCreator.class);
        }
        return bpmInstancesCreator;
    }

    @Override
    public FlowNodeExecutor getFlowNodeExecutor() {
        if (flowNodeExecutor == null) {
            flowNodeExecutor = beanAccessor.getService(FlowNodeExecutor.class);
        }
        return flowNodeExecutor;
    }

    @Override
    public ProcessExecutor getProcessExecutor() {
        if (processExecutor == null) {
            processExecutor = beanAccessor.getService(ProcessExecutor.class);
        }
        return processExecutor;
    }

    @Override
    public TransactionalProcessInstanceInterruptor getTransactionalProcessInstanceInterruptor() {
        if (transactionalProcessInstanceInterruptor == null) {
            transactionalProcessInstanceInterruptor = beanAccessor.getService(TransactionalProcessInstanceInterruptor.class);
        }
        return transactionalProcessInstanceInterruptor;
    }

    @Override
    public FlowNodeStateManager getFlowNodeStateManager() {
        if (flowNodeStateManager == null) {
            flowNodeStateManager = beanAccessor.getService(FlowNodeStateManager.class);
        }
        return flowNodeStateManager;
    }

    @Override
    public TransactionExecutor getTransactionExecutor() {
        if (transactionExecutor == null) {
            transactionExecutor = beanAccessor.getService(TransactionExecutor.class);
        }
        return transactionExecutor;
    }

    @Override
    public ActorMappingService getActorMappingService() {
        if (actorMappingService == null) {
            actorMappingService = beanAccessor.getService(ActorMappingService.class);
        }
        return actorMappingService;
    }

    @Override
    public ArchiveService getArchiveService() {
        if (archiveService == null) {
            archiveService = beanAccessor.getService(ArchiveService.class);
        }
        return archiveService;
    }

    @Override
    public CategoryService getCategoryService() {
        if (categoryService == null) {
            categoryService = beanAccessor.getService(CategoryService.class);
        }
        return categoryService;
    }

    @Override
    public CommandService getCommandService() {
        if (commandService == null) {
            commandService = beanAccessor.getService(CommandService.class);
        }
        return commandService;
    }

    @Override
    public TransitionService getTransitionInstanceService() {
        if (transitionService == null) {
            transitionService = beanAccessor.getService(TransitionService.class);
        }
        return transitionService;
    }

    @Override
    public ClassLoaderService getClassLoaderService() {
        if (classLoaderService == null) {
            classLoaderService = beanAccessor.getService("classLoaderService", ClassLoaderService.class);
        }
        return classLoaderService;
    }

    @Override
    public DependencyService getDependencyService() {
        if (dependencyService == null) {
            dependencyService = beanAccessor.getService("dependencyService", DependencyService.class);
        }
        return dependencyService;
    }

    @Override
    public long getTenantId() {
        return tenantId;
    }

    @Override
    public EventInstanceService getEventInstanceService() {
        if (eventInstanceService == null) {
            eventInstanceService = beanAccessor.getService(EventInstanceService.class);
        }
        return eventInstanceService;
    }

    @Override
    public ConnectorService getConnectorService() {
        if (connectorService == null) {
            connectorService = beanAccessor.getService("connectorService", ConnectorService.class);
        }
        return connectorService;
    }

    @Override
    public ConnectorInstanceService getConnectorInstanceService() {
        if (connectorInstanceService == null) {
            connectorInstanceService = beanAccessor.getService(ConnectorInstanceService.class);
        }
        return connectorInstanceService;
    }

    @Override
    public ConnectorExecutor getConnectorExecutor() {
        if (connectorExecutor == null) {
            connectorExecutor = beanAccessor.getService(ConnectorExecutor.class);
        }
        return connectorExecutor;
    }

    @Override
    public ExpressionService getExpressionService() {
        if (expressionService == null) {
            expressionService = beanAccessor.getService(ExpressionService.class);
        }
        return expressionService;
    }

    @Override
    public DocumentService getDocumentService() {
        if (documentService == null) {
            documentService = beanAccessor.getService(DocumentService.class);
        }
        return documentService;
    }

    @Override
    public ProfileService getProfileService() {
        if (profileService == null) {
            profileService = beanAccessor.getService(ProfileService.class);
        }
        return profileService;
    }

    @Override
    public DataInstanceService getDataInstanceService() {
        if (dataInstanceService == null) {
            dataInstanceService = beanAccessor.getService(DataInstanceService.class);
        }
        return dataInstanceService;
    }

    @Override
    public ParserFactory getParserFactgory() {
        if (parserFactory == null) {
            parserFactory = beanAccessor.getService(ParserFactory.class);
        }
        return parserFactory;
    }

    @Override
    public OperationService getOperationService() {
        if (operationService == null) {
            operationService = beanAccessor.getService(OperationService.class);
        }
        return operationService;
    }

    @Override
    public Parser getActorMappingParser() {
        final List<Class<? extends ElementBinding>> bindings = new ArrayList<Class<? extends ElementBinding>>();
        bindings.add(ActorMappingBinding.class);
        bindings.add(ActorBinding.class);
        bindings.add(UserNamesBinding.class);
        bindings.add(GroupPathsBinding.class);
        bindings.add(RoleNamesBinding.class);
        bindings.add(ActorMembershipBinding.class);
        final Parser parser = getParserFactgory().createParser(bindings);
        final InputStream resource = ImportActorMapping.class.getClassLoader().getResourceAsStream("actorMapping.xsd");
        try {
            parser.setSchema(resource);
            return parser;
        } catch (final SInvalidSchemaException ise) {
            throw new BonitaRuntimeException(ise);
        } finally {
            try {
                resource.close();
            } catch (final IOException ioe) {
                throw new BonitaRuntimeException(ioe);
            }
        }
    }

    @Override
    public XMLWriter getXMLWriter() {
        return beanAccessor.getService(XMLWriter.class);
    }

    @Override
    public ExpressionResolverService getExpressionResolverService() {
        if (expressionResolverService == null) {
            expressionResolverService = beanAccessor.getService(ExpressionResolverService.class);
        }
        return expressionResolverService;
    }

    @Override
    public SupervisorMappingService getSupervisorService() {
        if (supervisorService == null) {
            supervisorService = beanAccessor.getService(SupervisorMappingService.class);
        }
        return supervisorService;
    }

    @Override
    public UserFilterService getUserFilterService() {
        if (userFilterService == null) {
            userFilterService = beanAccessor.getService("userFilterService", UserFilterService.class);
        }
        return userFilterService;
    }

    @Override
    public SearchEntitiesDescriptor getSearchEntitiesDescriptor() {
        if (searchEntitiesDescriptor == null) {
            searchEntitiesDescriptor = beanAccessor.getService(SearchEntitiesDescriptor.class);
        }
        return searchEntitiesDescriptor;
    }

    @Override
    public SCommentService getCommentService() {
        if (commentService == null) {
            commentService = beanAccessor.getService(SCommentService.class);
        }
        return commentService;
    }

    @Override
    public ContainerRegistry getContainerRegistry() {
        if (containerRegistry == null) {
            containerRegistry = beanAccessor.getService(ContainerRegistry.class);
        }
        return containerRegistry;
    }

    @Override
    public ExternalIdentityMappingService getExternalIdentityMappingService() {
        if (externalIdentityMappingService == null) {
            externalIdentityMappingService = beanAccessor.getService(ExternalIdentityMappingService.class);
        }
        return externalIdentityMappingService;
    }

    @Override
    public LockService getLockService() {
        if (lockService == null) {
            lockService = beanAccessor.getService(LockService.class);
        }
        return lockService;
    }

    @Override
    public Parser getProfileParser() {
        final List<Class<? extends ElementBinding>> bindings = new ArrayList<Class<? extends ElementBinding>>();
        bindings.add(ProfileBinding.class);
        bindings.add(ProfilesBinding.class);
        bindings.add(ProfileEntryBinding.class);
        bindings.add(ParentProfileEntryBinding.class);
        bindings.add(ChildrenEntriesBinding.class);
        bindings.add(ProfileEntriesBinding.class);
        bindings.add(ProfileMappingBinding.class);
        bindings.add(MembershipsBinding.class);
        bindings.add(MembershipBinding.class);
        bindings.add(UserNamesBinding.class);
        bindings.add(RoleNamesBinding.class);
        bindings.add(RoleNamesBinding.class);
        bindings.add(GroupPathsBinding.class);
        final Parser parser = getParserFactgory().createParser(bindings);
        final InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream("profiles.xsd");
        try {
            parser.setSchema(resource);
            return parser;
        } catch (final SInvalidSchemaException ise) {
            throw new BonitaRuntimeException(ise);
        } finally {
            try {
                if (resource != null) {
                    resource.close();
                }
            } catch (final IOException ioe) {
                throw new BonitaRuntimeException(ioe);
            }
        }
    }

    @Override
    public EventsHandler getEventsHandler() {
        if (eventsHandler == null) {
            eventsHandler = beanAccessor.getService(EventsHandler.class);
        }
        return eventsHandler;
    }

    @Override
    public EventService getEventService() {
        if (eventService == null) {
            eventService = beanAccessor.getService(EventService.class);
        }
        return eventService;
    }

    public SpringTenantFileSystemBeanAccessor getBeanAccessor() {
        return beanAccessor;
    }

    @Override
    public CacheService getCacheService() {
        if (cacheService == null) {
            cacheService = beanAccessor.getService("cacheService", CacheService.class);
        }
        return cacheService;
    }

    @Override
    public DependencyResolver getDependencyResolver() {
        if (dependencyResolver == null) {
            dependencyResolver = beanAccessor.getService(DependencyResolver.class);
        }
        return dependencyResolver;
    }

    @Override
    public WorkService getWorkService() {
        if (workService == null) {
            workService = beanAccessor.getService(WorkService.class);
        }
        return workService;
    }

    @Override
    public SynchroService getSynchroService() {
        if (synchroService == null) {
            synchroService = beanAccessor.getService(SynchroService.class);
        }
        return synchroService;
    }

    @Override
    public IncidentService getIncidentService() {
        if (incidentService == null) {
            incidentService = beanAccessor.getService(IncidentService.class);
        }
        return incidentService;
    }

    @Override
    public SchedulerService getSchedulerService() {
        if (schedulerService == null) {
            schedulerService = beanAccessor.getService(SchedulerService.class);
        }
        return schedulerService;
    }

    @Override
    public JobService getJobService() {
        if (jobService == null) {
            jobService = beanAccessor.getService(JobService.class);
        }
        return jobService;
    }

    @Override
    public ThemeService getThemeService() {
        if (themeService == null) {
            themeService = beanAccessor.getService(ThemeService.class);
        }
        return themeService;
    }

    @Override
    public TransientDataService getTransientDataService() {
        if (transientDataService == null) {
            transientDataService = beanAccessor.getService(TransientDataService.class);
        }
        return transientDataService;
    }

    @Override
    public GatewayInstanceService getGatewayInstanceService() {
        if (gatewayInstanceService == null) {
            gatewayInstanceService = beanAccessor.getService(GatewayInstanceService.class);
        }
        return gatewayInstanceService;
    }

    @Override
    public void destroy() {
        beanAccessor.destroy();
    }

    @Override
    public TenantConfiguration getTenantConfiguration() {
        if (tenantConfiguration == null) {
            tenantConfiguration = beanAccessor.getService(TenantConfiguration.class);
        }
        return tenantConfiguration;
    }

    @Override
    public <T> T lookup(final String serviceName) {
        return beanAccessor.getService(serviceName);
    }

    @Override
    public PermissionService getPermissionService() {
        if (permissionService == null) {
            permissionService = beanAccessor.getService(PermissionService.class);
        }
        return permissionService;
    }

    @Override
    public ContractDataService getContractDataService() {
        if (contractDataService == null) {
            contractDataService = beanAccessor.getService(ContractDataService.class);
        }
        return contractDataService;
    }

    @Override
    public ParameterService getParameterService() {
        if (parameterService == null) {
            parameterService = beanAccessor.getService(ParameterService.class);
        }
        return parameterService;
    }
    /**
     * might not be an available service
     */
    @Override
    public PageService getPageService() {
        if (pageService == null) {
            pageService = beanAccessor.getService(PageService.class);
        }
        return pageService;
    }

    @Override
    public ApplicationService getApplicationService() {
        if (applicationService == null) {
            applicationService = beanAccessor.getService(ApplicationService.class);
        }
        return applicationService;
    }

    @Override
    public BusinessDataRepository getBusinessDataRepository() {
        if (businessDataRespository == null) {
            businessDataRespository = beanAccessor.getService(BusinessDataRepository.class);
        }
        return businessDataRespository;
    }

    @Override
    public BusinessDataModelRepository getBusinessDataModelRepository() {
        if (businessDataModelRespository == null) {
            businessDataModelRespository = beanAccessor.getService(BusinessDataModelRepository.class);
        }
        return businessDataModelRespository;
    }

    @Override
    public RefBusinessDataService getRefBusinessDataService() {
        if (refBusinessDataService == null) {
            refBusinessDataService = beanAccessor.getService(RefBusinessDataService.class);
        }
        return refBusinessDataService;
    }

    @Override
    public GenericAuthenticationService getAuthenticationService() {
        if (genericAuthenticationService == null) {
            genericAuthenticationService = beanAccessor.getService(GenericAuthenticationServiceAccessor.class).getAuthenticationService();
        }
        return genericAuthenticationService;
    }

    @Override
    public ReadPersistenceService getReadPersistenceService() {
        if (readPersistenceService == null) {
            readPersistenceService = beanAccessor.getService("persistenceService");
        }
        return readPersistenceService;
    }

    @Override
    public Recorder getRecorder() {
        if (recorder == null) {
            recorder = beanAccessor.getService(Recorder.class);
        }
        return recorder;
    }

    @Override
    public BusinessDataService getBusinessDataService() {
        if (businessDataService == null) {
            businessDataService = beanAccessor.getService(BusinessDataService.class);
        }
        return businessDataService;
    }



    @Override
    public FormMappingService getFormMappingService() {
        if (formMappingService == null) {
            formMappingService = beanAccessor.getService(FormMappingService.class);
        }
        return formMappingService;
    }

    @Override
    public PageMappingService getPageMappingService() {
        if (pageMappingService == null) {
            pageMappingService = beanAccessor.getService(PageMappingService.class);
        }
        return pageMappingService;
    }
}
