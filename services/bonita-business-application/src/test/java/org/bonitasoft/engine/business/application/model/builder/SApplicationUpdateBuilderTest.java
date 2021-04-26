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
package org.bonitasoft.engine.business.application.model.builder;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.bonitasoft.engine.business.application.model.AbstractSApplication;
import org.bonitasoft.engine.business.application.model.SApplicationState;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Test;

public class SApplicationUpdateBuilderTest {

    @Test
    public void constructorShouldUpdateUpdatedByAndLastUpdateDateFields() throws Exception {
        //given
        long homePageId = 4L;
        long updaterUserId = 17L;
        long profileId = 20L;
        long layoutId = 25L;
        long themeId = 26L;

        //when
        SApplicationUpdateBuilder builder = new SApplicationUpdateBuilder(updaterUserId);
        builder.updateDescription("new desc");
        builder.updateHomePageId(homePageId);
        builder.updateDisplayName("new display name");
        builder.updateIconPath("/icon.jpg");
        builder.updateProfileId(profileId);
        builder.updateState(SApplicationState.DEACTIVATED.name());
        builder.updateToken("newToken");
        builder.updateVersion("2.0");
        builder.updateLayoutId(layoutId);
        builder.updateThemeId(themeId);

        //then
        final EntityUpdateDescriptor desc = builder.done();
        final Map<String, Object> fields = desc.getFields();
        assertThat(fields).hasSize(12);
        assertThat(fields.get(AbstractSApplication.UPDATED_BY)).isEqualTo(updaterUserId);
        assertThat(fields.get(AbstractSApplication.LAST_UPDATE_DATE)).isNotNull();
        assertThat(fields.get(AbstractSApplication.DESCRIPTION)).isEqualTo("new desc");
        assertThat(fields.get(AbstractSApplication.HOME_PAGE_ID)).isEqualTo(homePageId);
        assertThat(fields.get(AbstractSApplication.DISPLAY_NAME)).isEqualTo("new display name");
        assertThat(fields.get(AbstractSApplication.ICON_PATH)).isEqualTo("/icon.jpg");
        assertThat(fields.get(AbstractSApplication.PROFILE_ID)).isEqualTo(profileId);
        assertThat(fields.get(AbstractSApplication.STATE)).isEqualTo(SApplicationState.DEACTIVATED.name());
        assertThat(fields.get(AbstractSApplication.TOKEN)).isEqualTo("newToken");
        assertThat(fields.get(AbstractSApplication.VERSION)).isEqualTo("2.0");
        assertThat(fields.get(AbstractSApplication.LAYOUT_ID)).isEqualTo(layoutId);
        assertThat(fields.get(AbstractSApplication.THEME_ID)).isEqualTo(themeId);
    }
}
