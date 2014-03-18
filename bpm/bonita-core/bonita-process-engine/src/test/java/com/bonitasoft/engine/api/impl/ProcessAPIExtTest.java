package com.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.Serializable;

import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.junit.Test;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.business.data.SBusinessDataNotFoundException;
import com.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SRefBusinessDataInstanceNotFoundException;
import com.bonitasoft.engine.core.process.instance.model.SRefBusinessDataInstance;
import com.bonitasoft.engine.service.TenantServiceAccessor;

public class ProcessAPIExtTest {

    @Test
    public void getTheBusinessDataInstance() throws Exception {
        final long processInstanceId = 45;
        final ProcessAPIExt processAPI = new ProcessAPIExt();
        final ProcessAPIExt spiAPI = spy(processAPI);
        final TenantServiceAccessor tenantAccessor = mock(TenantServiceAccessor.class);
        final RefBusinessDataService refBusinessDataService = mock(RefBusinessDataService.class);
        final BusinessDataRepository businessDataRespository = mock(BusinessDataRepository.class);
        final SRefBusinessDataInstance refBusinessDataInstance = mock(SRefBusinessDataInstance.class);
        final MyBusinessData myLeaveRequest = new MyBusinessData();
        doReturn(tenantAccessor).when(spiAPI).getTenantAccessor();
        when(tenantAccessor.getRefBusinessDataService()).thenReturn(refBusinessDataService);
        when(refBusinessDataService.getRefBusinessDataInstance("myLeaveRequest", processInstanceId)).thenReturn(refBusinessDataInstance);
        when(tenantAccessor.getBusinessDataRepository()).thenReturn(businessDataRespository);
        when(refBusinessDataInstance.getDataClassName()).thenReturn(MyBusinessData.class.getName());
        when(refBusinessDataInstance.getDataId()).thenReturn(78l);
        when(businessDataRespository.findById(MyBusinessData.class, 78l)).thenReturn(myLeaveRequest);

        final Serializable actual = spiAPI.getBusinessDataInstance("myLeaveRequest", processInstanceId);

        assertThat(actual).isEqualTo(myLeaveRequest);
    }

    @Test(expected = DataNotFoundException.class)
    public void getBusinessDataInstanceThrowsException() throws Exception {
        final long processInstanceId = 45;
        final ProcessAPIExt processAPI = new ProcessAPIExt();
        final ProcessAPIExt spiAPI = spy(processAPI);
        final TenantServiceAccessor tenantAccessor = mock(TenantServiceAccessor.class);
        final RefBusinessDataService refBusinessDataService = mock(RefBusinessDataService.class);
        doReturn(tenantAccessor).when(spiAPI).getTenantAccessor();
        when(tenantAccessor.getRefBusinessDataService()).thenReturn(refBusinessDataService);
        when(refBusinessDataService.getRefBusinessDataInstance("myLeaveRequest", processInstanceId)).thenThrow(
                new SRefBusinessDataInstanceNotFoundException(processInstanceId, "myLeaveRequest"));

        spiAPI.getBusinessDataInstance("myLeaveRequest", processInstanceId);
    }

    @Test(expected = RetrieveException.class)
    public void getBusinessDataInstanceThrowsRetrieveException() throws Exception {
        final long processInstanceId = 45;
        final ProcessAPIExt processAPI = new ProcessAPIExt();
        final ProcessAPIExt spiAPI = spy(processAPI);
        final TenantServiceAccessor tenantAccessor = mock(TenantServiceAccessor.class);
        final RefBusinessDataService refBusinessDataService = mock(RefBusinessDataService.class);
        doReturn(tenantAccessor).when(spiAPI).getTenantAccessor();
        when(tenantAccessor.getRefBusinessDataService()).thenReturn(refBusinessDataService);
        when(refBusinessDataService.getRefBusinessDataInstance("myLeaveRequest", processInstanceId)).thenThrow(new SBonitaReadException("ouch"));

        spiAPI.getBusinessDataInstance("myLeaveRequest", processInstanceId);
    }

