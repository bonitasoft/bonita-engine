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
import static org.bonitasoft.engine.commons.Pair.pair;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.bonitasoft.engine.page.SPage;
import com.bonitasoft.engine.page.SPageContent;
import com.bonitasoft.engine.page.SPageLogBuilder;
import com.bonitasoft.engine.page.impl.exception.SInvalidPageZipContentException;
import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;

@RunWith(MockitoJUnitRunner.class)
public class PageServiceImplTest {

    private static final String PAGE_PROPERTIES = "page.properties";

    private static final String INDEX_HTML = "index.html";

    private static final String INDEX_GROOVY = "Index.groovy";

    private static final String CONTENT_NAME = "content.zip";

    private static final boolean PROVIDED_TRUE = true;

    private static final int INSTALLED_BY_ID = 45;

    private static final int INSTALLATION_DATE_AS_LONG = 123456;

    private static final String PAGE_NAME = "pageName";

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
    private Manager manager;

    @Mock
    private SPageLogBuilder pageLogBuilder;

    @Mock
    private EntityUpdateDescriptor entityUpdateDescriptor;

    @Mock
    ProfileService profileService;

    private PageServiceImpl pageServiceImpl;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void before() {
        doReturn(true).when(manager).isFeatureActive(Features.CUSTOM_PAGE);

        when(technicalLoggerService.isLoggable(PageServiceImpl.class, TechnicalLogSeverity.DEBUG)).thenReturn(true);
        when(queriableLoggerService.isLoggable(any(String.class), any(SQueriableLogSeverity.class))).thenReturn(true);

        pageServiceImpl = spy(new PageServiceImpl(manager, readPersistenceService, recorder, eventService, technicalLoggerService, queriableLoggerService,
                profileService));
        doReturn(pageLogBuilder).when(pageServiceImpl).getPageLog(any(ActionType.class), anyString());
        doNothing().when(pageServiceImpl).initiateLogBuilder(anyLong(), anyInt(), any(SPersistenceLogBuilder.class), anyString());

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
        // when
        when(readPersistenceService.getNumberOfEntities(SPage.class, queryOptions, null)).thenThrow(new SBonitaReadException("ouch!"));
        pageServiceImpl.getNumberOfPages(queryOptions);

        // then
        // exception;
    }

    @Test
    public void createPage_should_throw_exception_when_name_is_empty() throws SBonitaException {

        final long pageId = 15;
        final SPage pageWithEmptyName = new SPageImpl("", 123456, 45, true, CONTENT_NAME);
        pageWithEmptyName.setId(pageId);
        try {
            pageServiceImpl.addPage(pageWithEmptyName, new byte[] { 1, 2, 3 });
            fail("should not be able to create a page with empty name");
        } catch (final SObjectCreationException e) {
            assertTrue(e.getMessage().contains("empty name"));
        }

    }

    @Test(expected = SObjectAlreadyExistsException.class)
    public void addPage_should_throw_exception_when_badContent() throws Exception {

        // given
        final SPage newPage = new SPageImpl(PAGE_NAME, INSTALLATION_DATE_AS_LONG, INSTALLED_BY_ID, PROVIDED_TRUE, CONTENT_NAME);

        // when
        when(pageServiceImpl.getPageByName(PAGE_NAME)).thenReturn(newPage);
        pageServiceImpl.addPage(newPage, validPageContent());

        // then exception

    }

    @Test(expected = SObjectAlreadyExistsException.class)
    public void should_create_page_throw_exception_when_name_exists() throws Exception {

        // given
        final SPage newPage = new SPageImpl(PAGE_NAME, 123456, 45, true, CONTENT_NAME);

        // when
        when(pageServiceImpl.getPageByName(PAGE_NAME)).thenReturn(newPage);
        final byte[] validContent = validPageContent();
        pageServiceImpl.addPage(newPage, validContent);

        // then exception

    }

    private byte[] validPageContent() throws IOException {
        return IOUtil.zip(Collections.singletonMap("Index.groovy", "content of the groovy".getBytes()));
    }

