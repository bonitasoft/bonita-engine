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
package org.bonitasoft.engine.core.process.instance.model.builder.impl;

import org.bonitasoft.engine.core.process.instance.model.builder.SHumanTaskInstanceBuilderFactory;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public abstract class SHumanTaskInstanceBuilderFactoryImpl extends SActivityInstanceBuilderFactoryImpl implements SHumanTaskInstanceBuilderFactory {

    private static final String ASSIGNEE_ID_KEY = "assigneeId";

    private static final String CLAIMED_DATE = "claimedDate";

    private static final String EXPECTED_END_DATE_KEY = "expectedEndDate";

    private static final String PRIORITY_KEY = "priority";

    @Override
    public String getAssigneeIdKey() {
        return ASSIGNEE_ID_KEY;
    }

    @Override
    public String getClaimedDateKey() {
        return CLAIMED_DATE;
    }

    @Override
    public String getPriorityKey() {
        return PRIORITY_KEY;
    }

    @Override
    public String getExpectedEndDateKey() {
        return EXPECTED_END_DATE_KEY;
    }

}
