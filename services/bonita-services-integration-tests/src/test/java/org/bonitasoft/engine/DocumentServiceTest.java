/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine;

import org.bonitasoft.engine.document.DocumentService;
import org.junit.AfterClass;

/**
 * @author Emmanuel Duchastenier
 */
public class DocumentServiceTest extends CommonServiceTest {

    private static DocumentService documentService;

    static {
        documentService = getServicesBuilder().getInstanceOf(DocumentService.class);
    }

    @AfterClass
    public static void finalCleanUp() throws Exception {
        cleanUp();
        cleanPlatform();
    }

    public static void cleanUp() {
        // if (documentService instanceof CMISDocumentServiceImpl) {
        // ((CMISDocumentServiceImpl) documentService).clear();
        // }
    }

    /**
     * Get the DocumentationService object for the tests
     * 
     * @return the instance of the service for this test case
     */
    private DocumentService getDocumentationService() {
        return documentService;
    }

}
