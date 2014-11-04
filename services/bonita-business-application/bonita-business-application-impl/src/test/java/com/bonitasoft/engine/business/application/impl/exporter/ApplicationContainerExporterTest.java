/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.impl.exporter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.net.URL;

import javax.xml.bind.JAXBException;

import com.bonitasoft.engine.business.application.SBonitaExportException;
import com.bonitasoft.engine.business.application.impl.exporter.ApplicationContainerExporter;
import com.bonitasoft.engine.business.application.model.xml.ApplicationNodeContainer;
import com.bonitasoft.engine.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(IOUtils.class)
public class ApplicationContainerExporterTest {

    @Test
    public void exportApplications_should_call_marshall() throws Exception {
        ApplicationContainerExporter exporter = new ApplicationContainerExporter();
        mockStatic(IOUtils.class);
        ApplicationNodeContainer container = mock(ApplicationNodeContainer.class);
        given(IOUtils.marshallObjectToXML(same(container), any(URL.class))).willReturn("<applications/>".getBytes());

        //when
        byte[] bytes = exporter.export(container);


        //then
        assertThat(new String(bytes)).isEqualTo("<applications/>");
    }

    @Test(expected = SBonitaExportException.class)
    public void exportApplications_should_throw_SBonitaExportException_if_marshalling_throws_exception() throws Exception {
        ApplicationContainerExporter exporter = new ApplicationContainerExporter();
        mockStatic(IOUtils.class);
        ApplicationNodeContainer container = mock(ApplicationNodeContainer.class);
        given(IOUtils.marshallObjectToXML(same(container), any(URL.class))).willThrow(new JAXBException(""));

        //when
        exporter.export(container);


        //then exception
    }

}