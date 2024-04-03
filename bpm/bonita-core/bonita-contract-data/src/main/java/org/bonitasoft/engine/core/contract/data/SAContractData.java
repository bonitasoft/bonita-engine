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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;
import org.bonitasoft.engine.persistence.SAPersistenceObjectImpl;
import org.hibernate.annotations.Type;

/**
 * author Emmanuel Duchastenier
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@IdClass(PersistentObjectId.class)
@DiscriminatorColumn(name = "kind")
@Table(name = "arch_contract_data")
public abstract class SAContractData extends SAPersistenceObjectImpl {

    @Column
    protected String name;
    @Column(name = "val")
    @Type(type = "xml_blob")
    protected Serializable value;
    @Column
    protected long scopeId;

    public SAContractData(long sourceObjectId, String name, Serializable value, long scopeId) {
        super(sourceObjectId);
        this.name = name;
        this.scopeId = scopeId;
        this.value = value;
    }

    public SAContractData(SContractData contractData) {
        this(contractData.getId(), contractData.getName(), contractData.getValue(), contractData.getScopeId());
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SContractData.class;
    }
}
