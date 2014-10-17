/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.command.api.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.command.SCommandGettingException;
import org.bonitasoft.engine.command.SCommandNotFoundException;
import org.bonitasoft.engine.command.model.SCommand;
import org.bonitasoft.engine.command.model.SCommandCriterion;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
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
        final List<SCommand> sCommands = new ArrayList<SCommand>();
        when(persistence.selectList(any(SelectListDescriptor.class))).thenReturn(sCommands);

        Assert.assertEquals(sCommands, commandServiceImpl.getAllCommands(0, 1, SCommandCriterion.NAME_ASC));
    }

    @Test(expected = SCommandGettingException.class)
    public final void getAllCommandsThrowException() throws SCommandGettingException, SBonitaReadException {
        when(persistence.selectList(any(SelectListDescriptor.class))).thenThrow(new SBonitaReadException(""));

        commandServiceImpl.getAllCommands(0, 1, SCommandCriterion.NAME_DESC);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.command.api.impl.CommandServiceImpl#get(long)}.
     * 
     * @throws SCommandNotFoundException
     * @throws SBonitaReadException
     */
    @Test
    public final void getById() throws SCommandNotFoundException, SBonitaReadException {
        final SCommand sCommand = mock(SCommand.class);
        when(persistence.selectById(any(SelectByIdDescriptor.class))).thenReturn(sCommand);

        Assert.assertEquals(sCommand, commandServiceImpl.get(456L));
    }

    @Test(expected = SCommandNotFoundException.class)
    public final void getByIdNotExists() throws SBonitaReadException, SCommandNotFoundException {
        when(persistence.selectById(any(SelectByIdDescriptor.class))).thenReturn(null);

        commandServiceImpl.get(456L);
    }

    @Test(expected = SCommandNotFoundException.class)
    public final void getByIdThrowException() throws SBonitaReadException, SCommandNotFoundException {
        when(persistence.selectById(any(SelectByIdDescriptor.class))).thenThrow(new SBonitaReadException(""));

        commandServiceImpl.get(456L);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.command.api.impl.CommandServiceImpl#get(java.lang.String)}.
     * 
     * @throws SCommandNotFoundException
     * @throws SBonitaReadException
     */
    @Test
    public final void getByName() throws SCommandNotFoundException, SBonitaReadException {
        final SCommand sCommand = mock(SCommand.class);
        when(persistence.selectOne(any(SelectOneDescriptor.class))).thenReturn(sCommand);

        Assert.assertEquals(sCommand, commandServiceImpl.get("name"));
    }

    @Test(expected = SCommandNotFoundException.class)
    public final void getByNameNotExists() throws SBonitaReadException, SCommandNotFoundException {
        when(persistence.selectOne(any(SelectOneDescriptor.class))).thenReturn(null);

        commandServiceImpl.get("name");
    }

    @Test(expected = SCommandNotFoundException.class)
    public final void getByNameThrowException() throws SBonitaReadException, SCommandNotFoundException {
        when(persistence.selectOne(any(SelectOneDescriptor.class))).thenThrow(new SBonitaReadException(""));

        commandServiceImpl.get("name");
    }

    /**
     * Test method for {@link org.bonitasoft.engine.command.api.impl.CommandServiceImpl#getNumberOfCommands(org.bonitasoft.engine.persistence.QueryOptions)}.
     * 
     * @throws SBonitaReadException
     * @throws SBonitaReadException
     */
    @Test
    public final void getNumberOfCommands() throws SBonitaReadException, SBonitaReadException {
        final long numberOfCommands = 54165L;
        final QueryOptions options = mock(QueryOptions.class);
        when(persistence.getNumberOfEntities(SCommand.class, options, null)).thenReturn(numberOfCommands);

        Assert.assertEquals(numberOfCommands, commandServiceImpl.getNumberOfCommands(options));
    }

    @Test(expected = SBonitaReadException.class)
    public final void getNumberOfCommandsThrowException() throws SBonitaReadException, SBonitaReadException {
        final QueryOptions options = mock(QueryOptions.class);
        when(persistence.getNumberOfEntities(SCommand.class, options, null)).thenThrow(new SBonitaReadException(""));

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
        final List<SCommand> sCommands = new ArrayList<SCommand>();
        when(persistence.selectList(any(SelectListDescriptor.class))).thenReturn(sCommands);

        Assert.assertEquals(sCommands, commandServiceImpl.getUserCommands(0, 1, SCommandCriterion.NAME_ASC));
    }

    @Test(expected = SCommandGettingException.class)
    public final void getUserCommandsThrowException() throws SCommandGettingException, SBonitaReadException {
        when(persistence.selectList(any(SelectListDescriptor.class))).thenThrow(new SBonitaReadException(""));

        commandServiceImpl.getUserCommands(0, 1, SCommandCriterion.NAME_DESC);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.command.api.impl.CommandServiceImpl#searchCommands(org.bonitasoft.engine.persistence.QueryOptions)}.
     * 
     * @throws SBonitaReadException
     * @throws SBonitaReadException
     */
    @Test
    public final void searchCommands() throws SBonitaReadException {
        final List<SCommand> sCommands = new ArrayList<SCommand>();
        final QueryOptions options = mock(QueryOptions.class);
        when(persistence.searchEntity(SCommand.class, options, null)).thenReturn(sCommands);

        Assert.assertEquals(sCommands, commandServiceImpl.searchCommands(options));
    }

    @Test(expected = SBonitaReadException.class)
    public final void searchCommandsThrowException() throws SBonitaReadException {
        final QueryOptions options = mock(QueryOptions.class);
        when(persistence.searchEntity(SCommand.class, options, null)).thenThrow(new SBonitaReadException(""));

        commandServiceImpl.searchCommands(options);
    }

}
