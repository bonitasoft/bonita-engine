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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.bpm.document.ArchivedDocumentsSearchDescriptor;
import org.bonitasoft.web.rest.model.bpm.cases.ArchivedCaseDocumentItem;
import org.bonitasoft.web.rest.server.datastore.converter.AttributeConverter;
import org.bonitasoft.web.toolkit.client.common.util.MapUtil;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;

/**
 * @author Fabio Lombardi
 */
public class ArchivedCaseDocumentSearchAttributeConverter implements AttributeConverter {

    private final Map<String, String> mapping;

    public ArchivedCaseDocumentSearchAttributeConverter() {
        mapping = createMapping();
    }

    private Map<String, String> createMapping() {
        final Map<String, String> mapping = new HashMap<>();
        mapping.put(ArchivedCaseDocumentItem.ATTRIBUTE_ID, ArchivedCaseDocumentItem.ATTRIBUTE_ID);
        mapping.put(ArchivedCaseDocumentItem.ATTRIBUTE_SUBMITTED_BY_USER_ID,
                ArchivedDocumentsSearchDescriptor.DOCUMENT_AUTHOR);
        mapping.put(ArchivedCaseDocumentItem.ATTRIBUTE_NAME, ArchivedDocumentsSearchDescriptor.DOCUMENT_NAME);
        mapping.put(ArchivedCaseDocumentItem.ATTRIBUTE_CREATION_DATE,
                ArchivedDocumentsSearchDescriptor.DOCUMENT_CREATIONDATE);
        mapping.put(ArchivedCaseDocumentItem.ATTRIBUTE_DESCRIPTION,
                ArchivedDocumentsSearchDescriptor.DOCUMENT_DESCRIPTION);
        mapping.put(ArchivedCaseDocumentItem.ATTRIBUTE_INDEX, ArchivedDocumentsSearchDescriptor.LIST_INDEX);
        mapping.put(ArchivedCaseDocumentItem.ATTRIBUTE_CASE_ID, ArchivedDocumentsSearchDescriptor.PROCESSINSTANCE_ID);
        mapping.put(ArchivedCaseDocumentItem.ATTRIBUTE_SOURCE_OBJECT_ID,
                ArchivedDocumentsSearchDescriptor.SOURCEOBJECT_ID);
        mapping.put(ArchivedCaseDocumentItem.ATTRIBUTE_ARCHIVED_DATE, ArchivedDocumentsSearchDescriptor.ARCHIVE_DATE);
        return mapping;
    }

    @Override
    public String convert(final String attribute) {
        return MapUtil.getMandatory(mapping, attribute);
    }

    @Override
    public Map<String, ItemAttribute.TYPE> getValueTypeMapping() {
        return Collections.emptyMap();
    }

}
