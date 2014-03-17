/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.page.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.bonitasoft.engine.page.SPage;

public class PageServiceImplTest {

    @Test
    public void getNumberOfPages() throws SBonitaException {
        final EventService eventService = mock(EventService.class);
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final ReadPersistenceService persistence = mock(ReadPersistenceService.class);
        final PageServiceImpl serviceImpl = new PageServiceImpl(persistence, null, eventService, logger, null);
        final QueryOptions options = mock(QueryOptions.class);
        when(logger.isLoggable(PageServiceImpl.class, TechnicalLogSeverity.TRACE)).thenReturn(true);
        final long expected = 50;
        when(persistence.getNumberOfEntities(SPage.class, options, null)).thenReturn(expected);
        final long numberOfPages = serviceImpl.getNumberOfPages(options);
        Assert.assertEquals(expected, numberOfPages);
    }

    @Test(expected = SBonitaReadException.class)
    public void getNumberOfPagesThrowsException() throws SBonitaException {
        final EventService eventService = mock(EventService.class);
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final ReadPersistenceService persistence = mock(ReadPersistenceService.class);
        final PageServiceImpl serviceImpl = new PageServiceImpl(persistence, null, eventService, logger, null);
        final QueryOptions options = mock(QueryOptions.class);
        when(logger.isLoggable(PageServiceImpl.class, TechnicalLogSeverity.TRACE)).thenReturn(true);
        when(persistence.getNumberOfEntities(SPage.class, options, null)).thenThrow(new SBonitaReadException("ouch!"));
        serviceImpl.getNumberOfPages(options);
    }

    @Test
    public void getPage() throws SBonitaException {
        final EventService eventService = mock(EventService.class);
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final ReadPersistenceService persistence = mock(ReadPersistenceService.class);
        final PageServiceImpl serviceImpl = new PageServiceImpl(persistence, null, eventService, logger, null);
        when(logger.isLoggable(PageServiceImpl.class, TechnicalLogSeverity.TRACE)).thenReturn(true);
        final long pageId = 15;
        final SPage expected = new SPageImpl("page1", 123456, 45, true);
        expected.setId(pageId);
        when(persistence.selectById(new SelectByIdDescriptor<SPage>("getPageById", SPage.class, pageId))).thenReturn(expected);

        final SPage page = serviceImpl.getPage(pageId);
        Assert.assertEquals(expected, page);
    }

    @Test(expected = SObjectNotFoundException.class)
    public void getPageThrowsPageNotFoundException() throws SBonitaException {
        final EventService eventService = mock(EventService.class);
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final ReadPersistenceService persistence = mock(ReadPersistenceService.class);
        final PageServiceImpl serviceImpl = new PageServiceImpl(persistence, null, eventService, logger, null);
        when(logger.isLoggable(PageServiceImpl.class, TechnicalLogSeverity.TRACE)).thenReturn(true);
        final long pageId = 15;
        final SPage expected = new SPageImpl("page1", 123456, 45, true);
        expected.setId(pageId);
        when(persistence.selectById(new SelectByIdDescriptor<SPage>("getPageById", SPage.class, pageId))).thenReturn(null);

        serviceImpl.getPage(pageId);
    }

    @Test(expected = SBonitaReadException.class)
    public void getPageThrowsException() throws SBonitaException {
        final EventService eventService = mock(EventService.class);
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final ReadPersistenceService persistence = mock(ReadPersistenceService.class);
        final PageServiceImpl serviceImpl = new PageServiceImpl(persistence, null, eventService, logger, null);
        when(logger.isLoggable(PageServiceImpl.class, TechnicalLogSeverity.TRACE)).thenReturn(true);
        final long pageId = 15;
        final SPage expected = new SPageImpl("page1", 123456, 45, true);
        expected.setId(pageId);
        when(persistence.selectById(new SelectByIdDescriptor<SPage>("getPageById", SPage.class, pageId))).thenThrow(new SBonitaReadException("ouch!"));

        serviceImpl.getPage(pageId);
    }

