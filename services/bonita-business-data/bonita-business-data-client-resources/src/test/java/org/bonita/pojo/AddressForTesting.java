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
package org.bonita.pojo;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 *
 */
@javax.persistence.Entity(name = "Address")
@Table(name = "ADDRESS")
@NamedQueries({
        @NamedQuery(name = "Address.findByStreet", query = "SELECT a\nFROM Address a\nWHERE a.street= :street\nORDER BY a.persistenceId"),
        @NamedQuery(name = "Address.findByCity", query = "SELECT a\nFROM Address a\nWHERE a.city= :city\nORDER BY a.persistenceId"),
        @NamedQuery(name = "Address.find", query = "SELECT a\nFROM Address a\nORDER BY a.persistenceId"),
        @NamedQuery(name = "Address.findAddressesByEmployeePersistenceId", query = "SELECT e.addresses\nFROM Employee e\nWHERE e.persistenceId= :persistenceId")
})
public class AddressForTesting implements org.bonitasoft.engine.bdm.Entity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long persistenceId;
    @Version
    private Long persistenceVersion;
    @Column(name = "STREET", nullable = true)
    private String street;
    @Column(name = "CITY", nullable = true)
    private String city;

    public AddressForTesting() {
    }

    public void setPersistenceId(Long persistenceId) {
        this.persistenceId = persistenceId;
    }

    @Override
    public Long getPersistenceId() {
        return persistenceId;
    }

    public void setPersistenceVersion(Long persistenceVersion) {
        this.persistenceVersion = persistenceVersion;
    }

    @Override
    public Long getPersistenceVersion() {
        return persistenceVersion;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreet() {
        return street;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AddressForTesting other = (AddressForTesting) obj;
        if (persistenceId == null) {
            if (other.persistenceId != null) {
                return false;
            }
        } else {
            if (!persistenceId.equals(other.persistenceId)) {
                return false;
            }
        }
        if (persistenceVersion == null) {
            if (other.persistenceVersion != null) {
                return false;
            }
        } else {
            if (!persistenceVersion.equals(other.persistenceVersion)) {
                return false;
            }
        }
        if (street == null) {
            if (other.street != null) {
                return false;
            }
        } else {
            if (!street.equals(other.street)) {
                return false;
            }
        }
        if (city == null) {
            if (other.city != null) {
                return false;
            }
        } else {
            if (!city.equals(other.city)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        int persistenceIdCode = 0;
        if (persistenceId != null) {
            persistenceIdCode = persistenceId.hashCode();
        }
        result = prime * result + persistenceIdCode;
        int persistenceVersionCode = 0;
        if (persistenceVersion != null) {
            persistenceVersionCode = persistenceVersion.hashCode();
        }
        result = prime * result + persistenceVersionCode;
        int streetCode = 0;
        if (street != null) {
            streetCode = street.hashCode();
        }
        result = prime * result + streetCode;
        int cityCode = 0;
        if (city != null) {
            cityCode = city.hashCode();
        }
        result = prime * result + cityCode;
        return result;
    }

}
