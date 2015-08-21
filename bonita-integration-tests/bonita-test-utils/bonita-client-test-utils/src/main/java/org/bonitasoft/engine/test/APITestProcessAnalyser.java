/*
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
 */

package org.bonitasoft.engine.test;

import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.session.InvalidSessionException;

/**
 * @author mazourd
 */
public interface APITestProcessAnalyser {

    void checkFlowNodeWasntExecuted(final long processInstancedId, final String flowNodeName);

    void checkWasntExecuted(final ProcessInstance parentProcessInstance, final String flowNodeName) throws InvalidSessionException, SearchException;

    void updateActivityInstanceVariablesWithOperations(final String updatedValue, final long activityInstanceId, final String dataName,
            final boolean isTransient)
            throws InvalidExpressionException, UpdateException;

}
