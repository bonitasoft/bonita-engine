/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.bpm.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.model.SActorBuilders;
import org.bonitasoft.engine.actor.privilege.api.ActorPrivilegeService;
import org.bonitasoft.engine.actor.privilege.model.builder.ActorPrivilegeBuilders;
import org.bonitasoft.engine.api.impl.NodeConfiguration;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.authentication.AuthenticationService;
import org.bonitasoft.engine.businesslogger.model.builder.SBusinessLogModelBuilder;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.command.CommandService;
import org.bonitasoft.engine.command.model.SCommandBuilderAccessor;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.category.CategoryService;
import org.bonitasoft.engine.core.category.model.builder.SCategoryBuilderAccessor;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.filter.UserFilterService;
import org.bonitasoft.engine.core.login.LoginService;
import org.bonitasoft.engine.core.migration.MigrationPlanService;
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
import org.bonitasoft.engine.core.process.instance.api.BreakpointService;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.GatewayInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
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
import org.bonitasoft.engine.monitoring.PlatformMonitoringService;
import org.bonitasoft.engine.monitoring.TenantMonitoringService;
import org.bonitasoft.engine.monitoring.mbean.SJvmMXBean;
import org.bonitasoft.engine.monitoring.mbean.SPlatformServiceMXBean;
import org.bonitasoft.engine.monitoring.mbean.SServiceMXBean;
import org.bonitasoft.engine.parameter.ParameterService;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.authentication.PlatformAuthenticationService;
import org.bonitasoft.engine.platform.command.PlatformCommandService;
import org.bonitasoft.engine.platform.command.model.SPlatformCommandBuilderAccessor;
import org.bonitasoft.engine.platform.model.builder.SPlatformBuilder;
import org.bonitasoft.engine.platform.model.builder.STenantBuilder;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.privilege.api.PrivilegeService;
import org.bonitasoft.engine.privilege.model.buidler.PrivilegeBuilders;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.model.SProfileBuilderAccessor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.search.SearchEntitiesDescriptor;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.impl.SessionAccessorAccessor;
import org.bonitasoft.engine.services.BusinessLoggerService;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.supervisor.mapping.SupervisorMappingService;
import org.bonitasoft.engine.supervisor.mapping.model.SSupervisorBuilders;
import org.bonitasoft.engine.test.util.BaseServicesBuilder;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.xml.Parser;
import org.bonitasoft.engine.xml.ParserFactory;
import org.bonitasoft.engine.xml.XMLWriter;

import com.bonitasoft.engine.search.SearchPlatformEntitiesDescriptor;

/**
 * @author Baptiste Mesta
 * @author Hongwen Zang
 * @author Zhao Na
 */
public class SPBPMServicesBuilder extends BaseServicesBuilder implements PlatformServiceAccessor, TenantServiceAccessor, SessionAccessorAccessor {

    private static final String BONITA_TEST_PROCESS_DEFINITION_MODEL = "bonita.test.process.definition.model";

    private static final String BONITA_TEST_PROCESS_DEFINITION = "bonita.test.process.definition";

    private static final String BONITA_TEST_PROCESS_INSTANCE_MODEL = "bonita.test.process.instance.model";

    private static final String BONITA_TEST_PROCESS_INSTANCE = "bonita.test.process.instance";

    private static final String BONITA_TEST_ACTOR = "bonita.test.actor";

    private static final String BONITA_TEST_LOGIN = "bonita.test.login";

    private static final String BONITA_TEST_PARAMETER = "bonita.test.parameter";

    private static final String BONITA_TEST_DOCUMENT_MAPPING_MODEL = "bonita.test.document.mapping.model";

    private static final String BONITA_TEST_MAPPING_DOCUMENT = "bonita.test.document.mapping";

    private static final String BONITA_TEST_DOCUMENT = "bonita.test.document";

    private static final String BONITA_TEST_CONNECTOR = "bonita.test.connector";

    private static final String BONITA_TEST_OPERATION = "bonita.test.operation";

    private static final String BONITA_TEST_OPERATION_MODEL = "bonita.test.operation.model";

    private static final String BONITA_TEST_SUPERVISOR = "bonita.test.supervisor";

    private static final String BONITA_TEST_USER_FILTER = "bonita.test.user.filter";

