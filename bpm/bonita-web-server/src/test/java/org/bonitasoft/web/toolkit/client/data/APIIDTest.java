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
package org.bonitasoft.web.toolkit.client.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemIdMalformedException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Dumitru Corini
 */
public class APIIDTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void makeAPIID_with_list_should_return_correct_result() throws Exception {
        List<String> ids = new ArrayList<>();
        ids.add("1");
        APIID result = APIID.makeAPIID(ids);
        assertThat(result.toLong()).isEqualTo(1L);

        ids = new ArrayList<>();
        ids.add("1");
        ids.add("6");
        ids.add("-1");
        result = APIID.makeAPIID(ids);
        assertThat(result.getIds()).isEqualTo(ids);

        ids = new ArrayList<>();
        ids.add("");
        ids.add("1");
        ids.add("-1");
        ids.add("instantiation");
        ids.add(null);
        result = APIID.makeAPIID(ids);
        assertThat(result.getIds()).isEqualTo(ids);
    }

    @Test
    public void makeAPIID_with_array_should_return_correct_result() throws Exception {
        Long[] ids = new Long[] { 1L };
        APIID result = APIID.makeAPIID(ids);
        assertThat(result.toLong()).isEqualTo(1L);

        ids = new Long[] { 1L, null, 6L, -1L };
        List<String> resultingAPIIDs = new ArrayList<>();
        resultingAPIIDs.add("1");
        resultingAPIIDs.add(null);
        resultingAPIIDs.add("6");
        resultingAPIIDs.add("-1");
        result = APIID.makeAPIID(ids);
        assertThat(result.getIds()).isEqualTo(resultingAPIIDs);
    }

    @Test(expected = APIItemIdMalformedException.class)
    public void toLong_should_throw_APIItemIdMalformedException_with_string() {
        APIID.makeAPIID("undefined").toLong();
    }
}
