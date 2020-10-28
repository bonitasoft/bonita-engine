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
package org.bonitasoft.engine.tenant;

import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnSingleCandidate(TenantElementsRestartSupervisor.class)
public class TenantElementsRestartSupervisorLocal implements TenantLifecycleService, TenantElementsRestartSupervisor {

    private boolean areTenantsElementsAlreadyRestarted;

    @Override
    public void start() throws SBonitaException {
    }

    @Override
    public void stop() {
        areTenantsElementsAlreadyRestarted = false;
    }

    @Override
    public void pause() {
        areTenantsElementsAlreadyRestarted = false;
    }

    @Override
    public void resume() {
    }

    @Override
    public boolean shouldRestartElements() {
        return !areTenantsElementsAlreadyRestarted;
    }

    @Override
    public boolean willRestartElements() {
        if (!areTenantsElementsAlreadyRestarted) {
            areTenantsElementsAlreadyRestarted = true;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isResponsibleForRecovery() {
        return true;
    }
}
