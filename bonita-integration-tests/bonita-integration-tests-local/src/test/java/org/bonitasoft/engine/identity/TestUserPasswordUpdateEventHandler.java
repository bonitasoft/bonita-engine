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
package org.bonitasoft.engine.identity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.identity.model.SUser;

/**
 * @author Feng Hui
 */
public class TestUserPasswordUpdateEventHandler extends UserUpdateEventHandler {

    private static final long serialVersionUID = 1L;

    private static final String USER_UPDATED = "USER_UPDATED";

    // Set username as key, and new password as value.
    private final Map<String, String> userMap = new HashMap<String, String>();

    private final String identifier;

    public TestUserPasswordUpdateEventHandler() {
        identifier = UUID.randomUUID().toString();
    }

    @Override
    public void execute(final SUpdateEvent updateEvent) {
        final SUser newUser = (SUser) updateEvent.getObject();
        userMap.put(newUser.getUserName(), newUser.getPassword());
    }

    @Override
    public boolean isInterested(final SUpdateEvent updateEvent) {
        if (USER_UPDATED.compareToIgnoreCase(updateEvent.getType()) == 0) {
            final SUser newUser = (SUser) updateEvent.getObject();
            final SUser oldUser = (SUser) updateEvent.getOldObject();

            return !newUser.getPassword().equals(oldUser.getPassword());
        }
        return false;
    }

    @Override
    public String getPassword(final String userName) {
        return userMap.get(userName);
    }

    @Override
    public void cleanUserMap() {
        userMap.clear();
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

}
