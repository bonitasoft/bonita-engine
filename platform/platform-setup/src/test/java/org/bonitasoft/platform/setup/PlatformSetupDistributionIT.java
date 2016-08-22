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

package org.bonitasoft.platform.setup;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Baptiste Mesta
 */
public class PlatformSetupDistributionIT {

    private File distFolder;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void before() throws Exception {
        distFolder = temporaryFolder.newFolder();
        PlatformSetupTestUtils.extractDistributionTo(distFolder);
    }

    @Test
    public void setupSh_should_work_with_init_on_h2() throws Exception {
        //given
        CommandLine oCmdLine = PlatformSetupTestUtils.createCommandLine();
        oCmdLine.addArgument("init");
        DefaultExecutor executor = PlatformSetupTestUtils.createExecutor(distFolder);
        //when
        int iExitValue = executor.execute(oCmdLine);
        //then
        assertThat(iExitValue).isEqualTo(0);
        Connection jdbcConnection = PlatformSetupTestUtils.getJdbcConnection(distFolder);
        Statement statement = jdbcConnection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS nb FROM CONFIGURATION");
        resultSet.next();
        assertThat(resultSet.getInt("nb")).isEqualTo(38);

    }

    @Test
    public void setupSh_should_have_error_with_no_argument() throws Exception {
        //given
        CommandLine oCmdLine = PlatformSetupTestUtils.createCommandLine();
        DefaultExecutor oDefaultExecutor = PlatformSetupTestUtils.createExecutor(distFolder);
        oDefaultExecutor.setExitValue(1);
        //when
        int iExitValue = oDefaultExecutor.execute(oCmdLine);
        //then
        assertThat(iExitValue).isEqualTo(1);
    }

}
