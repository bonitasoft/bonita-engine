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

import static java.lang.String.format;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.result.ExecutionResult;
import org.bonitasoft.engine.api.utils.VisibleForTesting;
import org.bonitasoft.engine.business.application.importer.DefaultLivingApplicationImporter;
import org.bonitasoft.engine.business.application.importer.MandatoryLivingApplicationImporter;
import org.bonitasoft.engine.event.PlatformStartedEvent;
import org.bonitasoft.engine.exception.ApplicationInstallationException;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.tenant.TenantServicesManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * Install custom application if one exists under a specific folder. If none, install default provided applications.
 * <br>
 * This installer listens to the event {@link PlatformStartedEvent} to ensure the platform is started before launching
 * any application installation.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CustomOrDefaultApplicationInstaller {

    public static final String CUSTOM_APPLICATION_DEFAULT_FOLDER = "my-application";

    @Value("${bonita.runtime.custom-application.install-folder:" + CUSTOM_APPLICATION_DEFAULT_FOLDER + "}")
    @Getter
    protected String applicationInstallFolder;

    protected final ApplicationInstaller applicationInstaller;

    private final DefaultLivingApplicationImporter defaultLivingApplicationImporter;
    private final MandatoryLivingApplicationImporter mandatoryLivingApplicationImporter;

    private final TenantServicesManager tenantServicesManager;

    protected final ResourcePatternResolver cpResourceResolver = new PathMatchingResourcePatternResolver(
            CustomOrDefaultApplicationInstaller.class.getClassLoader());
    private final ApplicationArchiveReader applicationArchiveReader;

    private final PlatformService platformService;

    @EventListener
    public void autoDeployDetectedCustomApplication(PlatformStartedEvent event)
            throws Exception {
        final Resource customApplication = detectCustomApplication();
        if (customApplication == null) {
            if (isPlatformFirstInitialization()) {
                // install default provided applications if custom application does not exist
                log.info("No custom application detected under folder {}. Continuing with default Bonita startup.",
                        applicationInstallFolder);
                installDefaultProvidedApplications();
            }
            return;
        }

        try (var applicationArchive = createApplicationArchive(customApplication)) {
            if (!applicationArchive.hasVersion()) {
                throw new ApplicationInstallationException(
                        "Application version not found. Abort installation.");
            }
            log.info("Custom application detected with name '{}' under folder '{}'",
                    customApplication.getFilename(),
                    applicationInstallFolder);
            if (isPlatformFirstInitialization()) {
                // install application if it exists and if it is the first init of the platform
                log.info("Bonita now tries to install it automatically...");
                applicationInstaller.install(applicationArchive);
            } else {
                var currentVersion = platformService.getPlatform().getApplicationVersion();
                log.info("Detected application version: '{}'; Current deployed version: '{}'",
                        applicationArchive.getVersion(),
                        currentVersion);
                if (applicationArchive.hasVersionGreaterThan(currentVersion)) {
                    log.info("Updating the application...");
                    applicationInstaller.update(applicationArchive);
                } else if (applicationArchive.hasVersionEquivalentTo(currentVersion)) {
                    log.info("Updating process configuration only...");
                    findAndUpdateConfiguration();
                } else {
                    throw new ApplicationInstallationException("An application has been detected, but its newVersion "
                            + applicationArchive.getVersion() + " is inferior to the one deployed: " + currentVersion
                            + ". Nothing will be updated, and the Bonita engine startup has been aborted.");
                }
            }
        }
    }

    boolean isPlatformFirstInitialization() {
        return mandatoryLivingApplicationImporter.isFirstRun();
    }

    /**
     * @return custom application resource if one is found, <code>null</code> if none
     * @throws IOException if a resource cannot be resolved
     * @throws ApplicationInstallationException if more than one application is detected
     */
    @VisibleForTesting
    Resource detectCustomApplication() throws IOException, ApplicationInstallationException {
        log.debug("Trying to detect custom application (.zip file from folder {})", applicationInstallFolder);
        return getResourceFromClasspath(getCustomAppResourcesFromClasspath(), "application zip");
    }

    protected static Resource getResourceFromClasspath(Resource[] resources, String type)
            throws IOException, ApplicationInstallationException {
        // loop over resources to find an existing, readable and not empty resource
        Resource customRsource = null;
        var nbZipApplication = 0;
        if (resources != null) {
            for (Resource resource : resources) {
                if (resource.exists() && resource.isReadable() && resource.contentLength() > 0) {
                    nbZipApplication++;
                    customRsource = resource;
                } else {
                    log.warn("A custom resource file '{}' is found but it cannot be read. It will be ignored.",
                            resource.getFilename());
                }
            }
            // if more than one application detected, stop execution by raising an exception
            if (nbZipApplication > 1) {
                throw new ApplicationInstallationException(
                        format("More than one resource of type %s detected. Abort startup.", type));
            }
        }
        return customRsource;
    }

    @VisibleForTesting
    Resource[] getCustomAppResourcesFromClasspath() throws IOException {
        return cpResourceResolver
                .getResources(
                        ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/" + applicationInstallFolder + "/*.zip");
    }

    private void setConfigurationFile(ApplicationArchive applicationArchive)
            throws IOException {
        detectConfigurationFile().ifPresent(resource -> {
            try {
                log.debug("Found application configuration file " + resource.getFilename());
                applicationArchive.setConfigurationFile(resource.getFile());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    protected ApplicationArchive createApplicationArchive(Resource customApplication) {
        try (var applicationZipFileStream = customApplication.getInputStream()) {
            var applicationArchive = applicationArchiveReader.read(applicationZipFileStream);
            setConfigurationFile(applicationArchive);
            return applicationArchive;
        } catch (IOException e) {
            throw new ApplicationInstallationException(
                    String.format("Unable to read the %s application archive.", customApplication.getFilename()), e);
        }
    }

    @VisibleForTesting
    void installDefaultProvidedApplications() throws ApplicationInstallationException {
        try {
            // default app importer requires a tenant session and to be executed inside a transaction
            tenantServicesManager.inTenantSessionTransaction(() -> {
                defaultLivingApplicationImporter.execute();
                return null;
            });
        } catch (Exception e) {
            throw new ApplicationInstallationException("Unable to import default living applications", e);
        }
    }

    protected Optional<Resource> detectConfigurationFile() throws IOException {
        log.debug("Trying to detect configuration file (.bconf file from folder {})", applicationInstallFolder);
        return Optional.ofNullable(
                getResourceFromClasspath(getConfigurationFileResourcesFromClasspath(), "configuration file .bconf"));
    }

    @VisibleForTesting
    Resource[] getConfigurationFileResourcesFromClasspath() throws IOException {
        return cpResourceResolver
                .getResources(
                        ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/" + applicationInstallFolder + "/*.bconf");
    }

    protected void findAndUpdateConfiguration() throws ApplicationInstallationException, IOException {
        final ExecutionResult executionResult = new ExecutionResult();
        detectConfigurationFile().ifPresent(resource -> {
            try {
                log.debug("Found application configuration file " + resource.getFilename());
                applicationInstaller.updateConfiguration(resource.getFile(), executionResult);
            } catch (Exception e) {
                throw new ApplicationInstallationException("Failed to update configuration.", e);
            }
        });
        applicationInstaller.logInstallationResult(executionResult);
        if (executionResult.hasErrors()) {
            throw new ApplicationInstallationException("The Application Archive install operation has been aborted");
        }
    }
}
