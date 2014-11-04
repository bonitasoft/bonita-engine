/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.api.impl.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import com.bonitasoft.engine.business.application.ApplicationExportService;
import com.bonitasoft.engine.business.application.SBonitaExportException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationExporterDeletegateTest {

    @Mock
    private ApplicationExportService exportService;

    @InjectMocks
    private ApplicationExporterDeletegate deletegate;

    @Test
    public void exportApplications_should_return_result_of_applicationExport_service() throws Exception {
        //given
        given(exportService.exportApplications(4)).willReturn("<applications/>".getBytes());

        //when
        byte[] exportedApps = deletegate.exportApplications(4);

        //then
        assertThat(new String(exportedApps)).isEqualTo("<applications/>");
    }

    @Test(expected = ExecutionException.class)
    public void exportApplications_should_throw_ExecutionException_when_applicationExportService_throws_SBonitaExportException() throws Exception {
        //given
        given(exportService.exportApplications(4)).willThrow(new SBonitaExportException(null));

        //when
        deletegate.exportApplications(4);

        //then exception
    }

}