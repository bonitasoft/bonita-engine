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
