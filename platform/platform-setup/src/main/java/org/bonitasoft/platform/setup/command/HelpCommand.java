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

import static java.lang.System.lineSeparator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.platform.exception.PlatformException;

/**
 * @author Baptiste Mesta
 */
public class HelpCommand extends PlatformSetupCommand {

    private List<PlatformSetupCommand> commands;

    public HelpCommand() {
        super("help", "Display the help", "Display the help", null);
    }

    @Override
    public void execute(Options options, CommandLine commandLine) throws PlatformException, CommandException {
        String[] args = commandLine.getArgs();
        if (args.length == 0) {
            printUsage(options);
            throw new CommandException("Need to specify a command, see usage above.");
        } else if (getName().equals(args[0])) {
            if (args.length > 1) {
                printHelpFor(options, args[1]);
            } else {
                printCommonHelp(options);
            }
        } else {
            printUsage(options);
            throw new CommandException("ERROR: no command named: " + args[0]);
        }
    }

    private void printHelpFor(Options options, String commandNameForHelp) throws CommandException {
        PlatformSetupCommand platformSetupCommand = getCommand(commandNameForHelp);
        if (platformSetupCommand == null) {
            printCommonHelp(options);
            throw new CommandException("ERROR: no command named: " + commandNameForHelp);
        }
        printHelpFor(options, platformSetupCommand);
    }

    public void setCommands(List<PlatformSetupCommand> commands) {
        this.commands = commands;
    }

    private PlatformSetupCommand getCommand(String commandName) {
        PlatformSetupCommand command = null;
        for (PlatformSetupCommand platformSetupCommand : commands) {
            if (commandName.equals(platformSetupCommand.getName())) {
                command = platformSetupCommand;
                break;
            }
        }
        return command;
    }

    private void printCommonHelp(Options options) {
        printUsage(options);
        printGlobalHelpHeader();
        printCommandsUsage();
        printGlobalHelpFooter();
    }

    private void printUsage(Options options) {
        List<String> names = new ArrayList<>(commands.size());
        for (PlatformSetupCommand command : commands) {
            if (!command.equals(this)) {
                names.add(command.getName());
            }
        }
        String footer = "use `setup help` or `setup help <command>` for more details" + lineSeparator();
        printUsageFor(options, "( " + StringUtils.join(names.iterator(), " | ") + " )", footer);
    }

    private void printCommandsUsage() {
        StringBuilder usage = new StringBuilder();
        usage.append(lineSeparator());
        usage.append("Available commands:").append(lineSeparator()).append(lineSeparator());
        for (PlatformSetupCommand command : commands) {
            usage.append(" ").append(command.getName()).append("  --  ").append(command.getSummary())
                    .append(lineSeparator());
        }
        System.out.println(usage.toString());
    }

    private void printHelpFor(Options options, PlatformSetupCommand command) {
        printUsageFor(options, command.getName(), lineSeparator());
        printCommandDescriptionHeader(command);
        printCommandUsage(command);
        printCommandDescriptionFooter(command);
    }

    private void printCommandDescriptionHeader(PlatformSetupCommand command) {
        if (command.getDescriptionHeader() != null) {
            System.out.println("  " + command.getDescriptionHeader().replace(lineSeparator(), lineSeparator() + "  "));
        }
    }

    private void printCommandDescriptionFooter(PlatformSetupCommand command) {
        if (command.getDescriptionFooter() != null) {
            System.out.println("  " + command.getDescriptionFooter().replace(lineSeparator(), lineSeparator() + "  "));
        }
    }

    private void printUsageFor(Options options, String commandName, String footer) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("setup " + commandName, lineSeparator() + "Available options:", options, footer, true);
    }

    private void printCommandUsage(PlatformSetupCommand command) {
        System.out.println(lineSeparator() +
                " " + command.getName() + "  --  " + command.getSummary() + lineSeparator());
    }

    private void printGlobalHelpHeader() {
        System.out.println(CommandUtils.getFileContentFromClassPath("global_usage_header.txt"));
    }

    private void printGlobalHelpFooter() {
        System.out.println(CommandUtils.getFileContentFromClassPath("global_usage_footer.txt"));
    }

}
