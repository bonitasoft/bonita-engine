/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl.transaction.document;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.process.document.api.ProcessDocumentService;
import org.bonitasoft.engine.core.process.document.model.SProcessDocument;

/**
 * @author Nicolas Chabanoles
 */
public class GetDocument implements TransactionContentWithResult<SProcessDocument> {

    private final ProcessDocumentService processDocumentService;

    private final long documentId;

    private SProcessDocument result;

    public GetDocument(final ProcessDocumentService processDocumentService, final long documentId) {
        this.processDocumentService = processDocumentService;
        this.documentId = documentId;
    }

    @Override
    public void execute() throws SBonitaException {
        result = processDocumentService.getDocument(documentId);
    }

    @Override
    public SProcessDocument getResult() {
        return result;
    }

}
