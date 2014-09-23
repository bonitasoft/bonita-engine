package org.bonitasoft.engine.filter.user;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.filter.AbstractUserFilter;

/**
 * @author Baptiste Mesta
 */
public class TestFilterUsingActorName extends AbstractUserFilter {

    @Override
    public List<Long> filter(final String actorName) {
        @SuppressWarnings("unchecked")
        final Map<String, Long> map = (Map<String, Long>) getInputParameter("userIds");
        return Arrays.asList(map.get(actorName));
    }

    @Override
    public void validateInputParameters() {

    }

    @Override
    public boolean shouldAutoAssignTaskIfSingleResult() {
        return false;
    }

}
