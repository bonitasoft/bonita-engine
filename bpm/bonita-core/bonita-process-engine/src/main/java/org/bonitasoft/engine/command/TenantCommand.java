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
package org.bonitasoft.engine.command;

/**
 * Class to be subclassed by implementors of a tenant scope {@link Command}. It is design to be executed by the
 * {@link org.bonitasoft.engine.api.CommandAPI}.
 *
 * @author Matthieu Chaffotte
 * @see org.bonitasoft.engine.api.CommandAPI
 * @since 6.0.0
 * @deprecated since 9.0.0, use {@link RuntimeCommand} instead
 */
@Deprecated(forRemoval = true, since = "9.0.0")
public abstract class TenantCommand extends RuntimeCommand {

}
