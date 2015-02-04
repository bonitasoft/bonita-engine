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
package org.bonitasoft.engine.platform.command;

import org.bonitasoft.engine.bpm.BonitaObject;

/**
 * Describes a <code>Platform command</code>. Actions related to <code>platform commands</code> are managed by the {@link org.bonitasoft.engine.api.PlatformCommandAPI}
 *
 * @author Zhang Bole
 * @see org.bonitasoft.engine.api.PlatformCommandAPI
 * @since 6.0.0
 */
public interface PlatformCommandDescriptor extends BonitaObject {

    /**
     * Retrieves the command identifier
     *
     * @return a long indicating the command identifier
     */
    long getId();

    /**
     * Retrieves the command name
     *
     * @return a String containing the command name
     */
    String getName();

    /**
     * Retrieves the command description
     *
     * @return a String containing the command description
     */
    String getDescription();

    /**
     * Retrieves the complete name of the class that implements the command
     *
     * @return a String containing the complete name of the class that implements the command
     */
    String getImplementation();

}
