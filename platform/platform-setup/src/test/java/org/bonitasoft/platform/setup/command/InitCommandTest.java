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

package org.bonitasoft.platform.setup.command;

import static org.bonitasoft.platform.setup.command.CommandTestUtils.buildCommandLine;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import org.apache.commons.cli.Options;
import org.bonitasoft.platform.setup.PlatformSetup;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class InitCommandTest {

    @Spy
    private InitCommand initCommand;
    @Mock
    private PlatformSetup platformSetup;

    @Rule
    public RestoreSystemProperties rule = new RestoreSystemProperties();
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void before() throws Exception {
        doReturn(platformSetup).when(initCommand).getPlatformSetup(any(String[].class));
    }

    @Test
    public void should_execute_init() throws Exception {
        initCommand.execute(new Options(), buildCommandLine());

        verify(platformSetup).init();
    }

    @Test
    public void should_call_h2_confirmation() throws Exception {
        initCommand.execute(new Options(), buildCommandLine());

        verify(initCommand).askConfirmationIfH2();
    }

    @Test
    public void execute_should_not_ask_confirmation_if_dbVendor_not_H2() throws Exception {
        // given:
        System.setProperty("db.vendor", "postgres");
        System.setProperty("bdm.db.vendor", "h2");

        // when:
        initCommand.execute(new Options(), buildCommandLine());

        // then:
        verify(initCommand, times(0)).warn(anyString());
    }

    @Test
    public void execute_should_not_ask_confirmation_if_bdmDbVendor_not_H2() throws Exception {
        // given:
        System.setProperty("db.vendor", "h2");
        System.setProperty("bdm.db.vendor", "postgres");

        // when:
        initCommand.execute(new Options(), buildCommandLine());

        // then:
        verify(initCommand, times(0)).warn(anyString());
    }

    @Test
    public void execute_should_throw_CommandException_if_answer_is_not_YES() throws Exception {
        // given:
        System.setProperty("db.vendor", "h2");
        System.setProperty("bdm.db.vendor", "h2");
        doReturn("N").when(initCommand).readAnswer();

        // then:
        expectedException.expect(CommandException.class);
        expectedException.expectMessage("Exiting");

        // when:
        initCommand.execute(new Options(), buildCommandLine());
    }

    @Test
    public void execute_should_continue_if_answer_is_YES() throws Exception {
        // given:
        System.setProperty("db.vendor", "h2");
        System.setProperty("bdm.db.vendor", "h2");
        doReturn("y").when(initCommand).readAnswer();

        // when:
        initCommand.execute(new Options(), buildCommandLine());

        // then:
        verify(platformSetup).init();
    }

    @Test
    public void execute_should_continue_if_H2_YES_property_is_defined() throws Exception {
        // given:
        System.setProperty("db.vendor", "h2");
        System.setProperty("bdm.db.vendor", "h2");
        System.setProperty("h2.noconfirm", "");

        // when:
        initCommand.execute(new Options(), buildCommandLine());

        // then:
        verify(platformSetup).init();
    }

}
