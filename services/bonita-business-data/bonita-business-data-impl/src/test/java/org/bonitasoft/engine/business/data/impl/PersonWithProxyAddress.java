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
package org.bonitasoft.engine.business.data.impl;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import org.bonitasoft.engine.bdm.Entity;

/**
 * A parent entity with a @OneToOne child relationship to Address.
 * <p>
 * This entity is used to reproduce the bug where a HibernateProxy child entity
 * (address field) is serialized as an empty object {} instead of including its fields.
 * <p>
 * The address field can hold either a real Address or an AddressHibernateProxy.
 */
public class PersonWithProxyAddress implements Entity {

    @Id
    @GeneratedValue
    private Long persistenceId;

    @Version
    private Long persistenceVersion;

    private String name;

    @OneToOne(optional = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Entity address;

    public PersonWithProxyAddress() {
    }

    public PersonWithProxyAddress(Long persistenceId, String name) {
        this.persistenceId = persistenceId;
        this.name = name;
    }

    @Override
    public Long getPersistenceId() {
        return persistenceId;
    }

    public void setPersistenceId(Long persistenceId) {
        this.persistenceId = persistenceId;
    }

    @Override
    public Long getPersistenceVersion() {
        return persistenceVersion;
    }

    public void setPersistenceVersion(Long persistenceVersion) {
        this.persistenceVersion = persistenceVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Entity getAddress() {
        return address;
    }

    public void setAddress(Entity address) {
        this.address = address;
    }
}
