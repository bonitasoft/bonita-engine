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
package org.bonitasoft.web.rest.server.api.bpm.flownode;

import java.io.IOException;
import java.util.Date;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerInstance;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.web.rest.server.api.resource.CommonResource;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

/**
 * REST resource to operate on BPM Timer event triggers.
 *
 * @author Emmanuel Duchastenier
 */
public class TimerEventTriggerResource extends CommonResource {

    public static final String ID_PARAM_NAME = "id";

    private final ProcessAPI processAPI;

    public TimerEventTriggerResource(final ProcessAPI processAPI) {
        this.processAPI = processAPI;
        //Prevent Restlet from setting the status to 404
        //HTTP conditional headers are not supported when setting the entity manually in the response (fix BS-18149)
        setConditional(false);
    }

    @Get("json")
    public void searchTimerEventTriggers() {
        try {
            final Long caseId = getLongParameter("caseId", true);
            final SearchResult<TimerEventTriggerInstance> searchResult = processAPI
                    .searchTimerEventTriggerInstances(caseId, buildSearchOptions());
            Representation representation = getConverterService().toRepresentation(searchResult.getResult(),
                    MediaType.APPLICATION_JSON);
            representation.setCharacterSet(CharacterSet.UTF_8);
            getResponse().setEntity(representation);
            setContentRange(searchResult);
        } catch (final BonitaException | IOException e) {
            throw new APIException(e);
        }
    }

    @Put("json")
    public TimerEventTrigger updateTimerEventTrigger(final TimerEventTrigger trigger) throws Exception {
        final String triggerId = getAttribute(ID_PARAM_NAME);
        if (triggerId == null) {
            throw new APIException("Attribute '" + ID_PARAM_NAME + "' is mandatory");
        }
        final long timerEventTriggerInstanceId = Long.parseLong(triggerId);
        final Date executionDate = new Date(trigger.getExecutionDate());
        return createTimerEventTrigger(
                processAPI.updateExecutionDateOfTimerEventTriggerInstance(timerEventTriggerInstanceId, executionDate)
                        .getTime());
    }

    /**
     * Builds the TimerEventTrigger to return to the REST call.
     *
     * @param executionDate the execution date for this TimerEventTrigger.
     * @return the new constructed object.
     */
    private TimerEventTrigger createTimerEventTrigger(final long executionDate) {
        return new TimerEventTrigger(executionDate);
    }

    @Override
    public String getAttribute(final String attributeName) {
        return super.getAttribute(attributeName);
    }

}
