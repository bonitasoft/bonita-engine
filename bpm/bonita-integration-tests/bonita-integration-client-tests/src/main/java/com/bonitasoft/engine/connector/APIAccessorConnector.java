/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.connector;

import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.connector.ConnectorException;
import org.bonitasoft.engine.exception.connector.ConnectorValidationException;
import org.bonitasoft.engine.profile.model.Profile;
import org.bonitasoft.engine.search.SearchOptionsBuilder;

import com.bonitasoft.engine.api.LogAPI;
import com.bonitasoft.engine.log.LogCriterion;

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

            final Profile profile = getAPIAccessor().getProfileAPI().createProfile("addProfileCommandFromConnector",
                    "test of call to a command through getAPIAccessor from a Connector implementation", "");
            setOutputParameter("profile", profile);

            getAPIAccessor().getProcessAPI().getNumberOfCategories();
        } catch (final BonitaException e) {
            throw new ConnectorException(e);
        }
    }
}
