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
import static org.bonitasoft.engine.commons.Pair.pair;

import java.util.Map;

import org.bonitasoft.engine.core.process.definition.model.SGatewayType;
import org.bonitasoft.engine.core.process.instance.model.archive.SAAutomaticTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SACallActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SALoopActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAManualTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAMultiInstanceActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAReceiveTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SASendTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SASubProcessActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAUserTaskInstance;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.test.persistence.repository.FlowNodeInstanceRepository;
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
public class ArchiveFlowNodeInstanceTest {

    @Autowired
    private FlowNodeInstanceRepository repository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void should_save_and_get_SAAutomaticTaskInstance() {
        SAFlowNodeInstance entity = new SAAutomaticTaskInstance();
        SAFlowNodeInstance flowNode = repository.add(entity);
        repository.flush();

        PersistentObject flowNodeFromQuery = repository.selectOne("getArchivedFlowNodeInstanceById",
                pair("id", flowNode.getId()));
        Map<String, Object> flowNodeAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM arch_flownode_instance where id = " + flowNode.getId());

        assertThat(flowNodeFromQuery).isEqualTo(flowNode);
        assertThat(flowNodeAsMap.get("KIND")).isEqualTo("auto");
    }

    @Test
    public void should_save_and_get_SAUserTaskInstance_with_task_priority() {
        SAUserTaskInstance entity = new SAUserTaskInstance();
        entity.setPriority(STaskPriority.ABOVE_NORMAL);
        SAUserTaskInstance flowNode = repository.add(entity);
        repository.flush();

        PersistentObject flowNodeFromQuery = repository.selectOne("getArchivedFlowNodeInstanceById",
                pair("id", flowNode.getId()));
        Map<String, Object> flowNodeAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM arch_flownode_instance where id = " + flowNode.getId());

        assertThat(flowNodeFromQuery).isEqualTo(flowNode);
        assertThat(((Number) flowNodeAsMap.get("PRIORITY")).intValue()).isEqualTo(3);
    }

    @Test
    public void should_save_and_get_SAGatewayInstance_with_gateway_type() {
        SAGatewayInstance entity = new SAGatewayInstance();
        entity.setGatewayType(SGatewayType.INCLUSIVE);
        SAFlowNodeInstance flowNode = repository.add(entity);
        repository.flush();

        PersistentObject flowNodeFromQuery = repository.selectOne("getArchivedFlowNodeInstanceById",
                pair("id", flowNode.getId()));
        Map<String, Object> flowNodeAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM arch_flownode_instance where id = " + flowNode.getId());

        assertThat(flowNodeFromQuery).isEqualTo(flowNode);
        assertThat(flowNodeAsMap.get("GATEWAYTYPE")).isEqualTo("INCLUSIVE");
    }

    @Test
    public void should_save_and_get_SACallActivityInstance() {
        SAFlowNodeInstance entity = new SACallActivityInstance();
        SAFlowNodeInstance flowNode = repository.add(entity);
        repository.flush();

        PersistentObject flowNodeFromQuery = repository.selectOne("getArchivedFlowNodeInstanceById",
                pair("id", flowNode.getId()));
        Map<String, Object> flowNodeAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM arch_flownode_instance where id = " + flowNode.getId());

        assertThat(flowNodeFromQuery).isEqualTo(flowNode);
        assertThat(flowNodeAsMap.get("KIND")).isEqualTo("call");
    }

    @Test
    public void should_save_and_get_SAGatewayInstance() {
        SAFlowNodeInstance entity = new SAGatewayInstance();
        SAFlowNodeInstance flowNode = repository.add(entity);
        repository.flush();

        PersistentObject flowNodeFromQuery = repository.selectOne("getArchivedFlowNodeInstanceById",
                pair("id", flowNode.getId()));
        Map<String, Object> flowNodeAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM arch_flownode_instance where id = " + flowNode.getId());

        assertThat(flowNodeFromQuery).isEqualTo(flowNode);
        assertThat(flowNodeAsMap.get("KIND")).isEqualTo("gate");
    }

