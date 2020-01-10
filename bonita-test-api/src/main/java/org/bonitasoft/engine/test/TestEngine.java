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
package org.bonitasoft.engine.test;

import org.bonitasoft.engine.BonitaDatabaseConfiguration;

/**
 * @author Baptiste Mesta
 */
public interface TestEngine {

    String TECHNICAL_USER_NAME = "install";
    String TECHNICAL_USER_PASSWORD = "install";

    boolean start() throws Exception;

    void stop() throws Exception;

    void clearData() throws Exception;

    void setDropOnStart(boolean dropOnStart);

    void setDropOnStop(boolean dropOnStop);

    void setBonitaDatabaseProperties(BonitaDatabaseConfiguration database);

    void setBusinessDataDatabaseProperties(BonitaDatabaseConfiguration database);
}
