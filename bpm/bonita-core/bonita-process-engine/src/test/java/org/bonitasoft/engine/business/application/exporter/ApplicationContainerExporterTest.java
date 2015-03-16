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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.net.URL;
import javax.xml.bind.JAXBException;

import org.bonitasoft.engine.business.application.xml.ApplicationNodeContainer;
import org.bonitasoft.engine.exception.ExportException;
import org.bonitasoft.engine.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(IOUtils.class)
public class ApplicationContainerExporterTest {

    @Test
    public void exportApplications_should_call_marshall() throws Exception {
        final ApplicationContainerExporter exporter = new ApplicationContainerExporter();
        mockStatic(IOUtils.class);
        final ApplicationNodeContainer container = mock(ApplicationNodeContainer.class);
        given(IOUtils.marshallObjectToXML(same(container), any(URL.class))).willReturn("<applications/>".getBytes());

        //when
        final byte[] bytes = exporter.export(container);

        //then
        assertThat(new String(bytes)).isEqualTo("<applications/>");
    }

    @Test(expected = ExportException.class)
    public void exportApplications_should_throw_SBonitaExportException_if_marshalling_throws_exception() throws Exception {
        final ApplicationContainerExporter exporter = new ApplicationContainerExporter();
        mockStatic(IOUtils.class);
        final ApplicationNodeContainer container = mock(ApplicationNodeContainer.class);
        given(IOUtils.marshallObjectToXML(same(container), any(URL.class))).willThrow(new JAXBException(""));

        //when
        exporter.export(container);

        //then exception
    }

}
