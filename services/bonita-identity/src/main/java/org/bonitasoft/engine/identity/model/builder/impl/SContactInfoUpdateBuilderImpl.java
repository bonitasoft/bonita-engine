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
package org.bonitasoft.engine.identity.model.builder.impl;

import org.bonitasoft.engine.identity.model.SContactInfo;
import org.bonitasoft.engine.identity.model.builder.SContactInfoUpdateBuilder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class SContactInfoUpdateBuilderImpl implements SContactInfoUpdateBuilder {

    private final EntityUpdateDescriptor descriptor;

    public SContactInfoUpdateBuilderImpl(final EntityUpdateDescriptor descriptor) {
        super();
        this.descriptor = descriptor;
    }

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

    @Override
    public SContactInfoUpdateBuilder updateEmail(final String email) {
        descriptor.addField(SContactInfo.EMAIL, email);
        return this;
    }

    @Override
    public SContactInfoUpdateBuilder updatePhoneNumber(final String phoneNumber) {
        descriptor.addField(SContactInfo.PHONE_NUMBER, phoneNumber);
        return this;
    }

    @Override
    public SContactInfoUpdateBuilder updateMobileNumber(final String mobileNumber) {
        descriptor.addField(SContactInfo.MOBILE_NUMBER, mobileNumber);
        return this;
    }

    @Override
    public SContactInfoUpdateBuilder updateFaxNumber(final String faxNumber) {
        descriptor.addField(SContactInfo.FAX_NUMBER, faxNumber);
        return this;
    }

    @Override
    public SContactInfoUpdateBuilder updateBuilding(final String building) {
        descriptor.addField(SContactInfo.BUILDING, building);
        return this;
    }

    @Override
    public SContactInfoUpdateBuilder updateRoom(final String room) {
        descriptor.addField(SContactInfo.ROOM, room);
        return this;
    }

    @Override
    public SContactInfoUpdateBuilder updateAddress(final String address) {
        descriptor.addField(SContactInfo.ADDRESS, address);
        return this;
    }

    @Override
    public SContactInfoUpdateBuilder updateZipCode(final String zipCode) {
        descriptor.addField(SContactInfo.ZIP_CODE, zipCode);
        return this;
    }

    @Override
    public SContactInfoUpdateBuilder updateCity(final String city) {
        descriptor.addField(SContactInfo.CITY, city);
        return this;
    }

    @Override
    public SContactInfoUpdateBuilder updateState(final String state) {
        descriptor.addField(SContactInfo.STATE, state);
        return this;
    }

    @Override
    public SContactInfoUpdateBuilder updateCountry(final String country) {
        descriptor.addField(SContactInfo.COUNTRY, country);
        return this;
    }

    @Override
    public SContactInfoUpdateBuilder updateWebsite(final String website) {
        descriptor.addField(SContactInfo.WEBSITE, website);
        return this;
    }

}
