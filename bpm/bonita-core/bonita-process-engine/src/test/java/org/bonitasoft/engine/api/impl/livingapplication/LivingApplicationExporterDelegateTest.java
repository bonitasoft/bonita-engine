/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.api.impl.livingapplication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.exporter.ApplicationExporter;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.exception.ExportException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LivingApplicationExporterDelegateTest {

    @Mock
    private ApplicationExporter exporter;

    @Mock
    private ApplicationService applicationService;

    @InjectMocks
    private LivingApplicationExporterDelegate delegate;

    @Test
    public void exportApplications_should_return_result_of_ApplicationsExporter() throws Exception {
        //given
        given(applicationService.getApplication(5L)).willReturn(mock(SApplication.class));
        given(exporter.export(anyList())).willReturn("<applications/>".getBytes());

        //when
        byte[] exportApplications = delegate.exportApplications(5);

        //then
        assertThat(new String(exportApplications)).isEqualTo("<applications/>");
    }

    @Test(expected = ExportException.class)
    public void exportApplications_should_throw_SBonitaExportException_when_applicationService_throwsSBonitaReadException()
            throws Exception {
        //given
        given(applicationService.getApplication(5L)).willThrow(new SBonitaReadException(""));

        //when
        delegate.exportApplications(5);

        //then exception
    }

}
