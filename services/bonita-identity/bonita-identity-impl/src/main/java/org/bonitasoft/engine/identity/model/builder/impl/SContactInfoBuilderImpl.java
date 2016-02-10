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
import org.bonitasoft.engine.identity.model.impl.SContactInfoImpl;

/**
 * @author Emmanuel Duchastenier
 */
public class SContactInfoBuilderImpl implements SContactInfoBuilder {

    private final SContactInfoImpl entity;

    public SContactInfoBuilderImpl(final SContactInfoImpl entity) {
        super();
        this.entity = entity;
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
}
