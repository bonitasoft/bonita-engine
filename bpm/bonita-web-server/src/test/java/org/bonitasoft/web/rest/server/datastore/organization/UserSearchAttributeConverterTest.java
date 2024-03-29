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
package org.bonitasoft.web.rest.server.datastore.organization;

import static junit.framework.Assert.assertEquals;

import org.bonitasoft.engine.identity.UserSearchDescriptor;
import org.bonitasoft.web.rest.model.identity.UserItem;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Colin PUY
 */
public class UserSearchAttributeConverterTest {

    private UserSearchAttributeConverter converter;

    @Before
    public void initConverter() {
        converter = new UserSearchAttributeConverter();
    }

    @Test
    public void convertFistName() throws Exception {
        String convert = converter.convert(UserItem.ATTRIBUTE_FIRSTNAME);

        assertEquals(UserSearchDescriptor.FIRST_NAME, convert);
    }

    @Test
    public void convertLastName() throws Exception {
        String convert = converter.convert(UserItem.ATTRIBUTE_LASTNAME);

        assertEquals(UserSearchDescriptor.LAST_NAME, convert);
    }

    @Test
    public void convertUserName() throws Exception {
        String convert = converter.convert(UserItem.ATTRIBUTE_USERNAME);

        assertEquals(UserSearchDescriptor.USER_NAME, convert);
    }

    @Test
    public void convertGroupId() throws Exception {
        String convert = converter.convert(UserItem.FILTER_GROUP_ID);

        assertEquals(UserSearchDescriptor.GROUP_ID, convert);
    }

    @Test
    public void convertManagerId() throws Exception {
        String convert = converter.convert(UserItem.ATTRIBUTE_MANAGER_ID);

        assertEquals(UserSearchDescriptor.MANAGER_USER_ID, convert);
    }

    @Test
    public void convertRoleId() throws Exception {
        String convert = converter.convert(UserItem.FILTER_ROLE_ID);

        assertEquals(UserSearchDescriptor.ROLE_ID, convert);
    }
}
