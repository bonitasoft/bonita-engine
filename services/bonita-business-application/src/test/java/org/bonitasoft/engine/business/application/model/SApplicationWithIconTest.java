/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.business.application.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SApplicationWithIconTest {

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
        SApplicationWithIcon application = new SApplicationWithIcon("token", "Name to display", "1.0",
                creationDate, createdBy, state);
        application.setDescription("This is my application");
        application.setLayoutId(layoutId);
        application.setThemeId(themeId);
        application.setHomePageId(homePageId);
        application.setIconPath("/icon.jpg");
        application.setLastUpdateDate(creationDate + 1);
        application.setProfileId(profileId);
        application.setUpdatedBy(updatedBy);

        //then
        assertThat(application.getToken()).isEqualTo("token");
        assertThat(application.getDisplayName()).isEqualTo("Name to display");
        assertThat(application.getVersion()).isEqualTo("1.0");
        assertThat(application.getCreationDate()).isEqualTo(creationDate);
        assertThat(application.getCreatedBy()).isEqualTo(createdBy);
        assertThat(application.getState()).isEqualTo(state);
        assertThat(application.getDescription()).isEqualTo("This is my application");
        assertThat(application.getHomePageId()).isEqualTo(homePageId);
        assertThat(application.getIconPath()).isEqualTo("/icon.jpg");
        assertThat(application.getLastUpdateDate()).isEqualTo(creationDate + 1);
        assertThat(application.getProfileId()).isEqualTo(profileId);
        assertThat(application.getUpdatedBy()).isEqualTo(updatedBy);
        assertThat(application.getLayoutId()).isEqualTo(layoutId);
        assertThat(application.getThemeId()).isEqualTo(themeId);;
    }

}
