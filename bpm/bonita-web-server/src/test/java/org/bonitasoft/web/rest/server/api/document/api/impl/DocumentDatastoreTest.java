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
import static org.mockito.Mockito.*;

import java.io.InputStream;

import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.io.FileContent;
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

    @Mock
    private Document document;

    @Mock
    private ProcessInstance processInstance;

    @Mock
    private ProcessDeploymentInfo processDeploymentInfo;

    private DocumentDatastore documentDatastore;

    @Mock
    private BonitaHomeFolderAccessor tenantFolder;

    @Test
    public void should_retrieve_temporary_file() throws Exception {
        documentDatastore = spy(new DocumentDatastore(session));
        doReturn(processAPI).when(documentDatastore).getProcessAPI();
        FileContent fileContent = new FileContent("doc.txt", InputStream.nullInputStream(), "text/plain");
        doReturn(fileContent).when(tenantFolder).retrieveUploadedTempContent("docKey");
        doReturn(1L).when(document).getProcessInstanceId();
        doReturn(document).when(processAPI).attachDocument(1L, "docName", "doc.txt", "text/plain", new byte[0]);
        doReturn(processInstance).when(processAPI).getProcessInstance(1L);
        doReturn(2L).when(processInstance).getProcessDefinitionId();
        doReturn(processDeploymentInfo).when(processAPI).getProcessDeploymentInfo(2L);

        final DocumentItem item = documentDatastore.createDocument(1L, "docName", DocumentDatastore.CREATE_NEW_DOCUMENT,
                "docKey", tenantFolder);

        assertNotNull(item);
        verify(processAPI).attachDocument(1L, "docName", "doc.txt", "text/plain", new byte[0]);
    }

}
