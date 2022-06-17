/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.engine.incident;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to set System Property before the logger initializes.
 * This class must be initialized before everything else in all test classes of this module !
 * Otherwise, the logger is initialized before the System Property is set, resulting in the logback conf not aware of
 * the System Property value.
 *
 * @author Emmanuel Duchastenier
 */
public class BeforeAllLoggerInitializer {

    static final String INCIDENT_LOG_PATH = "build/log/";

    static final String INCIDENT_LOG_PATH_PROP_NAME = "INCIDENT_LOG_PATH";

    static {
        System.setProperty(INCIDENT_LOG_PATH_PROP_NAME, INCIDENT_LOG_PATH);
        LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    }

    public static void initialize() {
    }
}
