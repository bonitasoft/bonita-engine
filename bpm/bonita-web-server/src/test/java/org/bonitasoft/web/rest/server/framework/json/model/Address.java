/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.framework.json.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Address implements Serializable {

    private String street;
    private String city;

    // Do not remove. Used by Json Serialization.
    public Address() {
    }

    public Address(String street, String city) {
        this.street = street;
        this.city = city;
    }

    // Do not remove. Used by Json Serialization.
    public String getStreet() {
        return street;
    }

    // Do not remove. Used by Json Serialization.
    public void setStreet(String street) {
        this.street = street;
    }

    // Do not remove. Used by Json Serialization.
    public String getCity() {
        return city;
    }

    // Do not remove. Used by Json Serialization.
    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return "Address [street=" + street + ", city=" + city + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((city == null) ? 0 : city.hashCode());
        result = prime * result + ((street == null) ? 0 : street.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Address other = (Address) obj;
        if (city == null) {
            if (other.city != null)
                return false;
        } else if (!city.equals(other.city))
            return false;
        if (street == null) {
            if (other.street != null)
                return false;
        } else if (!street.equals(other.street))
            return false;
        return true;
    }

}
