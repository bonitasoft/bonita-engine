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

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.MappedSuperclass;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

/**
 * Mapping for a document
 * <p>
 * can be part of a list of document, in that case all documents have the same name and an index
 * If it's standalone documents index = -1
 *
 * @author Baptiste Mesta
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
@IdClass(PersistentObjectId.class)
public class AbstractSDocumentMapping implements PersistentObject {

    @Id
    private long id;
    @Id
    private long tenantId;

    @Column(name = "processinstanceid")
    private long processInstanceId;

    @Column(name = "documentid")
    private long documentId;

    private String name;
    private String description;
    private String version;

    @Column(name = "index_")
    private int index;

    protected AbstractSDocumentMapping(long documentId, long processInstanceId, String name) {
        this.documentId = documentId;
        this.processInstanceId = processInstanceId;
        this.name = name;
    }
}
