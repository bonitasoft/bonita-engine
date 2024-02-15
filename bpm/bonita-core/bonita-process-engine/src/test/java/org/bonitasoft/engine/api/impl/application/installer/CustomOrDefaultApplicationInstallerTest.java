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
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import org.bonitasoft.engine.business.application.importer.DefaultLivingApplicationImporter;
import org.bonitasoft.engine.exception.ApplicationInstallationException;
import org.bonitasoft.engine.tenant.TenantServicesManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * @author Emmanuel Duchastenier
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomOrDefaultApplicationInstallerTest {

    @Captor
    ArgumentCaptor<Callable<Object>> callableCaptor;

    @Mock
    private ApplicationInstaller applicationInstaller;
    @Mock
    DefaultLivingApplicationImporter defaultLivingApplicationImporter;
    @Mock
    TenantServicesManager tenantServicesManager;

    @Mock
    private ApplicationArchiveReader applicationArchiveReader;
    @InjectMocks
    @Spy
    private CustomOrDefaultApplicationInstaller listener;

    @BeforeEach
    void before() throws Exception {
        doAnswer(inv -> callableCaptor.getValue().call()).when(tenantServicesManager)
                .inTenantSessionTransaction(callableCaptor.capture());
    }

    @Test
    void should_detect_one_custom_application() throws Exception {
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
    void should_raise_exception_if_more_than_one_application() throws Exception {
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

    @ParameterizedTest
    @MethodSource("ignoredApplicationResources")
    void ignoreDetectedCustomApplication(boolean exists, boolean readable, long contentSize) throws Exception {
        //given
        Resource resource1 = mockResource("resource1", exists, readable, contentSize);

        doReturn(new Resource[] { resource1 })
                .when(listener)
                .getCustomAppResourcesFromClasspath();

        //when
        Resource result = listener.detectCustomApplication();

        //then
        assertThat(result).isNull();
    }

    private static Stream<Arguments> ignoredApplicationResources() {
        return Stream.of(
                Arguments.of(false, false, 0L), // resource does not exist
                Arguments.of(true, false, 1L), // resource is not readable
                Arguments.of(true, true, 0L) // resource has no content
        );
    }

    @Test
    void should_install_custom_application_if_detected_and_platform_first_init()
            throws Exception {
        //given
        Resource resource1 = mockResource("resource1", true, true, 0L);
        InputStream resourceStream1 = mock(InputStream.class);

        doReturn(resource1).when(listener).detectCustomApplication();
        doReturn(resourceStream1).when(resource1).getInputStream();
        final ApplicationArchive applicationArchive = new ApplicationArchive();
        applicationArchive.setVersion("1.0.0");
        doReturn(applicationArchive).when(applicationArchiveReader).read(resourceStream1);
        doReturn(true).when(listener).isPlatformFirstInitialization();
        //when
        listener.autoDeployDetectedCustomApplication(any());

        //then
        verify(applicationInstaller).install(applicationArchive);
        verify(defaultLivingApplicationImporter, never()).execute();
    }

    @Test
    void should_update_custom_application_if_detected_version_superior_to_deployed_version()
            throws Exception {
        //given
        Resource resource1 = mockResource("resource1", true, true, 0L);
        InputStream resourceStream1 = mock(InputStream.class);

        doReturn(resource1).when(listener).detectCustomApplication();
        doReturn(resourceStream1).when(resource1).getInputStream();
        final ApplicationArchive applicationArchive = new ApplicationArchive();
        applicationArchive.setVersion("1.0.1");
        doReturn(applicationArchive).when(applicationArchiveReader).read(resourceStream1);
        doReturn(false).when(listener).isPlatformFirstInitialization();
        doReturn("1.0.0").when(listener).getInstalledApplicationVersion();
        //when
        listener.autoDeployDetectedCustomApplication(any());

        //then
        InOrder inOrder = inOrder(defaultLivingApplicationImporter, applicationInstaller);
        inOrder.verify(applicationInstaller).update(applicationArchive);
        verify(defaultLivingApplicationImporter, never()).execute();
    }

    @Test
    void should_update_conf_if_detected_version_equal_to_deployed_version()
            throws Exception {
        //given
        Resource resource1 = mockResource("resource1", true, true, 0L);
        doReturn(resource1).when(listener).detectCustomApplication();
        var applicationArchive = new ApplicationArchive();
        applicationArchive.setVersion("1.0.0");
        doReturn(applicationArchive).when(listener).createApplicationArchive(resource1);
        doReturn(false).when(listener).isPlatformFirstInitialization();
        doReturn("1.0.0").when(listener).getInstalledApplicationVersion();
        //when
        listener.autoDeployDetectedCustomApplication(any());

        //then
        InOrder inOrder = inOrder(defaultLivingApplicationImporter, applicationInstaller);
        inOrder.verify(applicationInstaller, never()).update(any());
        verify(listener, times(1)).findAndUpdateConfiguration();
        verify(defaultLivingApplicationImporter, never()).execute();
    }

    @Test
    void should_throw_an_exception_if_detected_version_inferior_to_deployed_version()
            throws Exception {
        //given
        Resource resource1 = mockResource("resource1", true, true, 0L);

        doReturn(resource1).when(listener).detectCustomApplication();
        var applicationArchive = new ApplicationArchive();
        applicationArchive.setVersion("0.0.9-SNAPSHOT");
        doReturn(applicationArchive).when(listener).createApplicationArchive(resource1);
        doReturn(false).when(listener).isPlatformFirstInitialization();
        doReturn("1.0.0").when(listener).getInstalledApplicationVersion();
        //when
        String exceptionMessage = Assertions.assertThrows(ApplicationInstallationException.class,
                () -> listener.autoDeployDetectedCustomApplication(any())).getMessage();

        //then
        assertThat(exceptionMessage).contains(
                "An application has been detected, but its newVersion 0.0.9-SNAPSHOT is inferior to the one deployed: 1.0.0. Nothing will be updated, and the Bonita engine startup has been aborted.");
    }

    @Test
    void should_install_default_applications_if_no_custom_app_detected()
            throws Exception {
        //given
        doReturn(null).when(listener).detectCustomApplication();
        doReturn(true).when(listener).isPlatformFirstInitialization();

        //when
        listener.autoDeployDetectedCustomApplication(any());

        //then
        verify(applicationInstaller, never()).install(any());
        verify(defaultLivingApplicationImporter).execute();
    }

    @Test
    void should_raise_exception_if_more_than_one_configuration_file() throws Exception {
        //given
        Resource resource1 = mockResource("resource1", true, true, 1L);
        Resource resource2 = mockResource("resource2", true, true, 1L);

        doReturn(new Resource[] { resource1, resource2 })
                .when(listener)
                .getConfigurationFileResourcesFromClasspath();

        //then
        assertThatExceptionOfType(ApplicationInstallationException.class)
                .isThrownBy(listener::detectConfigurationFile)
                .withMessage("More than one resource of type configuration file .bconf detected. Abort startup.");
    }

    @Test
    void should_return_empty_optional_if_no_configuration_file_found() throws Exception {
        doReturn(null)
                .when(listener)
                .getConfigurationFileResourcesFromClasspath();

        listener.detectConfigurationFile();

        //then
        assertThat(Optional.empty()).isEqualTo(listener.detectConfigurationFile());

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
