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
package com.company.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Version;

import org.bonitasoft.engine.bdm.Entity;

/**
 *
 */
@javax.persistence.Entity(name = "Person")
@Table(name = "PERSON")
public class Person implements Entity {

    private static final long serialVersionUID = 7686491164348658989L;

    @Id
    @GeneratedValue
    private Long persistenceId;

    @Version
    private Long persistenceVersion;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "NICKNAMES", nullable = true, length = 15)
    @OrderColumn
    private List<String> nickNames = new ArrayList<String>(10);

    public Person() {
        super();
    }

    public Person(final Person person) {
        persistenceId = person.getPersistenceId();
        persistenceVersion = person.getPersistenceVersion();
        nickNames = person.getNickNames();
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

    public void setNickNames(final List<String> nickNames) {
        this.nickNames = nickNames;
    }

    public List<String> getNickNames() {
        return nickNames;
    }

    public void addTo(final String addTo) {
        nickNames.add(addTo);
    }

    public void removeFrom(final String removeFrom) {
        nickNames.remove(removeFrom);
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
        final Person other = (Person) obj;
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
        if (nickNames == null) {
            if (other.nickNames != null) {
                return false;
            }
        } else {
            if (!nickNames.equals(other.nickNames)) {
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
        int nickNamesCode = 0;
        if (nickNames != null) {
            nickNamesCode = nickNames.hashCode();
        }
        result = prime * result + nickNamesCode;
        return result;
    }

}
