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

package org.bonitasoft.engine.business.application.impl;

import static org.bonitasoft.engine.business.application.impl.ApplicationImplAssert.assertThat;

import java.util.Date;

import org.bonitasoft.engine.business.application.ApplicationState;
import org.junit.Test;

public class ApplicationImplTest {

    @Test
    public void setters_and_getters_are_ok() throws Exception {
        //given
        String token = "hr";
        String version = "1.0";
        String description = "hr description";
        long layoutId = 4L;
        long themeId = 5L;
        long updatedBy = 100L;
        long profileId = 20L;
        String iconPath = "icon.jpg";
        long createdBy = 91L;
        Date creationDate = new Date();
        String displayName = "Human resources";
        long homePageId = 30L;
        Date lastUpdateDate = new Date(creationDate.getTime() + 1000);
        String state = ApplicationState.ACTIVATED.name();

        //when
        ApplicationImpl application = new ApplicationImpl(token, version, description, layoutId, themeId);
        application.setUpdatedBy(updatedBy);
        application.setProfileId(profileId);
        application.setIconPath(iconPath);
        application.setCreatedBy(createdBy);
        application.setCreationDate(creationDate);
        application.setDisplayName(displayName);
        application.setHomePageId(homePageId);
        application.setLastUpdateDate(lastUpdateDate);
        application.setState(state);

        //then
        assertThat(application).hasToken(token).hasVersion(version).hasDescription(description).hasLayoutId(layoutId).hasThemeId(themeId)
                .hasUpdatedBy(updatedBy).hasProfileId(profileId).hasIconPath(iconPath).hasCreatedBy(createdBy).hasCreationDate(creationDate)
                .hasDisplayName(displayName).hasHomePageId(homePageId).hasLastUpdateDate(lastUpdateDate).hasState(state);
    }
}
