/**
 * Copyright (C) 2022 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.test.toolkit.organization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bonitasoft.engine.identity.RoleCreator;

/**
 * @author Séverin Moussel
 */
public class TestRoleFactory {

    private static final String NAME_MANAGER = "Manager";
    private static final String DESCRIPTION_MANAGER = "Team manager";
    private static final String NAME_DEVELOPER = "Developer";
    private static final String DESCRIPTION_DEVELOPER = "The man who write code and drink a lot of coffee";

    private final Map<String, TestRole> roleList;
    private static TestRoleFactory instance;

    private TestRoleFactory() {
        this.roleList = new HashMap<>();
    }

    public static TestRoleFactory getInstance() {
        if (instance == null) {
            instance = new TestRoleFactory();
        }
        return instance;
    }

    public void clear() {
        for (TestRole testRole : this.roleList.values()) {
            testRole.delete();
        }
        this.roleList.clear();
    }

    /**
     * @return the userList
     */
    private Map<String, TestRole> getRoleList() {
        return this.roleList;
    }

    public List<TestRole> createRandomRoles(final int nbOfRoles) {
        final List<TestRole> results = new ArrayList<>();

        for (int i = 0; i < nbOfRoles; i++) {
            final String name = getRandomString();
            RoleCreator creator = new RoleCreator(name).setDescription(getRandomString());
            final TestRole testRole = new TestRole(creator);
            results.add(testRole);
            getInstance().getRoleList().put(name, testRole);
        }

        return results;
    }

    private static String getRandomString() {
        return String.valueOf(new Random().nextLong());
    }

    // ////////////////////////////////////////////////////////////////////////////////
    // / Role's creation
    // ////////////////////////////////////////////////////////////////////////////////

    public static TestRole createRole(final String name, final String description) {
        if (getInstance().getRoleList().get(name) == null) {
            RoleCreator creator = new RoleCreator(name).setDescription(description);
            getInstance().getRoleList().put(name, new TestRole(creator));
        }

        return getInstance().getRoleList().get(name);
    }

    public static TestRole getManager() {
        return createRole(NAME_MANAGER, DESCRIPTION_MANAGER);
    }

    public static TestRole getDeveloper() {
        return createRole(NAME_DEVELOPER, DESCRIPTION_DEVELOPER);
    }

    public void check() {
        if (!getRoleList().isEmpty()) {
            throw new RuntimeException(
                    this.getClass().getName() + " cannot be reset because the list is not empty: " + getRoleList());
        }
    }
}
