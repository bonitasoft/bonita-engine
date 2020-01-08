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
package org.bonitasoft.engine.api.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServerAPIFactoryTest {

    @Mock
    private BonitaHomeServer bonitaHomeServer;

    @InjectMocks
    private ServerAPIFactory serverAPIFactory;

    @Test
    public void getServerAPIImplemShouldReturnTestImplemOfServerAPI() throws Exception {
        // given:
        when(bonitaHomeServer.getServerAPIImplementation())
                .thenReturn("org.bonitasoft.engine.api.impl.TestImplemOfServerAPI");
        // when:
        ServerAPI serverAPIimplementation = serverAPIFactory.getServerAPIImplementation();
        // then:
        assertNotNull(serverAPIimplementation);
        assertEquals(TestImplemOfServerAPI.class, serverAPIimplementation.getClass());

    }

    @Test
    public void should_cache_the_serverapi_class() throws Exception {
        //given
        when(bonitaHomeServer.getServerAPIImplementation())
                .thenReturn("org.bonitasoft.engine.api.impl.TestImplemOfServerAPI");
        serverAPIFactory.getServerAPIImplementation();
        //when
        serverAPIFactory.getServerAPIImplementation();

        //then
        verify(bonitaHomeServer, times(1)).getServerAPIImplementation();
    }
}
