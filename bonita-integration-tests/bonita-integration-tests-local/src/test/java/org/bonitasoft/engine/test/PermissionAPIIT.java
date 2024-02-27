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
package org.bonitasoft.engine.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Map;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.api.permission.APICallContext;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class PermissionAPIIT extends TestWithUser {

    @Test
    public void should_allow_with_provided_dynamic_rule() throws Exception {

        APICallContext apiCallContext = new APICallContext("GET", "bpm", "process", null, "", "body");
        //when
        boolean isAllowedWithoutFilter = getPermissionAPI()
                .isAuthorized(apiCallContext);

        //then
        assertThat(isAllowedWithoutFilter).isFalse();

        //given
        apiCallContext = getApiCallContextWithUserFilter(getSession().getUserId());

        //when
        boolean isAllowedWithCurrentUserFilter = getPermissionAPI()
                .isAuthorized(apiCallContext);

        //then
        assertThat(isAllowedWithCurrentUserFilter).isTrue();

        //given
        apiCallContext = getApiCallContextWithUserFilter(99999L);

        //when
        boolean isAllowedWithOtherUserFilter = getPermissionAPI()
                .isAuthorized(apiCallContext);

        //then
        assertThat(isAllowedWithOtherUserFilter).isFalse();
    }

    private static APICallContext getApiCallContextWithUserFilter(long userId) {
        return new APICallContext("GET", "bpm", "process", null, "", "body") {

            @Override
            public Map<String, String> getFilters() {
                return Collections.singletonMap("user_id", String.valueOf(userId));
            }
        };
    }
}
