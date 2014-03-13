package org.bonitasoft.engine.identity.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SCustomUserInfoValueNotFoundException;
import org.bonitasoft.engine.identity.SCustomUserInfoValueReadException;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;
import org.bonitasoft.engine.identity.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class IdentityServiceImplForCustomUserInfoTest {
    
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
    
    @InjectMocks
    private IdentityServiceImpl identityServiceImpl;
    
    @Test
    public void searchCustomUserInfoValues_should_return_values_from_persistence_service() throws Exception {
        //given
        QueryOptions queryOptions = new QueryOptions(0, 10);
        List<SCustomUserInfoValue> persistenceResult = Arrays.asList(userInfoValue);
        given(persistenceService.searchEntity(SCustomUserInfoValue.class, queryOptions, null)).willReturn(persistenceResult);
        
        //when
        List<SCustomUserInfoValue> userInfoValues = identityServiceImpl.searchCustomUserInfoValue(queryOptions);

        //then
        assertThat(userInfoValues).isEqualTo(persistenceResult);
    }

    @Test(
            //then
            expected = SBonitaSearchException.class
            )
    public void searchCustomUserInfoValues_should_throw_exception_SBonitaSearchException_when_persistence_service_throws_SBonitaReadException() throws Exception {
        //given
        QueryOptions queryOptions = new QueryOptions(0, 10);
        given(persistenceService.searchEntity(SCustomUserInfoValue.class, queryOptions, null)).willThrow(new SBonitaReadException(""));
        
        //when
        identityServiceImpl.searchCustomUserInfoValue(queryOptions);
    }
    
    @Test
    public void createCustomUserInfoValue_should_call_recorder_insert() throws Exception {
        //given
        given(eventService.hasHandlers(IdentityService.CUSTOM_USER_INFO_VALUE, EventActionType.CREATED)).willReturn(true);
        ArgumentCaptor<InsertRecord> recordCaptor = ArgumentCaptor.forClass(InsertRecord.class);
        ArgumentCaptor<SInsertEvent> eventCaptor = ArgumentCaptor.forClass(SInsertEvent.class);
        
        //when
        identityServiceImpl.createCustomUserInfoValue(userInfoValue);

        //then
        verify(recorder, times(1)).recordInsert(recordCaptor.capture(), eventCaptor.capture());
        assertThat(recordCaptor.getValue().getEntity()).isEqualTo(userInfoValue);
        assertThat(eventCaptor.getValue().getObject()).isEqualTo(userInfoValue);
    }

    @Test (expected = SIdentityException.class)
    public void createCustomUserInfoValue_should_throw_SIdentityException_when_recorder_throws_SRecorderException() throws Exception {
        //given
        doThrow(SRecorderException.class).when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));
        
        //when
        identityServiceImpl.createCustomUserInfoValue(userInfoValue);
    }

    @Test
    public void deleteCustomUserInfoValue_should_call_recorder_delete() throws Exception {
        //given
        given(eventService.hasHandlers(IdentityService.CUSTOM_USER_INFO_VALUE, EventActionType.DELETED)).willReturn(true);
        ArgumentCaptor<DeleteRecord> recordCaptor = ArgumentCaptor.forClass(DeleteRecord.class);
        ArgumentCaptor<SDeleteEvent> eventCaptor = ArgumentCaptor.forClass(SDeleteEvent.class);
        
        //when
        identityServiceImpl.deleteCustomUserInfoValue(userInfoValue);
        
        //then
        verify(recorder, times(1)).recordDelete(recordCaptor.capture(), eventCaptor.capture());
        assertThat(recordCaptor.getValue().getEntity()).isEqualTo(userInfoValue);
        assertThat(eventCaptor.getValue().getObject()).isEqualTo(userInfoValue);
    }
    
    @Test (expected = SIdentityException.class)
    public void deleteCustomUserInfoValue_should_throw_SIdentityException_when_recorder_throws_SRecorderException() throws Exception {
        //given
        doThrow(SRecorderException.class).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        
        //when
        identityServiceImpl.deleteCustomUserInfoValue(userInfoValue);
    }
    
    @Test
    public void getCustomUserInfoValue_shoul_return_value_returned_by_persistence_service() throws Exception {
        long id = 10;
        //given
        SelectByIdDescriptor<SCustomUserInfoValue> selector = SelectDescriptorBuilder.getElementById(SCustomUserInfoValue.class, "SCustomUserInfoValue", id );
        given(persistenceService.selectById(selector)).willReturn(userInfoValue);
        
        //when
        SCustomUserInfoValue retriveidUserInfo = identityServiceImpl.getCustomUserInfoValue(id);

        //then
        assertThat(retriveidUserInfo).isEqualTo(userInfoValue);
    }

    @Test (expected = SCustomUserInfoValueNotFoundException.class)
    public void getCustomUserInfoValue_shoul_throw_SCustomUserInfoValueNotFoundException_when_persistence_service_returns_null() throws Exception {
        long id = 10;
        //given
        SelectByIdDescriptor<SCustomUserInfoValue> selector = SelectDescriptorBuilder.getElementById(SCustomUserInfoValue.class, "SCustomUserInfoValue", id );
        given(persistenceService.selectById(selector)).willReturn(null);
        
        //when
        identityServiceImpl.getCustomUserInfoValue(id);
    }

    @Test (expected = SCustomUserInfoValueReadException.class)
    public void getCustomUserInfoValue_shoul_throw_SCustomUserInfoValueReadException_when_persistence_service_throws_SBonitaReadException() throws Exception {
        long id = 10;
        //given
        SelectByIdDescriptor<SCustomUserInfoValue> selector = SelectDescriptorBuilder.getElementById(SCustomUserInfoValue.class, "SCustomUserInfoValue", id );
        given(persistenceService.selectById(selector)).willThrow(new SBonitaReadException(""));
        
        //when
        identityServiceImpl.getCustomUserInfoValue(id);
    }
    
    @Test
    public void updateCustomUserInfoValue_should_call_recorder_update() throws Exception {
        //given
        given(eventService.hasHandlers(IdentityService.CUSTOM_USER_INFO_VALUE, EventActionType.UPDATED)).willReturn(true);
        EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        ArgumentCaptor<UpdateRecord> recordCaptor = ArgumentCaptor.forClass(UpdateRecord.class);
        ArgumentCaptor<SUpdateEvent> eventCaptor = ArgumentCaptor.forClass(SUpdateEvent.class);
        
        //when
        identityServiceImpl.updateCustomUserInfoValue(userInfoValue, descriptor);

        //then
        verify(recorder, times(1)).recordUpdate(recordCaptor.capture(), eventCaptor.capture());
        assertThat(recordCaptor.getValue().getEntity()).isEqualTo(userInfoValue);
        assertThat(eventCaptor.getValue().getObject()).isEqualTo(userInfoValue);
    }

}
