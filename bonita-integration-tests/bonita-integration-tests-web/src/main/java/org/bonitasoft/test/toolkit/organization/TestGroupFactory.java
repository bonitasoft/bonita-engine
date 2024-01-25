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

import org.bonitasoft.engine.identity.GroupCreator;

/**
 * @author Séverin Moussel
 */
public class TestGroupFactory {

    private static final String NAME_R_AND_D = "RetD";

    private static final String DESCRIPTION_R_AND_D = "The team that drinks all the coffee";

    private static final String NAME_WEB = "Web";

    private static final String DESCRIPTION_WEB = "The team that also drinks all the beer";

    private final Map<String, TestGroup> groupList;

    private static TestGroupFactory instance;

    /**
     * Default Constructor.
     */
    public TestGroupFactory() {
        this.groupList = new HashMap<>();
    }

    public static TestGroupFactory getInstance() {
        if (instance == null) {
            instance = new TestGroupFactory();
        }
        return instance;
    }

    public void clear() throws Exception {
        for (TestGroup testGroup : this.groupList.values()) {
            testGroup.delete();
        }
        this.groupList.clear();
    }

    /**
     * @return the userList
     */
    private Map<String, TestGroup> getGroupList() {
        return this.groupList;
    }

    public static List<TestGroup> createRandomGroups(final int nbOfGroups) {
        final List<TestGroup> results = new ArrayList<>();

        for (int i = 0; i < nbOfGroups; i++) {
            results.add(createGroup(getRandomString(), getRandomString()));
        }

        return results;
    }

    private static String getRandomString() {
        return String.valueOf(new Random().nextLong());
    }

    // ////////////////////////////////////////////////////////////////////////////////
    // / Group's creation
    // ////////////////////////////////////////////////////////////////////////////////

    /**
     * @param name
     * @param description
     * @return
     */
    public static TestGroup createGroup(final String name, final String description) {
        if (getInstance().getGroupList().get(name) == null) {
            final GroupCreator groupBuilder = new GroupCreator(name).setDescription(description);

            getInstance().getGroupList().put(name, new TestGroup(groupBuilder));
        }

        return getInstance().getGroupList().get(name);
    }

    public static TestGroup getRAndD() {
        return createGroup(NAME_R_AND_D, DESCRIPTION_R_AND_D);
    }

    public static TestGroup getWeb() {
        return createGroup(NAME_WEB, DESCRIPTION_WEB);
    }

    public void check() {
        if (!getGroupList().isEmpty()) {
            throw new RuntimeException(
                    this.getClass().getName() + " cannot be reset because the list is not empty: " + getGroupList());
        }
    }
}
