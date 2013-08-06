/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * accessor program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * accessor program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with accessor program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.bpm;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.model.SActorBuilders;
import org.bonitasoft.engine.api.impl.NodeConfiguration;
import org.bonitasoft.engine.api.impl.resolver.DependencyResolver;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.authentication.AuthenticationService;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.cache.PlatformCacheService;
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
import org.bonitasoft.engine.core.platform.login.PlatformLoginService;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.comment.model.builder.SCommentBuilders;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.builder.BPMDefinitionBuilders;
import org.bonitasoft.engine.core.process.document.api.ProcessDocumentService;
import org.bonitasoft.engine.core.process.document.mapping.DocumentMappingService;
import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingBuilderAccessor;
import org.bonitasoft.engine.core.process.document.model.builder.SProcessDocumentBuilder;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.GatewayInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.TokenService;
import org.bonitasoft.engine.core.process.instance.api.TransitionService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.data.DataService;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilders;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilders;
import org.bonitasoft.engine.data.model.builder.SDataSourceBuilder;
import org.bonitasoft.engine.data.model.builder.SDataSourceModelBuilder;
import org.bonitasoft.engine.data.model.builder.SDataSourceParameterBuilder;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.builder.DependencyBuilder;
import org.bonitasoft.engine.dependency.model.builder.DependencyBuilderAccessor;
import org.bonitasoft.engine.dependency.model.builder.DependencyMappingBuilder;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.exceptions.ExceptionsManager;
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
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.authentication.PlatformAuthenticationService;
import org.bonitasoft.engine.platform.command.PlatformCommandService;
import org.bonitasoft.engine.platform.command.model.SPlatformCommandBuilderAccessor;
import org.bonitasoft.engine.platform.model.builder.SPlatformBuilder;
import org.bonitasoft.engine.platform.model.builder.STenantBuilder;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.builder.SProfileBuilderAccessor;
import org.bonitasoft.engine.queriablelogger.model.builder.SQueriableLogModelBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.impl.SessionAccessorAccessor;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.supervisor.mapping.SupervisorMappingService;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisorBuilders;
import org.bonitasoft.engine.test.util.ServicesAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.work.WorkService;
import org.bonitasoft.engine.xml.Parser;
import org.bonitasoft.engine.xml.ParserFactory;
import org.bonitasoft.engine.xml.XMLWriter;

/**
 * @author Baptiste Mesta
 * @author Hongwen Zang
 * @author Zhao Na
 * @author Celine Souchet
 */
public class BPMServicesBuilder implements PlatformServiceAccessor, TenantServiceAccessor, SessionAccessorAccessor {

    ServicesAccessor accessor;

    public BPMServicesBuilder() {
        super();
        accessor = ServicesAccessor.getInstance();
    }

    public BPMServicesBuilder(final Long tenantid) {
        super();
        accessor = ServicesAccessor.getInstance();
    }

    @Override
    public ProcessDefinitionService getProcessDefinitionService() {
        return accessor.getInstanceOf(ProcessDefinitionService.class);
    }

    @Override
    public ExpressionService getExpressionService() {
        return accessor.getInstanceOf(ExpressionService.class);
    }

    public QueriableLoggerService getQueriableLogger(final String name) {
        return accessor.getInstanceOf(name, QueriableLoggerService.class);
    }

    public QueriableLoggerService getQueriableLogger() {
        return this.getQueriableLogger("syncQueriableLoggerService"); // default is the sync one
    }

    @Override
    public IdentityModelBuilder getIdentityModelBuilder() {
        return accessor.getInstanceOf(IdentityModelBuilder.class);
    }

    public SExpressionBuilders getExpressionBuilders() {
        return accessor.getInstanceOf(SExpressionBuilders.class);
    }

    public SDataInstanceBuilders getSDataInstanceBuilders() {
        return accessor.getInstanceOf(SDataInstanceBuilders.class);
    }

    @Override
    public SDataDefinitionBuilders getSDataDefinitionBuilders() {
        return accessor.getInstanceOf(SDataDefinitionBuilders.class);
    }

    public DependencyBuilder getDependencyModelBuilder() {
        return accessor.getInstanceOf(DependencyBuilder.class);
    }

    public DependencyMappingBuilder getDependencyMappingModelBuilder() {
        return accessor.getInstanceOf(DependencyMappingBuilder.class);
    }

    public PersistenceService getPersistence() {
        return this.getPersistence("persistenceService");
    }

