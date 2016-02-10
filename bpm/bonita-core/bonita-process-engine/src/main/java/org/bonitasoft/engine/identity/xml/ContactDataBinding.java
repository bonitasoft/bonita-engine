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

import java.util.Map;

import org.bonitasoft.engine.identity.impl.ContactDataBuilder;
import org.bonitasoft.engine.xml.ElementBinding;

/**
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 */
public abstract class ContactDataBinding extends ElementBinding {

    private final ContactDataBuilder contactDataBuilder;

    public ContactDataBinding() {
        super();
        contactDataBuilder = new ContactDataBuilder();
    }

    @Override
    public void setAttributes(final Map<String, String> attributes) {
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
        if (XMLContactDataMapping.EMAIL.equals(name)) {
            contactDataBuilder.setEmail(value);
        } else if (XMLContactDataMapping.PHONE_NUMBER.equals(name)) {
            contactDataBuilder.setPhoneNumber(value);
        } else if (XMLContactDataMapping.MOBILE_NUMBER.equals(name)) {
            contactDataBuilder.setMobileNumber(value);
        } else if (XMLContactDataMapping.FAX_NUMBER.equals(name)) {
            contactDataBuilder.setFaxNumber(value);
        } else if (XMLContactDataMapping.BUILDING.equals(name)) {
            contactDataBuilder.setBuilding(value);
        } else if (XMLContactDataMapping.ROOM.equals(name)) {
            contactDataBuilder.setRoom(value);
        } else if (XMLContactDataMapping.ADDRESS.equals(name)) {
            contactDataBuilder.setAddress(value);
        } else if (XMLContactDataMapping.ZIP_CODE.equals(name)) {
            contactDataBuilder.setZipCode(value);
        } else if (XMLContactDataMapping.CITY.equals(name)) {
            contactDataBuilder.setCity(value);
        } else if (XMLContactDataMapping.STATE.equals(name)) {
            contactDataBuilder.setState(value);
        } else if (XMLContactDataMapping.COUNTRY.equals(name)) {
            contactDataBuilder.setCountry(value);
        } else if (XMLContactDataMapping.WEBSITE.equals(name)) {
            contactDataBuilder.setWebsite(value);
        }
    }

    @Override
    public void setChildObject(final String name, final Object value) {
    }

    @Override
    public Object getObject() {
        return new XMLContactDataMapping(contactDataBuilder.done());
    }

}
