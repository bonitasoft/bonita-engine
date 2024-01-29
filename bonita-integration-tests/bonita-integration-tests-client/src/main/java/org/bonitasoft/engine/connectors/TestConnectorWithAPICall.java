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
package org.bonitasoft.engine.connectors;

import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.connector.AbstractConnector;

/**
 * @author Charles Souillard
 */
public class TestConnectorWithAPICall extends AbstractConnector {

    @Override
    public void validateInputParameters() {
    }

    @Override
    protected void executeBusinessLogic() {
        final String name = (String) getInputParameter("processName");
        final String version = (String) getInputParameter("processVersion");
        long processId = -1;
        try {
            processId = getAPIAccessor().getProcessAPI().getProcessDefinitionId(name, version);
        } catch (ProcessDefinitionNotFoundException e) {
            throw new RuntimeException("Unable to get Process with name and version: " + name + ", " + version);
        } finally {
            setOutputParameter("processId", processId);
        }

    }

}
