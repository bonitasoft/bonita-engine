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
package org.bonitasoft.engine.core.contract.data;

import java.io.Serializable;

import javax.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;
import org.hibernate.annotations.Type;

/**
 * @author Matthieu Chaffotte
 */
@Data
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@IdClass(PersistentObjectId.class)
@DiscriminatorColumn(name = "kind")
@Table(name = "contract_data")
@SuperBuilder
public abstract class SContractData implements PersistentObject {

    @Id
    private long id;
    @Id
    private long tenantId;
    @Column
    private String name;
    @Column(name = "val")
    @Type(type = "xml_blob")
    private Serializable value;
    @Column
    private long scopeId;

    public SContractData(final String name, final Serializable value, long scopeId) {
        super();
        this.name = name;
        this.scopeId = scopeId;
        this.value = value;
    }
}
