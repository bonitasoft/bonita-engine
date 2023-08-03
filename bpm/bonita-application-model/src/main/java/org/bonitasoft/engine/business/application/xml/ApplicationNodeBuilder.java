/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.business.application.xml;

import org.bonitasoft.engine.business.application.ApplicationState;

public class ApplicationNodeBuilder {

    public static class ApplicationNodeContainerBuilder {

        private final ApplicationNodeContainer applicationNodeContainer = new ApplicationNodeContainer();

        public ApplicationNodeContainerBuilder havingApplications(ApplicationBuilder... applicationBuilders) {
            for (final ApplicationBuilder applicationBuilder : applicationBuilders) {
                applicationNodeContainer.addApplication(applicationBuilder.create());
            }
            return this;
        }

        public ApplicationNodeContainer create() {
            return applicationNodeContainer;
        }

    }

    public static class ApplicationBuilder {

        private final ApplicationNode applicationNode;

        public ApplicationBuilder(String token, String displayName, String version) {
            applicationNode = new ApplicationNode();
            applicationNode.setToken(token);
            applicationNode.setDisplayName(displayName);
            applicationNode.setVersion(version);
            applicationNode.setState(ApplicationState.ACTIVATED.name());
        }

        public ApplicationBuilder withHomePage(String homePage) {
            applicationNode.setHomePage(homePage);
            return this;
        }

        public ApplicationBuilder withDescription(String description) {
            applicationNode.setDescription(description);
            return this;
        }

        public ApplicationBuilder withIconPath(String iconPath) {
            applicationNode.setIconPath(iconPath);
            return this;
        }

        public ApplicationBuilder withProfile(String profile) {
            applicationNode.setProfile(profile);
            return this;
        }

        public ApplicationBuilder withLayout(String layout) {
            applicationNode.setLayout(layout);
            return this;
        }

        public ApplicationBuilder withTheme(String theme) {
            applicationNode.setTheme(theme);
            return this;
        }

        public ApplicationBuilder havingApplicationPages(PageBuilder... applicationPageBuilders) {
            for (final PageBuilder applicationPageBuilder : applicationPageBuilders) {
                applicationNode.addApplicationPage(applicationPageBuilder.create());
            }
            return this;
        }

        public ApplicationBuilder havingApplicationMenus(MenuBuilder... applicationMenuBuilders) {
            for (final MenuBuilder applicationMenuBuilder : applicationMenuBuilders) {
                applicationNode.addApplicationMenu(applicationMenuBuilder.create());
            }
            return this;
        }

        public ApplicationNode create() {
            return applicationNode;
        }

    }

    public static class PageBuilder {

        private final ApplicationPageNode applicationPage;

        public PageBuilder(String customPage, String token) {
            applicationPage = new ApplicationPageNode();
            applicationPage.setCustomPage(customPage);
            applicationPage.setToken(token);
        }

        public ApplicationPageNode create() {
            return applicationPage;
        }

    }

    public static class MenuBuilder {

        private final ApplicationMenuNode applicationMenu;

        public MenuBuilder(String displayName, String applicationPage) {
            applicationMenu = new ApplicationMenuNode();
            applicationMenu.setDisplayName(displayName);
            applicationMenu.setApplicationPage(applicationPage);
        }

        public MenuBuilder havingMenu(MenuBuilder... menuBuilders) {
            for (final MenuBuilder applicationMenuBuilder : menuBuilders) {
                applicationMenu.addApplicationMenu(applicationMenuBuilder.create());
            }
            return this;
        }

        public ApplicationMenuNode create() {
            return applicationMenu;
        }
    }

    private ApplicationNodeBuilder() {

    }

    public static ApplicationNodeContainerBuilder newApplicationContainer() {
        return new ApplicationNodeContainerBuilder();
    }

    public static ApplicationBuilder newApplication(String token, String displayName, String version) {
        return new ApplicationBuilder(token, displayName, version);
    }

    public static PageBuilder newApplicationPage(String customPage, String token) {
        return new PageBuilder(customPage, token);
    }

    public static MenuBuilder newMenu(String displayName, String page) {
        return new MenuBuilder(displayName, page);
    }
}
