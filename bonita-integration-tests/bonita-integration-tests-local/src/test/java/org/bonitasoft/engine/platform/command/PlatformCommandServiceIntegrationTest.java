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
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.platform.command.model.SPlatformCommand;
import org.bonitasoft.engine.platform.command.model.SPlatformCommandBuilderFactory;
import org.bonitasoft.engine.platform.command.model.SPlatformCommandUpdateBuilderFactory;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Test;

/**
 * @author Zhang Bole
 */
public class PlatformCommandServiceIntegrationTest extends CommonBPMServicesTest {

    private static PlatformCommandService platformCommandService;

    public PlatformCommandServiceIntegrationTest() {
        platformCommandService = getPlatformAccessor().getPlatformCommandService();
    }

    @Test(expected = SPlatformCommandAlreadyExistsException.class)
    public void testSPlatformCommandAlreadyExistsException() throws Exception {
        getTransactionService().begin();
        final SPlatformCommand sPlatformCommand = BuilderFactory.get(SPlatformCommandBuilderFactory.class)
                .createNewInstance("createCommand", "this is a command", "command implementation").done();
        platformCommandService.create(sPlatformCommand);
        try {
            platformCommandService.create(sPlatformCommand);
        } finally {
            platformCommandService.delete("createCommand");
            getTransactionService().complete();
        }
    }

    @Test(expected = SPlatformCommandNotFoundException.class)
    public void testSPlatformCommandNotFoundException() throws Exception {
        getTransactionService().begin();
        try {
            platformCommandService.getPlatformCommand("a");
        } finally {
            getTransactionService().complete();
        }
    }

    @Test
    public void testCreatePlatformCommand() throws Exception {
        getTransactionService().begin();
        final SPlatformCommand command1 = BuilderFactory.get(SPlatformCommandBuilderFactory.class)
                .createNewInstance("createCommand", "this is a command", "command implementation").done();
        platformCommandService.create(command1);
        final SPlatformCommand command2 = platformCommandService.getPlatformCommand("createCommand");
        assertNotNull("can't find the category after adding it", command2);
        assertEquals("can't retrieve the same category", command1.getName(), command2.getName());
        assertEquals("can't retrieve the same category", command1.getId(), command2.getId());
        platformCommandService.delete("createCommand");
        getTransactionService().complete();
    }

    @Test(expected = SPlatformCommandNotFoundException.class)
    public void testDeletePlatformCommand() throws Exception {
        getTransactionService().begin();
        final SPlatformCommand sPlatformCommand = BuilderFactory.get(SPlatformCommandBuilderFactory.class)
                .createNewInstance("testCommandDelete", "this is a command", "command implementation").done();
        platformCommandService.create(sPlatformCommand);
        platformCommandService.delete("testCommandDelete");
        try {
            platformCommandService.getPlatformCommand("testCommandDelete");
        } finally {
            getTransactionService().complete();
        }
    }

    @Test
    public void testDeleteAll() throws Exception {
        getTransactionService().begin();
        final SPlatformCommand command1 = BuilderFactory.get(SPlatformCommandBuilderFactory.class)
                .createNewInstance("createCommand1", "this is a command", "command implementation").done();
        final SPlatformCommand command2 = BuilderFactory.get(SPlatformCommandBuilderFactory.class)
                .createNewInstance("createCommand2", "this is a command", "command implementation").done();
        final SPlatformCommand command3 = BuilderFactory.get(SPlatformCommandBuilderFactory.class)
                .createNewInstance("createCommand3", "this is a command", "command implementation").done();
        platformCommandService.create(command1);
        platformCommandService.create(command2);
        platformCommandService.create(command3);
        final QueryOptions queryOptions = new QueryOptions(0, 500, SPlatformCommand.class, "name", OrderByType.ASC);
        final List<SPlatformCommand> commands1 = platformCommandService.getPlatformCommands(queryOptions);
        assertEquals(3, commands1.size());

        platformCommandService.deleteAll();
        final List<SPlatformCommand> commands2 = platformCommandService.getPlatformCommands(queryOptions);
        assertTrue(commands2.isEmpty());
        getTransactionService().complete();
    }

    @Test
    public void testUpdatePlatformCommand() throws Exception {
        getTransactionService().begin();
        final SPlatformCommand oldCommand = BuilderFactory.get(SPlatformCommandBuilderFactory.class)
                .createNewInstance("old", "this is an old command", "command implementation").done();
        platformCommandService.create(oldCommand);
        assertEquals("old", oldCommand.getName());
        assertEquals("this is an old command", oldCommand.getDescription());

        final String commandName = "new";
        final EntityUpdateDescriptor updateDescriptor = BuilderFactory.get(SPlatformCommandUpdateBuilderFactory.class).createNewInstance().updateName(commandName)
                .updateDescription("this is a new command").done();
        platformCommandService.update(oldCommand, updateDescriptor);
        final SPlatformCommand newCommand = platformCommandService.getPlatformCommand(commandName);
        assertEquals("new", newCommand.getName());
        assertEquals("this is a new command", newCommand.getDescription());
        platformCommandService.delete(commandName);
        getTransactionService().complete();
    }

    @Test
    public void testGetPlatformCommandByName() throws Exception {
        getTransactionService().begin();
        final SPlatformCommand sPlatformCommand = BuilderFactory.get(SPlatformCommandBuilderFactory.class)
                .createNewInstance("commandOne", "this is a command", "command implementation").done();
        platformCommandService.create(sPlatformCommand);
        final SPlatformCommand command = platformCommandService.getPlatformCommand("commandOne");
        assertEquals("commandOne", command.getName());
        assertEquals("this is a command", command.getDescription());
        platformCommandService.delete("commandOne");
        getTransactionService().complete();
    }

    @Test
    public void testGetPlatformCommandsWithCriterion() throws Exception {
        getTransactionService().begin();
        final SPlatformCommand sPlatformCommand1 = BuilderFactory.get(SPlatformCommandBuilderFactory.class)
                .createNewInstance("commandB", "this is command1", "command implementation").done();
        final SPlatformCommand sPlatformCommand2 = BuilderFactory.get(SPlatformCommandBuilderFactory.class)
                .createNewInstance("commandC", "this is command2", "command implementation").done();
        final SPlatformCommand sPlatformCommand3 = BuilderFactory.get(SPlatformCommandBuilderFactory.class)
                .createNewInstance("commandA", "this is command3", "command implementation").done();
        platformCommandService.create(sPlatformCommand1);
        platformCommandService.create(sPlatformCommand2);
        platformCommandService.create(sPlatformCommand3);

        final QueryOptions queryOptions = new QueryOptions(0, 5, SPlatformCommand.class, "name", OrderByType.ASC);
        final List<SPlatformCommand> commands = platformCommandService.getPlatformCommands(queryOptions);
        assertEquals(3, commands.size());
        assertEquals("commandA", commands.get(0).getName());
        assertEquals("commandB", commands.get(1).getName());
        assertEquals("commandC", commands.get(2).getName());

        platformCommandService.deleteAll();
        getTransactionService().complete();
    }

}
