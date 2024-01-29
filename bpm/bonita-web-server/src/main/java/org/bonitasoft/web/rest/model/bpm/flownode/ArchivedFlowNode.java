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
package org.bonitasoft.web.rest.model.bpm.flownode;

import java.util.Date;

import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Séverin Moussel
 */
public interface ArchivedFlowNode {

    String ATTRIBUTE_SOURCE_OBJECT_ID = "sourceObjectId";

    String FILTER_IS_TERMINAL = "isTerminal";

    String ATTRIBUTE_ARCHIVED_DATE = "archivedDate";

    void setArchivedDate(final String date);

    void setArchivedDate(final Date date);

    Date getArchivedDate();

    boolean isArchived();

    void setSourceObjectId(final APIID id);

    void setSourceObjectId(final String id);

    void setSourceObjectId(final Long id);

    APIID getSourceObjectId();

}
