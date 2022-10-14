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

import java.util.Map;

import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.identity.ContactData;
import org.bonitasoft.engine.identity.ContactDataUpdater;
import org.bonitasoft.engine.identity.UserUpdater;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.web.rest.model.identity.PersonalContactDataItem;
import org.bonitasoft.web.rest.server.datastore.CommonDatastore;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasAdd;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasGet;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasUpdate;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APISessionInvalidException;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Paul AMAR
 */
public class PersonalContactDataDatastore extends CommonDatastore<PersonalContactDataItem, ContactData> implements
        DatastoreHasGet<PersonalContactDataItem>, DatastoreHasUpdate<PersonalContactDataItem>,
        DatastoreHasAdd<PersonalContactDataItem> {

    public PersonalContactDataDatastore(final APISession engineSession) {
        super(engineSession);
    }

    @Override
    public PersonalContactDataItem get(final APIID id) {
        try {
            // Hard-coded at true because we want to retrieve ContactData
            final ContactData result = TenantAPIAccessor.getIdentityAPI(getEngineSession())
                    .getUserContactData(id.toLong(), true);
            return createContactDataItemConverter(id).convert(result);
        } catch (final NotFoundException e) {
            return null;
        } catch (final InvalidSessionException e) {
            throw new APISessionInvalidException(e);
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    @Override
    public PersonalContactDataItem update(final APIID id, final Map<String, String> attributes) {
        try {
            final ContactDataUpdater personalDataUpdater = new ContactDataUpdater();

            if (attributes.containsKey(PersonalContactDataItem.ATTRIBUTE_EMAIL)) {
                personalDataUpdater.setEmail(attributes.get(PersonalContactDataItem.ATTRIBUTE_EMAIL));
            }
            if (attributes.containsKey(PersonalContactDataItem.ATTRIBUTE_PHONE)) {
                personalDataUpdater.setPhoneNumber(attributes.get(PersonalContactDataItem.ATTRIBUTE_PHONE));
            }
            if (attributes.containsKey(PersonalContactDataItem.ATTRIBUTE_MOBILE)) {
                personalDataUpdater.setMobileNumber(attributes.get(PersonalContactDataItem.ATTRIBUTE_MOBILE));
            }
            if (attributes.containsKey(PersonalContactDataItem.ATTRIBUTE_FAX)) {
                personalDataUpdater.setFaxNumber(attributes.get(PersonalContactDataItem.ATTRIBUTE_FAX));
            }
            if (attributes.containsKey(PersonalContactDataItem.ATTRIBUTE_BUILDING)) {
                personalDataUpdater.setBuilding(attributes.get(PersonalContactDataItem.ATTRIBUTE_BUILDING));
            }
            if (attributes.containsKey(PersonalContactDataItem.ATTRIBUTE_ROOM)) {
                personalDataUpdater.setRoom(attributes.get(PersonalContactDataItem.ATTRIBUTE_ROOM));
            }
            if (attributes.containsKey(PersonalContactDataItem.ATTRIBUTE_ADDRESS)) {
                personalDataUpdater.setAddress(attributes.get(PersonalContactDataItem.ATTRIBUTE_ADDRESS));
            }
            if (attributes.containsKey(PersonalContactDataItem.ATTRIBUTE_ZIPCODE)) {
                personalDataUpdater.setZipCode(attributes.get(PersonalContactDataItem.ATTRIBUTE_ZIPCODE));
            }
            if (attributes.containsKey(PersonalContactDataItem.ATTRIBUTE_CITY)) {
                personalDataUpdater.setCity(attributes.get(PersonalContactDataItem.ATTRIBUTE_CITY));
            }
            if (attributes.containsKey(PersonalContactDataItem.ATTRIBUTE_STATE)) {
                personalDataUpdater.setState(attributes.get(PersonalContactDataItem.ATTRIBUTE_STATE));
            }
            if (attributes.containsKey(PersonalContactDataItem.ATTRIBUTE_COUNTRY)) {
                personalDataUpdater.setCountry(attributes.get(PersonalContactDataItem.ATTRIBUTE_COUNTRY));
            }
            if (attributes.containsKey(PersonalContactDataItem.ATTRIBUTE_WEBSITE)) {
                personalDataUpdater.setWebsite(attributes.get(PersonalContactDataItem.ATTRIBUTE_WEBSITE));
            }

            UserUpdater userUpdater = new UserUpdater()
                    .setPersonalContactData(personalDataUpdater)
                    // TODO remove once handle by engine
                    .setProfessionalContactData(new ContactDataUpdater());
            TenantAPIAccessor.getIdentityAPI(getEngineSession()).updateUser(id.toLong(), userUpdater);
            return get(id);

        } catch (final InvalidSessionException e) {
            throw new APISessionInvalidException(e);
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    @Override
    public PersonalContactDataItem add(final PersonalContactDataItem item) {
        // get the user id
        final APIID idUser = item.getId();

        final Map<String, String> attributes = item.getAttributes();
        return update(idUser, attributes);

    }

    @Override
    protected PersonalContactDataItem convertEngineToConsoleItem(final ContactData item) {
        throw new RuntimeException("Use ContactDataConverter instead!");
    }

    private ContactDataConverter<PersonalContactDataItem> createContactDataItemConverter(final APIID id) {
        return new ContactDataConverter<>() {

            @Override
            public PersonalContactDataItem createContactDataItem() {
                return new PersonalContactDataItem();
            }

            @Override
            public void setContactId(PersonalContactDataItem contactData) {
                contactData.setId(id);
            }

        };
    }
}
