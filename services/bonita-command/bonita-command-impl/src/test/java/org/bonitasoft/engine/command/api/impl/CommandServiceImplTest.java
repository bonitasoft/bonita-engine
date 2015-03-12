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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.command.SCommandGettingException;
import org.bonitasoft.engine.command.SCommandNotFoundException;
import org.bonitasoft.engine.command.api.record.SelectDescriptorBuilder;
import org.bonitasoft.engine.command.comparator.CommandComparator;
import org.bonitasoft.engine.command.model.SCommand;
import org.bonitasoft.engine.command.model.SCommandCriterion;
import org.bonitasoft.engine.command.model.SCommandImpl;
import org.bonitasoft.engine.command.model.SCommandLogBuilder;
import org.bonitasoft.engine.command.model.SCommandUpdateBuilderImpl;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 */
@RunWith(MockitoJUnitRunner.class)
public class CommandServiceImplTest {

    public static final int FETCH_SIZE = 2;
    @Mock
    private Recorder recorder;

    @Mock
    private ReadPersistenceService persistence;

    @Mock
    private EventService eventService;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private QueriableLoggerService queriableLoggerService;

    @Mock
    private CommandProvider commandProvider;

    private CommandServiceImpl commandServiceImpl;

    @Before
    public final void setUp() {
        commandServiceImpl = new CommandServiceImpl(persistence, recorder, eventService, logger, queriableLoggerService, commandProvider, FETCH_SIZE);
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
    public final void getNumberOfCommands() throws SBonitaReadException {
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
    public final void getNumberOfCommandsThrowException() throws SBonitaReadException {
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

    @Test
    public void create_should_call_record_insert() throws Exception {
        //given

        //when

        //then
    }

    @Test
    public void start_should_add_missing_system_commands() throws Exception {
        //given
        CommandDeployment firstDeploy = new CommandDeployment("first", "the first command", "com.company.FirstCommand");
        CommandDeployment secondDeploy = new CommandDeployment("second", "the second command", "com.company.SecondCommand");
        CommandDeployment thirdDeploy = new CommandDeployment("third", "the third command", "com.company.ThirdCommand");
        CommandDeployment fourthDeploy = new CommandDeployment("fourth", "the fourth command", "com.company.FourthCommand");
        CommandDeployment fifthDeploy = new CommandDeployment("fifth", "the fifth command", "com.company.FifthCommand");
        CommandDeployment sixthDeploy = new CommandDeployment("sixth", "the sixth command", "com.company.sixthCommand");

        SCommand first = new SCommandImpl("first", "the first command", "com.company.FirstCommand");
        SCommand second = new SCommandImpl("second", "the second command", "com.company.SecondCommand");
        SCommand third = new SCommandImpl("third", "the third command", "com.company.ThirdCommand");
        SCommand fourth = new SCommandImpl("fourth", "the fourth command", "com.company.FourthCommand");
        SCommand fifth = new SCommandImpl("fifth", "the fifth command", "com.company.FifthCommand");
        SCommand sixth = new SCommandImpl("sixth", "the sixth command", "com.company.sixthCommand");
        given(commandProvider.getDefaultCommands()).willReturn(Arrays.asList(firstDeploy, secondDeploy, thirdDeploy, fourthDeploy, fifthDeploy, sixthDeploy));

        CommandServiceImpl mockedCommandService = spy(commandServiceImpl);
        given(mockedCommandService.searchCommands(getQueryOptions(0, FETCH_SIZE))).willReturn(Arrays.asList(first, third));
        given(mockedCommandService.searchCommands(getQueryOptions(2, FETCH_SIZE))).willReturn(Arrays.asList(sixth));
        doNothing().when(mockedCommandService).create(any(SCommand.class));

        //when
        mockedCommandService.start();

        //then
        ArgumentCaptor<SCommand> commandArgumentCaptor = ArgumentCaptor.forClass(SCommand.class);
        verify(mockedCommandService, times(3)).create(commandArgumentCaptor.capture());
        assertThat(commandArgumentCaptor.getAllValues()).usingElementComparator(new CommandComparator()).contains(second, fourth, fifth);

    }

    @Test
    public void start_should_delete_system_commands_not_present_in_default_commands() throws Exception {
        //given
        CommandDeployment secondDeploy = new CommandDeployment("second", "the second command", "com.company.SecondCommand");
        CommandDeployment fifthDeploy = new CommandDeployment("fifth", "the fifth command", "com.company.FifthCommand");

        SCommand first = new SCommandImpl("first", "the first command", "com.company.FirstCommand");
        SCommand second = new SCommandImpl("second", "the second command", "com.company.SecondCommand");
        SCommand third = new SCommandImpl("third", "the third command", "com.company.ThirdCommand");
        SCommand fourth = new SCommandImpl("fourth", "the fourth command", "com.company.FourthCommand");
        SCommand fifth = new SCommandImpl("fifth", "the fifth command", "com.company.FifthCommand");
        given(commandProvider.getDefaultCommands()).willReturn(Arrays.asList(secondDeploy, fifthDeploy));

        CommandServiceImpl mockedCommandService = spy(commandServiceImpl);
        given(mockedCommandService.searchCommands(getQueryOptions(0, FETCH_SIZE))).willReturn(Arrays.asList(first, second));
        given(mockedCommandService.searchCommands(getQueryOptions(2, FETCH_SIZE))).willReturn(Arrays.asList(third, fourth));
        given(mockedCommandService.searchCommands(getQueryOptions(4, FETCH_SIZE))).willReturn(Arrays.asList(fifth));
        doNothing().when(mockedCommandService).delete(any(SCommand.class), any(SCommandLogBuilder.class));

        //when
        mockedCommandService.start();

        //then
        ArgumentCaptor<SCommand> commandArgumentCaptor = ArgumentCaptor.forClass(SCommand.class);
        verify(mockedCommandService, times(3)).delete(commandArgumentCaptor.capture(), any(SCommandLogBuilder.class));
        assertThat(commandArgumentCaptor.getAllValues()).usingElementComparator(new CommandComparator()).contains(first, third, fourth);

    }

    @Test
    public void start_should_update_existing_system_commands() throws Exception {
        //given
        CommandDeployment firstDeploy = new CommandDeployment("first", "the first command with a modified description", "com.company.FirstCommand");
        CommandDeployment secondDeploy = new CommandDeployment("second", "the second command", "com.company.SecondCommand");
        CommandDeployment thirdDeploy = new CommandDeployment("third", "the third command", "com.company.ThirdCommandModified");

        SCommand first = new SCommandImpl("first", "the first command", "com.company.FirstCommand");
        SCommand second = new SCommandImpl("second", "the second command", "com.company.SecondCommand");
        SCommand third = new SCommandImpl("third", "the third command", "com.company.ThirdCommand");
        given(commandProvider.getDefaultCommands()).willReturn(Arrays.asList(firstDeploy, secondDeploy, thirdDeploy));

        CommandServiceImpl mockedCommandService = spy(commandServiceImpl);
        given(mockedCommandService.searchCommands(getQueryOptions(0, FETCH_SIZE))).willReturn(Arrays.asList(first, second));
        given(mockedCommandService.searchCommands(getQueryOptions(2, FETCH_SIZE))).willReturn(Arrays.asList(third));
        doNothing().when(mockedCommandService).update(any(SCommand.class), any(EntityUpdateDescriptor.class));

        //when
        mockedCommandService.start();

        //then
        ArgumentCaptor<SCommand> commandCaptor = ArgumentCaptor.forClass(SCommand.class);
        ArgumentCaptor<EntityUpdateDescriptor> updateDescriptorCaptor = ArgumentCaptor.forClass(EntityUpdateDescriptor.class);
        verify(mockedCommandService, times(2)).update(commandCaptor.capture(), updateDescriptorCaptor.capture());
        assertThat(commandCaptor.getAllValues()).usingElementComparator(new CommandComparator()).contains(first, third);
        assertThat(updateDescriptorCaptor.getAllValues()).contains(
                new SCommandUpdateBuilderImpl().updateDescription("the first command with a modified description").done(),
                new SCommandUpdateBuilderImpl().updateImplementation("com.company.ThirdCommandModified").done());

    }

    private QueryOptions getQueryOptions(final int fromIndex, int maxResults) {
        return new QueryOptions(fromIndex, maxResults, Collections.singletonList(new OrderByOption(SCommand.class, "id", OrderByType.ASC)),
                Collections.singletonList(new FilterOption(SCommand.class, "system", true)), null);
    }
}
