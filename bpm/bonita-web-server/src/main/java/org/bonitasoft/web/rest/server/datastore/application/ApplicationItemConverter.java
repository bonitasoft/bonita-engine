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
package org.bonitasoft.web.rest.server.datastore.application;

import java.util.Map;

import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.console.common.server.utils.IconDescriptor;
import org.bonitasoft.engine.business.application.Application;
import org.bonitasoft.engine.business.application.ApplicationCreator;
import org.bonitasoft.engine.business.application.ApplicationUpdater;
import org.bonitasoft.engine.business.application.IApplication;
import org.bonitasoft.web.rest.model.application.AbstractApplicationItem;
import org.bonitasoft.web.rest.model.application.ApplicationItem;
import org.bonitasoft.web.rest.model.application.ApplicationLinkItem;
import org.bonitasoft.web.toolkit.client.common.util.MapUtil;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasIcon;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationItemConverter {

    protected BonitaHomeFolderAccessor bonitaHomeFolderAccessor;

    public ApplicationItemConverter(BonitaHomeFolderAccessor bonitaHomeFolderAccessor) {
        this.bonitaHomeFolderAccessor = bonitaHomeFolderAccessor;
    }

    public AbstractApplicationItem toApplicationItem(final IApplication application) {
        final AbstractApplicationItem item;

        if (application instanceof Application legacy) {
            var legacyItem = new ApplicationItem();
            item = legacyItem;
            if (legacy.getHomePageId() != null) {
                legacyItem.setHomePageId(legacy.getHomePageId());
            } else {
                legacyItem.setHomePageId(-1L);
            }
            if (legacy.getLayoutId() != null) {
                legacyItem.setLayoutId(legacy.getLayoutId());
            } else {
                legacyItem.setLayoutId(-1L);
            }
            if (legacy.getThemeId() != null) {
                legacyItem.setThemeId(legacy.getThemeId());
            } else {
                legacyItem.setThemeId(-1L);
            }
        } else {
            item = new ApplicationLinkItem();
        }

        item.setId(application.getId());
        item.setToken(application.getToken());
        item.setDisplayName(application.getDisplayName());
        item.setVersion(application.getVersion());
        item.setDescription(application.getDescription());
        item.setIcon(application.hasIcon() ? ApplicationItem.ICON_PATH_API_PREFIX + application.getId() + "?t="
                + application.getLastUpdateDate().getTime() : "");
        item.setCreationDate(Long.toString(application.getCreationDate().getTime()));
        item.setCreatedBy(application.getCreatedBy());
        item.setLastUpdateDate(Long.toString(application.getLastUpdateDate().getTime()));
        item.setUpdatedBy(application.getUpdatedBy());
        item.setState(application.getState());
        item.setVisibility(application.getVisibility().name());
        item.setEditable(application.isEditable());
        if (application.getProfileId() != null) {
            item.setProfileId(application.getProfileId());
        } else {
            item.setProfileId(-1L);
        }

        return item;
    }

    /**
     * @deprecated ApplicationCreator should no longer be used. Since 9.0.0, Applications should be created at startup.
     */
    @Deprecated(since = "10.2.0")
    public ApplicationCreator toApplicationCreator(final ApplicationItem appItem) {
        final ApplicationCreator creator = new ApplicationCreator(appItem.getToken(), appItem.getDisplayName(),
                appItem.getVersion());
        creator.setDescription(appItem.getDescription());
        creator.setProfileId(appItem.getProfileId().toLong());
        return creator;
    }

    /**
     * @deprecated ApplicationUpdater should no longer be used. Since 9.0.0, Applications should be created at startup.
     */
    @Deprecated(since = "10.2.0")
    public ApplicationUpdater toApplicationUpdater(final Map<String, String> attributes) {
        final ApplicationUpdater applicationUpdater = getApplicationUpdater();

        if (attributes.containsKey(ApplicationItem.ATTRIBUTE_TOKEN)) {
            applicationUpdater.setToken(attributes.get(ApplicationItem.ATTRIBUTE_TOKEN));
        }
        if (attributes.containsKey(ApplicationItem.ATTRIBUTE_DISPLAY_NAME)) {
            applicationUpdater.setDisplayName(attributes.get(ApplicationItem.ATTRIBUTE_DISPLAY_NAME));
        }
        if (attributes.containsKey(ApplicationItem.ATTRIBUTE_DESCRIPTION)) {
            applicationUpdater.setDescription(attributes.get(ApplicationItem.ATTRIBUTE_DESCRIPTION));
        }
        if (attributes.containsKey(ApplicationItem.ATTRIBUTE_PROFILE_ID)) {
            applicationUpdater.setProfileId(Long.parseLong(attributes.get(ApplicationItem.ATTRIBUTE_PROFILE_ID)));
        }

        if (attributes.containsKey(ApplicationItem.ATTRIBUTE_HOME_PAGE_ID)) {
            Long homePageId = Long.parseLong(attributes.get(ApplicationItem.ATTRIBUTE_HOME_PAGE_ID));
            if (homePageId == -1) {
                homePageId = null;
            }
            applicationUpdater.setHomePageId(homePageId);
        }
        if (attributes.containsKey(ApplicationItem.ATTRIBUTE_STATE)) {
            applicationUpdater.setState(attributes.get(ApplicationItem.ATTRIBUTE_STATE));
        }
        if (attributes.containsKey(ApplicationItem.ATTRIBUTE_VERSION)) {
            applicationUpdater.setVersion(attributes.get(ApplicationItem.ATTRIBUTE_VERSION));
        }
        if (!MapUtil.isBlank(attributes, ItemHasIcon.ATTRIBUTE_ICON)
                && !attributes.get(ItemHasIcon.ATTRIBUTE_ICON).startsWith(ApplicationItem.ICON_PATH_API_PREFIX)) {
            IconDescriptor iconDescriptor = bonitaHomeFolderAccessor
                    .getIconFromFileSystem(attributes.get(ItemHasIcon.ATTRIBUTE_ICON));
            applicationUpdater.setIcon(iconDescriptor.getFilename(), iconDescriptor.getContent());
        }

        return applicationUpdater;

    }

    /**
     * @deprecated ApplicationUpdater should no longer be used. Since 9.0.0, Applications should be created at startup.
     */
    @Deprecated(since = "10.2.0")
    protected ApplicationUpdater getApplicationUpdater() {
        return new ApplicationUpdater();
    }

}
