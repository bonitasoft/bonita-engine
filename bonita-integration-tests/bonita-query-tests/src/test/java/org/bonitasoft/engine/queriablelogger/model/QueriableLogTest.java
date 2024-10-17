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
public class QueriableLogTest {

    @Autowired
    TestRepository testRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void should_be_able_to_add_querriable_log() {

        SQueriableLog queriableLog = SQueriableLog.builder()
                .initializeNow()//
                .id(1)
                .userId("userId1")
                .actionType("actionType1") //
                .actionScope("actionScope1") //
                .productVersion("productVersion1") //
                .clusterNode("clusterNode1") //
                .severity(SQueriableLogSeverity.BUSINESS) //
                .actionScope("actionScope1") //
                .callerClassName("callerClassName1") //
                .callerMethodName("callerMethodName1") //
                .rawMessage("message1").build();

        SQueriableLog queriableLog1 = SQueriableLog.builder() //
                .initializeNow()
                .id(2)
                .userId("userId2")
                .productVersion("productVersion2") //
                .actionType("actionType2") //
                .actionScope("actionScope2") //
                .clusterNode("clusterNode2") //
                .severity(SQueriableLogSeverity.BUSINESS) //
                .actionScope("actionScope2") //
                .callerClassName("callerClassName2") //
                .callerMethodName("callerMethodName2") //
                .rawMessage("message2").build();

        testRepository.add(queriableLog);
        testRepository.add(queriableLog1);

        testRepository.flush();

        List<Map<String, Object>> queriableLogs = jdbcTemplate.queryForList("SELECT * from queriable_log");

        assertThat(queriableLogs).hasSize(2);
        assertThat(queriableLogs.stream().filter(m -> m.get("ID").equals(1L)).findFirst().get()).containsOnly(
                entry("ACTIONSCOPE", "actionScope1"), entry("ACTIONSTATUS", -1), entry("ACTIONTYPE", "actionType1"),
                entry("CALLERCLASSNAME", "callerClassName1"),
                entry("CALLERMETHODNAME", "callerMethodName1"),
                entry("CLUSTERNODE", "clusterNode1"),
                entry("NUMERICINDEX1", -1L),
                entry("NUMERICINDEX2", -1L),
                entry("NUMERICINDEX3", -1L),
                entry("NUMERICINDEX4", -1L),
                entry("NUMERICINDEX5", -1L),
                entry("PRODUCTVERSION", "productVersion1"),
                entry("SEVERITY", "BUSINESS"),
                entry("RAWMESSAGE", "message1"),
                entry("TENANTID", 0L), // remove when tenant notion disappears completely
                entry("THREADNUMBER", queriableLog.getThreadNumber()),
                entry("USERID", "userId1"),
                entry("WEEKOFYEAR", queriableLog.getWeekOfYear()),
                entry("WHATMONTH", queriableLog.getMonth()),
                entry("DAYOFYEAR", queriableLog.getDayOfYear()),
                entry("LOG_TIMESTAMP", queriableLog.getTimeStamp()),
                entry("ID", 1L),
                entry("WHATYEAR", queriableLog.getYear()));
        assertThat(queriableLogs.stream().filter(m -> m.get("ID").equals(2L)).findFirst().get()).containsOnly(
                entry("ACTIONSCOPE", "actionScope2"),
                entry("ACTIONSTATUS", -1),
                entry("ACTIONTYPE", "actionType2"),
                entry("CALLERCLASSNAME", "callerClassName2"),
                entry("CALLERMETHODNAME", "callerMethodName2"),
                entry("CLUSTERNODE", "clusterNode2"),
                entry("NUMERICINDEX1", -1L),
                entry("NUMERICINDEX2", -1L),
                entry("NUMERICINDEX3", -1L),
                entry("NUMERICINDEX4", -1L),
                entry("NUMERICINDEX5", -1L),
                entry("PRODUCTVERSION", "productVersion2"),
                entry("SEVERITY", "BUSINESS"),
                entry("RAWMESSAGE", "message2"),
                entry("TENANTID", 0L), // remove when tenant notion disappears completely
                entry("THREADNUMBER", queriableLog.getThreadNumber()),
                entry("USERID", "userId2"),
                entry("WEEKOFYEAR", queriableLog1.getWeekOfYear()),
                entry("WHATMONTH", queriableLog1.getMonth()),
                entry("DAYOFYEAR", queriableLog1.getDayOfYear()),
                entry("LOG_TIMESTAMP", queriableLog1.getTimeStamp()),
                entry("ID", 2L),
                entry("WHATYEAR", queriableLog1.getYear())

        );

    }
}
