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
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;

import com.bonitasoft.engine.business.application.impl.converter.ApplicationContainerConverter;
import com.bonitasoft.engine.business.application.impl.exporter.ApplicationContainerExporter;
import com.bonitasoft.engine.business.application.impl.exporter.ApplicationsExporter;
import com.bonitasoft.engine.business.application.model.SApplication;
import com.bonitasoft.engine.business.application.model.xml.ApplicationNodeContainer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationsExporterTest {

    @Mock
    private ApplicationContainerConverter converter;

    @Mock
    private ApplicationContainerExporter containerExporter;

    @InjectMocks
    private ApplicationsExporter applicationsExporter;

    @Test
    public void export_should_return_result_of_ApplicationContainerExporter() throws Exception {
        //given
        List<SApplication> applications = Arrays.asList(mock(SApplication.class));
        ApplicationNodeContainer container = mock(ApplicationNodeContainer.class);

        given(converter.toNode(applications)).willReturn(container);
        given(containerExporter.export(container)).willReturn("<applications/>".getBytes());

        //when
        byte[] exportedApplications = applicationsExporter.export(applications);


        //then
        assertThat(new String(exportedApplications)).isEqualTo("<applications/>");
    }
}