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
package org.bonitasoft.web.rest.server.datastore.profile.member;

import static junit.framework.Assert.assertTrue;
import static org.bonitasoft.web.rest.model.builder.profile.member.EngineProfileMemberBuilder.anEngineProfileMember;
import static org.bonitasoft.web.rest.model.builder.profile.member.ProfileMemberItemBuilder.aProfileMemberItem;

import org.bonitasoft.engine.profile.ProfileMember;
import org.bonitasoft.web.rest.model.portal.profile.ProfileMemberItem;
import org.bonitasoft.web.rest.server.APITestWithMock;
import org.junit.Test;

/**
 * @author Vincent Elcrin
 */
public class ProfileMemberItemConverterTest extends APITestWithMock {

    @Test
    public void testProfileMemberItemConversion() {
        ProfileMemberItemConverter converter = new ProfileMemberItemConverter();
        ProfileMember profileMember = anEngineProfileMember().build();

        ProfileMemberItem item = converter.convert(profileMember);

        assertTrue(areEquals(aProfileMemberItem().from(profileMember).build(), item));
    }
}
