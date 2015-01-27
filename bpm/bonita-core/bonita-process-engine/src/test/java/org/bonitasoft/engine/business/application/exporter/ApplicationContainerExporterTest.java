/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

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
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.exception.ExportException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(IOUtil.class)
public class ApplicationContainerExporterTest {

    @Test
    public void exportApplications_should_call_marshall() throws Exception {
        final ApplicationContainerExporter exporter = new ApplicationContainerExporter();
        mockStatic(IOUtil.class);
        final ApplicationNodeContainer container = mock(ApplicationNodeContainer.class);
        given(IOUtil.marshallObjectToXML(same(container), any(URL.class))).willReturn("<applications/>".getBytes());

        //when
        final byte[] bytes = exporter.export(container);

        //then
        assertThat(new String(bytes)).isEqualTo("<applications/>");
    }

    @Test(expected = ExportException.class)
    public void exportApplications_should_throw_SBonitaExportException_if_marshalling_throws_exception() throws Exception {
        final ApplicationContainerExporter exporter = new ApplicationContainerExporter();
        mockStatic(IOUtil.class);
        final ApplicationNodeContainer container = mock(ApplicationNodeContainer.class);
        given(IOUtil.marshallObjectToXML(same(container), any(URL.class))).willThrow(new JAXBException(""));

        //when
        exporter.export(container);

        //then exception
    }

}
