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
package org.bonitasoft.engine.execution.archive;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.contract.data.ContractDataService;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SProcessMultiRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SProcessSimpleRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SRefBusinessDataInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class BPMArchiverServiceTest {

    @Mock
    private ArchiveService archiveService;
    @Mock
    private ProcessInstanceService processInstanceService;
    @Mock
    private DocumentService documentService;
    @Mock
    private TechnicalLoggerService logger;
    @Mock
    private SCommentService commentService;
    @Mock
    private ProcessDefinitionService processDefinitionService;
    @Mock
    private ConnectorInstanceService connectorInstanceService;
    @Mock
    private ClassLoaderService classLoaderService;
    @Mock
    private RefBusinessDataService refBusinessDataService;
    @Mock
    private ContractDataService contractDataService;
    @Mock
    private DataInstanceService dataInstanceService;
    @Mock
    private ActivityInstanceService activityInstanceService;
    @Spy
    @InjectMocks
    private BPMArchiverService bpmArchiverService;

    @Test
    public void archiveProcessInstance_should_archive_SRefBusinessDataInstances() throws Exception {
        final SProcessSimpleRefBusinessDataInstance ref1 = new SProcessSimpleRefBusinessDataInstance();
        ref1.setId(1L); // so that those 3 objects are not considered the same (in the verify)
        final SProcessSimpleRefBusinessDataInstance ref2 = new SProcessSimpleRefBusinessDataInstance();
        ref2.setId(2L);
        final SProcessMultiRefBusinessDataInstance ref3 = new SProcessMultiRefBusinessDataInstance();
        List<SRefBusinessDataInstance> sRefBusinessDataInstances = Arrays.asList(ref1, ref2, ref3);
        SProcessInstance processInstance = new SProcessInstance();
        processInstance.setId(451L);

        doReturn(mock(SAProcessInstance.class)).when(bpmArchiverService).buildArchiveProcessInstance(processInstance);
        doNothing().when(bpmArchiverService).archiveConnectorInstancesIfAny(eq(processInstance),
                nullable(SProcessDefinition.class),
                anyLong());

        doReturn(sRefBusinessDataInstances).when(refBusinessDataService)
                .getRefBusinessDataInstances(eq(processInstance.getId()), eq(0), anyInt());
        doNothing().when(refBusinessDataService)
                .archiveRefBusinessDataInstance(nullable(SRefBusinessDataInstance.class));

        bpmArchiverService.archiveAndDeleteProcessInstance(processInstance);

        verify(refBusinessDataService).archiveRefBusinessDataInstance(ref1);
        verify(refBusinessDataService).archiveRefBusinessDataInstance(ref2);
        verify(refBusinessDataService).archiveRefBusinessDataInstance(ref3);
    }

}
