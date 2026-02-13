/**
 * Copyright (C) 2026 Bonitasoft S.A.
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

import javax.servlet.http.HttpSession;

import org.bonitasoft.engine.bpm.flownode.SendEventException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.web.rest.server.api.AbstractRESTController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/API/bpm/signal")
public class BPMSignalController extends AbstractRESTController {

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void broadcast(@RequestBody BPMSignal signal, HttpSession httpSession)
            throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException, SendEventException {
        if (signal.name() == null) {
            throw new IllegalArgumentException("'name' attribute is mandatory");
        }
        getProcessAPI(httpSession).sendSignal(signal.name());
    }
}