    @Test(expected = DataNotFoundException.class)
    public void getTheBusinessDataInstanceThrowsDataNotFoundException() throws Exception {
        final long processInstanceId = 45;
        final ProcessAPIExt processAPI = new ProcessAPIExt();
        final ProcessAPIExt spiAPI = spy(processAPI);
        final TenantServiceAccessor tenantAccessor = mock(TenantServiceAccessor.class);
        final RefBusinessDataService refBusinessDataService = mock(RefBusinessDataService.class);
        final BusinessDataRepository businessDataRespository = mock(BusinessDataRepository.class);
        final SRefBusinessDataInstance refBusinessDataInstance = mock(SRefBusinessDataInstance.class);
        doReturn(tenantAccessor).when(spiAPI).getTenantAccessor();
        when(tenantAccessor.getRefBusinessDataService()).thenReturn(refBusinessDataService);
        when(refBusinessDataService.getRefBusinessDataInstance("myLeaveRequest", processInstanceId)).thenReturn(refBusinessDataInstance);
        when(tenantAccessor.getBusinessDataRepository()).thenReturn(businessDataRespository);
        when(refBusinessDataInstance.getDataClassName()).thenReturn(MyBusinessData.class.getName());
        when(refBusinessDataInstance.getDataId()).thenReturn(78l);
        when(businessDataRespository.findById(MyBusinessData.class, 78l)).thenThrow(new SBusinessDataNotFoundException("ouch!"));

        spiAPI.getBusinessDataInstance("myLeaveRequest", processInstanceId);
    }

    @Test(expected = RetrieveException.class)
    public void getTheBusinessDataInstanceThrowsRetrieveException() throws Exception {
        final long processInstanceId = 45;
        final ProcessAPIExt processAPI = new ProcessAPIExt();
        final ProcessAPIExt spiAPI = spy(processAPI);
        final TenantServiceAccessor tenantAccessor = mock(TenantServiceAccessor.class);
        final RefBusinessDataService refBusinessDataService = mock(RefBusinessDataService.class);
        final BusinessDataRepository businessDataRespository = mock(BusinessDataRepository.class);
        final SRefBusinessDataInstance refBusinessDataInstance = mock(SRefBusinessDataInstance.class);
        doReturn(tenantAccessor).when(spiAPI).getTenantAccessor();
        when(tenantAccessor.getRefBusinessDataService()).thenReturn(refBusinessDataService);
        when(refBusinessDataService.getRefBusinessDataInstance("myLeaveRequest", processInstanceId)).thenReturn(refBusinessDataInstance);
        when(tenantAccessor.getBusinessDataRepository()).thenReturn(businessDataRespository);
        when(refBusinessDataInstance.getDataClassName()).thenReturn("org.bonitasoft.engine.LeaveRequest");
        when(refBusinessDataInstance.getDataId()).thenReturn(78l);

        spiAPI.getBusinessDataInstance("myLeaveRequest", processInstanceId);
    }

    @Test
    public void getTheBusinessDataInstanceReturnsNull() throws Exception {
        final long processInstanceId = 45;
        final ProcessAPIExt processAPI = new ProcessAPIExt();
        final ProcessAPIExt spiAPI = spy(processAPI);
        final TenantServiceAccessor tenantAccessor = mock(TenantServiceAccessor.class);
        final RefBusinessDataService refBusinessDataService = mock(RefBusinessDataService.class);
        final BusinessDataRepository businessDataRespository = mock(BusinessDataRepository.class);
        final SRefBusinessDataInstance refBusinessDataInstance = mock(SRefBusinessDataInstance.class);
        doReturn(tenantAccessor).when(spiAPI).getTenantAccessor();
        when(tenantAccessor.getRefBusinessDataService()).thenReturn(refBusinessDataService);
        when(refBusinessDataService.getRefBusinessDataInstance("myLeaveRequest", processInstanceId)).thenReturn(refBusinessDataInstance);
        when(tenantAccessor.getBusinessDataRepository()).thenReturn(businessDataRespository);
        when(refBusinessDataInstance.getDataClassName()).thenReturn("org.bonitasoft.engine.LeaveRequest");
        when(refBusinessDataInstance.getDataId()).thenReturn(null);

        final Serializable businessDataInstance = spiAPI.getBusinessDataInstance("myLeaveRequest", processInstanceId);
        assertThat(businessDataInstance).isNull();
    }

    class MyBusinessData implements Entity {

        private final int age = 35;

        @Override
        public Long getPersistenceId() {
            return null;
        }

        @Override
        public Long getPersistenceVersion() {
            return null;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + age;
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MyBusinessData other = (MyBusinessData) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (age != other.age)
                return false;
            return true;
        }

        private ProcessAPIExtTest getOuterType() {
            return ProcessAPIExtTest.this;
        }

    }
}
