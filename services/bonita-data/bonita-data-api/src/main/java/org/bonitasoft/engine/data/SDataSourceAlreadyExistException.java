/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.data;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Charles Souillard
 * @author Matthieu Chaffotte
 */
public class SDataSourceAlreadyExistException extends SBonitaException {

    private static final long serialVersionUID = -5634245989800709240L;

    private final String name;

    private final String version;

    public SDataSourceAlreadyExistException(final String name, final String version) {
        super("A datasource with name: " + name + ", and version: " + version + " already exists.");
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

}
