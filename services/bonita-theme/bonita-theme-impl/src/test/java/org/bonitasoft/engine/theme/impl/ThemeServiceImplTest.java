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
package org.bonitasoft.engine.theme.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteAllRecord;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.theme.builder.SThemeUpdateBuilder;
import org.bonitasoft.engine.theme.builder.SThemeUpdateBuilderFactory;
import org.bonitasoft.engine.theme.exception.SRestoreThemeException;
import org.bonitasoft.engine.theme.exception.SThemeCreationException;
import org.bonitasoft.engine.theme.exception.SThemeDeletionException;
import org.bonitasoft.engine.theme.exception.SThemeNotFoundException;
import org.bonitasoft.engine.theme.exception.SThemeReadException;
import org.bonitasoft.engine.theme.exception.SThemeUpdateException;
import org.bonitasoft.engine.theme.model.STheme;
import org.bonitasoft.engine.theme.model.SThemeType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 */
@RunWith(MockitoJUnitRunner.class)
public class ThemeServiceImplTest {

    @Mock
    private EventService eventService;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private ReadPersistenceService persistenceService;

    @Mock
    private QueriableLoggerService queriableLoggerService;

    @Mock
    private Recorder recorder;

    @InjectMocks
    private ThemeServiceImpl themeServiceImpl;

    /**
     * Test method for {@link org.bonitasoft.engine.theme.impl.ThemeServiceImpl#createTheme(org.bonitasoft.engine.theme.model.STheme)}.
     *
     * @throws SThemeCreationException
     * @throws SRecorderException
     */
    @Test
    public final void createTheme() throws SThemeCreationException, SRecorderException {
        final STheme sTheme = mock(STheme.class);
        doReturn(1L).when(sTheme).getId();

        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doNothing().when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        final STheme result = themeServiceImpl.createTheme(sTheme);
        assertNotNull(result);
        assertEquals(sTheme, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void createNullTheme() throws Exception {
        themeServiceImpl.createTheme(null);
    }

    @Test(expected = SThemeCreationException.class)
    public final void createThemeThrowException() throws SThemeCreationException, SRecorderException {
        final STheme sTheme = mock(STheme.class);
        doReturn(1L).when(sTheme).getId();

        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doThrow(new SRecorderException("plop")).when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        themeServiceImpl.createTheme(sTheme);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.theme.impl.ThemeServiceImpl#deleteTheme(long)}.
     *
     * @throws SThemeDeletionException
     * @throws SThemeNotFoundException
     * @throws SRecorderException
     * @throws SBonitaReadException
     */
    @Test
    public final void deleteThemeById() throws SThemeNotFoundException, SThemeDeletionException, SRecorderException, SBonitaReadException {
        final STheme sTheme = mock(STheme.class);
        doReturn(3L).when(sTheme).getId();

        doReturn(sTheme).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<STheme>> any());
        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doNothing().when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        themeServiceImpl.deleteTheme(1);
    }

    @Test(expected = SThemeNotFoundException.class)
    public final void deleteNoThemeById() throws SBonitaReadException, SThemeDeletionException, SThemeNotFoundException {
        when(persistenceService.selectById(Matchers.<SelectByIdDescriptor<STheme>> any())).thenReturn(null);

        themeServiceImpl.deleteTheme(1);
    }

    @Test(expected = SThemeDeletionException.class)
    public void deleteThemeByIdThrowException() throws Exception {
        final STheme sTheme = mock(STheme.class);
        doReturn(3L).when(sTheme).getId();

        doReturn(sTheme).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<STheme>> any());
        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));

