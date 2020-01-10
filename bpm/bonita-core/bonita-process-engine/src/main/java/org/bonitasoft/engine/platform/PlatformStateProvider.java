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
package org.bonitasoft.engine.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PlatformStateProvider {

    private static Logger logger = LoggerFactory.getLogger(PlatformStateProvider.class);

    private PlatformState state = PlatformState.STOPPED;

    public PlatformStateProvider() {
    }

    PlatformStateProvider(PlatformState state) {
        this.state = state;
    }

    public PlatformState getState() {
        return state;
    }

    /**
     * Transition the Platform state to STARTED ( state put in STARTING )
     *
     * @return true only if the state was changed
     */
    boolean initializeStart() {
        if (state != PlatformState.STOPPED) {
            return false;
        }
        state = PlatformState.STARTING;
        return true;
    }

    void setStarted() {
        state = PlatformState.STARTED;
    }

    /**
     * Transition the Platform state to STOPPED ( state put in STOPPING )
     *
     * @return true only if the state was changed
     */
    boolean initializeStop() {
        if (state != PlatformState.STARTED) {
            return false;
        }
        state = PlatformState.STOPPING;
        return true;
    }

    void setStopped() {
        state = PlatformState.STOPPED;
    }

}
