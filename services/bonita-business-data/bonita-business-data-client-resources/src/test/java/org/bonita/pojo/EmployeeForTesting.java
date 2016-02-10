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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OrderColumn;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.Index;

import org.bonitasoft.engine.bdm.lazy.LazyLoaded;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Describe a simple employee
 */
@javax.persistence.Entity(name = "Employee")
@org.hibernate.annotations.Table(appliesTo = "EMPLOYEE", indexes = {
        @Index(name = "IDX_LSTNM", columnNames = {
                "LASTNAME"
        })
})
@javax.persistence.Table(name = "EMPLOYEE", uniqueConstraints = {
        @UniqueConstraint(name = "UK_FL", columnNames = {
                "FIRSTNAME",
                "LASTNAME"
        })
})
@NamedQueries({
        @NamedQuery(name = "Employee.findByFirstNameAndLastName", query = "SELECT e\nFROM Employee e\nWHERE e.firstName= :firstName\nAND e.lastName= :lastName\n"),
        @NamedQuery(name = "Employee.findByFirstName", query = "SELECT e\nFROM Employee e\nWHERE e.firstName= :firstName\nORDER BY e.persistenceId"),
        @NamedQuery(name = "Employee.findByLastName", query = "SELECT e\nFROM Employee e\nWHERE e.lastName= :lastName\nORDER BY e.persistenceId"),
        @NamedQuery(name = "Employee.find", query = "SELECT e\nFROM Employee e\nORDER BY e.persistenceId"),
        @NamedQuery(name = "Employee.findByPhoneNumber", query = "SELECT e FROM Employee e WHERE :phoneNumber IN ELEMENTS(e.phoneNumbers)"),
        @NamedQuery(name = "Employee.findByFirstNameAndLastNameNewOrder", query = "SELECT e FROM Employee e WHERE e.firstName =:firstName AND e.lastName = :lastName ORDER BY e.lastName"),
        @NamedQuery(name = "Employee.findByFirstNameFetchAddresses", query = "SELECT e FROM Employee e INNER JOIN FETCH e.addresses WHERE e.firstName =:firstName ORDER BY e.lastName"),
        @NamedQuery(name = "Employee.countEmployee", query = "SELECT COUNT(e) FROM Employee e")
})
public class EmployeeForTesting implements org.bonitasoft.engine.bdm.Entity {

    private static final long serialVersionUID = 6685785209798954931L;
    @Id
    @GeneratedValue
    private Long persistenceId;
    @Version
    private Long persistenceVersion;
    @Column(name = "FIRSTNAME", nullable = true, length = 10)
    private String firstName;
    @Column(name = "LASTNAME", nullable = false)
    private String lastName;
    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn
    @Column(name = "PHONENUMBERS", nullable = true, length = 10)
    private List<String> phoneNumbers = new ArrayList<String>(10);
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(name = "EMPLOYEE_ADDRESSES", joinColumns = {
            @JoinColumn(name = "EMPLOYEE_PID")
    }, inverseJoinColumns = {
            @JoinColumn(name = "ADDRESS_PID")
    })
    @OrderColumn
    @JsonIgnore
    private List<org.bonita.pojo.AddressForTesting> addresses = new ArrayList<org.bonita.pojo.AddressForTesting>(10);

    public EmployeeForTesting() {
    }

    public void setPersistenceId(final Long persistenceId) {
        this.persistenceId = persistenceId;
    }

    @Override
    public Long getPersistenceId() {
        return persistenceId;
    }

    public void setPersistenceVersion(final Long persistenceVersion) {
        this.persistenceVersion = persistenceVersion;
    }

    @Override
    public Long getPersistenceVersion() {
        return persistenceVersion;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setPhoneNumbers(final List<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void addToPhoneNumbers(final String addTo) {
        final List<String> phoneNumbers = getPhoneNumbers();
        phoneNumbers.add(addTo);
    }

    public void removeFromPhoneNumbers(final String removeFrom) {
        final List<?> phoneNumbers = getPhoneNumbers();
        phoneNumbers.remove(removeFrom);
    }

    public void setAddresses(final List<org.bonita.pojo.AddressForTesting> addresses) {
        this.addresses = addresses;
    }

    @LazyLoaded
    public List<org.bonita.pojo.AddressForTesting> getAddresses() {
        return addresses;
    }

    public void addToAddresses(final org.bonita.pojo.AddressForTesting addTo) {
        final List<AddressForTesting> addresses = getAddresses();
        addresses.add(addTo);
    }

    public void removeFromAddresses(final org.bonita.pojo.AddressForTesting removeFrom) {
        final List<?> addresses = getAddresses();
        addresses.remove(removeFrom);
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
        final EmployeeForTesting other = (EmployeeForTesting) obj;
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
        if (firstName == null) {
            if (other.firstName != null) {
                return false;
            }
        } else {
            if (!firstName.equals(other.firstName)) {
                return false;
            }
        }
        if (lastName == null) {
            if (other.lastName != null) {
                return false;
            }
        } else {
            if (!lastName.equals(other.lastName)) {
                return false;
            }
        }
        if (phoneNumbers == null) {
            if (other.phoneNumbers != null) {
                return false;
            }
        } else {
            if (!phoneNumbers.equals(other.phoneNumbers)) {
                return false;
            }
        }
        if (addresses == null) {
            if (other.addresses != null) {
                return false;
            }
        } else {
            if (!addresses.equals(other.addresses)) {
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
        int firstNameCode = 0;
        if (firstName != null) {
            firstNameCode = firstName.hashCode();
        }
        result = prime * result + firstNameCode;
        int lastNameCode = 0;
        if (lastName != null) {
            lastNameCode = lastName.hashCode();
        }
        result = prime * result + lastNameCode;
        int phoneNumbersCode = 0;
        if (phoneNumbers != null) {
            phoneNumbersCode = phoneNumbers.hashCode();
        }
        result = prime * result + phoneNumbersCode;
        int addressesCode = 0;
        if (addresses != null) {
            addressesCode = addresses.hashCode();
        }
        result = prime * result + addressesCode;
        return result;
    }

}
