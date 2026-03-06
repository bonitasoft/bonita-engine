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
package org.bonitasoft.web.rest.server.api.bpm.flownode;

import static org.bonitasoft.web.rest.server.APIPaginationUtils.buildContentRange;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerInstance;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.web.rest.server.api.AbstractRESTController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring MVC Controller for Timer Event Triggers.
 *
 * @author Emmanuel Duchastenier
 */
@RestController
@RequestMapping("/API/bpm/timerEventTrigger")
public class TimerEventTriggerController extends AbstractRESTController {

    /**
     * Search timer event triggers for a given case.
     *
     * @param caseId the case ID to search timers for
     * @param page the page number (p parameter)
     * @param count the number of results per page (c parameter)
     * @param httpSession the HTTP session
     * @return the list of timer event triggers with Content-Range header
     * @throws BonitaException if an error occurs
     */
    @GetMapping
    public ResponseEntity<List<TimerEventTriggerInstance>> searchTimerEventTriggers(
            @RequestParam(value = "caseId") long caseId,
            @RequestParam(value = "p", defaultValue = "0") int page,
            @RequestParam(value = "c", defaultValue = "10") int count,
            HttpSession httpSession) throws BonitaException {

        final SearchResult<TimerEventTriggerInstance> searchResult = getProcessAPI(httpSession)
                .searchTimerEventTriggerInstances(caseId, new SearchOptionsBuilder(page * count, count).done());

        final List<TimerEventTriggerInstance> results = searchResult.getResult();

        // Return 204 No Content for empty results
        if (results.isEmpty()) {
            return ResponseEntity.noContent()
                    .header(HttpHeaders.CONTENT_RANGE, buildContentRange(0, 0, 0))
                    .build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE,
                        buildContentRange(page, results.size(), searchResult.getCount()))
                .body(results);
    }

    /**
     * Update the execution date of a timer event trigger.
     *
     * @param id the timer event trigger instance ID
     * @param trigger the timer event trigger data containing the new execution date
     * @param httpSession the HTTP session
     * @return the updated timer event trigger
     * @throws BonitaException if an error occurs
     */
    @PutMapping("/{id}")
    public TimerEventTrigger updateTimerEventTrigger(@PathVariable long id, @RequestBody TimerEventTrigger trigger,
            HttpSession httpSession) throws BonitaException {
        final Date executionDate = new Date(trigger.executionDate());
        final Date updatedDate = getProcessAPI(httpSession).updateExecutionDateOfTimerEventTriggerInstance(id,
                executionDate);
        return new TimerEventTrigger(updatedDate.getTime());
    }
}
