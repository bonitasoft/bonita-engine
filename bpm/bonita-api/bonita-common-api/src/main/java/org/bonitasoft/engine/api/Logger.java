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
package org.bonitasoft.engine.api;

/**
 * A logger given to {@link org.bonitasoft.engine.api.permission.PermissionRule}
 *
 * @author Baptiste Mesta
 */
public interface Logger {

    void trace(String message, Throwable t);

    void trace(String message);

    void debug(String message, Throwable t);

    void debug(String message);

    void info(String message, Throwable t);

    void info(String message);

    void warning(String message, Throwable t);

    void warning(String message);

    void error(String message, Throwable t);

    void error(String message);

}
