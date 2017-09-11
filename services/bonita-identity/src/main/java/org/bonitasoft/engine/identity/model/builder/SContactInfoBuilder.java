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
package org.bonitasoft.engine.identity.model.builder;

import org.bonitasoft.engine.identity.model.SContactInfo;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public interface SContactInfoBuilder {

    SContactInfoBuilder setPersonal(final boolean personal);

    SContactInfoBuilder setId(final long id);

    SContactInfoBuilder setUserId(final long userId);

    SContactInfoBuilder setEmail(final String email);

    SContactInfoBuilder setPhoneNumber(final String phoneNumber);

    SContactInfoBuilder setMobileNumber(final String mobileNumber);

    SContactInfoBuilder setFaxNumber(final String faxNumber);

    SContactInfoBuilder setBuilding(final String building);

    SContactInfoBuilder setRoom(final String room);

    SContactInfoBuilder setAddress(final String address);

    SContactInfoBuilder setZipCode(final String zipCode);

    SContactInfoBuilder setCity(final String city);

    SContactInfoBuilder setState(final String state);

    SContactInfoBuilder setCountry(final String country);

    SContactInfoBuilder setWebsite(final String website);

    SContactInfo done();

}
