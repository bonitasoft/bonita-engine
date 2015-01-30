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
package org.bonitasoft.engine.identity.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SCustomUserInfoDefinitionAlreadyExistsException;
import org.bonitasoft.engine.identity.SCustomUserInfoDefinitionCreationException;
import org.bonitasoft.engine.identity.SCustomUserInfoDefinitionNotFoundException;
import org.bonitasoft.engine.identity.SCustomUserInfoDefinitionReadException;
import org.bonitasoft.engine.identity.SCustomUserInfoValueNotFoundException;
import org.bonitasoft.engine.identity.SCustomUserInfoValueReadException;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IdentityServiceImplForCustomUserInfoTest {

    private static final long CUSTOM_USER_INFO_DEFINITION_ID = 10L;

    @Mock
    private ReadPersistenceService persistenceService;

    @Mock
    private Recorder recorder;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private QueriableLoggerService queriableLoggerService;

    @Mock
    private EventService eventService;

    @Mock
    private CredentialsEncrypter encrypter;

    @Mock
    private SCustomUserInfoValue userInfoValue;

    @Mock
    private SCustomUserInfoDefinition userInfoDef;

    @InjectMocks
    private IdentityServiceImpl identityServiceImpl;

    private static String DEFAULT_NAME = "skill";

    @Before
    public void setUp() {
        when(userInfoDef.getId()).thenReturn(CUSTOM_USER_INFO_DEFINITION_ID);
        when(userInfoDef.getName()).thenReturn(DEFAULT_NAME);
    }

    @Test
    public void createCustomUserInfoDefinition_should_call_recorder_insert() throws Exception {
        // given
        given(eventService.hasHandlers(IdentityService.CUSTOM_USER_INFO_DEFINITION, EventActionType.CREATED)).willReturn(true);
        final ArgumentCaptor<SInsertEvent> eventCaptor = ArgumentCaptor.forClass(SInsertEvent.class);

        // when
        identityServiceImpl.createCustomUserInfoDefinition(userInfoDef);

        // then
        verify(recorder, times(1)).recordInsert(eq(new InsertRecord(userInfoDef)), eventCaptor.capture());
        assertThat(eventCaptor.getValue().getObject()).isEqualTo(userInfoDef);
    }

    @Test
    public void createCustomUserInfoDefinition_should_throw_SCustomUserInfoDefinitionCreationException_when_recorder_throws_SRecorderException()
            throws Exception {
        // given
        final SRecorderException recorderException = new SRecorderException("");
        doThrow(recorderException).when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));

        try {
            // when
            identityServiceImpl.createCustomUserInfoDefinition(userInfoDef);
            fail("Creation exception expected");
        } catch (final SCustomUserInfoDefinitionCreationException e) {
            // then
            assertThat(e.getDefinitionName()).isEqualTo(DEFAULT_NAME);
            assertThat(e.getCause()).isEqualTo(recorderException);
        }

    }

    @Test
    public void createCustomUserInfoDefinition_should_throw_SCustomUserInfoDefinitionAlreadyExistsException_when_persistence_service_returns_result_for_check()
            throws Exception {
        // given
        final SCustomUserInfoDefinition duplicateDef = mock(SCustomUserInfoDefinition.class);
        given(duplicateDef.getName()).willReturn(DEFAULT_NAME);
        given(persistenceService.selectOne(SelectDescriptorBuilder.getCustomUserInfoDefinitionByName(DEFAULT_NAME))).willReturn(userInfoDef);

        try {
            // when
            identityServiceImpl.createCustomUserInfoDefinition(duplicateDef);
            fail("Already exists exception found");
        } catch (final SCustomUserInfoDefinitionAlreadyExistsException e) {
            // then
            assertThat(e.getDefinitionName()).isEqualTo(DEFAULT_NAME);
            verify(recorder, never()).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));
        }

    }

    @Test
    public void getCustomUserInfoDefinitionByName_should_return_result_of_persistence_service() throws Exception {
        // given
        given(persistenceService.selectOne(SelectDescriptorBuilder.getCustomUserInfoDefinitionByName(DEFAULT_NAME))).willReturn(userInfoDef);

        // when
        final SCustomUserInfoDefinition retrievedDef = identityServiceImpl.getCustomUserInfoDefinitionByName(DEFAULT_NAME);

        // then
        assertThat(retrievedDef).isEqualTo(userInfoDef);
    }

    @Test
    public void getCustomUserInfoDefinitionByName_should_throw_SCustomUserInfoDefinitionNotFoundException_when_persistence_service_returns_null()
            throws Exception {
        // given
        given(persistenceService.selectOne(SelectDescriptorBuilder.getCustomUserInfoDefinitionByName(DEFAULT_NAME))).willReturn(null);

        try {
            // when
            identityServiceImpl.getCustomUserInfoDefinitionByName(DEFAULT_NAME);
            fail("Not found exception expected");
        } catch (final SCustomUserInfoDefinitionNotFoundException e) {
            // then
            assertThat(e.getDefinitionName()).isEqualTo(DEFAULT_NAME);
        }
    }

    @Test
    public void getCustomUserInfoDefinitionByName_should_throw_SCustomUserInfoDefinitionReadException_when_persistence_service_throws_SBonitaReadException()
            throws Exception {
        // given
        final SBonitaReadException persistenceException = new SBonitaReadException("");
        given(persistenceService.selectOne(SelectDescriptorBuilder.getCustomUserInfoDefinitionByName(DEFAULT_NAME))).willThrow(persistenceException);

        try {
            // when
            identityServiceImpl.getCustomUserInfoDefinitionByName(DEFAULT_NAME);
            fail("Read exception expected");
        } catch (final SCustomUserInfoDefinitionReadException e) {
            // then
            assertThat(e.getDefinitionName()).isEqualTo(DEFAULT_NAME);
            assertThat(e.getCause()).isEqualTo(persistenceException);
        }
    }

    @Test
    public void hasCustomUserInfoDefinition_should_return_true_if_persistence_service_finds_an_element() throws Exception {
        // given
        given(persistenceService.selectOne(SelectDescriptorBuilder.getCustomUserInfoDefinitionByName(DEFAULT_NAME))).willReturn(userInfoDef);

        // when
        final boolean hasDef = identityServiceImpl.hasCustomUserInfoDefinition(DEFAULT_NAME);

        // then
        assertThat(hasDef).isTrue();
    }

    @Test
    public void hasCustomUserInfoDefinition_should_throw_SCustomUserInfoDefinitionReadException_when_persistence_service_throws_SBonitaReadException()
            throws Exception {
        // given
        final SBonitaReadException persistenceException = new SBonitaReadException("");
        given(persistenceService.selectOne(SelectDescriptorBuilder.getCustomUserInfoDefinitionByName(DEFAULT_NAME))).willThrow(persistenceException);

        try {
            // when
            identityServiceImpl.hasCustomUserInfoDefinition(DEFAULT_NAME);
            fail("Read exception expected");
        } catch (final SCustomUserInfoDefinitionReadException e) {
            // then
            assertThat(e.getDefinitionName()).isEqualTo(DEFAULT_NAME);
            assertThat(e.getCause()).isEqualTo(persistenceException);
        }
    }

    @Test
    public void searchCustomUserInfoValues_should_return_values_from_persistence_service() throws Exception {
        // given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final List<SCustomUserInfoValue> persistenceResult = Arrays.asList(userInfoValue);
        given(persistenceService.searchEntity(SCustomUserInfoValue.class, queryOptions, null)).willReturn(persistenceResult);

        // when
        final List<SCustomUserInfoValue> userInfoValues = identityServiceImpl.searchCustomUserInfoValue(queryOptions);

        // then
        assertThat(userInfoValues).isEqualTo(persistenceResult);
    }

    @Test(
            // then
            expected = SBonitaReadException.class)
    public void searchCustomUserInfoValues_should_throw_exception_SBonitaReadException_when_persistence_service_throws_SBonitaReadException()
            throws Exception {
        // given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        given(persistenceService.searchEntity(SCustomUserInfoValue.class, queryOptions, null)).willThrow(new SBonitaReadException(""));

        // when
        identityServiceImpl.searchCustomUserInfoValue(queryOptions);
    }

    @Test
    public void createCustomUserInfoValue_should_call_recorder_insert() throws Exception {
        // given
        given(eventService.hasHandlers(IdentityService.CUSTOM_USER_INFO_VALUE, EventActionType.CREATED)).willReturn(true);
        final ArgumentCaptor<InsertRecord> recordCaptor = ArgumentCaptor.forClass(InsertRecord.class);
        final ArgumentCaptor<SInsertEvent> eventCaptor = ArgumentCaptor.forClass(SInsertEvent.class);

        // when
        identityServiceImpl.createCustomUserInfoValue(userInfoValue);

        // then
        verify(recorder, times(1)).recordInsert(recordCaptor.capture(), eventCaptor.capture());
        assertThat(recordCaptor.getValue().getEntity()).isEqualTo(userInfoValue);
        assertThat(eventCaptor.getValue().getObject()).isEqualTo(userInfoValue);
    }

    @Test(expected = SIdentityException.class)
    public void createCustomUserInfoValue_should_throw_SIdentityException_when_recorder_throws_SRecorderException() throws Exception {
        // given
        doThrow(SRecorderException.class).when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));

        // when
        identityServiceImpl.createCustomUserInfoValue(userInfoValue);
    }

    @Test
    public void deleteCustomUserInfoValue_should_call_recorder_delete() throws Exception {
        // given
        given(eventService.hasHandlers(IdentityService.CUSTOM_USER_INFO_VALUE, EventActionType.DELETED)).willReturn(true);
        final ArgumentCaptor<DeleteRecord> recordCaptor = ArgumentCaptor.forClass(DeleteRecord.class);
        final ArgumentCaptor<SDeleteEvent> eventCaptor = ArgumentCaptor.forClass(SDeleteEvent.class);

        // when
        identityServiceImpl.deleteCustomUserInfoValue(userInfoValue);

        // then
        verify(recorder, times(1)).recordDelete(recordCaptor.capture(), eventCaptor.capture());
        assertThat(recordCaptor.getValue().getEntity()).isEqualTo(userInfoValue);
        assertThat(eventCaptor.getValue().getObject()).isEqualTo(userInfoValue);
    }

    @Test(expected = SIdentityException.class)
    public void deleteCustomUserInfoValue_should_throw_SIdentityException_when_recorder_throws_SRecorderException() throws Exception {
        // given
        doThrow(SRecorderException.class).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));

        // when
        identityServiceImpl.deleteCustomUserInfoValue(userInfoValue);
    }

    @Test
    public void getCustomUserInfoValue_shoul_return_value_returned_by_persistence_service() throws Exception {
        final long id = 10;
        // given
        final SelectByIdDescriptor<SCustomUserInfoValue> selector = SelectDescriptorBuilder.getElementById(SCustomUserInfoValue.class, "SCustomUserInfoValue",
                id);
        given(persistenceService.selectById(selector)).willReturn(userInfoValue);

        // when
        final SCustomUserInfoValue retriveidUserInfo = identityServiceImpl.getCustomUserInfoValue(id);

        // then
        assertThat(retriveidUserInfo).isEqualTo(userInfoValue);
    }

    @Test(expected = SCustomUserInfoValueNotFoundException.class)
    public void getCustomUserInfoValue_shoul_throw_SCustomUserInfoValueNotFoundException_when_persistence_service_returns_null() throws Exception {
        final long id = 10;
        // given
        final SelectByIdDescriptor<SCustomUserInfoValue> selector = SelectDescriptorBuilder.getElementById(SCustomUserInfoValue.class, "SCustomUserInfoValue",
                id);
        given(persistenceService.selectById(selector)).willReturn(null);

        // when
        identityServiceImpl.getCustomUserInfoValue(id);
    }

    @Test(expected = SCustomUserInfoValueReadException.class)
    public void getCustomUserInfoValue_shoul_throw_SCustomUserInfoValueReadException_when_persistence_service_throws_SBonitaReadException() throws Exception {
        final long id = 10;
        // given
        final SelectByIdDescriptor<SCustomUserInfoValue> selector = SelectDescriptorBuilder.getElementById(SCustomUserInfoValue.class, "SCustomUserInfoValue",
                id);
        given(persistenceService.selectById(selector)).willThrow(new SBonitaReadException(""));

        // when
        identityServiceImpl.getCustomUserInfoValue(id);
    }

    @Test
    public void updateCustomUserInfoValue_should_call_recorder_update() throws Exception {
        // given
        given(eventService.hasHandlers(IdentityService.CUSTOM_USER_INFO_VALUE, EventActionType.UPDATED)).willReturn(true);
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        final ArgumentCaptor<UpdateRecord> recordCaptor = ArgumentCaptor.forClass(UpdateRecord.class);
        final ArgumentCaptor<SUpdateEvent> eventCaptor = ArgumentCaptor.forClass(SUpdateEvent.class);

        // when
        identityServiceImpl.updateCustomUserInfoValue(userInfoValue, descriptor);

        // then
        verify(recorder, times(1)).recordUpdate(recordCaptor.capture(), eventCaptor.capture());
        assertThat(recordCaptor.getValue().getEntity()).isEqualTo(userInfoValue);
        assertThat(eventCaptor.getValue().getObject()).isEqualTo(userInfoValue);
    }

    @Test
    public void getUserIdsWithCustomUserInfo_should_use_query_getUserIdsWithCustomUserInfo_when_no_partial_match() throws Exception {
        //given
        final Map<String, Object> parameters = new HashMap<String, Object>(2);
        parameters.put("userInfoName", DEFAULT_NAME);
        parameters.put("userInfoValue", "Java");
        final SelectListDescriptor<Long> descriptor = new SelectListDescriptor<Long>("getUserIdsWithCustomUserInfo", parameters, SUser.class, Long.class,
                new QueryOptions(0, 10));
        given(persistenceService.selectList(descriptor)).willReturn(Arrays.asList(10L, 20L));

        //when
        final List<Long> userIds = identityServiceImpl.getUserIdsWithCustomUserInfo(DEFAULT_NAME, "Java", false, 0, 10);

        //then
        assertThat(userIds).containsExactly(10L, 20L);
    }

    @Test
    public void getUserIdsWithCustomUserInfo_should_use_query_getUserIdsWithCustomUserInfoContains_when_partial_match() throws Exception {
        //given
        final Map<String, Object> parameters = new HashMap<String, Object>(2);
        parameters.put("userInfoName", DEFAULT_NAME);
        parameters.put("userInfoValue", "Java");
        final SelectListDescriptor<Long> descriptor = new SelectListDescriptor<Long>("getUserIdsWithCustomUserInfoContains", parameters, SUser.class,
                Long.class,
                new QueryOptions(0, 10));
        given(persistenceService.selectList(descriptor)).willReturn(Arrays.asList(10L, 20L));

        //when
        final List<Long> userIds = identityServiceImpl.getUserIdsWithCustomUserInfo(DEFAULT_NAME, "Java", true, 0, 10);

        //then
        assertThat(userIds).containsExactly(10L, 20L);
    }

    @Test(expected = SIdentityException.class)
    //then
    public void getUserIdsWithCustomUserInfo_should_throw_SIdentityException_when_persistence_service_throws_exception() throws Exception {
        //given
        given(persistenceService.selectList(Matchers.<SelectListDescriptor<Long>> any())).willThrow(new SBonitaReadException(""));

        //when
        identityServiceImpl.getUserIdsWithCustomUserInfo(DEFAULT_NAME, "Java", false, 0, 10);
    }

}
