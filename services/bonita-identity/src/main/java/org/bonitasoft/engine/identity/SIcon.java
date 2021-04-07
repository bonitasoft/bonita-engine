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
package org.bonitasoft.engine.identity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;

/**
 * @author Baptiste Mesta
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "icon")
@Filter(name = "tenantFilter")
@IdClass(PersistentObjectId.class)
public class SIcon implements PersistentObject {

    @Id
    private long tenantId;
    @Id
    private long id;
    @Column(name = "mimetype")
    private String mimeType;
    @Type(type = "materialized_blob")
    @Column(name = "content")
    private byte[] content;

    public SIcon(String mimeType, byte[] content) {
        this.mimeType = mimeType;
        this.content = content;
    }
}
