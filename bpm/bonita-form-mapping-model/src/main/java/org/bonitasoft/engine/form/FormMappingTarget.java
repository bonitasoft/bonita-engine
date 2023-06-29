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
package org.bonitasoft.engine.form;

/**
 * @author Baptiste Mesta
 */
public enum FormMappingTarget {

    /**
     * the target form is a bonita form
     */
    INTERNAL,

    /**
     * the target form is an external url
     */
    URL,

    /**
     * the target form is a legacy
     */
    LEGACY,

    /**
     * The form mapping is explicitly not yet defined but IS necessary for the process to be resolved
     */
    UNDEFINED,

    /**
     * The form mapping is not defined and IS NOT necessary. This value is automatically set when nothing is specified
     * at design-time.
     * It does not prevent the process to be resolved. It can be used when user tasks are meant to be executed out of a
     * web form context.
     */
    NONE
}
