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
package org.bonitasoft.engine.command.api.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.command.SCommandGettingException;
import org.bonitasoft.engine.command.SCommandNotFoundException;
import org.bonitasoft.engine.command.api.record.SelectDescriptorBuilder;
import org.bonitasoft.engine.command.model.SCommand;
import org.bonitasoft.engine.command.model.SCommandCriterion;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Celine Souchet
 */
public class CommandServiceImplTest {

    private Recorder recorder;

    private ReadPersistenceService persistence;

    private EventService eventService;

    private TechnicalLoggerService logger;

    private QueriableLoggerService queriableLoggerService;

    private CommandServiceImpl commandServiceImpl;

    @Before
    public final void setUp() {
        recorder = mock(Recorder.class);
        persistence = mock(ReadPersistenceService.class);
        eventService = mock(EventService.class);
        logger = mock(TechnicalLoggerService.class);
        queriableLoggerService = mock(QueriableLoggerService.class);
        commandServiceImpl = new CommandServiceImpl(persistence, recorder, eventService, logger, queriableLoggerService);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.command.api.impl.CommandServiceImpl#getAllCommands(int, int, org.bonitasoft.engine.command.model.SCommandCriterion)}.
     *
     * @throws SCommandGettingException
     * @throws SBonitaReadException
     */
    @Test
    public final void getAllCommands() throws SCommandGettingException, SBonitaReadException {
        // Given
        final List<SCommand> sCommands = new ArrayList<SCommand>();
        final String field = "name";
        final OrderByType orderByType = OrderByType.ASC;
        final int startIndex = 0;
        final int maxResults = 1;
        when(persistence.selectList(SelectDescriptorBuilder.getCommands(field, orderByType, startIndex, maxResults))).thenReturn(sCommands);

        // When
        final List<SCommand> allCommands = commandServiceImpl.getAllCommands(startIndex, maxResults, SCommandCriterion.NAME_ASC);

        // Then
        Assert.assertEquals(sCommands, allCommands);
    }

    @Test(expected = SCommandGettingException.class)
    public final void getAllCommandsThrowException() throws SCommandGettingException, SBonitaReadException {
        // Given
        final String field = "name";
        final OrderByType orderByType = OrderByType.ASC;
        final int startIndex = 0;
        final int maxResults = 1;
        when(persistence.selectList(SelectDescriptorBuilder.getCommands(field, orderByType, startIndex, maxResults))).thenThrow(new SBonitaReadException(""));

        // When
        commandServiceImpl.getAllCommands(startIndex, maxResults, SCommandCriterion.NAME_DESC);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.command.api.impl.CommandServiceImpl#get(long)}.
     *
     * @throws SCommandNotFoundException
     * @throws SBonitaReadException
     */
    @Test
    public final void getById() throws SCommandNotFoundException, SBonitaReadException {
        // Given
        final SCommand sCommand = mock(SCommand.class);
        final long commandId = 456L;
        when(persistence.selectById(SelectDescriptorBuilder.getCommandById(commandId))).thenReturn(sCommand);

        // When
        final SCommand actual = commandServiceImpl.get(commandId);

        // Then
        Assert.assertEquals(sCommand, actual);
    }

    @Test(expected = SCommandNotFoundException.class)
    public final void getByIdNotExists() throws SBonitaReadException, SCommandNotFoundException {
        // Given
        final long commandId = 456L;
        when(persistence.selectById(SelectDescriptorBuilder.getCommandById(commandId))).thenReturn(null);

        // When
        commandServiceImpl.get(commandId);
    }

    @Test(expected = SCommandNotFoundException.class)
    public final void getByIdThrowException() throws SBonitaReadException, SCommandNotFoundException {
        // Given
        final long commandId = 456L;
        when(persistence.selectById(SelectDescriptorBuilder.getCommandById(commandId))).thenThrow(new SBonitaReadException(""));

        // When
        commandServiceImpl.get(commandId);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.command.api.impl.CommandServiceImpl#get(java.lang.String)}.
     *
     * @throws SCommandNotFoundException
     * @throws SBonitaReadException
     */
    @Test
    public final void getByName() throws SCommandNotFoundException, SBonitaReadException {
        // Given
        final SCommand sCommand = mock(SCommand.class);
        final String commandName = "name";
        when(persistence.selectOne(SelectDescriptorBuilder.getCommandByName(commandName))).thenReturn(sCommand);

        // When
        final SCommand actual = commandServiceImpl.get(commandName);

        // Then
        Assert.assertEquals(sCommand, actual);
    }

    @Test(expected = SCommandNotFoundException.class)
    public final void getByNameNotExists() throws SBonitaReadException, SCommandNotFoundException {
        // Given
        final String commandName = "name";
        when(persistence.selectOne(SelectDescriptorBuilder.getCommandByName(commandName))).thenReturn(null);

        // When
        commandServiceImpl.get(commandName);
    }

    @Test(expected = SCommandNotFoundException.class)
    public final void getByNameThrowException() throws SBonitaReadException, SCommandNotFoundException {
        // Given
        final String commandName = "name";
        when(persistence.selectOne(SelectDescriptorBuilder.getCommandByName(commandName))).thenThrow(new SBonitaReadException(""));

        // When
        commandServiceImpl.get(commandName);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.command.api.impl.CommandServiceImpl#getNumberOfCommands(org.bonitasoft.engine.persistence.QueryOptions)}.
     *
     * @throws SBonitaReadException
     * @throws SBonitaReadException
     */
    @Test
    public final void getNumberOfCommands() throws SBonitaReadException, SBonitaReadException {
        // Given
        final long numberOfCommands = 54165L;
        final QueryOptions options = mock(QueryOptions.class);
        when(persistence.getNumberOfEntities(SCommand.class, options, null)).thenReturn(numberOfCommands);

        // When
        final long result = commandServiceImpl.getNumberOfCommands(options);

        // Then
        Assert.assertEquals(numberOfCommands, result);
    }

    @Test(expected = SBonitaReadException.class)
    public final void getNumberOfCommandsThrowException() throws SBonitaReadException, SBonitaReadException {
        // Given
        final QueryOptions options = mock(QueryOptions.class);
        when(persistence.getNumberOfEntities(SCommand.class, options, null)).thenThrow(new SBonitaReadException(""));

        // When
        commandServiceImpl.getNumberOfCommands(options);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.command.api.impl.CommandServiceImpl#getUserCommands(int, int, org.bonitasoft.engine.command.model.SCommandCriterion)}.
     *
     * @throws SCommandGettingException
     * @throws SBonitaReadException
     */
    @Test
    public final void getUserCommands() throws SCommandGettingException, SBonitaReadException {
        // Given
        final List<SCommand> sCommands = new ArrayList<SCommand>();
        final String field = "name";
        final OrderByType orderByType = OrderByType.ASC;
        final int startIndex = 0;
        final int maxResults = 1;
        when(persistence.selectList(SelectDescriptorBuilder.getUserCommands(field, orderByType, startIndex, maxResults))).thenReturn(sCommands);

        // When
        final List<SCommand> userCommands = commandServiceImpl.getUserCommands(startIndex, maxResults, SCommandCriterion.NAME_ASC);

        // Then
        Assert.assertEquals(sCommands, userCommands);
    }

    @Test(expected = SCommandGettingException.class)
    public final void getUserCommandsThrowException() throws SCommandGettingException, SBonitaReadException {
        // Given
        final String field = "name";
        final OrderByType orderByType = OrderByType.ASC;
        final int startIndex = 0;
        final int maxResults = 1;
        when(persistence.selectList(SelectDescriptorBuilder.getUserCommands(field, orderByType, startIndex, maxResults))).thenThrow(
                new SBonitaReadException(""));

        // When
        commandServiceImpl.getUserCommands(startIndex, maxResults, SCommandCriterion.NAME_DESC);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.command.api.impl.CommandServiceImpl#searchCommands(org.bonitasoft.engine.persistence.QueryOptions)}.
     *
     * @throws SBonitaReadException
     * @throws SBonitaReadException
     */
    @Test
    public final void searchCommands() throws SBonitaReadException {
        // Given
        final List<SCommand> sCommands = new ArrayList<SCommand>();
        final QueryOptions options = mock(QueryOptions.class);
        when(persistence.searchEntity(SCommand.class, options, null)).thenReturn(sCommands);

        // When
        final List<SCommand> searchCommands = commandServiceImpl.searchCommands(options);

        // Then
        Assert.assertEquals(sCommands, searchCommands);
    }

    @Test(expected = SBonitaReadException.class)
    public final void searchCommandsThrowException() throws SBonitaReadException {
        // Given
        final QueryOptions options = mock(QueryOptions.class);
        when(persistence.searchEntity(SCommand.class, options, null)).thenThrow(new SBonitaReadException(""));

        // When
        commandServiceImpl.searchCommands(options);
    }

}
