/**
 * Copyright (C) 2011, 2013 BonitaSoft S.A.
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

import java.util.Date;

import org.bonitasoft.engine.api.impl.transaction.process.GetArchivedProcessInstanceList;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.process.document.api.DocumentService;
import org.bonitasoft.engine.core.process.document.mapping.model.SDocumentMapping;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAProcessInstanceBuilderFactory;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class GetDocumentByNameAtProcessInstantiation implements TransactionContentWithResult<SDocumentMapping> {

    private final DocumentService documentService;

    private final ProcessInstanceService processInstanceService;

    private final SearchEntitiesDescriptor searchEntitiesDescriptor;

    private final long processInstanceId;

    private SDocumentMapping result;

    private final String documentName;

    public GetDocumentByNameAtProcessInstantiation(final DocumentService documentService, final ProcessInstanceService processInstanceService,
            final SearchEntitiesDescriptor searchEntitiesDescriptor, final long processInstanceId,
            final String documentName) {
        this.documentService = documentService;
        this.processInstanceId = processInstanceId;
        this.documentName = documentName;
        this.processInstanceService = processInstanceService;
        this.searchEntitiesDescriptor = searchEntitiesDescriptor;
    }

    @Override
    public void execute() throws SBonitaException {
        final GetArchivedProcessInstanceList getArchivedProcessInstanceList = new GetArchivedProcessInstanceList(processInstanceService,
                searchEntitiesDescriptor, processInstanceId, 0, 1, BuilderFactory.get(SAProcessInstanceBuilderFactory.class).getIdKey(), OrderByType.ASC);
        getArchivedProcessInstanceList.execute();
        final ArchivedProcessInstance saProcessInstance = getArchivedProcessInstanceList.getResult().get(0);
        final Date startDate = saProcessInstance.getStartDate();
        final long startTime = startDate != null ? startDate.getTime() : 0;
        result = documentService.getDocument(processInstanceId, documentName, startTime);
    }

    @Override
    public SDocumentMapping getResult() {
        return result;
    }

}
