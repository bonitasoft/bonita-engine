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
package org.bonitasoft.engine.process;

import java.io.Serializable;

public class Employee implements Comparable<Object>, Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;

    private final String firstName;

    private final int experience;

    public Employee(final String name, final String firstName) {
        this.name = name;
        this.firstName = firstName;
        experience = 0;
    }

    public Employee(final String name, final String firstName, final int experience) {
        this.name = name;
        this.firstName = firstName;
        this.experience = experience;
    }

    public String getName() {
        return name;
    }

    public String getFirstName() {
        return firstName;
    }

    public int getExperience() {
        return experience;
    }

    @Override
    public int compareTo(final Object obj) {
        final Employee empl = (Employee) obj;
        if (experience != empl.experience) {
            return experience - empl.experience;
        }
        if (!name.equals(empl.name)) {
            return name.compareTo(empl.name);
        }
        return firstName.compareTo(empl.firstName);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + experience;
        result = prime * result + (firstName == null ? 0 : firstName.hashCode());
        result = prime * result + (name == null ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Employee)) {
            return false;
        }
        final Employee other = (Employee) obj;
        if (experience != other.experience) {
            return false;
        }
        if (firstName == null) {
            if (other.firstName != null) {
                return false;
            }
        } else if (!firstName.equals(other.firstName)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

}
