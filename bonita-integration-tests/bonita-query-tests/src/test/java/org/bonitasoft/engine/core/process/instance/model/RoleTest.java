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
package org.bonitasoft.engine.core.process.instance.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.test.persistence.builder.RoleBuilder.aRole;

import org.bonitasoft.engine.test.persistence.repository.RoleRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Danila Mazour
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class RoleTest {

    @Autowired
    private RoleRepository repository;

    @Test
    public void should_get_role_by_name() {
        repository.add(aRole().forTenant(1L).forRoleId(10L).forRoleName("MyRole10").build());
        repository.add(aRole().forTenant(1L).forRoleId(11L).forRoleName("MyRole11").build());

        assertThat(repository.getRoleByName("MyRole10").getId()).isEqualTo(10L);
        assertThat(repository.getRoleByName("MyRole11").getId()).isEqualTo(11L);

        assertThat(repository.getRoleByName("NonExistingRole")).isNull();

    }

}
