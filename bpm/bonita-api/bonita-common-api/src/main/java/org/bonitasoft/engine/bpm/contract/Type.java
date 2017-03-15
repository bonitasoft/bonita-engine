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
package org.bonitasoft.engine.bpm.contract;

/**
 * @author Matthieu Chaffotte
 * @since 7.0
 *
 *  All the default types accepted by the contract
 *
 */
public enum Type {
    TEXT, BOOLEAN,
    /**
     * Old java Date format. Maintained for compatibility reasons.
     */
    @Deprecated
    DATE,
    INTEGER, DECIMAL, BYTE_ARRAY, FILE, LONG,
    /**
     * Date format following the ISO-8601 norm. Timezone-independent, stores a date. Ex: 1993-02-24.
     * Uses the java 1.8 LocalDate.class.
     */
    LOCALDATE,
    /**
     * Date format following the ISO-8601 norm. Timezone-independent, stores a date, and a time. Ex: 1993-02-24 17:25:00.
     * The time is precise up to the nanosecond. Uses the java 1.8 LocalDateTime.class.
      */
    LOCALDATETIME,
    /**
     * Date format following the ISO-8601 norm. Timezone-dependent, stores a date, a time, and an Offset. Systematically rebased to the UTC
     * timezone by the engine. Ex: 1993-02-24 17:25:00Z. Uses the java 1.8 OffsetDateTime.class.
     *
     */
    OFFSETDATETIME
}
