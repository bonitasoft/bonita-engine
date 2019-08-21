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
package org.bonitasoft.engine.api.impl.application.deployer.validator;

import static java.lang.String.format;

import java.io.File;
import java.io.FileNotFoundException;

import org.bonitasoft.engine.api.impl.application.deployer.ApplicationArchive;
import org.bonitasoft.engine.api.impl.application.deployer.descriptor.DeploymentDescriptor;
import org.bonitasoft.engine.api.impl.application.deployer.detector.ArtifactTypeDetector;
import org.bonitasoft.engine.api.impl.application.deployer.model.Application;
import org.bonitasoft.engine.api.impl.application.deployer.model.Layout;
import org.bonitasoft.engine.api.impl.application.deployer.model.Page;
import org.bonitasoft.engine.api.impl.application.deployer.model.Process;
import org.bonitasoft.engine.api.impl.application.deployer.model.RestAPIExtension;
import org.bonitasoft.engine.api.impl.application.deployer.model.Theme;
import org.bonitasoft.engine.api.utils.VisibleForTesting;


public class ArtifactValidator {

    private ArtifactTypeDetector artifactTypeDetector;

    protected ArtifactValidator(ArtifactTypeDetector artifactTypeDetector) {
        this.artifactTypeDetector = artifactTypeDetector;
    }

    @VisibleForTesting
    ValidationStatus computeValidationStatus(ApplicationArchive applicationArchive) {
        ValidationStatus globalStatus = ValidationStatus.ok();
        DeploymentDescriptor deploymentDescriptor = applicationArchive.getDeploymentDescriptor();

        for (Application application : deploymentDescriptor.getApplications()) {
            globalStatus.addChild(computeValidationStatus(applicationArchive, application));
        }

        for (Page page : deploymentDescriptor.getPages()) {
            globalStatus.addChild(computeValidationStatus(applicationArchive, page));
        }

        for (Layout layout : deploymentDescriptor.getLayouts()) {
            globalStatus.addChild(computeValidationStatus(applicationArchive, layout));
        }

        for (Theme theme : deploymentDescriptor.getThemes()) {
            globalStatus.addChild(computeValidationStatus(applicationArchive, theme));
        }

        for (RestAPIExtension restAPIExtension : deploymentDescriptor.getRestAPIExtensions()) {
            globalStatus.addChild(computeValidationStatus(applicationArchive, restAPIExtension));
        }

        for (Process process : deploymentDescriptor.getProcesses()) {
            globalStatus.addChild(computeValidationStatus(applicationArchive, process));
        }

        return globalStatus;
    }

    public void validate(ApplicationArchive applicationArchive) throws InvalidArtifactException {
        ValidationStatus globalStatus = computeValidationStatus(applicationArchive);
        if (!globalStatus.isOK()) {
            throw new InvalidArtifactException("Some artifacts in the Application Archive are not valid",
                    globalStatus);
        }
    }

    public void validateApplicationType(File file) throws InvalidArtifactException {
        ValidationStatus status = computeApplicationValidationStatus(file);
        if (!status.isOK()) {
            throw new InvalidArtifactException(status.getMessage());
        }
    }

    private ValidationStatus computeValidationStatus(ApplicationArchive applicationArchive, Application application) {
        try {
            return computeApplicationValidationStatus(applicationArchive.getFile(application));
        } catch (FileNotFoundException e) {
            return errorFileNotFound(application.getFile(), ArtifactTypes.APPLICATION);
        }
    }

    @VisibleForTesting
    ValidationStatus computeApplicationValidationStatus(File file) {
        return artifactTypeDetector.isApplication(file)
                ? ValidationStatus.ok()
                : error(file.getName(), ArtifactTypes.APPLICATION);
    }

    public void validatePageType(File file) throws InvalidArtifactException {
        ValidationStatus status = computePageValidationStatus(file);
        if (!status.isOK()) {
            throw new InvalidArtifactException(status.getMessage());
        }
    }

