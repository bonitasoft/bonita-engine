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
package org.bonitasoft.engine.tenant.restart;

import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.execution.work.RestartException;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public interface TenantRestartHandler {

    /**
     * Called in a transaction during {@link PlatformAPI#startNode()}.
     */
    void beforeServicesStart() throws RestartException;

    /**
     * Called outside a transaction after {@link PlatformAPI#startNode()} in a separate thread from the api call.
     */
    void afterServicesStart();

}
