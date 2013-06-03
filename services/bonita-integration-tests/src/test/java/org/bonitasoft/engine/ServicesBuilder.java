package org.bonitasoft.engine;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.authentication.AuthenticationService;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.command.CommandService;
import org.bonitasoft.engine.command.model.SCommandBuilderAccessor;
import org.bonitasoft.engine.data.DataService;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilders;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilders;
import org.bonitasoft.engine.data.model.builder.SDataSourceBuilder;
import org.bonitasoft.engine.data.model.builder.SDataSourceParameterBuilder;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.builder.DependencyBuilder;
import org.bonitasoft.engine.dependency.model.builder.DependencyBuilderAccessor;
import org.bonitasoft.engine.dependency.model.builder.DependencyMappingBuilder;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.exceptions.ExceptionsManager;
import org.bonitasoft.engine.expression.ExpressionService;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.builder.IdentityModelBuilder;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.monitoring.PlatformMonitoringService;
import org.bonitasoft.engine.monitoring.TenantMonitoringService;
import org.bonitasoft.engine.monitoring.mbean.SJvmMXBean;
import org.bonitasoft.engine.monitoring.mbean.SPlatformServiceMXBean;
import org.bonitasoft.engine.monitoring.mbean.SServiceMXBean;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.authentication.PlatformAuthenticationService;
import org.bonitasoft.engine.platform.command.PlatformCommandService;
import org.bonitasoft.engine.platform.command.model.SPlatformCommandBuilderAccessor;
import org.bonitasoft.engine.platform.model.builder.SPlatformBuilder;
import org.bonitasoft.engine.platform.model.builder.STenantBuilder;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.queriablelogger.model.builder.SQueriableLogModelBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.test.util.ServicesAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.work.WorkService;
import org.bonitasoft.engine.xml.Parser;

/**
 * You can set these system properties to change implementations
 * sysprop.bonita.db.vendor
 * change the DB:
 * h2 (default)
 * mysql
 * oracle
 * postgres
 * bonita.test.identity.service
 * change the identity service
 * bonita-identity-impl (default)
 * bonita.test.identity.model
 * change the identity model
 * bonita-identity-model-impl (default)
 * 
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 */
public class ServicesBuilder {

    ServicesAccessor accessor;

    public ServicesBuilder() {
        super();
        accessor = ServicesAccessor.getInstance();
    }

    public QueriableLoggerService buildQueriableLogger(final String name) {
        return accessor.getInstanceOf(name, QueriableLoggerService.class);
    }

    public QueriableLoggerService buildQueriableLogger() {
        return this.buildQueriableLogger("syncQueriableLoggerService"); // default is the sync one
    }

    public IdentityModelBuilder buildIdentityModelBuilder() {
        return accessor.getInstanceOf(IdentityModelBuilder.class);
    }

    public SDataInstanceBuilders builderSDataInstanceBuilder() {
        return accessor.getInstanceOf(SDataInstanceBuilders.class);
    }

    public SDataDefinitionBuilders builderSDataDefinitionBuilders() {
        return accessor.getInstanceOf(SDataDefinitionBuilders.class);
    }

    public DependencyBuilder buildDependencyModelBuilder() {
        return accessor.getInstanceOf(DependencyBuilder.class);
    }

    public DependencyMappingBuilder buildDependencyMappingModelBuilder() {
        return accessor.getInstanceOf(DependencyMappingBuilder.class);
    }

    public PersistenceService buildPersistence() {
        return this.buildPersistence("persistenceService");
    }

    public PersistenceService buildPersistence(final String name) {
        return accessor.getInstanceOf(name, PersistenceService.class);
    }

    public PersistenceService buildJournal() {
        return this.buildPersistence();
    }

    public PersistenceService buildHistory() {
        return this.buildPersistence("history");
    }

    public Recorder buildRecorder(final boolean sync) {
        String synchType = "recorderAsync";
        if (sync) {
            synchType = "recorderSync";
        }
        return accessor.getInstanceOf(synchType, Recorder.class);
    }

    public TransactionService buildTransactionService() {
        return accessor.getInstanceOf(TransactionService.class);
    }

    public PlatformService buildPlatformService() {
        return accessor.getInstanceOf(PlatformService.class);
    }

    public DataService buildDataService() {
        return accessor.getInstanceOf(DataService.class);
    }

    public SDataSourceParameterBuilder buildDataSourceParameterModelBuilder() {
        return accessor.getInstanceOf(SDataSourceParameterBuilder.class);
    }

    public SDataSourceBuilder buildDataSourceModelBuilder() {
        return accessor.getInstanceOf(SDataSourceBuilder.class);
    }