    @Test
    public void should_save_and_get_SALoopActivityInstance() {
        SAFlowNodeInstance entity = new SALoopActivityInstance();
        SAFlowNodeInstance flowNode = repository.add(entity);
        repository.flush();

        PersistentObject flowNodeFromQuery = repository.selectOne("getArchivedFlowNodeInstanceById",
                pair("id", flowNode.getId()));
        Map<String, Object> flowNodeAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM arch_flownode_instance where id = " + flowNode.getId());

        assertThat(flowNodeFromQuery).isEqualTo(flowNode);
        assertThat(flowNodeAsMap.get("KIND")).isEqualTo("loop");
    }

    @Test
    public void should_save_and_get_SAManualTaskInstance() {
        SAFlowNodeInstance entity = new SAManualTaskInstance();
        SAFlowNodeInstance flowNode = repository.add(entity);
        repository.flush();

        PersistentObject flowNodeFromQuery = repository.selectOne("getArchivedFlowNodeInstanceById",
                pair("id", flowNode.getId()));
        Map<String, Object> flowNodeAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM arch_flownode_instance where id = " + flowNode.getId());

        assertThat(flowNodeFromQuery).isEqualTo(flowNode);
        assertThat(flowNodeAsMap.get("KIND")).isEqualTo("manual");
    }

    @Test
    public void should_save_and_get_SAMultiInstanceActivityInstance() {
        SAFlowNodeInstance entity = new SAMultiInstanceActivityInstance();
        SAFlowNodeInstance flowNode = repository.add(entity);
        repository.flush();

        PersistentObject flowNodeFromQuery = repository.selectOne("getArchivedFlowNodeInstanceById",
                pair("id", flowNode.getId()));
        Map<String, Object> flowNodeAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM arch_flownode_instance where id = " + flowNode.getId());

        assertThat(flowNodeFromQuery).isEqualTo(flowNode);
        assertThat(flowNodeAsMap.get("KIND")).isEqualTo("multi");
    }

    @Test
    public void should_save_and_get_SAReceiveTaskInstance() {
        SAFlowNodeInstance entity = new SAReceiveTaskInstance();
        SAFlowNodeInstance flowNode = repository.add(entity);
        repository.flush();

        PersistentObject flowNodeFromQuery = repository.selectOne("getArchivedFlowNodeInstanceById",
                pair("id", flowNode.getId()));
        Map<String, Object> flowNodeAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM arch_flownode_instance where id = " + flowNode.getId());

        assertThat(flowNodeFromQuery).isEqualTo(flowNode);
        assertThat(flowNodeAsMap.get("KIND")).isEqualTo("receive");
    }

    @Test
    public void should_save_and_get_SASendTaskInstance() {
        SAFlowNodeInstance entity = new SASendTaskInstance();
        SAFlowNodeInstance flowNode = repository.add(entity);
        repository.flush();

        PersistentObject flowNodeFromQuery = repository.selectOne("getArchivedFlowNodeInstanceById",
                pair("id", flowNode.getId()));
        Map<String, Object> flowNodeAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM arch_flownode_instance where id = " + flowNode.getId());

        assertThat(flowNodeFromQuery).isEqualTo(flowNode);
        assertThat(flowNodeAsMap.get("KIND")).isEqualTo("send");
    }

    @Test
    public void should_save_and_get_SASubProcessActivityInstance() {
        SAFlowNodeInstance entity = new SASubProcessActivityInstance();
        SAFlowNodeInstance flowNode = repository.add(entity);
        repository.flush();

        PersistentObject flowNodeFromQuery = repository.selectOne("getArchivedFlowNodeInstanceById",
                pair("id", flowNode.getId()));
        Map<String, Object> flowNodeAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM arch_flownode_instance where id = " + flowNode.getId());

        assertThat(flowNodeFromQuery).isEqualTo(flowNode);
        assertThat(flowNodeAsMap.get("KIND")).isEqualTo("subProc");
    }

    @Test
    public void should_save_and_get_SAUserTaskInstance() {
        SAFlowNodeInstance entity = new SAUserTaskInstance();
        SAFlowNodeInstance flowNode = repository.add(entity);
        repository.flush();

        PersistentObject flowNodeFromQuery = repository.selectOne("getArchivedFlowNodeInstanceById",
                pair("id", flowNode.getId()));
        Map<String, Object> flowNodeAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM arch_flownode_instance where id = " + flowNode.getId());

        assertThat(flowNodeFromQuery).isEqualTo(flowNode);
        assertThat(flowNodeAsMap.get("KIND")).isEqualTo("user");
    }

}
