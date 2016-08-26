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
 * connect to the server using EJB3, must specify<br> For Wildfly 9 :
 * <ul>
 * <li>java.naming.factory.url.pkgs, e.g. org.jboss.ejb.client.naming</li>
 * <li>org.bonitasoft.engine.ejb.naming.reference, e.g. ejb:bonita-ear/bonita-ejb/serverAPIBean!org.bonitasoft.engine.api.internal.ServerAPI</li>
 * </ul>
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
            final String apiType = getAPITypeFromProperties();
            if (LOCAL.name().equalsIgnoreCase(apiType)) {
                apiAccessType = LOCAL;
            } else if (EJB3.name().equalsIgnoreCase(apiType)) {
                apiAccessType = EJB3;
            } else if (HTTP.name().equalsIgnoreCase(apiType)) {
                apiAccessType = HTTP;
            } else if (TCP.name().equalsIgnoreCase(apiType)) {
                apiAccessType = TCP;
            } else {
                throw new UnknownAPITypeException("Invalid API type: " + apiType);
            }
        }
        return apiAccessType;
    }

    private static String getAPITypeFromProperties() throws IOException {
        String property = getProperties().get(API_TYPE);
        if (property != null) {
            return property;
        }
        return LOCAL.name();
    }

    public static Map<String, String> getAPITypeParameters() throws ServerAPIException, IOException {
        if (apiTypeParameters == null) {
            final Map<String, String> properties = getProperties();
            apiTypeParameters = new HashMap<>(properties.size());
            for (Map.Entry<String, String> property : properties.entrySet()) {
                if (API_TYPE.equals(property.getKey())) {
                    continue;
                }
                apiTypeParameters.put(property.getKey(), property.getValue());
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

    private static Map<String, String> getProperties() throws IOException {
        Map<String, String> properties = getPropertiesFromSystemProperties();
        Properties propertiesFromBonitaHome = getPropertiesFromBonitaHome();
        for (String property : propertiesFromBonitaHome.stringPropertyNames()) {
            if (!properties.containsKey(property)) {
                properties.put(property, propertiesFromBonitaHome.getProperty(property));
            }
        }
        return properties;
    }

    private static Map<String, String> getPropertiesFromSystemProperties() {
        Map<String, String> properties = new HashMap<>();
        String apiType = System.getProperty("org.bonitasoft.engine.api-type");
        if (apiType != null) {
            properties.put("org.bonitasoft.engine.api-type", apiType);
        }
        addParameter(properties, "org.bonitasoft.engine.api-type.", "server.url");
        addParameter(properties, "org.bonitasoft.engine.api-type.", "application.name");
        addParameter(properties, "", "org.bonitasoft.engine.ejb.naming.reference");
        addParameter(properties, "", "java.naming.factory.url.pkgs");
        return properties;
    }

    private static void addParameter(Map<String, String> properties, String parameterPrefix, String parameterName) {
        String parameter = System.getProperty(parameterPrefix + parameterName);
        if (parameter != null) {
            properties.put(parameterName, parameter);
        }
    }

    /*
     * LEGACY MODE
     */
    private static Properties getPropertiesFromBonitaHome() throws IOException {
        String bonitaHomePath = System.getProperty("bonita.home");
        if (bonitaHomePath == null || bonitaHomePath.isEmpty()) {
            return new Properties();
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
