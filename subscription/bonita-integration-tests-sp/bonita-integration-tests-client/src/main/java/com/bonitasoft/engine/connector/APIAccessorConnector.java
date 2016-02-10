/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.connector;

import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.search.SearchOptionsBuilder;

import com.bonitasoft.engine.api.LogAPI;
import com.bonitasoft.engine.log.LogCriterion;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class APIAccessorConnector extends AbstractConnector {

    @Override
    public void validateInputParameters() {
    }

    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        long numberOfUsers;
        try {
            numberOfUsers = getAPIAccessor().getIdentityAPI().getNumberOfUsers();
            setOutputParameter("numberOfUsers", numberOfUsers);

            setOutputParameter("procInstId", getExecutionContext().getProcessInstanceId());

            final LogAPI logAPI = getAPIAccessor().getLogAPI();
            setOutputParameter("nbLogs", logAPI.getNumberOfLogs());
            setOutputParameter("searchLogs", logAPI.searchLogs(new SearchOptionsBuilder(0, 10).done()));
            setOutputParameter("getLogs", logAPI.getLogs(0, 10, LogCriterion.SEVERITY_LEVEL_DESC));

            final Profile profile = getAPIAccessor().getProfileAPI().createProfile("addProfileCommandFromConnector",
                    "test of call to a command through getAPIAccessor from a Connector implementation", "");
            setOutputParameter("profile", profile);

            getAPIAccessor().getProcessAPI().getNumberOfCategories();
        } catch (final BonitaException e) {
            throw new ConnectorException(e);
        }
    }
}
