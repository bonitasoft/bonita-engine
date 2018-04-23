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

import org.bonitasoft.engine.bpm.BonitaObject;

/**
 * Represents the descriptor of a (tenant) command or a platform command.
 * 
 * @author Zhang Bole
 * @author Emmanuel Duchastenier
 * @see org.bonitasoft.engine.api.CommandAPI
 * @since 6.0.0
 */
public interface CommandDescriptor extends BonitaObject {

    /**
     * Get the identifier of this <code>CommandDescriptor</code>
     * 
     * @return the if of this <code>CommandDescriptor</code>
     */
    long getId();

    /**
     * Get the name of the command
     * 
     * @return the name of the command
     */
    String getName();

    /**
     * Get the description of the command
     * 
     * @return the description of the command
     */
    String getDescription();

    /**
     * Get the implementation class name of the command
     * 
     * @return the implementation class name of the command
     */
    String getImplementation();

    /**
     * Is the command a default system command or a custom command.
     * 
     * @return true if this is a default system command, false otherwise.
     */
    boolean isSystemCommand();

}
