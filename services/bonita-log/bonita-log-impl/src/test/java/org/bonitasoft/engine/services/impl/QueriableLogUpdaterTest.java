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
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.SPlatformProperties;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.impl.SQueriableLogImpl;
import org.bonitasoft.engine.services.QueriableLogSessionProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QueriableLogUpdaterTest {

    @Mock
    private QueriableLogSessionProvider sessionProvider;

    @Mock
    private PlatformService platformService;

    @Mock
    private TechnicalLoggerService logger;

    private SQueriableLogImpl log;

    @InjectMocks
    private QueriableLogUpdater updater;

    @Before
    public void setUp() {
        given(sessionProvider.getUserId()).willReturn("user");
        given(sessionProvider.getClusterNode()).willReturn("node1");

        final SPlatformProperties properties = mock(SPlatformProperties.class);
        given(platformService.getSPlatformProperties()).willReturn(properties);
        given(properties.getPlatformVersion()).willReturn("platform.version");

        given(logger.isLoggable(Matchers.<Class<?>> any(), any(TechnicalLogSeverity.class))).willReturn(true);

        log = getLogBuilderWithMandatoryFields();
    }

    private SQueriableLogImpl getLogBuilderWithMandatoryFields() {
        final SQueriableLogImpl log = new SQueriableLogImpl();
        log.setSeverity(SQueriableLogSeverity.INTERNAL);
        log.setActionType("insert");
        log.setActionStatus(SQueriableLog.STATUS_OK);
        log.setActionScope("scope");
        log.setRawMessage("message");
        return log;
    }

    @Test
    public void buildFinalLog_should_add_meta_information() {
        //when
        final SQueriableLog finalLog = updater.buildFinalLog("class", "method", log);

        //then
        assertThat(finalLog.getCallerClassName()).isEqualTo("class");
        assertThat(finalLog.getCallerMethodName()).isEqualTo("method");
        assertThat(finalLog.getUserId()).isEqualTo("user");
        assertThat(finalLog.getClusterNode()).isEqualTo("node1");
        assertThat(finalLog.getProductVersion()).isEqualTo("platform.version");
    }

    @Test
    public void raw_message_should_be_trunked_to_255_characters() {
        //given
        final String base = "base message ";
        final StringBuilder stb = new StringBuilder();
        while (stb.length() <= 255) {
            stb.append(base);
        }
        final SQueriableLogImpl log = getLogBuilderWithMandatoryFields();
        log.setRawMessage(stb.toString());

        //when
        final SQueriableLog finalLog = updater.buildFinalLog("class", "method", log);

        //then
        assertThat(finalLog.getRawMessage()).hasSize(255);
    }

}
