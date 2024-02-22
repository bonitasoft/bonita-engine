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
package org.bonitasoft.web.rest.model.document;

import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;

/**
 * @author Gai Cuisha
 */
public class DocumentDefinition extends ItemDefinition {

    /**
     * Singleton
     */
    public static DocumentDefinition get() {
        return (DocumentDefinition) Definitions.get(TOKEN);
    }

    /**
     * token
     */
    public static final String TOKEN = "document";

    /**
     * the URL of document
     */
    private static final String API_URL = "../API/bpm/document";

    @Override
    public String defineToken() {
        return TOKEN;
    }

    @Override
    protected String defineAPIUrl() {
        return API_URL;
    }

    @Override
    protected void defineAttributes() {
        createAttribute(DocumentItem.DOCUMENT_ID, ItemAttribute.TYPE.STRING);
        createAttribute(DocumentItem.DOCUMENT_VERSION, ItemAttribute.TYPE.STRING);
        createAttribute(DocumentItem.PROCESSINSTANCE_ID, ItemAttribute.TYPE.STRING);
        createAttribute(DocumentItem.PROCESSINSTANCE_NAME, ItemAttribute.TYPE.STRING);
        createAttribute(DocumentItem.PROCESS_DISPLAY_NAME, ItemAttribute.TYPE.STRING);
        createAttribute(DocumentItem.PROCESS_VERSION, ItemAttribute.TYPE.STRING);
        createAttribute(DocumentItem.DOCUMENT_NAME, ItemAttribute.TYPE.STRING);
        createAttribute(DocumentItem.DOCUMENT_AUTHOR, ItemAttribute.TYPE.STRING);
        createAttribute(DocumentItem.DOCUMENT_CREATIONDATE, ItemAttribute.TYPE.STRING);
        createAttribute(DocumentItem.DOCUMENT_HAS_CONTENT, ItemAttribute.TYPE.STRING);
        createAttribute(DocumentItem.DOCUMENT_CONTENT_FILENAME, ItemAttribute.TYPE.STRING);
        createAttribute(DocumentItem.DOCUMENT_CONTENT_MIMETYPE, ItemAttribute.TYPE.STRING);
        createAttribute(DocumentItem.CONTENT_STORAGE_ID, ItemAttribute.TYPE.STRING);
        createAttribute(DocumentItem.DOCUMENT_URL, ItemAttribute.TYPE.STRING);
        createAttribute(DocumentItem.DOCUMENT_CREATION_TYPE, ItemAttribute.TYPE.STRING);
        createAttribute(DocumentItem.DOCUMENT_UPLOAD, ItemAttribute.TYPE.STRING);

    }

    @Override
    protected void definePrimaryKeys() {
        setPrimaryKeys(DocumentItem.DOCUMENT_ID);
    }

    @Override
    protected IItem _createItem() {
        return new DocumentItem();
    }
}
