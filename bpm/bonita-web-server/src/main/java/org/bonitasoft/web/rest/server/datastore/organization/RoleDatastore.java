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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.console.common.server.utils.IconDescriptor;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.*;
import org.bonitasoft.engine.identity.*;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.web.rest.model.identity.RoleItem;
import org.bonitasoft.web.rest.server.datastore.CommonDatastore;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasAdd;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasDelete;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasGet;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasSearch;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasUpdate;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.rest.server.framework.utils.SearchOptionsBuilderUtil;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIForbiddenException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APINotFoundException;
import org.bonitasoft.web.toolkit.client.common.i18n.T_;
import org.bonitasoft.web.toolkit.client.common.texttemplate.Arg;
import org.bonitasoft.web.toolkit.client.common.util.MapUtil;
import org.bonitasoft.web.toolkit.client.common.util.StringUtil;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Séverin Moussel
 */
public class RoleDatastore extends CommonDatastore<RoleItem, Role> implements
        DatastoreHasGet<RoleItem>,
        DatastoreHasSearch<RoleItem>,
        DatastoreHasAdd<RoleItem>,
        DatastoreHasUpdate<RoleItem>,
        DatastoreHasDelete {

    public RoleDatastore(final APISession engineSession) {
        super(engineSession);
    }

    @Override
    public void delete(final List<APIID> ids) {
        try {
            final List<Long> longIds = new ArrayList<>();
            for (final APIID id : ids) {
                longIds.add(id.toLong());
            }

            getIdentityAPI().deleteRoles(longIds);

        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    @Override
    public RoleItem update(final APIID id, final Map<String, String> attributes) {
        try {
            final RoleUpdater updater = new RoleUpdater();

            if (attributes.containsKey(RoleItem.ATTRIBUTE_NAME)) {
                updater.setName(attributes.get(RoleItem.ATTRIBUTE_NAME));
            }
            if (attributes.containsKey(RoleItem.ATTRIBUTE_DISPLAY_NAME)) {
                updater.setDisplayName(attributes.get(RoleItem.ATTRIBUTE_DISPLAY_NAME));
            }
            if (attributes.containsKey(RoleItem.ATTRIBUTE_DESCRIPTION)) {
                updater.setDescription(attributes.get(RoleItem.ATTRIBUTE_DESCRIPTION));
            }
            if (!MapUtil.isBlank(attributes, RoleItem.ATTRIBUTE_ICON)) {
                IconDescriptor iconDescriptor = getBonitaHomeFolderAccessor()
                        .getIconFromFileSystem(attributes.get(RoleItem.ATTRIBUTE_ICON));
                updater.setIcon(iconDescriptor.getFilename(), iconDescriptor.getContent());
            }

            return convertEngineToConsoleItem(getIdentityAPI().updateRole(id.toLong(), updater));
        } catch (final RoleNotFoundException e) {
            throw new APINotFoundException(new T_("Unable to find role %roleId%", new Arg("roleId", id)));
        } catch (final BonitaException e) {
            throw new APIException(e);
        }

    }

    @Override
    public RoleItem add(final RoleItem role) {
        try {
            final RoleCreator creator = new RoleCreator(role.getName());

            if (!StringUtil.isBlank(role.getDisplayName())) {
                creator.setDisplayName(role.getDisplayName());
            }
            if (!StringUtil.isBlank(role.getDescription())) {
                creator.setDescription(role.getDescription());
            }
            if (!StringUtil.isBlank(role.getIcon())) {
                IconDescriptor iconDescriptor = getBonitaHomeFolderAccessor().getIconFromFileSystem(role.getIcon());
                creator.setIcon(iconDescriptor.getFilename(), iconDescriptor.getContent());
            }

            return convertEngineToConsoleItem(getIdentityAPI().createRole(creator));
        } catch (AlreadyExistsException e) {
            throw new APIForbiddenException(
                    new T_("Can't create role. Role '%roleName%' already exists", new Arg("roleName", role.getName())),
                    e);
        } catch (final BonitaException e) {
            throw new APIException(e);
        }

    }

    BonitaHomeFolderAccessor getBonitaHomeFolderAccessor() {
        return new BonitaHomeFolderAccessor();
    }

    @Override
    public ItemSearchResult<RoleItem> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {
        try {
            final SearchOptionsBuilder builder = SearchOptionsBuilderUtil.buildSearchOptions(page, resultsByPage,
                    orders, search);

            addStringFilterToSearchBuilder(filters, builder, RoleItem.ATTRIBUTE_NAME, RoleSearchDescriptor.NAME);
            addStringFilterToSearchBuilder(filters, builder, RoleItem.ATTRIBUTE_DISPLAY_NAME,
                    RoleSearchDescriptor.DISPLAY_NAME);

            final SearchResult<Role> engineSearchResults = getIdentityAPI().searchRoles(builder.done());
            final List<RoleItem> consoleSearchResults = new ArrayList<>();
            for (final Role engineItem : engineSearchResults.getResult()) {
                consoleSearchResults.add(convertEngineToConsoleItem(engineItem));
            }

            return new ItemSearchResult<>(page, resultsByPage, engineSearchResults.getCount(), consoleSearchResults);

        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    @Override
    public RoleItem get(final APIID id) {
        try {
            return convertEngineToConsoleItem(getIdentityAPI().getRole(id.toLong()));
        } catch (final RoleNotFoundException e) {
            throw new APINotFoundException(new T_("Unable to find role %roleId%", new Arg("roleId", id)));
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    public Long getNumberOfUsers(final APIID roleId) {
        try {
            return getIdentityAPI().getNumberOfUsersInRole(roleId.toLong());
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    IdentityAPI getIdentityAPI()
            throws InvalidSessionException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return TenantAPIAccessor.getIdentityAPI(getEngineSession());
    }

    @Override
    protected RoleItem convertEngineToConsoleItem(final Role item) {
        final RoleItem roleItem = new RoleItem();
        roleItem.setId(item.getId());
        roleItem.setName(item.getName());
        roleItem.setDisplayName(item.getDisplayName());
        roleItem.setDescription(item.getDescription());
        roleItem.setIcon(item.getIconId() == null ? "" : Avatars.PATH + item.getIconId());
        roleItem.setCreatedByUserId(item.getCreatedBy());
        roleItem.setCreationDate(item.getCreationDate());
        roleItem.setLastUpdateDate(item.getLastUpdate());
        return roleItem;
    }

}
