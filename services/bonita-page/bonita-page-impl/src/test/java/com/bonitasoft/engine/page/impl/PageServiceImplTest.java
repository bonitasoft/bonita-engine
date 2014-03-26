/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.page.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

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
import com.bonitasoft.engine.page.SPageContent;

@RunWith(MockitoJUnitRunner.class)
public class PageServiceImplTest {

    @Mock
    private EventService eventService;

    @Mock
    private Recorder recorder;

    @Mock
    private TechnicalLoggerService technicalLoggerService;

    @Mock
    private ReadPersistenceService readPersistenceService;

    @Mock
    private QueryOptions queryOptions;

    @Mock
    private QueriableLoggerService queriableLoggerService;

    @Mock
    private SPageContent sPageContent;

    @Mock
    private EntityUpdateDescriptor entityUpdateDescriptor;

    private PageServiceImpl pageServiceImpl;

    @Before
    public void before() {

        when(technicalLoggerService.isLoggable(PageServiceImpl.class, TechnicalLogSeverity.TRACE)).thenReturn(true);
        when(queriableLoggerService.isLoggable(any(String.class), any(SQueriableLogSeverity.class))).thenReturn(true);

        pageServiceImpl = spy(new PageServiceImpl(readPersistenceService, recorder, eventService, technicalLoggerService, queriableLoggerService));

    }

    @Test
    public void getNumberOfPages() throws SBonitaException {

        // given
        final long expected = 50;
        when(readPersistenceService.getNumberOfEntities(SPage.class, queryOptions, null)).thenReturn(expected);

        // when
        final long numberOfPages = pageServiceImpl.getNumberOfPages(queryOptions);

        // then
        Assert.assertEquals(expected, numberOfPages);
    }

    @Test(expected = SBonitaReadException.class)
    public void getNumberOfPagesThrowsException() throws SBonitaException {
        // given
        final PageServiceImpl serviceImpl = new PageServiceImpl(readPersistenceService, null, eventService, technicalLoggerService, null);

        // when
        when(readPersistenceService.getNumberOfEntities(SPage.class, queryOptions, null)).thenThrow(new SBonitaReadException("ouch!"));
        serviceImpl.getNumberOfPages(queryOptions);

        // then
        // exception;
    }

    @Test
    public void getPage() throws SBonitaException {
        final PageServiceImpl serviceImpl = new PageServiceImpl(readPersistenceService, null, eventService, technicalLoggerService, null);

        final long pageId = 15;
        final SPage expected = new SPageImpl("page1", 123456, 45, true);
        expected.setId(pageId);
        when(readPersistenceService.selectById(new SelectByIdDescriptor<SPage>("getPageById", SPage.class, pageId))).thenReturn(expected);
        // when
        final SPage page = serviceImpl.getPage(pageId);
        // then
        Assert.assertEquals(expected, page);
    }

    @Test(expected = SObjectNotFoundException.class)
    public void getPageThrowsPageNotFoundException() throws SBonitaException {

        new PageServiceImpl(readPersistenceService, null, eventService, technicalLoggerService, null);

        final long pageId = 15;
        final SPage expected = new SPageImpl("page1", 123456, 45, true);
        expected.setId(pageId);
        when(readPersistenceService.selectById(new SelectByIdDescriptor<SPage>("getPageById", SPage.class, pageId))).thenReturn(null);

        pageServiceImpl.getPage(pageId);
    }

    @Test
    public void getPageByNameReturnsNullWhenNotFound() throws SBonitaException {
        // given: page does not exists
        // when
        final SPage pageByName = pageServiceImpl.getPageByName("unknown");
        // then
        assertTrue(pageByName == null);
    }

