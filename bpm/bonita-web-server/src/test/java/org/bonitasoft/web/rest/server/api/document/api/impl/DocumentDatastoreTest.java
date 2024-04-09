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
package org.bonitasoft.web.rest.server.api.document.api.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.File;

import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.document.DocumentItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Julien Mege
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentDatastoreTest {

    @Mock
    private APISession session;

    @Mock
    private ProcessAPI processAPI;

    private DocumentDatastore documentDatastore;

    @Mock
    private BonitaHomeFolderAccessor tenantFolder;

    @Test
    public void should_verify_authorisation_for_the_given_document_path() throws Exception {
        documentDatastore = spy(new DocumentDatastore(session));
        doReturn(processAPI).when(documentDatastore).getProcessAPI();
        doReturn(new File("doc.txt")).when(tenantFolder).getTempFile("docPath");

        final DocumentItem item = documentDatastore.createDocument(1L, "docName", "docType", "docPath", tenantFolder);
        assertNotNull(item);
    }

}
