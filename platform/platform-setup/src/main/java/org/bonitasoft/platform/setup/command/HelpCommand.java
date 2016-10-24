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
        super("help", "Display the help", "Display the help");
    }

    @Override
    public void execute(Options options, String... args) throws PlatformException, CommandException {
        if (args.length == 0) {
            printCommonHelp(options);
            throw new CommandException("Need to specify a command, see usage above.");
        } else if (getName().equals(args[0])) {
            if (args.length > 1) {
                printHelpFor(options, args[1]);
            } else {
                printCommonHelp(options);
            }
        } else {
            printCommonHelp(options);
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

    @Override
    public boolean isDisplayed() {
        return false;
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
        printUsage("setup " + getCommandNames(), options, "use `setup help <command>` for more details on a command" + lineSeparator());
        printGlobalHelp();
        printCommandsUsage();
    }

    private void printCommandsUsage() {
        StringBuilder usage = new StringBuilder();
        usage.append(lineSeparator());
        usage.append("Available commands:").append(lineSeparator()).append(lineSeparator());
        for (PlatformSetupCommand command : commands) {
            if (!command.isDisplayed()) {
                continue;
            }
            usage.append(" ").append(command.getName()).append("  --  ").append(command.getSummary()).append(lineSeparator());
            usage.append(lineSeparator());
        }
        System.out.println(usage.toString());
    }

    private String getCommandNames() {
        List<String> names = new ArrayList<>(commands.size());
        for (PlatformSetupCommand command : commands) {
            if (command.isDisplayed()) {
                names.add(command.getName());
            }
        }
        return "( " + StringUtils.join(names.iterator(), " | ") + " )";
    }

    private void printHelpFor(Options options, PlatformSetupCommand command) {
        printUsage("setup " + command.getName(), options, lineSeparator());
        printCommandUsage(command);
    }

    private void printCommandUsage(PlatformSetupCommand command) {
        System.out.println(System.lineSeparator() +
                " " + command.getName() + "  --  " + command.getSummary() + System.lineSeparator() +
                System.lineSeparator() +
                "  " + command.getDescription().replace(System.lineSeparator(), System.lineSeparator() + "  ") + System.lineSeparator() +
                System.lineSeparator());
    }

    private void printGlobalHelp() {
        System.out.println(CommandUtils.getFileContentFromClassPath("global_usage.txt"));
    }

    private void printUsage(String cmdLineSyntax, Options options, String footer) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(cmdLineSyntax, lineSeparator() + "Available options:", options, footer, true);
    }
}
