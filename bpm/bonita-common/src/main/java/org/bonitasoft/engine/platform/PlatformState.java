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

/**
 * Describes the possible states of the Platform.
 */
public enum PlatformState {

    /**
     * The Platform is started:
     * - Services are running
     * - Tenants that are Activated are STARTED or are STARTING
     * - Calls can be made on platform APIs
     */
    STARTED,

    /**
     * The Platform is transitioning from state STOPPED to state STARTED
     * - Tenants are still STOPPED ( will be started once the Platform is STARTED)
     * - No calls can be made on APIs
     */
    STARTING,

    /**
     * The Platform is stopped:
     * - Services are stopped
     * - All Tenants are also STOPPED
     * - No call to APIs can be made (except to login and start platform)
     */
    STOPPED,

    /**
     * The Platform is transitioning from state STARTED to state STOPPED
     * - Tenants are STOPPED or STOPPING
     * - No calls can be made on APIs
     */
    STOPPING,
}
