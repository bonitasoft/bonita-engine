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
package org.bonitasoft.engine.util;

import static org.bonitasoft.engine.api.ApiAccessType.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.io.PropertiesManager;

/**
 * Specify how the client communicate with the engine
 * <p>
 * Use system properties <code>org.bonitasoft.engine.api-type</code> to specify that:
 * <ul>
 * <li>LOCAL:
 * <p>connect to the server in the local JVM (default)</p>
 * </li>
 * <li>HTTP
 * <p>
 * connect to the server using HTTP, must specify
 * <ul>
 * <li><code>org.bonitasoft.engine.api-type.server.url</code>, e.g. http://localhost:8080</li>
 * <li><code>org.bonitasoft.engine.api-type.application.name</code>, this is the name of the web application, e.g. bonita</li>
 * </ul>
 * </p>
 * </li>
 * <li>EJB3
 * <p>
 * connect to the server using EJB3
 * </p>
 * </li>
 * <li>TCP
 * <p>
 * not recommended, only for testing purpose
 * </p></li>
 * </ul>
 * </p>
 *
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class APITypeManager {

    private static final String API_TYPE = "org.bonitasoft.engine.api-type";

    private static ApiAccessType apiAccessType = null;

    private static Map<String, String> apiTypeParameters = null;

    public static ApiAccessType getAPIType() throws ServerAPIException, UnknownAPITypeException, IOException {
        if (apiAccessType == null) {
            final String apiType = getProperties().getProperty(API_TYPE);
            if (LOCAL.name().equalsIgnoreCase(apiType)) {
                apiAccessType = LOCAL;
            } else if (EJB3.name().equalsIgnoreCase(apiType)) {
                apiAccessType = EJB3;
            } else if (apiType.equalsIgnoreCase(HTTP.name())) {
                apiAccessType = HTTP;
            } else if (apiType.equalsIgnoreCase(TCP.name())) {
                apiAccessType = TCP;
            } else {
                throw new UnknownAPITypeException("Invalid API type: " + apiType);
            }
        }
        return apiAccessType;
    }

    public static Map<String, String> getAPITypeParameters() throws ServerAPIException, IOException {
        if (apiTypeParameters == null) {
            final Properties properties = getProperties();
            apiTypeParameters = new HashMap<>(properties.size());
            for (String property : properties.stringPropertyNames()) {
                if (API_TYPE.equals(property)) {
                    continue;
                }
                apiTypeParameters.put(property, properties.getProperty(property));
            }
        }
        return apiTypeParameters;
    }

    public static void setAPITypeAndParams(final ApiAccessType type, final Map<String, String> parameters) {
        apiAccessType = type;
        apiTypeParameters = new HashMap<>();
        if (parameters != null) {
            apiTypeParameters.putAll(parameters);
        }
    }

    private static Properties getProperties() throws IOException {
        Properties properties = getPropertiesFromSystemProperties();
        if (!properties.isEmpty()) {
            return properties;
        }
        properties = getPropertiesFromBonitaHome();
        if (properties == null || properties.isEmpty()) {
            properties = new Properties();
            properties.setProperty(API_TYPE, LOCAL.name());
        }
        return properties;
    }

    private static Properties getPropertiesFromSystemProperties() {
        Properties properties = new Properties();
        String apiType = System.getProperty("org.bonitasoft.engine.api-type");
        if (apiType != null) {
            properties.setProperty("org.bonitasoft.engine.api-type", apiType);
        }
        addParameter(properties, "org.bonitasoft.engine.api-type.", "server.url");
        addParameter(properties, "org.bonitasoft.engine.api-type.", "application.name");
        addParameter(properties, "", "org.bonitasoft.engine.ejb.naming.reference");
        addParameter(properties, "", "java.naming.factory.url.pkgs");
        addParameter(properties, "", "java.naming.factory.initial");
        addParameter(properties, "", "java.naming.provider.url");
        return properties;
    }

    private static void addParameter(Properties properties, String parameterPrefix, String parameterName) {
        String parameter = System.getProperty(parameterPrefix + parameterName);
        if (parameter != null) {
            properties.setProperty(parameterName, parameter);
        }
    }

    /*
     * LEGACY MODE
     */
    private static Properties getPropertiesFromBonitaHome() throws IOException {
        String bonitaHomePath = System.getProperty("bonita.home");
        if (bonitaHomePath == null || bonitaHomePath.isEmpty()) {
            return null;
        }
        File clientFolder = new File(bonitaHomePath.trim(), "engine-client");
        final Properties result = new Properties();
        addPropertiesFrom(clientFolder, result, "work", "bonita-client-community.properties");
        addPropertiesFrom(clientFolder, result, "conf", "bonita-client-custom.properties");
        return result;
    }

    private static void addPropertiesFrom(File clientFolder, Properties result, String... strings) throws IOException {
        File folder = FileUtils.getFile(clientFolder, strings);
        if (folder.exists()) {
            final Properties defaultProperties = PropertiesManager.getProperties(folder);
            result.putAll(defaultProperties);
        }
    }

    public static void refresh() {
        apiAccessType = null;
        apiTypeParameters = null;
    }

}
