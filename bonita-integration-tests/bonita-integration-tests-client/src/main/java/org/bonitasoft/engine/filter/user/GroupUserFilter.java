/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
