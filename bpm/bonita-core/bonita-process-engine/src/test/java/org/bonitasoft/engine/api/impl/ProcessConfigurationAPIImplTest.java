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
package org.bonitasoft.engine.api.impl;

import static org.mockito.Mockito.doReturn;

import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.core.form.impl.SFormMappingImpl;
import org.bonitasoft.engine.form.FormMappingTarget;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProcessConfigurationAPIImplTest {

    private static final long PROCESS_DEF_ID = 158l;
    private static final long FORM_MAPPING_ID = 458l;
    @Mock
    public FormMappingService formMappingService;
    @Mock
    public TenantServiceAccessor tenantServiceAccessor;
    @Spy
    public ProcessConfigurationAPIImpl processConfigurationAPI;

    @Before
    public void setUp() throws Exception {
        doReturn(tenantServiceAccessor).when(processConfigurationAPI).getTenantAccessor();
        doReturn(formMappingService).when(tenantServiceAccessor).getFormMappingService();

    }

    @Ignore
    @Test
    public void testSearchFormMappings() throws Exception {

    }

    @Test
    public void testGetProcessStartForm() throws Exception {
        //given
        SFormMappingImpl sFormMapping = createSFormMapping(FormMappingType.PROCESS_START, FORM_MAPPING_ID, null, "myForm", PROCESS_DEF_ID);
        doReturn(sFormMapping).when(formMappingService).get(PROCESS_DEF_ID, FormMappingType.PROCESS_START.getId());
        //when
        //        FormMapping processStartForm = processConfigurationAPI.getProcessStartForm(PROCESS_DEF_ID);
        //        //then
        //        verify(formMappingService, times(1)).get(PROCESS_DEF_ID, FormMappingType.PROCESS_START);
        //        assertThat(processStartForm.getProcessDefinitionId()).isEqualTo(PROCESS_DEF_ID);
        //        assertThat(processStartForm.getType()).isEqualTo(FormMappingType.PROCESS_START);
        //        assertThat(processStartForm.getTarget()).isEqualToIgnoringGivenFields(FormMappingTarget.INTERNAL);
        //        assertThat(processStartForm.getForm()).isEqualTo("myForm");
        //        assertThat(processStartForm.getId()).isEqualTo(FORM_MAPPING_ID);
    }

    SFormMappingImpl createSFormMapping(FormMappingType type, long formMappingId, String target, String myForm1, long processDefinitionId) {
        SFormMappingImpl sFormMapping = new SFormMappingImpl();
        sFormMapping.setId(formMappingId);
        sFormMapping.setProcessDefinitionId(processDefinitionId);
        sFormMapping.setType(type.getId());
        sFormMapping.setPageMappingKey(target);
        //        sFormMapping.setForm(myForm1);
        return sFormMapping;
    }

    @Test
    public void testGetProcessOverviewForm() throws Exception {
        //given
        SFormMappingImpl sFormMapping = createSFormMapping(FormMappingType.PROCESS_START, FORM_MAPPING_ID, null, "myForm", PROCESS_DEF_ID);
        doReturn(sFormMapping).when(formMappingService).get(PROCESS_DEF_ID, FormMappingType.PROCESS_OVERVIEW.getId());
        //when
        //        FormMapping processStartForm = processConfigurationAPI.getProcessOverviewForm(PROCESS_DEF_ID);
        //then
        //        verify(formMappingService, times(1)).get(PROCESS_DEF_ID, FormMappingType.PROCESS_OVERVIEW);
        //        assertThat(processStartForm.getId()).isEqualTo(FORM_MAPPING_ID);

    }

    @Test
    public void testGetHumanTaskForm() throws Exception {
        //given
        SFormMappingImpl sFormMapping = createSFormMapping(FormMappingType.PROCESS_START, FORM_MAPPING_ID, null, "myForm", PROCESS_DEF_ID);
        doReturn(sFormMapping).when(formMappingService).get(PROCESS_DEF_ID, FormMappingType.TASK.getId(), "myTask");
        //when
        //        FormMapping processStartForm = processConfigurationAPI.getTaskForm(PROCESS_DEF_ID, "myTask");
        //then
        //        verify(formMappingService, times(1)).get(PROCESS_DEF_ID, FormMappingType.TASK, "myTask");
        //        assertThat(processStartForm.getId()).isEqualTo(FORM_MAPPING_ID);
    }

    @Test
    public void testUpdateFormMapping() throws Exception {
        //given
        SFormMappingImpl sFormMapping = createSFormMapping(FormMappingType.PROCESS_START, FORM_MAPPING_ID, null, "myForm", PROCESS_DEF_ID);
        doReturn(sFormMapping).when(formMappingService).get(FORM_MAPPING_ID);
        //when
        processConfigurationAPI.updateFormMapping(FORM_MAPPING_ID, "theNewForm", FormMappingTarget.INTERNAL);
        //then
        //        verify(formMappingService, times(1)).update(sFormMapping,"theNewForm",FormMappingTarget.INTERNAL);

    }
}