    private static final String BONITA_TEST_ACTOR_PRIVILEGE = "bonita.test.actor.privilege";

    private static final String BONITA_TEST_ACTOR_PRIVILEGE_MODEL = "bonita.test.actor.privilege.model";

    private static final String BONITA_TEST_PROCESS_COMMENT = "bonita.test.process.comment";

    private static final String BONITA_TEST_LOCK = "bonita.test.lock";

    private static final String BONITA_TEST_PROCESS_COMMENT_MODEL = "bonita.test.process.comment.model";

    private static Map<String, String> propertyKeyToPath;

    private static HashSet<String> modelKeyList;

    private static Map<String, String> defaultImplementations;

    static {
        propertyKeyToPath = new HashMap<String, String>();
        modelKeyList = new HashSet<String>();
        defaultImplementations = new HashMap<String, String>();
        // actor
        add(BONITA_TEST_ACTOR, "org/bonitasoft/engine/actor/mapping", "bos-actor-mapping-impl", true);
        // login
        add(BONITA_TEST_LOGIN, "org/bonitasoft/engine/core/login", "bonita-login", false);
        // parameter
        add(BONITA_TEST_PARAMETER, "org/bonitasoft/engine/core/parameter", "bos-parameter-propertyfile", true);
        // Process definition
        add(BONITA_TEST_PROCESS_DEFINITION, "org/bonitasoft/engine/core/process/definition", "bonita-process-definition-impl", false);
        add(BONITA_TEST_PROCESS_DEFINITION_MODEL, "org/bonitasoft/engine/core/process/definition/model", "bonita-process-definition-model-impl", true);
        // process instance
        add(BONITA_TEST_PROCESS_INSTANCE, "org/bonitasoft/engine/core/process/instance", "bonita-process-instance-impl", false);
        add(BONITA_TEST_PROCESS_INSTANCE_MODEL, "org/bonitasoft/engine/core/process/instance/model", "bonita-process-instance-model-impl", true);
        // process document mapping:
        add(BONITA_TEST_MAPPING_DOCUMENT, "org/bonitasoft/engine/core/process/document/mapping/api", "bos-process-document-mapping-api-impl", false);
        add(BONITA_TEST_DOCUMENT_MAPPING_MODEL, "org/bonitasoft/engine/core/process/document/mapping/model", "bos-process-document-mapping-model-impl", true);
        // process document :
        add(BONITA_TEST_DOCUMENT, "org/bonitasoft/engine/core/process/document/api", "bos-process-document-impl", false);
        // connector :
        add(BONITA_TEST_CONNECTOR, "org/bonitasoft/engine/core/connector/service", "bos-connector-service-api-impl", false);
        // operation
        add(BONITA_TEST_OPERATION, "org/bonitasoft/engine/core/operation", "bos-operation-api-impl", false);
        add(BONITA_TEST_OPERATION_MODEL, "org/bonitasoft/engine/core/operation/model/impl", "bos-operation-model-imp", true);
        // process supervisor
        add(BONITA_TEST_SUPERVISOR, "org/bonitasoft/engine/supervisor/mapping", "bos-supervisor-mapping-impl", false);
        // user filter
        add(BONITA_TEST_USER_FILTER, "org/bonitasoft/engine/core/filter/user", "bos-user-filter-impl", false);
        // actor privilege
        add(BONITA_TEST_ACTOR_PRIVILEGE, "org/bonitasoft/engine/actor/privilege/api/impl", "bos-actor-privilege-api-impl", false);
        add(BONITA_TEST_ACTOR_PRIVILEGE_MODEL, "org/bonitasoft/engine/actor/privilege/model/impl", "bos-actor-privilege-model-impl", true);
        // comment
        add(BONITA_TEST_PROCESS_COMMENT, "org/bonitasoft/engine/core/process/comment/api", "bos-process-comment-api-impl", false);
        add(BONITA_TEST_PROCESS_COMMENT_MODEL, "org/bonitasoft/engine/core/process/comment/model/impl", "bos-process-comment-model-impl", true);
        // lock service
        add(BONITA_TEST_LOCK, "org/bonitasoft/engine/lock", "bonita-lock-memory-impl", false);
    }

    public SPBPMServicesBuilder() {
        super();
    }