        themeServiceImpl.deleteTheme(1);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.theme.impl.ThemeServiceImpl#deleteTheme(org.bonitasoft.engine.theme.model.STheme)}.
     *
     * @throws SRecorderException
     * @throws SThemeDeletionException
     */
    @Test
    public final void deleteThemeByObject() throws SRecorderException, SThemeDeletionException {
        final STheme sTheme = mock(STheme.class);
        doReturn(3L).when(sTheme).getId();

        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doNothing().when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        themeServiceImpl.deleteTheme(sTheme);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void deleteNoThemeByObject() throws SThemeDeletionException {
        themeServiceImpl.deleteTheme(null);
    }

    @Test(expected = SThemeDeletionException.class)
    public void deleteThemeByObjectThrowException() throws Exception {
        final STheme sTheme = mock(STheme.class);
        doReturn(3L).when(sTheme).getId();

        doReturn(sTheme).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<STheme>> any());
        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));

        themeServiceImpl.deleteTheme(sTheme);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.theme.impl.ThemeServiceImpl#restoreDefaultTheme(SThemeType)}.
     *
     * @throws SRestoreThemeException
     * @throws SRecorderException
     */
    @Test
    public final void restoreDefaultTheme() throws SRestoreThemeException, SRecorderException {
        doNothing().when(recorder).recordDeleteAll(any(DeleteAllRecord.class));

        themeServiceImpl.restoreDefaultTheme(SThemeType.MOBILE);
    }

    @Test(expected = SRestoreThemeException.class)
    public void restoreDefaultThemeThrowExceptionWhenDelete() throws Exception {
        doThrow(new SRecorderException("")).when(recorder).recordDeleteAll(any(DeleteAllRecord.class));

        themeServiceImpl.restoreDefaultTheme(SThemeType.PORTAL);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.theme.impl.ThemeServiceImpl#getCurrentTheme(org.bonitasoft.engine.theme.model.SThemeType)}.
     *
     * @throws SThemeNotFoundException
     * @throws SBonitaReadException
     * @throws SThemeReadException
     */
    @Test
    public final void getCurrentTheme() throws SThemeNotFoundException, SBonitaReadException, SThemeReadException {
        final STheme sTheme = mock(STheme.class);
        final SThemeType type = SThemeType.MOBILE;

        doReturn(sTheme).when(persistenceService).selectOne(Matchers.<SelectOneDescriptor<STheme>> any());

        assertEquals(sTheme, themeServiceImpl.getTheme(type, false));
    }

    @Test(expected = SThemeNotFoundException.class)
    public void getNoCurrentTheme() throws Exception {
        final SThemeType type = SThemeType.MOBILE;

        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<STheme>> any())).thenReturn(null);

        themeServiceImpl.getTheme(type, false);
    }

    @Test(expected = SThemeReadException.class)
    public void getCurrentThemeThrowException() throws Exception {
        final SThemeType type = SThemeType.MOBILE;

        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<STheme>> any())).thenThrow(new SBonitaReadException(""));

        themeServiceImpl.getTheme(type, false);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.theme.impl.ThemeServiceImpl#getDefaultTheme(org.bonitasoft.engine.theme.model.SThemeType)}.
     *
     * @throws SBonitaReadException
     * @throws SThemeNotFoundException
     * @throws SThemeReadException
     */
    @Test
    public final void getDefaultTheme() throws SBonitaReadException, SThemeNotFoundException, SThemeReadException {
        final STheme sTheme = mock(STheme.class);
        final SThemeType type = SThemeType.MOBILE;

        doReturn(sTheme).when(persistenceService).selectOne(Matchers.<SelectOneDescriptor<STheme>> any());

        assertEquals(sTheme, themeServiceImpl.getTheme(type, true));
    }

    @Test(expected = SThemeNotFoundException.class)
    public void getNoDefaultTheme() throws Exception {
        final SThemeType type = SThemeType.MOBILE;

        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<STheme>> any())).thenReturn(null);

        themeServiceImpl.getTheme(type, true);
    }

    @Test(expected = SThemeReadException.class)
    public void getDefaultThemeThrowException() throws Exception {
        final SThemeType type = SThemeType.MOBILE;

        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<STheme>> any())).thenThrow(new SBonitaReadException(""));

        themeServiceImpl.getTheme(type, true);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.theme.impl.ThemeServiceImpl#getTheme(long)}.
     */
    @Test
    public void getThemeById() throws Exception {
        final STheme sTheme = mock(STheme.class);

        doReturn(sTheme).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<STheme>> any());

        assertEquals(sTheme, themeServiceImpl.getTheme(1));
    }

    @Test(expected = SThemeNotFoundException.class)
    public void getNoThemeById() throws Exception {
        when(persistenceService.selectById(Matchers.<SelectByIdDescriptor<STheme>> any())).thenReturn(null);

        themeServiceImpl.getTheme(1);
    }

    @Test(expected = SThemeReadException.class)
    public void getThemeByIdThrowException() throws Exception {
        when(persistenceService.selectById(Matchers.<SelectByIdDescriptor<STheme>> any())).thenThrow(new SBonitaReadException(""));

        themeServiceImpl.getTheme(1);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.theme.impl.ThemeServiceImpl#getLastModifiedTheme(org.bonitasoft.engine.theme.model.SThemeType)}.
     *
     * @throws SBonitaReadException
     * @throws SThemeNotFoundException
     * @throws SThemeReadException
     */
    @Test
    public final void getLastModifiedTheme() throws SBonitaReadException, SThemeNotFoundException, SThemeReadException {
        final STheme sTheme = mock(STheme.class);
        final SThemeType type = SThemeType.MOBILE;

        doReturn(sTheme).when(persistenceService).selectOne(Matchers.<SelectOneDescriptor<STheme>> any());

        assertEquals(sTheme, themeServiceImpl.getLastModifiedTheme(type));
    }

    @Test(expected = SThemeNotFoundException.class)
    public void getNoLastModifiedTheme() throws Exception {
        final SThemeType type = SThemeType.MOBILE;

        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<STheme>> any())).thenReturn(null);

        themeServiceImpl.getLastModifiedTheme(type);
    }

    @Test(expected = SThemeReadException.class)
    public void getLastModifiedThemeThrowException() throws Exception {
        final SThemeType type = SThemeType.MOBILE;

        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<STheme>> any())).thenThrow(new SBonitaReadException(""));

        themeServiceImpl.getLastModifiedTheme(type);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.theme.impl.ThemeServiceImpl#getNumberOfThemes(org.bonitasoft.engine.persistence.QueryOptions)}.
     */
    @Test
    public void getNumberOfThemesWithOptions() throws Exception {
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistenceService.getNumberOfEntities(STheme.class, options, Collections.<String, Object> emptyMap())).thenReturn(1L);

        assertEquals(1L, themeServiceImpl.getNumberOfThemes(options));
    }

    @Test(expected = SBonitaReadException.class)
    public void getNumberOfThemesWithOptionsThrowException() throws Exception {
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistenceService.getNumberOfEntities(STheme.class, options, Collections.<String, Object> emptyMap())).thenThrow(new SBonitaReadException(""));

        themeServiceImpl.getNumberOfThemes(options);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.theme.impl.ThemeServiceImpl#searchThemes(org.bonitasoft.engine.persistence.QueryOptions)}.
     */
    @Test
    public void searchThemesWithOptions() throws Exception {
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistenceService.searchEntity(STheme.class, options, Collections.<String, Object> emptyMap())).thenReturn(new ArrayList<STheme>());

        assertNotNull(themeServiceImpl.searchThemes(options));
    }

    @Test(expected = SBonitaReadException.class)
    public void searchThemesWithOptionsThrowException() throws Exception {
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistenceService.searchEntity(STheme.class, options, Collections.<String, Object> emptyMap())).thenThrow(new SBonitaReadException(""));

        themeServiceImpl.searchThemes(options);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.theme.impl.ThemeServiceImpl#updateTheme(org.bonitasoft.engine.theme.model.STheme, org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor)}
     *
     * @throws SBonitaReadException
     * @throws SRecorderException
     * @throws SThemeUpdateException
     */
    @Test
    public final void updateTheme() throws SThemeUpdateException {
        final STheme sTheme = mock(STheme.class);
        doReturn(3L).when(sTheme).getId();
        final SThemeUpdateBuilder sThemeUpdateBuilder = BuilderFactory.get(SThemeUpdateBuilderFactory.class).createNewInstance();
        sThemeUpdateBuilder.setType(SThemeType.MOBILE);

        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        final STheme result = themeServiceImpl.updateTheme(sTheme, sThemeUpdateBuilder.done());
        assertNotNull(result);
        assertEquals(sTheme, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void updateNullTheme() throws SThemeUpdateException {
        final SThemeUpdateBuilder sThemeUpdateBuilder = BuilderFactory.get(SThemeUpdateBuilderFactory.class).createNewInstance();

        themeServiceImpl.updateTheme(null, sThemeUpdateBuilder.done());
    }

    @Test(expected = IllegalArgumentException.class)
    public final void updateThemeWithNullDescriptor() throws SThemeUpdateException {
        final STheme sTheme = mock(STheme.class);
        doReturn(3L).when(sTheme).getId();

        themeServiceImpl.updateTheme(sTheme, null);
    }

    @Test(expected = SThemeUpdateException.class)
    public final void updateThemeThrowException() throws SRecorderException, SThemeUpdateException {
        final STheme sTheme = mock(STheme.class);
        doReturn(3L).when(sTheme).getId();
        final SThemeUpdateBuilder sThemeUpdateBuilder = BuilderFactory.get(SThemeUpdateBuilderFactory.class).createNewInstance();
        sThemeUpdateBuilder.setType(SThemeType.MOBILE);

        doThrow(new SRecorderException("plop")).when(recorder).recordUpdate(any(UpdateRecord.class), any(SUpdateEvent.class));

        themeServiceImpl.updateTheme(sTheme, sThemeUpdateBuilder.done());
    }

}
