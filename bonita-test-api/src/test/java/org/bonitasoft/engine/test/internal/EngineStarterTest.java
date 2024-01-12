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
package org.bonitasoft.engine.test.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.bonitasoft.engine.BonitaEngine;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.session.PlatformSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class EngineStarterTest {

    @Mock
    private PlatformSession platformSession;
    @Mock
    private PlatformAPI platformAPI;
    @Mock
    private PlatformLoginAPI platformLoginAPI;
    @Mock
    private BonitaEngine bonitaEngine;
    private EngineStarter engineStarter;

    @Before
    public void before() throws Exception {
        engineStarter = spy(EngineStarter.create(bonitaEngine));
        doReturn(platformSession).when(platformLoginAPI).login(anyString(), anyString());
        doReturn(platformLoginAPI).when(engineStarter).getPlatformLoginAPI();
        doReturn(platformAPI).when(engineStarter).getPlatformAPI(any(PlatformSession.class));
        doNothing().when(engineStarter).undeployCommands();
        doNothing().when(engineStarter).checkTempFoldersAreCleaned();
        doNothing().when(engineStarter).checkThreadsAreStopped();
    }

    @Test
    public void should_stop_node() throws Exception {
        //given
        doReturn(true).when(platformAPI).isNodeStarted();
        //when
        engineStarter.stop();
        //then
        verify(platformAPI).stopNode();
        verify(bonitaEngine).stop();
    }

}
