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
package org.bonitasoft.engine.api.impl.transaction.document;

import java.util.Date;

import org.bonitasoft.engine.api.impl.transaction.process.GetArchivedProcessInstanceList;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAProcessInstanceBuilderFactory;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class GetDocumentByNameAtProcessInstantiation implements TransactionContentWithResult<SMappedDocument> {

    private final DocumentService documentService;

    private final ProcessInstanceService processInstanceService;

    private final SearchEntitiesDescriptor searchEntitiesDescriptor;

    private final long processInstanceId;

    private SMappedDocument result;

    private final String documentName;

    private final ProcessDefinitionService processDefinitionService;

    public GetDocumentByNameAtProcessInstantiation(final DocumentService documentService, final ProcessInstanceService processInstanceService,
            final ProcessDefinitionService processDefinitionService, final SearchEntitiesDescriptor searchEntitiesDescriptor, final long processInstanceId,
            final String documentName) {
        this.documentService = documentService;
        this.processDefinitionService = processDefinitionService;
        this.processInstanceId = processInstanceId;
        this.documentName = documentName;
        this.processInstanceService = processInstanceService;
        this.searchEntitiesDescriptor = searchEntitiesDescriptor;
    }

    @Override
    public void execute() throws SBonitaException {
        final GetArchivedProcessInstanceList getArchivedProcessInstanceList = new GetArchivedProcessInstanceList(processInstanceService,
                processDefinitionService, searchEntitiesDescriptor, processInstanceId, 0, 1, BuilderFactory.get(SAProcessInstanceBuilderFactory.class)
                        .getIdKey(), OrderByType.ASC);
        getArchivedProcessInstanceList.execute();
        final ArchivedProcessInstance saProcessInstance = getArchivedProcessInstanceList.getResult().get(0);
        final Date startDate = saProcessInstance.getStartDate();
        final long startTime = startDate != null ? startDate.getTime() : 0;
        result = documentService.getMappedDocument(processInstanceId, documentName, startTime);
    }

    @Override
    public SMappedDocument getResult() {
        return result;
    }

}
