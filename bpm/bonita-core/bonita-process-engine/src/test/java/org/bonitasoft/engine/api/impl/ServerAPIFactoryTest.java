package org.bonitasoft.engine.api.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServerAPIFactoryTest {

    @Mock
    private BonitaHomeServer bonitaHomeServer;

    @InjectMocks
    ServerAPIFactory serverAPIFactory;

    @Test
    public void getServerAPIImplemShouldReturnTestImplemOfServerAPI() throws Exception {
        // given:
        when(bonitaHomeServer.getServerAPIImplementation()).thenReturn("org.bonitasoft.engine.api.impl.TestImplemOfServerAPI");
        // when:
        ServerAPI serverAPIimplementation = serverAPIFactory.getServerAPIImplementation();
        // then:
        assertNotNull(serverAPIimplementation);
        assertEquals(TestImplemOfServerAPI.class, serverAPIimplementation.getClass());

    }
}
