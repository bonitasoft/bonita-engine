/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.connector;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.LogAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.ConnectorException;
import org.bonitasoft.engine.exception.ConnectorValidationException;
import org.bonitasoft.engine.log.LogCriterion;
import org.bonitasoft.engine.search.SearchOptionsBuilder;

/**
 * @author Matthieu Chaffotte
 */
public class APIAccessorConnector extends AbstractConnector {

    @Override
    public void validateInputParameters() throws ConnectorValidationException {
    }

    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        long numberOfUsers;
        try {
            numberOfUsers = getAPIAccessor().getIdentityAPI().getNumberOfUsers();
            setOutputParameter("numberOfUsers", numberOfUsers);

            setOutputParameter("procInstId", getExecutionContext().getParentProcessInstanceId());

            final LogAPI logAPI = getAPIAccessor().getLogAPI();
            final int numberOfLogs = logAPI.getNumberOfLogs();
            setOutputParameter("nbLogs", numberOfLogs);
            setOutputParameter("searchLogs", logAPI.searchLogs(new SearchOptionsBuilder(0, numberOfLogs).done()));
            setOutputParameter("getLogs", logAPI.getLogs(0, numberOfLogs, LogCriterion.SEVERITY_LEVEL_DESC));

            final CommandAPI commandAPI = getAPIAccessor().getCommandAPI();
            final Map<String, Serializable> commandParams = new HashMap<String, Serializable>(2);
            commandParams.put("name", "addProfileCommandFromConnector");
            commandParams.put("description", "test of call to a command through getAPIAccessor from a Connector implementation");
            final Serializable profileAttributeMap = commandAPI.execute(commandAPI.get("addProfile").getId(), commandParams);
            setOutputParameter("profileAttributeMap", profileAttributeMap);

            getAPIAccessor().getProcessAPI().getNumberOfCategories();
            getAPIAccessor().getMonitoringAPI().getNumberOfExecutingProcesses();

        } catch (final BonitaException e) {
            throw new ConnectorException(e);
        }
    }

}
