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
package org.bonitasoft.livingapps.menu;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.business.application.ApplicationMenu;
import org.bonitasoft.engine.business.application.ApplicationPageNotFoundException;
import org.bonitasoft.engine.exception.SearchException;

public class MenuFactory {

    public interface Collector {

        boolean isCollectible(ApplicationMenu item);
    }

    private final ApplicationAPI applicationApi;

    public MenuFactory(final ApplicationAPI applicationApi) {
        this.applicationApi = applicationApi;
    }

    public List<Menu> create(final List<ApplicationMenu> menuList)
            throws ApplicationPageNotFoundException, SearchException {
        return collect(menuList, new RootMenuCollector());
    }

    private Menu create(final ApplicationMenu menu, final List<ApplicationMenu> menuList)
            throws ApplicationPageNotFoundException, SearchException {
        if (menu.getApplicationPageId() == null) {
            return new MenuContainer(menu,
                    collect(menuList, new ChildrenMenuCollector(menu.getId())));
        }
        return new MenuLink(menu, applicationApi.getApplicationPage(menu.getApplicationPageId()).getToken());
    }

    private List<Menu> collect(final List<ApplicationMenu> items, final Collector collector)
            throws ApplicationPageNotFoundException, SearchException {
        final List<Menu> menuList = new ArrayList<>();
        for (final ApplicationMenu item : items) {
            if (collector.isCollectible(item)) {
                menuList.add(create(item, items));
            }
        }
        return menuList;
    }
}
