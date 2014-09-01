/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.document.model.recorder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.core.document.model.SDocument;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.document.model.archive.SADocumentMapping;
import org.bonitasoft.engine.core.document.model.archive.SAMappedDocument;
import org.bonitasoft.engine.persistence.*;

/**
 * @author Emmanuel Duchastenier
 * @author Nicolas Chabanoles
 */
public class SelectDescriptorBuilder {

    public static <T extends PersistentObject> SelectByIdDescriptor<T> getElementById(final Class<T> clazz, final String elementName, final long id) {
        return new SelectByIdDescriptor<T>("get" + elementName + "ById", clazz, id);
    }

    public static SelectListDescriptor<SMappedDocument> getDocumentMappingsForProcessInstance(final long processInstanceId, final int fromIndex,
                                                                                              final int maxResults, final String sortFieldOrder, final OrderByType orderBy) {
        QueryOptions queryOptions = null;
        String queryName = "getSMappedDocumentOfProcess";
        if (sortFieldOrder == null) {
            queryOptions = new QueryOptions(fromIndex, maxResults);
            queryName = "getSMappedDocumentOfProcessOrderedById";
        } else {
            queryOptions = new QueryOptions(fromIndex, maxResults, SMappedDocument.class, sortFieldOrder, orderBy);
        }
        final Map<String, Object> parameters = new HashMap<String, Object>(1);
        parameters.put("processInstanceId", processInstanceId);
        return new SelectListDescriptor<SMappedDocument>(queryName, parameters, SMappedDocument.class, queryOptions);
    }

    public static SelectOneDescriptor<SMappedDocument> getSMappedDocumentOfProcessWithName(final long processInstanceId, final String documentName) {
        final Map<String, Object> parameters = new HashMap<String, Object>(2);
        parameters.put("processInstanceId", processInstanceId);
        parameters.put("name", documentName);
        return new SelectOneDescriptor<SMappedDocument>("getSMappedDocumentOfProcessWithName", parameters, SMappedDocument.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfSMappedDocumentOfProcess(final long processInstanceId) {
        final Map<String, Object> parameters = new HashMap<String, Object>(1);
        parameters.put("processInstanceId", processInstanceId);
        return new SelectOneDescriptor<Long>("getNumberOfSMappedDocumentOfProcess", parameters, SDocument.class);
    }

    public static SelectListDescriptor<SAMappedDocument> getSAMappedDocumentOfProcessWithName(final long processInstanceId, final String documentName, final long time) {
        final Map<String, Object> parameters = new HashMap<String, Object>(3);
        parameters.put("processInstanceId", processInstanceId);
        parameters.put("name", documentName);
        parameters.put("time", time);
        return new SelectListDescriptor<SAMappedDocument>("getSAMappedDocumentOfProcessWithName", parameters, SAMappedDocument.class,
                new QueryOptions(0, 1));
    }

    public static SelectByIdDescriptor<SAMappedDocument> getArchivedDocumentById(final long documentId) {
        return new SelectByIdDescriptor<SAMappedDocument>("getArchivedDocumentById", SAMappedDocument.class, documentId);
    }

    public static SelectOneDescriptor<SAMappedDocument> getArchivedVersionOdDocument(final long documentId) {
        final Map<String, Object> parameters = new HashMap<String, Object>(1);
        parameters.put("sourceObjectId", documentId);
        return new SelectOneDescriptor<SAMappedDocument>("getArchivedVersionOfDocument", parameters, SAMappedDocument.class);
    }
}