    public SPBPMServicesBuilder(final Long tenantid) {
    }

    /**
     * @param key
     *            key that is used in to change the implementation
     * @param path
     *            path of the file (must be the same for each implementation)
     * @param defaultImpl
     *            name of the default implementation
     * @param isModel
     *            true if we need to include db specific file for this module
     */
    private static void add(final String key, final String path, final String defaultImpl, final boolean isModel) {
        propertyKeyToPath.put(key, path);
        defaultImplementations.put(key, defaultImpl);
        if (isModel) {
            modelKeyList.add(key);
        }
    }

    @Override
    public ProcessDefinitionService getProcessDefinitionService() {
        return this.getInstanceOf(ProcessDefinitionService.class);
    }

    @Override
    public ExpressionService getExpressionService() {
        return this.getInstanceOf(ExpressionService.class);
    }

    @Override
    protected List<String> getResourceList() {
        final List<String> resources = super.getResourceList();
        final String persistenceService = System.getProperty(BONITA_TEST_PERSISTENCE, getDefaultPersistenceType());
        final String persistenceType = persistenceService.substring(BONITA_PERSISTENCE.length());
        for (final Entry<String, String> entry : propertyKeyToPath.entrySet()) {
            resources.add(getServiceImplementationConfigurationFile(entry.getValue(), entry.getKey(), defaultImplementations.get(entry.getKey())));
            if (modelKeyList.contains(entry.getKey())) {
                resources.add(getPersistenceServiceConfigurationFileForModule(entry.getValue(), entry.getKey(), defaultImplementations.get(entry.getKey()),
                        persistenceType));
            }
        }
        return resources;
    }

    public BusinessLoggerService getBusinessLogger(final String name) {
        return this.getInstanceOf(name, BusinessLoggerService.class);
    }

    public BusinessLoggerService getBusinessLogger() {
        return this.getBusinessLogger("syncBusinessLoggerService"); // default is the sync one
    }

    @Override
    public IdentityModelBuilder getIdentityModelBuilder() {
        return this.getInstanceOf(IdentityModelBuilder.class);
    }

    public SExpressionBuilders getExpressionBuilders() {
        return this.getInstanceOf(SExpressionBuilders.class);
    }

    public SDataInstanceBuilders geterSDataInstanceBuilder() {
        return this.getInstanceOf(SDataInstanceBuilders.class);
    }

    public SDataDefinitionBuilders geterSDataDefinitionBuilders() {
        return this.getInstanceOf(SDataDefinitionBuilders.class);
    }

    public DependencyBuilder getDependencyModelBuilder() {
        return this.getInstanceOf(DependencyBuilder.class);
    }

    public DependencyMappingBuilder getDependencyMappingModelBuilder() {
        return this.getInstanceOf(DependencyMappingBuilder.class);
    }

    public PersistenceService getPersistence() {
        return this.getPersistence("persistenceService");
    }

    public PersistenceService getPersistence(final String name) {
        return this.getInstanceOf(name, PersistenceService.class);
    }

    public PersistenceService getJournal() {
        return this.getPersistence();
    }

    public PersistenceService getHistory() {
        return this.getPersistence("history");
    }

    public Recorder getRecorder(final boolean sync) {
        String synchType = "recorderAsync";
        if (sync) {
            synchType = "recorderSync";
        }
        return this.getInstanceOf(synchType, Recorder.class);
    }

    @Override
    public TransactionService getTransactionService() {
        return this.getInstanceOf(TransactionService.class);
    }

    @Override
    public PlatformService getPlatformService() {
        return this.getInstanceOf(PlatformService.class);
    }

    @Override
    public DataService getDataService() {
        return this.getInstanceOf(DataService.class);
    }

    public SDataSourceParameterBuilder getDataSourceParameterModelBuilder() {
        return this.getInstanceOf(SDataSourceParameterBuilder.class);
    }

    public SDataSourceBuilder getDataSourceModelBuilder() {
        return this.getInstanceOf(SDataSourceBuilder.class);
    }

    public SPlatformBuilder getPlatformBuilder() {
        return this.getInstanceOf(SPlatformBuilder.class);
    }

    public STenantBuilder getTenantBuilder() {
        return this.getInstanceOf(STenantBuilder.class);
    }

