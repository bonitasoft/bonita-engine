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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.business.application.importer.DefaultLivingApplicationImporter;
import org.bonitasoft.engine.exception.ApplicationInstallationException;
import org.bonitasoft.engine.tenant.TenantServicesManager;
import org.bonitasoft.platform.version.ApplicationVersionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * @author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomOrDefaultApplicationInstallerTest {

    @Captor
    ArgumentCaptor<Callable<Object>> callableCaptor;

    @Mock
    private ApplicationInstaller applicationInstaller;
    @Mock
    DefaultLivingApplicationImporter defaultLivingApplicationImporter;
    @Mock
    TenantServicesManager tenantServicesManager;
    @Mock
    ApplicationVersionService applicationVersionService;
    @InjectMocks
    @Spy
    private CustomOrDefaultApplicationInstaller listener;

    @Before
    public void before() throws Exception {
        doAnswer(inv -> callableCaptor.getValue().call()).when(tenantServicesManager)
                .inTenantSessionTransaction(callableCaptor.capture());
    }

    @Test
    public void should_detect_one_custom_application() throws Exception {
        //given
        Resource resource1 = mockResource("resource1", true, true, 1L);

        doReturn(new Resource[] { resource1 })
                .when(listener)
                .getCustomAppResourcesFromClasspath();

        //when
        Resource result = listener.detectCustomApplication();

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
                .when(listener)
                .getCustomAppResourcesFromClasspath();

        //then
        assertThatExceptionOfType(ApplicationInstallationException.class)
                .isThrownBy(listener::detectCustomApplication)
                .withMessage("More than one resource of type application zip detected. Abort startup.");
    }

    @Test
    public void should_ignore_non_existing_zip_file() throws Exception {
        //given
        Resource resource1 = mockResource("resource1", false, true, 1L);

        doReturn(new Resource[] { resource1 })
                .when(listener)
                .getCustomAppResourcesFromClasspath();

        //when
        Resource result = listener.detectCustomApplication();

        //then
        assertThat(result).isNull();
    }

    @Test
    public void should_ignore_non_readable_zip_file() throws Exception {
        //given
        Resource resource1 = mockResource("resource1", true, false, 1L);

        doReturn(new Resource[] { resource1 })
                .when(listener)
                .getCustomAppResourcesFromClasspath();

        //when
        Resource result = listener.detectCustomApplication();

        //then
        assertThat(result).isNull();
    }

    @Test
    public void should_ignore_empty_zip_file() throws Exception {
        //given
        Resource resource1 = mockResource("resource1", true, true, 0L);

        doReturn(new Resource[] { resource1 })
                .when(listener)
                .getCustomAppResourcesFromClasspath();

        //when
        Resource result = listener.detectCustomApplication();

        //then
        assertThat(result).isNull();
    }

    @Test
    public void should_install_custom_application_if_detected_and_platform_first_init_and_install_provided_resources_is_false()
            throws Exception {
        //given
        Resource resource1 = mockResource("resource1", true, true, 0L);
        InputStream resourceStream1 = mock(InputStream.class);

        doReturn(resource1).when(listener).detectCustomApplication();
        doReturn(Optional.of("1.0.0")).when(listener).readApplicationVersion(resource1);
        doReturn(resourceStream1).when(resource1).getInputStream();
        final ApplicationArchive applicationArchive = mock(ApplicationArchive.class);
        doReturn(applicationArchive).when(listener).getApplicationArchive(resourceStream1);
        doReturn(true).when(listener).isPlatformFirstInitialization();
        doReturn(false).when(listener).isAddDefaultPages();
        //when
        listener.autoDeployDetectedCustomApplication(any());

        //then
        verify(defaultLivingApplicationImporter, never()).importDefaultPages();
        verify(applicationInstaller).install(applicationArchive, "1.0.0");

        verify(defaultLivingApplicationImporter, never()).execute();
    }

    @Test
    public void should_install_custom_application_and_provided_provide_page_if_detected_and_platform_first_init_and_install_provided_resources_is_true()
            throws Exception {
        //given
        Resource resource1 = mockResource("resource1", true, true, 0L);
        InputStream resourceStream1 = mock(InputStream.class);

        doReturn(resource1).when(listener).detectCustomApplication();
        doReturn(Optional.of("1.0.0")).when(listener).readApplicationVersion(resource1);
        doReturn(resourceStream1).when(resource1).getInputStream();
        final ApplicationArchive applicationArchive = mock(ApplicationArchive.class);
        doReturn(applicationArchive).when(listener).getApplicationArchive(resourceStream1);
        doReturn(true).when(listener).isPlatformFirstInitialization();
        doReturn(true).when(listener).isAddDefaultPages();
        //when
        listener.autoDeployDetectedCustomApplication(any());

        //then
        InOrder inOrder = inOrder(defaultLivingApplicationImporter, applicationInstaller);
        inOrder.verify(defaultLivingApplicationImporter).importDefaultPages();
        inOrder.verify(applicationInstaller).install(applicationArchive, "1.0.0");
        verify(defaultLivingApplicationImporter, never()).execute();
    }

    @Test
    public void should_update_custom_application_if_detected_version_superior_to_deployed_version()
            throws Exception {
        //given
        Resource resource1 = mockResource("resource1", true, true, 0L);
        InputStream resourceStream1 = mock(InputStream.class);

        doReturn(resource1).when(listener).detectCustomApplication();
        doReturn(Optional.of("1.0.1")).when(listener).readApplicationVersion(resource1);
        doReturn(resourceStream1).when(resource1).getInputStream();
        final ApplicationArchive applicationArchive = mock(ApplicationArchive.class);
        doReturn(applicationArchive).when(listener).getApplicationArchive(resourceStream1);
        doReturn(false).when(listener).isPlatformFirstInitialization();
        doReturn("1.0.0").when(applicationVersionService).retrieveApplicationVersion();
        //when
        listener.autoDeployDetectedCustomApplication(any());

        //then
        InOrder inOrder = inOrder(defaultLivingApplicationImporter, applicationInstaller);
        inOrder.verify(applicationInstaller).update(applicationArchive, "1.0.1");
        verify(defaultLivingApplicationImporter, never()).execute();
    }

    @Test
    public void should_update_conf_if_detected_version_equal_to_deployed_version()
            throws Exception {
        //given
        Resource resource1 = mockResource("resource1", true, true, 0L);
        doReturn(resource1).when(listener).detectCustomApplication();
        doReturn(Optional.of("1.0.0")).when(listener).readApplicationVersion(resource1);
        doReturn(false).when(listener).isPlatformFirstInitialization();
        doReturn("1.0.0").when(applicationVersionService).retrieveApplicationVersion();
        //when
        listener.autoDeployDetectedCustomApplication(any());

        //then
        InOrder inOrder = inOrder(defaultLivingApplicationImporter, applicationInstaller);
        inOrder.verify(applicationInstaller, never()).update(any(), any());
        verify(listener, times(1)).findAndUpdateConfiguration();
        verify(defaultLivingApplicationImporter, never()).execute();
    }

    @Test
    public void should_throw_an_exception_if_detected_version_inferior_to_deployed_version()
            throws Exception {
        //given
        Resource resource1 = mockResource("resource1", true, true, 0L);

        doReturn(resource1).when(listener).detectCustomApplication();
        doReturn(Optional.of("0.0.9-SNAPSHOT")).when(listener).readApplicationVersion(resource1);
        doReturn(false).when(listener).isPlatformFirstInitialization();
        doReturn("1.0.0").when(applicationVersionService).retrieveApplicationVersion();
        //when
        String exceptionMessage = Assertions.assertThrows(ApplicationInstallationException.class,
                () -> listener.autoDeployDetectedCustomApplication(any())).getMessage();

        //then
        assertThat(exceptionMessage).contains(
                "An application has been detected, but its newVersion 0.0.9-SNAPSHOT is inferior to the one deployed: 1.0.0. Nothing will be updated, and the Bonita engine startup has been aborted.");
    }

    @Test
    public void should_install_default_applications_if_no_custom_app_detected()
            throws Exception {
        //given
        doReturn(null).when(listener).detectCustomApplication();
        doReturn(true).when(listener).isPlatformFirstInitialization();

        //when
        listener.autoDeployDetectedCustomApplication(any());

        //then
        verify(applicationInstaller, never()).install(any(), eq("1.0.0"));
        verify(defaultLivingApplicationImporter).execute();
        verify(defaultLivingApplicationImporter, never()).importDefaultPages();
    }

    @Test
    public void should_read_the_application_version() throws Exception {
        ResourcePatternResolver cpResourceResolver = new PathMatchingResourcePatternResolver(
                CustomOrDefaultApplicationInstallerTest.class.getClassLoader());

        // load first zip, version should be 1.0.0
        Resource archive = cpResourceResolver.getResource("/customer-application.zip");
        var version = listener.readApplicationVersion(archive);

        assertThat(version.get()).isEqualTo("1.0.0");

        // load second zip, version should be 1.0.1
        archive = cpResourceResolver.getResource("/customer-application-v2.zip");
        version = listener.readApplicationVersion(archive);
        assertThat(version.get()).isEqualTo("1.0.1");

        // load empty zip, expect gracious response of underlying method
        archive = cpResourceResolver.getResource("/empty-customer-application.zip");
        version = listener.readApplicationVersion(archive);
        assertThat(version).isEmpty();
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
