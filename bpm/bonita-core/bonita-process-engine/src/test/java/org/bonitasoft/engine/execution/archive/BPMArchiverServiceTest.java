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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.business.data.DataRetentionBdmTrackingService;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.contract.data.ContractDataService;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.BPMFailureService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SProcessMultiRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SProcessSimpleRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SRefBusinessDataInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
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
    @Mock
    private BPMFailureService failureService;
    @Mock
    private DataRetentionBdmTrackingService dataRetentionBdmTrackingService;
    @Spy
    @InjectMocks
    private BPMArchiverService bpmArchiverService;

    @Test
    public void archiveProcessInstance_should_archive_SRefBusinessDataInstances() throws Exception {
        final SProcessSimpleRefBusinessDataInstance ref1 = new SProcessSimpleRefBusinessDataInstance();
        ref1.setId(1L); // so that those 3 objects are not considered the same (in the verify)
        ref1.setDataId(100L);
        ref1.setDataClassName("com.company.model.Invoice");
        final SProcessSimpleRefBusinessDataInstance ref2 = new SProcessSimpleRefBusinessDataInstance();
        ref2.setId(2L);
        ref2.setDataId(200L);
        ref2.setDataClassName("com.company.model.Invoice");
        final SProcessMultiRefBusinessDataInstance ref3 = new SProcessMultiRefBusinessDataInstance();
        ref3.setId(3L);
        ref3.setDataIds(Arrays.asList(300L, 301L));
        ref3.setDataClassName("com.company.model.LineItem");
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

        // Verify tracking upsert for simple refs
        verify(dataRetentionBdmTrackingService).upsert(100L, "com.company.model.Invoice");
        verify(dataRetentionBdmTrackingService).upsert(200L, "com.company.model.Invoice");
        // Verify tracking upsert for multi ref (each data ID)
        verify(dataRetentionBdmTrackingService).upsert(300L, "com.company.model.LineItem");
        verify(dataRetentionBdmTrackingService).upsert(301L, "com.company.model.LineItem");
    }

    @Test
    public void archiveAndDeleteFlowNodeInstance_should_archive_failures() throws Exception {
        var flowNodeInstance = mock(SFlowNodeInstance.class);
        when(flowNodeInstance.getId()).thenReturn(123L);
        when(flowNodeInstance.getType()).thenReturn(SFlowNodeType.END_EVENT);

        bpmArchiverService.archiveAndDeleteFlowNodeInstance(flowNodeInstance, 1L);

        verify(failureService).archiveFlowNodeFailures(eq(123L), any(long.class));
    }

}
