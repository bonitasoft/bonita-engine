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
package org.bonitasoft.web.rest.server.api.bpm.process;

import java.io.IOException;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.web.rest.server.api.resource.CommonResource;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.restlet.data.CharacterSet;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.resource.Get;

/**
 * @author Nicolas Tith
 */
public class ProcessDefinitionDesignResource extends CommonResource {

    private static final String PROCESS_DEFINITION_ID = "processDefinitionId";

    private final ProcessAPI processAPI;

    public ProcessDefinitionDesignResource(final ProcessAPI processAPI) {
        this.processAPI = processAPI;
    }

    @Get("json")
    public String getDesign() throws ProcessDefinitionNotFoundException, IOException {
        final DesignProcessDefinition design = processAPI.getDesignProcessDefinition(getProcessDefinitionIdParameter());
        final JacksonRepresentation<DesignProcessDefinition> jacksonRepresentation = new JacksonRepresentation<>(
                design);
        jacksonRepresentation.getObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        jacksonRepresentation.setCharacterSet(CharacterSet.UTF_8);
        return replaceLongIdToString(jacksonRepresentation.getText());
    }

    /**
     * @param design
     * @return
     * @throws IOException
     */
    protected String replaceLongIdToString(final String design) throws IOException {
        return design.replaceAll("([^\\\\]\"id\"\\s*:\\s*)(\\d+)", "$1\"$2\"");
    }

    protected long getProcessDefinitionIdParameter() {
        final String processDefinitionId = getAttribute(PROCESS_DEFINITION_ID);
        if (processDefinitionId == null) {
            throw new APIException("Attribute '" + PROCESS_DEFINITION_ID + "' is mandatory");
        }
        return Long.parseLong(processDefinitionId);
    }
}
