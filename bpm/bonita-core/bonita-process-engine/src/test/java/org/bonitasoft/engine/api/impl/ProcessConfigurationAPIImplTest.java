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
package org.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.core.form.impl.SFormMappingImpl;
import org.bonitasoft.engine.form.FormMapping;
import org.bonitasoft.engine.form.FormMappingTarget;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.page.impl.SPageMappingImpl;
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

    SFormMappingImpl createSFormMapping(FormMappingType type, long formMappingId, String url, Long pageId, String urlAdapter, long processDefinitionId) {
        SFormMappingImpl sFormMapping = new SFormMappingImpl();
        sFormMapping.setId(formMappingId);
        sFormMapping.setProcessDefinitionId(processDefinitionId);
        sFormMapping.setType(type.getId());
        SPageMappingImpl pageMapping = new SPageMappingImpl();
        sFormMapping.setPageMapping(pageMapping);
        pageMapping.setUrlAdapter(urlAdapter);
        pageMapping.setPageId(pageId);
        pageMapping.setUrl(url);
        return sFormMapping;
    }


    @Test
    public void testUpdateFormMapping() throws Exception {
        //given
        SFormMappingImpl sFormMapping = createSFormMapping(FormMappingType.PROCESS_START, FORM_MAPPING_ID, "theUrl", null, null, PROCESS_DEF_ID);
        doReturn(sFormMapping).when(formMappingService).get(FORM_MAPPING_ID);
        //when
        processConfigurationAPI.updateFormMapping(FORM_MAPPING_ID, "theNewForm", FormMappingTarget.INTERNAL);
        //then
        verify(formMappingService, times(1)).update(sFormMapping, FormMappingTarget.INTERNAL.name(), "theNewForm");

    }
}