    public PersistenceService getPersistence(final String name) {
        return accessor.getInstanceOf(name, PersistenceService.class);
    }

    public PersistenceService getJournal() {
        return this.getPersistence();
    }

    public PersistenceService geaccessortory() {
        return this.getPersistence("history");
    }

    public Recorder getRecorder(final boolean sync) {
        String synchType = "recorderAsync";
        if (sync) {
            synchType = "recorderSync";
        }
        return accessor.getInstanceOf(synchType, Recorder.class);
    }

    @Override
    public TransactionService getTransactionService() {
        return accessor.getInstanceOf(TransactionService.class);
    }

    @Override
    public PlatformService getPlatformService() {
        return accessor.getInstanceOf(PlatformService.class);
    }

    @Override
    public DataService getDataService() {
        return accessor.getInstanceOf(DataService.class);
    }

    public SDataSourceParameterBuilder getDataSourceParameterModelBuilder() {
        return accessor.getInstanceOf(SDataSourceParameterBuilder.class);
    }

    public SDataSourceBuilder getDataSourceModelBuilder() {
        return accessor.getInstanceOf(SDataSourceBuilder.class);
    }

    public SPlatformBuilder getPlatformBuilder() {
        return accessor.getInstanceOf(SPlatformBuilder.class);
    }

    public STenantBuilder getTenantBuilder() {
        return accessor.getInstanceOf(STenantBuilder.class);
    }

    @Override
    public SessionAccessor getSessionAccessor() {
        return accessor.getInstanceOf(SessionAccessor.class);
    }

    @Override
    public IdentityService getIdentityService() {
        return accessor.getInstanceOf(IdentityService.class);
    }

    @Override
    public ArchiveService getArchiveService() {
        return accessor.getInstanceOf(ArchiveService.class);
    }

    @Override
    public DataInstanceService getDataInstanceService() {
        return accessor.getInstanceOf(DataInstanceService.class);
    }

    @Override
    public DependencyService getDependencyService() {
        return accessor.getInstanceOf("platformDependencyService", DependencyService.class);
    }

    public SQueriableLogModelBuilder getQueriableLogModelBuilder() {
        return accessor.getInstanceOf(SQueriableLogModelBuilder.class);
    }

    @Override
    public SchedulerService getSchedulerService() {
        return accessor.getInstanceOf(SchedulerService.class);
    }

    @Override
    public ClassLoaderService getClassLoaderService() {
        return accessor.getInstanceOf(ClassLoaderService.class);
    }

    @Override
    public EventService getEventService() {
        return accessor.getInstanceOf(EventService.class);
    }

    public ExceptionsManager getExceptionsManager() {
        return accessor.getInstanceOf(ExceptionsManager.class);
    }

    public ProcessDefinitionService getProcessDefinitionManager() {
        return accessor.getInstanceOf(ProcessDefinitionService.class);
    }

    public FlowNodeInstanceService getActivityInstanceManager() {
        return accessor.getInstanceOf(ActivityInstanceService.class);
    }

    @Override
    public CacheService getCacheService() {
        return accessor.getInstanceOf(CacheService.class);
    }

    public AuthenticationService getAuthenticationService() {
        return accessor.getInstanceOf(AuthenticationService.class);
    }

    public PlatformAuthenticationService getPlatformAuthenticationService() {
        return accessor.getInstanceOf(PlatformAuthenticationService.class);
    }

    @Override
    public SessionService getSessionService() {
        return accessor.getInstanceOf(SessionService.class);
    }

    @Override
    public PlatformSessionService getPlatformSessionService() {
        return accessor.getInstanceOf(PlatformSessionService.class);
    }

    @Override
    public PlatformLoginService getPlatformLoginService() {
        return accessor.getInstanceOf(PlatformLoginService.class);
    }

    @Override
    public LoginService getLoginService() {
        return accessor.getInstanceOf(LoginService.class);
    }

    @Override
    public TechnicalLoggerService getTechnicalLoggerService() {
        return accessor.getInstanceOf(TechnicalLoggerService.class);
    }

    @Override
    public SPlatformBuilder getSPlatformBuilder() {
        return accessor.getInstanceOf(SPlatformBuilder.class);
    }

    @Override
    public STenantBuilder getSTenantBuilder() {
        return accessor.getInstanceOf(STenantBuilder.class);
    }

    @Override
    public TenantServiceAccessor getTenantServiceAccessor(final long tenantId) {
        return this;
    }

    @Override
    public TransactionExecutor getTransactionExecutor() {
        return accessor.getInstanceOf(TransactionExecutor.class);
    }

