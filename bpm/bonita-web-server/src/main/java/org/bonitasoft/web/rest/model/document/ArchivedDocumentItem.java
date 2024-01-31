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
package org.bonitasoft.web.rest.model.document;

import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * Document item
 *
 * @author Gai Cuisha
 */
public class ArchivedDocumentItem extends DocumentItem {

    public static final String SOURCEOBJECT_ID = "sourceObjectId";

    public static final String ARCHIVED_DATE = "archivedDate";

    public ArchivedDocumentItem() {
        super();
    }

    public ArchivedDocumentItem(final IItem item) {
        super(item);
    }

    public void setDocumentSourceObjectId(final String sourceObjectId) {
        this.setAttribute(SOURCEOBJECT_ID, sourceObjectId);
    }

    public void setArchivedDate(final String archivedDate) {
        this.setAttribute(ARCHIVED_DATE, archivedDate);
    }

    @Override
    public ItemDefinition getItemDefinition() {
        return new ArchivedDocumentDefinition();
    }
}
