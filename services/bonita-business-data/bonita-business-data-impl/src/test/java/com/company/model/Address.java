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

import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;

import org.bonitasoft.engine.bdm.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Address implements Entity {

    @Id
    @GeneratedValue
    private Long persistenceId;
    @Version
    private Long persistenceVersion;

    private String street;

    private Float number;

    private List<Double> floors;

    @JsonIgnore
    private String doorCode;

    public void setPersistenceId(Long persistenceId) {
        this.persistenceId = persistenceId;
    }

    @Override
    public Long getPersistenceId() {
        return persistenceId;
    }

    @Override
    public Long getPersistenceVersion() {
        return persistenceVersion;
    }

    public void setPersistenceVersion(Long persistenceVersion) {
        this.persistenceVersion = persistenceVersion;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getDoorCode() {
        return doorCode;
    }

    public void setDoorCode(String doorCode) {
        this.doorCode = doorCode;
    }

    public Float getNumber() {
        return number;
    }

    public void setNumber(Float number) {
        this.number = number;
    }

    public List<Double> getFloors() {
        return floors;
    }

    public void setFloors(List<Double> floors) {
        this.floors = floors;
    }

}
