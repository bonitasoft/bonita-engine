package org.bonitasoft.engine.test;

import java.util.List;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.builder.IdentityModelBuilder;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.builder.SPlatformBuilder;
import org.bonitasoft.engine.platform.model.builder.STenantBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.test.util.BaseServicesBuilder;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * You can set these system properties to change implementations
 * bonita.test.db.vendor
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
 * @author Elias Ricken de Medeiros
 */
public class ServicesBuilder extends BaseServicesBuilder {

    private static final String BONITA_TEST_PERSISTENCE_TEST = "bonita.test.persistence.test";

    public static final String BONITA_EVENT_IMPL_SP = "bos-events-api-impl";

    public static final String BONITA_ARCHIVE_TEST = "bonita-archive-test-model";

    @Override
    protected List<String> getResourceList() {
        System.setProperty(BaseServicesBuilder.BONITA_TEST_EVENT_SERVICE, BONITA_EVENT_IMPL_SP);
        final List<String> resourceList = super.getResourceList();
        final String property = System.getProperty(BONITA_TEST_PERSISTENCE, getDefaultPersistenceType());
        final String persistenceType = property.substring(BONITA_PERSISTENCE.length());
        resourceList.add(getServiceImplementationConfigurationFile("org/bonitasoft/engine/conf", BONITA_TEST_PERSISTENCE_TEST, "bonita-persistence-"
                + persistenceType + "-test"));
        resourceList.add(getPersistenceServiceConfigurationFileForModule("org/bonitasoft/engine/conf", BONITA_ARCHIVE_TEST, BONITA_ARCHIVE_TEST,
                persistenceType));
        return resourceList;
    }

    public IdentityModelBuilder buildIdentityModelBuilder() {
        return getInstanceOf(IdentityModelBuilder.class);
    }

    public PersistenceService buildPersistence() {
        return buildPersistence("persistenceService");
    }

    public PersistenceService buildPersistence(final String name) {
        return getInstanceOf(name, PersistenceService.class);
    }

    public PersistenceService buildJournal() {
        return buildPersistence();
    }

    public PersistenceService buildHistory() {
        return buildPersistence("history");
    }

    public Recorder buildRecorder(final boolean sync) {
        String synchType = "recorderAsync";
        if (sync) {
            synchType = "recorderSync";
        }
        return getInstanceOf(synchType, Recorder.class);
    }

    public TransactionService buildTransactionService() {
        return getInstanceOf(TransactionService.class);
    }

    public PlatformService buildPlatformService() {
        return getInstanceOf(PlatformService.class);
    }

    public SPlatformBuilder buildPlatformBuilder() {
        return getInstanceOf(SPlatformBuilder.class);
    }

    public STenantBuilder buildTenantBuilder() {
        return getInstanceOf(STenantBuilder.class);
    }

    public SessionAccessor buildSessionAccessor() {
        return getInstanceOf(SessionAccessor.class);
    }

    public IdentityService buildIdentityService() {
        return getInstanceOf(IdentityService.class);
    }

    public EventService buildEventService() {
        return getInstanceOf(EventService.class);
    }

    public SessionService buildSessionService() {
        return getInstanceOf(SessionService.class);
    }

    public TechnicalLoggerService buildTechnicalLoggerService() {
        return getInstanceOf(TechnicalLoggerService.class);
    }

    public SchedulerService buildSchedulerService() {
        return getInstanceOf(SchedulerService.class);
    }

}
