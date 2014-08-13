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

import org.bonitasoft.engine.core.document.model.SDocumentMapping;
import org.bonitasoft.engine.core.document.model.archive.SADocumentMapping;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;

/**
 * @author Emmanuel Duchastenier
 * @author Nicolas Chabanoles
 */
public class SelectDescriptorBuilder {

    public static <T extends PersistentObject> SelectByIdDescriptor<T> getElementById(final Class<T> clazz, final String elementName, final long id) {
        return new SelectByIdDescriptor<T>("get" + elementName + "ById", clazz, id);
    }

    public static SelectListDescriptor<SDocumentMapping> getDocumentMappingsforProcessInstance(final long processInstanceId, final int fromIndex,
            final int maxResults, final String sortFieldOrder, final OrderByType orderBy) {
        QueryOptions queryOptions = null;
        String queryName = "getDocumentMappingsforProcessInstance";
        if (sortFieldOrder == null) {
            queryOptions = new QueryOptions(fromIndex, maxResults);
            queryName = "getDocumentMappingsforProcessInstanceOrderedById";
        } else {
            queryOptions = new QueryOptions(fromIndex, maxResults, SDocumentMapping.class, sortFieldOrder, orderBy);
        }
        final Map<String, Object> parameters = new HashMap<String, Object>(1);
        parameters.put("processInstanceId", processInstanceId);
        return new SelectListDescriptor<SDocumentMapping>(queryName, parameters, SDocumentMapping.class, queryOptions);
    }

    public static SelectOneDescriptor<Long> getNumberOfElement(final String elementName, final Class<? extends PersistentObject> clazz) {
        final Map<String, Object> emptyMap = Collections.emptyMap();
        return new SelectOneDescriptor<Long>("getNumberOf" + elementName, emptyMap, clazz, Long.class);
    }

    public static SelectListDescriptor<SDocumentMapping> getDocumentMappings(final int fromIndex, final int maxResults, final String sortFieldOrder,
            final OrderByType orderBy) {
        final Map<String, Object> emptyMap = Collections.emptyMap();
        QueryOptions queryOptions = null;
        if (sortFieldOrder == null) {
            queryOptions = new QueryOptions(fromIndex, maxResults);
        } else {
            queryOptions = new QueryOptions(fromIndex, maxResults, SDocumentMapping.class, sortFieldOrder, orderBy);
        }
        return new SelectListDescriptor<SDocumentMapping>("getDocumentMappings", emptyMap, SDocumentMapping.class, queryOptions);
    }

    public static SelectOneDescriptor<SDocumentMapping> getDocumentByName(final long processInstanceId, final String documentName) {
        final Map<String, Object> parameters = new HashMap<String, Object>(2);
        parameters.put("processInstanceId", processInstanceId);
        parameters.put("documentName", documentName);
        return new SelectOneDescriptor<SDocumentMapping>("getDocumentMappingsforProcessInstanceAndName", parameters, SDocumentMapping.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfDocumentMappingsforProcessInstance(final long processInstanceId) {
        final Map<String, Object> parameters = new HashMap<String, Object>(1);
        parameters.put("processInstanceId", processInstanceId);
        return new SelectOneDescriptor<Long>("getNumberOfDocumentMappingsforProcessInstance", parameters, SDocumentMapping.class);
    }

    public static SelectListDescriptor<SADocumentMapping> getArchivedDocumentByName(final long processInstanceId, final String documentName, final long time) {
        final Map<String, Object> parameters = new HashMap<String, Object>(3);
        parameters.put("processInstanceId", processInstanceId);
        parameters.put("documentName", documentName);
        parameters.put("time", time);
        return new SelectListDescriptor<SADocumentMapping>("getSADocumentMappingsforProcessInstanceAndName", parameters, SADocumentMapping.class,
                new QueryOptions(0, 1));
    }

    public static SelectByIdDescriptor<SADocumentMapping> getArchivedDocumentById(final long documentId) {
        return new SelectByIdDescriptor<SADocumentMapping>("getArchivedDocumentById", SADocumentMapping.class, documentId);
    }

    public static SelectOneDescriptor<SADocumentMapping> getArchivedVersionOdDocument(final long documentId) {
        final Map<String, Object> parameters = new HashMap<String, Object>(1);
        parameters.put("sourceObjectId", documentId);
        return new SelectOneDescriptor<SADocumentMapping>("getArchivedVersionOfDocument", parameters, SADocumentMapping.class);
    }
}
