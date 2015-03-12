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
package org.bonitasoft.engine.core.process.definition.model.bindings;

import java.util.Map;

import org.bonitasoft.engine.core.process.definition.model.SDocumentDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SDocumentDefinitionImpl;

/**
 * @author Baptiste Mesta
 */
public class SDocumentDefinitionBinding extends SNamedElementBinding {

    private String description;

    private String mimeType;

    private String url;

    private String file;

    private String fileName;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        super.setAttributes(attributes);
        mimeType = attributes.get(XMLSProcessDefinition.DOCUMENT_DEFINITION_MIME_TYPE);
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
        if (XMLSProcessDefinition.DOCUMENT_DEFINITION_FILE_NAME.equals(name)) {
            fileName = value;
        }
        if (XMLSProcessDefinition.DESCRIPTION.equals(name)) {
            description = value;
        }
        if (XMLSProcessDefinition.DOCUMENT_DEFINITION_URL.equals(name)) {
            url = value;
        }
        if (XMLSProcessDefinition.DOCUMENT_DEFINITION_FILE.equals(name)) {
            file = value;
        }
    }

    @Override
    public void setChildObject(final String name, final Object value) {
    }

    @Override
    public SDocumentDefinition getObject() {
        final SDocumentDefinitionImpl documentDefinitionImpl = new SDocumentDefinitionImpl(name);
        documentDefinitionImpl.setMimeType(mimeType);
        if (description != null) {
            documentDefinitionImpl.setDescription(description);
        }
        if (url != null) {
            documentDefinitionImpl.setUrl(url);
        }
        if (file != null) {
            documentDefinitionImpl.setFile(file);
        }
        if (fileName != null) {
            documentDefinitionImpl.setFileName(fileName);
        }
        return documentDefinitionImpl;
    }

    @Override
    public String getElementTag() {
        return XMLSProcessDefinition.DOCUMENT_DEFINITION_NODE;
    }
}
