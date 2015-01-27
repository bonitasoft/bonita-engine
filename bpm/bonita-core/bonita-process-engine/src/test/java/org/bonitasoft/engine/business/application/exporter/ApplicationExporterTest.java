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
