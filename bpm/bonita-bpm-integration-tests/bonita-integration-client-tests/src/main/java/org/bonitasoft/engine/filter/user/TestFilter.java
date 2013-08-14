package org.bonitasoft.engine.filter.user;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.filter.AbstractUserFilter;

/**
 * @author Baptiste Mesta
 */
public class TestFilter extends AbstractUserFilter {

    @Override
    public List<Long> filter(final String actorName) {
        return Collections.singletonList((Long) getInputParameter("userId"));
    }

    @Override
    public void validateInputParameters() {

    }

    @Override
    public boolean shouldAutoAssignTaskIfSingleResult() {
        return false;
    }

}
