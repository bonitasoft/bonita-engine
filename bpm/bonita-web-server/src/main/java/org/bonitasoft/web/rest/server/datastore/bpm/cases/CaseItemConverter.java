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
package org.bonitasoft.web.rest.server.datastore.bpm.cases;

import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.web.rest.model.bpm.cases.CaseItem;
import org.bonitasoft.web.rest.server.datastore.converter.ItemConverter;

public class CaseItemConverter extends ItemConverter<CaseItem, ProcessInstance> {

    @Override
    public CaseItem convert(final ProcessInstance process) {
        final CaseItem item = new CaseItem();
        item.setId(process.getId());
        item.setLastUpdateDate(process.getLastUpdate());
        item.setState(process.getState());
        item.setStartDate(process.getStartDate());
        item.setEndDate(process.getEndDate());
        item.setProcessId(process.getProcessDefinitionId());
        item.setRootCaseId(process.getRootProcessInstanceId());
        item.setStartedByUserId(process.getStartedBy());
        item.setStartedBySubstituteUserId(process.getStartedBySubstitute());
        item.setSearchIndex1Label(process.getStringIndexLabel(1));
        item.setSearchIndex2Label(process.getStringIndexLabel(2));
        item.setSearchIndex3Label(process.getStringIndexLabel(3));
        item.setSearchIndex4Label(process.getStringIndexLabel(4));
        item.setSearchIndex5Label(process.getStringIndexLabel(5));
        item.setSearchIndex1Value(process.getStringIndex1());
        item.setSearchIndex2Value(process.getStringIndex2());
        item.setSearchIndex3Value(process.getStringIndex3());
        item.setSearchIndex4Value(process.getStringIndex4());
        item.setSearchIndex5Value(process.getStringIndex5());
        return item;
    }

}
