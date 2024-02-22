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
package org.bonitasoft.web.rest.server.api.document;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.document.DocumentItem;
import org.bonitasoft.web.rest.server.api.document.api.impl.DocumentDatastore;
import org.bonitasoft.web.rest.server.framework.APIServletCall;
import org.bonitasoft.web.toolkit.client.ItemDefinitionFactory;
import org.bonitasoft.web.toolkit.client.common.CommonDateFormater;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIForbiddenException;
import org.bonitasoft.web.toolkit.server.utils.ServerDateFormater;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Julien Mege
 */
@RunWith(MockitoJUnitRunner.class)
public class APIDocumentTest {

    @Mock
    APIServletCall caller;

    @Mock
    private HttpSession session;

    @Mock
    DocumentItem item;

    APIDocument apiDocument;

    @Mock
    private ItemDefinitionFactory factory;

    @Mock
    private DocumentDatastore documentDatastore;

    @Mock
    private APISession engineSession;

    @Before
    public void setup() throws Exception {
        ItemDefinitionFactory.setDefaultFactory(factory);
        I18n.getInstance();
        CommonDateFormater.setDateFormater(new ServerDateFormater());
        apiDocument = spy(new APIDocument());
        apiDocument.setCaller(caller);
        doReturn(documentDatastore).when(apiDocument).getDataStore();
        doReturn("../../..").when(item).getAttributeValue(DocumentItem.DOCUMENT_UPLOAD);
        doReturn("doc").when(item).getAttributeValue(DocumentItem.DOCUMENT_NAME);
        doReturn("1").when(item).getAttributeValue(DocumentItem.PROCESSINSTANCE_ID);
        doReturn("type").when(item).getAttributeValue(DocumentItem.DOCUMENT_CREATION_TYPE);

    }

    @Test(expected = APIException.class)
    public void it_throws_an_exception_when_cannot_write_file_on_add() throws Exception {
        // Given
        doThrow(new IOException("error")).when(documentDatastore).createDocument(any(Long.class), any(String.class),
                any(String.class),
                any(String.class), any(BonitaHomeFolderAccessor.class));

        // When
        try {
            apiDocument.add(item);
        } catch (final APIForbiddenException e) {
            fail("Don't expect the APIException to be an APIForbiddenException");
        }

    }
}
