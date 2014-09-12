package org.bonitasoft.engine.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.command.model.SCommand;
import org.bonitasoft.engine.command.model.SCommandBuilderFactory;
import org.bonitasoft.engine.command.model.SCommandCriterion;
import org.bonitasoft.engine.command.model.SCommandUpdateBuilderFactory;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Test;

/**
 * @author Zhang Bole
 */
public class CommandServiceIntegrationTest extends CommonServiceTest {

    private static CommandService commandService;

    private static SCommandBuilderFactory commandBuilderFactory;

    static {
        commandService = getServicesBuilder().buildCommandService();
        commandBuilderFactory = BuilderFactory.get(SCommandBuilderFactory.class);
    }

    @Test(expected = SCommandAlreadyExistsException.class)
    public void testSCommandAlreadyExistsException() throws Exception {
        getTransactionService().begin();
        final SCommand command = commandBuilderFactory.createNewInstance("createCommand", "this is a command", "command implementation")
                .done();
        commandService.create(command);
        try {
            commandService.create(command);
        } finally {
            commandService.delete("createCommand");
            getTransactionService().complete();
        }
    }

    @Test(expected = SCommandNotFoundException.class)
    public void testSCommandNotFoundException() throws Exception {
        getTransactionService().begin();
        try {
            commandService.get("a");
        } finally {
            getTransactionService().complete();
        }

    }

    @Test
    public void testCreateCommand() throws Exception {
        getTransactionService().begin();
        final SCommand command1 = commandBuilderFactory.createNewInstance("createCommand", "this is a command", "command implementation")
                .done();
        commandService.create(command1);
        final SCommand command2 = commandService.get("createCommand");
        assertNotNull("can't find the category after adding it", command2);
        assertEquals("can't retrieve the same category", command1.getName(), command2.getName());
        assertEquals("can't retrieve the same category", command1.getId(), command2.getId());
        commandService.delete("createCommand");
        getTransactionService().complete();
    }

    @Test(expected = SCommandNotFoundException.class)
    public void testDeleteCommand() throws Exception {
        getTransactionService().begin();
        final SCommand command = commandBuilderFactory
                .createNewInstance("testCommandDelete", "this is a command", "command implementation").done();
        commandService.create(command);
        commandService.delete("testCommandDelete");
        try {
            commandService.get("testCommandDelete");
        } finally {
            getTransactionService().complete();
        }
    }

    @Test
    public void testDeleteAll() throws Exception {
        getTransactionService().begin();
        final SCommand command1 = commandBuilderFactory
                .createNewInstance("createCommand1", "this is a command", "command implementation").done();
        final SCommand command2 = commandBuilderFactory
                .createNewInstance("createCommand2", "this is a command", "command implementation").done();
        final SCommand command3 = commandBuilderFactory
                .createNewInstance("createCommand3", "this is a command", "command implementation").done();
        commandService.create(command1);
        commandService.create(command2);
        commandService.create(command3);
        final List<SCommand> commands1 = commandService.getAllCommands(0, 500, SCommandCriterion.NAME_ASC);
        assertEquals(3, commands1.size());

        commandService.deleteAll();
        final List<SCommand> commands2 = commandService.getAllCommands(0, 500, SCommandCriterion.NAME_ASC);
        assertTrue(commands2.isEmpty());
        getTransactionService().complete();
    }

    @Test
    public void testUpdateCommand() throws Exception {
        getTransactionService().begin();
        final SCommand oldCommand = commandBuilderFactory.createNewInstance("old", "this is an old command", "command implementation")
                .done();
        commandService.create(oldCommand);
        assertEquals("old", oldCommand.getName());
        assertEquals("this is an old command", oldCommand.getDescription());

        final String commandName = "new";
        final EntityUpdateDescriptor updateDescriptor = BuilderFactory.get(SCommandUpdateBuilderFactory.class).createNewInstance().updateName(commandName)
                .updateDescription("this is a new command").done();
        commandService.update(oldCommand, updateDescriptor);
        final SCommand newCommand = commandService.get(commandName);
        assertEquals("new", newCommand.getName());
        assertEquals("this is a new command", newCommand.getDescription());
        commandService.delete(commandName);
        getTransactionService().complete();
    }

    @Test
    public void testget() throws Exception {
        getTransactionService().begin();
        final SCommand sCommand = commandBuilderFactory.createNewInstance("commandOne", "this is a command", "command implementation")
                .done();
        commandService.create(sCommand);
        final SCommand command = commandService.get("commandOne");
        assertEquals("commandOne", command.getName());
        assertEquals("this is a command", command.getDescription());
        commandService.delete("commandOne");
        getTransactionService().complete();
    }

    @Test
    public void testGetCommandsWithCriterion() throws Exception {
        getTransactionService().begin();
        final SCommand sCommand1 = commandBuilderFactory.createNewInstance("commandB", "this is command1", "command implementation")
                .done();
        final SCommand sCommand2 = commandBuilderFactory.createNewInstance("commandC", "this is command2", "command implementation")
                .done();
        final SCommand sCommand3 = commandBuilderFactory.createNewInstance("commandA", "this is command3", "command implementation")
                .done();
        commandService.create(sCommand1);
        commandService.create(sCommand2);
        commandService.create(sCommand3);

        final List<SCommand> commands = commandService.getAllCommands(0, 5, SCommandCriterion.NAME_ASC);
        assertEquals(3, commands.size());
        assertEquals("commandA", commands.get(0).getName());
        assertEquals("commandB", commands.get(1).getName());
        assertEquals("commandC", commands.get(2).getName());

        commandService.deleteAll();
        getTransactionService().complete();
    }

}
