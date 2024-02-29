/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.internal.servlet;

import static org.mockito.Mockito.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.bonitasoft.engine.EngineInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EngineInitializerListenerTest {

    @Spy
    private EngineInitializerListener listener;

    @Mock
    private AnnotationConfigWebApplicationContext context;
    @Mock
    private EngineInitializer engineInitializer;

    private MockEnvironment env;

    @BeforeEach
    void setUp() throws Exception {
        doReturn(engineInitializer).when(listener).getEngineInitializer();
        env = new MockEnvironment();
        when(context.getEnvironment()).thenReturn(env);
        doReturn(context).when(listener).initializeWebApplicationContext(any(), any());
        doNothing().when(listener).exit(anyInt());
    }

    @Test
    void doNotExit() {
        listener.contextInitialized(new ServletContextEvent(mock(ServletContext.class)));

        verify(listener, never()).exit(anyInt());
    }

    @Test
    void exitNormally() {
        env.setProperty(EngineInitializerListener.UPDATE_ONLY_STARTUP_PROPERTY, Boolean.TRUE.toString());
        listener.contextInitialized(new ServletContextEvent(mock(ServletContext.class)));

        verify(listener).exit(0);
    }

    @Test
    void exitOnError() throws Exception {
        doThrow(IllegalStateException.class).when(listener).initializeWebApplicationContext(any(), any());
        listener.contextInitialized(new ServletContextEvent(mock(ServletContext.class)));

        verify(listener).exit(1);
    }

}
