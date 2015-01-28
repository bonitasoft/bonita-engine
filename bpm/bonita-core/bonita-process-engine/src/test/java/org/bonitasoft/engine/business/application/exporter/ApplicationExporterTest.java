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
package org.bonitasoft.engine.business.application.exporter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.business.application.converter.ApplicationContainerConverter;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.xml.ApplicationNodeContainer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationExporterTest {

    @Mock
    private ApplicationContainerConverter converter;

    @Mock
    private ApplicationContainerExporter containerExporter;

    @InjectMocks
    private ApplicationExporter applicationExporter;

    @Test
    public void export_should_return_result_of_ApplicationContainerExporter() throws Exception {
        //given
        List<SApplication> applications = Arrays.asList(mock(SApplication.class));
        ApplicationNodeContainer container = mock(ApplicationNodeContainer.class);

        given(converter.toNode(applications)).willReturn(container);
        given(containerExporter.export(container)).willReturn("<applications/>".getBytes());

        //when
        byte[] exportedApplications = applicationExporter.export(applications);

        //then
        assertThat(new String(exportedApplications)).isEqualTo("<applications/>");
    }
}
