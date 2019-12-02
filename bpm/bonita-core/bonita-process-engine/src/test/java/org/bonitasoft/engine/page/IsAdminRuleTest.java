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
package org.bonitasoft.engine.page;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class IsAdminRuleTest {

    IsAdminRule isAdminRule = new IsAdminRule();

    @Test
    public void isAllowed_should_return_true_if_admin_in_context() throws Exception {
        Map<String, Serializable> context = new HashMap<String, Serializable>();
        context.put(AuthorizationRuleConstants.IS_ADMIN, true);

        assertThat(isAdminRule.isAllowed("key", context)).isTrue();
    }

    @Test
    public void isAllowed_should_return_false_if_not_admin_in_context() throws Exception {
        Map<String, Serializable> context = new HashMap<String, Serializable>();
        context.put(AuthorizationRuleConstants.IS_ADMIN, false);

        assertThat(isAdminRule.isAllowed("key", context)).isFalse();
    }

    @Test
    public void isAllowed_should_return_false_if_empty_context() throws Exception {
        Map<String, Serializable> context = new HashMap<String, Serializable>();

        assertThat(isAdminRule.isAllowed("key", context)).isFalse();
    }

    @Test
    public void getIdShouldReturnIsAdmin() throws Exception {
        assertThat(isAdminRule.getId()).isEqualTo("IS_ADMIN");
    }
}
