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
package org.bonitasoft.engine.api.impl.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.exporter.ApplicationExporter;
import org.bonitasoft.engine.business.application.filter.ApplicationsWithIdsFilterBuilder;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.exception.ExportException;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
