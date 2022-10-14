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
package org.bonitasoft.web.rest.server.engineclient;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.process.ProcessActivationException;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessExecutionException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APINotFoundException;
import org.bonitasoft.web.toolkit.client.common.i18n.T_;
import org.bonitasoft.web.toolkit.client.common.texttemplate.Arg;

/**
 * @author Colin PUY
 * @author Elias Ricken de Medeiros
 */
// TODO migrate all engine methods relating to cases (i.e. especially those in CaseDatastore) in this class
public class CaseEngineClient {

    protected final ProcessAPI processAPI;

    public CaseEngineClient(final ProcessAPI processAPI) {
        this.processAPI = processAPI;
    }

    public ProcessInstance start(final long userId, final long processId) {
        return start(userId, processId, null);
    }

    public ProcessInstance start(final long userId, final long processId, final Map<String, Serializable> variables) {
        try {
            if (userId != -1L) {
                if (variables == null || variables.isEmpty()) {
                    return processAPI.startProcess(userId, processId);
                } else {
                    return processAPI.startProcess(userId, processId, variables);
                }
            } else {
                if (variables == null || variables.isEmpty()) {
                    return processAPI.startProcess(processId);
                } else {
                    return processAPI.startProcess(processId, variables);
                }
            }
        } catch (final ProcessDefinitionNotFoundException e) {
            throw new APINotFoundException(
                    new T_("Can't start process, process %processId% not found", new Arg("processId", processId)), e);
        } catch (final ProcessActivationException e) {
            throw new APIException(
                    new T_("Can't start process, process %processId% is not enabled", new Arg("processId", processId)),
                    e);
        } catch (final ProcessExecutionException e) {
            throw new APIException(
                    new T_("Error occured when starting process %processId%", new Arg("processId", processId)), e);
        } catch (final UserNotFoundException e) {
            throw new APIException(
                    new T_("Can't start process %processId%, user %userId% not found", new Arg("processId", processId),
                            new Arg("userId", userId)),
                    e);
        }
    }

    public long countOpenedCases() {
        final SearchOptions search = new SearchOptionsBuilder(0, 0).done();
        try {
            return processAPI.searchOpenProcessInstances(search).getCount();
        } catch (final Exception e) {
            throw new APIException("Error when counting opened cases", e);
        }
    }

}
