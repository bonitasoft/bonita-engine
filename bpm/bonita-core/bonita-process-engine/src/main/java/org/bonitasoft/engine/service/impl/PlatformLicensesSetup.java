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

package org.bonitasoft.engine.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.naming.NamingException;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.platform.configuration.ConfigurationService;
import org.bonitasoft.platform.configuration.exception.PlatformConfigurationException;
import org.bonitasoft.platform.configuration.impl.ConfigurationServiceImpl;
import org.bonitasoft.platform.configuration.model.BonitaConfiguration;

/**
 * @author Laurent Leseigneur
 */
public class PlatformLicensesSetup {

    public static final String BONITA_CLIENT_HOME = "bonita.client.home";

    void setupLicenses() {
        final String bonitaClientHomePath = System.getProperty(BONITA_CLIENT_HOME);
        if (shouldExtractLicensesToTempFolder(bonitaClientHomePath)) {
            try {
                extractLicenses();
            } catch (NamingException | PlatformConfigurationException | IOException e) {
                throw new IllegalStateException("unable to get license files from database", e);
            }
        }
    }

    void extractLicenses() throws IOException, NamingException, PlatformConfigurationException {
        final File licensesFolder = initLicensesFolder();
        ConfigurationService configurationService = getConfigurationService();

        final List<BonitaConfiguration> licenses = configurationService.getLicenses();
        if (licenses.isEmpty()) {
            //no licenses, the system property is not set
            return;
        }
        for (BonitaConfiguration license : licenses) {
            File licenseFile = new File(licensesFolder, license.getResourceName());
            IOUtil.write(licenseFile, license.getResourceContent());
        }
        System.setProperty(BONITA_CLIENT_HOME, licensesFolder.getAbsolutePath());
    }

    ConfigurationService getConfigurationService() throws NamingException {
        return new ConfigurationServiceImpl();
    }

    private File initLicensesFolder() throws IOException {
        final File licensesFolder = new File(IOUtil.TMP_DIRECTORY, "bonita_licenses");
        if (licensesFolder.exists()) {
            FileUtils.deleteDirectory(licensesFolder);
        }
        licensesFolder.mkdirs();
        return licensesFolder;
    }

    private boolean shouldExtractLicensesToTempFolder(String bonitaClientHomePath) {
        return bonitaClientHomePath == null || bonitaClientHomePath.trim().isEmpty();
    }
}
