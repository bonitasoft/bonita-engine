/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.engine.bar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Map;

import org.bonitasoft.engine.api.impl.resolver.BusinessArchiveArtifactsManager;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.form.model.FormMappingModel;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BusinessArchiveServiceImplTest {

    private static final long PROCESS_DEFINITION_ID = 42L;

    @Mock
    private ProcessDefinitionService processDefinitionService;
    @Mock
    private BusinessArchiveArtifactsManager businessArchiveArtifactsManager;
    @Mock
    private ClassLoaderService classLoaderService;
    @Mock
    private DesignProcessDefinition designProcessDefinition;
    @Mock
    private SProcessDefinition sProcessDefinition;
    @Mock
    private BusinessArchive businessArchive;

    private BusinessArchiveServiceImpl businessArchiveService;

    @BeforeEach
    void setUp() {
        businessArchiveService = new BusinessArchiveServiceImpl(processDefinitionService,
                businessArchiveArtifactsManager, classLoaderService);
    }

    @Test
    void deploy_should_reject_business_archive_with_resource_outside_of_archive() {
        //given
        doReturn(Map.of("resources/../../evil.txt", new byte[0])).when(businessArchive).getResources();

        //when-then
        assertThatThrownBy(() -> businessArchiveService.deploy(businessArchive))
                .isInstanceOf(SObjectCreationException.class)
                .hasMessage("The business archive contains a resource whose path is outside of the archive");
        verifyNoInteractions(processDefinitionService, businessArchiveArtifactsManager, classLoaderService);
    }

    @Test
    void deploy_should_store_process_when_resources_are_inside_of_archive() throws Exception {
        //given
        doReturn(Map.of("resources/document.txt", new byte[0])).when(businessArchive).getResources();
        doReturn(designProcessDefinition).when(businessArchive).getProcessDefinition();
        doReturn(new FormMappingModel()).when(businessArchive).getFormMappingModel();
        doReturn("process").when(designProcessDefinition).getName();
        doReturn("1.0").when(designProcessDefinition).getVersion();
        doThrow(new SProcessDefinitionNotFoundException("not deployed yet", 0L)).when(processDefinitionService)
                .getProcessDefinitionId("process", "1.0");
        doReturn(sProcessDefinition).when(processDefinitionService).store(designProcessDefinition);
        // the deployment log reads the user name from the session, not available in a unit test
        var service = spy(businessArchiveService);
        doNothing().when(service).info(sProcessDefinition);

        //when
        var deployedProcessDefinition = service.deploy(businessArchive);

        //then
        assertThat(deployedProcessDefinition).isSameAs(sProcessDefinition);
    }

    @Test
    void export_should_not_check_resource_paths() throws Exception {
        //given
        doReturn(designProcessDefinition).when(processDefinitionService)
                .getDesignProcessDefinition(PROCESS_DEFINITION_ID);
        doReturn(businessArchive).when(businessArchiveArtifactsManager).exportBusinessArchive(PROCESS_DEFINITION_ID,
                designProcessDefinition);

        //when
        var exportedBusinessArchive = businessArchiveService.export(PROCESS_DEFINITION_ID);

        //then the in-memory export (also used by getProcessResources) is not rejected: the paths are checked when
        // the business archive is written to a folder (ProcessAPIImpl.exportBarProcessContentUnderHome)
        assertThat(exportedBusinessArchive).isSameAs(businessArchive);
        verifyNoInteractions(businessArchive);
    }
}
