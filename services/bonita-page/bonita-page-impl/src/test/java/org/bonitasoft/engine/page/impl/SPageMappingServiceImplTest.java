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
 */

package org.bonitasoft.engine.page.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SDeletionException;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.page.AuthorizationRule;
import org.bonitasoft.engine.page.SAuthorizationException;
import org.bonitasoft.engine.page.SPageMapping;
import org.bonitasoft.engine.page.SPageURL;
import org.bonitasoft.engine.page.URLAdapter;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class SPageMappingServiceImplTest {

    public static final long PAGE_ID = 2812l;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private Recorder recorder;

    @Mock
    private ReadPersistenceService persistenceService;

    @Mock
    private SessionService sessionService;

    @Mock
    private ReadSessionAccessor readSessionAccessor;

    @InjectMocks
    private PageMappingServiceImpl pageMappingService;

    @Before
    public void before() {
        URLAdapter urlAdapter = new URLAdapter() {

            @Override
            public String adapt(String url, String key, Map<String, Serializable> context) {
                return url + "_adapted_" + context.size();
            }

            @Override
            public String getId() {
                return "testAdapter";
            }
        };
        pageMappingService.setURLAdapters(Collections.singletonList(urlAdapter));
    }

    @Test
    public void should_create_return_the_created_element() throws Exception {
        //given
        //when
        final List<String> authorizationRules = new ArrayList<>(2);
        authorizationRules.add("toto");
        authorizationRules.add("titi");
        SPageMapping sPageMapping = pageMappingService.create("theKey", PAGE_ID, authorizationRules);
        //then
        assertThat(sPageMapping).isNotNull();
        assertThat(sPageMapping.getKey()).isEqualTo("theKey");
        assertThat(sPageMapping.getPageId()).isEqualTo(PAGE_ID);
        assertThat(sPageMapping.getUrl()).isEqualTo(null);
        assertThat(sPageMapping.getPageAuthorizationRules()).isEqualTo(authorizationRules);

        final ArgumentCaptor<SInsertEvent> insertEvent = ArgumentCaptor.forClass(SInsertEvent.class);
        final ArgumentCaptor<InsertRecord> insertRecord = ArgumentCaptor.forClass(InsertRecord.class);
        verify(recorder).recordInsert(insertRecord.capture(), insertEvent.capture());

        assertThat(insertEvent.getValue().getObject()).isEqualTo(sPageMapping);
        assertThat(insertEvent.getValue().getType()).isEqualTo("PAGE_MAPPING_CREATED");
        assertThat(insertRecord.getValue().getEntity()).isEqualTo(sPageMapping);
    }

    @Test
    public void should_create_throw_creation_exception() throws Exception {
        //given
        doThrow(SRecorderException.class).when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));
        //when
        expectedException.expect(SObjectCreationException.class);
        pageMappingService.create("theKey", PAGE_ID, Collections.<String> emptyList());
    }

    @Test
    public void should_create_return_external_mapping() throws Exception {
        //given
        //when
        SPageMapping sPageMapping = pageMappingService.create("theKey", "http://www.mycompagny.com/aResource/page.html", "myAdapter",
                Collections.<String> emptyList());
        //then
        assertThat(sPageMapping).isNotNull();
        assertThat(sPageMapping.getKey()).isEqualTo("theKey");
        assertThat(sPageMapping.getUrl()).isEqualTo("http://www.mycompagny.com/aResource/page.html");
        assertThat(sPageMapping.getPageId()).isNull();
        assertThat(sPageMapping.getUrlAdapter()).isEqualTo("myAdapter");

        final ArgumentCaptor<SInsertEvent> insertEvent = ArgumentCaptor.forClass(SInsertEvent.class);
        final ArgumentCaptor<InsertRecord> insertRecord = ArgumentCaptor.forClass(InsertRecord.class);
        verify(recorder).recordInsert(insertRecord.capture(), insertEvent.capture());

        assertThat(insertEvent.getValue().getObject()).isEqualTo(sPageMapping);
        assertThat(insertEvent.getValue().getType()).isEqualTo("PAGE_MAPPING_CREATED");
        assertThat(insertRecord.getValue().getEntity()).isEqualTo(sPageMapping);
    }

    @Test
    public void should_get_return_the_object() throws Exception {
        SPageMappingImpl pageMapping = new SPageMappingImpl();
        pageMapping.setKey("myKey");
        doReturn(pageMapping).when(persistenceService).selectOne(any(SelectOneDescriptor.class));

        SPageMapping pageHavingMyKey = pageMappingService.get("myKey");

        assertThat(pageHavingMyKey).isEqualTo(pageMapping);
    }

    @Test
    public void should_get_throw_not_found() throws Exception {
        expectedException.expect(SObjectNotFoundException.class);
        pageMappingService.get("unknown");
    }

    @Test
    public void should_delete_call_the_recorder() throws Exception {
        //given
        SPageMappingImpl sPageMapping = new SPageMappingImpl();

        //when
        pageMappingService.delete(sPageMapping);

        //then
        final ArgumentCaptor<SDeleteEvent> deleteEvent = ArgumentCaptor.forClass(SDeleteEvent.class);
        final ArgumentCaptor<DeleteRecord> deleteRecord = ArgumentCaptor.forClass(DeleteRecord.class);
        verify(recorder).recordDelete(deleteRecord.capture(), deleteEvent.capture());
        assertThat(deleteEvent.getValue().getObject()).isEqualTo(sPageMapping);
        assertThat(deleteEvent.getValue().getType()).isEqualTo("PAGE_MAPPING_DELETED");
        assertThat(deleteRecord.getValue().getEntity()).isEqualTo(sPageMapping);
    }

    @Test
    public void should_delete_throw_exception() throws Exception {
        //given
        doThrow(SRecorderException.class).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        //when
        expectedException.expect(SDeletionException.class);
        pageMappingService.delete(new SPageMappingImpl());
    }

    @Test
    public void should_update_pageId_set_null_on_url() throws Exception {
        //given
        SPageMappingImpl pageMapping = new SPageMappingImpl();
        pageMapping.setKey("myKey");
        doReturn(pageMapping).when(persistenceService).selectOne(any(SelectOneDescriptor.class));

        //when
        pageMappingService.update(pageMapping, 124l);

        //then
        ArgumentCaptor<SUpdateEvent> updateEvent = ArgumentCaptor.forClass(SUpdateEvent.class);
        ArgumentCaptor<UpdateRecord> updateRecord = ArgumentCaptor.forClass(UpdateRecord.class);
        verify(recorder).recordUpdate(updateRecord.capture(), updateEvent.capture());
        assertThat(updateEvent.getValue().getObject()).isEqualTo(pageMapping);
        assertThat(updateEvent.getValue().getType()).isEqualTo("PAGE_MAPPING_UPDATED");
        assertThat(updateRecord.getValue().getEntity()).isEqualTo(pageMapping);
        assertThat(updateRecord.getValue().getFields()).contains(entry("pageId", 124l), entry("url", null), entry("urlAdapter", null),
                entry("lastUpdatedBy", 0L));

    }

    @Test
    public void should_update_throw_exception() throws Exception {
        //given
        SPageMappingImpl pageMapping = new SPageMappingImpl();
        pageMapping.setKey("myKey");
        doThrow(SRecorderException.class).when(recorder).recordUpdate(any(UpdateRecord.class), any(SUpdateEvent.class));
        //when
        expectedException.expect(SObjectModificationException.class);
        pageMappingService.update(pageMapping, 124l);
    }

    @Test
    public void should_update_url_set_null_on_pageId() throws Exception {
        //given
        SPageMappingImpl pageMapping = new SPageMappingImpl();
        pageMapping.setKey("myKey");

        //when
        pageMappingService.update(pageMapping, "myNewUrl", "urlAdapter");

        //then
        ArgumentCaptor<SUpdateEvent> updateEvent = ArgumentCaptor.forClass(SUpdateEvent.class);
        ArgumentCaptor<UpdateRecord> updateRecord = ArgumentCaptor.forClass(UpdateRecord.class);
        verify(recorder).recordUpdate(updateRecord.capture(), updateEvent.capture());
        assertThat(updateEvent.getValue().getObject()).isEqualTo(pageMapping);
        assertThat(updateEvent.getValue().getType()).isEqualTo("PAGE_MAPPING_UPDATED");
        assertThat(updateRecord.getValue().getEntity()).isEqualTo(pageMapping);
        assertThat(updateRecord.getValue().getFields()).contains(entry("pageId", null), entry("url", "myNewUrl"), entry("urlAdapter", "urlAdapter"),
                entry("lastUpdatedBy", 0L));
    }

    @Test
    public void should_resolveUrl_return_page_id() throws Exception {
        SPageMappingImpl pageMapping = new SPageMappingImpl();
        pageMapping.setPageId(56l);

        SPageURL sPageURL = pageMappingService.resolvePageURL(pageMapping, Collections.<String, Serializable> emptyMap(), true);

        assertThat(sPageURL.getPageId()).isEqualTo(56l);
        assertThat(sPageURL.getUrl()).isEqualTo(null);
    }

    @Test
    public void should_resolveUrl_return_url_with_no_adapter() throws Exception {
        SPageMappingImpl pageMapping = new SPageMappingImpl();
        pageMapping.setUrl("theUrl");

        SPageURL sPageURL = pageMappingService.resolvePageURL(pageMapping, Collections.<String, Serializable> emptyMap(), true);

        assertThat(sPageURL.getPageId()).isEqualTo(null);
        assertThat(sPageURL.getUrl()).isEqualTo("theUrl");
    }

    @Test
    public void should_resolveUrl_return_call_adapter_with_no_url() throws Exception {
        SPageMappingImpl pageMapping = new SPageMappingImpl();
        pageMapping.setUrl(null);
        pageMapping.setUrlAdapter("testAdapter");

        SPageURL sPageURL = pageMappingService.resolvePageURL(pageMapping, Collections.<String, Serializable> emptyMap(), true);

        assertThat(sPageURL.getPageId()).isEqualTo(null);
        assertThat(sPageURL.getUrl()).isEqualTo("null_adapted_0");
    }

    @Test
    public void should_resolveUrl_return_url_should_execute_adapter() throws Exception {
        SPageMappingImpl pageMapping = new SPageMappingImpl();
        pageMapping.setUrl("theUrl");
        pageMapping.setUrlAdapter("testAdapter");

        SPageURL sPageURL = pageMappingService.resolvePageURL(pageMapping, Collections.<String, Serializable> singletonMap("test", "test"), true);

        assertThat(sPageURL.getPageId()).isEqualTo(null);
        assertThat(sPageURL.getUrl()).isEqualTo("theUrl_adapted_1");/* 1 is the map size */
    }

    @Test(expected = SExecutionException.class)
    public void should_resolveUrl_with_unknown_adapter() throws Exception {
        SPageMappingImpl pageMapping = new SPageMappingImpl();
        pageMapping.setUrl("theUrl");
        pageMapping.setUrlAdapter("unknown");

        pageMappingService.resolvePageURL(pageMapping, Collections.<String, Serializable> singletonMap("test", "test"), true);
    }

    class ValidRule implements AuthorizationRule {

        private String validRuleKey;

        public ValidRule(String validRuleKey) {
            this.validRuleKey = validRuleKey;
        }

        @Override
        public boolean isAllowed(String key, Map<String, Serializable> context) {
            return true;
        }

        @Override
        public String getId() {
            return validRuleKey;
        }
    }

    class NeverValidRule implements AuthorizationRule {

        private String invalidRuleKey;

        public NeverValidRule(String invalidRuleKey) {
            this.invalidRuleKey = invalidRuleKey;
        }

        @Override
        public boolean isAllowed(String key, Map<String, Serializable> context) {
            return false;
        }

        @Override
        public String getId() {
            return invalidRuleKey;
        }
    }

    @Test
    public void resolvePageURL_shouldExecuteAuthorizationRule() throws Exception {
        SPageMappingImpl pageMapping = new SPageMappingImpl();
        final String validRuleKey = "validRule";
        final List<String> rules = new ArrayList<>(1);
        rules.add(validRuleKey);
        pageMapping.setPageAuthorizationRules(rules);

        final AuthorizationRule authorizationRule = spy(new ValidRule(validRuleKey));
        pageMappingService.setAuthorizationRules(Arrays.<AuthorizationRule> asList(authorizationRule));

        pageMappingService.resolvePageURL(pageMapping, Collections.<String, Serializable> emptyMap(), true);

        verify(authorizationRule).isAllowed(anyString(), anyMap());
    }
    
    @Test
    public void resolvePageURL_shouldNotExecuteAuthorizationRule() throws Exception {
        SPageMappingImpl pageMapping = new SPageMappingImpl();
        final String validRuleKey = "validRule";
        final List<String> rules = new ArrayList<>(1);
        rules.add(validRuleKey);
        pageMapping.setPageAuthorizationRules(rules);

        final AuthorizationRule authorizationRule = spy(new ValidRule(validRuleKey));
        pageMappingService.setAuthorizationRules(Arrays.<AuthorizationRule> asList(authorizationRule));

        pageMappingService.resolvePageURL(pageMapping, Collections.<String, Serializable> emptyMap(), false);

        verify(authorizationRule, never()).isAllowed(anyString(), anyMap());
    }

    @Test(expected = SAuthorizationException.class)
    public void resolvePageURL_shouldThrowExceptionIfAuthorizationRuleInvalid() throws Exception {
        SPageMappingImpl pageMapping = new SPageMappingImpl();
        final String invalidRuleKey = "invalidRuleKey";
        final List<String> rules = new ArrayList<>(1);
        rules.add(invalidRuleKey);
        pageMapping.setPageAuthorizationRules(rules);

        final AuthorizationRule authorizationRule = new NeverValidRule(invalidRuleKey);
        pageMappingService.setAuthorizationRules(Arrays.<AuthorizationRule> asList(authorizationRule));

        pageMappingService.resolvePageURL(pageMapping, Collections.<String, Serializable> emptyMap(), true);
    }

    @Test
    public void resolvePageURL_shouldAllowAsSoonAsOneRuleIsValid() throws Exception {
        SPageMappingImpl pageMapping = new SPageMappingImpl();
        final String invalidRuleKey = "invalidRule";
        final String validKey = "validRule";
        final String invalidRuleKey2 = "invalidRule2";
        final List<String> rules = new ArrayList<>(2);
        rules.add(invalidRuleKey);
        rules.add(validKey);
        pageMapping.setPageAuthorizationRules(rules);

        final NeverValidRule neverValidRule = spy(new NeverValidRule(invalidRuleKey));
        final ValidRule validRule = spy(new ValidRule(validKey));
        final NeverValidRule neverValidRule2 = spy(new NeverValidRule(invalidRuleKey2));
        pageMappingService.setAuthorizationRules(Arrays.<AuthorizationRule> asList(neverValidRule, validRule));

        pageMappingService.resolvePageURL(pageMapping, Collections.<String, Serializable> emptyMap(), true);

        verify(neverValidRule).isAllowed(anyString(), anyMap());
        verify(validRule).isAllowed(anyString(), anyMap());
        verifyZeroInteractions(neverValidRule2);
    }

    @Test
    public void resolvePageURL_shouldThrowExecutionExceptionIfAuthorizationRuleIsNotKnown() throws Exception {
        SPageMappingImpl pageMapping = new SPageMappingImpl();
        final String unknownRuleKey = "unknownRuleKey";
        final List<String> rules = new ArrayList<>(1);
        rules.add(unknownRuleKey);
        pageMapping.setPageAuthorizationRules(rules);

        expectedException.expect(SExecutionException.class);
        expectedException.expectMessage("Authorization rule " + unknownRuleKey);

        pageMappingService.resolvePageURL(pageMapping, Collections.<String, Serializable> emptyMap(), true);
    }
}
