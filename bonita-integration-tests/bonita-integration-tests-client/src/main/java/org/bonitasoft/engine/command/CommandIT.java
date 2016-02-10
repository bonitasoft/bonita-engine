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
package org.bonitasoft.engine.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.TestWithTechnicalUser;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

public class CommandIT extends TestWithTechnicalUser {

    @Test(expected = AlreadyExistsException.class)
    public void commandAlreadyExistsException() throws Exception {
        getCommandAPI().addDependency("commands", "jar".getBytes());
        getCommandAPI().register("command1", "command description", "implementation");
        try {
            getCommandAPI().register("command1", "command description", "implementation");
        } finally {
            getCommandAPI().unregister("command1");
            getCommandAPI().removeDependency("commands");
        }
    }

    @Test(expected = CommandNotFoundException.class)
    public void commandNotFoundException() throws BonitaException {
        getCommandAPI().getCommand("b");
    }

    @Test(expected = CommandNotFoundException.class)
    public void executeUnknownCommand() throws BonitaException {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("n1", "v1");
        getCommandAPI().execute("com", parameters);
    }

    @Test
    public void executeCommandWithParameters() throws BonitaException, IOException {
        final InputStream stream = CommonAPIIT.class.getResourceAsStream("/commands-jar.bak");
        assertNotNull(stream);
        final byte[] byteArray = IOUtils.toByteArray(stream);
        stream.close();
        getCommandAPI().addDependency("commands", byteArray);
        getCommandAPI().register("intReturn", "Retrieving the integer value", "org.bonitasoft.engine.command.IntergerCommand");
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("int", 83);
        final Integer actual = (Integer) getCommandAPI().execute("intReturn", parameters);
        assertEquals(Integer.valueOf(83), actual);
        getCommandAPI().unregister("intReturn");
        getCommandAPI().removeDependency("commands");
    }

    @Test(expected = CommandParameterizationException.class)
    public void commandThrowsCommandParameterizationException() throws BonitaException, IOException {
        final InputStream stream = CommonAPIIT.class.getResourceAsStream("/commands-jar.bak");
        assertNotNull(stream);
        final byte[] byteArray = IOUtils.toByteArray(stream);
        stream.close();
        getCommandAPI().addDependency("commands", byteArray);
        getCommandAPI().register("except", "Throws ParameterizationException", "org.bonitasoft.engine.command.ParameterizationExceptionCommand");
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("key", 83);
        try {
            getCommandAPI().execute("except", parameters);
        } finally {
            getCommandAPI().unregister("except");
            getCommandAPI().removeDependency("commands");
        }
    }

    @Test(expected = CommandExecutionException.class)
    public void commandThrowsCommandExecutionException() throws BonitaException, IOException {
        final InputStream stream = CommonAPIIT.class.getResourceAsStream("/commands-jar.bak");
        assertNotNull(stream);
        final byte[] byteArray = IOUtils.toByteArray(stream);
        stream.close();
        getCommandAPI().addDependency("commands", byteArray);
        getCommandAPI().register("except", "Throws ExecutionExceptionCommand", "org.bonitasoft.engine.command.ExecutionExceptionCommand");
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("key", 83);
        try {
            getCommandAPI().execute("except", parameters);
        } finally {
            getCommandAPI().unregister("except");
            getCommandAPI().removeDependency("commands");
        }
    }

    @Test
    public void createCommand() throws BonitaException {
        try {
            getCommandAPI().addDependency("commands", "jar".getBytes());
            final CommandDescriptor command = getCommandAPI().register("command1", "command description", "implementation");
            assertNotNull(command);
            assertEquals("command1", command.getName());
            assertEquals("command description", command.getDescription());
        } finally {
            getCommandAPI().unregister("command1");
            getCommandAPI().removeDependency("commands");
        }
    }

