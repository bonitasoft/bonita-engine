/**
 * Copyright (C) 2017 Bonitasoft S.A.
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

package org.bonitasoft.engine.core.form.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.page.AuthorizationRuleConstants.IS_MANAGER_OF_USER_INVOLVED_IN_PROCESS_INSTANCE;

import java.util.List;

import org.junit.Test;

/**
 * @author Emmanuel Duchastenier
 */
public class ManagerInvolvedAuthorizationRuleMappingImplTest {

    @Test
    public void should_allow_manager_of_user_involved() throws Exception {
        // given:
        final ManagerInvolvedAuthorizationRuleMappingImpl ruleMapping = new ManagerInvolvedAuthorizationRuleMappingImpl();

        // when:
        final List<String> rules = ruleMapping.getProcessOverviewRuleKeys();

        // then:
        assertThat(rules).contains(IS_MANAGER_OF_USER_INVOLVED_IN_PROCESS_INSTANCE);
    }
}
