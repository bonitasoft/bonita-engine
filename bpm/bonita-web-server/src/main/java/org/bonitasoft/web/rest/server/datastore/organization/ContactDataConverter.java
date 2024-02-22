/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.datastore.organization;

import org.bonitasoft.engine.identity.ContactData;
import org.bonitasoft.web.rest.model.identity.AbstractContactDataItem;
import org.bonitasoft.web.rest.server.datastore.converter.ItemConverter;

/**
 * @author Vincent Elcrin
 */
public abstract class ContactDataConverter<C extends AbstractContactDataItem>
        extends ItemConverter<AbstractContactDataItem, ContactData> {

    @Override
    public C convert(ContactData item) {
        if (item == null) {
            return createContactDataItem();
        }
        C contactData = createContactDataItem();
        contactData.setAddress(item.getAddress());
        contactData.setEmail(item.getEmail());
        contactData.setPhoneNumber(item.getPhoneNumber());
        contactData.setMobileNumber(item.getMobileNumber());
        contactData.setFaxNumber(item.getFaxNumber());
        contactData.setBuilding(item.getBuilding());
        contactData.setRoom(item.getRoom());
        contactData.setZipCode(item.getZipCode());
        contactData.setCity(item.getCity());
        contactData.setState(item.getState());
        contactData.setCountry(item.getCountry());
        contactData.setWebsite(item.getWebsite());
        setContactId(contactData);
        return contactData;
    }

    public abstract C createContactDataItem();

    public abstract void setContactId(C contactData);

}