    @Test
    public void deleteCommand() throws BonitaException {
        // delete command by name
        getCommandAPI().addDependency("commands", "jar".getBytes());
        CommandDescriptor command = getCommandAPI().register("command1", "command description", "implementation");
        assertNotNull(command);
        getCommandAPI().unregister("command1");
        try {
            getCommandAPI().getCommand("command1");
            fail("Command does not exist anymore");
        } catch (final CommandNotFoundException e) {
            getCommandAPI().removeDependency("commands");
        }
        // delete command by id
        getCommandAPI().addDependency("commands", "jar".getBytes());
        command = getCommandAPI().register("command1", "command description", "implementation");
        assertNotNull(command);
        getCommandAPI().unregister(command.getId());
        try {
            getCommandAPI().getCommand("command1");
            fail("Command does not exist anymore");
        } catch (final CommandNotFoundException e) {
            getCommandAPI().removeDependency("commands");
        }
    }

    @Test
    public void getCommandByName() throws BonitaException {
        final String commandName = "command1";
        try {
            getCommandAPI().addDependency("commands", "jar".getBytes());
            getCommandAPI().register(commandName, "command description", "implementation");
            final CommandDescriptor command = getCommandAPI().getCommand(commandName);
            assertEquals("command1", command.getName());
        } finally {
            getCommandAPI().unregister(commandName);
            getCommandAPI().removeDependency("commands");
        }
    }

    @Test
    public void updateCommand() throws BonitaException {
        getCommandAPI().addDependency("commands", "jar".getBytes());
        final CommandDescriptor oldCommand = getCommandAPI().register("command", "old description", "implementation");
        final long commandId = oldCommand.getId();
        assertEquals("command", oldCommand.getName());
        assertEquals("old description", oldCommand.getDescription());
        // test update name specified command
        final CommandUpdater commandUpdateDescriptor = new CommandUpdater();
        commandUpdateDescriptor.setDescription("new description");
        getCommandAPI().update("command", commandUpdateDescriptor);
        CommandDescriptor newCommand = getCommandAPI().getCommand("command");
        assertEquals(commandId, newCommand.getId());
        assertEquals("command", newCommand.getName());
        assertEquals("new description", newCommand.getDescription());

        // test update id specified command
        commandUpdateDescriptor.setName("updatedCommandName for the id specified command");
        commandUpdateDescriptor.setDescription("updatedDescription for the id specified command");
        getCommandAPI().update(commandId, commandUpdateDescriptor);
        newCommand = getCommandAPI().get(commandId);
        assertEquals("updatedCommandName for the id specified command", newCommand.getName());
        assertEquals("updatedDescription for the id specified command", newCommand.getDescription());

        getCommandAPI().unregister(commandId);
        getCommandAPI().removeDependency("commands");
    }

    @Test
    public void getCommandsWithCommandCriterion() throws BonitaException {
        try {
            getCommandAPI().addDependency("commands", "jar".getBytes());
            getCommandAPI().register("aaaCommand2", "command description", "implementation");
            getCommandAPI().register("aaaCommand3", "command description", "implementation");
            getCommandAPI().register("aaaCommand1", "command description", "implementation");

            final List<CommandDescriptor> commandsPage1 = getCommandAPI().getAllCommands(0, 2, CommandCriterion.NAME_ASC);
            assertEquals(2, commandsPage1.size());
            assertEquals("aaaCommand1", commandsPage1.get(0).getName());
            assertEquals("aaaCommand2", commandsPage1.get(1).getName());

            final List<CommandDescriptor> commandsPage2 = getCommandAPI().getAllCommands(2, 1, CommandCriterion.NAME_ASC);
            assertEquals(1, commandsPage2.size());
            assertEquals("aaaCommand3", commandsPage2.get(0).getName());
        } finally {
            getCommandAPI().unregister("aaaCommand2");
            getCommandAPI().unregister("aaaCommand3");
            getCommandAPI().unregister("aaaCommand1");
            getCommandAPI().removeDependency("commands");
        }
    }

