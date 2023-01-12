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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.event.PlatformStartedEvent;
import org.bonitasoft.engine.exception.ApplicationInstallationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * Spring listener of the event {@link PlatformStartedEvent}.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CustomOrDefaultApplicationInstaller {

    private static final String CUSTOM_APPLICATION_DEFAULT_FOLDER = "my-application";

    @Value("${bonita.runtime.custom-application.install-folder:" + CUSTOM_APPLICATION_DEFAULT_FOLDER + "}")
    @Getter
    private String applicationInstallFolder;
    private final ApplicationInstaller applicationInstaller;

    private final ResourcePatternResolver cpResourceResolver = new PathMatchingResourcePatternResolver(
            CustomOrDefaultApplicationInstaller.class.getClassLoader());

    @EventListener
    public void autoDeployDetectedCustomApplication(PlatformStartedEvent event)
            throws ApplicationInstallationException, IOException {
        log.info("Trying to detect custom application (.zip file from folder {})", applicationInstallFolder);

        Resource[] resources = cpResourceResolver
                .getResources(
                        ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/" + applicationInstallFolder + "/*.zip");

        ensureNoMoreThanOneApplicationDetected(resources);

        boolean foundAndDeployedOneApplication = false;
        for (Resource resource : resources) {
            if (resource.exists() && resource.isReadable() && resource.contentLength() > 0) {
                String resourceName = resource.getFilename();
                try (InputStream applicationZipFileStream = resource.getInputStream()) {
                    log.info(
                            "Custom application detected with name '{}' at the root of the classpath." +
                                    " Bonita now tries to install it automatically...",
                            resourceName);
                    applicationInstaller.install(applicationZipFileStream);
                    foundAndDeployedOneApplication = true;
                    break;
                } catch (IOException e) {
                    throw new ApplicationInstallationException("Unable to install the page " + resourceName, e);
                }
            } else {
                log.warn("Zip file '{}' was detected in /{} but could not be read.", resource.getDescription(),
                        applicationInstallFolder);
            }
        }

        if (!foundAndDeployedOneApplication) {
            log.info(
                    "No custom application detected at the root of the classpath. Continuing with default Bonita startup.");
        }
    }

    private static void ensureNoMoreThanOneApplicationDetected(Resource[] resources)
            throws IOException, ApplicationInstallationException {
        var nbZipApplication = 0;
        for (Resource resource : resources) {
            if (resource.exists() && resource.isReadable() && resource.contentLength() > 0) {
                nbZipApplication++;
            }
        }
        if (nbZipApplication > 1) {
            throw new ApplicationInstallationException("More than one application detected. Abandoning.");
        }
    }

}