    @Test
    public void getPage() throws SBonitaException {
        final long pageId = 15;
        final SPage expected = new SPageImpl("page1", 123456, 45, true, CONTENT_NAME);
        expected.setId(pageId);
        when(readPersistenceService.selectById(new SelectByIdDescriptor<SPage>("getPageById", SPage.class, pageId))).thenReturn(expected);
        // when
        final SPage page = pageServiceImpl.getPage(pageId);
        // then
        Assert.assertEquals(expected, page);
    }

    @Test(expected = SObjectNotFoundException.class)
    public void getPageThrowsPageNotFoundException() throws SBonitaException {

        final long pageId = 15;
        final SPage expected = new SPageImpl("page1", 123456, 45, true, CONTENT_NAME);
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
        final SPage expected = new SPageImpl("page1", 123456, 45, true, CONTENT_NAME);
        expected.setId(pageId);
        when(readPersistenceService.selectById(new SelectByIdDescriptor<SPage>("getPageById", SPage.class, pageId))).thenThrow(
                new SBonitaReadException("ouch!"));

        pageServiceImpl.getPage(pageId);
    }

    @Test
    public void start_should_import_provided_page() throws SBonitaException {
        // given
        // resource in the classpath bonita-groovy-example-page.zip
        doReturn(null).when(pageServiceImpl).addPage(any(SPage.class), any(byte[].class));

        // when
        pageServiceImpl.start();

        // then
        verify(pageServiceImpl, times(1)).addPage(any(SPage.class), any(byte[].class));
        verify(pageServiceImpl, times(0)).updatePage(anyLong(), any(EntityUpdateDescriptor.class));
        verify(pageServiceImpl, times(0)).updatePageContent(anyLong(), any(EntityUpdateDescriptor.class));

    }

