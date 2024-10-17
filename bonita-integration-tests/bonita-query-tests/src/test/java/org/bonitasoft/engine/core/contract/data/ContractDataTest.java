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
package org.bonitasoft.engine.core.contract.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.commons.Pair.pair;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.test.persistence.repository.ContractDataRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * author Emmanuel Duchastenier
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class ContractDataTest {

    @Autowired
    private ContractDataRepository repository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void should_be_able_to_add_contract_data() {
        repository.add(SProcessContractData.builder().name("myProcessContractData").scopeId(123L)
                .value("SerializedValue").build());
        repository.add(
                STaskContractData.builder().name("myTaskContractData").scopeId(124L).value("SerializedValue").build());

        repository.flush();

        List<Map<String, Object>> contractData = jdbcTemplate
                .queryForList("SELECT kind, name, scopeId, val from contract_data");

        assertThat(contractData).hasSize(2);
        assertThat(contractData).anySatisfy(c -> {
            assertThat(c.get("kind")).isEqualTo("PROCESS");
            assertThat(c.get("name")).isEqualTo("myProcessContractData");
            assertThat(c.get("scopeId")).isEqualTo(123L);
            assertThat(c.get("val")).isEqualTo("<string>SerializedValue</string>");
        });
        assertThat(contractData).anySatisfy(c -> {
            assertThat(c.get("kind")).isEqualTo("TASK");
            assertThat(c.get("name")).isEqualTo("myTaskContractData");
            assertThat(c.get("scopeId")).isEqualTo(124L);
            assertThat(c.get("val")).isEqualTo("<string>SerializedValue</string>");
        });
    }

    @Test
    public void should_be_able_to_retrieve_process_and_task_contract_data() {
        SProcessContractData processContractData = repository.add(SProcessContractData.builder()
                .name("myProcessContractData").scopeId(123L).value("SerializedValue").build());
        STaskContractData taskContractData = repository.add(STaskContractData.builder()
                .name("myTaskContractData").scopeId(124L).value("SerializedValue").build());

        assertThat(repository.selectOne("getContractDataByProcessInstanceId", pair("scopeId", 123L)))
                .isEqualTo(processContractData);
        assertThat(repository.selectOne("getContractDataByUserTaskId", pair("scopeId", 124L)))
                .isEqualTo(taskContractData);
    }

}
