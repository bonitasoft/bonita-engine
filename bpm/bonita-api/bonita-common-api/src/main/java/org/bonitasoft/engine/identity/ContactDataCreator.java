/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
 * represents a helper to create {@link ContactData}
 *
 * @author Matthieu Chaffotte
 * @since 6.0.0
 */
public class ContactDataCreator implements Serializable {

    private static final long serialVersionUID = -1414989152963184543L;

    /**
     * represents the available {@link ContactData} fields
     */
    public enum ContactDataField {
        EMAIL, PHONE, MOBILE, FAX, BUILDING, ROOM, ADDRESS, ZIP_CODE, CITY, STATE, COUNTRY, WEBSITE;
    }

    private final Map<ContactDataField, Serializable> fields;

    /**
     * create a new creator instance
     */
    public ContactDataCreator() {
        fields = new HashMap<ContactDataField, Serializable>(5);
    }

    /**
     * @param email the contact email to create
     * @return the current {@link ContactDataCreator} for chaining purpose
     */
    public ContactDataCreator setEmail(final String email) {
        fields.put(ContactDataField.EMAIL, email);
        return this;
    }

    /**
     * @param email the contact phone number to create
     * @return the current {@link ContactDataCreator} for chaining purpose
     */
    public ContactDataCreator setPhoneNumber(final String phoneNumber) {
        fields.put(ContactDataField.PHONE, phoneNumber);
        return this;
    }

    /**
     * @param email the contact mobile number to create
     * @return the current {@link ContactDataCreator} for chaining purpose
     */
    public ContactDataCreator setMobileNumber(final String mobileNumber) {
        fields.put(ContactDataField.MOBILE, mobileNumber);
        return this;
    }

    /**
     * @param email the contact fax number to create
     * @return the current {@link ContactDataCreator} for chaining purpose
     */
    public ContactDataCreator setFaxNumber(final String faxNumber) {
        fields.put(ContactDataField.FAX, faxNumber);
        return this;
    }

    /**
     * @param email the contact building to create
     * @return the current {@link ContactDataCreator} for chaining purpose
     */
    public ContactDataCreator setBuilding(final String building) {
        fields.put(ContactDataField.BUILDING, building);
        return this;
    }

    /**
     * @param email the contact room to create
     * @return the current {@link ContactDataCreator} for chaining purpose
     */
    public ContactDataCreator setRoom(final String room) {
        fields.put(ContactDataField.ROOM, room);
        return this;
    }

    /**
     * @param email the contact address to create
     * @return the current {@link ContactDataCreator} for chaining purpose
     */
    public ContactDataCreator setAddress(final String address) {
        fields.put(ContactDataField.ADDRESS, address);
        return this;
    }

    /**
     * @param email the contact ZIP code to create
     * @return the current {@link ContactDataCreator} for chaining purpose
     */
    public ContactDataCreator setZipCode(final String zipCode) {
        fields.put(ContactDataField.ZIP_CODE, zipCode);
        return this;
    }

    /**
     * @param email the contact city to create
     * @return the current {@link ContactDataCreator} for chaining purpose
     */
    public ContactDataCreator setCity(final String city) {
        fields.put(ContactDataField.CITY, city);
        return this;
    }

    /**
     * @param email the contact state to create
     * @return the current {@link ContactDataCreator} for chaining purpose
     */
    public ContactDataCreator setState(final String state) {
        fields.put(ContactDataField.STATE, state);
        return this;
    }

    /**
     * @param email the contact country to create
     * @return the current {@link ContactDataCreator} for chaining purpose
     */
    public ContactDataCreator setCountry(final String country) {
        fields.put(ContactDataField.COUNTRY, country);
        return this;
    }

    /**
     * @param email the contact web site address to create
     * @return the current {@link ContactDataCreator} for chaining purpose
     */
    public ContactDataCreator setWebsite(final String website) {
        fields.put(ContactDataField.WEBSITE, website);
        return this;
    }

    /**
     * @return the current contact data information to create
     */
    public Map<ContactDataField, Serializable> getFields() {
        return fields;
    }

}
