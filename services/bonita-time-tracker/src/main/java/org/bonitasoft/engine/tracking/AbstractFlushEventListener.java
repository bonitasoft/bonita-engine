package org.bonitasoft.engine.tracking;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Charles Souillard
 */
public abstract class AbstractFlushEventListener implements FlushEventListener {

    private boolean active = false;
    protected final TechnicalLoggerService logger;

    protected AbstractFlushEventListener(final boolean activateByDefault, final TechnicalLoggerService logger) {
        this.active = activateByDefault;
        this.logger = logger;
    }

    @Override
    public String getStatus() {
        String status = getName() + ": \n" + "active: " + isActive();
        return status;
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public void activate() {
        this.active = true;
    }

    @Override
    public void deactivate() {
        this.active = false;
        notifyStopTracking();
    }
}
