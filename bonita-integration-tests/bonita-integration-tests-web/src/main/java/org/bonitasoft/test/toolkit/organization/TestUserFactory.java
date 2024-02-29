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

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.identity.ContactDataCreator;
import org.bonitasoft.engine.identity.UserCreator;

/**
 * @author Vincent Elcrin
 */
public class TestUserFactory {

    private static final String JOHN_CARPENTER_LOGIN = "john.carpenter";

    private static final String TEAM_MANAGER_LOGIN = "team.manager";

    private static final String RIDLEY_SCOTT_LOGIN = "ridley.scott";

    private static final String MR_SPECHAR_LOGIN = "#*Ã©Ã Ã¢Ã¤Ã«ÃªÃ©~Ã§ÃžÅ¡Å’Ã˜Ã�Ã†";

    private final Map<String, TestUser> userList;

    private static TestUserFactory instance;

    /**
     * Default Constructor.
     */
    public TestUserFactory() {
        this.userList = new HashMap<>();
    }

    public static TestUserFactory getInstance() {
        if (instance == null) {
            instance = new TestUserFactory();
        }
        return instance;
    }

    public void clear() {
        this.userList.clear();
    }

    /**
     * @return the userList
     */
    public Map<String, TestUser> getUserList() {
        return userList;
    }

    // ////////////////////////////////////////////////////////////////////////////////
    // / User's creation
    // ////////////////////////////////////////////////////////////////////////////////

    public static TestUser getJohnCarpenter() {
        if (getInstance().getUserList().get(JOHN_CARPENTER_LOGIN) == null) {
            UserCreator creator = new UserCreator(JOHN_CARPENTER_LOGIN, "bpm")
                    .setFirstName("John")
                    .setLastName("Carpenter")
                    .setJobTitle("Director");
            getInstance().getUserList().put(JOHN_CARPENTER_LOGIN,
                    new TestUser(TestToolkitCtx.getInstance().getAdminUser().logIn(), creator));
            TestToolkitCtx.getInstance().getAdminUser().logOut();
        }

        return getInstance().getUserList().get(JOHN_CARPENTER_LOGIN);
    }

    public static TestUser getTeamManagerUser() {
        if (getInstance().getUserList().get(TEAM_MANAGER_LOGIN) == null) {
            UserCreator creator = new UserCreator(TEAM_MANAGER_LOGIN, "bpm")
                    .setFirstName("Team")
                    .setLastName("Manager")
                    .setJobTitle("Team Manager");
            getInstance().getUserList().put(TEAM_MANAGER_LOGIN,
                    new TestUser(TestToolkitCtx.getInstance().getAdminUser().logIn(), creator));
            TestToolkitCtx.getInstance().getAdminUser().logOut();
        }

        return getInstance().getUserList().get(TEAM_MANAGER_LOGIN);
    }

    public static Map<String, TestUser> getManagedUsers(int nbManagedUsers) {
        for (int i = 0; i < nbManagedUsers; i++) {
            String firstName = "managed";
            String lastName = "user" + i;
            UserCreator creator = new UserCreator(firstName + "." + lastName, "bpm")
                    .setFirstName(firstName)
                    .setLastName(lastName)
                    .setManagerUserId(getTeamManagerUser().getId());
            getInstance().getUserList().put(firstName + "." + lastName,
                    new TestUser(TestToolkitCtx.getInstance().getAdminUser().logIn(), creator));
            TestToolkitCtx.getInstance().getAdminUser().logOut();
        }

        return getInstance().getUserList();
    }

    public static TestUser getRidleyScott() {
        if (getInstance().getUserList().get(RIDLEY_SCOTT_LOGIN) == null) {
            final UserCreator userCreator = new UserCreator(RIDLEY_SCOTT_LOGIN, "bpm")
                    .setFirstName("Ridley")
                    .setLastName("Scott")
                    .setJobTitle("Director");

            final ContactDataCreator personalContactData = new ContactDataCreator()
                    .setAddress("address")
                    .setBuilding("building")
                    .setCity("city")
                    .setCountry("country")
                    .setEmail("ridley.scott@gmail.com")
                    .setFaxNumber("123456789")
                    .setMobileNumber("9876543214")
                    .setRoom("1408")
                    .setState("51")
                    .setWebsite("http://www.prometheus-movie.com")
                    .setZipCode("BT7 1GU");

            final ContactDataCreator professionalContactData = new ContactDataCreator()
                    .setAddress("address pro")
                    .setBuilding("building pro")
                    .setCity("city pro")
                    .setCountry("country pro")
                    .setEmail("ridley.scott.pro@gmail.com")
                    .setFaxNumber("0123456789")
                    .setMobileNumber("98765432140")
                    .setRoom("1408-b")
                    .setState("42")
                    .setWebsite("http://www.imdb.com/title/tt0078748")
                    .setZipCode("38000");

            getInstance().getUserList().put(RIDLEY_SCOTT_LOGIN,
                    new TestUser(TestToolkitCtx.getInstance().getAdminUser().logIn(), userCreator, personalContactData,
                            professionalContactData));
            TestToolkitCtx.getInstance().getAdminUser().logOut();
        }

        return getInstance().getUserList().get(RIDLEY_SCOTT_LOGIN);
    }

    public static TestUser getMrSpechar() {
        if (getInstance().getUserList().get(MR_SPECHAR_LOGIN) == null) {
            final UserCreator userBuilder = new UserCreator(MR_SPECHAR_LOGIN, "Ã«ÃªÃ©~Ã")
                    .setFirstName("#*Ã©Ã Ã¢Ã¤Ã«ÃªÃ©~Ã§ÃžÅ¡Å’Ã˜Ã�Ã†")
                    .setLastName("Ã Ã¢Ã¤Ã«")
                    .setJobTitle("©Ã Ã¢Ã¤Ã«Ãª");
            getInstance().getUserList().put(MR_SPECHAR_LOGIN,
                    new TestUser(TestToolkitCtx.getInstance().getAdminUser().logIn(), userBuilder));
            TestToolkitCtx.getInstance().getAdminUser().logOut();
        }

        return getInstance().getUserList().get(MR_SPECHAR_LOGIN);
    }

    public void check() {
        if (!getUserList().isEmpty()) {
            throw new RuntimeException(
                    this.getClass().getName() + " cannot be reset because the list is not empty: " + getUserList());
        }
    }
}
