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

import org.bonitasoft.engine.profile.ProfileMemberSearchDescriptor;
import org.bonitasoft.web.rest.model.portal.profile.ProfileMemberItem;
import org.junit.Test;

/**
 * @author Vincent Elcrin
 */
public class ProfileMemberSearchDescriptorConverterTest {

    @Test
    public void testWeCanConvertProfileMemberId() {
        assertTrue(testConversion(ProfileMemberSearchDescriptor.ID,
                ProfileMemberItem.ATTRIBUTE_ID));
    }

    @Test
    public void testWeCanConvertProfileId() {
        assertTrue(testConversion(ProfileMemberSearchDescriptor.PROFILE_ID,
                ProfileMemberItem.ATTRIBUTE_PROFILE_ID));
    }

    @Test
    public void testWeCanConvertUserId() {
        assertTrue(testConversion(ProfileMemberSearchDescriptor.USER_ID,
                ProfileMemberItem.ATTRIBUTE_USER_ID));
    }

    @Test
    public void testWeCanConvertGroupId() {
        assertTrue(testConversion(ProfileMemberSearchDescriptor.GROUP_ID,
                ProfileMemberItem.ATTRIBUTE_GROUP_ID));
    }

    @Test
    public void testWeCanConvertRoleId() {
        assertTrue(testConversion(ProfileMemberSearchDescriptor.ROLE_ID,
                ProfileMemberItem.ATTRIBUTE_ROLE_ID));
    }

    public boolean testConversion(String expected, String actual) {
        ProfileMemberSearchDescriptorConverter converter = new ProfileMemberSearchDescriptorConverter();

        String descriptor = converter.convert(actual);

        return expected.equals(descriptor);
    }
}
