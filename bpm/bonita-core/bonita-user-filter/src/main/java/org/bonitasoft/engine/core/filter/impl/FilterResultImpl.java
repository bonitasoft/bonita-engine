/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.core.filter.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.bonitasoft.engine.core.filter.FilterResult;

/**
 * @author Baptiste Mesta
 */
public class FilterResultImpl implements FilterResult {

    private static final long serialVersionUID = -6952608023861384532L;

    private final List<Long> result;

    private final boolean shouldAutoAssignTaskIfSingleResult;

    public FilterResultImpl(final List<Long> userIds, final boolean shouldAutoAssignTaskIfSingleResult) {
        //To avoid errors due to duplicates see BS-16189
        if (userIds != null) {
            result = new ArrayList<>(new LinkedHashSet<>(userIds));
        } else {
            result = null;
        }
        this.shouldAutoAssignTaskIfSingleResult = shouldAutoAssignTaskIfSingleResult;
    }

    @Override
    public List<Long> getResult() {
        return result;
    }

    @Override
    public boolean shouldAutoAssignTaskIfSingleResult() {
        return shouldAutoAssignTaskIfSingleResult;
    }

}
