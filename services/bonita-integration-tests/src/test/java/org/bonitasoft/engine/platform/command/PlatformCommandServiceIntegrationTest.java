package org.bonitasoft.engine.platform.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.platform.command.model.SPlatformCommand;
import org.bonitasoft.engine.platform.command.model.SPlatformCommandBuilder;
import org.bonitasoft.engine.platform.command.model.SPlatformCommandBuilderAccessor;
import org.bonitasoft.engine.platform.command.model.SPlatformCommandCriterion;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Test;

/**
 * @author Zhang Bole
 */
public class PlatformCommandServiceIntegrationTest extends CommonServiceTest {

    private static PlatformCommandService platformCommandService;

    private static SPlatformCommandBuilderAccessor platformCommandBuilderAccessor;

    static {
        platformCommandService = getServicesBuilder().buildPlatformCommandService();
        platformCommandBuilderAccessor = getServicesBuilder().buildSPlatformCommandBuilderAccessor();
    }

    public PlatformCommandServiceIntegrationTest() {
        super();
    }

    @Test(expected = SPlatformCommandAlreadyExistsException.class)
    public void testSPlatformCommandAlreadyExistsException() throws Exception {
        getTransactionService().begin();
        final SPlatformCommand sPlatformCommand = platformCommandBuilderAccessor.getSPlatformCommandBuilder()
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
        final SPlatformCommand command1 = platformCommandBuilderAccessor.getSPlatformCommandBuilder()
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
        final SPlatformCommand sPlatformCommand = platformCommandBuilderAccessor.getSPlatformCommandBuilder()
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
        final SPlatformCommand command1 = platformCommandBuilderAccessor.getSPlatformCommandBuilder()
                .createNewInstance("createCommand1", "this is a command", "command implementation").done();
        final SPlatformCommand command2 = platformCommandBuilderAccessor.getSPlatformCommandBuilder()
                .createNewInstance("createCommand2", "this is a command", "command implementation").done();
        final SPlatformCommand command3 = platformCommandBuilderAccessor.getSPlatformCommandBuilder()
                .createNewInstance("createCommand3", "this is a command", "command implementation").done();
        platformCommandService.create(command1);
        platformCommandService.create(command2);
        platformCommandService.create(command3);
        final List<SPlatformCommand> commands1 = platformCommandService.getPlatformCommands(0, 500, SPlatformCommandCriterion.NAME_ASC);
        assertEquals(3, commands1.size());

        platformCommandService.deleteAll();
        final List<SPlatformCommand> commands2 = platformCommandService.getPlatformCommands(0, 500, SPlatformCommandCriterion.NAME_ASC);
        assertTrue(commands2.isEmpty());
        getTransactionService().complete();
    }

    @Test
    public void testUpdatePlatformCommand() throws Exception {
        getTransactionService().begin();
        final SPlatformCommand oldCommand = platformCommandBuilderAccessor.getSPlatformCommandBuilder()
                .createNewInstance("old", "this is an old command", "command implementation").done();
        platformCommandService.create(oldCommand);
        assertEquals("old", oldCommand.getName());
        assertEquals("this is an old command", oldCommand.getDescription());

        final String commandName = "new";
        final EntityUpdateDescriptor updateDescriptor = platformCommandBuilderAccessor.getSPlatformCommandUpdateBuilder().updateName(commandName)
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
        final SPlatformCommand sPlatformCommand = platformCommandBuilderAccessor.getSPlatformCommandBuilder()
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
        final SPlatformCommand sPlatformCommand1 = platformCommandBuilderAccessor.getSPlatformCommandBuilder()
                .createNewInstance("commandB", "this is command1", "command implementation").done();
        final SPlatformCommand sPlatformCommand2 = platformCommandBuilderAccessor.getSPlatformCommandBuilder()
                .createNewInstance("commandC", "this is command2", "command implementation").done();
        final SPlatformCommand sPlatformCommand3 = platformCommandBuilderAccessor.getSPlatformCommandBuilder()
                .createNewInstance("commandA", "this is command3", "command implementation").done();
        platformCommandService.create(sPlatformCommand1);
        platformCommandService.create(sPlatformCommand2);
        platformCommandService.create(sPlatformCommand3);

        final List<SPlatformCommand> commands = platformCommandService.getPlatformCommands(0, 5, SPlatformCommandCriterion.NAME_ASC);
        assertEquals(3, commands.size());
        assertEquals("commandA", commands.get(0).getName());
        assertEquals("commandB", commands.get(1).getName());
        assertEquals("commandC", commands.get(2).getName());

        platformCommandService.deleteAll();
        getTransactionService().complete();
    }

}
