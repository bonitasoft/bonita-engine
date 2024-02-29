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
package org.bonitasoft.engine.api.impl.application.installer.detector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.impl.application.installer.ApplicationArchive;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnSingleCandidate(ArtifactTypeDetector.class)
@Slf4j
public class ArtifactTypeDetector {

    private static final String VERSION_PROPERTY = "version";
    private static final String APPLICATION_PROPERTIES_FILENAME = "application.properties";

    private static final String REST_API_EXTENSION_CONTENT_TYPE = "apiExtension";

    private final BdmDetector bdmDetector;

    private final LivingApplicationDetector livingApplicationDetector;
    private final OrganizationDetector organizationDetector;
    private final CustomPageDetector customPageDetector;
    private final ProcessDetector processDetector;
    private final ThemeDetector themeDetector;
    private final PageAndFormDetector pageAndFormDetector;
    private final LayoutDetector layoutDetector;
    private final IconDetector iconDetector;

    public ArtifactTypeDetector(BdmDetector bdmDetector, LivingApplicationDetector livingApplicationDetector,
            OrganizationDetector organizationDetector, CustomPageDetector customPageDetector,
            ProcessDetector processDetector, ThemeDetector themeDetector, PageAndFormDetector pageAndFormDetector,
            LayoutDetector layoutDetector, IconDetector iconDetector) {
        this.bdmDetector = bdmDetector;
        this.livingApplicationDetector = livingApplicationDetector;
        this.organizationDetector = organizationDetector;
        this.customPageDetector = customPageDetector;
        this.processDetector = processDetector;
        this.themeDetector = themeDetector;
        this.pageAndFormDetector = pageAndFormDetector;
        this.layoutDetector = layoutDetector;
        this.iconDetector = iconDetector;
    }

    public boolean isApplication(File file) throws IOException {
        return livingApplicationDetector.isCompliant(file);
    }

    public boolean isApplicationIcon(File file) throws IOException {
        return iconDetector.isCompliant(file);
    }

    public boolean isOrganization(File file) throws IOException {
        return organizationDetector.isCompliant(file);
    }

    public boolean isRestApiExtension(File file) throws IOException {
        return customPageDetector.isCompliant(file, REST_API_EXTENSION_CONTENT_TYPE);
    }

    public boolean isPage(File file) throws IOException {
        return pageAndFormDetector.isCompliant(file);
    }

    public boolean isLayout(File file) throws IOException {
        return layoutDetector.isCompliant(file);
    }

    public boolean isTheme(File file) throws IOException {
        return themeDetector.isCompliant(file);
    }

    public boolean isBdm(File file) {
        return bdmDetector.isCompliant(file);
    }

    public boolean isProcess(File file) throws IOException {
        return processDetector.isCompliant(file);
    }

    private boolean isApplicationProperties(File file) {
        return file.isFile() && Objects.equals(APPLICATION_PROPERTIES_FILENAME, file.getName());
    }

    public void checkFileAndAddToArchive(File file, ApplicationArchive applicationArchive) throws IOException {
        if (isApplication(file)) {
            log.debug("Found application file: '{}'. ", file.getName());
            applicationArchive.addApplication(file);
        } else if (isApplicationIcon(file)) {
            log.debug("Found icon file: '{}'. ", file.getName());
            applicationArchive.addApplicationIcon(file);
        } else if (isProcess(file)) {
            log.debug("Found process file: '{}'. ", file.getName());
            applicationArchive.addProcess(file);
        } else if (isOrganization(file)) {
            log.debug("Found organization file: '{}'. ", file.getName());
            if (applicationArchive.getOrganization() != null) {
                log.warn("An organization file has already been set. Using {}",
                        applicationArchive.getOrganization());
                ignoreFile(file, applicationArchive);
            } else {
                applicationArchive.setOrganization(file);
            }
        } else if (isPage(file)) {
            log.debug("Found page file: '{}'. ", file.getName());
            applicationArchive.addPage(file);
        } else if (isLayout(file)) {
            log.debug("Found layout file: '{}'. ", file.getName());
            applicationArchive.addLayout(file);
        } else if (isTheme(file)) {
            log.debug("Found theme file: '{}'. ", file.getName());
            applicationArchive.addTheme(file);
        } else if (isRestApiExtension(file)) {
            log.debug("Found rest api extension file: '{}'. ", file.getName());
            applicationArchive.addRestAPIExtension(file);
        } else if (isBdm(file)) {
            log.debug("Found business data model file: '{}'. ", file.getName());
            applicationArchive.setBdm(file);
        } else if (isApplicationProperties(file)) {
            applicationArchive.setVersion(readVersion(file));
        } else {
            ignoreFile(file, applicationArchive);
        }
    }

    String readVersion(File applicationPropertiesFile) throws IOException {
        try (var is = Files.newInputStream(applicationPropertiesFile.toPath())) {
            var properties = new Properties();
            properties.load(is);
            return properties.getProperty(VERSION_PROPERTY, null);
        }
    }

    private void ignoreFile(File file, ApplicationArchive applicationArchive) {
        log.debug("Ignoring file '{}'.", file.getName());
        applicationArchive.addIgnoredFile(file);
    }

}
