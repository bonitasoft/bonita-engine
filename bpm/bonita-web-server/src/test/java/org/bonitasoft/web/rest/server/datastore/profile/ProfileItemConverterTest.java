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
package org.bonitasoft.web.rest.server.datastore.profile;

import static junit.framework.Assert.assertTrue;
import static org.bonitasoft.web.rest.model.builder.profile.ProfileItemBuilder.aProfileItem;
import static org.bonitasoft.web.rest.server.datastore.profile.EngineProfileBuilder.anEngineProfile;

import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.web.rest.model.portal.profile.ProfileItem;
import org.bonitasoft.web.rest.server.APITestWithMock;
import org.junit.Test;

/**
 * @author Vincent Elcrin
 */
public class ProfileItemConverterTest extends APITestWithMock {

    @Test
    public void testProfileItemConvertion() {
        final Profile profile = anEngineProfile().withName("aName").withDescription("aDescription").build();

        final ProfileItem item = new ProfileItemConverter().convert(profile);

        assertTrue(areEquals(
                aProfileItem().fromEngineItem(profile).withIcon(ProfileItemConverter.DEFAULT_PROFILE_ICONPATH).build(),
                item));
    }

}
