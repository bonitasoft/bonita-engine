/*
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 */
package com.bonitasoft.engine.api.impl;

import static org.mockito.Mockito.*;

import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.core.form.impl.SFormMappingImpl;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.page.impl.SPageMappingImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessConfigurationAPIExtTest {

    private static final long PROCESS_DEF_ID = 158l;
    private static final long FORM_MAPPING_ID = 458l;

    @Mock
    public FormMappingService formMappingService;
    @Mock
    public TenantServiceAccessor tenantServiceAccessor;
    @Spy
    public ProcessConfigurationAPIExt processConfigurationAPI;

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
        processConfigurationAPI.updateFormMapping(FORM_MAPPING_ID, "theNewForm", null);
        //then
        verify(formMappingService, times(1)).update(sFormMapping, "theNewForm", null);

    }
}
