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
package org.bonitasoft.engine.api.impl.projectdeployer.detector;

import java.io.File;

import org.bonitasoft.engine.api.impl.projectdeployer.detector.artifact.DefaultDetectedArtifact;
import org.bonitasoft.engine.api.impl.projectdeployer.detector.artifact.DetectedApplication;
import org.bonitasoft.engine.api.impl.projectdeployer.detector.artifact.DetectedBdm;
import org.bonitasoft.engine.api.impl.projectdeployer.detector.artifact.DetectedBdmAccessControl;
import org.bonitasoft.engine.api.impl.projectdeployer.detector.artifact.DetectedLayout;
import org.bonitasoft.engine.api.impl.projectdeployer.detector.artifact.DetectedOrganization;
import org.bonitasoft.engine.api.impl.projectdeployer.detector.artifact.DetectedPage;
import org.bonitasoft.engine.api.impl.projectdeployer.detector.artifact.DetectedProcess;
import org.bonitasoft.engine.api.impl.projectdeployer.detector.artifact.DetectedProfile;
import org.bonitasoft.engine.api.impl.projectdeployer.detector.artifact.DetectedRestAPIExtension;
import org.bonitasoft.engine.api.impl.projectdeployer.detector.artifact.DetectedTheme;

public class ArtifactTypeDetector {

    private static final String ORGANIZATION_NAMESPACE = "http://documentation.bonitasoft.com/organization-xml-schema";
    private static final String APPLICATION_NAMESPACE = "http://documentation.bonitasoft.com/application-xml-schema/1.0";
    private static final String PROFILE_NAMESPACE = "http://www.bonitasoft.org/ns/profile/6.1";
    private static final String ACCESS_CONTROL_NAMESPACE = "http://documentation.bonitasoft.com/bdm-access-control-xml-schema/1.0";
    private static final String REST_API_EXTENSION_CONTENT_TYPE = "apiExtension";

    private XmlDetector xmlDetector;
    private CustomPageDetector customPageDetector;
    private ProcessDetector processDetector;
    private BdmDetector bdmDetector;
    private ThemeDetector themeDetector;
    private PageAndFormDetector pageAndFormDetector;
    private LayoutDetector layoutDetector;

    public ArtifactTypeDetector(XmlDetector xmlDetector, CustomPageDetector customPageDetector, ProcessDetector processDetector,
                                BdmDetector bdmDetector, ThemeDetector themeDetector, PageAndFormDetector pageAndFormDetector, LayoutDetector layoutDetector) {
        this.xmlDetector = xmlDetector;
        this.customPageDetector = customPageDetector;
        this.processDetector = processDetector;
        this.bdmDetector = bdmDetector;
        this.themeDetector = themeDetector;
        this.pageAndFormDetector = pageAndFormDetector;
        this.layoutDetector = layoutDetector;
    }

    public DefaultDetectedArtifact detect(File file) {
        String path = file.getAbsolutePath();
        if (isProfile(file)) {
            return new DetectedProfile(path);
        }
        if (isApplication(file)) {
            return new DetectedApplication(path);
        }
        if (isOrganization(file)) {
            return new DetectedOrganization(path);
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
        if (isBdm(file)) {
            return new DetectedBdm(path);
        }
        if (isAccessControl(file)) {
            return new DetectedBdmAccessControl(path);
        }
        return new DefaultDetectedArtifact(path);
    }

    public boolean isOrganization(File file) {
        return xmlDetector.isCompliant(file, ORGANIZATION_NAMESPACE);
    }

    public boolean isApplication(File file) {
        return xmlDetector.isCompliant(file, APPLICATION_NAMESPACE);
    }

    public boolean isProfile(File file) {
        return xmlDetector.isCompliant(file, PROFILE_NAMESPACE);
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

    public boolean isBdm(File file) {
        return bdmDetector.isCompliant(file);
    }

    public boolean isAccessControl(File file) {
        return xmlDetector.isCompliant(file, ACCESS_CONTROL_NAMESPACE);
    }

}