    @Override
    public QueriableLoggerService getQueriableLoggerService() {
        return accessor.getInstanceOf(QueriableLoggerService.class);
    }

    @Override
    public SQueriableLogModelBuilder getSQueriableLogModelBuilder() {
        return accessor.getInstanceOf(SQueriableLogModelBuilder.class);
    }

    @Override
    public ProcessInstanceService getProcessInstanceService() {
        return accessor.getInstanceOf(ProcessInstanceService.class);
    }

    @Override
    public TokenService getTokenService() {
        return accessor.getInstanceOf(TokenService.class);
    }

    @Override
    public ActivityInstanceService getActivityInstanceService() {
        return accessor.getInstanceOf(ActivityInstanceService.class);
    }

    @Override
    public BPMDefinitionBuilders getBPMDefinitionBuilders() {
        return accessor.getInstanceOf(BPMDefinitionBuilders.class);
    }

    @Override
    public BPMInstanceBuilders getBPMInstanceBuilders() {
        return accessor.getInstanceOf(BPMInstanceBuilders.class);
    }

    @Override
    public FlowNodeExecutor getFlowNodeExecutor() {
        return accessor.getInstanceOf(FlowNodeExecutor.class);
    }

    @Override
    public ProcessExecutor getProcessExecutor() {
        return accessor.getInstanceOf(ProcessExecutor.class);
    }

    @Override
    public TransactionalProcessInstanceInterruptor getTransactionalProcessInstanceInterruptor() {
        return accessor.getInstanceOf(TransactionalProcessInstanceInterruptor.class);
    }

    @Override
    public FlowNodeStateManager getFlowNodeStateManager() {
        return accessor.getInstanceOf(FlowNodeStateManager.class);
    }

    @Override
    public ActorMappingService getActorMappingService() {
        return accessor.getInstanceOf(ActorMappingService.class);
    }

    @Override
    public SActorBuilders getSActorBuilders() {
        return accessor.getInstanceOf(SActorBuilders.class);
    }

    @Override
    public SCategoryBuilderAccessor getCategoryModelBuilderAccessor() {
        return accessor.getInstanceOf(SCategoryBuilderAccessor.class);
    }

    @Override
    public CategoryService getCategoryService() {
        return accessor.getInstanceOf(CategoryService.class);
    }

    @Override
    public SExpressionBuilders getSExpressionBuilders() {
        return accessor.getInstanceOf(SExpressionBuilders.class);
    }

    public GatewayInstanceService getGatewayInstanceService() {
        return accessor.getInstanceOf(GatewayInstanceService.class);
    }

    @Override
    public TransitionService getTransitionInstanceService() {
        return accessor.getInstanceOf(TransitionService.class);
    }

    @Override
    public CommandService getCommandService() {
        return accessor.getInstanceOf(CommandService.class);
    }

    @Override
    public DocumentMappingService getDocumentMappingService() {
        return accessor.getInstanceOf(DocumentMappingService.class);
    }

    @Override
    public SDocumentMappingBuilderAccessor getDocumentMappingBuilderAccessor() {
        return accessor.getInstanceOf(SDocumentMappingBuilderAccessor.class);
    }

    @Override
    public ProcessDocumentService getProcessDocumentService() {
        return accessor.getInstanceOf(ProcessDocumentService.class);
    }

    @Override
    public SProcessDocumentBuilder getProcessDocumentBuilder() {
        return accessor.getInstanceOf(SProcessDocumentBuilder.class);
    }

    @Override
    public SCommandBuilderAccessor getSCommandBuilderAccessor() {
        return accessor.getInstanceOf(SCommandBuilderAccessor.class);
    }

    @Override
    public EventInstanceService getEventInstanceService() {
        return accessor.getInstanceOf(EventInstanceService.class);
    }

    @Override
    public DependencyBuilderAccessor getDependencyBuilderAccessor() {
        return accessor.getInstanceOf("platformDependencyBuilderAccessor", DependencyBuilderAccessor.class);
    }

    @Override
    public long getTenantId() {
        return 0;
    }

    @Override
    public ConnectorService getConnectorService() {
        return accessor.getInstanceOf(ConnectorService.class);
    }

    @Override
    public ConnectorInstanceService getConnectorInstanceService() {
        return accessor.getInstanceOf(ConnectorInstanceService.class);
    }

    @Override
    public ProfileService getProfileService() {
        return accessor.getInstanceOf(ProfileService.class);
    }

