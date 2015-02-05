/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.businessdata.BusinessDataReference;
import com.bonitasoft.engine.businessdata.MultipleBusinessDataReference;
import com.bonitasoft.engine.businessdata.SimpleBusinessDataReference;
import com.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SRefBusinessDataInstanceNotFoundException;
import com.bonitasoft.engine.core.process.instance.model.SProcessMultiRefBusinessDataInstance;
import com.bonitasoft.engine.core.process.instance.model.SProcessSimpleRefBusinessDataInstance;
import com.bonitasoft.engine.core.process.instance.model.impl.SProcessMultiRefBusinessDataInstanceImpl;
import com.bonitasoft.engine.core.process.instance.model.impl.SProcessSimpleRefBusinessDataInstanceImpl;
import com.bonitasoft.engine.service.TenantServiceAccessor;

@RunWith(MockitoJUnitRunner.class)
public class ProcessAPIExtTest {

    @Mock
    private TenantServiceAccessor serviceAccessor;

    @Mock
    private RefBusinessDataService refBusinessDataService;

    @Spy
    private ProcessAPIExt processAPI;

    @Before
    public void setUp() {
        doReturn(serviceAccessor).when(processAPI).getTenantAccessor();
        when(serviceAccessor.getRefBusinessDataService()).thenReturn(refBusinessDataService);
    }

    private SProcessSimpleRefBusinessDataInstance buildSimpleRefBusinessData(final String name, final String dataClassName, final Long dataId) {
        final SProcessSimpleRefBusinessDataInstanceImpl reference = new SProcessSimpleRefBusinessDataInstanceImpl();
        reference.setId(64645L);
        reference.setName(name);
        reference.setDataClassName(dataClassName);
        reference.setDataId(dataId);
        return reference;
    }

    private SProcessMultiRefBusinessDataInstance buildMultiRefBusinessData(final String name, final String dataClassName, final List<Long> dataIds) {
        final SProcessMultiRefBusinessDataInstanceImpl reference = new SProcessMultiRefBusinessDataInstanceImpl();
        reference.setId(64645L);
        reference.setName(name);
        reference.setDataClassName(dataClassName);
        reference.setDataIds(dataIds);
        return reference;
    }

    @Test
    public void getProcessBusinessDataShouldReturnASimpleBusinessDataReference() throws Exception {
        final String name = "address";
        final String dataClassName = "com.bonitasoft.Address";
        final Long dataId = 6873654654L;
        final SProcessSimpleRefBusinessDataInstance reference = buildSimpleRefBusinessData(name, dataClassName, dataId);
        when(refBusinessDataService.getRefBusinessDataInstance(name, 487654L)).thenReturn(reference);

        final BusinessDataReference businessData = processAPI.getProcessBusinessDataReference(name, 487654L);

        assertThat(businessData).isInstanceOf(SimpleBusinessDataReference.class);
        assertThat(businessData.getName()).isEqualTo(name);
        assertThat(businessData.getType()).isEqualTo(dataClassName);
        assertThat(((SimpleBusinessDataReference) businessData).getStorageId()).isEqualTo(dataId);
    }

    @Test
    public void getProcessBusinessDataShouldReturnAMultipleBusinessDataReference() throws Exception {
        final String name = "myEmployees";
        final String dataClassName = "com.bonitasoft.Employee";
        final List<Long> dataIds = Arrays.asList(8735468463748L, 87634386731L);
        final SProcessMultiRefBusinessDataInstance reference = buildMultiRefBusinessData(name, dataClassName, dataIds);
        when(refBusinessDataService.getRefBusinessDataInstance(name, 487654L)).thenReturn(reference);

        final BusinessDataReference businessData = processAPI.getProcessBusinessDataReference(name, 487654L);

        assertThat(businessData).isInstanceOf(MultipleBusinessDataReference.class);
        assertThat(businessData.getName()).isEqualTo(name);
        assertThat(businessData.getType()).isEqualTo(dataClassName);
        assertThat(((MultipleBusinessDataReference) businessData).getStorageIds()).isEqualTo(dataIds);
    }

    @Test(expected = RetrieveException.class)
    public void getProcessBusinessDataShouldThrowARuntimeException() throws Exception {
        final String name = "myEmployees";
        when(refBusinessDataService.getRefBusinessDataInstance(name, 487654L)).thenThrow(new SBonitaReadException("exception"));

        processAPI.getProcessBusinessDataReference(name, 487654L);
    }

    @Test(expected = DataNotFoundException.class)
    public void getProcessBusinessDataShouldThrowADataNotFoundException() throws Exception {
        final String name = "myEmployees";
        when(refBusinessDataService.getRefBusinessDataInstance(name, 487654L)).thenThrow(new SRefBusinessDataInstanceNotFoundException(487654L, name));

        processAPI.getProcessBusinessDataReference(name, 487654L);
    }

    @Test
    public void getProcessBusinessDataReferencesShouldReturnAListOfBusinessDataReferences() throws Exception {
        final String name = "address";
        final String dataClassName = "com.bonitasoft.Address";
        final Long dataId = 6873654654L;
        final List<Long> dataIds = Arrays.asList(8735468463748L, 87634386731L);
        final SProcessSimpleRefBusinessDataInstance sReference1 = buildSimpleRefBusinessData(name, dataClassName, dataId);
        final SProcessMultiRefBusinessDataInstance sReference2 = buildMultiRefBusinessData(name, dataClassName, dataIds);
        when(refBusinessDataService.getRefBusinessDataInstances(487654L, 0, 10)).thenReturn(Arrays.asList(sReference1, sReference2));

        final List<BusinessDataReference> references = processAPI.getProcessBusinessDataReferences(487654L, 0, 10);

        assertThat(references).hasSize(2);
        final BusinessDataReference reference1 = references.get(0);
        assertThat(reference1).isInstanceOf(SimpleBusinessDataReference.class);
        assertThat(reference1.getName()).isEqualTo(name);
        assertThat(reference1.getType()).isEqualTo(dataClassName);
        assertThat(((SimpleBusinessDataReference) reference1).getStorageId()).isEqualTo(dataId);
        final BusinessDataReference reference2 = references.get(1);
        assertThat(reference2).isInstanceOf(MultipleBusinessDataReference.class);
        assertThat(reference2.getName()).isEqualTo(name);
        assertThat(reference2.getType()).isEqualTo(dataClassName);
        assertThat(((MultipleBusinessDataReference) reference2).getStorageIds()).isEqualTo(dataIds);
    }

    @Test(expected = RetrieveException.class)
    public void getProcessBusinessDataReferencesShouldThrowAnException() throws Exception {
        when(refBusinessDataService.getRefBusinessDataInstances(487654L, 0, 10)).thenThrow(new SBonitaReadException("exception"));

        processAPI.getProcessBusinessDataReferences(487654L, 0, 10);
    }

}