    @Test
    public void testGetCommands() throws BonitaException {
        // Create and register Commands as System is false
        getCommandAPI().addDependency("commands", "jar".getBytes());
        getCommandAPI().register("command1", "GetCommands description", "implementation");
        getCommandAPI().register("command2", "GetCommands description", "implementation");
        getCommandAPI().register("command3", "GetCommands description", "implementation");

        // Search and test the result
        final List<CommandDescriptor> commands = getCommandAPI().getUserCommands(0, 3, CommandCriterion.NAME_ASC);
        assertEquals(3, commands.size());
        assertEquals(false, commands.get(0).isSystemCommand());
        assertEquals(false, commands.get(1).isSystemCommand());
        assertEquals(false, commands.get(2).isSystemCommand());

        // Clean Commands
        getCommandAPI().unregister("command1");
        getCommandAPI().unregister("command2");
        getCommandAPI().unregister("command3");
        getCommandAPI().removeDependency("commands");
    }

    @Test
    public void getCommandById() throws BonitaException {
        final String commandName = "command1";
        try {
            getCommandAPI().addDependency("commands", "jar".getBytes());
            final CommandDescriptor registeredCommand = getCommandAPI().register(commandName, "command description", "implementation");
            final CommandDescriptor command = getCommandAPI().get(registeredCommand.getId());
            assertEquals("command1", command.getName());
        } finally {
            getCommandAPI().unregister(commandName);
            getCommandAPI().removeDependency("commands");
        }
    }

