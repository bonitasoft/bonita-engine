/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.engine.mdc;

import java.util.Map;

import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserCreator;
import org.bonitasoft.engine.identity.UserCreator.UserField;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

public class APICallLogIT extends CommonAPIIT {

    private User bill;

    @Before
    public void beforeTest() throws BonitaException {
        loginWithTechnicalUser();
        bill = createUser(USERNAME, "bpm");
    }

    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Test
    public void apiCallAndInspectLogs() throws Exception {
        // given
        loginWithTechnicalUser();

        logout();
        // when
        loginOnDefaultTenantWith(USERNAME, "bpm");
        systemOutRule.clearLog();
        try {
            var creator = new UserCreator("johny", "bpm");
            // using the deprecated method should log a warning
            creator.getFields().put(UserField.ICON_NAME, "test.png");
            getIdentityAPI().createUser(creator);
            // then and error is logged with user id in context
            LogITUtil.checkLogEntryContains(systemOutRule.getLog(),
                    Map.of(MDCConstants.USER_ID, Long.toString(bill.getId())));
        } finally {
            getIdentityAPI().deleteUser("johny");
        }
    }

}
