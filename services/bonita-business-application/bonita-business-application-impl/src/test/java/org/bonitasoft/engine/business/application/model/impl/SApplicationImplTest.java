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

package org.bonitasoft.engine.business.application.model.impl;

import static org.bonitasoft.engine.business.application.model.impl.SApplicationImplAssert.assertThat;

import org.bonitasoft.engine.business.application.model.SApplicationState;
import org.junit.Test;

public class SApplicationImplTest {

    @Test
    public void constructor_and_setter_are_ok() {
        //given
        long creationDate = System.currentTimeMillis();
        String state = SApplicationState.ACTIVATED.name();
        long createdBy = 1L;
        long homePageId = 5L;
        long profileId = 10L;
        long updatedBy = 2L;
        long layoutId = 20L;
        long themeId = 21L;

        //when
        SApplicationImpl application = new SApplicationImpl("token", "Name to display", "1.0", creationDate, createdBy, state, layoutId, themeId);
        application.setDescription("This is my application");
        application.setHomePageId(homePageId);
        application.setIconPath("/icon.jpg");
        application.setLastUpdateDate(creationDate + 1);
        application.setProfileId(profileId);
        application.setUpdatedBy(updatedBy);

        //then
        assertThat(application).hasToken("token").hasDisplayName("Name to display").hasVersion("1.0").hasCreationDate(creationDate).hasCreatedBy(createdBy)
                .hasState(state).hasDescription("This is my application").hasHomePageId(homePageId).hasIconPath("/icon.jpg")
                .hasLastUpdateDate(creationDate + 1).hasProfileId(profileId).hasUpdatedBy(updatedBy).hasLayoutId(layoutId).hasThemeId(themeId);
    }

}