    @Test
    public void searchCommands() throws BonitaException {
        final CommandAPI commandAPI = getCommandAPI();
        commandAPI.addDependency("commands", "jar".getBytes());
        final CommandDescriptor command1 = commandAPI.register("testCommand1", "SearchCommands description1", "implementation");
        final CommandDescriptor command2 = commandAPI.register("testCommand2", "GetCommands description2", "implementation");
        final CommandDescriptor command3 = commandAPI.register("testCommand3", "GetCommands description3", "implementation");
        try {
            // search paging with order ASC
            SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 2);
            builder.searchTerm("testCommand");
            builder.sort(CommandSearchDescriptor.NAME, Order.ASC);
            SearchResult<CommandDescriptor> searchCommands = commandAPI.searchCommands(builder.done());
            assertNotNull(searchCommands);
            assertEquals(3, searchCommands.getCount());
            List<CommandDescriptor> commands = searchCommands.getResult();
            assertEquals(2, commands.size());
            assertEquals(command1, commands.get(0));
            assertEquals(command2, commands.get(1));

            builder = new SearchOptionsBuilder(2, 2);
            builder.searchTerm("testCommand");
            builder.sort(CommandSearchDescriptor.NAME, Order.ASC);
            searchCommands = commandAPI.searchCommands(builder.done());
            assertNotNull(searchCommands);
            assertEquals(3, searchCommands.getCount());
            commands = searchCommands.getResult();
            assertEquals(1, commands.size());
            assertEquals(command3, commands.get(0));

            builder = new SearchOptionsBuilder(3, 2);
            builder.searchTerm("testCommand");
            builder.sort(CommandSearchDescriptor.NAME, Order.ASC);
            searchCommands = commandAPI.searchCommands(builder.done());
            assertEquals(0, searchCommands.getResult().size());

            // test Desc
            builder = new SearchOptionsBuilder(0, 2);
            builder.searchTerm("testCommand");
            builder.sort(CommandSearchDescriptor.NAME, Order.DESC);
            searchCommands = commandAPI.searchCommands(builder.done());
            assertNotNull(searchCommands);
            assertEquals(3, searchCommands.getCount());
            commands = searchCommands.getResult();
            assertEquals(2, commands.size());
            assertEquals(command3, commands.get(0));
            assertEquals(command2, commands.get(1));

            // test search with filter
            builder = new SearchOptionsBuilder(0, 10);
            builder.filter(CommandSearchDescriptor.NAME, "testCommand1");
            searchCommands = commandAPI.searchCommands(builder.done());
            assertNotNull(searchCommands);
            assertEquals(1, searchCommands.getCount());
            commands = searchCommands.getResult();
            assertEquals(1, commands.size());
            assertEquals(command1, commands.get(0));

            // test search with term
            builder = new SearchOptionsBuilder(0, 10);
            builder.searchTerm("testCommand");
            builder.sort(CommandSearchDescriptor.NAME, Order.ASC);
            searchCommands = commandAPI.searchCommands(builder.done());
            assertNotNull(searchCommands);
            assertEquals(3, searchCommands.getCount());
            commands = searchCommands.getResult();
            assertEquals(3, commands.size());
            assertEquals(command1, commands.get(0));
            assertEquals(command2, commands.get(1));
            assertEquals(command3, commands.get(2));

        } finally {
            // Clean Commands
            commandAPI.unregister("testCommand1");
            commandAPI.unregister("testCommand2");
            commandAPI.unregister("testCommand3");
            commandAPI.removeDependency("commands");
        }
    }

    @Cover(classes = { SearchOptionsBuilder.class, CommandAPI.class }, concept = BPMNConcept.OTHERS, keywords = { "SearchCommands", "Apostrophe" }, jira = "ENGINE-366")
    @Test
    public void searchCommandsWithApostrophe() throws BonitaException {
        getCommandAPI().addDependency("commands", "jar".getBytes());
        final CommandDescriptor command1 = getCommandAPI().register("'command'1", "SearchCommands description1", "implementation");
        final CommandDescriptor command2 = getCommandAPI().register("command2", "'command'1", "implementation");
        final CommandDescriptor command3 = getCommandAPI().register("command3", "'SearchCommands description1", "command'tation");

        // test search with filter
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(CommandSearchDescriptor.NAME, Order.ASC);
        builder.searchTerm("'");
        final CommandAPI commandAPI = getCommandAPI();
        final SearchResult<CommandDescriptor> searchCommands = commandAPI.searchCommands(builder.done());
        assertNotNull(searchCommands);
        assertEquals(3, searchCommands.getCount());
        final List<CommandDescriptor> commands = searchCommands.getResult();
        assertEquals(3, commands.size());
        assertEquals(command1, commands.get(0));
        assertEquals(command2, commands.get(1));
        assertEquals(command3, commands.get(2));

        // Clean Commands
        getCommandAPI().unregister("'command'1");
        getCommandAPI().unregister("command2");
        getCommandAPI().unregister("command3");
        getCommandAPI().removeDependency("commands");
    }

    @Test
    public void executeCommandById() throws BonitaException, IOException {
        final InputStream stream = CommonAPIIT.class.getResourceAsStream("/commands-jar.bak");
        assertNotNull(stream);
        final byte[] byteArray = IOUtils.toByteArray(stream);
        stream.close();
        getCommandAPI().addDependency("commands", byteArray);
        final CommandDescriptor command = getCommandAPI()
                .register("intReturn", "Retrieving the integer value", "org.bonitasoft.engine.command.IntergerCommand");

        final CommandDescriptor commandById = getCommandAPI().get(command.getId());
        assertEquals(commandById.getId(), command.getId());

        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("int", 83);
        final Integer actual = (Integer) getCommandAPI().execute(commandById.getId(), parameters);
        assertEquals(Integer.valueOf(83), actual);

        getCommandAPI().unregister("intReturn");
        getCommandAPI().removeDependency("commands");
    }

    @Test(expected = BonitaRuntimeException.class)
    public void executeCommandThrowsANPE() throws BonitaException, IOException {
        final InputStream stream = CommonAPIIT.class.getResourceAsStream("/npe-command-jar.bak");
        assertNotNull(stream);
        final byte[] byteArray = IOUtils.toByteArray(stream);
        stream.close();
        getCommandAPI().addDependency("commands", byteArray);
        getCommandAPI().register("NPEReturns", "Throws a NPE", "org.bonitasoft.engine.command.NPECommand");
        try {
            getCommandAPI().execute("NPEReturns", null);
        } finally {
            getCommandAPI().unregister("NPEReturns");
            getCommandAPI().removeDependency("commands");
        }
    }

}
