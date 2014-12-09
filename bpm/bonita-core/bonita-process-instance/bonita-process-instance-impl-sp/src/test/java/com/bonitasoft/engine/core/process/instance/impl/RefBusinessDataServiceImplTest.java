package com.bonitasoft.engine.core.process.instance.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.core.process.instance.api.exceptions.SRefBusinessDataInstanceCreationException;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SRefBusinessDataInstanceModificationException;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SRefBusinessDataInstanceNotFoundException;
import com.bonitasoft.engine.core.process.instance.model.SRefBusinessDataInstance;
import com.bonitasoft.engine.core.process.instance.model.impl.SProcessSimpleRefBusinessDataInstanceImpl;
import com.bonitasoft.engine.core.process.instance.model.impl.SSimpleRefBusinessDataInstanceImpl;
import com.bonitasoft.engine.core.process.instance.recorder.SelectDescriptorBuilderExt;

@RunWith(MockitoJUnitRunner.class)
public class RefBusinessDataServiceImplTest {

    @Mock
    private ReadPersistenceService persistence;

    @Mock
    private EventService eventService;

    @Mock
    private Recorder recorder;

    @Mock
    QueriableLoggerService loggerService;

    @InjectMocks
    private RefBusinessDataServiceImpl service;

    private SSimpleRefBusinessDataInstanceImpl buildSRefBusinessDataInstance() {
        final SSimpleRefBusinessDataInstanceImpl instance = new SProcessSimpleRefBusinessDataInstanceImpl();
        instance.setName("myLeaveRequest");
        instance.setDataId(45l);
        instance.setDataClassName("org.bonitasoft.LeaveRequest");
        return instance;
    }

    private SelectOneDescriptor<SRefBusinessDataInstance> buildGetDescriptor(final String name, final long dataId) {
        return SelectDescriptorBuilderExt.getSRefBusinessDataInstance(name, dataId);
    }

    @Test
    public void getRefBusinessDataInstanceReturnsTheRightObject() throws Exception {
        final String name = "myLeaveRequest";
        final long dataId = 45;
        final SRefBusinessDataInstance expectedDataInstance = buildSRefBusinessDataInstance();
        when(persistence.selectOne(buildGetDescriptor(name, dataId))).thenReturn(expectedDataInstance);

        final SRefBusinessDataInstance actualDataInstance = service.getRefBusinessDataInstance(name, dataId);

        assertThat(actualDataInstance).isEqualTo(expectedDataInstance);
    }

    @Test(expected = SRefBusinessDataInstanceNotFoundException.class)
    public void getRefBusinessDataInstanceThrowsAnExceptionWhenObjectDoesNotExist() throws Exception {
        final String name = "myLeaveRequest";
        final long dataId = 45;
        when(persistence.selectOne(buildGetDescriptor(name, dataId))).thenReturn(null);

        service.getRefBusinessDataInstance(name, dataId);
    }

    @Test(expected = SBonitaReadException.class)
    public void getRefBusinessDataInstanceThrowsAnExceptionWhenPeristenceServiceIsDown() throws Exception {
        final String name = "myLeaveRequest";
        final long dataId = 45;
        when(persistence.selectOne(buildGetDescriptor(name, dataId))).thenThrow(new SBonitaReadException("down"));

        service.getRefBusinessDataInstance(name, dataId);
    }

    @Test
    public void updateRefBusinessData() throws Exception {
        final SSimpleRefBusinessDataInstanceImpl refBusinessDataInstance = buildSRefBusinessDataInstance();
        final long dataId = 564654654654654l;
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("dataId", dataId);
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(refBusinessDataInstance, fields);

        service.updateRefBusinessDataInstance(refBusinessDataInstance, dataId);

        verify(recorder).recordUpdate(updateRecord, null);
    }

    @Test(expected = SRefBusinessDataInstanceModificationException.class)
    public void updateRefBusinessDataThrowException() throws Exception {
        final SSimpleRefBusinessDataInstanceImpl refBusinessDataInstance = buildSRefBusinessDataInstance();
        final long dataId = 564654654654654l;
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("dataId", dataId);
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(refBusinessDataInstance, fields);
        doThrow(new SRecorderException("ouch!")).when(recorder).recordUpdate(updateRecord, null);

        service.updateRefBusinessDataInstance(refBusinessDataInstance, dataId);
    }

    @Test
    public void addRefBusinessData() throws Exception {
        final SRefBusinessDataInstance refBusinessDataInstance = buildSRefBusinessDataInstance();

        service.addRefBusinessDataInstance(refBusinessDataInstance);

        verify(recorder).recordInsert(new InsertRecord(refBusinessDataInstance), null);
    }

    @Test(expected = SRefBusinessDataInstanceCreationException.class)
    public void addRefBusinessDataThrowException() throws Exception {
        final SRefBusinessDataInstance refBusinessDataInstance = buildSRefBusinessDataInstance();

        doThrow(new SRecorderException("ouch!")).when(recorder).recordInsert(new InsertRecord(refBusinessDataInstance), null);

        service.addRefBusinessDataInstance(refBusinessDataInstance);
    }

    @Test
    public void getRefBusinessDataInstancesShouldCallThePersistenceService() throws Exception {
        final long processIntanceId = 789798798L;
        final int startIndex = 0;
        final int maxResults = 100;

        service.getRefBusinessDataInstances(processIntanceId, startIndex, maxResults);

        verify(persistence).selectList(SelectDescriptorBuilderExt.getSRefBusinessDataInstances(processIntanceId,
                startIndex, maxResults));
    }

}
