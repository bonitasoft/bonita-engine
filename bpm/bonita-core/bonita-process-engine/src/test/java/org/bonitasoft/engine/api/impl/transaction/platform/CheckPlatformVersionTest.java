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
package org.bonitasoft.engine.api.impl.transaction.platform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.SPlatformProperties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CheckPlatformVersionTest {

    @Mock
    private PlatformService platformService;

    @Mock
    private SPlatform platform;

    @Mock
    private SPlatformProperties platformProperties;

    @Mock
    private BonitaHomeServer bonitaHomeServer;

    @InjectMocks
    private CheckPlatformVersion checkPlatformVersion;

    @Before
    public void initialize() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(platformService.getPlatform()).thenReturn(platform);
        when(platformService.getSPlatformProperties()).thenReturn(platformProperties);
    }

    @Test
    public void check_db_and_jars_with_same_minor_version_is_accepted() throws Exception {
        given_DB_Jar_Home_Versions("6.1.0", "6.1.1", "6.1.1");

        Boolean sameVersion = checkPlatformVersion.call();

        assertTrue(sameVersion);
    }

    @Test
    public void check_db_and_jars_with_same_maintenance_version_is_accepted() throws Exception {
        given_DB_Jar_Home_Versions("6.1.1", "6.1.1-SNAPSHOT", "6.1.1-SNAPSHOT");

        Boolean sameVersion = checkPlatformVersion.call();

        assertTrue(sameVersion);
    }

    @Test
    public void check_db_and_jars_with_exact_same_version_is_accepted() throws Exception {
        given_DB_Jar_Home_Versions("6.1.1-SNAPSHOT", "6.1.1-SNAPSHOT", "6.1.1-SNAPSHOT");

        Boolean sameVersion = checkPlatformVersion.call();

        assertTrue(sameVersion);
    }

    @Test
    public void check_db_and_jars_with_different_minor_version_is_rejected() throws Exception {
        given_DB_Jar_Home_Versions("6.0.3", "6.1.1-SNAPSHOT", "6.1.1-SNAPSHOT");

        Boolean sameVersion = checkPlatformVersion.call();

        assertFalse(sameVersion);
        assertEquals(
                "The version of the platform in database is not the same as expected: bonita-server version is <6.1.1-SNAPSHOT> and database version is <6.0.3>",
                checkPlatformVersion.getErrorMessage());
    }

    @Test
    public void check_db_and_jars_with_different_major_version_is_rejected() throws Exception {
        given_DB_Jar_Home_Versions("5.0.3", "6.0.3", "6.0.3");

        Boolean sameVersion = checkPlatformVersion.call();
        assertFalse(sameVersion);
        assertEquals("The version of the platform in database is not the same as expected: bonita-server version is <6.0.3> and database version is <5.0.3>",
                checkPlatformVersion.getErrorMessage());
    }

    @Test
    public void check_db_and_jars_with_different_minor_version_with_2_digits_is_rejected() throws Exception {
        given_DB_Jar_Home_Versions("6.1", "6.10", "6.10");

        Boolean sameVersion = checkPlatformVersion.call();

        assertFalse(sameVersion);
        assertEquals("The version of the platform in database is not the same as expected: bonita-server version is <6.10> and database version is <6.1>",
                checkPlatformVersion.getErrorMessage());
    }

    @Test
    public void check_home_and_jars_with_different_version_is_rejected() throws Exception {
        given_DB_Jar_Home_Versions("6.2.0", "6.2.0", "6.1.0");

        Boolean sameVersion = checkPlatformVersion.call();

        assertFalse(sameVersion);
        assertEquals(
                "The version of the bonita home is not the same as expected: bonita-server version is <6.2.0> and bonita home version is <6.1.0>",
                checkPlatformVersion.getErrorMessage());
    }

    @Test
    public void check_home_and_jars_with_different_maintenance_version_is_rejected() throws Exception {
        given_DB_Jar_Home_Versions("6.2.1", "6.2.0", "6.2.1");

        Boolean sameVersion = checkPlatformVersion.call();

        assertFalse(sameVersion);
        assertEquals(
                "The version of the bonita home is not the same as expected: bonita-server version is <6.2.0> and bonita home version is <6.2.1>",
                checkPlatformVersion.getErrorMessage());
    }

    @Test
    public void check_home_and_db_with_different_maintenance_version_is_accepted() throws Exception {
        given_DB_Jar_Home_Versions("6.2.0", "6.2.1", "6.2.1");

        Boolean sameVersion = checkPlatformVersion.call();

        assertTrue(sameVersion);
    }

    @Test(expected = IllegalStateException.class)
    public void check_bonita_home_without_version_throw_nice_exception() throws Exception {
        when(platform.getVersion()).thenReturn("6");
        when(platformProperties.getPlatformVersion()).thenReturn("6");
        when(bonitaHomeServer.getVersion()).thenThrow(new IllegalStateException("error"));

        checkPlatformVersion.call();
    }

    private void given_DB_Jar_Home_Versions(final String dbVersion, final String jarVersion, final String homeVersion) {
        when(platform.getVersion()).thenReturn(dbVersion);
        when(platformProperties.getPlatformVersion()).thenReturn(jarVersion);
        when(bonitaHomeServer.getVersion()).thenReturn(homeVersion);
    }

}
