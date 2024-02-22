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

import static junit.framework.Assert.assertEquals;

import org.bonitasoft.web.rest.model.portal.profile.AbstractMemberItem;
import org.junit.Test;

/**
 * @author Vincent Elcrin
 */
public class MemberTypeTest {

    @Test
    public void testWeCanRetrieveMemberTypeFromAString() throws Exception {
        MemberType type = MemberType.from(AbstractMemberItem.VALUE_MEMBER_TYPE_USER);

        assertEquals(MemberType.USER, type);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotExistingConstConversionThrowsException() throws Exception {
        MemberType type = MemberType.from("notExistingConst");

        assertEquals(MemberType.USER, type);
    }
}
