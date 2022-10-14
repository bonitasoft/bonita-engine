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
package org.bonitasoft.web.rest.model.bpm.cases;

import java.util.Date;

import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Fabio Lombardi
 */
public class ArchivedCaseDocumentItem extends CaseDocumentItem {

    public static final String ATTRIBUTE_SOURCE_OBJECT_ID = "sourceObjectId";

    public static final String ATTRIBUTE_ARCHIVED_DATE = "archivedDate";

    public static final String FILTER_ARCHIVED_CASE_ID = "archivedCaseId";

    public ArchivedCaseDocumentItem() {
        super();
    }

    public ArchivedCaseDocumentItem(final IItem item) {
        super(item);
    }

    // SETTER

    public void setArchivedDate(final Date date) {
        setAttribute(ATTRIBUTE_ARCHIVED_DATE, date);
    }

    public void setArchivedDate(final String date) {
        setAttribute(ATTRIBUTE_ARCHIVED_DATE, date);
    }

    public void setSourceObjectId(final Long id) {
        setAttribute(ATTRIBUTE_SOURCE_OBJECT_ID, id);
    }

    public void setSourceObjectId(final APIID id) {
        setAttribute(ATTRIBUTE_SOURCE_OBJECT_ID, id);
    }

    public void setSourceObjectId(final String id) {
        setAttribute(ATTRIBUTE_SOURCE_OBJECT_ID, id);
    }

    // GETTER

    @Override
    public ItemDefinition getItemDefinition() {
        return new ArchivedCaseDefinition();
    }

    public APIID getSourceObjectId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_SOURCE_OBJECT_ID);
    }

    public Date getArchivedDate() {
        return getAttributeValueAsDate(ATTRIBUTE_ARCHIVED_DATE);
    }

}
