package com.bonitasoft.engine.core.process.instance.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.bonitasoft.engine.core.process.instance.api.exceptions.SRefBusinessDataInstanceNotFoundException;
import com.bonitasoft.engine.core.process.instance.model.SRefBusinessDataInstance;
import com.bonitasoft.engine.core.process.instance.model.impl.SRefBusinessDataInstanceImpl;
import com.bonitasoft.engine.core.process.instance.recorder.SelectDescriptorBuilderExt;

public class RefBusinessDataServiceImplTest {

    @Mock
    private ReadPersistenceService persistence;

    @InjectMocks
    private RefBusinessDataServiceImpl service;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    private SRefBusinessDataInstance buildSRefBusinessDataInstance(final String name, final long dataId) {
        final SRefBusinessDataInstanceImpl instance = new SRefBusinessDataInstanceImpl();
        instance.setName("myLeaveRequest");
        instance.setDataId(45);
        instance.setDataClassName("org.bonitasoft.LeaveRequest");
        return instance;
    }

    @Test
    public void getRefBusinessDataInstanceReturnsTheRightObject() throws Exception {
        final String name = "myLeaveRequest";
        final long dataId = 45;
        final SRefBusinessDataInstance expectedDataInstance = buildSRefBusinessDataInstance(name, dataId);
        when(persistence.selectOne(buildGetDescriptor(name, dataId))).thenReturn(expectedDataInstance);

        final SRefBusinessDataInstance actualDataInstance = service.getRefBusinessDataInstance(name, dataId);

        assertThat(actualDataInstance).isEqualTo(expectedDataInstance);
    }

    private SelectOneDescriptor<SRefBusinessDataInstance> buildGetDescriptor(final String name, final long dataId) {
        return SelectDescriptorBuilderExt.getSRefBusinessDataInstance(name, dataId);
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

}
