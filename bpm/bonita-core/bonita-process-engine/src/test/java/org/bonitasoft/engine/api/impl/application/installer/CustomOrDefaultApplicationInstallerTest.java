/**
 * Copyright (C) 2023 Bonitasoft S.A.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.bonitasoft.engine.api.impl.resolver.BusinessArchiveArtifactsManager;
import org.bonitasoft.engine.api.result.ExecutionResult;
import org.bonitasoft.engine.business.application.importer.DefaultLivingApplicationImporter;
import org.bonitasoft.engine.business.application.importer.MandatoryLivingApplicationImporter;
import org.bonitasoft.engine.business.data.BusinessDataModelRepository;
import org.bonitasoft.engine.exception.ApplicationInstallationException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.tenant.TenantServicesManager;
import org.bonitasoft.engine.tenant.TenantStateManager;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * @author Emmanuel Duchastenier
 */
public class CustomOrDefaultApplicationInstallerTest {

    private CustomOrDefaultApplicationInstaller installer;
    private ApplicationInstaller applicationInstaller;

    @Before
    public void before() {
        applicationInstaller = spy(new ApplicationInstaller(mock(BusinessDataModelRepository.class),
                mock(TransactionService.class), 1L, mock(SessionAccessor.class), mock(SessionService.class),
                mock(TenantStateManager.class), mock(BusinessArchiveArtifactsManager.class),
                mock(ApplicationArchiveReader.class)));
        installer = spy(new CustomOrDefaultApplicationInstaller(applicationInstaller,
                mock(DefaultLivingApplicationImporter.class),
                mock(MandatoryLivingApplicationImporter.class),
                mock(TenantServicesManager.class)));
    }

    @Test
    public void should_detect_one_custom_application() throws Exception {
        //given
        Resource resource1 = mockResource("resource1", true, true, 1L);

        doReturn(new Resource[] { resource1 })
                .when(installer)
                .getResourcesFromClasspath();

        //when
        Resource result = installer.detectCustomApplication();

        //then
        assertThat(result).isNotNull();
        assertThat(result.getFilename()).isEqualTo("resource1");
    }

    @Test
    public void should_raise_exception_if_more_than_one_application() throws Exception {
        //given
        Resource resource1 = mockResource("resource1", true, true, 1L);
        Resource resource2 = mockResource("resource2", true, true, 1L);

        doReturn(new Resource[] { resource1, resource2 })
                .when(installer)
                .getResourcesFromClasspath();

        //then
        assertThatExceptionOfType(ApplicationInstallationException.class)
                .isThrownBy(installer::detectCustomApplication)
                .withMessage("More than one application detected. Abort startup.");
    }

    @Test
    public void should_ignore_non_existing_zip_file() throws Exception {
        //given
        Resource resource1 = mockResource("resource1", false, true, 1L);

        doReturn(new Resource[] { resource1 })
                .when(installer)
                .getResourcesFromClasspath();

        //when
        Resource result = installer.detectCustomApplication();

        //then
        assertThat(result).isNull();
    }

    @Test
    public void should_ignore_non_readable_zip_file() throws Exception {
        //given
        Resource resource1 = mockResource("resource1", true, false, 1L);

        doReturn(new Resource[] { resource1 })
                .when(installer)
                .getResourcesFromClasspath();

        //when
        Resource result = installer.detectCustomApplication();

        //then
        assertThat(result).isNull();
    }

    @Test
    public void should_ignore_empty_zip_file() throws Exception {
        //given
        Resource resource1 = mockResource("resource1", true, true, 0L);

        doReturn(new Resource[] { resource1 })
                .when(installer)
                .getResourcesFromClasspath();

        //when
        Resource result = installer.detectCustomApplication();

        //then
        assertThat(result).isNull();
    }

    @Test
    public void should_install_custom_application_if_detected_and_platform_first_init() throws Exception {
        //given
        Resource resource1 = mockResource("resource1", true, true, 0L);
        InputStream resourceStream1 = mock(InputStream.class);

        doReturn(resource1).when(installer).detectCustomApplication();
        doReturn(resourceStream1).when(resource1).getInputStream();
        doReturn(mock(ExecutionResult.class)).when(applicationInstaller).install(any(InputStream.class));
        doReturn(true).when(installer).isPlatformFirstInitialization();

        //when
        installer.autoDeployDetectedCustomApplication(any());

        //then
        verify(installer).installCustomApplication(eq(resource1));
        verify(applicationInstaller).install(eq(resourceStream1));
        verify(installer, never()).installDefaultProvidedApplications();
    }

    @Test
    public void should_install_default_applications_if_no_custom_app_detected_and_platform_first_init()
            throws Exception {
        //given
        doReturn(null).when(installer).detectCustomApplication();
        doReturn(true).when(installer).isPlatformFirstInitialization();

        //when
        installer.autoDeployDetectedCustomApplication(any());

        //then
        verify(installer, never()).installCustomApplication(any());
        verify(applicationInstaller, never()).install(any(InputStream.class));
        verify(installer).installDefaultProvidedApplications();
    }

    @Test
    public void should_install_default_applications_if_no_custom_app_detected_and_not_platform_first_init()
            throws Exception {
        //given
        doReturn(null).when(installer).detectCustomApplication();
        doReturn(false).when(installer).isPlatformFirstInitialization();

        //when
        installer.autoDeployDetectedCustomApplication(any());

        //then
        verify(installer, never()).installCustomApplication(any());
        verify(applicationInstaller, never()).install(any(InputStream.class));
        verify(installer).installDefaultProvidedApplications();
    }

    private Resource mockResource(String filename, boolean exists, boolean isReadable, long contentLength)
            throws IOException {
        Resource resource = spy(new FileSystemResource(mock(File.class)));
        doReturn(exists).when(resource).exists();
        doReturn(isReadable).when(resource).isReadable();
        doReturn(contentLength).when(resource).contentLength();
        doReturn(filename).when(resource).getFilename();
        return resource;
    }
}
