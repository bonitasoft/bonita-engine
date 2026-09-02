/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.engine.bpm;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * Interface <code>ArchivedRestElement</code> identifies an archived <code>BonitaObject</code> that can be used in the
 * REST API.
 */
public interface ArchivedRestElement extends ArchivedElement {

    /**
     * Gets the unique object identifier.
     * Serialized as a String in json as a Java long can be too big for JavaScript
     *
     * @return the unique object identifier
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @Override
    long getSourceObjectId();

    /**
     * Gets the date when the element was archived in milliseconds since epoch.
     * Serialized as a String in json as a Java long can be too big for JavaScript
     *
     * @return the unique object identifier
     */
    @JsonView
    @JsonProperty("archiveDate")
    @JsonSerialize(using = ToStringSerializer.class)
    long getArchiveDateAsLong();

    /**
     * Gets the date when the element was archived.
     * Ignored in Json serialization as it is already serialized as a long
     *
     * @return the archived date
     */
    @JsonIgnore
    @Override
    Date getArchiveDate();

}
