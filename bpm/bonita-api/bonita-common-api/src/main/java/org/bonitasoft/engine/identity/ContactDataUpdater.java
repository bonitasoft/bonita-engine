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
package org.bonitasoft.engine.identity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * represents a helper for updating a {@link ContactData}
 *
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @see ContactData
 * @since 6.0.0
 */
public class ContactDataUpdater implements Serializable {

    private static final long serialVersionUID = 7871478791386229141L;

    /**
     * The available contact fields
     */
    public enum ContactDataField {
        EMAIL, PHONE, MOBILE, FAX, BUILDING, ROOM, ADDRESS, ZIP_CODE, CITY, STATE, COUNTRY, WEBSITE
    }

    private final Map<ContactDataField, Serializable> fields;

    /**
     * Default Constructor.
     */
    public ContactDataUpdater() {
        fields = new HashMap<ContactDataField, Serializable>(5);
    }

    /**
     * @param email
     *        The contact email to create
     */
    public void setEmail(final String email) {
        fields.put(ContactDataField.EMAIL, email);
    }

    /**
     * @param phoneNumber
     *        The contact phone number to create
     */
    public void setPhoneNumber(final String phoneNumber) {
        fields.put(ContactDataField.PHONE, phoneNumber);
    }

    /**
     * @param mobileNumber
     *        The contact mobile number to create
     */
    public void setMobileNumber(final String mobileNumber) {
        fields.put(ContactDataField.MOBILE, mobileNumber);
    }

    /**
     * @param faxNumber
     *        The contact fax number to create
     */
    public void setFaxNumber(final String faxNumber) {
        fields.put(ContactDataField.FAX, faxNumber);
    }

    /**
     * @param building
     *        The contact building to create
     */
    public void setBuilding(final String building) {
        fields.put(ContactDataField.BUILDING, building);
    }

    /**
     * @param room
     *        The contact room to create
     */
    public void setRoom(final String room) {
        fields.put(ContactDataField.ROOM, room);
    }

    /**
     * @param address
     *        The contact address to create
     */
    public void setAddress(final String address) {
        fields.put(ContactDataField.ADDRESS, address);
    }

    /**
     * @param zipCode
     *        The contact ZIP code to create
     */
    public void setZipCode(final String zipCode) {
        fields.put(ContactDataField.ZIP_CODE, zipCode);
    }

    /**
     * @param city
     *        The contact city to create
     */
    public void setCity(final String city) {
        fields.put(ContactDataField.CITY, city);
    }

    /**
     * @param state
     *        The contact state to create
     */
    public void setState(final String state) {
        fields.put(ContactDataField.STATE, state);
    }

    /**
     * @param country
     *        The contact country to create
     */
    public void setCountry(final String country) {
        fields.put(ContactDataField.COUNTRY, country);
    }

    /**
     * @param website
     *        The contact web site address to create
     */
    public void setWebsite(final String website) {
        fields.put(ContactDataField.WEBSITE, website);
    }

    /**
     * @return The current contact data information to update
     */
    public Map<ContactDataField, Serializable> getFields() {
        return fields;
    }

    /**
     * @return True if there are some contact data to update
     */
    public boolean hasFields() {
        return !fields.isEmpty();
    }

}
