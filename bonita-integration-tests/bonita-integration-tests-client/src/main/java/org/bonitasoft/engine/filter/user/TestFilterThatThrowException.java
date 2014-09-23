package org.bonitasoft.engine.filter.user;

import java.util.List;

import org.bonitasoft.engine.filter.AbstractUserFilter;
import org.bonitasoft.engine.filter.UserFilterException;

/**
 * @author Baptiste Mesta
 */
public class TestFilterThatThrowException extends AbstractUserFilter {

    public TestFilterThatThrowException() {
    }

    @Override
    public List<Long> filter(final String actorName) throws UserFilterException {
        if (getInputParameter("exception").equals("runtime")) {
            throw new RuntimeException("unexpected");
        }
        throw new UserFilterException("unexpected");
    }

    @Override
    public void validateInputParameters() {

    }

    @Override
    public boolean shouldAutoAssignTaskIfSingleResult() {
        return false;
    }

}
