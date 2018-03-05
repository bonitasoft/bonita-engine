package org.bonitasoft.engine.page.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.page.SPageMapping;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PageMappingServiceImplTest {

    @Mock
    private Recorder recorder;

    @Mock
    private ReadPersistenceService persistenceService;

    @Mock
    private SessionService sessionService;

    @Mock
    private ReadSessionAccessor sessionAccessor;

    @InjectMocks
    private PageMappingServiceImpl pageMappingService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void get_should_return_page_mappings() throws Exception {
        final QueryOptions options = new QueryOptions(0, 2);
        final SelectListDescriptor<SPageMapping> listDescriptor = new SelectListDescriptor<SPageMapping>("getPageMappingByPageId",
                Collections.<String, Object> singletonMap("pageId", 1983L), SPageMapping.class, options);

        pageMappingService.get(1983L, 0, 2);

        verify(persistenceService).selectList(listDescriptor);
    }

    @Test(expected = SBonitaReadException.class)
    public void get_should_throw_an_exception() throws Exception {
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenThrow(new SBonitaReadException("exception"));

        pageMappingService.get(1983L, 0, 2);
    }

    @Test
    public void should_throw_a_SObjectCreationException_when_create_a_page_mapping_with_a_key_that_already_exists() throws Exception {
        final SPageMapping pageMapping = mock(SPageMapping.class);
        when(pageMapping.getPageId()).thenReturn(5L);
        when(persistenceService.selectOne(new SelectOneDescriptor<SPageMapping>("getPageMappingByKey", Collections
                .<String, Object> singletonMap("key", "aMappinKey"), SPageMapping.class))).thenReturn(pageMapping);

        expectedException.expect(SObjectCreationException.class);
        expectedException.expectMessage("Mapping key aMappinKey already exists for page with id 5");

        pageMappingService.create("aMappinKey", 1L, newArrayList("rule1"));
    }

    @Test
    public void should_throw_a_SObjectCreationException_when_create_a_page_mapping_with_a_key_that_already_exists_for_an_url() throws Exception {
        final SPageMapping pageMapping = mock(SPageMapping.class);
        when(pageMapping.getPageId()).thenReturn(5L);
        when(persistenceService.selectOne(new SelectOneDescriptor<SPageMapping>("getPageMappingByKey", Collections
                .<String, Object> singletonMap("key", "aMappinKey"), SPageMapping.class))).thenReturn(pageMapping);

        expectedException.expect(SObjectCreationException.class);
        expectedException.expectMessage("Mapping key aMappinKey already exists for page with id 5");

        pageMappingService.create("aMappinKey", "http://bonitasoft.com", "adapter", newArrayList("rule1"));
    }

    @Test
    public void should_create_a_page_mapping_for_a_given_page() throws Exception {
        when(persistenceService.selectOne(new SelectOneDescriptor<SPageMapping>("getPageMappingByKey", Collections
                .<String, Object> singletonMap("key", "aMappinKey"), SPageMapping.class))).thenReturn(null);

        pageMappingService.create("aMappinKey", 1L, newArrayList("rule1"));
        final ArgumentCaptor<InsertRecord> captor = ArgumentCaptor.forClass(InsertRecord.class);
        
        verify(recorder).recordInsert(captor.capture(), anyString());
        final InsertRecord insertRecord = captor.getValue();
        final PersistentObject entity = insertRecord.getEntity();
        assertThat(entity).isInstanceOf(SPageMapping.class);
        final SPageMapping mapping = (SPageMapping) entity;
        assertThat(mapping.getKey()).isEqualTo("aMappinKey");
        assertThat(mapping.getPageId()).isEqualTo(1L);
        assertThat(mapping.getPageAuthorizationRules()).containsOnly("rule1");
    }

    @Test
    public void should_create_a_page_mapping_for_a_given_url() throws Exception {
        when(persistenceService.selectOne(new SelectOneDescriptor<SPageMapping>("getPageMappingByKey", Collections
                .<String, Object> singletonMap("key", "aMappinKey"), SPageMapping.class))).thenReturn(null);

        pageMappingService.create("aMappinKey", "http://bonitasoft.com", "adapter", newArrayList("rule1"));
        final ArgumentCaptor<InsertRecord> captor = ArgumentCaptor.forClass(InsertRecord.class);

        verify(recorder).recordInsert(captor.capture(), anyString());
        final InsertRecord insertRecord = captor.getValue();
        final PersistentObject entity = insertRecord.getEntity();
        assertThat(entity).isInstanceOf(SPageMapping.class);
        final SPageMapping mapping = (SPageMapping) entity;
        assertThat(mapping.getKey()).isEqualTo("aMappinKey");
        assertThat(mapping.getUrl()).isEqualTo("http://bonitasoft.com");
        assertThat(mapping.getUrlAdapter()).isEqualTo("adapter");
        assertThat(mapping.getPageAuthorizationRules()).containsOnly("rule1");
    }

}
