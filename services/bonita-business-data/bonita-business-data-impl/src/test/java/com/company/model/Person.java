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

package com.company.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;


/**
 *
 */
@javax.persistence.Entity(name = "Person")
@Table(name = "PERSON")
@NamedQueries({
        @NamedQuery(name = "Person.findByName", query = "SELECT p\nFROM Person p\nWHERE p.name= :name\nORDER BY p.persistenceId"),
        @NamedQuery(name = "Person.findByAge", query = "SELECT p\nFROM Person p\nWHERE p.age= :age\nORDER BY p.persistenceId"),
        @NamedQuery(name = "Person.findByBirthday", query = "SELECT p\nFROM Person p\nWHERE p.birthday= :birthday\nORDER BY p.persistenceId"),
        @NamedQuery(name = "Person.findByHasMobile", query = "SELECT p\nFROM Person p\nWHERE p.hasMobile= :hasMobile\nORDER BY p.persistenceId"),
        @NamedQuery(name = "Person.find", query = "SELECT p\nFROM Person p\nORDER BY p.persistenceId"),
        @NamedQuery(name = "Person.findById", query = "SELECT p \nFROM Person p \nWHERE p.persistenceId = :persistenceId"),
        @NamedQuery(name = "Person.findAdults", query = "SELECT p \nFROM Person p \nWHERE p.age > 18\nORDER BY p.persistenceId ASC"),
        @NamedQuery(name = "Person.query1", query = "SELECT p \nFROM Person p \nWHERE p.hasMobile = :hasMobile\nORDER BY p.persistenceId ASC"),
        @NamedQuery(name = "Person.findByRange", query = "SELECT p \nFROM Person p \nWHERE p.birthday >= :date1 AND p.birthday <= :date2\nORDER BY p.persistenceId ASC")
})
public class Person implements org.bonitasoft.engine.bdm.Entity
{

    @Id
    @GeneratedValue
    private Long persistenceId;
    @Version
    private Long persistenceVersion;
    @Column(name = "NAME", nullable = true, length = 255)
    private String name;
    @Column(name = "AGE", nullable = true)
    private Integer age;
    @OneToMany(orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "PERSON_PID", nullable = false)
    @OrderColumn
    private List<com.company.model.Phone> phones = new ArrayList<com.company.model.Phone>(10);
    @Column(name = "BIRTHDAY", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date birthday;
    @Column(name = "HASMOBILE", nullable = false)
    private Boolean hasMobile;
    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn
    @Column(name = "BOOLS", nullable = true)
    private List<Boolean> bools = new ArrayList<Boolean>(10);

    public Person() {
    }

    public void setPersistenceId(Long persistenceId) {
        this.persistenceId = persistenceId;
    }

    public Long getPersistenceId() {
        return persistenceId;
    }

    public void setPersistenceVersion(Long persistenceVersion) {
        this.persistenceVersion = persistenceVersion;
    }

    public Long getPersistenceVersion() {
        return persistenceVersion;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getAge() {
        return age;
    }

    public void setPhones(List<com.company.model.Phone> phones) {
        if (this.phones == null) {
            this.phones = phones;
        } else {
            this.phones.clear();
            this.phones.addAll(phones);
        }
    }

    public List<com.company.model.Phone> getPhones() {
        return phones;
    }

    public void addToPhones(com.company.model.Phone addTo) {
        List phones = getPhones();
        phones.add(addTo);
    }

    public void removeFromPhones(com.company.model.Phone removeFrom) {
        List phones = getPhones();
        phones.remove(removeFrom);
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setHasMobile(Boolean hasMobile) {
        this.hasMobile = hasMobile;
    }

    public Boolean isHasMobile() {
        return hasMobile;
    }

    public void setBools(List<Boolean> bools) {
        if (this.bools == null) {
            this.bools = bools;
        } else {
            this.bools.clear();
            this.bools.addAll(bools);
        }
    }

    public List<Boolean> getBools() {
        return bools;
    }

    public void addToBools(Boolean addTo) {
        List bools = getBools();
        bools.add(addTo);
    }

    public void removeFromBools(Boolean removeFrom) {
        List bools = getBools();
        bools.remove(removeFrom);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass()!= obj.getClass()) {
            return false;
        }
        Person other = ((Person) obj);
        if (persistenceId == null) {
            if (other.persistenceId!= null) {
                return false;
            }
        } else {
            if (!persistenceId.equals(other.persistenceId)) {
                return false;
            }
        }
        if (persistenceVersion == null) {
            if (other.persistenceVersion!= null) {
                return false;
            }
        } else {
            if (!persistenceVersion.equals(other.persistenceVersion)) {
                return false;
            }
        }
        if (name == null) {
            if (other.name!= null) {
                return false;
            }
        } else {
            if (!name.equals(other.name)) {
                return false;
            }
        }
        if (age == null) {
            if (other.age!= null) {
                return false;
            }
        } else {
            if (!age.equals(other.age)) {
                return false;
            }
        }
        if (phones == null) {
            if (other.phones!= null) {
                return false;
            }
        } else {
            if (!phones.equals(other.phones)) {
                return false;
            }
        }
        if (birthday == null) {
            if (other.birthday!= null) {
                return false;
            }
        } else {
            if (!birthday.equals(other.birthday)) {
                return false;
            }
        }
        if (hasMobile == null) {
            if (other.hasMobile!= null) {
                return false;
            }
        } else {
            if (!hasMobile.equals(other.hasMobile)) {
                return false;
            }
        }
        if (bools == null) {
            if (other.bools!= null) {
                return false;
            }
        } else {
            if (!bools.equals(other.bools)) {
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
        if (persistenceId!= null) {
            persistenceIdCode = persistenceId.hashCode();
        }
        result = ((prime*result)+ persistenceIdCode);
        int persistenceVersionCode = 0;
        if (persistenceVersion!= null) {
            persistenceVersionCode = persistenceVersion.hashCode();
        }
        result = ((prime*result)+ persistenceVersionCode);
        int nameCode = 0;
        if (name!= null) {
            nameCode = name.hashCode();
        }
        result = ((prime*result)+ nameCode);
        int ageCode = 0;
        if (age!= null) {
            ageCode = age.hashCode();
        }
        result = ((prime*result)+ ageCode);
        int phonesCode = 0;
        if (phones!= null) {
            phonesCode = phones.hashCode();
        }
        result = ((prime*result)+ phonesCode);
        int birthdayCode = 0;
        if (birthday!= null) {
            birthdayCode = birthday.hashCode();
        }
        result = ((prime*result)+ birthdayCode);
        int hasMobileCode = 0;
        if (hasMobile!= null) {
            hasMobileCode = hasMobile.hashCode();
        }
        result = ((prime*result)+ hasMobileCode);
        int boolsCode = 0;
        if (bools!= null) {
            boolsCode = bools.hashCode();
        }
        result = ((prime*result)+ boolsCode);
        return result;
    }

}
