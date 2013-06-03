/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.identity.model.builder.impl;

import org.bonitasoft.engine.identity.model.SContactInfo;
import org.bonitasoft.engine.identity.model.builder.SContactInfoBuilder;
import org.bonitasoft.engine.identity.model.impl.SContactInfoImpl;

/**
 * @author Emmanuel Duchastenier
 */
public class SContactInfoBuilderImpl implements SContactInfoBuilder {

    static final String ID = "id";

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

    private static final String IS_PERSONAL = "personal";

    private SContactInfoImpl entity;

    @Override
    public SContactInfoBuilder createNewInstance(final long userId, final boolean isPersonal) {
        entity = new SContactInfoImpl();
        entity.setUserId(userId);
        entity.setPersonal(isPersonal);
        return this;
    }

    @Override
    public SContactInfoBuilder createNewInstance(final SContactInfo contactInfo) {
        entity = new SContactInfoImpl(contactInfo);
        return this;
    }

    @Override
    public SContactInfoBuilder setUserId(final long userId) {
        entity.setUserId(userId);
        return this;
    }

    @Override
    public SContactInfoBuilder setPersonal(final boolean personal) {
        entity.setPersonal(personal);
        return this;
    }

    @Override
    public SContactInfoBuilder setId(final long id) {
        entity.setId(id);
        return this;
    }

    @Override
    public SContactInfoBuilder setEmail(final String email) {
        entity.setEmail(email);
        return this;
    }

    @Override
    public SContactInfoBuilder setPhoneNumber(final String phone) {
        entity.setPhoneNumber(phone);
        return this;
    }

    @Override
    public SContactInfoBuilder setMobileNumber(final String mobile) {
        entity.setMobileNumber(mobile);
        return this;
    }

    @Override
    public SContactInfoBuilder setFaxNumber(final String fax) {
        entity.setFaxNumber(fax);
        return this;
    }

    @Override
    public SContactInfoBuilder setBuilding(final String building) {
        entity.setBuilding(building);
        return this;
    }

    @Override
    public SContactInfoBuilder setRoom(final String room) {
        entity.setRoom(room);
        return this;
    }

    @Override
    public SContactInfoBuilder setAddress(final String adress) {
        entity.setAddress(adress);
        return this;
    }

    @Override
    public SContactInfoBuilder setZipCode(final String zipcode) {
        entity.setZipCode(zipcode);
        return this;
    }

    @Override
    public SContactInfoBuilder setCity(final String city) {
        entity.setCity(city);
        return this;
    }

    @Override
    public SContactInfoBuilder setState(final String state) {
        entity.setState(state);
        return this;
    }

    @Override
    public SContactInfoBuilder setCountry(final String country) {
        entity.setCountry(country);
        return this;
    }

    @Override
    public SContactInfoBuilder setWebsite(final String website) {
        entity.setWebsite(website);
        return this;
    }

    @Override
    public SContactInfo done() {
        return entity;
    }

    @Override
    public String getIdKey() {
        return ID;
    }

    @Override
    public String getEmailKey() {
        return EMAIL;
    }

    @Override
    public String getPhoneNumberKey() {
        return PHONE_NUMBER;
    }

    @Override
    public String getMobileNumberKey() {
        return MOBILE_NUMBER;
    }

    @Override
    public String getFaxNumberKey() {
        return FAX_NUMBER;
    }

    @Override
    public String getBuildingKey() {
        return BUILDING;
    }

    @Override
    public String getRoomKey() {
        return ROOM;
    }

    @Override
    public String getAddressKey() {
        return ADDRESS;
    }

    @Override
    public String getZipCodeKey() {
        return ZIP_CODE;
    }

    @Override
    public String getCityKey() {
        return CITY;
    }

    @Override
    public String getStateKey() {
        return STATE;
    }

    @Override
    public String getCountryKey() {
        return COUNTRY;
    }

    @Override
    public String getWebsiteKey() {
        return WEBSITE;
    }

    @Override
    public String getIsPersonalKey() {
        return IS_PERSONAL;
    }

}