    @Override
    public SProfileBuilderAccessor getSProfileBuilderAccessor() {
        return accessor.getInstanceOf(SProfileBuilderAccessor.class);
    }

    @Override
    public PlatformCommandService getPlatformCommandService() {
        return accessor.getInstanceOf(PlatformCommandService.class);
    }

    @Override
    public SPlatformCommandBuilderAccessor getSPlatformCommandBuilderAccessor() {
        return accessor.getInstanceOf(SPlatformCommandBuilderAccessor.class);
    }

    @Override
    public SDataSourceModelBuilder getSDataSourceModelBuilder() {
        return accessor.getInstanceOf(SDataSourceModelBuilder.class);
    }

    @Override
    public ParserFactory getParserFactgory() {
        return accessor.getInstanceOf(ParserFactory.class);
    }

    @Override
    public Parser getActorMappingParser() {
        return accessor.getInstanceOf(Parser.class);
    }

    @Override
    public ExpressionResolverService getExpressionResolverService() {
        return accessor.getInstanceOf(ExpressionResolverService.class);
    }

    @Override
    public XMLWriter getXMLWriter() {
        return accessor.getInstanceOf(XMLWriter.class);
    }

    @Override
    public SupervisorMappingService getSupervisorService() {
        return accessor.getInstanceOf(SupervisorMappingService.class);
    }

    @Override
    public SProcessSupervisorBuilders getSSupervisorBuilders() {
        return accessor.getInstanceOf(SProcessSupervisorBuilders.class);
    }

    @Override
    public OperationService getOperationService() {
        return accessor.getInstanceOf(OperationService.class);
    }

    @Override
    public SOperationBuilders getSOperationBuilders() {
        return accessor.getInstanceOf(SOperationBuilders.class);
    }

    @Override
    public UserFilterService getUserFilterService() {
        return accessor.getInstanceOf("userFilterService", UserFilterService.class);
    }

    @Override
    public SearchEntitiesDescriptor getSearchEntitiesDescriptor() {
        return accessor.getInstanceOf(SearchEntitiesDescriptor.class);
    }

    @Override
    public SCommentService getCommentService() {
        return accessor.getInstanceOf(SCommentService.class);
    }

    @Override
    public SCommentBuilders getSCommentBuilders() {
        return accessor.getInstanceOf(SCommentBuilders.class);
    }

    @Override
    public ContainerRegistry getContainerRegistry() {
        return accessor.getInstanceOf(ContainerRegistry.class);
    }

    @Override
    public ExternalIdentityMappingService getExternalIdentityMappingService() {
        return accessor.getInstanceOf(ExternalIdentityMappingService.class);
    }

    @Override
    public SExternalIdentityMappingBuilders getExternalIdentityMappingBuilders() {
        return accessor.getInstanceOf(SExternalIdentityMappingBuilders.class);
    }

    @Override
    public LockService getLockService() {
        return accessor.getInstanceOf(LockService.class);
    }

    @Override
    public Parser getProfileParser() {
        return accessor.getInstanceOf(Parser.class);
    }

    @Override
    public EventsHandler getEventsHandler() {
        return accessor.getInstanceOf(EventsHandler.class);
    }

    @Override
    public void initializeServiceAccessor(final ClassLoader classLoader) {
    }

    @Override
    public NodeConfiguration getPlaformConfiguration() {
        return accessor.getInstanceOf(NodeConfiguration.class);
    }

    @Override
    public ConnectorExecutor getConnectorExecutor() {
        return accessor.getInstanceOf(ConnectorExecutor.class);
    }

    @Override
    public WorkService getWorkService() {
        return accessor.getInstanceOf(WorkService.class);
    }

    @Override
    public DependencyResolver getDependencyResolver() {
        return accessor.getInstanceOf(DependencyResolver.class);
    }

    public <T> T getInstanceOf(final Class<T> class1) {
        return accessor.getInstanceOf(class1);
    }

    public <T> T getInstanceOf(final String name, final Class<T> class1) {
        return accessor.getInstanceOf(name, class1);
    }

    @Override
    public DefaultCommandProvider getDefaultCommandProvider() {
        return accessor.getInstanceOf(DefaultCommandProvider.class);
    }

    @Override
    public PlatformCacheService getPlatformCacheService() {
        return accessor.getInstanceOf(PlatformCacheService.class);
    }

    @Override
    public ReadSessionAccessor getReadSessionAccessor() {
        return accessor.getInstanceOf(ReadSessionAccessor.class);
    }

}
