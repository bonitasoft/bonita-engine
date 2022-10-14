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
package org.bonitasoft.livingapps;

import java.util.List;

import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.business.application.Application;
import org.bonitasoft.engine.business.application.ApplicationMenuSearchDescriptor;
import org.bonitasoft.engine.business.application.ApplicationPageNotFoundException;
import org.bonitasoft.engine.business.application.ApplicationVisibility;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.page.PageNotFoundException;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileCriterion;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.livingapps.menu.Menu;
import org.bonitasoft.livingapps.menu.MenuFactory;

public class ApplicationModel {

    private final ProfileAPI profileApi;
    private final ApplicationAPI applicationApi;
    private final PageAPI pageApi;
    private final Application application;
    private final MenuFactory factory;

    public ApplicationModel(
            final ApplicationAPI applicationApi,
            final PageAPI pageApi,
            final ProfileAPI profileApi,
            final Application application,
            final MenuFactory factory) {
        this.applicationApi = applicationApi;
        this.pageApi = pageApi;
        this.profileApi = profileApi;
        this.application = application;
        this.factory = factory;
    }

    public long getId() {
        return application.getId();
    }

    public String getApplicationLayoutName() throws PageNotFoundException {
        return pageApi.getPage(application.getLayoutId()).getName();
    }

    public String getApplicationThemeName() throws PageNotFoundException {
        return pageApi.getPage(application.getThemeId()).getName();
    }

    public Long getApplicationThemeId() throws PageNotFoundException {
        return pageApi.getPage(application.getThemeId()).getId();
    }

    public String getApplicationHomePage() throws ApplicationPageNotFoundException {
        return applicationApi.getApplicationHomePage(application.getId()).getToken() + "/";
    }

    public boolean hasPage(final String pageToken) {
        try {
            applicationApi.getApplicationPage(application.getToken(), pageToken);
            return true;
        } catch (final ApplicationPageNotFoundException e) {
            return false;
        }
    }

    public boolean authorize(final APISession session) {
        if (ApplicationVisibility.ALL.equals(application.getVisibility())) {
            return true;
        } else if (ApplicationVisibility.TECHNICAL_USER.equals(application.getVisibility())) {
            return session.isTechnicalUser();
        } else {
            for (final Profile userProfile : getUserProfiles(session)) {
                if (userProfile.getId() == application.getProfileId()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasProfileMapped() {
        if (ApplicationVisibility.RESTRICTED.equals(application.getVisibility())) {
            return application.getProfileId() != null;
        }
        return true;
    }

    private List<Profile> getUserProfiles(final APISession session) {
        return profileApi.getProfilesForUser(
                session.getUserId(),
                0,
                Integer.MAX_VALUE,
                ProfileCriterion.ID_ASC);
    }

    public Page getCustomPage(final String pageToken) throws ApplicationPageNotFoundException, PageNotFoundException {
        return pageApi.getPage(applicationApi.getApplicationPage(application.getToken(), pageToken).getPageId());
    }

    public List<Menu> getMenuList() throws SearchException, ApplicationPageNotFoundException {
        return factory.create(applicationApi.searchApplicationMenus(new SearchOptionsBuilder(0, Integer.MAX_VALUE)
                .filter(ApplicationMenuSearchDescriptor.APPLICATION_ID, application.getId())
                .sort(ApplicationMenuSearchDescriptor.INDEX, Order.ASC).done())
                .getResult());
    }
}
