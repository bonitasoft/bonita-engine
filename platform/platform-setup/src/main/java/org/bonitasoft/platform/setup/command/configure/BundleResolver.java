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
package org.bonitasoft.platform.setup.command.configure;

import static org.bonitasoft.platform.setup.PlatformSetup.BONITA_SETUP_FOLDER;
import static org.bonitasoft.platform.setup.command.configure.BundleConfigurator.APPSERVER_FOLDERNAME;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.platform.exception.PlatformException;

/**
 * This class contains the logic to determine if we are in the context of an application server that we must configure.
 * Only Tomcat is supported.
 */
@Slf4j
class BundleResolver {

    private final Path rootPath;

    BundleResolver() {
        final String setupFolder = System.getProperty(BONITA_SETUP_FOLDER);
        if (setupFolder != null) {
            rootPath = Paths.get(setupFolder).getParent();
        } else {
            rootPath = Paths.get("..");
        }
    }

    private boolean fileExists(Path filePath) {
        final boolean exists = Files.exists(filePath);
        if (!exists) {
            log.debug("File {} does not exist.", filePath);
        }
        return exists;
    }

    private Path getPath(String partialPath) {
        final String[] paths = partialPath.split("/");
        Path build = rootPath;
        for (String path : paths) {
            build = build.resolve(path);
        }
        return build;
    }

    private boolean isTomcatEnvironment() {
        return fileExists(getPath(APPSERVER_FOLDERNAME + "/bin/catalina.sh"))
                || fileExists(getPath(APPSERVER_FOLDERNAME + "/bin/catalina.bat"));
    }

    BundleConfigurator getConfigurator() throws PlatformException {
        if (isTomcatEnvironment()) {
            return new TomcatBundleConfigurator(rootPath);
        } else {
            log.info("No Application Server detected. You may need to manually configure the access to the database. " +
                    "Only Tomcat 9.0.x is supported");
            return null;
        }
    }

}
