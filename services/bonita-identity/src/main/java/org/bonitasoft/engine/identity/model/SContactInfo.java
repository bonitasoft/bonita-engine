/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.identity.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * User contact info: can be personal or professional contact information.
 *
 * @author Emmanuel Duchastenier
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SContactInfo implements PersistentObject {

    public static final String ID = "id";
    public static final String WEBSITE = "website";
    public static final String COUNTRY = "country";
    public static final String STATE = "state";
    public static final String CITY = "city";
    public static final String ZIP_CODE = "zipCode";
    public static final String ADDRESS = "address";
    public static final String ROOM = "room";
    public static final String BUILDING = "building";
    public static final String FAX_NUMBER = "faxNumber";
    public static final String MOBILE_NUMBER = "mobileNumber";
    public static final String PHONE_NUMBER = "phoneNumber";
    public static final String EMAIL = "email";
    public static final String IS_PERSONAL = "personal";
    private long id;
    private long tenantId;
    private Long userId;
    private String email;
    private String phoneNumber;
    private String mobileNumber;
    private String faxNumber;
    private String building;
    private String room;
    private String address;
    private String zipCode;
    private String city;
    private String state;
    private String country;
    private String website;
    private boolean personal;

    public SContactInfo(final SContactInfo contactInfo) {
        this();
        userId = contactInfo.getUserId();
        personal = contactInfo.isPersonal();
        address = contactInfo.getAddress();
        building = contactInfo.getBuilding();
        city = contactInfo.getCity();
        country = contactInfo.getCountry();
        email = contactInfo.getEmail();
        faxNumber = contactInfo.getFaxNumber();
        mobileNumber = contactInfo.getMobileNumber();
        phoneNumber = contactInfo.getPhoneNumber();
        room = contactInfo.getRoom();
        state = contactInfo.getState();
        website = contactInfo.getWebsite();
        zipCode = contactInfo.getZipCode();
    }

}
