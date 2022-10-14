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

import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasLastUpdateDate;

/**
 * @author Séverin Moussel
 */
public interface IActivityItem extends ItemHasLastUpdateDate, IFlowNodeItem {

    // // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ATTRIBUTES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    String ATTRIBUTE_REACHED_STATE_DATE = "reached_state_date";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FILTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    String FILTER_SUPERVISOR_ID = "supervisor_id";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SETTERS AND GETTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    void setReachStateDate(final String reachedStateDate);

    void setReachStateDate(final Date reachedStateDate);

    Date getReachStateDate();

    @Override
    void setLastUpdateDate(final String date);

    @Override
    void setLastUpdateDate(final Date date);

    @Override
    Date getLastUpdateDate();
}
