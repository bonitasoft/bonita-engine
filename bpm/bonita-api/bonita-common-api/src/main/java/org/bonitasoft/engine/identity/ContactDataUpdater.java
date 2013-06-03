/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.identity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class ContactDataUpdater implements Serializable {

    private static final long serialVersionUID = 7871478791386229141L;

    public enum ContactDataField {
        EMAIL, PHONE, MOBILE, FAX, BUILDING, ROOM, ADDRESS, ZIP_CODE, CITY, STATE, COUNTRY, WEBSITE
    }

    private final Map<ContactDataField, Serializable> fields;

    public ContactDataUpdater() {
        fields = new HashMap<ContactDataField, Serializable>(5);
    }

    public void setEmail(final String email) {
        fields.put(ContactDataField.EMAIL, email);
    }

    public void setPhoneNumber(final String phoneNumber) {
        fields.put(ContactDataField.PHONE, phoneNumber);
    }

    public void setMobileNumber(final String mobileNumber) {
        fields.put(ContactDataField.MOBILE, mobileNumber);
    }

    public void setFaxNumber(final String faxNumber) {
        fields.put(ContactDataField.FAX, faxNumber);
    }

    public void setBuilding(final String building) {
        fields.put(ContactDataField.BUILDING, building);
    }

    public void setRoom(final String room) {
        fields.put(ContactDataField.ROOM, room);
    }

    public void setAddress(final String address) {
        fields.put(ContactDataField.ADDRESS, address);
    }

    public void setZipCode(final String zipCode) {
        fields.put(ContactDataField.ZIP_CODE, zipCode);
    }

    public void setCity(final String city) {
        fields.put(ContactDataField.CITY, city);
    }

    public void setState(final String state) {
        fields.put(ContactDataField.STATE, state);
    }

    public void setCountry(final String country) {
        fields.put(ContactDataField.COUNTRY, country);
    }

    public void setWebsite(final String website) {
        fields.put(ContactDataField.WEBSITE, website);
    }

    public Map<ContactDataField, Serializable> getFields() {
        return fields;
    }

    public boolean hasFields() {
        return !fields.isEmpty();
    }

}