    @Test
    public void deletePage() throws SBonitaException {
        final EventService eventService = mock(EventService.class);
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final ReadPersistenceService persistence = mock(ReadPersistenceService.class);
        final Recorder recorder = mock(Recorder.class);
        final QueriableLoggerService loggerService = mock(QueriableLoggerService.class);
        final PageServiceImpl serviceImpl = new PageServiceImpl(persistence, recorder, eventService, logger, loggerService);
        when(logger.isLoggable(PageServiceImpl.class, TechnicalLogSeverity.TRACE)).thenReturn(true);
        final long pageId = 15;
        final SPage expected = new SPageImpl("page1", 123456, 45, true);
        expected.setId(pageId);

        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                // Deletion OK
                return null;
            }

        }).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        when(persistence.selectById(new SelectByIdDescriptor<SPage>("getPageById", SPage.class, pageId))).thenReturn(expected);
        when(loggerService.isLoggable(any(String.class), any(SQueriableLogSeverity.class))).thenReturn(true);

        serviceImpl.deletePage(pageId);
    }

    @Test(expected = SObjectModificationException.class)
    public void deletePageThrowsPageNotFoundException() throws SBonitaException {
        final EventService eventService = mock(EventService.class);
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final ReadPersistenceService persistence = mock(ReadPersistenceService.class);
        final Recorder recorder = mock(Recorder.class);
        final QueriableLoggerService loggerService = mock(QueriableLoggerService.class);
        final PageServiceImpl serviceImpl = new PageServiceImpl(persistence, recorder, eventService, logger, loggerService);
        when(logger.isLoggable(PageServiceImpl.class, TechnicalLogSeverity.TRACE)).thenReturn(true);
        final long pageId = 15;
        final SPage expected = new SPageImpl("page1", 123456, 45, true);
        expected.setId(pageId);

        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                throw new SRecorderException("ouch !");
            }

        }).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        when(persistence.selectById(new SelectByIdDescriptor<SPage>("getPageById", SPage.class, pageId))).thenReturn(expected);
        when(loggerService.isLoggable(any(String.class), any(SQueriableLogSeverity.class))).thenReturn(true);

        serviceImpl.deletePage(pageId);

    }

    // @Test
    // public void updatePage() {
    // final EventService eventService = mock(EventService.class);
    // final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
    // final ReadPersistenceService persistence = mock(ReadPersistenceService.class);
    // final Recorder recorder = mock(Recorder.class);
    // final QueriableLoggerService loggerService = mock(QueriableLoggerService.class);
    // final PageServiceImpl serviceImpl = new PageServiceImpl(persistence, recorder, eventService, logger, loggerService);
    // when(logger.isLoggable(PageServiceImpl.class, TechnicalLogSeverity.TRACE)).thenReturn(true);
    // final long pageId = 15;
    // final SPage expected = new SPageImpl("page1", 123456, 45, true);
    // expected.setId(pageId);
    //
    // // doAnswer(new Answer<Object>() {
    // //
    // // @Override
    // // public Object answer(final InvocationOnMock invocation) throws Throwable {
    // // throw new SRecorderException("ouch !");
    // // }
    // //
    // // }).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
    // when(persistence.selectById(new SelectByIdDescriptor<SPage>("getPageById", SPage.class, pageId))).thenReturn(expected);
    // when(loggerService.isLoggable(any(String.class), any(SQueriableLogSeverity.class))).thenReturn(true);
    //
    // serviceImpl.deletePage(pageId);
    // }

    // @Test
    // public void updatePageContent() {
    // final EventService eventService = mock(EventService.class);
    // final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
    // final ReadPersistenceService persistence = mock(ReadPersistenceService.class);
    // final Recorder recorder = mock(Recorder.class);
    // final QueriableLoggerService loggerService = mock(QueriableLoggerService.class);
    // final PageServiceImpl serviceImpl = new PageServiceImpl(persistence, recorder, eventService, logger, loggerService);
    // when(logger.isLoggable(PageServiceImpl.class, TechnicalLogSeverity.TRACE)).thenReturn(true);
    // final long pageId = 15;
    // final SPage expected = new SPageImpl("page1", 123456, 45, true);
    // expected.setId(pageId);
    //
    // when(persistence.selectById(new SelectByIdDescriptor<SPage>("getPageById", SPage.class, pageId))).thenReturn(expected);
    // when(loggerService.isLoggable(any(String.class), any(SQueriableLogSeverity.class))).thenReturn(true);
    //
    // serviceImpl.updatePage(pageId, sPage)
    // }

}
