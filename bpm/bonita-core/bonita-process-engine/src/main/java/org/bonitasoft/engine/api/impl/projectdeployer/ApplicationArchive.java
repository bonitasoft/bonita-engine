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
package org.bonitasoft.engine.api.impl.projectdeployer;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

import org.bonitasoft.engine.api.impl.projectdeployer.descriptor.DeploymentDescriptor;
import org.bonitasoft.engine.api.impl.projectdeployer.descriptor.DeploymentDescriptorGenerator;
import org.bonitasoft.engine.api.impl.projectdeployer.model.Application;
import org.bonitasoft.engine.api.impl.projectdeployer.model.BdmAccessControl;
import org.bonitasoft.engine.api.impl.projectdeployer.model.BusinessDataModel;
import org.bonitasoft.engine.api.impl.projectdeployer.model.Layout;
import org.bonitasoft.engine.api.impl.projectdeployer.model.Organization;
import org.bonitasoft.engine.api.impl.projectdeployer.model.Page;
import org.bonitasoft.engine.api.impl.projectdeployer.model.Process;
import org.bonitasoft.engine.api.impl.projectdeployer.model.Profile;
import org.bonitasoft.engine.api.impl.projectdeployer.model.RestAPIExtension;
import org.bonitasoft.engine.api.impl.projectdeployer.model.Theme;

/**
 * @author Baptiste Mesta.
 */
public class ApplicationArchive implements Closeable {

    private final Path applicationArchiveDir;
    private DeploymentDescriptor deploymentDescriptor;

    public ApplicationArchive() throws IOException {
        applicationArchiveDir = Files.createTempDirectory("applicationArchive");
    }

    @Override
    public void close() throws IOException {
        if (applicationArchiveDir.toFile().exists()) {
            Files.walk(applicationArchiveDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    public void addFile(String name, InputStream inputStream) throws IOException {
        Path target = applicationArchiveDir.resolve(name);
        if (!target.getParent().toFile().exists()) {
            target.getParent().toFile().mkdirs();
        }
        Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
    }

    public DeploymentDescriptor getDeploymentDescriptor() {
        return deploymentDescriptor;
    }

    public void setDeploymentDescriptor(DeploymentDescriptor deploymentDescriptor) {
        this.deploymentDescriptor = deploymentDescriptor;
    }

    public File getFile(String relativePath) throws FileNotFoundException {
        File file;
        if (relativePath.startsWith(applicationArchiveDir.toString())) {
            file = new File(relativePath);
        } else {
            file = applicationArchiveDir.resolve(relativePath).toFile();
        }
        if (!file.isFile()) {
            throw new FileNotFoundException(file.getPath());
        }
        return file;
    }

    public File getFile(Organization organization) throws FileNotFoundException {
        return getFile(organization.getFile());
    }

    public File getFile(Profile profile) throws FileNotFoundException {
        return getFile(profile.getFile());
    }

    public File getFile(Application application) throws FileNotFoundException {
        return getFile(application.getFile());
    }

    public File getFile(RestAPIExtension restAPIExtension) throws FileNotFoundException {
        return getFile(restAPIExtension.getFile());
    }

    public File getFile(Page page) throws FileNotFoundException {
        return getFile(page.getFile());
    }

    public File getFile(Process process) throws FileNotFoundException {
        return getFile(process.getFile());
    }

    public File getFile(BusinessDataModel bdm) throws FileNotFoundException {
        return getFile(bdm.getFile());
    }

    public File getFile(Layout layout) throws FileNotFoundException {
        return getFile(layout.getFile());
    }

    public File getFile(Theme theme) throws FileNotFoundException {
        return getFile(theme.getFile());
    }

    public File getFile(BdmAccessControl accessControl) throws FileNotFoundException {
        return getFile(accessControl.getFile());
    }

    public void generateDeploymentDescriptor() {
        deploymentDescriptor = DeploymentDescriptorGenerator.create().fromDirectory(applicationArchiveDir.toFile());
    }

    public boolean isEmpty() {
        return applicationArchiveDir.toFile().list().length == 0;
    }

}
