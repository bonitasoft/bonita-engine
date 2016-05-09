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
package org.bonitasoft.engine.platform.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.PlatformCommandAPI;
import org.bonitasoft.engine.command.CommandDescriptor;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.PlatformSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PlatformCommandIT extends CommonAPIIT {

    private static PlatformCommandAPI platformCommandAPI;

    private static PlatformSession session;

    @Before
    public void before() throws BonitaException, IOException {
        session = loginOnPlatform();
        platformCommandAPI = PlatformAPIAccessor.getPlatformCommandAPI(session);
    }

    @After
    public void after() throws BonitaException {
        logoutOnPlatform(session);
    }

    //
    // @Test(expected = AlreadyExistsException.class)
    // public void commandAlreadyExistsException() throws Exception {
    // platformCommandAPI.addDependency("platformCommands", "jar".getBytes());
    // platformCommandAPI.register("platformCommand1", "command description", "implementation");
    // try {
    // platformCommandAPI.register("platformCommand1", "command description", "implementation");
    // fail();
    // } finally {
    // platformCommandAPI.unregister("platformCommand1");
    // platformCommandAPI.removeDependency("platformCommands");
    // }
    // }
    //
    // @Test(expected = CommandNotFoundException.class)
    // public void commandNotFoundException() throws Exception {
    // platformCommandAPI.get("b");
    // }
    //
    // @Test(expected = CommandNotFoundException.class)
    // public void executeUnknownPlatformCommand() throws BonitaException {
    // final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
    // parameters.put("n1", "v1");
    // platformCommandAPI.execute("com", parameters);
    // }
    //
    // @Test
    // public void executePlatformCommandWithParameters() throws BonitaException, IOException {
    // final InputStream stream = BPMRemoteTests.class.getResourceAsStream("/platformCommands-jar.bak");
    // assertNotNull(stream);
    // final byte[] byteArray = IOUtils.toByteArray(stream);
    // platformCommandAPI.addDependency("commands", byteArray);
    // platformCommandAPI.register("IntReturn", "Retrieving the integer value", "org.bonitasoft.engine.platform.command.IntergerPlatformCommand");
    // final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
    // parameters.put("int", 83);
    // final Integer actual = (Integer) platformCommandAPI.execute("IntReturn", parameters);
    // assertEquals(Integer.valueOf(83), actual);
    // platformCommandAPI.unregister("IntReturn");
    // platformCommandAPI.removeDependency("commands");
    // }
    //
    // @Test(expected = CommandParameterizationException.class)
    // public void platformCommandThrowsPlatformCommandParameterizationException() throws BonitaException, IOException {
    // final InputStream stream = BPMRemoteTests.class.getResourceAsStream("/platformCommands-jar.bak");
    // assertNotNull(stream);
    // final byte[] byteArray = IOUtils.toByteArray(stream);
    // platformCommandAPI.addDependency("commands", byteArray);
    // platformCommandAPI.register("except", "Throws ParameterizationException",
    // "org.bonitasoft.engine.platform.command.ParameterizationExceptionPlatformCommand");
    // final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
    // parameters.put("key", 83);
    // try {
    // platformCommandAPI.execute("except", parameters);
    // fail();
    // } finally {
    // platformCommandAPI.unregister("except");
    // platformCommandAPI.removeDependency("commands");
    // }
    // }
    //
    // @Test(expected = CommandExecutionException.class)
    // public void platformCommandThrowsPlatformCommandExecutionException() throws BonitaException, IOException {
    // final InputStream stream = BPMRemoteTests.class.getResourceAsStream("/platformCommands-jar.bak");
    // assertNotNull(stream);
    // final byte[] byteArray = IOUtils.toByteArray(stream);
    // platformCommandAPI.addDependency("commands", byteArray);
    // platformCommandAPI.register("except", "Throws ExecutionExceptionCommand", "org.bonitasoft.engine.platform.command.ExecutionExceptionPlatformCommand");
    // final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
    // parameters.put("key", 83);
    // try {
    // platformCommandAPI.execute("except", parameters);
    // fail();
    // } finally {
    // platformCommandAPI.unregister("except");
    // platformCommandAPI.removeDependency("commands");
    // }
    // }
    //
    @Test
    public void createPlatformCommand() throws BonitaException {
        try {
            platformCommandAPI.addDependency("commands", "jar".getBytes());
            final CommandDescriptor command = platformCommandAPI.register("testPlatformCommand", "command description", "implementation");
            assertNotNull(command);
            assertEquals("testPlatformCommand", command.getName());
            assertEquals("command description", command.getDescription());
        } finally {
            platformCommandAPI.unregister("testPlatformCommand");
            platformCommandAPI.removeDependency("commands");
        }
    }

    @Test
    public void deletePlatformCommand() throws BonitaException {
        platformCommandAPI.addDependency("commands", "jar".getBytes());
        platformCommandAPI.register("platformCommand1", "command description", "implementation");
        final CommandDescriptor command = platformCommandAPI.getCommand("platformCommand1");
        assertNotNull(command);
        platformCommandAPI.unregister("platformCommand1");
        platformCommandAPI.removeDependency("commands");
        try {
            platformCommandAPI.getCommand("platformCommand1");
        } catch (final CommandNotFoundException e) {
        }
    }
    //
    // @Test
    // public void getPlatformCommandByName() throws BonitaException {
    // final String commandName = "platformCommand1";
    // try {
    // platformCommandAPI.addDependency("commands", "jar".getBytes());
    // platformCommandAPI.register(commandName, "command description", "implementation");
    // final CommandDescriptor command = platformCommandAPI.get(commandName);
    // assertEquals("platformCommand1", command.getName());
    // } finally {
    // platformCommandAPI.unregister(commandName);
    // platformCommandAPI.removeDependency("commands");
    // }
    // }
    //
    // @Test
    // public void updateCommand() throws BonitaException {
    // try {
    // platformCommandAPI.addDependency("commands", "jar".getBytes());
    // platformCommandAPI.register("command", "old description", "implementation");
    // final CommandDescriptor oldCommand = platformCommandAPI.get("command");
    // assertEquals("command", oldCommand.getName());
    // assertEquals("old description", oldCommand.getDescription());
    //
    // final CommandUpdater commandUpdateDescriptor = new CommandUpdater();
    // commandUpdateDescriptor.setDescription("new description");
    // platformCommandAPI.update("command", commandUpdateDescriptor);
    // final CommandDescriptor newCommand = platformCommandAPI.get("command");
    // assertEquals("command", newCommand.getName());
    // assertEquals("new description", newCommand.getDescription());
    // } finally {
    // platformCommandAPI.unregister("command");
    // platformCommandAPI.removeDependency("commands");
    // }
    // }
    //
    // @Test
    // public void getCommandsWithCommandCriterion_NAME_ASC() throws BonitaException {
    // try {
    // platformCommandAPI.addDependency("commands", "jar".getBytes());
    // platformCommandAPI.register("platformCommand2", "command description", "implementation");
    // platformCommandAPI.register("platformCommand3", "command description", "implementation");
    // platformCommandAPI.register("platformCommand1", "command description", "implementation");
    //
    // final List<CommandDescriptor> commandsPage1 = platformCommandAPI.getCommands(0, 2, CommandCriterion.NAME_ASC);
    // assertEquals(2, commandsPage1.size());
    // assertEquals("platformCommand1", commandsPage1.get(0).getName());
    // assertEquals("platformCommand2", commandsPage1.get(1).getName());
    //
    // final List<CommandDescriptor> commandsPage2 = platformCommandAPI.getCommands(2, 10, CommandCriterion.NAME_ASC);
    // assertEquals(1, commandsPage2.size());
    // assertEquals("platformCommand3", commandsPage2.get(0).getName());
    // } finally {
    // platformCommandAPI.unregister("platformCommand2");
    // platformCommandAPI.unregister("platformCommand3");
    // platformCommandAPI.unregister("platformCommand1");
    // platformCommandAPI.removeDependency("commands");
    // }
    // }
    //
    // @Test
    // public void getCommandsWithCommandCriterion_NAME_DESC() throws BonitaException {
    // try {
    // platformCommandAPI.addDependency("commands", "jar".getBytes());
    // platformCommandAPI.register("platformCommand2", "command description", "implementation");
    // platformCommandAPI.register("platformCommand3", "command description", "implementation");
    // platformCommandAPI.register("platformCommand1", "command description", "implementation");
    //
    // final List<CommandDescriptor> commandsPage = platformCommandAPI.getCommands(0, 3, CommandCriterion.NAME_DESC);
    // assertEquals(3, commandsPage.size());
    // assertEquals("platformCommand3", commandsPage.get(0).getName());
    // assertEquals("platformCommand2", commandsPage.get(1).getName());
    // assertEquals("platformCommand1", commandsPage.get(2).getName());
    // } finally {
    // platformCommandAPI.unregister("platformCommand2");
    // platformCommandAPI.unregister("platformCommand3");
    // platformCommandAPI.unregister("platformCommand1");
    // platformCommandAPI.removeDependency("commands");
    // }
    // }
}
