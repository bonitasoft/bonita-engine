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
package org.bonitasoft.engine.identity.xml;

import org.bonitasoft.engine.identity.ContactData;

/**
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 */
public class XMLContactDataMapping {

    static final String WEBSITE = "website";

    static final String COUNTRY = "country";

    static final String STATE = "state";

    static final String CITY = "city";

    static final String ZIP_CODE = "zipCode";

    static final String ADDRESS = "address";

    static final String ROOM = "room";

    static final String BUILDING = "building";

    static final String FAX_NUMBER = "faxNumber";

    static final String MOBILE_NUMBER = "mobileNumber";

    static final String PHONE_NUMBER = "phoneNumber";

    static final String EMAIL = "email";

    private final ContactData contactData;

    public XMLContactDataMapping(final ContactData contactData) {
        this.contactData = contactData;
    }

    public String getEmail() {
        return contactData.getEmail();
    }

    public String getPhoneNumber() {
        return contactData.getPhoneNumber();
    }

    public String getMobileNumber() {
        return contactData.getMobileNumber();
    }

    public String getFaxNumber() {
        return contactData.getFaxNumber();
    }

    public String getBuilding() {
        return contactData.getBuilding();
    }

    public String getRoom() {
        return contactData.getRoom();
    }

    public String getAddress() {
        return contactData.getAddress();
    }

    public String getZipCode() {
        return contactData.getZipCode();
    }

    public String getCity() {
        return contactData.getCity();
    }

    public String getState() {
        return contactData.getState();
    }

    public String getCountry() {
        return contactData.getCountry();
    }

    public String getWebsite() {
        return contactData.getWebsite();
    }

    public ContactData getContactData() {
        return contactData;
    }

}