    @Test(expected = SBonitaReadException.class)
    public void getPageThrowsException() throws SBonitaException {

        final long pageId = 15;
        final SPage expected = new SPageImpl("page1", 123456, 45, true);
        expected.setId(pageId);
        when(readPersistenceService.selectById(new SelectByIdDescriptor<SPage>("getPageById", SPage.class, pageId))).thenThrow(
                new SBonitaReadException("ouch!"));

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
        final SPageImpl currentPage = new SPageImpl("example", "example", "example", System.currentTimeMillis(), -1, true, System.currentTimeMillis());
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
        final SPageImpl currentPage = new SPageImpl("example", "example", "example", System.currentTimeMillis(), -1, true, System.currentTimeMillis());
        currentPage.setId(12);
        doReturn(currentPage).when(pageServiceImpl).getPageByName("example");
        final InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("provided-page.zip");
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

        new PageServiceImpl(readPersistenceService, recorder, eventService, technicalLoggerService, queriableLoggerService);

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
        when(readPersistenceService.selectById(new SelectByIdDescriptor<SPage>("getPageById", SPage.class, pageId))).thenReturn(expected);
        doReturn(expected).when(pageServiceImpl).getPage(pageId);

        pageServiceImpl.deletePage(pageId);

        // FIXME assert ?
    }

    @Test(expected = SObjectModificationException.class)
    public void deletePageThrowsPageNotFoundException() throws SBonitaException {

        new PageServiceImpl(readPersistenceService, recorder, eventService, technicalLoggerService, queriableLoggerService);

        final long pageId = 15;
        final SPage expected = new SPageImpl("page1", 123456, 45, true);
        expected.setId(pageId);

        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                throw new SRecorderException("ouch !");
            }

        }).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        when(readPersistenceService.selectById(new SelectByIdDescriptor<SPage>("getPageById", SPage.class, pageId))).thenReturn(expected);

        pageServiceImpl.deletePage(pageId);

    }

    @Test(expected = SBonitaException.class)
    public void updatePageContent_should_check_zip_content() throws Exception {
        final long pageId = 15;
        final SPage sPage = new SPageImpl("page1", 123456, 45, true);
        final Map<String, Object> fields = new HashMap<String, Object>();

        // given
        sPage.setId(pageId);
        fields.put(SPageContentFields.PAGE_CONTENT, "aaa".getBytes());

        // when
        doReturn(fields).when(entityUpdateDescriptor).getFields();
        when(readPersistenceService.selectById(new SelectByIdDescriptor<SPage>("getPageById", SPage.class, pageId))).thenReturn(sPage);
        pageServiceImpl.updatePageContent(pageId, entityUpdateDescriptor);

        // then
        // exception
    }

    @Test(expected = SBonitaException.class)
    public void addPage_should_check_zip_content() throws Exception {

        // given
        final long pageId = 15;
        final Map<String, Object> fields = new HashMap<String, Object>();
        final SPage sPage = new SPageImpl("page1", 123456, 45, true);
        sPage.setId(pageId);
        final byte[] content = "invalid content".getBytes();
        fields.put(SPageContentFields.PAGE_CONTENT, content);
        when(readPersistenceService.selectById(new SelectByIdDescriptor<SPage>("getPageById", SPage.class, pageId))).thenReturn(sPage);
        doReturn(fields).when(entityUpdateDescriptor).getFields();

        // when
        pageServiceImpl.addPage(sPage, content);

        // then
        // exception

    }

    @Test(expected = SBonitaReadException.class)
    public void zipTestNotZip() throws Exception {

        // given
        final byte[] content = "badContent".getBytes();

        // when
        pageServiceImpl.checkContentIsValid(content);

        // then
        // exception
    }

    @Test(expected = SBonitaReadException.class)
    public void zipTestBadContent() throws Exception {

        // given
        final byte[] content = getPageContent("badContent.zip");

        // when
        pageServiceImpl.checkContentIsValid(content);

        // then
        // exception

    }

    @Test
    public void zipTestGroovy() throws Exception {

        // given
        final byte[] content = getPageContent("index.groovy.zip");

        // when then
        assertThat(pageServiceImpl.checkContentIsValid(content)).isTrue();

    }

    @Test
    public void zipTestHtml() throws Exception {

        // given
        final byte[] content = getPageContent("index.html.zip");

        // when then
        assertThat(pageServiceImpl.checkContentIsValid(content)).isTrue();

    }

    private byte[] getPageContent(final String zipResourceName) throws IOException, URISyntaxException {

        final File file = new File(getClass().getResource(zipResourceName).toURI());
        final byte[] buffer = new byte[(int) file.length()];
        InputStream ios = null;
        try {
            ios = new FileInputStream(file);
            if (ios.read(buffer) == -1) {
                return null;
            }
        } finally {
            try {
                if (ios != null) {
                    ios.close();
                }
            } catch (final IOException e) {
            }
        }

        return buffer;
    }

}
