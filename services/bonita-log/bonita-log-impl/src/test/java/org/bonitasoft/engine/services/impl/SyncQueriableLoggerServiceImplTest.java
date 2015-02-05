/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.SPlatformProperties;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.impl.SQueriableLogImpl;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.QueriableLogSessionProvider;
import org.bonitasoft.engine.services.QueriableLoggerStrategy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SyncQueriableLoggerServiceImplTest {

    private static final String FIRST_ACTION = "first_action";

    private static final String SECOND_ACTION = "sencond_action";

    @Mock
    private PersistenceService persistenceService;

    @Mock
    private QueriableLoggerStrategy loggerStrategy;

    @Mock
    private QueriableLogSessionProvider sessionProvider;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private SQueriableLogImpl log1;

    @Mock
    private SQueriableLogImpl log2;

    @Mock
    private PlatformService platformService;

    @InjectMocks
    private SyncQueriableLoggerServiceImpl logService;

    @Before
    public void setUp() {
        SPlatformProperties platformProperties = mock(SPlatformProperties.class);
        doReturn(FIRST_ACTION).when(log1).getActionType();
        doReturn(SQueriableLogSeverity.INTERNAL).when(log1).getSeverity();
        doReturn("msg").when(log1).getRawMessage();

        doReturn(SECOND_ACTION).when(log2).getActionType();
        doReturn(SQueriableLogSeverity.INTERNAL).when(log2).getSeverity();
        doReturn("new action msg").when(log2).getRawMessage();

        doReturn(platformProperties).when(platformService).getSPlatformProperties();
        doReturn("6.3").when(platformProperties).getPlatformVersion();

    }

    @Test
    public void isLoggable_should_return_true_if_strategy_is_loggable() {
        // given
        doReturn(true).when(loggerStrategy).isLoggable(FIRST_ACTION, SQueriableLogSeverity.INTERNAL);

        // when
        boolean loggable = logService.isLoggable(FIRST_ACTION, SQueriableLogSeverity.INTERNAL);

        // then
        assertThat(loggable).isTrue();
    }

    @Test
    public void isLoggable_should_return_false_if_strategy_is_not_loggable() {
        // given
        doReturn(false).when(loggerStrategy).isLoggable(FIRST_ACTION, SQueriableLogSeverity.INTERNAL);

        // when
        boolean loggable = logService.isLoggable(FIRST_ACTION, SQueriableLogSeverity.INTERNAL);

        // then
        assertThat(loggable).isFalse();
    }

    @Test
    public void calling_log_must_insert_log_if_isLogable() throws Exception {
        // given
        doReturn(true).when(loggerStrategy).isLoggable(FIRST_ACTION, SQueriableLogSeverity.INTERNAL);
        doReturn("walter.bates").when(sessionProvider).getUserId();
        doReturn("node1").when(sessionProvider).getClusterNode();

        // when
        logService.log("CallerClass", "callerMethod", log1);

        // then
        verify(persistenceService, times(1)).insert(log1);
        verify(log1, times(1)).setClusterNode("node1");
        verify(log1, times(1)).setCallerClassName("CallerClass");
        verify(log1, times(1)).setCallerMethodName("callerMethod");
        verify(log1, times(1)).setProductVersion("6.3");
        verify(log1, times(1)).setUserId("walter.bates");

    }

    @Test
    public void calling_log_must_not_insert_log_if_is_not_logable() throws Exception {
        // given
        doReturn(false).when(loggerStrategy).isLoggable(FIRST_ACTION, SQueriableLogSeverity.INTERNAL);

        // when
        logService.log("CallerClass", "callerMethod", log1);

        // then
        verify(persistenceService, never()).insert(log1);
    }

    @Test
    public void calling_log_must_insert_all_logable_logs() throws Exception {
        // given
        doReturn(true).when(loggerStrategy).isLoggable(FIRST_ACTION, SQueriableLogSeverity.INTERNAL);
        doReturn(true).when(loggerStrategy).isLoggable(SECOND_ACTION, SQueriableLogSeverity.INTERNAL);

        // when
        logService.log("CallerClass", "callerMethod", log1, log2);

        // then
        verify(persistenceService, times(1)).insert(log1);
        verify(persistenceService, times(1)).insert(log2);
    }

    @Test
    public void getNumberOfLogs_should_return_number_of_logs_from_persistence_service() throws Exception {
        // given
        doReturn(15L).when(persistenceService).selectOne(argThat(new SelectOneMatcher("getNumberOfLogs")));

        // when
        int numberOfLogs = logService.getNumberOfLogs();

        // then
        assertThat(numberOfLogs).isEqualTo(15);
    }

    @Test
    public void getLogs_should_return_logs_from_persitence_service() throws Exception {
        // given
        SelectListMatcher matcher = new SelectListMatcher("getLogs", 5, 10, "id", OrderByType.ASC);
        List<SQueriableLogImpl> persistenceLogs = Arrays.asList(log1, log2);
        doReturn(persistenceLogs).when(persistenceService).selectList(argThat(matcher));

        // when
        List<SQueriableLog> logs = logService.getLogs(5, 10, "id", OrderByType.ASC);

        // then
        assertThat(logs).isEqualTo(persistenceLogs);
    }

    @Test
    public void getNumberOfEntities_should_return_number_of_entities_from_persistence_service() throws Exception {
        // given
        QueryOptions queryOptions = new QueryOptions(0, 10);
        doReturn(20L).when(persistenceService).getNumberOfEntities(SQueriableLog.class, queryOptions, null);

        // when
        long numberOfLogs = logService.getNumberOfLogs(queryOptions);

        // then
        assertThat(numberOfLogs).isEqualTo(20L);
    }

    @Test
    public void searchLogs_should_return_logs_from_persistence_service() throws Exception {
        // given
        QueryOptions queryOptions = new QueryOptions(0, 10);
        List<SQueriableLogImpl> persistenceLogs = Arrays.asList(log1, log2);
        doReturn(persistenceLogs).when(persistenceService).searchEntity(SQueriableLog.class, queryOptions, null);

        // when
        List<SQueriableLog> logs = logService.searchLogs(queryOptions);

        // then
        assertThat(logs).isEqualTo(persistenceLogs);
    }

    @Test
    public void getLog_should_return_log_from_persistence_service() throws Exception {
        // given
        doReturn(log1).when(persistenceService).selectById(argThat(new SelectByIdMatcher("getQueriableLogById", 100L)));

        // when
        SQueriableLog log = logService.getLog(100L);

        // then
        assertThat(log).isEqualTo(log1);
    }

}
