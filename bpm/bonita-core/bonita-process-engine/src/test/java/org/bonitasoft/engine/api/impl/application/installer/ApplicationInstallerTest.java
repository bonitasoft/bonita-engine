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
package org.bonitasoft.engine.api.impl.application.installer;

import static java.util.Collections.emptyList;
import static org.bonitasoft.engine.io.FileAndContentUtils.file;
import static org.bonitasoft.engine.io.FileAndContentUtils.zip;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;

import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.internal.ProcessDefinitionImpl;
import org.bonitasoft.engine.business.application.ApplicationImportPolicy;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.io.FileAndContent;
import org.bonitasoft.engine.io.FileOperations;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.search.impl.SearchResultImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta.
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicationInstallerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final ApplicationArchiveReader applicationArchiveReader = spy(new ApplicationArchiveReader());

    @Mock
    private PageAPI pageAPI;
    @Mock
    private ApplicationAPI livingApplicationAPI;
    @Mock
    private ProcessAPI processAPI;

    private ApplicationInstaller applicationInstaller;

    @Before
    public void before() throws Exception {
        applicationInstaller = new ApplicationInstaller.ApplicationInstallerBuilder()
                .pageAPI(pageAPI)
                .livingApplicationAPI(livingApplicationAPI)
                .processAPI(processAPI)
                .applicationArchiveReader(applicationArchiveReader)
                .build();
        doReturn(new SearchResultImpl<Page>(0, emptyList())).when(pageAPI).searchPages(any());
    }

    @Test
    public void should_install_application_containing_all_kind_of_custom_pages() throws Exception {
        ApplicationArchive applicationArchive = ApplicationArchive.builder()
                .page(new FileAndContent("page.zip", zip(file("page.properties", "name=page"))))
                .layout(new FileAndContent("layout.zip", zip(file("page.properties", "name=layout"))))
                .theme(new FileAndContent("theme.zip", zip(file("page.properties", "name=theme"))))
                .restAPIExtension(new FileAndContent("restApiExtension.zip",
                        zip(file("page.properties", "name=restApiExtension"))))
                .build();
        doReturn(mock(Page.class)).when(pageAPI).createPage(anyString(), any(byte[].class));

        applicationInstaller.install(applicationArchive);

        verify(pageAPI).createPage("page", zip(file("page.properties", "name=page")));
        verify(pageAPI).createPage("layout", zip(file("page.properties", "name=layout")));
        verify(pageAPI).createPage("theme", zip(file("page.properties", "name=theme")));
        verify(pageAPI).createPage("restApiExtension", zip(file("page.properties", "name=restApiExtension")));
    }

    @Test
    public void should_install_application_containing_living_applications() throws Exception {
        ApplicationArchive applicationArchive = ApplicationArchive.builder()
                .application(new FileAndContent("application.xml", "content".getBytes())).build();

        applicationInstaller.install(applicationArchive);

        verify(livingApplicationAPI).importApplications("content".getBytes(),
                ApplicationImportPolicy.REPLACE_DUPLICATES);
    }

    @Test
    public void should_install_and_enable_resolved_process() throws Exception {
        byte[] barContent = createValidBusinessArchive();
        ApplicationArchive applicationArchive = ApplicationArchive.builder()
                .process(new FileAndContent("process.bar", barContent)).build();
        ProcessDefinition myProcess = aProcessDefinition(123L);
        doReturn(myProcess).when(processAPI).deploy(any(BusinessArchive.class));
        hasConfigurationState(myProcess, ConfigurationState.RESOLVED);

        applicationInstaller.install(applicationArchive);

        verify(processAPI).deploy(ArgumentMatchers
                .<BusinessArchive> argThat(b -> b.getProcessDefinition().getName().equals("myProcess")));
        verify(processAPI).enableProcess(123L);
    }

    @Test
    public void should_install_only_unresolved_process() throws Exception {
        byte[] barContent = createValidBusinessArchive();
        ApplicationArchive applicationArchive = ApplicationArchive.builder()
                .process(new FileAndContent("process.bar", barContent)).build();
        ProcessDefinition myProcess = aProcessDefinition(123L);
        doReturn(myProcess).when(processAPI).deploy(any(BusinessArchive.class));
        hasConfigurationState(myProcess, ConfigurationState.UNRESOLVED);

        applicationInstaller.install(applicationArchive);

        verify(processAPI).deploy(ArgumentMatchers
                .<BusinessArchive> argThat(b -> b.getProcessDefinition().getName().equals("myProcess")));
        verify(processAPI, never()).enableProcess(anyLong());
    }

    @Test
    public void should_replace_existing_process() throws Exception {
        byte[] barContent = createValidBusinessArchive();
        ApplicationArchive applicationArchive = ApplicationArchive.builder()
                .process(new FileAndContent("process.bar", barContent)).build();
        ProcessDefinition myProcess = aProcessDefinition(123L);
        when(processAPI.deploy(any(BusinessArchive.class))).thenThrow(new AlreadyExistsException("already exists"))
                .thenReturn(myProcess);
        doReturn(456L).when(processAPI).getProcessDefinitionId("myProcess", "1.0");
        hasConfigurationState(myProcess, ConfigurationState.UNRESOLVED);
        doReturn(new SearchResultImpl<>(0, emptyList())).when(processAPI).searchProcessInstances(any());
        doReturn(new SearchResultImpl<>(0, emptyList())).when(processAPI).searchArchivedProcessInstances(any());

        applicationInstaller.install(applicationArchive);

        verify(processAPI).disableProcess(456L);
        verify(processAPI).deleteProcessDefinition(456L);
        verify(processAPI, times(2)).deploy(ArgumentMatchers
                .<BusinessArchive> argThat(b -> b.getProcessDefinition().getName().equals("myProcess")));
    }

    private ProcessDefinitionImpl aProcessDefinition(long id) {
        ProcessDefinitionImpl myProcess = new ProcessDefinitionImpl("myProcess", "1.0");
        myProcess.setId(id);
        return myProcess;
    }

    private void hasConfigurationState(ProcessDefinition myProcess, ConfigurationState state)
            throws ProcessDefinitionNotFoundException {
        ProcessDeploymentInfo processDeploymentInfo = mock(ProcessDeploymentInfo.class);
        doReturn(state).when(processDeploymentInfo).getConfigurationState();
        doReturn(processDeploymentInfo).when(processAPI).getProcessDeploymentInfo(myProcess.getId());
    }

    private byte[] createValidBusinessArchive()
            throws InvalidBusinessArchiveFormatException, InvalidProcessDefinitionException, IOException {
        BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(new ProcessDefinitionBuilder().createNewInstance("myProcess", "1.0").done())
                .done();
        File businessArchiveFile = temporaryFolder.newFile();
        businessArchiveFile.delete();
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, businessArchiveFile);
        return FileOperations.readFully(businessArchiveFile);
    }

}
