/**
 * Copyright (C) 2017 BonitaSoft S.A.
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

import com.fasterxml.jackson.annotation.JsonIgnore;

import javassist.util.proxy.MethodHandler;

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
public class PersonWithDetails implements org.bonitasoft.engine.bdm.Entity {

    @Id
    @GeneratedValue
    private Long persistenceId;
    @Version
    private Long persistenceVersion;
    @Column(name = "NAME", nullable = true, length = 255)
    private String name;
    @Column(name = "AGE", nullable = true)
    private Integer age;

    @JsonIgnore
    private Phone secretPhone = null;

    @OneToMany(orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "PERSON_PID", nullable = false)
    @OrderColumn
    private List<Phone> phones = new ArrayList<Phone>(10);
    @Column(name = "BIRTHDAY", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date birthday;
    @Column(name = "HASMOBILE", nullable = false)
    private Boolean hasMobile;
    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn
    @Column(name = "BOOLS", nullable = true)
    private List<Boolean> bools = new ArrayList<Boolean>(10);

    @ElementCollection(fetch = FetchType.LAZY)
    @OrderColumn
    @Column(name = "TOIGNORE", nullable = true)
    @JsonIgnore
    private List<Long> toIgnores = new ArrayList<>(10);

    private Address address;

    private List<Long> toIncludes = new ArrayList<>(10);

    private MethodHandler methodHandlerObject;

    public PersonWithDetails() {
        // needed by jackson (otherwise, add an annotation for constructor)
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

    public void setPhones(List<Phone> phones) {
        if (this.phones == null) {
            this.phones = phones;
        } else {
            this.phones.clear();
            this.phones.addAll(phones);
        }
    }

    public List<Phone> getPhones() {
        return phones;
    }

    public void addToPhones(Phone addTo) {
        List phones = getPhones();
        phones.add(addTo);
    }

    public void removeFromPhones(Phone removeFrom) {
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

    public List<Long> getToIgnores() {
        return toIgnores;
    }

    public void addToIgnores(Long addTo) {
        toIgnores.add(addTo);
    }

    public List<Long> getToIncludes() {
        return toIncludes;
    }

    public void setToIncludes(List<Long> toIncludes) {
        this.toIncludes = toIncludes;
    }

    public void addToIncludes(Long addTo) {
        toIncludes.add(addTo);
    }

    public Boolean getHasMobile() {
        return hasMobile;
    }

    public void setToIgnores(List<Long> toIgnores) {
        this.toIgnores = toIgnores;
    }

    public Phone getSecretPhone() {
        return secretPhone;
    }

    public void setSecretPhone(Phone secretPhone) {
        this.secretPhone = secretPhone;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public MethodHandler getMethodHandlerObject() {
        return methodHandlerObject;
    }

    public void setMethodHandlerObject(MethodHandler methodHandlerObject) {
        this.methodHandlerObject = methodHandlerObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        PersonWithDetails that = (PersonWithDetails) o;

        if (persistenceId != null ? !persistenceId.equals(that.persistenceId) : that.persistenceId != null)
            return false;
        if (persistenceVersion != null ? !persistenceVersion.equals(that.persistenceVersion)
                : that.persistenceVersion != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null)
            return false;
        if (age != null ? !age.equals(that.age) : that.age != null)
            return false;
        if (secretPhone != null ? !secretPhone.equals(that.secretPhone) : that.secretPhone != null)
            return false;
        if (phones != null ? !phones.equals(that.phones) : that.phones != null)
            return false;
        if (birthday != null ? !birthday.equals(that.birthday) : that.birthday != null)
            return false;
        if (hasMobile != null ? !hasMobile.equals(that.hasMobile) : that.hasMobile != null)
            return false;
        if (bools != null ? !bools.equals(that.bools) : that.bools != null)
            return false;
        if (toIgnores != null ? !toIgnores.equals(that.toIgnores) : that.toIgnores != null)
            return false;
        if (address != null ? !address.equals(that.address) : that.address != null)
            return false;
        if (toIncludes != null ? !toIncludes.equals(that.toIncludes) : that.toIncludes != null)
            return false;
        return methodHandlerObject != null ? methodHandlerObject.equals(that.methodHandlerObject)
                : that.methodHandlerObject == null;
    }

    @Override
    public int hashCode() {
        int result = persistenceId != null ? persistenceId.hashCode() : 0;
        result = 31 * result + (persistenceVersion != null ? persistenceVersion.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (age != null ? age.hashCode() : 0);
        result = 31 * result + (secretPhone != null ? secretPhone.hashCode() : 0);
        result = 31 * result + (phones != null ? phones.hashCode() : 0);
        result = 31 * result + (birthday != null ? birthday.hashCode() : 0);
        result = 31 * result + (hasMobile != null ? hasMobile.hashCode() : 0);
        result = 31 * result + (bools != null ? bools.hashCode() : 0);
        result = 31 * result + (toIgnores != null ? toIgnores.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (toIncludes != null ? toIncludes.hashCode() : 0);
        result = 31 * result + (methodHandlerObject != null ? methodHandlerObject.hashCode() : 0);
        return result;
    }

}
