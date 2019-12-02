/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.tracking;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
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
        return getName() + ": \n" + "active: " + isActive();
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
        notifyStartTracking();
    }

    @Override
    public void deactivate() {
        this.active = false;
        notifyStopTracking();
    }

    protected void log(final TechnicalLogSeverity severity, final String message) {
        if (this.logger.isLoggable(getClass(), severity)) {
            this.logger.log(getClass(), severity, message);
        }
    }
}
