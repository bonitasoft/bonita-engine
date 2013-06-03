/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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

import org.bonitasoft.engine.identity.model.builder.SContactInfoUpdateBuilder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class ContactInfoUpdateBuilderImpl implements SContactInfoUpdateBuilder {

    private final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

    @Override
    public SContactInfoUpdateBuilder updateEmail(final String email) {
        descriptor.addField(SContactInfoBuilderImpl.EMAIL, email);
        return this;
    }

    @Override
    public SContactInfoUpdateBuilder updatePhoneNumber(final String phoneNumber) {
        descriptor.addField(SContactInfoBuilderImpl.PHONE_NUMBER, phoneNumber);
        return this;
    }

    @Override
    public SContactInfoUpdateBuilder updateMobileNumber(final String mobileNumber) {
        descriptor.addField(SContactInfoBuilderImpl.MOBILE_NUMBER, mobileNumber);
        return this;
    }

    @Override
    public SContactInfoUpdateBuilder updateFaxNumber(final String faxNumber) {
        descriptor.addField(SContactInfoBuilderImpl.FAX_NUMBER, faxNumber);
        return this;
    }

    @Override
    public SContactInfoUpdateBuilder updateBuilding(final String building) {
        descriptor.addField(SContactInfoBuilderImpl.BUILDING, building);
        return this;
    }

    @Override
    public SContactInfoUpdateBuilder updateRoom(final String room) {
        descriptor.addField(SContactInfoBuilderImpl.ROOM, room);
        return this;
    }

    @Override
    public SContactInfoUpdateBuilder updateAddress(final String address) {
        descriptor.addField(SContactInfoBuilderImpl.ADDRESS, address);
        return this;
    }

    @Override
    public SContactInfoUpdateBuilder updateZipCode(final String zipCode) {
        descriptor.addField(SContactInfoBuilderImpl.ZIP_CODE, zipCode);
        return this;
    }

    @Override
    public SContactInfoUpdateBuilder updateCity(final String city) {
        descriptor.addField(SContactInfoBuilderImpl.CITY, city);
        return this;
    }

    @Override
    public SContactInfoUpdateBuilder updateState(final String state) {
        descriptor.addField(SContactInfoBuilderImpl.STATE, state);
        return this;
    }

    @Override
    public SContactInfoUpdateBuilder updateCountry(final String country) {
        descriptor.addField(SContactInfoBuilderImpl.COUNTRY, country);
        return this;
    }

    @Override
    public SContactInfoUpdateBuilder updateWebsite(final String website) {
        descriptor.addField(SContactInfoBuilderImpl.WEBSITE, website);
        return this;
    }

}
