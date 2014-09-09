/**
 * Copyright (C) 2013-2014 BonitaSoft S.A.
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
 * Represents a helper to create {@link ContactData}
 *
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @since 6.0.0
 */
public class ContactDataCreator implements Serializable {

    private static final long serialVersionUID = -1414989152963184543L;

    /**
     * Represents the available {@link ContactData} fields
     */
    public enum ContactDataField {
        EMAIL, PHONE, MOBILE, FAX, BUILDING, ROOM, ADDRESS, ZIP_CODE, CITY, STATE, COUNTRY, WEBSITE;
    }

    private final Map<ContactDataField, Serializable> fields;

    /**
     * Create a new creator instance
     */
    public ContactDataCreator() {
        fields = new HashMap<ContactDataField, Serializable>(5);
    }

    /**
     * @param email
     *        The contact email to create
     * @return The current {@link ContactDataCreator} for chaining purpose
     */
    public ContactDataCreator setEmail(final String email) {
        fields.put(ContactDataField.EMAIL, email);
        return this;
    }

    /**
     * @param phoneNumber
     *        The contact phone number to create
     * @return The current {@link ContactDataCreator} for chaining purpose
     */
    public ContactDataCreator setPhoneNumber(final String phoneNumber) {
        fields.put(ContactDataField.PHONE, phoneNumber);
        return this;
    }

    /**
     * @param mobileNumber
     *        The contact mobile number to create
     * @return The current {@link ContactDataCreator} for chaining purpose
     */
    public ContactDataCreator setMobileNumber(final String mobileNumber) {
        fields.put(ContactDataField.MOBILE, mobileNumber);
        return this;
    }

    /**
     * @param faxNumber
     *        The contact fax number to create
     * @return The current {@link ContactDataCreator} for chaining purpose
     */
    public ContactDataCreator setFaxNumber(final String faxNumber) {
        fields.put(ContactDataField.FAX, faxNumber);
        return this;
    }

    /**
     * @param building
     *        The contact building to create
     * @return The current {@link ContactDataCreator} for chaining purpose
     */
    public ContactDataCreator setBuilding(final String building) {
        fields.put(ContactDataField.BUILDING, building);
        return this;
    }

    /**
     * @param room
     *        The contact room to create
     * @return The current {@link ContactDataCreator} for chaining purpose
     */
    public ContactDataCreator setRoom(final String room) {
        fields.put(ContactDataField.ROOM, room);
        return this;
    }

    /**
     * @param address
     *        The contact address to create
     * @return The current {@link ContactDataCreator} for chaining purpose
     */
    public ContactDataCreator setAddress(final String address) {
        fields.put(ContactDataField.ADDRESS, address);
        return this;
    }

    /**
     * @param zipCode
     *        The contact ZIP code to create
     * @return The current {@link ContactDataCreator} for chaining purpose
     */
    public ContactDataCreator setZipCode(final String zipCode) {
        fields.put(ContactDataField.ZIP_CODE, zipCode);
        return this;
    }

    /**
     * @param city
     *        The contact city to create
     * @return The current {@link ContactDataCreator} for chaining purpose
     */
    public ContactDataCreator setCity(final String city) {
        fields.put(ContactDataField.CITY, city);
        return this;
    }

    /**
     * @param state
     *        The contact state to create
     * @return The current {@link ContactDataCreator} for chaining purpose
     */
    public ContactDataCreator setState(final String state) {
        fields.put(ContactDataField.STATE, state);
        return this;
    }

    /**
     * @param country
     *        The contact country to create
     * @return The current {@link ContactDataCreator} for chaining purpose
     */
    public ContactDataCreator setCountry(final String country) {
        fields.put(ContactDataField.COUNTRY, country);
        return this;
    }

    /**
     * @param website
     *        The contact web site address to create
     * @return The current {@link ContactDataCreator} for chaining purpose
     */
    public ContactDataCreator setWebsite(final String website) {
        fields.put(ContactDataField.WEBSITE, website);
        return this;
    }

    /**
     * @return The current contact data information to create
     */
    public Map<ContactDataField, Serializable> getFields() {
        return fields;
    }

}