    @Override
    public SessionAccessor getSessionAccessor() {
        return this.getInstanceOf(SessionAccessor.class);
    }

    @Override
    public IdentityService getIdentityService() {
        return this.getInstanceOf(IdentityService.class);
    }

    @Override
    public ArchiveService getArchiveService() {
        return this.getInstanceOf(ArchiveService.class);
    }

    @Override
    public DataInstanceService getDataInstanceService() {
        return this.getInstanceOf(DataInstanceService.class);
    }

    @Override
    public DependencyService getDependencyService() {
        return this.getInstanceOf("platformDependencyService", DependencyService.class);
    }

    public SBusinessLogModelBuilder getBusinessLogModelBuilder() {
        return this.getInstanceOf(SBusinessLogModelBuilder.class);
    }

    @Override
    public SchedulerService getSchedulerService() {
        return this.getInstanceOf(SchedulerService.class);
    }

    @Override
    public ClassLoaderService getClassLoaderService() {
        return this.getInstanceOf(ClassLoaderService.class);
    }

    public SServiceMXBean getServiceMXBean() {
        return this.getInstanceOf(SServiceMXBean.class);
    }

    public SPlatformServiceMXBean getPlatformServiceMXBean() {
        return this.getInstanceOf(SPlatformServiceMXBean.class);
    }

    public SJvmMXBean getJvmMXBean() {
        return this.getInstanceOf(SJvmMXBean.class);
    }

    @Override
    public EventService getEventService() {
        return this.getInstanceOf(EventService.class);
    }

    public ExceptionsManager getExceptionsManager() {
        return this.getInstanceOf(ExceptionsManager.class);
    }

    public ProcessDefinitionService getProcessDefinitionManager() {
        return this.getInstanceOf(ProcessDefinitionService.class);
    }

    public FlowNodeInstanceService getActivityInstanceManager() {
        return this.getInstanceOf(ActivityInstanceService.class);
    }

    public CacheService getCacheService() {
        return this.getInstanceOf(CacheService.class);
    }

    public AuthenticationService getAuthenticationService() {
        return this.getInstanceOf(AuthenticationService.class);
    }

    public PlatformAuthenticationService getPlatformAuthenticationService() {
        return this.getInstanceOf(PlatformAuthenticationService.class);
    }

    @Override
    public SessionService getSessionService() {
        return this.getInstanceOf(SessionService.class);
    }

    @Override
    public PlatformSessionService getPlatformSessionService() {
        return this.getInstanceOf(PlatformSessionService.class);
    }

    @Override
    public PlatformLoginService getPlatformLoginService() {
        return this.getInstanceOf(PlatformLoginService.class);
    }

    @Override
    public LoginService getLoginService() {
        return this.getInstanceOf(LoginService.class);
    }

    public TenantMonitoringService getTenantMonitoringService(final boolean useCache) {
        if (useCache) {
            return this.getInstanceOf("monitoringServiceWithCache", TenantMonitoringService.class);
        } else {
            return this.getInstanceOf("monitoringService", TenantMonitoringService.class);
        }
    }

    @Override
    public PlatformMonitoringService getPlatformMonitoringService() {
        return this.getInstanceOf(PlatformMonitoringService.class);
    }

    @Override
    public TechnicalLoggerService getTechnicalLoggerService() {
        return this.getInstanceOf(TechnicalLoggerService.class);
    }

    @Override
    public SPlatformBuilder getSPlatformBuilder() {
        return this.getInstanceOf(SPlatformBuilder.class);
    }

    @Override
    public STenantBuilder getSTenantBuilder() {
        return this.getInstanceOf(STenantBuilder.class);
    }

    @Override
    public TenantServiceAccessor getTenantServiceAccessor(final long tenantId) {
        return this;
    }

    @Override
    public TransactionExecutor getTransactionExecutor() {
        return this.getInstanceOf(TransactionExecutor.class);
    }

    @Override
    public BusinessLoggerService getBusinessLoggerService() {
        return this.getInstanceOf(BusinessLoggerService.class);
    }

    @Override
    public SBusinessLogModelBuilder getSBusinessLogModelBuilder() {
        return this.getInstanceOf(SBusinessLogModelBuilder.class);
    }

