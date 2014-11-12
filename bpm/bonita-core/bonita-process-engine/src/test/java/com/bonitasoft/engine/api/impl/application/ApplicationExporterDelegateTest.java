/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.api.impl.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.exception.ExportException;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.exporter.ApplicationExporter;
import com.bonitasoft.engine.business.application.filter.ApplicationsWithIdsFilterBuilder;
import com.bonitasoft.engine.business.application.model.SApplication;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationExporterDelegateTest {

    @Mock
    private ApplicationExporter exporter;

    @Mock
    private ApplicationService applicationService;

    @InjectMocks
    private ApplicationExporterDelegate delegate;

    @Test
    public void exportApplications_should_return_result_of_ApplicationsExporter() throws Exception {
        //given
        QueryOptions options = new ApplicationsWithIdsFilterBuilder(5L).buildQueryOptions();
        List<SApplication> applications = Arrays.asList(mock(SApplication.class));
        given(applicationService.searchApplications(options)).willReturn(applications);
        given(exporter.export(applications)).willReturn("<applications/>".getBytes());

        //when
        byte[] exportApplications = delegate.exportApplications(5);

        //then
        assertThat(new String(exportApplications)).isEqualTo("<applications/>");
    }

    @Test(expected = ExportException.class)
    public void exportApplications_should_throw_SBonitaExportException_when_applicationService_throwsSBonitaReadException() throws Exception {
        //given
        given(applicationService.searchApplications(any(QueryOptions.class))).willThrow(new SBonitaReadException(""));

        //when
        delegate.exportApplications(5);

        //then exception
    }

}
