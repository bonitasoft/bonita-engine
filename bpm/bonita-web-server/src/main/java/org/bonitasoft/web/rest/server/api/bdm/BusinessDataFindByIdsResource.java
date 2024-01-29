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
package org.bonitasoft.web.rest.server.api.bdm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.command.CommandExecutionException;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.web.rest.server.api.resource.CommonResource;
import org.restlet.resource.Get;

/**
 * @author Matthieu Chaffotte
 */
public class BusinessDataFindByIdsResource extends CommonResource {

    private final CommandAPI commandAPI;

    public BusinessDataFindByIdsResource(final CommandAPI commandAPI) {
        this.commandAPI = commandAPI;
    }

    @Get("json")
    public String getBusinessData()
            throws CommandNotFoundException, CommandExecutionException, CommandParameterizationException {
        final Map<String, Serializable> parameters = new HashMap<>();
        parameters.put("entityClassName", getPathParam("className"));
        parameters.put("businessDataIds", (Serializable) getParameterAsLongList("ids"));
        parameters.put("businessDataURIPattern", BusinessDataFieldValue.URI_PATTERN);
        return (String) commandAPI.execute("getBusinessDataByIds", parameters);
    }

}
