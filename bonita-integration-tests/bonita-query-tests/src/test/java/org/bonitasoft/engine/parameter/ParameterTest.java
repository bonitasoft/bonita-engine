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
package org.bonitasoft.engine.parameter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.bonitasoft.engine.commons.Pair.pair;

import java.util.Map;

import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.test.persistence.jdbc.JdbcRowMapper;
import org.bonitasoft.engine.test.persistence.repository.TestRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class ParameterTest {

    @Autowired
    private TestRepository testRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void should_save_and_get_parameter() {
        SParameter sParameter = testRepository.add(new SParameter("parameterName", "StringValue", 12345L));

        PersistentObject parameterFromQuery = testRepository.selectOne("getParameterByName",
                pair("name", "parameterName"), pair("processDefinitionId", 12345L));
        testRepository.flush();
        Map<String, Object> parameterAsMap = jdbcTemplate
                .queryForObject("SELECT * FROM proc_parameter WHERE name = 'parameterName'",
                        new JdbcRowMapper("TENANTID", "ID", "PROCESS_ID"));

        assertThat(parameterFromQuery).isEqualTo(sParameter);
        assertThat(parameterAsMap).containsOnly(
                entry("TENANTID", 0L), // remove when tenant notion disappears completely
                entry("ID", sParameter.getId()),
                entry("PROCESS_ID", 12345L),
                entry("NAME", "parameterName"),
                entry("VALUE", "StringValue"));
    }
}
