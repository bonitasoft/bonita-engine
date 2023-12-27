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

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

/**
 * User contact info: can be personal or professional contact information.
 *
 * @author Emmanuel Duchastenier
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_contactinfo")
@IdClass(PersistentObjectId.class)
@Cacheable(false)
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
    @Id
    private long id;
    @Id
    private long tenantId;
    @Column
    private Long userId;
    @Column
    private String email;
    @Column(name = "phone")
    private String phoneNumber;
    @Column(name = "mobile")
    private String mobileNumber;
    @Column(name = "fax")
    private String faxNumber;
    @Column
    private String building;
    @Column
    private String room;
    @Column
    private String address;
    @Column
    private String zipCode;
    @Column
    private String city;
    @Column
    private String state;
    @Column
    private String country;
    @Column
    private String website;
    @Column
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
