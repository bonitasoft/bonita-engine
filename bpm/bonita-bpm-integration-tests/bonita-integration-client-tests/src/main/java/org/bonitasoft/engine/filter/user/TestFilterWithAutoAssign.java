package org.bonitasoft.engine.filter.user;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.filter.AbstractUserFilter;
import org.bonitasoft.engine.filter.UserFilterException;

/**
 * @author Baptiste Mesta
 */
public class TestFilterWithAutoAssign extends AbstractUserFilter {

    @Override
    public List<Long> filter(final String actorName) throws UserFilterException {
        return Collections.singletonList((Long) getInputParameter("userId"));
    }

    @Override
    public void validateInputParameters() throws ConnectorValidationException {

    }

    @Override
    public boolean shouldAutoAssignTaskIfSingleResult() {
        return true;
    }

}
