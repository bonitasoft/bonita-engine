/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.page.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.commons.io.IOUtil;
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
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.bonitasoft.engine.page.SPage;

@RunWith(MockitoJUnitRunner.class)
public class PageServiceImplTest {

    @Mock
    private EventService eventService;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private ReadPersistenceService persistence;

    @Mock
    private QueriableLoggerService loggerService;

    @Mock
    private Recorder recorder;

    private PageServiceImpl pageServiceImpl;

    @Before
    public void before() {
        pageServiceImpl = spy(new PageServiceImpl(persistence, recorder, eventService, logger, loggerService));
        when(logger.isLoggable(PageServiceImpl.class, TechnicalLogSeverity.TRACE)).thenReturn(true);
    }

    @Test
    public void getNumberOfPages() throws SBonitaException {
        final PageServiceImpl serviceImpl = new PageServiceImpl(persistence, null, eventService, logger, null);
        final QueryOptions options = mock(QueryOptions.class);
        final long expected = 50;
        when(persistence.getNumberOfEntities(SPage.class, options, null)).thenReturn(expected);
        final long numberOfPages = serviceImpl.getNumberOfPages(options);
        Assert.assertEquals(expected, numberOfPages);
    }

    @Test(expected = SBonitaReadException.class)
    public void getNumberOfPagesThrowsException() throws SBonitaException {
        final QueryOptions options = mock(QueryOptions.class);
        when(logger.isLoggable(PageServiceImpl.class, TechnicalLogSeverity.TRACE)).thenReturn(true);
        when(persistence.getNumberOfEntities(SPage.class, options, null)).thenThrow(new SBonitaReadException("ouch!"));
        pageServiceImpl.getNumberOfPages(options);
    }

    @Test
    public void getPage() throws SBonitaException {
        final long pageId = 15;
        final SPage expected = new SPageImpl("page1", 123456, 45, true);
        expected.setId(pageId);
        when(persistence.selectById(new SelectByIdDescriptor<SPage>("getPageById", SPage.class, pageId))).thenReturn(expected);

        final SPage page = pageServiceImpl.getPage(pageId);
        Assert.assertEquals(expected, page);
    }

    @Test(expected = SObjectNotFoundException.class)
    public void getPageThrowsPageNotFoundException() throws SBonitaException {
        final long pageId = 15;
        final SPage expected = new SPageImpl("page1", 123456, 45, true);
        expected.setId(pageId);
        when(persistence.selectById(new SelectByIdDescriptor<SPage>("getPageById", SPage.class, pageId))).thenReturn(null);

        pageServiceImpl.getPage(pageId);
    }

    @Test
    public void getPageByNameReturnsNullWhenNotFound() throws SBonitaException {
        // given: page does not exists
        // when
        SPage pageByName = pageServiceImpl.getPageByName("unknown");
        // then
        assertTrue(pageByName == null);
    }

    @Test(expected = SBonitaReadException.class)
    public void getPageThrowsException() throws SBonitaException {
        final long pageId = 15;
        final SPage expected = new SPageImpl("page1", 123456, 45, true);
        expected.setId(pageId);
        when(persistence.selectById(new SelectByIdDescriptor<SPage>("getPageById", SPage.class, pageId))).thenThrow(new SBonitaReadException("ouch!"));

        pageServiceImpl.getPage(pageId);
    }

    @Test
    public void should_start_import_provided_page() throws SBonitaException {
        // given
        // resource in the classpath provided-page.properties and provided-page.zip
        doReturn(null).when(pageServiceImpl).addPage(any(SPage.class), any(byte[].class));
        // when
        pageServiceImpl.start();
        // then
        verify(pageServiceImpl, times(1)).addPage(any(SPage.class), any(byte[].class));
        verify(pageServiceImpl, times(0)).updatePage(anyLong(), any(EntityUpdateDescriptor.class));
        verify(pageServiceImpl, times(0)).updatePageContent(anyLong(), any(EntityUpdateDescriptor.class));

    }

    @Test
    public void should_start_update_provided_page_if_different() throws SBonitaException {
        // given
        // resource in the classpath provided-page.properties and provided-page.zip
        SPageImpl currentPage = new SPageImpl("example", "example", "example", System.currentTimeMillis(), -1, true, System.currentTimeMillis());
        currentPage.setId(12);
        doReturn(currentPage).when(pageServiceImpl).getPageByName("example");
        doReturn(new byte[] { 1, 2, 3 }).when(pageServiceImpl).getPageContent(12);
        doReturn(null).when(pageServiceImpl).addPage(any(SPage.class), any(byte[].class));
        doReturn(null).when(pageServiceImpl).updatePage(anyLong(), any(EntityUpdateDescriptor.class));
        doNothing().when(pageServiceImpl).updatePageContent(anyLong(), any(EntityUpdateDescriptor.class));
        // when
        pageServiceImpl.start();
        // then
        verify(pageServiceImpl, times(0)).addPage(any(SPage.class), any(byte[].class));
        verify(pageServiceImpl, times(1)).updatePage(eq(12l), any(EntityUpdateDescriptor.class));
        verify(pageServiceImpl, times(1)).updatePageContent(eq(12l), any(EntityUpdateDescriptor.class));
    }

    @Test
    public void should_start_do_nothing_if_already_here_and_the_same() throws SBonitaException, IOException {
        // given
        // resource in the classpath provided-page.properties and provided-page.zip
        SPageImpl currentPage = new SPageImpl("example", "example", "example", System.currentTimeMillis(), -1, true, System.currentTimeMillis());
        currentPage.setId(12);
        doReturn(currentPage).when(pageServiceImpl).getPageByName("example");
        InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("provided-page.zip");
        doReturn(IOUtil.getAllContentFrom(resourceAsStream)).when(pageServiceImpl).getPageContent(12);
        doReturn(null).when(pageServiceImpl).addPage(any(SPage.class), any(byte[].class));
        // when
        pageServiceImpl.start();
        // then
        verify(pageServiceImpl, times(0)).addPage(any(SPage.class), any(byte[].class));
        verify(pageServiceImpl, times(0)).updatePage(anyLong(), any(EntityUpdateDescriptor.class));
        verify(pageServiceImpl, times(0)).updatePageContent(anyLong(), any(EntityUpdateDescriptor.class));
    }

    @Test
    public void deletePage() throws SBonitaException {
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

        pageServiceImpl.deletePage(pageId);
    }

    @Test(expected = SObjectModificationException.class)
    public void deletePageThrowsPageNotFoundException() throws SBonitaException {
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

        pageServiceImpl.deletePage(pageId);

    }

}
