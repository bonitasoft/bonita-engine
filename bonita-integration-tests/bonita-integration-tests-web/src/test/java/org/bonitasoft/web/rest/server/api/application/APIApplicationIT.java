/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api.application;

import static org.bonitasoft.web.rest.server.api.page.builder.PageItemBuilder.aPageItem;
import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.business.application.Application;
import org.bonitasoft.engine.business.application.ApplicationImportPolicy;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.application.*;
import org.bonitasoft.web.rest.model.portal.page.PageItem;
import org.bonitasoft.web.rest.server.api.applicationpage.APIApplicationDataStoreFactory;
import org.bonitasoft.web.rest.server.datastore.application.ApplicationDataStoreCreator;
import org.bonitasoft.web.test.AbstractConsoleTest;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class APIApplicationIT extends AbstractConsoleTest {

    public static final String HOME_PAGE_ZIP = "/homePage.zip";
    public static final String LAYOUT_ZIP = "/layout.zip";
    public static final String THEME_ZIP = "/theme.zip";
    public static final String APP_LINK_DESCRIPTOR = "/appLinkDescriptor.xml";
    private APIApplication apiApplication;
    private APISession session;

    @Override
    public void consoleTestSetUp() throws Exception {
        session = getInitiator().getSession();
        apiApplication = spy(
                new APIApplication(new ApplicationDataStoreCreator(), new APIApplicationDataStoreFactory()));
        apiApplication.setCaller(getAPICaller(session, "API/living/application"));
    }

    private PageAPI getPageAPI() throws Exception {
        return TenantAPIAccessor.getCustomPageAPI(session);
    }

    private ApplicationAPI getApplicationAPI() throws Exception {
        return TenantAPIAccessor.getApplicationAPI(session);
    }

    @After
    public void cleanPagesAndApplications() throws Exception {
        final SearchOptions searchOptions = new SearchOptionsBuilder(0, 100000).done();

        List<Application> apps = getApplicationAPI().searchApplications(searchOptions).getResult();
        apps.stream().map(Application::getId).forEach(t -> {
            try {
                getApplicationAPI().deleteApplication(t);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        List<Page> pages = getPageAPI().searchPages(searchOptions).getResult();
        var ids = pages.stream().map(Page::getId).toList();
        getPageAPI().deletePages(ids);
    }

    private PageItem addPage(String pageFileName) throws Exception {
        final PageItem pageItem = aPageItem().build();
        final InputStream pageInputStream = getClass().getResourceAsStream(pageFileName);
        final byte[] pageContent = IOUtils.toByteArray(pageInputStream);
        return addPageItemToRepository(pageItem.getContentName(), pageContent);
    }

    private PageItem addPageItemToRepository(final String pageContentName, final byte[] pageContent) throws Exception {
        return aPageItem().fromEngineItem(getPageAPI().createPage(pageContentName, pageContent)).build();
    }

    @Test
    public void add_ApplicationLink_is_forbidden() {
        // Given
        final ApplicationLinkItem linkItem = ApplicationLinkDefinition.get().createItem();
        linkItem.setToken("tokenLink");
        linkItem.setDisplayName("Link");
        linkItem.setVersion("1.0");
        linkItem.setProfileId(2L);
        linkItem.setState("Activated");

        // When, Then exception
        assertThrows("Expected exception: This deprecated API is not supported for application links.",
                APIException.class, () -> apiApplication.add(linkItem));
    }

    @Test
    public void should_add_LegacyApplication() throws Exception {
        // Given
        var legacyApp = createLegacyApplication();

        // Then
        Map<String, String> attributes = new HashMap<>(legacyApp.getAttributes().size());
        legacyApp.getAttributes().keySet().forEach(k -> attributes.put(k, legacyApp.getAttributes().get(k)));
        Assert.assertEquals(new HashMap<>(legacyApp.getAttributes()), attributes);
    }

    @Test
    public void update_ApplicationLink_is_forbidden_and_not_effective() throws Exception {
        // Given
        getApplicationAPI().importApplications(
                IOUtils.toByteArray(getClass().getResourceAsStream(APP_LINK_DESCRIPTOR)),
                ApplicationImportPolicy.REPLACE_DUPLICATES);
        ApplicationLinkItem linkItem = (ApplicationLinkItem) apiApplication
                .search(0, 1, null, null, Collections.singletonMap("token", "app1")).getResults().get(0);
        Map<String, String> attributes = Map.of(AbstractApplicationItem.ATTRIBUTE_DISPLAY_NAME, "Link Updated");

        // When, Then exception
        assertThrows(
                "Expected exception: This deprecated API is not supported for application links.",
                APIException.class, () -> apiApplication.update(linkItem.getId(), attributes));
        // Then not updated
        assertEquals("Application 1", apiApplication.get(linkItem.getId()).getDisplayName());
    }

    @Test
    public void should_update_LegacyApplication() throws Exception {
        // Given
        var legacyApp = createLegacyApplication();

        // When
        Map<String, String> attributes = Map.of(AbstractApplicationItem.ATTRIBUTE_DISPLAY_NAME, "Legacy Updated");
        var updatedItem = apiApplication.update(legacyApp.getId(), attributes);

        // Then
        assertEquals("Legacy Updated", updatedItem.getDisplayName());
    }

    @Test
    public void should_search_not_support_filtering_on_ApplicationLinks() throws Exception {
        // Given
        var legacyApp = createLegacyApplication();

        // When
        final String search = legacyApp.getDisplayName();
        final String orders = ApplicationItem.ATTRIBUTE_TOKEN + " DESC";
        final HashMap<String, String> filters = new HashMap<>();
        filters.put(ApplicationItem.ATTRIBUTE_LINK, "true");

        // Then
        assertThrows(
                "Expected exception: The search does not support filtering on application links in this edition.",
                BonitaRuntimeException.class, () -> apiApplication.search(0, 1, search, orders, filters));
    }

    private AbstractApplicationItem createLegacyApplication() throws Exception {
        addPage(HOME_PAGE_ZIP);
        final PageItem layout = addPage(LAYOUT_ZIP);
        final PageItem theme = addPage(THEME_ZIP);
        final ApplicationItem legacyItem = ApplicationDefinition.get().createItem();
        legacyItem.setToken("tokenLegacy");
        legacyItem.setDisplayName("Legacy");
        legacyItem.setVersion("1.0");
        legacyItem.setProfileId(2L);
        legacyItem.setState("ACTIVATED");
        legacyItem.setLayoutId(layout.getId().toLong());
        legacyItem.setThemeId(theme.getId().toLong());

        return apiApplication.add(legacyItem);
    }

}
