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
package org.bonitasoft.web.rest.model.builder.identity;

import org.bonitasoft.engine.identity.ContactDataCreator;
import org.bonitasoft.web.rest.model.identity.ProfessionalContactDataItem;

/**
 * @author Colin PUY
 */
public class ContactDataBuilder {

    private String address = "anAddress";
    private final String building = "aBuilding";
    private final String city = "aCity";
    private final String country = "aCountry";
    private final String email = "anEmail";
    private final String faxNumber = "aFaxNumber";
    private final String mobileNumber = "aMobileNumber";
    private final String phoneNumber = "aPhoneNumber";
    private final String room = "aRoom";
    private final String state = "aState";
    private final String website = "aWebsite";
    private final String zipCode = "aZipCode";

    public static ContactDataBuilder aContactData() {
        return new ContactDataBuilder();
    }

    public ContactDataBuilder withAddress(String address) {
        this.address = address;
        return this;
    }

    public ContactDataCreator toContactDataCreator() {
        return new ContactDataCreator()
                .setAddress(address)
                .setBuilding(building)
                .setCity(city)
                .setCountry(country)
                .setEmail(email)
                .setFaxNumber(faxNumber)
                .setMobileNumber(mobileNumber)
                .setPhoneNumber(phoneNumber)
                .setRoom(room)
                .setState(state)
                .setWebsite(website)
                .setZipCode(zipCode);
    }

    public ProfessionalContactDataItem toProfessionalContactDataItem() {
        ProfessionalContactDataItem item = new ProfessionalContactDataItem();
        item.setAttribute(ProfessionalContactDataItem.ATTRIBUTE_EMAIL, email);
        item.setAttribute(ProfessionalContactDataItem.ATTRIBUTE_PHONE, phoneNumber);
        item.setAttribute(ProfessionalContactDataItem.ATTRIBUTE_MOBILE, mobileNumber);
        item.setAttribute(ProfessionalContactDataItem.ATTRIBUTE_FAX, faxNumber);
        item.setAttribute(ProfessionalContactDataItem.ATTRIBUTE_BUILDING, building);
        item.setAttribute(ProfessionalContactDataItem.ATTRIBUTE_ROOM, room);
        item.setAttribute(ProfessionalContactDataItem.ATTRIBUTE_ADDRESS, address);
        item.setAttribute(ProfessionalContactDataItem.ATTRIBUTE_ZIPCODE, zipCode);
        item.setAttribute(ProfessionalContactDataItem.ATTRIBUTE_CITY, city);
        item.setAttribute(ProfessionalContactDataItem.ATTRIBUTE_STATE, state);
        item.setAttribute(ProfessionalContactDataItem.ATTRIBUTE_COUNTRY, country);
        item.setAttribute(ProfessionalContactDataItem.ATTRIBUTE_WEBSITE, website);
        return item;
    }
}
