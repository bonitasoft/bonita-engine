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
package org.bonitasoft.engine.queriablelogger.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

import java.util.List;
import java.util.Map;

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
public class QueriableLogParameterTest {

    @Autowired
    TestRepository testRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void should_be_able_to_add_querriable_log() {

        SQueriableLogParameter queriableLogParameter = SQueriableLogParameter
                .builder()
                .id(1L)
                .queriableLogId(1L)
                .tenantId(1L)
                .name("name1")
                .stringValue("stringValue1") //
                .valueType("valueType1") //
                .build();
        SQueriableLogParameter queriableLogParameter2 = SQueriableLogParameter.builder()
                .id(2L)
                .queriableLogId(2L)
                .tenantId(1L)
                .name("name2")
                .stringValue("stringValue2") //
                .valueType("valueType2") //
                .build();

        testRepository.add(queriableLogParameter);
        testRepository.add(queriableLogParameter2);

        testRepository.flush();

        List<Map<String, Object>> queriableLogs = jdbcTemplate.query("SELECT * from queriableLog_p",
                new JdbcRowMapper("ID", "TENANTID", "B_LOG_ID"));

        assertThat(queriableLogs).hasSize(2);
        assertThat(queriableLogs.stream().filter(m -> m.get("ID").equals(1L)).findFirst().get()).containsOnly(
                entry("ID", 1L),
                entry("TENANTID", 1L),
                entry("PARAM_NAME", "name1"),
                entry("B_LOG_ID", 1L),
                entry("STRINGVALUE", "stringValue1"),
                entry("VALUETYPE", "valueType1"));
        assertThat(queriableLogs.stream().filter(m -> m.get("ID").equals(2L)).findFirst().get()).containsOnly(
                entry("ID", 2L),
                entry("TENANTID", 1L),
                entry("PARAM_NAME", "name2"),
                entry("B_LOG_ID", 2L),
                entry("STRINGVALUE", "stringValue2"),
                entry("VALUETYPE", "valueType2")

        );

    }
}