    public SPlatformBuilder buildPlatformBuilder() {
        return accessor.getInstanceOf(SPlatformBuilder.class);
    }

    public STenantBuilder buildTenantBuilder() {
        return accessor.getInstanceOf(STenantBuilder.class);
    }

    public SessionAccessor buildSessionAccessor() {
        return accessor.getInstanceOf(SessionAccessor.class);
    }

    public IdentityService buildIdentityService() {
        return accessor.getInstanceOf(IdentityService.class);
    }

    public ArchiveService buildArchiveService() {
        return accessor.getInstanceOf(ArchiveService.class);
    }

    public ExpressionService buildExpressionService() {
        return accessor.getInstanceOf(ExpressionService.class);
    }

    public DataInstanceService buildDataInstanceService() {
        return accessor.getInstanceOf(DataInstanceService.class);
    }

    public DependencyService buildDependencyService() {
        return accessor.getInstanceOf("dependencyService", DependencyService.class);
    }

    public DependencyService buildPlatformDependencyService() {
        return accessor.getInstanceOf("platformDependencyService", DependencyService.class);
    }

    public SQueriableLogModelBuilder buildQueriableLogModelBuilder() {
        return accessor.getInstanceOf(SQueriableLogModelBuilder.class);
    }

    public SchedulerService buildSchedulerService() {
        return accessor.getInstanceOf(SchedulerService.class);
    }

    public ClassLoaderService buildClassLoaderService() {
        return accessor.getInstanceOf(ClassLoaderService.class);
    }

    public SServiceMXBean getServiceMXBean() {
        return accessor.getInstanceOf(SServiceMXBean.class);
    }

    public SPlatformServiceMXBean getPlatformServiceMXBean() {
        return accessor.getInstanceOf(SPlatformServiceMXBean.class);
    }

    public SJvmMXBean getJvmMXBean() {
        return accessor.getInstanceOf(SJvmMXBean.class);
    }

    public EventService buildEventService() {
        return accessor.getInstanceOf(EventService.class);
    }

    public ExceptionsManager getExceptionsManager() {
        return accessor.getInstanceOf(ExceptionsManager.class);
    }

    public CacheService buildCacheService() {
        return accessor.getInstanceOf(CacheService.class);
    }

    public AuthenticationService buildAuthenticationService() {
        return accessor.getInstanceOf(AuthenticationService.class);
    }

    public PlatformAuthenticationService buildPlatformAuthenticationService() {
        return accessor.getInstanceOf(PlatformAuthenticationService.class);
    }

    public SessionService buildSessionService() {
        return accessor.getInstanceOf(SessionService.class);
    }

    public PlatformSessionService buildPlatformSessionService() {
        return accessor.getInstanceOf(PlatformSessionService.class);
    }

    public TenantMonitoringService buildTenantMonitoringService(final boolean useCache) {
        if (useCache) {
            return accessor.getInstanceOf("monitoringServiceWithCache", TenantMonitoringService.class);
        } else {
            return accessor.getInstanceOf("monitoringService", TenantMonitoringService.class);
        }
    }

    public PlatformMonitoringService buildPlatformMonitoringService() {
        return accessor.getInstanceOf(PlatformMonitoringService.class);
    }

    public TechnicalLoggerService buildTechnicalLoggerService() {
        return accessor.getInstanceOf(TechnicalLoggerService.class);
    }

    public CommandService buildCommandService() {
        return accessor.getInstanceOf(CommandService.class);
    }

    public SCommandBuilderAccessor buildSCommandBuilderAccessor() {
        return accessor.getInstanceOf(SCommandBuilderAccessor.class);
    }

    public DependencyBuilderAccessor buildPlatformDependencyBuilderAccessor() {
        return accessor.getInstanceOf("platformDependencyBuilderAccessor", DependencyBuilderAccessor.class);
    }

    public PlatformCommandService buildPlatformCommandService() {
        return accessor.getInstanceOf(PlatformCommandService.class);
    }

    public SPlatformCommandBuilderAccessor buildSPlatformCommandBuilderAccessor() {
        return accessor.getInstanceOf(SPlatformCommandBuilderAccessor.class);
    }

    public ProfileService buildProfileService() {
        return accessor.getInstanceOf(ProfileService.class);
    }

    public Parser getParser() {
        return accessor.getInstanceOf(Parser.class);
    }

    public WorkService buildWorkService() {
        return accessor.getInstanceOf(WorkService.class);
    }

    public <T> T getInstanceOf(final Class<T> class1) {
        return accessor.getInstanceOf(class1);
    }

}
