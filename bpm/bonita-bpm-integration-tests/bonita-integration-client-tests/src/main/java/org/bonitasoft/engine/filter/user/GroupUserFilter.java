package org.bonitasoft.engine.filter.user;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.filter.AbstractUserFilter;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserCriterion;

public class GroupUserFilter extends AbstractUserFilter {

    @Override
    public void validateInputParameters() {
        // Nothing to validate
    }

    @Override
    public List<Long> filter(final String actorName) {
        final Long groupId = (Long) getInputParameter("groupId");
        final List<User> users = getAPIAccessor().getIdentityAPI().getUsersInGroup(groupId, 0, 10, UserCriterion.USER_NAME_DESC);
        final List<Long> userIds = new ArrayList<Long>();
        for (final User user : users) {
            userIds.add(user.getId());
        }
        return userIds;
    }

}
