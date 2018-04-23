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
package org.bonitasoft.engine.command;

/**
 * Contains constants to search for commands using {@link org.bonitasoft.engine.api.CommandAPI#searchCommands(org.bonitasoft.engine.search.SearchOptions)}
 *
 * @author Yanyan Liu
 * @see org.bonitasoft.engine.api.CommandAPI#searchCommands(org.bonitasoft.engine.search.SearchOptions)
 * @since 6.0.0
 */
public class CommandSearchDescriptor {

    public static final String ID = "id";

    public static final String NAME = "name";

    public static final String DESCRIPTION = "description";

    public static final String IMPLEMENTATION = "implementation";

    public static final String SYSTEM = "system";
}
