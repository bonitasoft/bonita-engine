/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api.bpm.signal;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.SendEventException;
import org.bonitasoft.web.rest.server.api.resource.CommonResource;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.restlet.resource.Post;

/**
 * REST resource to broadcast a BPM signal.
 */
public class BPMSignalResource extends CommonResource {

    private final ProcessAPI processAPI;

    public BPMSignalResource(final ProcessAPI processAPI) {
        this.processAPI = processAPI;
    }

    @Post
    public void broadcast(BPMSignal signal) {
        if (signal == null) {
            throw new IllegalArgumentException("Signal is mandatory");
        }
        String signalName = signal.getName();
        if (signalName == null) {
            throw new IllegalArgumentException("Signal name is mandatory");
        }
        try {
            processAPI.sendSignal(signalName);
        } catch (final SendEventException e) {
            throw new APIException(e);
        }
    }

}
