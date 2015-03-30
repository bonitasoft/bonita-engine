/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.home;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.io.PropertiesManager;

/**
 * Utility class that handles the path to the client part of the bonita home
 * <p>
 * The client part of the bonita home contains connection information to the server and can also contains working directories for client application of the
 * bonita engine
 * </p>
 *
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @since 6.0.0
 */
public final class BonitaHomeClient extends BonitaHome {

    public static final String BONITA_HOME_CLIENT = "engine-client";

    private File clientPath;

    public static final BonitaHomeClient INSTANCE = new BonitaHomeClient();

    private BonitaHomeClient() {
        super();
    }

    public static BonitaHomeClient getInstance() {
        return INSTANCE;
    }

    /**
     * @return the path to the client part of the  bonita home
     * @throws BonitaHomeNotSetException
     *         when bonita.home system property is not set
     * @since 6.0.0
     */
    protected File getBonitaHomeClientFolder() throws BonitaHomeNotSetException {
        if (clientPath == null) {
            final StringBuilder path = new StringBuilder(getBonitaHomeFolderPath());
            path.append(File.separatorChar);
            path.append(BONITA_HOME_CLIENT);
            clientPath = new File(path.toString());
        }
        return clientPath;
    }

    @Override
    protected void refresh() {
        clientPath = null;
    }

    public static String getProperty(final String propertyName) throws BonitaHomeNotSetException, ServerAPIException, IOException {
        final Properties properties = getProperties();
        return properties.getProperty(propertyName);
    }

    public static Properties getProperties() throws BonitaHomeNotSetException, ServerAPIException, IOException {
        final File clientFolder = getInstance().getBonitaHomeClientFolder();
        final Properties defaultProperties = PropertiesManager.getProperties(FileUtils.getFile(clientFolder, "work", "bonita-client-community.properties"));
        final Properties customProperties = PropertiesManager.getProperties(FileUtils.getFile(clientFolder, "conf", "bonita-client-custom.properties"));
        final Properties result = new Properties();
        result.putAll(defaultProperties);
        result.putAll(customProperties);
        return result;
    }

}
