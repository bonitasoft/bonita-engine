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
package org.bonitasoft.engine.filter.user;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.filter.AbstractUserFilter;

/**
 * @author Baptiste Mesta
 */
public class TestFilterUsingActorName extends AbstractUserFilter {

    @Override
    public List<Long> filter(final String actorName) {
        @SuppressWarnings("unchecked")
        final Map<String, Long> map = (Map<String, Long>) getInputParameter("userIds");
        return Arrays.asList(map.get(actorName));
    }

    @Override
    public void validateInputParameters() {

    }

    @Override
    public boolean shouldAutoAssignTaskIfSingleResult() {
        return false;
    }

}