    @Override
    public ProcessInstanceService getProcessInstanceService() {
        return this.getInstanceOf(ProcessInstanceService.class);
    }

    @Override
    public ActivityInstanceService getActivityInstanceService() {
        return this.getInstanceOf(ActivityInstanceService.class);
    }

    @Override
    public BPMDefinitionBuilders getBPMDefinitionBuilders() {
        return this.getInstanceOf(BPMDefinitionBuilders.class);
    }

    @Override
    public BPMInstanceBuilders getBPMInstanceBuilders() {
        return this.getInstanceOf(BPMInstanceBuilders.class);
    }

    @Override
    public FlowNodeExecutor getFlowNodeExecutor() {
        return this.getInstanceOf(FlowNodeExecutor.class);
    }

    @Override
    public ProcessExecutor getProcessExecutor() {
        return this.getInstanceOf(ProcessExecutor.class);
    }

    @Override
    public FlowNodeStateManager getFlowNodeStateManager() {
        return this.getInstanceOf(FlowNodeStateManager.class);
    }

    @Override
    public TenantMonitoringService getTenantMonitoringService() {
        return this.getInstanceOf(TenantMonitoringService.class);
    }

    @Override
    public ActorMappingService getActorMappingService() {
        return this.getInstanceOf(ActorMappingService.class);
    }

    @Override
    public SActorBuilders getSActorBuilders() {
        return this.getInstanceOf(SActorBuilders.class);
    }

    @Override
    public SCategoryBuilderAccessor getCategoryModelBuilderAccessor() {
        return this.getInstanceOf(SCategoryBuilderAccessor.class);
    }

    @Override
    public CategoryService getCategoryService() {
        return this.getInstanceOf(CategoryService.class);
    }

    @Override
    protected String getDefaultPersistenceType() {
        return BONITA_PERSISTENCE + "hibernate";
    }

    @Override
    public ProcessInstanceStateManager getProcessInstanceStateManager() {
        return this.getInstanceOf(ProcessInstanceStateManager.class);
    }

    @Override
    public SExpressionBuilders getSExpressionBuilders() {
        return this.getInstanceOf(SExpressionBuilders.class);
    }

    public GatewayInstanceService getGatewayInstanceService() {
        return this.getInstanceOf(GatewayInstanceService.class);
    }

    @Override
    public TransitionService getTransitionInstanceService() {
        return this.getInstanceOf(TransitionService.class);
    }

    @Override
    public CommandService getCommandService() {
        return this.getInstanceOf(CommandService.class);
    }

    @Override
    public DocumentMappingService getDocumentMappingService() {
        return this.getInstanceOf(DocumentMappingService.class);
    }

    @Override
    public SDocumentMappingBuilderAccessor getDocumentMappingBuilderAccessor() {
        return this.getInstanceOf(SDocumentMappingBuilderAccessor.class);
    }

    @Override
    public ProcessDocumentService getProcessDocumentService() {
        return this.getInstanceOf(ProcessDocumentService.class);
    }

    @Override
    public SProcessDocumentBuilder getProcessDocumentBuilder() {
        return this.getInstanceOf(SProcessDocumentBuilder.class);
    }

    @Override
    public SCommandBuilderAccessor getSCommandBuilderAccessor() {
        return this.getInstanceOf(SCommandBuilderAccessor.class);
    }

    @Override
    public EventInstanceService getEventInstanceService() {
        return this.getInstanceOf(EventInstanceService.class);
    }

    @Override
    public DependencyBuilderAccessor getDependencyBuilderAccessor() {
        return this.getInstanceOf("platformDependencyBuilderAccessor", DependencyBuilderAccessor.class);
    }

    @Override
    public long getTenantId() {
        return 0;
    }

    @Override
    public ConnectorService getConnectorService() {
        return this.getInstanceOf(ConnectorService.class);
    }

    @Override
    public ProfileService getProfileService() {
        return this.getInstanceOf(ProfileService.class);
    }

    @Override
    public SProfileBuilderAccessor getSProfileBuilderAccessor() {
        return getInstanceOf(SProfileBuilderAccessor.class);
    }

    @Override
    public PlatformCommandService getPlatformCommandService() {
        return this.getInstanceOf(PlatformCommandService.class);
    }

