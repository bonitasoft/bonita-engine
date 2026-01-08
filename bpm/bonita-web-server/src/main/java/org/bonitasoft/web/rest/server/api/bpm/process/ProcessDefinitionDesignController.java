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
package org.bonitasoft.web.rest.server.api.bpm.process;

import static org.bonitasoft.web.rest.server.api.AbstractRESTController.API_SPRING_INTERNAL;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.web.rest.server.api.AbstractRESTController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/" + API_SPRING_INTERNAL + "/bpm/process/{processDefinitionId}/design")
public class ProcessDefinitionDesignController extends AbstractRESTController {

    final static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String getDesign(@PathVariable Long processDefinitionId, HttpSession httpSession)
            throws ProcessDefinitionNotFoundException, IOException, BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException {
        final DesignProcessDefinition design = getProcessAPI(httpSession)
                .getDesignProcessDefinition(processDefinitionId);
        return replaceLongIdToString(objectMapper.writeValueAsString(design));
    }

    /**
     * Replace numeric id values with string values in JSON to avoid precision loss in JavaScript
     *
     * @param design the JSON string containing the design
     * @return the JSON string with numeric ids replaced with string ids
     */
    protected String replaceLongIdToString(final String design) {
        return design.replaceAll("([^\\\\]\"id\"\\s*:\\s*)(\\d+)", "$1\"$2\"");
    }
}
