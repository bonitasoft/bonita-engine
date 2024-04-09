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
package org.bonitasoft.web.rest.server.api.organization;

import static org.bonitasoft.web.rest.model.identity.CustomUserInfoItem.ATTRIBUTE_VALUE;
import static org.bonitasoft.web.rest.server.api.APIPreconditions.check;
import static org.bonitasoft.web.rest.server.api.APIPreconditions.containsOnly;

import java.util.Map;

import org.bonitasoft.engine.identity.CustomUserInfoValue;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.web.rest.model.identity.CustomUserInfoDefinition;
import org.bonitasoft.web.rest.model.identity.CustomUserInfoItem;
import org.bonitasoft.web.rest.server.api.ConsoleAPI;
import org.bonitasoft.web.rest.server.datastore.converter.ItemSearchResultConverter;
import org.bonitasoft.web.rest.server.datastore.filter.Filters;
import org.bonitasoft.web.rest.server.datastore.filter.GenericFilterCreator;
import org.bonitasoft.web.rest.server.datastore.utils.SearchOptionsCreator;
import org.bonitasoft.web.rest.server.datastore.utils.Sorts;
import org.bonitasoft.web.rest.server.engineclient.CustomUserInfoEngineClient;
import org.bonitasoft.web.rest.server.engineclient.CustomUserInfoEngineClientCreator;
import org.bonitasoft.web.rest.server.framework.api.APIHasSearch;
import org.bonitasoft.web.rest.server.framework.api.APIHasUpdate;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.i18n.T_;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Vincent Elcrin
 */
public class APICustomUserInfoValue extends ConsoleAPI<CustomUserInfoItem>
        implements APIHasSearch<CustomUserInfoItem>, APIHasUpdate<CustomUserInfoItem> {

    private final CustomUserInfoEngineClientCreator engineClientCreator;

    private final CustomUserInfoConverter converter = new CustomUserInfoConverter();

    public APICustomUserInfoValue(CustomUserInfoEngineClientCreator engineClientCreator) {
        this.engineClientCreator = engineClientCreator;
    }

    @Override
    public ItemSearchResult<CustomUserInfoItem> search(int page, int resultsByPage, String search, String orders,
            Map<String, String> filters) {
        SearchResult<CustomUserInfoValue> result = getClient().searchCustomUserInfoValues(new SearchOptionsCreator(
                page,
                resultsByPage,
                search,
                new Sorts(orders),
                new Filters(filters, new GenericFilterCreator(new CustomUserInfoAttributeConverter()))).create());
        return new ItemSearchResultConverter<>(
                page,
                resultsByPage,
                result,
                new CustomUserInfoConverter()).toItemSearchResult();
    }

    @Override
    public String defineDefaultSearchOrder() {
        return ATTRIBUTE_VALUE + " ASC";
    }

    @Override
    public CustomUserInfoItem update(APIID id, Map<String, String> attributes) {
        check(containsOnly(ATTRIBUTE_VALUE, attributes), new T_("Only the value attribute can be updated"));

        return converter.convert(getClient().setCustomUserInfoValue(
                id.getPartAsLong(1),
                id.getPartAsLong(0),
                attributes.get(ATTRIBUTE_VALUE)));
    }

    private CustomUserInfoEngineClient getClient() {
        return engineClientCreator.create(getEngineSession());
    }

    @Override
    protected ItemDefinition<CustomUserInfoItem> defineItemDefinition() {
        return CustomUserInfoDefinition.get();
    }
}
