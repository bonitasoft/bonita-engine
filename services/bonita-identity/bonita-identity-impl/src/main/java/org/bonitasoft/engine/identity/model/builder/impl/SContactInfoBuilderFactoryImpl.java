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
package org.bonitasoft.engine.identity.model.builder.impl;

import org.bonitasoft.engine.identity.model.SContactInfo;
import org.bonitasoft.engine.identity.model.builder.SContactInfoBuilder;
import org.bonitasoft.engine.identity.model.builder.SContactInfoBuilderFactory;
import org.bonitasoft.engine.identity.model.impl.SContactInfoImpl;

/**
 * @author Emmanuel Duchastenier
 */
public class SContactInfoBuilderFactoryImpl implements SContactInfoBuilderFactory {

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

    @Override
    public SContactInfoBuilder createNewInstance(final long userId, final boolean isPersonal) {
        final SContactInfoImpl entity = new SContactInfoImpl();
        entity.setUserId(userId);
        entity.setPersonal(isPersonal);
        return new SContactInfoBuilderImpl(entity);
    }

    @Override
    public SContactInfoBuilder createNewInstance(final SContactInfo contactInfo) {
        final SContactInfoImpl entity = new SContactInfoImpl(contactInfo);
        return new SContactInfoBuilderImpl(entity);
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
