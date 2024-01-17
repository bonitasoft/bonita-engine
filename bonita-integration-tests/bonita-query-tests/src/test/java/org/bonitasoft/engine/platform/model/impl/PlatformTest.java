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
package org.bonitasoft.engine.platform.model.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Map;

import javax.inject.Inject;

import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.test.persistence.repository.PlatformRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class PlatformTest {

    @Inject
    private PlatformRepository repository;
    @Inject
    private JdbcTemplate jdbcTemplate;

    @Test
    public void should_save_and_get_SPlatform() {
        SPlatform platform = repository.add(SPlatform.builder()
                .initialBonitaVersion("5.9.0")
                .dbSchemaVersion("1.2")
                .applicationVersion("0.0.0")
                .created(345L)
                .information("some infos XYZ")
                .createdBy("The almighty")
                .build());
        repository.flush();

        PersistentObject platformFromQuery = repository.selectOneOnPlatform("getPlatform");
        Map<String, Object> platformAsMap = jdbcTemplate.queryForMap("SELECT * FROM platform");

        assertThat(platformFromQuery).isEqualTo(platform);
        assertThat(platformAsMap).containsOnly(
                entry("ID", platform.getId()),
                entry("CREATED", 345L),
                entry("CREATED_BY", "The almighty"),
                entry("INITIAL_BONITA_VERSION", "5.9.0"),
                entry("APPLICATION_VERSION", "0.0.0"),
                entry("MAINTENANCE_MESSAGE", null),
                entry("MAINTENANCE_MESSAGE_ACTIVE", false),
                entry("VERSION", "1.2"),
                entry("INFORMATION", "some infos XYZ"));
    }

}
