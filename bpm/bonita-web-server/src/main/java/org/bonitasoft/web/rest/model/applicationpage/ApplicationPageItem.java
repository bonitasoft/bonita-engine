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
package org.bonitasoft.web.rest.model.applicationpage;

import org.bonitasoft.web.rest.model.application.ApplicationItem;
import org.bonitasoft.web.rest.model.portal.page.PageItem;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasUniqueId;

/**
 * @author Julien Mege
 */
public class ApplicationPageItem extends Item implements ItemHasUniqueId {

    /* Token use to access the Page using URL : ../appName/pageToken/ */
    public static final String ATTRIBUTE_TOKEN = "token";

    public static final String ATTRIBUTE_APPLICATION_ID = "applicationId";

    public static final String ATTRIBUTE_PAGE_ID = "pageId";

    @Override
    public ApplicationPageDefinition getItemDefinition() {
        return ApplicationPageDefinition.get();
    }

    @Override
    public void setId(final String id) {
        setId(APIID.makeAPIID(id));
    }

    @Override
    public void setId(final Long id) {
        setId(APIID.makeAPIID(id));
    }

    public String getToken() {
        return getAttributeValue(ATTRIBUTE_TOKEN);
    }

    public void setToken(final String name) {
        setAttribute(ATTRIBUTE_TOKEN, name);
    }

    public APIID getApplicationId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_APPLICATION_ID);
    }

    public ApplicationItem getApplication() {
        return new ApplicationItem(getDeploy(ATTRIBUTE_APPLICATION_ID));
    }

    public void setApplicationId(final Long appId) {
        setAttribute(ATTRIBUTE_APPLICATION_ID, appId);
    }

    public APIID getPageId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_PAGE_ID);
    }

    public PageItem getPage() {
        return new PageItem(getDeploy(ATTRIBUTE_PAGE_ID));
    }

    public void setPageId(final Long pageId) {
        setAttribute(ATTRIBUTE_PAGE_ID, pageId);
    }

}
