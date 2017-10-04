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
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.company.model.javassist.MethodHandlerImpl;
import com.company.model.javassist.ProxyImpl;
import com.company.model.javassist.ProxyObjectImpl;
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

    @OneToOne(orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "PERSON_PID", nullable = false)
    private Address address1;

    @OneToOne(orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "PERSON_PID", nullable = false)
    private Address address2;

    private List<Long> toIncludes = new ArrayList<>(10);

    private MethodHandler methodHandlerObject;

    private List<Object> proxys = new ArrayList<>(10);

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

    public Address getAddress1() {
        return address1;
    }

    public void setAddress1(Address address1) {
        this.address1 = address1;
    }

    public Address getAddress2() {
        return address2;
    }

    public void setAddress2(Address address2) {
        this.address2 = address2;
    }

    public MethodHandler getMethodHandlerObject() {
        return methodHandlerObject;
    }

    public void setMethodHandlerObject(MethodHandler methodHandlerObject) {
        this.methodHandlerObject = methodHandlerObject;
    }

    public void addToProxysAsMethodHandler(Object object) {
        proxys.add(new MethodHandlerImpl(object));
    }

    public void addToProxysAsProxyImpl(Object object) {
        proxys.add(new ProxyImpl(object));
    }

    public void addToProxysAsProxyObjectImpl(Object object) {
        proxys.add(new ProxyObjectImpl(object));
    }

}
