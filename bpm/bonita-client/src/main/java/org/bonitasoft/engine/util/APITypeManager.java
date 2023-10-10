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
package org.bonitasoft.engine.util;

import static org.bonitasoft.engine.api.ApiAccessType.HTTP;
import static org.bonitasoft.engine.api.ApiAccessType.LOCAL;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.io.PropertiesManager;

/**
 * Specify how the client communicate with the engine. There are three ways of doing it:
 * <ul>
 * <li>Using Java System Properties</li>
 * <li>Programmatically</li>
 * <li>DEPRECATED: using a file inside bonita-home. See <a
 * href="https://documentation.bonitasoft.com/bonita/latest/configure-client-of-bonita-bpm-engine#_configure_client_using_bonita_home_client">online
 * documentation</a>.</li>
 * </ul>
 * <h1>Using Java System Properties</h1>
 * <p>
 * Use System property <code>-Dorg.bonitasoft.engine.api-type=</code>
 * <ul>
 * <li>LOCAL:
 * <p>connect to the server in the local JVM (default). No other configuration is necessary.</p>
 * </li>
 * <li>HTTP
 * <p>
 * connect to the server using HTTP. You must also specify:
 * <ul>
 * <li><code>-Dorg.bonitasoft.engine.api-type.server.url=HTTP_SERVER_URL</code>, e.g.
 * <code>http://localhost:8080</code></li>
 * <li><code>-Dorg.bonitasoft.engine.api-type.application.name=WEBAPP_NAME</code>, this is the name of the web
 * application, e.g. <code>bonita</code></li>
 * </ul>
 * Optionally you can specify the maximum number of connections (JVM-wide) using
 * <code>-Dorg.bonitasoft.engine.api-type.connections.max=CONNECTIONS_MAX</code>
 * </p>
 * </li>
 * <li>TCP
 * <p>
 * not recommended, only for testing purpose.
 * </p></li>
 * </ul>
 * </p>
 * <h1>Programmatically</h1>
 * <ul>
 * <li>LOCAL access:
 * <p>APITypeManager.setAPITypeAndParams(ApiAccessType.LOCAL, null);</p>
 * </li>
 * <li>HTTP access:
 *
 * <pre>
 * <code>HashMap<String, String> parameters = new HashMap<>();
 * parameters.put("server.url", "http://myserver.com:8080");
 * parameters.put("application.name", "bonita-application");
 * parameters.put("connections.max", "5");
 * APITypeManager.setAPITypeAndParams(ApiAccessType.HTTP, parameters);</code>
 * </pre>
 *
 * </li>
 * </ul>
 *
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @see <a
 *      href="https://documentation.bonitasoft.com/bonita/latest/configure-client-of-bonita-bpm-engine#client_config">Online
 *      documentation on Client configuration</a>
 */
public class APITypeManager {

    private static final Logger LOGGER = Logger.getLogger(APITypeManager.class.getName());

    private static final String API_TYPE = "org.bonitasoft.engine.api-type";

    private static ApiAccessType apiAccessType = null;

    private static Map<String, String> apiTypeParameters = null;

    public static ApiAccessType getAPIType() throws ServerAPIException, UnknownAPITypeException, IOException {
        if (apiAccessType == null) {
            final String apiType = getAPITypeFromProperties();
            if (LOCAL.name().equalsIgnoreCase(apiType)) {
                apiAccessType = LOCAL;
            } else if (HTTP.name().equalsIgnoreCase(apiType)) {
                apiAccessType = HTTP;
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
        warnIfUsingRemoteConnectionWithLocalEngine(type);
        apiAccessType = type;
        apiTypeParameters = new HashMap<>();
        if (parameters != null) {
            apiTypeParameters.putAll(parameters);
        }
    }

    private static void warnIfUsingRemoteConnectionWithLocalEngine(ApiAccessType type) {
        if (type != null && type != LOCAL) {
            try {
                Thread.currentThread().getContextClassLoader()
                        .loadClass("org.bonitasoft.engine.api.impl.ProcessAPIImpl");
                LOGGER.warning(
                        "You are declaring an API access to Bonita Engine as a remote connection, whereas it looks like you are running in the same JVM. You should use LOCAL connection, using constant 'ApiAccessType.LOCAL'");
            } catch (ClassNotFoundException ignored) {
                //no warning
            }
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
        addParameter(properties, "org.bonitasoft.engine.api-type.", "connections.max");
        addParameter(properties, "org.bonitasoft.engine.api-type.", "basicAuthentication.active");
        addParameter(properties, "org.bonitasoft.engine.api-type.", "basicAuthentication.username");
        addParameter(properties, "org.bonitasoft.engine.api-type.", "basicAuthentication.password");
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

    private static void addPropertiesFrom(File clientFolder, Properties result, String... paths) throws IOException {
        File folder = Paths.get(clientFolder.getPath(), paths).toFile();
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
