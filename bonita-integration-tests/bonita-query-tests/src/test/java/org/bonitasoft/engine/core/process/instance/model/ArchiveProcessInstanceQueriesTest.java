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
import static org.assertj.core.api.Assertions.entry;
import static org.bonitasoft.engine.commons.Pair.mapOf;
import static org.bonitasoft.engine.commons.Pair.pair;
import static org.bonitasoft.engine.test.persistence.builder.PersistentObjectBuilder.DEFAULT_TENANT_ID;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.business.data.SAFlowNodeSimpleRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.business.data.SAProcessMultiRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.business.data.SAProcessSimpleRefBusinessDataInstance;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.test.persistence.repository.ProcessInstanceRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class ArchiveProcessInstanceQueriesTest {

    private static final long PROCESS_INSTANCE_ID = 43578923425L;
    private static final long FLOW_NODE_INSTANCE_ID = 342678L;

    @Inject
    private ProcessInstanceRepository repository;

    @Inject
    private JdbcTemplate jdbcTemplate;

    @Test
    public void getArchivedProcessInstancesInAllStates_should_return_archived_process_instances_when_exist() {
        // Given
        final SAProcessInstance saProcessInstance1 = repository.add(buildSAProcessInstance(1L));
        final SAProcessInstance saProcessInstance2 = repository.add(buildSAProcessInstance(2L));

        // When
        final List<SAProcessInstance> archivedProcessInstances = repository
                .getArchivedProcessInstancesInAllStates(Arrays.asList(1L, 2L));

        // Then
        assertFalse("The list of archived process instance must not be empty !!", archivedProcessInstances.isEmpty());
        assertEquals("The first element of the list must to have as id 1", saProcessInstance1,
                archivedProcessInstances.get(0));
        assertEquals("The second element of the list must to have as id 2", saProcessInstance2,
                archivedProcessInstances.get(1));
    }

    @Test
    public void getArchivedProcessInstancesInAllStates_should_return_empty_list_when_no_archived_process_instances_with_ids() {
        // Given

        // When
        final List<SAProcessInstance> archivedProcessInstances = repository
                .getArchivedProcessInstancesInAllStates(Arrays.asList(1L, 2L));

        // Then
        assertTrue("The list of archived process instance must be empty !!", archivedProcessInstances.isEmpty());
    }

    @Test
    public void should_save_and_get_multi_business_data_reference_for_process() {
        SAProcessMultiRefBusinessDataInstance multiRefBusinessDataInstance = new SAProcessMultiRefBusinessDataInstance();
        multiRefBusinessDataInstance.setDataIds(Arrays.asList(23L, 25L, 27L));
        multiRefBusinessDataInstance.setProcessInstanceId(PROCESS_INSTANCE_ID);
        multiRefBusinessDataInstance.setName("myMultiProcData");
        multiRefBusinessDataInstance.setDataClassName("someDataClassName");
        multiRefBusinessDataInstance = repository.add(multiRefBusinessDataInstance);
        repository.flush();

        PersistentObject multiRefBusinessData = repository.selectOne("getSARefBusinessDataInstance",
                pair("processInstanceId", PROCESS_INSTANCE_ID), pair("name", "myMultiProcData"));
        Map<String, Object> multiRefBusinessDataAsMap = jdbcTemplate
                .queryForMap(
                        "SELECT ID, KIND, NAME, DATA_CLASSNAME, DATA_ID, ORIG_PROC_INST_ID, ORIG_FN_INST_ID FROM arch_ref_biz_data_inst WHERE orig_proc_inst_id="
                                + PROCESS_INSTANCE_ID + " AND name='myMultiProcData'");
        List<Map<String, Object>> dataIds = jdbcTemplate
                .queryForList("SELECT ID, IDX, DATA_ID FROM arch_multi_biz_data WHERE id="
                        + multiRefBusinessDataInstance.getId());

        assertThat(((SAProcessMultiRefBusinessDataInstance) multiRefBusinessData).getDataIds())
                .isEqualTo(Arrays.asList(23L, 25L, 27L));
        assertThat(multiRefBusinessData).isEqualTo(multiRefBusinessDataInstance);
        assertThat(multiRefBusinessDataAsMap).containsOnly(
                entry("ID", multiRefBusinessDataInstance.getId()),
                entry("KIND", "proc_multi_ref"),
                entry("NAME", "myMultiProcData"),
                entry("DATA_CLASSNAME", "someDataClassName"),
                entry("DATA_ID", null),
                entry("ORIG_PROC_INST_ID", PROCESS_INSTANCE_ID),
                entry("ORIG_FN_INST_ID", null));
        assertThat(dataIds).containsExactly(
                mapOf(pair("ID", multiRefBusinessDataInstance.getId()), pair("IDX", 0), pair("DATA_ID", 23L)),
                mapOf(pair("ID", multiRefBusinessDataInstance.getId()), pair("IDX", 1), pair("DATA_ID", 25L)),
                mapOf(pair("ID", multiRefBusinessDataInstance.getId()), pair("IDX", 2), pair("DATA_ID", 27L)));
    }

    @Test
    public void should_save_and_get_single_business_data_reference_for_process() {
        SAProcessSimpleRefBusinessDataInstance singleRef = new SAProcessSimpleRefBusinessDataInstance();
        singleRef.setDataId(43L);
        singleRef.setProcessInstanceId(PROCESS_INSTANCE_ID);
        singleRef.setName("mySingleData");
        singleRef.setDataClassName("someDataClassName");
        singleRef = repository.add(singleRef);
        repository.flush();

        PersistentObject singleRefFromQuery = repository.selectOne("getSARefBusinessDataInstance",
                pair("processInstanceId", PROCESS_INSTANCE_ID), pair("name", "mySingleData"));
        Map<String, Object> multiRefBusinessDataAsMap = jdbcTemplate
                .queryForMap(
                        "SELECT ID, KIND, NAME, DATA_CLASSNAME, DATA_ID, ORIG_PROC_INST_ID, ORIG_FN_INST_ID FROM arch_ref_biz_data_inst WHERE orig_proc_inst_id="
                                + PROCESS_INSTANCE_ID
                                + " AND name='mySingleData'");
        assertThat(singleRefFromQuery).isEqualTo(singleRef);
        assertThat(multiRefBusinessDataAsMap).containsOnly(
                entry("ID", singleRef.getId()),
                entry("KIND", "proc_simple_ref"),
                entry("NAME", "mySingleData"),
                entry("DATA_CLASSNAME", "someDataClassName"),
                entry("DATA_ID", 43L),
                entry("ORIG_PROC_INST_ID", PROCESS_INSTANCE_ID),
                entry("ORIG_FN_INST_ID", null));
    }

    @Test
    public void should_save_and_get_single_business_data_reference_for_flow_node() {
        SAFlowNodeSimpleRefBusinessDataInstance singleRef = new SAFlowNodeSimpleRefBusinessDataInstance();
        singleRef.setDataId(43L);
        singleRef.setFlowNodeInstanceId(FLOW_NODE_INSTANCE_ID);
        singleRef.setName("mySingleData");
        singleRef.setDataClassName("someDataClassName");
        singleRef = repository.add(singleRef);
        repository.flush();

        PersistentObject singleRefFromQuery = repository.selectOne("getSAFlowNodeRefBusinessDataInstance",
                pair("flowNodeInstanceId", FLOW_NODE_INSTANCE_ID), pair("name", "mySingleData"));
        Map<String, Object> multiRefBusinessDataAsMap = jdbcTemplate
                .queryForMap(
                        "SELECT ID, KIND, NAME, DATA_CLASSNAME, DATA_ID, ORIG_PROC_INST_ID, ORIG_FN_INST_ID FROM arch_ref_biz_data_inst WHERE orig_fn_inst_id="
                                + FLOW_NODE_INSTANCE_ID
                                + " AND name='mySingleData'");
        assertThat(singleRefFromQuery).isEqualTo(singleRef);
        assertThat(multiRefBusinessDataAsMap).containsOnly(
                entry("ID", singleRef.getId()),
                entry("KIND", "fn_simple_ref"),
                entry("NAME", "mySingleData"),
                entry("DATA_CLASSNAME", "someDataClassName"),
                entry("DATA_ID", 43L),
                entry("ORIG_PROC_INST_ID", null),
                entry("ORIG_FN_INST_ID", FLOW_NODE_INSTANCE_ID));
    }

    private SAProcessInstance buildSAProcessInstance(final long id) {
        final SAProcessInstance saProcessInstance = new SAProcessInstance();
        saProcessInstance.setId(id);
        saProcessInstance.setSourceObjectId(id);
        saProcessInstance.setTenantId(DEFAULT_TENANT_ID);
        saProcessInstance.setName("process" + id);
        return saProcessInstance;
    }

}
