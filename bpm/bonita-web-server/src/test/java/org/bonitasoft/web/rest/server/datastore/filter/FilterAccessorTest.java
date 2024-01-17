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
package org.bonitasoft.web.rest.server.datastore.filter;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.bonitasoft.web.rest.server.datastore.profile.member.MemberTypeConverter;
import org.bonitasoft.web.rest.server.framework.exception.APIFilterMandatoryException;
import org.junit.Test;

/**
 * @author Vincent Elcrin
 */
public class FilterAccessorTest {

    @Test
    public void testWeCanRetrieveMandatoryValue() throws Exception {
        FilterAccessor filterAccess = new FilterAccessor(Collections.singletonMap("key", "value"));

        String value = filterAccess.getMandatory("key");

        assertEquals("value", value);
    }

    @Test(expected = APIFilterMandatoryException.class)
    public void testAccessToMandatoryValueWhichItDoesntExitThrowException() {
        FilterAccessor filterAccess = new FilterAccessor(Collections.<String, String> emptyMap());

        filterAccess.getMandatory("key");
    }

    @Test(expected = APIFilterMandatoryException.class)
    public void testAccessToMandatoryValueNotConvertibleThrowException() {
        FilterAccessor filterAccess = new FilterAccessor(Collections.singletonMap("key", "value"));

        filterAccess.getMandatory("key", new MemberTypeConverter());
    }
}
