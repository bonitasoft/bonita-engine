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
package org.bonitasoft.engine.core.form;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.commons.Pair.pair;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.bonitasoft.engine.page.SPageMapping;
import org.bonitasoft.engine.test.persistence.repository.TestRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class FormMappingTest {

    @Inject
    TestRepository testRepository;

    @Inject
    private JdbcTemplate jdbcTemplate;

    @Test
    public void should_be_able_to_add_form_mapping() {
        SPageMapping pageMapping = SPageMapping.builder().id(1).build();
        testRepository.add(pageMapping);
        testRepository.add(SFormMapping.builder().pageMapping(pageMapping).type(3).task("task1").target("target1")
                .lastUpdateDate(200L).lastUpdatedBy(100L).processDefinitionId(2L).build());
        testRepository.add(SFormMapping.builder().pageMapping(pageMapping).type(4).task("task2").target("target2")
                .processDefinitionId(3L).build());

        testRepository.flush();

        List<Map<String, Object>> formMapping = jdbcTemplate.queryForList("SELECT * from form_mapping");
        assertThat(formMapping).hasSize(2);

        assertThat(formMapping).anySatisfy(c -> {
            assertThat(c.get("task")).isEqualTo("task1");
            assertThat(c.get("type")).isEqualTo(3);
            assertThat(c.get("page_mapping_id")).isEqualTo(1L);
            assertThat(c.get("lastupdatedate")).isEqualTo(200L);
            assertThat(c.get("lastupdatedby")).isEqualTo(100L);
            assertThat(c.get("process")).isEqualTo(2L);
            assertThat(c.get("target")).isEqualTo("target1");
        });
        assertThat(formMapping).anySatisfy(c -> {
            assertThat(c.get("task")).isEqualTo("task2");
            assertThat(c.get("page_mapping_id")).isEqualTo(1L);
            assertThat(c.get("type")).isEqualTo(4);
            assertThat(c.get("process")).isEqualTo(3L);
            assertThat(c.get("target")).isEqualTo("target2");
        });
    }

    @Test
    public void should_be_able_to_retrieve_formMapping_with_process_definition_id() {
        SFormMapping formMapping = testRepository.add(SFormMapping.builder().processDefinitionId(123L).build());
        SFormMapping formMapping1 = testRepository.add(SFormMapping.builder().processDefinitionId(124L).build());

        assertThat(testRepository.selectOne("getFormMappingsOfProcessDefinition", pair("processDefinitionId", 123L)))
                .isEqualTo(formMapping);
        assertThat(testRepository.selectOne("getFormMappingsOfProcessDefinition", pair("processDefinitionId", 124L)))
                .isEqualTo(formMapping1);

    }

}
