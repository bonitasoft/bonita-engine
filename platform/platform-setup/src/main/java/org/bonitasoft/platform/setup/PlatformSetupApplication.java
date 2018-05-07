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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.bonitasoft.platform.exception.PlatformException;
import org.bonitasoft.platform.setup.command.CommandException;
import org.bonitasoft.platform.setup.command.HelpCommand;
import org.bonitasoft.platform.setup.command.InitCommand;
import org.bonitasoft.platform.setup.command.PlatformSetupCommand;
import org.bonitasoft.platform.setup.command.PullCommand;
import org.bonitasoft.platform.setup.command.PushCommand;
import org.bonitasoft.platform.setup.command.configure.ConfigureCommand;
import org.bonitasoft.platform.setup.command.configure.PropertyLoader;
import org.bonitasoft.platform.setup.jndi.MemoryJNDISetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Emmanuel Duchastenier
 */
@SpringBootApplication
@ComponentScan(basePackages = { "org.bonitasoft.platform.setup", "org.bonitasoft.platform.configuration",
        "org.bonitasoft.platform.version" })
public class PlatformSetupApplication {

    private final static Logger LOGGER = LoggerFactory.getLogger(PlatformSetupApplication.class);
    private HelpCommand helpCommand;
    private List<PlatformSetupCommand> commands;
    private Options options;

    @Autowired
    MemoryJNDISetup memoryJNDISetup;

    @Autowired
    PlatformSetup platformSetup;

    public static void main(String[] args) {
        new PlatformSetupApplication().run(args);
    }

    public static PlatformSetup getPlatformSetup(String[] args) throws PlatformException {
        new ConfigurationChecker(new PropertyLoader().loadProperties()).validate();
        return SpringApplication.run(PlatformSetupApplication.class, args).getBean(PlatformSetup.class);
    }

    private void run(String[] args) {
        CommandLineParser parser = new GnuParser();
        options = createOptions();
        commands = createCommands();
        helpCommand.setCommands(commands);
        CommandLine line = parseArguments(args, parser);
        configureApplication(line);
        execute(line);
    }

    private PlatformSetupCommand getCommand(CommandLine line) {
        List argList = line.getArgList();
        if (argList.isEmpty()) {
            return helpCommand;
        }
        final String commandName = argList.get(0).toString();
        for (PlatformSetupCommand platformSetupCommand : commands) {
            if (commandName.equals(platformSetupCommand.getName())) {
                return platformSetupCommand;
            }
        }
        return helpCommand;
    }

    private void configureApplication(CommandLine line) {
        Properties systemProperties = line.getOptionProperties("D");
        for (Map.Entry<Object, Object> systemProperty : systemProperties.entrySet()) {
            System.setProperty(systemProperty.getKey().toString(), systemProperty.getValue().toString());
        }
    }

    private void execute(CommandLine line) {
        try {
            getCommand(line).execute(options, line);
        } catch (CommandException e) {
            //this is an known exception we do not show any stack trace
            LOGGER.error(e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("ERROR: ", e);
            } else {
                LOGGER.error(e.getMessage());
                LOGGER.error(
                        "You might get more detailed information about the error by adding '--debug' to the command line, and run again");
            }
            // Exit code allows the calling script to catch an invalid execution:
            System.exit(1);
        }
        System.exit(0);
    }

    private CommandLine parseArguments(String[] args, CommandLineParser parser) {
        try {
            // parse the command line arguments
            return parser.parse(options, args);
        } catch (ParseException exp) {
            System.err.println("ERROR: error while parsing arguments " + exp.getMessage());
            System.exit(1);
        }
        return null;
    }

    private List<PlatformSetupCommand> createCommands() {
        List<PlatformSetupCommand> commandList = new ArrayList<>();
        commandList.add(new InitCommand());
        commandList.add(new ConfigureCommand());
        commandList.add(new PullCommand());
        commandList.add(new PushCommand());
        helpCommand = new HelpCommand();
        commandList.add(helpCommand);
        return commandList;
    }

    private Options createOptions() {
        Options options = new Options();
        Option systemPropertyOption = new Option("D",
                "specify system property to override configuration from database.properties");
        systemPropertyOption.setArgName("property=value");
        systemPropertyOption.setValueSeparator('=');
        systemPropertyOption.setArgs(2);
        options.addOption(systemPropertyOption);
        options.addOption("f", "force", false, "Force push even if critical folders will be deleted");
        return options;
    }

}
