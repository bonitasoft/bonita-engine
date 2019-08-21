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
package org.bonitasoft.engine.api.impl.application.deployer.detector;

import java.io.File;

import org.bonitasoft.engine.api.impl.application.deployer.detector.artifact.DefaultDetectedArtifact;
import org.bonitasoft.engine.api.impl.application.deployer.detector.artifact.DetectedApplication;
import org.bonitasoft.engine.api.impl.application.deployer.detector.artifact.DetectedLayout;
import org.bonitasoft.engine.api.impl.application.deployer.detector.artifact.DetectedPage;
import org.bonitasoft.engine.api.impl.application.deployer.detector.artifact.DetectedProcess;
import org.bonitasoft.engine.api.impl.application.deployer.detector.artifact.DetectedRestAPIExtension;
import org.bonitasoft.engine.api.impl.application.deployer.detector.artifact.DetectedTheme;

public class ArtifactTypeDetector {

    private static final String APPLICATION_NAMESPACE = "http://documentation.bonitasoft.com/application-xml-schema/1.0";
    private static final String REST_API_EXTENSION_CONTENT_TYPE = "apiExtension";

    private XmlDetector xmlDetector;
    private CustomPageDetector customPageDetector;
    private ProcessDetector processDetector;
    private ThemeDetector themeDetector;
    private PageAndFormDetector pageAndFormDetector;
    private LayoutDetector layoutDetector;

    public ArtifactTypeDetector(XmlDetector xmlDetector, CustomPageDetector customPageDetector, ProcessDetector processDetector,
                                ThemeDetector themeDetector, PageAndFormDetector pageAndFormDetector, LayoutDetector layoutDetector) {
        this.xmlDetector = xmlDetector;
        this.customPageDetector = customPageDetector;
        this.processDetector = processDetector;
        this.themeDetector = themeDetector;
        this.pageAndFormDetector = pageAndFormDetector;
        this.layoutDetector = layoutDetector;
    }

    public DefaultDetectedArtifact detect(File file) {
        String path = file.getAbsolutePath();
        if (isApplication(file)) {
            return new DetectedApplication(path);
        }
        if (isProcess(file)) {
            return new DetectedProcess(path);
        }
        if (isPage(file)) {
            return new DetectedPage(path);
        }
        if (isLayout(file)) {
            return new DetectedLayout(path);
        }
        if (isTheme(file)) {
            return new DetectedTheme(path);
        }
        if (isRestApiExtension(file)) {
            return new DetectedRestAPIExtension(path);
        }
        return new DefaultDetectedArtifact(path);
    }

    public boolean isApplication(File file) {
        return xmlDetector.isCompliant(file, APPLICATION_NAMESPACE);
    }

    public boolean isRestApiExtension(File file) {
        return customPageDetector.isCompliant(file, REST_API_EXTENSION_CONTENT_TYPE);
    }

    public boolean isPage(File file) {
        return pageAndFormDetector.isCompliant(file);
    }

    public boolean isLayout(File file) {
        return layoutDetector.isCompliant(file);
    }

    public boolean isTheme(File file) {
        return themeDetector.isCompliant(file);
    }

    public boolean isProcess(File file) {
        return processDetector.isCompliant(file);
    }

}
