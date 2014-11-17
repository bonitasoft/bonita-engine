/*
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
