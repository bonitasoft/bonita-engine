/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

import org.bonitasoft.platform.exception.PlatformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains the logic to determine if we are in the context of an application server that we must configure: Tomcat or Wildfly.
 *
 * @author Emmanuel Duchastenier
 */
class BundleResolver {

    protected final static Logger LOGGER = LoggerFactory.getLogger(BundleConfigurator.class);
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
            LOGGER.debug("File " + filePath.toString() + " does not exist.");
        }
        return exists;
    }

    protected Path getPath(String partialPath) throws PlatformException {
        final String[] paths = partialPath.split("/");
        Path build = rootPath;
        for (String path : paths) {
            build = build.resolve(path);
        }
        return build;
    }

    private boolean isTomcatEnvironment() throws PlatformException {
        return fileExists(getPath(APPSERVER_FOLDERNAME + "/bin/catalina.sh")) || fileExists(getPath(APPSERVER_FOLDERNAME + "/bin/catalina.bat"));
    }

    private boolean isWildflyEnvironment() throws PlatformException {
        return fileExists(getPath(APPSERVER_FOLDERNAME + "/bin/standalone.conf")) || fileExists(getPath(APPSERVER_FOLDERNAME + "/bin/standalone.conf.bat"));
    }

    BundleConfigurator getConfigurator() throws PlatformException {
        if (isTomcatEnvironment()) {
            return new TomcatBundleConfigurator(rootPath);
        } else if (isWildflyEnvironment()) {
            return new WildflyBundleConfigurator(rootPath);
        } else {
            LOGGER.info(
                    "No Application Server detected. You may need to manually configure the access to the database. Supported App Servers are: Tomcat 8, Wildfly 10");
            return null;
        }
    }

}
