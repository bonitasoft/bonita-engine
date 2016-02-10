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

import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.business.application.xml.ApplicationPageNode;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Emmanuel Duchastenier
 */
public class ApplicationPageToNodeConverter {

    private final PageService pageService;

    public ApplicationPageToNodeConverter(final PageService pageService) {
        this.pageService = pageService;
    }

    /**
     * @param page the application page to convert to xml node.
     * @return the converted page.
     * @throws SObjectNotFoundException if the referenced page does not exist.
     * @throws SBonitaReadException if the referenced page cannot be retrieved.
     */
    public ApplicationPageNode toPage(final SApplicationPage page) throws SBonitaReadException, SObjectNotFoundException {
        if (page == null) {
            throw new IllegalArgumentException("Application page to convert cannot be null");
        }
        final ApplicationPageNode pageNode = new ApplicationPageNode();
        pageNode.setToken(page.getToken());
        pageNode.setCustomPage(pageService.getPage(page.getPageId()).getName());
        return pageNode;
    }

}
