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
package org.bonitasoft.console.common.server.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.console.common.server.auth.impl.standard.StandardAuthenticationManagerImpl;
import org.junit.Test;

/**
 * @author Rohart Bastien
 * @author Emmanuel Duchastenier
 */
public class AuthenticationManagerFactoryTest {

    @Test
    public void testGetLoginManager() throws AuthenticationManagerNotFoundException {
        assertThat(AuthenticationManagerFactory.getAuthenticationManager()).as("Cannot get the login manager")
                .isNotNull();
    }

    @Test
    public void default_manager_implementation_should_be_StandardAuthenticationManagerImpl_class()
            throws AuthenticationManagerNotFoundException {
        // when:
        AuthenticationManager managerImpl = AuthenticationManagerFactory.getAuthenticationManager();

        // then:
        assertThat(managerImpl).isInstanceOf(StandardAuthenticationManagerImpl.class);
    }
}
