/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package org.bonitasoft.engine.business.application.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.business.application.xml.ApplicationNodeContainer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationsImporterTest {

    @Mock
    private ApplicationContainerImporter containerImporter;

    @Mock
    private ApplicationImporter applicationImporter;

    @InjectMocks
    private ApplicationsImporter applicationsImporter;

    @Test
    public void importApplications_should_create_applications_contained_in_xml_file_and_return_status() throws Exception {
        //given
        long createdBy = 5L;

        ApplicationNode node1 = mock(ApplicationNode.class);
        ApplicationNode node2 = mock(ApplicationNode.class);

        ImportStatus status1 = mock(ImportStatus.class);
        ImportStatus status2 = mock(ImportStatus.class);

        ApplicationNodeContainer nodeContainer = mock(ApplicationNodeContainer.class);
        given(containerImporter.importXML("<applications/>".getBytes())).willReturn(nodeContainer);
        given(nodeContainer.getApplications()).willReturn(Arrays.asList(node1, node2));
        given(applicationImporter.importApplication(node1, createdBy)).willReturn(status1);
        given(applicationImporter.importApplication(node2, createdBy)).willReturn(status2);

        //when
        List<ImportStatus> importStatus = applicationsImporter.importApplications("<applications/>".getBytes(), createdBy);

        //then
        assertThat(importStatus).containsExactly(status1, status2);
    }

}
