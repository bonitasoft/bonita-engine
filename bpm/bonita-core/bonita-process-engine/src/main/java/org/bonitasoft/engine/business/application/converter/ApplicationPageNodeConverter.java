/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package org.bonitasoft.engine.business.application.converter;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.business.application.importer.ApplicationPageImportResult;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.business.application.model.builder.SApplicationPageBuilder;
import org.bonitasoft.engine.business.application.model.builder.SApplicationPageBuilderFactory;
import org.bonitasoft.engine.business.application.xml.ApplicationPageNode;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.persistence.SBonitaReadException;

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

    /**
     * @param applicationPageNode the XML node to convert to {@link org.bonitasoft.engine.business.application.model.SApplicationPage}
     * @param application the {@link org.bonitasoft.engine.business.application.model.SApplication} where the {@code SApplicationPage} will be attached
     * @return an ApplicationPageImportResult containing the converted {@code SApplicationPage} and an error (if any)
     */
    public ApplicationPageImportResult toSApplicationPage(ApplicationPageNode applicationPageNode, SApplication application) throws SBonitaReadException {
        long pageId = 0;
        ImportError importError = null;
        SPage page = pageService.getPageByName(applicationPageNode.getCustomPage());
        if (page != null) {
            pageId = page.getId();
        } else {
            importError = new ImportError(applicationPageNode.getCustomPage(), ImportError.Type.PAGE);
        }
        SApplicationPageBuilderFactory factory = BuilderFactory.get(SApplicationPageBuilderFactory.class);
        SApplicationPageBuilder builder = factory.createNewInstance(application.getId(), pageId, applicationPageNode.getToken());
        ApplicationPageImportResult importResult = new ApplicationPageImportResult(builder.done(), importError);
        return importResult;
    }

}
