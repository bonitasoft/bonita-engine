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
package org.bonitasoft.engine.api.impl.transaction.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
        givenDatabaseSchemaVersion("7.11");
        givenBinaryVersion("7.11.0");

        Boolean sameVersion = checkPlatformVersion.call();

        assertThat(sameVersion).isTrue();
    }

    @Test
    public void should_allow_to_run_snapshot_on_same_schema_version() throws Exception {
        givenDatabaseSchemaVersion("6.1");
        givenBinaryVersion("6.1.1-SNAPSHOT");

        Boolean sameVersion = checkPlatformVersion.call();

        assertThat(sameVersion).isTrue();
    }

    @Test
    public void should_not_allow_to_run_snapshot_on_different_schema_version() throws Exception {
        givenDatabaseSchemaVersion("6.0.3");
        givenBinaryVersion("6.1.1-SNAPSHOT");

        Boolean sameVersion = checkPlatformVersion.call();

        assertThat(sameVersion).isFalse();
        assertThat(checkPlatformVersion.getErrorMessage())
                .contains("Supported database schema version is <6.1> and current database schema version is <6.0.3>");
    }

    @Test
    public void should_not_allow_to_run_when_database_schema_is_in_old_format() throws Exception {
        givenDatabaseSchemaVersion("7.10.5");
        givenBinaryVersion("7.11.0");

        Boolean sameVersion = checkPlatformVersion.call();

        assertThat(sameVersion).isFalse();
        assertThat(checkPlatformVersion.getErrorMessage()).contains(
                "Supported database schema version is <7.11> and current database schema version is <7.10.5>");
    }

    @Test
    public void should_not_allow_to_run_when_schema_is_in_a_different_version_but_starts_with_same_digit()
            throws Exception {
        givenDatabaseSchemaVersion("6.1");
        givenBinaryVersion("6.10.1");

        Boolean sameVersion = checkPlatformVersion.call();

        assertThat(sameVersion).isFalse();
        assertThat(checkPlatformVersion.getErrorMessage())
                .contains("Supported database schema version is <6.10> and current database schema version is <6.1>");
    }

    private void givenBinaryVersion(String jarVersion) {
        when(platformProperties.getPlatformVersion()).thenReturn(jarVersion);
    }

    private void givenDatabaseSchemaVersion(String dbVersion) {
        when(platform.getDbSchemaVersion()).thenReturn(dbVersion);
    }

}