    @Test
    public void start_should_update_provided_page_if_different() throws SBonitaException {
        // given
        // resource in the classpath provided-page.properties and provided-page.zip
        final SPageImpl currentPage = new SPageImpl("groovy-example", "example", "example", System.currentTimeMillis(), -1, true, System.currentTimeMillis(),
                -1,
                CONTENT_NAME);
        currentPage.setId(12);
        doReturn(currentPage).when(pageServiceImpl).getPageByName("groovy-example");
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
    public void getPageContent_should_add_properties_in_the_zip() throws SBonitaException, IOException {
        // given: a zip without properties
        final SPageImpl page = new SPageImpl("mypage", "mypage description", "mypage display name", System.currentTimeMillis(), -1, false,
                System.currentTimeMillis(),
                -1,
                CONTENT_NAME);
        page.setId(12);
        final byte[] content = IOUtil.zip(Collections.singletonMap("Index.groovy", "content of the groovy".getBytes()));
        doReturn(new SPageContentBuilderFactoryImpl().createNewInstance(content).done()).when(readPersistenceService).selectById(
                new SelectByIdDescriptor<SPageContent>("getPageContent",
                        SPageContent.class, 12));
        doReturn(page).when(pageServiceImpl).getPage(12);
        // when
        final byte[] result = pageServiceImpl.getPageContent(12);
        // then
        final Map<String, byte[]> unzip = IOUtil.unzip(result);
        assertThat(unzip.size()).isEqualTo(2);
        final Properties pageProperties = new Properties();
        pageProperties.load(new ByteArrayInputStream(unzip.get(PAGE_PROPERTIES)));

        assertThat(pageProperties.get("name")).isEqualTo("mypage");
        assertThat(pageProperties.get("displayName")).isEqualTo("mypage display name");
        assertThat(pageProperties.get("description")).isEqualTo("mypage description");
    }

    @Test
    public void getPageContent_should_update_properties_in_the_zip_if_exists() throws SBonitaException, IOException {
        // given: a zip with outdated properties
        final SPageImpl page = new SPageImpl("mypageUpdated", "mypageUpdated description", "mypageUpdated display name", System.currentTimeMillis(), -1, false,
                System.currentTimeMillis(),
                -1,
                CONTENT_NAME);
        page.setId(12);
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(pair("Index.groovy", "content of the groovy".getBytes()),
                pair(PAGE_PROPERTIES, "name=custompage_mypage\ndisplayName=mypage display name\ndescription=mypage description\n".getBytes()));
        doReturn(new SPageContentBuilderFactoryImpl().createNewInstance(content).done()).when(readPersistenceService).selectById(
                new SelectByIdDescriptor<SPageContent>("getPageContent",
                        SPageContent.class, 12));
        doReturn(page).when(pageServiceImpl).getPage(12);
        // when
        final byte[] result = pageServiceImpl.getPageContent(12);
        // then
        final Map<String, byte[]> unzip = IOUtil.unzip(result);
        assertThat(unzip.size()).isEqualTo(2);
        final Properties pageProperties = new Properties();
        pageProperties.load(new ByteArrayInputStream(unzip.get(PAGE_PROPERTIES)));

        assertThat(pageProperties.get("name")).isEqualTo("mypageUpdated");
        assertThat(pageProperties.get("displayName")).isEqualTo("mypageUpdated display name");
        assertThat(pageProperties.get("description")).isEqualTo("mypageUpdated description");
    }

    @Test
    public void start_should_do_nothing_if_already_here_and_the_same() throws SBonitaException, IOException {
        // given
        // resource in the classpath provided-page.properties and provided-page.zip
        final SPageImpl currentGroovyPage = new SPageImpl("groovy-example", "example", "example", System.currentTimeMillis(), -1, true,
                System.currentTimeMillis(),
                -1,
                CONTENT_NAME);

        currentGroovyPage.setId(12);
        doReturn(currentGroovyPage).when(pageServiceImpl).getPageByName("groovy-example");

        final InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("bonita-groovy-page-example.zip");
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
        final SPage expected = new SPageImpl("page1", 123456, 45, true, CONTENT_NAME);
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

        verify(recorder, times(1)).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));

    }

    @Test(expected = SObjectModificationException.class)
    public void deletePageThrowsPageNotFoundException() throws SBonitaException {

        final long pageId = 15;
        final SPage expected = new SPageImpl("page1", 123456, 45, true, CONTENT_NAME);
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
        final SPage sPage = new SPageImpl("page1", 123456, 45, true, CONTENT_NAME);
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

    @Test(expected = SObjectAlreadyExistsException.class)
    public void updatePageWithExistingName() throws Exception {

        final long pageId1 = 15;
        final long pageId2 = 20;

        final SPage page1 = new SPageImpl("page1", 123456, 45, true, CONTENT_NAME);
        final SPage page2 = new SPageImpl("page2", 123456, 45, true, CONTENT_NAME);
        final byte[] content = IOUtil.zip(Collections.singletonMap("Index.groovy", "content of the groovy".getBytes()));
        final Map<String, Object> fields = new HashMap<String, Object>();

        doReturn(fields).when(entityUpdateDescriptor).getFields();
        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                throw new SRecorderException("ouch !");
            }

        }).when(recorder).recordUpdate(any(UpdateRecord.class), any(SUpdateEvent.class));
        when(readPersistenceService.selectById(new SelectByIdDescriptor<SPage>("getPageById", SPage.class, pageId2))).thenReturn(page2);
        when(pageServiceImpl.getPageByName(page1.getName())).thenReturn(page1);

        // given
        pageServiceImpl.addPage(page1, content);
        page1.setId(pageId1);

        pageServiceImpl.addPage(page2, content);
        page2.setId(pageId2);

        // when

        // try to update page2 with page1 name
        fields.put(SPageFields.PAGE_NAME, page1.getName());
        fields.put(SPageFields.PAGE_ID, page1.getId());

        pageServiceImpl.updatePage(page2.getId(), entityUpdateDescriptor);

        // then
        // exception

    }

    @Test(expected = SBonitaException.class)
    public void addPage_should_check_zip_content() throws Exception {

        // given
        final long pageId = 15;
        final Map<String, Object> fields = new HashMap<String, Object>();
        final SPage sPage = new SPageImpl("page1", 123456, 45, false, CONTENT_NAME);
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

    @Test
    public void zipTest_not_a_zip() throws Exception {
        exception.expect(SInvalidPageZipContentException.class);
        exception.expectMessage(PageServiceImpl.PAGE_CONTENT_IS_NOT_A_VALID_ZIP_FILE);
        // given
        final byte[] content = "badContent".getBytes();

        // when
        pageServiceImpl.checkContentIsValid(content);

        // then exception
    }

    @Test
    public void zipTest_Bad_Content() throws Exception {
        exception.expect(SInvalidPageZipContentException.class);
        exception.expectMessage(PageServiceImpl.PAGE_CONTENT_DOES_NOT_CONTAINS_A_INDEX_GROOVY_OR_INDEX_HTML_FILE);

        // given
        final byte[] content = IOUtil.zip(Collections.singletonMap("aFile.txt", "hello".getBytes()));

        // when
        pageServiceImpl.checkContentIsValid(content);

        // then
        // exception

    }

    @Test
    public void zipTest_valid_Groovy() throws Exception {

        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(pair(INDEX_GROOVY, "content of the groovy".getBytes()),
                pair(PAGE_PROPERTIES, "name=custompage_mypage\ndisplayName=mypage display name\ndescription=mypage description\n".getBytes()));

        // when then
        assertThat(pageServiceImpl.checkContentIsValid(content)).isTrue();

    }

    @Test
    public void zipTest_page_properties_no_description() throws Exception {
        exception.expect(SBonitaReadException.class);
        exception.expectMessage(PageServiceImpl.PAGE_PROPERTIES_CONTENT_IS_NOT_VALID);

        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(pair(INDEX_GROOVY, "content of the groovy".getBytes()),
                pair(PAGE_PROPERTIES, "name=custompage_mypage\ndisplayName=mypage display name\n".getBytes()));

        // when then
        assertThat(pageServiceImpl.checkContentIsValid(content)).isTrue();

    }

    @Test
    public void zipTest_page_properties_invalid_name() throws Exception {
        exception.expect(SBonitaReadException.class);
        exception.expectMessage(PageServiceImpl.PAGE_PROPERTIES_CONTENT_IS_NOT_VALID);

        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(pair(INDEX_GROOVY, "content of the groovy".getBytes()),
                pair(PAGE_PROPERTIES, "name=mypage\ndisplayName=mypage display name\ndescription=mypage description\n".getBytes()));

        // when then
        assertThat(pageServiceImpl.checkContentIsValid(content)).isTrue();

    }

    @Test
    public void zipTest_page_properties_no_name() throws Exception {
        exception.expect(SBonitaReadException.class);
        exception.expectMessage(PageServiceImpl.PAGE_PROPERTIES_CONTENT_IS_NOT_VALID);

        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(pair(INDEX_GROOVY, "content of the groovy".getBytes()),
                pair(PAGE_PROPERTIES, "displayName=mypage display name\ndescription=mypage description\n".getBytes()));

        // when then
        assertThat(pageServiceImpl.checkContentIsValid(content)).isTrue();

    }

    @Test
    public void zipTest_page_properties_invalid_display_name() throws Exception {
        exception.expect(SInvalidPageZipContentException.class);
        exception.expectMessage(PageServiceImpl.PAGE_PROPERTIES_CONTENT_IS_NOT_VALID);

        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(pair(INDEX_GROOVY, "content of the groovy".getBytes()),
                pair(PAGE_PROPERTIES, "name=custompage_mypage\ndisplayName=\ndescription=mypage description\n".getBytes()));

        // when then
        assertThat(pageServiceImpl.checkContentIsValid(content)).isTrue();

    }

    @Test
    public void zipTest_page_properties_no_display_name() throws Exception {
        exception.expect(SBonitaReadException.class);
        exception.expectMessage(PageServiceImpl.PAGE_PROPERTIES_CONTENT_IS_NOT_VALID);

        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(pair(INDEX_GROOVY, "content of the groovy".getBytes()),
                pair(PAGE_PROPERTIES, "name=custompage_mypage\ndescription=mypage description\n".getBytes()));

        // when then
        assertThat(pageServiceImpl.checkContentIsValid(content)).isTrue();

    }

    @Test
    public void zipTestGroovyWithWrongName() throws Exception {
        exception.expect(SBonitaReadException.class);
        exception.expectMessage(PageServiceImpl.PAGE_CONTENT_DOES_NOT_CONTAINS_A_INDEX_GROOVY_OR_INDEX_HTML_FILE);

        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(pair("index.groovy", "content of the groovy".getBytes()),
                pair(PAGE_PROPERTIES, "name=custompage_mypage\ndisplayName=mypage display name\ndescription=mypage description\n".getBytes()));

        // when then
        assertThat(pageServiceImpl.checkContentIsValid(content)).isFalse();

    }

    @Test
    public void zipTest_valid_Html() throws Exception {
        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(pair(INDEX_HTML, "content of the groovy".getBytes()),
                pair(PAGE_PROPERTIES, "name=custompage_mypage\ndisplayName=mypage final display name\ndescription=final mypage description\n".getBytes()));

        // when then
        assertThat(pageServiceImpl.checkContentIsValid(content)).isTrue();

    }

    @Test
    public void zipTest_no_page_properties() throws Exception {
        exception.expect(SInvalidPageZipContentException.class);
        exception.expectMessage(PageServiceImpl.PAGE_CONTENT_DOES_NOT_CONTAINS_A_PAGE_PROPERTIES_FILE);

        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(pair(INDEX_HTML, "content of the groovy".getBytes()));

        // when then
        assertThat(pageServiceImpl.checkContentIsValid(content)).isTrue();

    }

    @Test
    public void checkPageContentIsValid_null() throws Exception {
        exception.expect(SBonitaReadException.class);
        // given

        // when
        pageServiceImpl.checkPageContentIsValid(null);

        // then

    }

    @Test
    public void checkPageContentIsValid_noFields() throws Exception {
        exception.expect(SBonitaReadException.class);

        // given
        final Map<String, Object> fields = new HashMap<String, Object>();
        doReturn(fields).when(entityUpdateDescriptor).getFields();

        // when
        pageServiceImpl.checkPageContentIsValid(entityUpdateDescriptor);

        // then exception

    }

    @Test
    public void checkPageContentIsValid_badZip() throws Exception {
        exception.expect(SBonitaReadException.class);

        // given
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put(SPageContentFields.PAGE_CONTENT, "not a zip".getBytes());
        doReturn(fields).when(entityUpdateDescriptor).getFields();

        // when
        pageServiceImpl.checkPageContentIsValid(entityUpdateDescriptor);

        // then

    }

    @Test
    public void checkPageContentIsValid_validZip() throws Exception {

        // given
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put(SPageContentFields.PAGE_CONTENT, IOUtil.zip(pair(INDEX_GROOVY, "content of the groovy".getBytes()),
                pair(PAGE_PROPERTIES, "name=custompage_mypage\ndisplayName=mypage display name\ndescription=mypage description\n".getBytes())));

        doReturn(fields).when(entityUpdateDescriptor).getFields();

        // when
        pageServiceImpl.checkPageContentIsValid(entityUpdateDescriptor);

        // then no exception

    }

    @Test
    public void checkPageContentIsValid_badField() throws Exception {
        exception.expect(SBonitaReadException.class);

        // given
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put(SPageContentFields.PAGE_ID, 1);
        doReturn(fields).when(entityUpdateDescriptor).getFields();

        // when
        pageServiceImpl.checkPageContentIsValid(entityUpdateDescriptor);

        // then exception

    }

}
