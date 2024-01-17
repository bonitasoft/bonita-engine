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
package org.bonitasoft.console.common.server.utils;

import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;

/**
 * @author Vincent Elcrin
 */
public class ProcessAccessor {

    private final ProcessAPI processAPI;

    public ProcessAccessor(ProcessAPI processAPI) {
        this.processAPI = processAPI;
    }

    public Map<String, byte[]> getResources(long processDefinitionId, String fileNamesPattern) {
        return processAPI.getProcessResources(processDefinitionId, fileNamesPattern);
    }

    public ProcessDefinition getDefinition(long processDefinitionId) throws ProcessDefinitionNotFoundException {
        return processAPI.getProcessDefinition(processDefinitionId);
    }

}
