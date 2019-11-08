package org.bonitasoft.engine.tenant;

import static org.bonitasoft.engine.api.impl.transaction.SetServiceState.ServiceAction.*;

import org.bonitasoft.engine.api.impl.TenantConfiguration;
import org.bonitasoft.engine.api.impl.transaction.SetServiceState;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.service.RunnableWithException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Handles the lifecycle of tenant services: start, stop, (pause, resume -> will be removed)
 * Does not handle state of the tenant in database
 */
@Component
public class TenantServicesManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenantServicesManager.class);

    public enum TenantServiceState {
        STOPPED, STARTING, STARTED, STOPPING
    }
    private SessionAccessor sessionAccessor;

    private SessionService sessionService;

    private TransactionService transactionService;

    private ClassLoaderService classLoaderService;
    private TenantConfiguration tenantConfiguration;
    private Long tenantId;
    private TenantElementsRestarter tenantElementsRestarter;
    private TenantServiceState tenantServiceState = TenantServiceState.STOPPED;
    public TenantServicesManager(SessionAccessor sessionAccessor, SessionService sessionService,
            TransactionService transactionService, ClassLoaderService classLoaderService,
            TenantConfiguration tenantConfiguration, @Value("${tenantId}") Long tenantId, TenantElementsRestarter tenantElementsRestarter) {
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
        this.transactionService = transactionService;
        this.classLoaderService = classLoaderService;
        this.tenantConfiguration = tenantConfiguration;
        this.tenantId = tenantId;
        this.tenantElementsRestarter = tenantElementsRestarter;
    }

    public boolean isStarted() {
        return tenantServiceState == TenantServiceState.STARTED;
    }

    public void start() throws Exception {
        doStart(START);
    }

    public void resume() throws Exception {
        doStart(RESUME);
    }

    public void stop() throws Exception {
        // stop the tenant services:
        doStop(STOP);
    }

    public void pause() throws Exception {
        doStop(PAUSE);
    }

    private void doStart(SetServiceState.ServiceAction serviceAction) throws Exception {
        LOGGER.debug("Starting services of tenant {}", tenantId);
        if (tenantServiceState != TenantServiceState.STOPPED) {
            LOGGER.debug("Tenant services cannot be started, they are {}", tenantServiceState);
            return;
        }
        tenantServiceState = TenantServiceState.STARTING;
        inTenantSession(() -> {
            tenantElementsRestarter.prepareRestartOfElements();
            changeServiceState(serviceAction);
            tenantElementsRestarter.restartElements();
        });
        //FIXME handle state on exception
        tenantServiceState = TenantServiceState.STARTED;
        LOGGER.debug("Services of tenant {} are started.", tenantId);
    }

    private void doStop(SetServiceState.ServiceAction serviceAction) throws Exception {
        LOGGER.debug("Stopping services of tenant {}", tenantId);
        if (tenantServiceState != TenantServiceState.STARTED) {
            LOGGER.debug("Tenant services cannot be stopped, they are {}", tenantServiceState);
            return;
        }
        tenantServiceState = TenantServiceState.STOPPING;
        changeServiceState(serviceAction);
        tenantServiceState = TenantServiceState.STOPPED;
        LOGGER.debug("Services of tenant {} are stopped.", tenantId);
    }

    private void changeServiceState(SetServiceState.ServiceAction serviceAction) throws Exception {
        transactionService.executeInTransaction(() -> new SetServiceState(tenantId, serviceAction)
                .changeServiceState(classLoaderService, tenantConfiguration));
    }

    protected Long createSession(final long tenantId, final SessionService sessionService) throws SBonitaException {
        return sessionService.createSession(tenantId, SessionService.SYSTEM).getId();
    }

    private void inTenantSession(RunnableWithException runnable) throws Exception {
        if (sessionAccessor.isTenantSession()) {
            runnable.run();
        } else { // is a platform session: create a tenant session to run that
            long currentSessionId = sessionAccessor.getSessionId();
            try {
                final long sessionId = createSession(tenantId, sessionService);
                sessionAccessor.deleteSessionId();
                sessionAccessor.setSessionInfo(sessionId, tenantId);
                runnable.run();
                sessionService.deleteSession(sessionId);
            } finally {
                sessionAccessor.setSessionInfo(currentSessionId, -1);
            }
        }
    }
}
