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

import java.io.IOException;
import java.io.InputStream;

import javax.naming.NamingException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.utils.VisibleForTesting;
import org.bonitasoft.engine.business.application.importer.DefaultLivingApplicationImporter;
import org.bonitasoft.engine.event.PlatformStartedEvent;
import org.bonitasoft.engine.exception.ApplicationInstallationException;
import org.bonitasoft.engine.tenant.TenantServicesManager;
import org.bonitasoft.platform.setup.PlatformSetupAccessor;
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
    private String applicationInstallFolder;

    private final ApplicationInstaller applicationInstaller;

    private final DefaultLivingApplicationImporter defaultLivingApplicationImporter;

    private final TenantServicesManager tenantServicesManager;

    private final ResourcePatternResolver cpResourceResolver = new PathMatchingResourcePatternResolver(
            CustomOrDefaultApplicationInstaller.class.getClassLoader());

    @EventListener
    public void autoDeployDetectedCustomApplication(PlatformStartedEvent event)
            throws ApplicationInstallationException, IOException {

        // check under custom application default folder if an application is found, and retrieve it
        final Resource customApplication = detectCustomApplication();

        if (customApplication != null && isPlatformFirstInitialization()) {
            // install application if it exists and if it is a first init of the platform
            installCustomApplication(customApplication);
        } else {
            // install default provided applications if custom application does not exist
            installDefaultProvidedApplications();
        }
    }

    boolean isPlatformFirstInitialization() {
        try {
            return PlatformSetupAccessor.getPlatformSetup().isFirstInitialization();
        } catch (NamingException e) {
            log.warn("Failed to retrieve platform setup configuration.", e);
            return false;
        }
    }

    /**
     * @return custom application resource if one is found, <code>null</code> if none
     * @throws IOException if a resource cannot be resolved
     * @throws ApplicationInstallationException if more than one application is detected
     */
    @VisibleForTesting
    Resource detectCustomApplication() throws IOException, ApplicationInstallationException {
        log.info("Trying to detect custom application (.zip file from folder {})", applicationInstallFolder);

        Resource[] resources = getResourcesFromClasspath();

        // loop over resources to find an existing, readable and not empty resource
        Resource customApplicationResource = null;
        var nbZipApplication = 0;
        for (Resource resource : resources) {
            if (resource.exists() && resource.isReadable() && resource.contentLength() > 0) {
                nbZipApplication++;
                customApplicationResource = resource;
            } else {
                log.info("A zip file '{}' is found but it cannot be read. It will be ignored.", resource.getFilename());
            }
        }
        // if more than one application detected, stop execution by raising an exception
        if (nbZipApplication > 1) {
            throw new ApplicationInstallationException("More than one application detected. Abort startup.");
        }

        return customApplicationResource;
    }

    @VisibleForTesting
    Resource[] getResourcesFromClasspath() throws IOException {
        return cpResourceResolver
                .getResources(
                        ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/" + applicationInstallFolder + "/*.zip");
    }

    /**
     * @param customApplication custom application resource
     * @throws ApplicationInstallationException if unable to install the application
     */
    @VisibleForTesting
    void installCustomApplication(final Resource customApplication) throws ApplicationInstallationException {
        String resourceName = customApplication.getFilename();
        try (final InputStream applicationZipFileStream = customApplication.getInputStream()) {
            log.info(
                    "No custom application detected under folder {}. Continuing with default Bonita startup.",
                    applicationInstallFolder);
            log.info(
                    "Custom application detected with name '{}' under folder '{}'."
                            + " Bonita now tries to install it automatically...",
                    resourceName, applicationInstallFolder);
            applicationInstaller.install(applicationZipFileStream);
        } catch (IOException | ApplicationInstallationException e) {
            throw new ApplicationInstallationException("Unable to install the application " + resourceName, e);
        }
    }

    @VisibleForTesting
    void installDefaultProvidedApplications() throws ApplicationInstallationException {
        log.info("No custom application detected under folder {}. Continuing with default Bonita startup.",
                applicationInstallFolder);
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
}
