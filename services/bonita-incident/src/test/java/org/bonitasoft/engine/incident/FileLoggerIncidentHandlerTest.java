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
package org.bonitasoft.engine.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.home.TenantStorage;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FileLoggerIncidentHandlerTest {

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();
    @Mock
    private BonitaHomeServer bonitaHomeServer;
    @Mock
    private TenantStorage tenantStorage;

    String INCIDENT_LOG_PATH = "log/";

    @Before
    public void init() throws IOException {
        Files.createDirectories(Path.of(INCIDENT_LOG_PATH));
        System.setProperty("INCIDENT_LOG_PATH", INCIDENT_LOG_PATH);
    }

    @After
    public void clean() throws IOException {
        org.bonitasoft.engine.io.IOUtil.deleteDir(new File(INCIDENT_LOG_PATH));
    }

    @Test
    public void should_write_into_file_when_handle_incident() throws Exception {
        //Given
        long tenantId = 1;

        String cause = "an unexpected exception";
        String handlingFailure = "unable to handle failure";

        String recovery = "recovery";
        String description = "test";
        final Incident incident = new Incident(description, recovery, new Exception(cause),
                new Exception(handlingFailure));
        FileLoggerIncidentHandler fileLoggerIncidentHandler = new FileLoggerIncidentHandler();
        //When
        fileLoggerIncidentHandler.handle(tenantId, incident);

        String expectedDescription = "An incident on tenant id 1 occurred: " + description;
        String expectedRecoveryProcedureMessage = "Procedure to recover: " + recovery;

        // as defined in logback.xml all logs are log in system out so we  check that incident are logged into console also.
        assertThat(systemOutRule.getLog()).contains(expectedDescription);
        assertThat(systemOutRule.getLog()).contains(cause);
        assertThat(systemOutRule.getLog()).contains(handlingFailure);
        assertThat(systemOutRule.getLog()).contains(expectedRecoveryProcedureMessage);

        //  as defined in logback.xml logs marked with INCIDENT should be logged also in a specific file.
        String log = org.bonitasoft.engine.io.IOUtil.read(new File(INCIDENT_LOG_PATH + "/incidents.log"));

        assertThat(log).contains(expectedDescription);
        assertThat(log).contains(cause);
        assertThat(log).contains(handlingFailure);
        assertThat(log).contains(expectedRecoveryProcedureMessage);

    }

}
