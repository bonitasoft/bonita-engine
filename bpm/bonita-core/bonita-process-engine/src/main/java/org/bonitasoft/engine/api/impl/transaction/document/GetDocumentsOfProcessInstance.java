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

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.process.document.api.ProcessDocumentService;
import org.bonitasoft.engine.core.process.document.model.SProcessDocument;
import org.bonitasoft.engine.persistence.OrderByType;

/**
 * @author Nicolas Chabanoles
 */
public class GetDocumentsOfProcessInstance implements TransactionContentWithResult<List<SProcessDocument>> {

    private final ProcessDocumentService processDocumentService;

    private final long processInstanceId;

    private List<SProcessDocument> result;

    private final int pageIndex;

    private final int numberPerPage;

    private final String field;

    private final OrderByType order;

    public GetDocumentsOfProcessInstance(final ProcessDocumentService processDocumentService, final long processInstanceId, final int pageIndex,
            final int numberPerPage, final String field, final OrderByType order) {
        this.processDocumentService = processDocumentService;
        this.processInstanceId = processInstanceId;
        this.pageIndex = pageIndex;
        this.numberPerPage = numberPerPage;
        this.field = field;
        this.order = order;
    }

    @Override
    public void execute() throws SBonitaException {
        result = processDocumentService.getDocumentsOfProcessInstance(processInstanceId, pageIndex, numberPerPage, field, order);
    }

    @Override
    public List<SProcessDocument> getResult() {
        return result;
    }

}
