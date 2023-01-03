/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.engine.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Spring listener example of the event {@link PlatformStartedEvent}.
 *
 * @deprecated class used to ensure the proper publishing of this event and should be removed as soon as an official
 *             implementation is made.
 */
@Component
@Slf4j
@Deprecated(forRemoval = true, since = "7.16.0")
public class PlatformStartedListener {

    @EventListener
    public void handlePlatformStarted(PlatformStartedEvent event) {
        log.info("Platform started event received: {}", event);
    }
}
