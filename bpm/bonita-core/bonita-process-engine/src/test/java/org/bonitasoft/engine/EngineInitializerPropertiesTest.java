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
package org.bonitasoft.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.IOException;
import java.util.Properties;

import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * 
 * @author Baptiste Mesta
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BonitaHomeServer.class)
public class EngineInitializerPropertiesTest {

    private EngineInitializerProperties engineInitializerProperties;

    private BonitaHomeServer bonitaHomeServer;

    private Properties properties;

    @Before
    public void before() throws BonitaHomeNotSetException, IOException {
        mockStatic(BonitaHomeServer.class);
        bonitaHomeServer = mock(BonitaHomeServer.class);
        when(BonitaHomeServer.getInstance()).thenReturn(bonitaHomeServer);
        properties = new Properties();
        when(bonitaHomeServer.getPlatformProperties()).thenReturn(properties);
        engineInitializerProperties = new EngineInitializerProperties();
    }

    @Test
    public void testShouldCreatePlatform() {
        properties.put("platform.create", "true");
        assertTrue(engineInitializerProperties.shouldCreatePlatform());
    }

    @Test
    public void testShouldNotCreatePlatform() {
        properties.put("platform.create", "false");
        assertFalse(engineInitializerProperties.shouldCreatePlatform());
    }

    @Test
    public void testShouldStartPlatform() {
        properties.put("node.start", "true");
        assertTrue(engineInitializerProperties.shouldStartPlatform());
    }

    @Test
    public void testShouldNotStartPlatform() {
        properties.put("node.start", "false");
        assertFalse(engineInitializerProperties.shouldStartPlatform());
    }

    @Test
    public void testShouldStopPlatform() {
        properties.put("node.stop", "true");
        assertTrue(engineInitializerProperties.shouldStopPlatform());
    }

    @Test
    public void testShouldNotStopPlatform() {
        properties.put("node.stop", "false");
        assertFalse(engineInitializerProperties.shouldStopPlatform());
    }

    @Test
    public void testPropertyNotSet() {
        try {
            engineInitializerProperties.shouldStopPlatform();
            fail("should fail with property not set");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Mandatory property not set") && e.getMessage().contains("node.stop"));
        }
    }

    @Test
    public void testGetPlatformAdminUsername() {
        properties.put("platformAdminUsername", "theUserNameOfThePlatform");
        assertEquals("theUserNameOfThePlatform", engineInitializerProperties.getPlatformAdminUsername());
    }

    @Test
    public void testGetTenantAdminUsername() throws Exception {
        Properties tenantProperties = new Properties();
        tenantProperties.put("userName", "theUserNameOfTheTenant");
        when(bonitaHomeServer.getTenantProperties(123l)).thenReturn(tenantProperties);
        assertEquals("theUserNameOfTheTenant", engineInitializerProperties.getTenantAdminUsername(123l));
    }

}
