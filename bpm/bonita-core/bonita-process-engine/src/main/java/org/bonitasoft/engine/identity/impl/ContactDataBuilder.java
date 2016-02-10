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
package org.bonitasoft.engine.identity.impl;

import org.bonitasoft.engine.identity.ContactData;

/**
 * @author Matthieu Chaffotte
 */
public class ContactDataBuilder {

    private final ContactDataImpl contactData;

    public ContactDataBuilder() {
        contactData = new ContactDataImpl();
    }

    public ContactDataBuilder setEmail(final String email) {
        contactData.setEmail(email);
        return this;
    }

    public ContactDataBuilder setPhoneNumber(final String phoneNumber) {
        contactData.setPhoneNumber(phoneNumber);
        return this;
    }

    public ContactDataBuilder setMobileNumber(final String mobileNumber) {
        contactData.setMobileNumber(mobileNumber);
        return this;
    }

    public ContactDataBuilder setFaxNumber(final String faxNumber) {
        contactData.setFaxNumber(faxNumber);
        return this;
    }

    public ContactDataBuilder setBuilding(final String building) {
        contactData.setBuilding(building);
        return this;
    }

    public ContactDataBuilder setRoom(final String room) {
        contactData.setRoom(room);
        return this;
    }

    public ContactDataBuilder setAddress(final String address) {
        contactData.setAddress(address);
        return this;
    }

    public ContactDataBuilder setZipCode(final String zipCode) {
        contactData.setZipCode(zipCode);
        return this;
    }

    public ContactDataBuilder setCity(final String city) {
        contactData.setCity(city);
        return this;
    }

    public ContactDataBuilder setState(final String state) {
        contactData.setState(state);
        return this;
    }

    public ContactDataBuilder setCountry(final String country) {
        contactData.setCountry(country);
        return this;
    }

    public ContactDataBuilder setWebsite(final String website) {
        contactData.setWebsite(website);
        return this;
    }

    public ContactDataBuilder setPersonal(final boolean personal) {
        contactData.setPersonal(personal);
        return this;
    }

    public ContactData done() {
        return contactData;
    }

}
