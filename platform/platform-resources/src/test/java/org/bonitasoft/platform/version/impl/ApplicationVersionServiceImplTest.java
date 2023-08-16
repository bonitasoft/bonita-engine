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
package org.bonitasoft.platform.version.impl;

import static org.bonitasoft.platform.version.impl.ApplicationVersionServiceImpl.SQL_PLATFORM_APPLICATION_VERSION;
import static org.bonitasoft.platform.version.impl.ApplicationVersionServiceImpl.SQL_PLATFORM_APPLICATION_VERSION_UPDATE;
import static org.mockito.Mockito.*;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author laurent Leseigneur
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicationVersionServiceImplTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    JdbcTemplate jdbcTemplate;

    @InjectMocks
    @Spy
    ApplicationVersionServiceImpl applicationVersionService;

    @Test
    public void should_return_application_version_from_database() throws Exception {
        //given
        doReturn(List.of("a.b.c")).when(jdbcTemplate).queryForList(eq(SQL_PLATFORM_APPLICATION_VERSION),
                ArgumentMatchers.eq(String.class));

        //when
        final String appVersion = applicationVersionService.retrieveApplicationVersion();

        //then
        verify(jdbcTemplate, times(1)).queryForList(SQL_PLATFORM_APPLICATION_VERSION, String.class);
        Assertions.assertThat(appVersion).as("should return same version").isEqualTo("a.b.c");
    }

    @Test
    public void should_update_application_version() throws Exception {
        //given
        doReturn(1).when(jdbcTemplate).update(eq(SQL_PLATFORM_APPLICATION_VERSION_UPDATE),
                eq("1.1.1"));

        //when
        applicationVersionService.updateApplicationVersion("1.1.1");

        //then
        verify(jdbcTemplate, times(1)).update(SQL_PLATFORM_APPLICATION_VERSION_UPDATE, "1.1.1");
    }

}