    private ValidationStatus computeValidationStatus(ApplicationArchive applicationArchive, Page page) {
        try {
            return computePageValidationStatus(applicationArchive.getFile(page));
        } catch (FileNotFoundException e) {
            return errorFileNotFound(page.getFile(), ArtifactTypes.PAGE);
        }
    }

    @VisibleForTesting
    ValidationStatus computePageValidationStatus(File file) {
        return artifactTypeDetector.isPage(file)
                ? ValidationStatus.ok()
                : error(file.getName(), ArtifactTypes.PAGE);
    }

    public void validateLayoutType(File file) throws InvalidArtifactException {
        ValidationStatus status = computeLayoutValidationStatus(file);
        if (!status.isOK()) {
            throw new InvalidArtifactException(status.getMessage());
        }
    }

    private ValidationStatus computeValidationStatus(ApplicationArchive applicationArchive, Layout layout) {
        try {
            return computeLayoutValidationStatus(applicationArchive.getFile(layout));
        } catch (FileNotFoundException e) {
            return errorFileNotFound(layout.getFile(), ArtifactTypes.LAYOUT);
        }
    }

    @VisibleForTesting
    ValidationStatus computeLayoutValidationStatus(File file) {
        return artifactTypeDetector.isLayout(file)
                ? ValidationStatus.ok()
                : error(file.getName(), ArtifactTypes.LAYOUT);
    }

    public void validateThemeType(File file) throws InvalidArtifactException {
        ValidationStatus status = computeThemeValidationStatus(file);
        if (!status.isOK()) {
            throw new InvalidArtifactException(status.getMessage());
        }
    }

    private ValidationStatus computeValidationStatus(ApplicationArchive applicationArchive, Theme theme) {
        try {
            return computeThemeValidationStatus(applicationArchive.getFile(theme));
        } catch (FileNotFoundException e) {
            return errorFileNotFound(theme.getFile(), ArtifactTypes.THEME);
        }
    }

    @VisibleForTesting
    ValidationStatus computeThemeValidationStatus(File file) {
        return artifactTypeDetector.isTheme(file)
                ? ValidationStatus.ok()
                : error(file.getName(), ArtifactTypes.THEME);
    }

    public void validateRestApiExtensionType(File file) throws InvalidArtifactException {
        ValidationStatus status = computeRestApiExtensionValidationStatus(file);
        if (!status.isOK()) {
            throw new InvalidArtifactException(status.getMessage());
        }
    }

    private ValidationStatus computeValidationStatus(ApplicationArchive applicationArchive,
            RestAPIExtension restAPIExtension) {
        try {
            return computeRestApiExtensionValidationStatus(applicationArchive.getFile(restAPIExtension));
        } catch (FileNotFoundException e) {
            return errorFileNotFound(restAPIExtension.getFile(), ArtifactTypes.REST_API_EXTENSION);
        }
    }

    @VisibleForTesting
    ValidationStatus computeRestApiExtensionValidationStatus(File file) {
        return artifactTypeDetector.isRestApiExtension(file)
                ? ValidationStatus.ok()
                : error(file.getName(), ArtifactTypes.REST_API_EXTENSION);
    }

    private ValidationStatus computeValidationStatus(ApplicationArchive applicationArchive, Process process) {
        try {
            return computeProcessValidationStatus(applicationArchive.getFile(process));
        } catch (FileNotFoundException e) {
            return errorFileNotFound(process.getFile(), ArtifactTypes.PROCESS);
        }
    }

    @VisibleForTesting
    ValidationStatus computeProcessValidationStatus(File file) {
        return artifactTypeDetector.isProcess(file)
                ? ValidationStatus.ok()
                : error(file.getName(), ArtifactTypes.PROCESS);
    }

    private static ValidationStatus error(String filename, ArtifactTypes type) {
        return ValidationStatus.error(format("The artifact '%s' is not an artifact of type '%s'.", filename, type));
    }

    private static ValidationStatus errorFileNotFound(String filename, ArtifactTypes type) {
        return ValidationStatus
                .error(format("The file '%s' for artifact of type '%s' does not exist.", filename, type));
    }

}
