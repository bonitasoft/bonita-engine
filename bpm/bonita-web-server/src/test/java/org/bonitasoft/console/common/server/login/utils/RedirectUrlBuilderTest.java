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
package org.bonitasoft.console.common.server.login.utils;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * @author Vincent Elcrin
 */
public class RedirectUrlBuilderTest {

    @Test
    public void testWeCanBuildHashTaggedParamAreKept() throws Exception {
        final RedirectUrlBuilder redirectUrlBuilder = new RedirectUrlBuilder(
                "myredirecturl?parambeforehash=true#hashparam=true");
        final String url = redirectUrlBuilder.build().getUrl();

        assertEquals("myredirecturl?parambeforehash=true#hashparam=true", url);
    }

    @Test
    public void testPostParamsAreNotAddedToTheUrl() {
        final Map<String, String[]> parameters = new HashMap<>();
        parameters.put("postParam", someValues("true"));

        final RedirectUrlBuilder redirectUrlBuilder = new RedirectUrlBuilder(
                "myredirecturl?someparam=value#hashparam=true");
        final String url = redirectUrlBuilder.build().getUrl();

        assertEquals("myredirecturl?someparam=value#hashparam=true", url);
    }

    private String[] someValues(final String... values) {
        return values;
    }
}
