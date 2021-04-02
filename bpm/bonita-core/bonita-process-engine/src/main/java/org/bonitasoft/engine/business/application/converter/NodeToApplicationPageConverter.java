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
package org.bonitasoft.engine.business.application.converter;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.impl.validator.ApplicationImportValidator;
import org.bonitasoft.engine.business.application.importer.ApplicationPageImportResult;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.business.application.model.SApplicationWithIcon;
import org.bonitasoft.engine.business.application.xml.ApplicationPageNode;
import org.bonitasoft.engine.exception.ImportException;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Emmanuel Duchastenier
 */
public class NodeToApplicationPageConverter {

    private final PageService pageService;
    private final ApplicationImportValidator importValidator;

    public NodeToApplicationPageConverter(final PageService pageService,
            final ApplicationImportValidator importValidator) {
        this.pageService = pageService;
        this.importValidator = importValidator;
    }

    /**
     * @param applicationPageNode the XML node to convert to {@link SApplicationPage}
     * @param application the {@link SApplicationWithIcon} where the {@code SApplicationPage} will be attached
     * @return an ApplicationPageImportResult containing the converted {@code SApplicationPage} and an error (if any)
     */
    public ApplicationPageImportResult toSApplicationPage(ApplicationPageNode applicationPageNode,
            SApplicationWithIcon application) throws SBonitaReadException, ImportException {
        String token = applicationPageNode.getToken();
        importValidator.validate(token);
        long pageId = 0;
        ImportError importError = null;
        SPage page = pageService.getPageByName(applicationPageNode.getCustomPage());
        if (page != null) {
            pageId = page.getId();
        } else {
            importError = new ImportError(applicationPageNode.getCustomPage(), ImportError.Type.PAGE);
        }
        return new ApplicationPageImportResult(
                SApplicationPage.builder().applicationId(application.getId()).pageId(pageId).token(token).build(),
                importError);
    }

}
