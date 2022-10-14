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
import org.bonitasoft.web.rest.model.identity.ProfessionalContactDataItem;
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
public class ProfessionalContactDataDatastore extends CommonDatastore<ProfessionalContactDataItem, ContactData>
        implements
        DatastoreHasGet<ProfessionalContactDataItem>, DatastoreHasUpdate<ProfessionalContactDataItem>,
        DatastoreHasAdd<ProfessionalContactDataItem> {

    public ProfessionalContactDataDatastore(final APISession engineSession) {
        super(engineSession);
    }

    @Override
    public ProfessionalContactDataItem get(final APIID id) {
        try {
            // Hard-coded at true because we want to retrieve ContactData
            final ContactData result = TenantAPIAccessor.getIdentityAPI(getEngineSession())
                    .getUserContactData(id.toLong(), false);
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
    public ProfessionalContactDataItem update(final APIID id, final Map<String, String> attributes) {
        try {
            final ContactDataUpdater professionalDataUpdater = new ContactDataUpdater();

            if (attributes.containsKey(ProfessionalContactDataItem.ATTRIBUTE_EMAIL)) {
                professionalDataUpdater.setEmail(attributes.get(ProfessionalContactDataItem.ATTRIBUTE_EMAIL));
            }
            if (attributes.containsKey(ProfessionalContactDataItem.ATTRIBUTE_PHONE)) {
                professionalDataUpdater.setPhoneNumber(attributes.get(ProfessionalContactDataItem.ATTRIBUTE_PHONE));
            }
            if (attributes.containsKey(ProfessionalContactDataItem.ATTRIBUTE_MOBILE)) {
                professionalDataUpdater.setMobileNumber(attributes.get(ProfessionalContactDataItem.ATTRIBUTE_MOBILE));
            }
            if (attributes.containsKey(ProfessionalContactDataItem.ATTRIBUTE_FAX)) {
                professionalDataUpdater.setFaxNumber(attributes.get(ProfessionalContactDataItem.ATTRIBUTE_FAX));
            }
            if (attributes.containsKey(ProfessionalContactDataItem.ATTRIBUTE_BUILDING)) {
                professionalDataUpdater.setBuilding(attributes.get(ProfessionalContactDataItem.ATTRIBUTE_BUILDING));
            }
            if (attributes.containsKey(ProfessionalContactDataItem.ATTRIBUTE_ROOM)) {
                professionalDataUpdater.setRoom(attributes.get(ProfessionalContactDataItem.ATTRIBUTE_ROOM));
            }
            if (attributes.containsKey(ProfessionalContactDataItem.ATTRIBUTE_ADDRESS)) {
                professionalDataUpdater.setAddress(attributes.get(ProfessionalContactDataItem.ATTRIBUTE_ADDRESS));
            }
            if (attributes.containsKey(ProfessionalContactDataItem.ATTRIBUTE_ZIPCODE)) {
                professionalDataUpdater.setZipCode(attributes.get(ProfessionalContactDataItem.ATTRIBUTE_ZIPCODE));
            }
            if (attributes.containsKey(ProfessionalContactDataItem.ATTRIBUTE_CITY)) {
                professionalDataUpdater.setCity(attributes.get(ProfessionalContactDataItem.ATTRIBUTE_CITY));
            }
            if (attributes.containsKey(ProfessionalContactDataItem.ATTRIBUTE_STATE)) {
                professionalDataUpdater.setState(attributes.get(ProfessionalContactDataItem.ATTRIBUTE_STATE));
            }
            if (attributes.containsKey(ProfessionalContactDataItem.ATTRIBUTE_COUNTRY)) {
                professionalDataUpdater.setCountry(attributes.get(ProfessionalContactDataItem.ATTRIBUTE_COUNTRY));
            }
            if (attributes.containsKey(ProfessionalContactDataItem.ATTRIBUTE_WEBSITE)) {
                professionalDataUpdater.setWebsite(attributes.get(ProfessionalContactDataItem.ATTRIBUTE_WEBSITE));
            }

            UserUpdater userUpdater = new UserUpdater()
                    .setProfessionalContactData(professionalDataUpdater)
                    // TODO remove once handle by engine
                    .setPersonalContactData(new ContactDataUpdater());
            TenantAPIAccessor.getIdentityAPI(getEngineSession()).updateUser(id.toLong(), userUpdater);
            return get(id);

        } catch (final InvalidSessionException e) {
            throw new APISessionInvalidException(e);
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    @Override
    public ProfessionalContactDataItem add(final ProfessionalContactDataItem item) {
        return update(item.getId(), item.getAttributes());

    }

    @Override
    protected ProfessionalContactDataItem convertEngineToConsoleItem(ContactData item) {
        throw new RuntimeException("Use ContactDataConverter instead!");
    }

    private ContactDataConverter<ProfessionalContactDataItem> createContactDataItemConverter(final APIID id) {
        return new ContactDataConverter<>() {

            @Override
            public ProfessionalContactDataItem createContactDataItem() {
                return new ProfessionalContactDataItem();
            }

            @Override
            public void setContactId(ProfessionalContactDataItem contactData) {
                contactData.setId(id);
            }

        };
    }
}
