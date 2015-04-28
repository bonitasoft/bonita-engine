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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.home.BonitaHomeClient;
import org.bonitasoft.engine.io.PropertiesManager;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class APITypeManager {

    private static final String API_TYPE = "org.bonitasoft.engine.api-type";

    private static ApiAccessType apiAccessType = null;

    private static Map<String, String> apiTypeParameters = null;

    public static ApiAccessType getAPIType() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException, IOException {
        if (apiAccessType == null) {
            final String apiType = getProperty(API_TYPE);
            if (ApiAccessType.LOCAL.name().equalsIgnoreCase(apiType)) {
                apiAccessType = ApiAccessType.LOCAL;
            } else if (ApiAccessType.EJB3.name().equalsIgnoreCase(apiType)) {
                apiAccessType = ApiAccessType.EJB3;
            } else if (apiType.equalsIgnoreCase(ApiAccessType.HTTP.name())) {
                apiAccessType = ApiAccessType.HTTP;
            } else if (apiType.equalsIgnoreCase(ApiAccessType.TCP.name())) {
                apiAccessType = ApiAccessType.TCP;
            } else {
                throw new UnknownAPITypeException("Invalid API type: " + apiType);
            }
        }
        return apiAccessType;
    }

    public static void setAPITypeAndParams(final ApiAccessType type, final Map<String, String> parameters) {
        apiAccessType = type;

        if (!ApiAccessType.LOCAL.equals(type)) {
            apiTypeParameters = new HashMap<String, String>();
            apiTypeParameters.putAll(parameters);
        }
    }

    private static String getProperty(final String propertyName) throws BonitaHomeNotSetException, ServerAPIException, IOException {
        return BonitaHomeClient.getInstance().getProperty(propertyName);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Map<String, String> getAPITypeParameters() throws BonitaHomeNotSetException, ServerAPIException, IOException {
        if (apiTypeParameters == null) {
            final Properties properties = BonitaHomeClient.getInstance().getProperties();
            apiTypeParameters = new HashMap<String, String>((Map) properties);
            apiTypeParameters.remove(API_TYPE);
        }
        return apiTypeParameters;
    }

    public static void refresh() {
        apiAccessType = null;
        apiTypeParameters = null;
        BonitaHomeClient.getInstance().refreshBonitaHome();
    }

}
