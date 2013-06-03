package org.bonitasoft.engine.filter.user;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.filter.AbstractUserFilter;
import org.bonitasoft.engine.filter.UserFilterException;

/**
 * @author Baptiste Mesta
 */
public class TestFilterUsingActorName extends AbstractUserFilter {

    @Override
    public List<Long> filter(final String actorName) throws UserFilterException {
        @SuppressWarnings("unchecked")
        final Map<String, Long> map = (Map<String, Long>) getInputParameter("userIds");
        return Arrays.asList(map.get(actorName));
    }

    @Override
    public void validateInputParameters() throws ConnectorValidationException {

    }

    @Override
    public boolean shouldAutoAssignTaskIfSingleResult() {
        return false;
    }

}
