/*
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
 */
package org.bonitasoft.engine.core.form.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.bonitasoft.engine.page.AuthorizationRuleConstants.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;

import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.form.FormMappingKeyGenerator;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.page.PageMappingService;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.impl.SPageImpl;
import org.bonitasoft.engine.page.impl.SPageMappingImpl;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class FormMappingServiceImplTest {

    private static final long PROCESS_DEFINITION_ID = 456123l;
    private static final long ID = 154l;
    private static final Long PAGE_ID = 554l;
    public static final String EXTERNAL = "external";
    private static final String LEGACY = "legacy";
    @Mock
    private Recorder recorder;
    @Mock
    private ReadPersistenceService persistenceService;
    @Mock
    private SessionService sessionService;
    @Mock
    private ReadSessionAccessor sessionAccessor;
    @Mock
    private FormMappingKeyGenerator formMappingKeyGenerator;
    @Mock
    private PageMappingService pageMappingService;
    @Mock
    private PageService pageService;
    @Captor
    private ArgumentCaptor<SUpdateEvent> updateEventCaptor;
    @Captor
    private ArgumentCaptor<UpdateRecord> updateRecordCaptor;

    @InjectMocks
    private FormMappingServiceImpl formMappingService;

    @Before
    public void before() throws Exception {
        formMappingService = new FormMappingServiceImpl(recorder, persistenceService, sessionService, sessionAccessor, pageMappingService, pageService,
                formMappingKeyGenerator, EXTERNAL, LEGACY);
        doThrow(SObjectNotFoundException.class).when(pageService).getPage(anyLong());
        doReturn(new SPageImpl("myPage", 0, 0, false, "page.zip")).when(pageService).getPage(PAGE_ID);
    }

    @Test
    public void createFormMappingWithUndefinedTargetShouldNotAddPageMapping() throws Exception {
        doReturn("mockedKey").when(formMappingKeyGenerator).generateKey(PROCESS_DEFINITION_ID, "someHumanTask", 84);

        formMappingService.create(PROCESS_DEFINITION_ID, "someHumanTask", 84, SFormMapping.TARGET_UNDEFINED, null);

        verifyZeroInteractions(pageMappingService);
    }

    @Test
    public void createWithInternalPageShouldCallPageMapping_create() throws Exception {
        doReturn("theKey").when(formMappingKeyGenerator).generateKey(PROCESS_DEFINITION_ID, "step1", 2);

        formMappingService.create(PROCESS_DEFINITION_ID, "step1", 2, SFormMapping.TARGET_INTERNAL, null);

        verify(pageMappingService).create(eq("theKey"), isNull(Long.class), anyList());
    }

    @Test
    public void createForTaskShouldAddCorrectAuthorizations() throws Exception {
        doReturn("clef").when(formMappingKeyGenerator).generateKey(PROCESS_DEFINITION_ID, "task", FormMappingType.TASK.getId());

        formMappingService.create(PROCESS_DEFINITION_ID, "task", FormMappingType.TASK.getId(), SFormMapping.TARGET_INTERNAL, null);

        verify(pageMappingService).create("clef", null, Arrays.asList(IS_ADMIN, IS_PROCESS_OWNER, IS_TASK_AVAILABLE_FOR_USER));
    }

    @Test
    public void createForProcessStartShouldAddCorrectAuthorizations() throws Exception {
        doReturn("keye").when(formMappingKeyGenerator).generateKey(PROCESS_DEFINITION_ID, null, FormMappingType.PROCESS_START.getId());

        formMappingService.create(PROCESS_DEFINITION_ID, null, FormMappingType.PROCESS_START.getId(), SFormMapping.TARGET_URL, null);

        verify(pageMappingService).create("keye", null, EXTERNAL, Arrays.asList(IS_ADMIN, IS_PROCESS_OWNER, IS_ACTOR_INITIATOR));
    }


    @Test
    public void createLegacyFormShouldNotAddCorrectAuthorizations() throws Exception {
        doReturn("keye").when(formMappingKeyGenerator).generateKey(PROCESS_DEFINITION_ID, null, FormMappingType.PROCESS_START.getId());

        formMappingService.create(PROCESS_DEFINITION_ID, null, FormMappingType.PROCESS_START.getId(), SFormMapping.TARGET_LEGACY, null);

        verify(pageMappingService).create("keye", null, LEGACY, null);
    }

    @Test
    public void createForProcessOverviewShouldAddCorrectAuthorizations() throws Exception {
        doReturn("clave").when(formMappingKeyGenerator).generateKey(PROCESS_DEFINITION_ID, null, FormMappingType.PROCESS_OVERVIEW.getId());

        formMappingService.create(PROCESS_DEFINITION_ID, null, FormMappingType.PROCESS_OVERVIEW.getId(), SFormMapping.TARGET_URL, null);

        verify(pageMappingService).create("clave", null, EXTERNAL,
                Arrays.asList(IS_ADMIN, IS_PROCESS_OWNER, IS_PROCESS_INITIATOR, IS_TASK_PERFORMER, IS_INVOLVED_IN_PROCESS_INSTANCE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNullTarget() throws Exception {
        formMappingService.create(1654L, "step1", 1, null, null);
    }

    @Test
    public void testGetNumberOfFormMappings() throws Exception {
        QueryOptions queryOptions = mock(QueryOptions.class);
        formMappingService.getNumberOfFormMappings(queryOptions);

        verify(persistenceService).getNumberOfEntities(SFormMapping.class, queryOptions, Collections.<String, Object> emptyMap());
    }

    @Test
    public void testGetByKey() throws Exception {
        formMappingService.get("theKey");

        verify(persistenceService).selectOne(
                new SelectOneDescriptor<SFormMapping>("getFormMappingByKey", Collections.<String, Object> singletonMap("key", "theKey"), SFormMapping.class));
    }

    @Test
    public void testSearchFormMappings() throws Exception {
        QueryOptions queryOptions = mock(QueryOptions.class);
        formMappingService.searchFormMappings(queryOptions);

        verify(persistenceService).searchEntity(SFormMapping.class, queryOptions, Collections.<String, Object> emptyMap());
    }

    @Test
    public void test_update_with_url() throws Exception {
        SFormMapping formMapping = createFormMapping(ID);

        formMappingService.update(formMapping, "http://fake.url", null);

        verify(recorder).recordUpdate(updateRecordCaptor.capture(), updateEventCaptor.capture());
        assertThat(updateRecordCaptor.getValue().getFields()).contains(entry("pageMapping.url", "http://fake.url"), entry("pageMapping.pageId", null),
                entry("pageMapping.urlAdapter", EXTERNAL));
    }

    private SFormMapping createFormMapping(long id) {
        SFormMappingImpl sFormMapping = new SFormMappingImpl();
        sFormMapping.setId(id);
        SPageMappingImpl pageMapping = new SPageMappingImpl();
        sFormMapping.setPageMapping(pageMapping);
        return sFormMapping;
    }

    @Test
    public void test_update_with_page() throws Exception {
        SFormMapping formMapping = createFormMapping(ID);

        formMappingService.update(formMapping, null, PAGE_ID);

        verify(recorder).recordUpdate(updateRecordCaptor.capture(), updateEventCaptor.capture());
        assertThat(updateRecordCaptor.getValue().getFields()).contains(entry("pageMapping.url", null), entry("pageMapping.pageId", PAGE_ID),
                entry("pageMapping.urlAdapter", null));
    }

    @Test(expected = SObjectModificationException.class)
    public void test_update_with_page_that_does_not_exists() throws Exception {
        SFormMapping formMapping = createFormMapping(ID);

        formMappingService.update(formMapping, null, 557441l);
    }

    @Test(expected = SObjectModificationException.class)
    public void test_update_with_invalid_parameters1() throws Exception {
        SFormMapping formMapping = createFormMapping(ID);

        formMappingService.update(formMapping, "plop", PAGE_ID);
    }

    @Test(expected = SObjectModificationException.class)
    public void test_update_with_invalid_parameters2() throws Exception {
        SFormMapping formMapping = createFormMapping(ID);

        formMappingService.update(formMapping, "", PAGE_ID);
    }

    @Test(expected = SObjectModificationException.class)
    public void test_update_with_invalid_parameters3() throws Exception {
        SFormMapping formMapping = createFormMapping(ID);

        formMappingService.update(formMapping, "", null);
    }

    @Test(expected = SObjectModificationException.class)
    public void test_update_with_invalid_parameters4() throws Exception {
        SFormMapping formMapping = createFormMapping(ID);

        formMappingService.update(formMapping, null, null);
    }
}