    @Override
    public SPlatformCommandBuilderAccessor getSPlatformCommandBuilderAccessor() {
        return this.getInstanceOf(SPlatformCommandBuilderAccessor.class);
    }

    @Override
    public SDataDefinitionBuilders getSDataDefinitionBuilders() {
        return this.getInstanceOf(SDataDefinitionBuilders.class);
    }

    @Override
    public SDataSourceModelBuilder getSDataSourceModelBuilder() {
        return this.getInstanceOf(SDataSourceModelBuilder.class);
    }

    @Override
    public ParserFactory getParserFactgory() {
        return this.getInstanceOf(ParserFactory.class);
    }

    @Override
    public Parser getActorMappingParser() {
        return this.getInstanceOf(Parser.class);
    }

    @Override
    public ExpressionResolverService getExpressionResolverService() {
        return this.getInstanceOf(ExpressionResolverService.class);
    }

    @Override
    public XMLWriter getXMLWriter() {
        return this.getInstanceOf(XMLWriter.class);
    }

    @Override
    public SupervisorMappingService getSupervisorService() {
        return this.getInstanceOf(SupervisorMappingService.class);
    }

    @Override
    public SSupervisorBuilders getSSupervisorBuilders() {
        return this.getInstanceOf(SSupervisorBuilders.class);
    }

    @Override
    public OperationService getOperationService() {
        return this.getInstanceOf(OperationService.class);
    }

    @Override
    public SOperationBuilders getSOperationBuilders() {
        return this.getInstanceOf(SOperationBuilders.class);
    }

    @Override
    public ActorPrivilegeService getActorPrivilegeService() {
        return this.getInstanceOf(ActorPrivilegeService.class);
    }

    @Override
    public ActorPrivilegeBuilders getActorPrivilegeBuilders() {
        return this.getInstanceOf(ActorPrivilegeBuilders.class);
    }

    @Override
    public PrivilegeService getPrivilegeService() {
        return this.getInstanceOf(PrivilegeService.class);
    }

    @Override
    public PrivilegeBuilders getPrivilegeBuilders() {
        return this.getInstanceOf(PrivilegeBuilders.class);
    }

    @Override
    public UserFilterService getUserFilterService() {
        return this.getInstanceOf("userFilterService", UserFilterService.class);
    }

    @Override
    public SearchEntitiesDescriptor getSearchEntitiesDescriptor() {
        return this.getInstanceOf(SearchEntitiesDescriptor.class);
    }

    @Override
    public SCommentService getCommentService() {
        return this.getInstanceOf(SCommentService.class);
    }

    @Override
    public SCommentBuilders getSCommentBuilders() {
        return this.getInstanceOf(SCommentBuilders.class);
    }

    @Override
    public ContainerRegistry getContainerRegistry() {
        return this.getInstanceOf(ContainerRegistry.class);
    }

    @Override
    public ExternalIdentityMappingService getExternalIdentityMappingService() {
        return this.getInstanceOf(ExternalIdentityMappingService.class);
    }

    @Override
    public SExternalIdentityMappingBuilders getExternalIdentityMappingBuilders() {
        return this.getInstanceOf(SExternalIdentityMappingBuilders.class);
    }

    @Override
    public BreakpointService getBreakpointService() {
        return this.getInstanceOf(BreakpointService.class);
    }

    @Override
    public SearchPlatformEntitiesDescriptor getSearchPlatformEntitiesDescriptor() {
        return this.getInstanceOf(SearchPlatformEntitiesDescriptor.class);
    }

    @Override
    public LockService getLockService() {
        return this.getInstanceOf(LockService.class);
    }

    @Override
    public MigrationPlanService getMigrationPlanService() {
        return this.getInstanceOf(MigrationPlanService.class);
    }

    @Override
    public Parser getProfileParser() {
        return this.getInstanceOf(Parser.class);
    }

    @Override
    public EventsHandler getEventsHandler() {
        return this.getInstanceOf(EventsHandler.class);
    }

    @Override
    public void initializeServiceAccessor(final ClassLoader classLoader) {
    }

    @Override
    public NodeConfiguration getPlaformConfiguration() {
        return this.getInstanceOf(NodeConfiguration.class);
    }

    @Override
    public ParameterService getParameterService() {
        return this.getInstanceOf(ParameterService.class);
    }

}
