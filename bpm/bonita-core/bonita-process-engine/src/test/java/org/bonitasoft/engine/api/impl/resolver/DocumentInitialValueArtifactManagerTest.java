/**
 * Copyright (C) 2015 Bonitasoft S.A.
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
 */

package org.bonitasoft.engine.api.impl.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionImpl;
import org.bonitasoft.engine.resources.BARResourceType;
import org.bonitasoft.engine.resources.ProcessResourcesService;
import org.bonitasoft.engine.resources.SBARResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentInitialValueArtifactManagerTest {

    public static final long PROCESS_ID = 456L;
    @Mock
    private ProcessResourcesService processResourcesService;
    @InjectMocks
    private DocumentInitialValueArtifactManager documentInitialValueDependencyManager;

    @Test
    public void deploy_should_call_resourceService_add_on_each_resource() throws Exception {
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchiveBuilder.addDocumentResource(new BarResource("myDoc.pdf",  new byte[] { 0 }));
        businessArchiveBuilder.addDocumentResource(new BarResource("myDoc2.pdf", new byte[] { 1 }));
        businessArchiveBuilder.setProcessDefinition(new ProcessDefinitionBuilder().createNewInstance("tata", "toto").done());
        final SProcessDefinitionImpl processDefinition = new SProcessDefinitionImpl("tata", "toto");
        processDefinition.setId(PROCESS_ID);

        documentInitialValueDependencyManager.deploy(businessArchiveBuilder.done(), processDefinition);

        verify(processResourcesService).add(PROCESS_ID, "myDoc.pdf", BARResourceType.DOCUMENT, new byte[] { 0 });
        verify(processResourcesService).add(PROCESS_ID, "myDoc2.pdf", BARResourceType.DOCUMENT, new byte[] { 1 });
    }

    @Test
    public void exportBusinessArchive_should_export_only_initial_documents() throws Exception {
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchiveBuilder.setProcessDefinition(new ProcessDefinitionBuilder().createNewInstance("tata", "toto").done());
        doReturn(getBarResources()).when(processResourcesService).get(PROCESS_ID, BARResourceType.DOCUMENT, 0, 10);
        doReturn(Collections.singletonList(new SBARResource("10.pdf", BARResourceType.DOCUMENT, PROCESS_ID, new byte[] { 10 }))).when(
                processResourcesService).get(PROCESS_ID, BARResourceType.DOCUMENT, 10, 10);

        documentInitialValueDependencyManager.exportToBusinessArchive(PROCESS_ID, businessArchiveBuilder);

        final BusinessArchive done = businessArchiveBuilder.done();
        final Map<String, byte[]> resources = done.getResources("^documents/.*");
        final ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            strings.add("documents/" + i + ".pdf");
        }
        assertThat(resources.keySet()).containsOnly(strings.toArray(new String[strings.size()]));

    }

    List<SBARResource> getBarResources() {
        final List<SBARResource> sbarResources = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final SBARResource sbarResource = new SBARResource(i + ".pdf", BARResourceType.DOCUMENT, PROCESS_ID, new byte[] { (byte) i });
            sbarResources.add(sbarResource);
        }
        return sbarResources;
    }
}
