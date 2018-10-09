/**
 * Copyright (C) 2016-2018 Bonitasoft S.A.
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

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.naming.NamingException;

import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.platform.configuration.ConfigurationService;
import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.bonitasoft.platform.exception.PlatformException;
import org.bonitasoft.platform.setup.PlatformSetupAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Laurent Leseigneur
 */
public class PlatformLicensesSetup {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformLicensesSetup.class);
    public static final String BONITA_CLIENT_HOME = "bonita.client.home";

    private boolean initialized;

    PlatformLicensesSetup() {
        // enforce retrieving instance using the Factory class
    }

    public void setupLicenses() {
        if (initialized) {
            LOGGER.debug("Platform licenses already setup, so skip processing");
            return;
        }
        try {
            extractLicenses();
            initialized = true;
        } catch (NamingException | PlatformException | IOException e) {
            throw new IllegalStateException("Unable to get license files from database", e);
        }
    }

    public void forceSetupLicenses() {
        LOGGER.debug("Force licenses extraction");
        this.initialized = false;
        setupLicenses();
    }

    private void extractLicenses() throws IOException, NamingException, PlatformException {
        LOGGER.debug("Start extracting licenses...");
        Path licensesFolder = getLicensesFolder().toPath();
        LOGGER.debug("License folder: {}", licensesFolder);
        if (LOGGER.isDebugEnabled()) {
            File[] files = ofNullable(licensesFolder.toFile().listFiles()).orElse(new File[0]);
            LOGGER.debug("It contains {} files/folders", files.length);
            stream(files).forEach(file -> LOGGER.debug(file.getName()));
        }
        LOGGER.debug("Retrieving licenses from configuration....");
        final List<BonitaConfiguration> licenses = getConfigurationService().getLicenses();
        if (licenses.isEmpty()) {
            return;
        }
        writeLicenses(licensesFolder, licenses);
        setBonitaClientHomeIfNotSet(licensesFolder);
    }

    private void setBonitaClientHomeIfNotSet(Path licensesFolder) {
        String property = System.getProperty(BONITA_CLIENT_HOME);
        if (property != null && !property.equals(licensesFolder.toString())) {
            LOGGER.warn("Licenses are taken from folder " + property + " instead of database. This happens when you are in a Studio environment.");
        } else {
            System.setProperty(BONITA_CLIENT_HOME, licensesFolder.toString());
        }
    }

    private void writeLicenses(Path licensesFolder, List<BonitaConfiguration> licenses) throws IOException {
        if (!Files.exists(licensesFolder)) {
            Files.createDirectory(licensesFolder);
        }
        for (BonitaConfiguration license : licenses) {
            File licenseFile = licensesFolder.resolve(license.getResourceName()).toFile();
            IOUtil.write(licenseFile, license.getResourceContent());
        }
    }

    private File getLicensesFolder() throws IOException {
        return BonitaHomeServer.getInstance().getLicensesFolder();
    }

    // visible for testing
    ConfigurationService getConfigurationService() throws NamingException {
        return PlatformSetupAccessor.getConfigurationService();
    }

}
