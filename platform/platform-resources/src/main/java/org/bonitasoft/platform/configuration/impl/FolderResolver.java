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
package org.bonitasoft.platform.configuration.impl;

import java.io.File;
import java.nio.file.Path;

import org.bonitasoft.platform.configuration.model.FullBonitaConfiguration;

/**
 * utility class to map configuration files and licenses to pulling folder.
 * <ul>
 * <li>license files are pulled to licenseFolder</li>
 * <li>tenant files are pulled to configurationFolder/tenants/TENANT_ID/CONFIGURATION_TYPE</li>
 * <li>other files to configurationFolder/CONFIGURATION_TYPE</li>
 * </ul>
 *
 * @author Laurent Leseigneur
 */
public class FolderResolver {

    private final Path configurationFolder;
    private final Path licenseFolder;

    public FolderResolver(Path configurationFolder, Path licenseFolder) {

        this.configurationFolder = configurationFolder;
        this.licenseFolder = licenseFolder;
    }

    public File getFolder(FullBonitaConfiguration fullBonitaConfiguration) {
        File confFolder = resolveFolder(fullBonitaConfiguration).toFile();
        confFolder.mkdirs();
        return confFolder;

    }

    private Path resolveSubFolder(Path rootPath, FullBonitaConfiguration fullBonitaConfiguration) {
        if (fullBonitaConfiguration.isLicenseFile()) {
            return rootPath;
        }
        if (fullBonitaConfiguration.isTenantFile()) {
            return rootPath.resolve("tenants").resolve(fullBonitaConfiguration.getTenantId().toString())
                    .resolve(fullBonitaConfiguration.getConfigurationType().toLowerCase());
        } else {
            return rootPath.resolve(fullBonitaConfiguration.getConfigurationType().toLowerCase());
        }
    }

    private Path resolveFolder(FullBonitaConfiguration fullBonitaConfiguration) {
        if (fullBonitaConfiguration.isLicenseFile()) {
            return resolveSubFolder(licenseFolder, fullBonitaConfiguration);
        }
        return resolveSubFolder(configurationFolder, fullBonitaConfiguration);
    }
}
