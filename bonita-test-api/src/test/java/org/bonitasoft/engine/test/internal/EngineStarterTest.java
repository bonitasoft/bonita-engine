/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.session.PlatformSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
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
    @Spy
    private EngineStarter engineStarter;

    @Before
    public void before() throws Exception {
        doReturn(platformSession).when(platformLoginAPI).login(anyString(), anyString());
        doReturn(platformLoginAPI).when(engineStarter).getPlatformLoginAPI();
        doReturn(platformAPI).when(engineStarter).getPlatformAPI(any(PlatformSession.class));
        doNothing().when(engineStarter).undeployCommands();
        doNothing().when(engineStarter).checkTempFoldersAreCleaned();
        doNothing().when(engineStarter).checkThreadsAreStopped();
    }

    @Test
    public void should_not_drop_platform_when_dropOnStop_is_false() throws Exception {
        //given
        doReturn(true).when(platformAPI).isNodeStarted();
        //when
        engineStarter.setDropOnStop(false);
        engineStarter.stop();
        //then
        verify(platformAPI, never()).cleanPlatform();
        verify(platformAPI).stopNode();
    }

    @Test
    public void should_not_drop_platform_when_dropOnStop_is_true() throws Exception {
        //given
        doReturn(true).when(platformAPI).isNodeStarted();
        //when
        engineStarter.setDropOnStop(true);
        engineStarter.stop();
        //then
        verify(platformAPI).cleanPlatform();
        verify(platformAPI).stopNode();
    }
}
