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
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Emmanuel Duchastenier
 */
public class NodeToApplicationMenuConverter {

    private final ApplicationService applicationService;

    public NodeToApplicationMenuConverter(final ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * Convert an {@link ApplicationMenuNode} to
     * {@link SApplicationMenu}
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
