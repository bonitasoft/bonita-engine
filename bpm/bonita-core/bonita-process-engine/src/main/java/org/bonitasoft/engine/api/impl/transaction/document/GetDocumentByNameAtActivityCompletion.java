/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.archive.SAActivityInstance;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class GetDocumentByNameAtActivityCompletion implements TransactionContentWithResult<SProcessDocument> {

    private final ProcessDocumentService processDocumentService;

    private final long activityInstanceId;

    private SProcessDocument result;

    private final String documentName;

    private final ActivityInstanceService activityInstanceService;

    public GetDocumentByNameAtActivityCompletion(final ProcessDocumentService processDocumentService, final long activityInstanceId, final String documentName,
            final ActivityInstanceService activityInstanceService) {
        this.processDocumentService = processDocumentService;
        this.activityInstanceId = activityInstanceId;
        this.documentName = documentName;
        this.activityInstanceService = activityInstanceService;
    }

    @Override
    public void execute() throws SBonitaException {
        final SAActivityInstance aactivity = activityInstanceService.getArchivedActivityInstance(activityInstanceId);
        result = processDocumentService.getDocument(aactivity.getRootContainerId(), documentName, aactivity.getArchiveDate());
    }

    @Override
    public SProcessDocument getResult() {
        return result;
    }

}
