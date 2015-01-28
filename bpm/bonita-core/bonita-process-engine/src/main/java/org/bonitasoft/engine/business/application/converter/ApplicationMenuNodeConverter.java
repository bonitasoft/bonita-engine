/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.business.application.converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.importer.ApplicationMenuImportResult;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.business.application.model.builder.SApplicationMenuBuilder;
import org.bonitasoft.engine.business.application.model.builder.SApplicationMenuBuilderFactory;
import org.bonitasoft.engine.business.application.xml.ApplicationMenuNode;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Emmanuel Duchastenier
 */
public class ApplicationMenuNodeConverter {

    private final ApplicationService applicationService;

    public ApplicationMenuNodeConverter(final ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * RECURSIVELY convert menu (and sub-menus) to xml node.
     *
     * @param menu the menu to convert
     * @return the converted menu.
     * @throws SObjectNotFoundException if the referenced menu does not exist.
     * @throws SBonitaReadException if the referenced menu cannot be retrieved.
     */
    protected ApplicationMenuNode toMenu(final SApplicationMenu menu) throws SBonitaReadException, SObjectNotFoundException {
        if (menu == null) {
            throw new IllegalArgumentException("Application menu to convert cannot be null");
        }
        final ApplicationMenuNode menuNode = new ApplicationMenuNode();
        // applicationPage attribute in the menu is the token in the referenced application page:
        final Long applicationPageId = menu.getApplicationPageId();
        if (applicationPageId != null) {
            menuNode.setApplicationPage(applicationService.getApplicationPage(applicationPageId).getToken());
        }
        menuNode.setDisplayName(menu.getDisplayName());
        return menuNode;
    }

    /**
     * @param applicationId application ID.
     * @param parentMenuId Id of the parent menu, use <code>null</code> for explicit no parent.
     * @param startIndex pagination start index.
     * @param maxResults pagination max results to retrieve.
     * @return the newly built {@link QueryOptions}
     */
    protected QueryOptions buildApplicationMenusQueryOptions(final long applicationId, final Long parentMenuId, final int startIndex, final int maxResults) {
        final SApplicationMenuBuilderFactory factory = BuilderFactory.get(SApplicationMenuBuilderFactory.class);
        final List<OrderByOption> orderByOptions = Collections.singletonList(new OrderByOption(SApplicationMenu.class, factory.getIndexKey(), OrderByType.ASC));

        final List<FilterOption> filters = Arrays.asList(new FilterOption(SApplicationMenu.class, factory.getApplicationIdKey(), applicationId),
                new FilterOption(SApplicationMenu.class, factory.getParentIdKey(), parentMenuId));

        return new QueryOptions(startIndex, maxResults, orderByOptions, filters, null);
    }

    /**
     * RECURSIVELY add menu elements (and sub-menus) to xml node, from menu identified by parentMenuId.
     *
     * @param applicationId ID of the application.
     * @param parentMenuId Id of the parent menu, use <code>null</code> for explicit no parent.
     * @param applicationNode
     * @param menuNode the menu node to add new menu elements to. Pass null if new menu node must be added to root application node.
     * @throws SBonitaReadException
     * @throws SObjectNotFoundException
     */
    public void addMenusToApplicationNode(final long applicationId, final Long parentMenuId, final ApplicationNode applicationNode,
            final ApplicationMenuNode menuNode)
            throws SBonitaReadException, SObjectNotFoundException {
        int startIndex = 0;
        final int maxResults = 50;
        List<SApplicationMenu> menus;
        do {
            menus = applicationService.searchApplicationMenus(buildApplicationMenusQueryOptions(applicationId, parentMenuId, startIndex, maxResults));
            for (final SApplicationMenu menu : menus) {
                // Add converted current menu...
                final ApplicationMenuNode menuNode2 = toMenu(menu);
                if (menuNode == null) {
                    applicationNode.addApplicationMenu(menuNode2);
                } else {
                    menuNode.addApplicationMenu(menuNode2);
                }
                // ... and recursively add sub-menu:
                addMenusToApplicationNode(applicationId, menu.getId(), applicationNode, menuNode2);
            }
            startIndex += maxResults;
        } while (menus.size() == maxResults);
    }

    /**
     * Convert an {@link org.bonitasoft.engine.business.application.xml.ApplicationMenuNode} to
     * {@link org.bonitasoft.engine.business.application.model.SApplicationMenu}
     * 
     * @param applicationMenuNode the XML node to convert
     * @param application the application where the menu will be attached to
     * @param parentMenu the parent menu. Null if no parent
     * @return the application where the menu will be attached to
     * @throws SBonitaReadException
     */
    public ApplicationMenuImportResult toSApplicationMenu(ApplicationMenuNode applicationMenuNode, SApplication application, SApplicationMenu parentMenu)
            throws SBonitaReadException {
        Long appPageId = null;
        ImportError error = null;
        if (applicationMenuNode.getApplicationPage() != null) {
            try {
                SApplicationPage applicationPage = applicationService.getApplicationPage(application.getToken(), applicationMenuNode.getApplicationPage());
                appPageId = applicationPage.getId();
            } catch (SObjectNotFoundException e) {
                error = new ImportError(applicationMenuNode.getApplicationPage(), ImportError.Type.APPLICATION_PAGE);
            }
        }
        int index = getIndex(parentMenu);
        SApplicationMenu applicationMenu = buildApplicationMenu(applicationMenuNode, application, parentMenu, appPageId, index);
        return new ApplicationMenuImportResult(error, applicationMenu);
    }

    private int getIndex(final SApplicationMenu parentMenu) throws SBonitaReadException {
        int index;
        if (parentMenu == null) {
            index = applicationService.getNextAvailableIndex(null);
        } else {
            index = applicationService.getNextAvailableIndex(parentMenu.getId());
        }
        return index;
    }

    private SApplicationMenu buildApplicationMenu(final ApplicationMenuNode applicationMenuNode, final SApplication application,
            final SApplicationMenu parentMenu, final Long appPageId, final int index) {
        SApplicationMenuBuilderFactory factory = BuilderFactory.get(SApplicationMenuBuilderFactory.class);
        SApplicationMenuBuilder builder = factory.createNewInstance(applicationMenuNode.getDisplayName(), application.getId(), appPageId, index);
        if (parentMenu != null) {
            builder.setParentId(parentMenu.getId());
        }
        return builder.done();
    }

}
