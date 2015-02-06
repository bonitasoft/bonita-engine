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
package org.bonitasoft.engine.bpm.process.impl;

import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.impl.DocumentImpl;

/**
 * @author Nicolas Chabanoles
 */
public class DocumentBuilder {

    private DocumentImpl document;

    /**
     * Create a new instance of {@link Document}. This method is the entry point of this builder: it must be called before any other method.
     * @param documentName document name
     * @param hasContent defines whether the document has content or not
     * @return
     */
    public DocumentBuilder createNewInstance(final String documentName, final boolean hasContent) {
        document = new DocumentImpl();
        document.setName(documentName);
        document.setHasContent(hasContent);
        return this;
    }

    public Document done() {
        return document;
    }

    /**
     * Sets the document file name
     * @param fileName file name
     * @return
     */
    public DocumentBuilder setFileName(final String fileName) {
        document.setFileName(fileName);
        return this;
    }

    /**
     * Sets the document URL
     * @param documentUrl document URL
     * @return
     */
    public DocumentBuilder setURL(final String documentUrl) {
        document.setUrl(documentUrl);
        return this;
    }

    /**
     * Sets the description of the document
     * @param description the description of the document
     * @return
     */
    public DocumentBuilder setDescription(final String description){
        document.setDescription(description);
        return this;
    }



    /**
     * Sets the document MIME type
     * @param documentContentMimeType document MIME type
     * @return
     */
    public DocumentBuilder setContentMimeType(final String documentContentMimeType) {
        document.setContentMimeType(documentContentMimeType);
        return this;
    }

}
