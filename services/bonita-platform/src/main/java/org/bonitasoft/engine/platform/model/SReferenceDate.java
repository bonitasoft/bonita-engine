/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.engine.platform.model;

/**
 * Defines which date field on a BDM object instance is used as the starting point
 * for the data retention period calculation.
 *
 * @see SDataRetentionConfig#getReferenceDate()
 */
public enum SReferenceDate {

    /**
     * Retention is calculated from the date the BDM object instance was first created.
     * The clock never resets, regardless of subsequent modifications.
     */
    CREATION,

    /**
     * Retention is calculated from the date the BDM object instance was last modified.
     * The clock resets to zero on every modification.
     */
    LAST_UPDATE
}
