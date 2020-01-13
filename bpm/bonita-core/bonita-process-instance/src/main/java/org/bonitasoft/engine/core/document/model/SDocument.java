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
package org.bonitasoft.engine.core.document.model;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Type;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = "content")
@ToString(exclude = { "content" })
@SuperBuilder
@Entity
@Table(name = "document")
@Cacheable(false)
public class SDocument extends AbstractSDocument {

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String AUTHOR = "author";
    public static final String CREATION_DATE = "creationDate";
    public static final String HAS_CONTENT = "hasContent";
    public static final String FILENAME = "fileName";
    public static final String MIMETYPE = "mimeType";
    public static final String URL = "url";
    public static final String VERSION = "version";
    public static final String DESCRIPTION = "description";
    public static final String INDEX = "index";

    @Type(type = "materialized_blob")
    private byte[] content;
}
