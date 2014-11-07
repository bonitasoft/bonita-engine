/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.converter;

import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.persistence.SBonitaReadException;

import com.bonitasoft.engine.business.application.model.SApplicationPage;
import com.bonitasoft.engine.business.application.xml.ApplicationPageNode;
import com.bonitasoft.engine.page.PageService;

/**
 * @author Emmanuel Duchastenier
 */
public class ApplicationPageNodeConverter {

    private final PageService pageService;

    public ApplicationPageNodeConverter(final PageService pageService) {
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
