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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.bonitasoft.platform.setup.command.CommandTestUtils.buildCommandLine;

import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.bonitasoft.platform.exception.PlatformException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;

/**
 * @author Baptiste Mesta
 */
public class HelpCommandTest {

    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();
    @Rule
    public SystemErrRule systemErrRule = new SystemErrRule().enableLog().muteForSuccessfulTests();
    private HelpCommand helpCommand;

    @Before
    public void before() throws Exception {
        helpCommand = new HelpCommand();
        helpCommand.setCommands(
                Arrays.asList(new PlatformSetupCommand("command1", "summary1", "description of command 1", null) {

                    @Override
                    public void execute(Options options, CommandLine commandLine) throws PlatformException {

                    }
                }, new PlatformSetupCommand("command2", "summary2", "description of command 2", null) {

                    @Override
                    public void execute(Options options, CommandLine commandLine) throws PlatformException {

                    }
                }, helpCommand));
    }

    @Test
    public void should_print_only_usage_when_no_command_is_given() throws Exception {
        //when
        try {
            helpCommand.execute(new Options(), buildCommandLine());
            fail("no option should throw exception");
        } catch (CommandException e) {
            assertThat(e.getMessage()).isEqualTo("Need to specify a command, see usage above.");
        }
        //then
        assertThat(systemOutRule.getLog()).as("contains the usage").contains("usage: setup ( command1 | command2 )");
        assertThat(systemOutRule.getLog()).as("do not contains other help").doesNotContain("command1  --  summary1");
        assertThat(systemOutRule.getLog()).as("contains how to run help")
                .contains("use `setup help` or `setup help <command>` for more details");
    }

    @Test
    public void should_print_common_help_when_asking_help_on_unknown_command() throws Exception {
        try {
            helpCommand.execute(new Options(), buildCommandLine("help", "thisIsAnUnknownCommand"));
            fail("should throw exception");
        } catch (CommandException e) {
            assertThat(e.getMessage()).isEqualTo("ERROR: no command named: thisIsAnUnknownCommand");
        }
        assertThat(systemOutRule.getLog()).as("contains the usage").contains("usage: setup ( command1 | command2 )");
        assertThat(systemOutRule.getLog()).as("contains how to run help")
                .contains("use `setup help` or `setup help <command>` for more details");
    }

    @Test
    public void should_print_only_usage_when_called_with_unknown_command() throws Exception {
        try {
            helpCommand.execute(new Options(), buildCommandLine("thisIsAnUnknownCommand"));
            fail("should throw exception");
        } catch (CommandException e) {
            assertThat(e.getMessage()).isEqualTo("ERROR: no command named: thisIsAnUnknownCommand");
        }
        assertThat(systemOutRule.getLog()).as("contains the usage").contains("usage: setup ( command1 | command2 )");
        assertThat(systemOutRule.getLog()).as("do not contains other help").doesNotContain("command1  --  summary1");
        assertThat(systemOutRule.getLog()).as("contains how to run help")
                .contains("use `setup help` or `setup help <command>` for more details");
    }

    @Test
    public void should_print_command_help_when_asking_help_on_a_command() throws Exception {
        helpCommand.execute(new Options(), buildCommandLine("help", "command2"));

        assertThat(systemOutRule.getLog()).contains("description of command 2");
        assertThat(systemOutRule.getLog()).contains("usage: setup command2");
    }

    @Test
    public void should_print_usage_with_options() throws Exception {
        helpCommand.execute(new Options().addOption("a", "anOption", false, "the option"),
                buildCommandLine("help", "command2"));

        assertThat(systemOutRule.getLog()).as("contains the usage").contains("usage: setup command2 [-a]");
        assertThat(systemOutRule.getLog()).as("contains the option").contains("-a,--anOption   the option");
    }

}
