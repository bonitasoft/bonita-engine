/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.external.permission;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 */
@RunWith(MockitoJUnitRunner.class)
public class IsInvolvedInHumanTaskTest {

    @Mock
    TenantServiceAccessor serviceAccessor;

    /**
     * Test method for
     * {@link org.bonitasoft.engine.external.actor.IsInvolvedInHumanTask#execute(java.util.Map, org.bonitasoft.engine.service.TenantServiceAccessor)} .
     */
    @Test(expected = SCommandParameterizationException.class)
    public final void should_throw_exception_when_Execute_with_wrong_parameter() throws SCommandParameterizationException, SCommandExecutionException {
        // Given
        final IsInvolvedInHumanTask isInvolvedInHumanTask = new IsInvolvedInHumanTask();
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("BAD_PARAMETER", "aa");

        // When
        isInvolvedInHumanTask.execute(parameters, serviceAccessor);
    }

    @Test(expected = SCommandParameterizationException.class)
    public final void should_throw_exception_when_Execute_with_non_existent_user() throws SCommandParameterizationException, SCommandExecutionException {
        // Given
        final IsInvolvedInHumanTask isInvolvedInHumanTask = new IsInvolvedInHumanTask();
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("USER_ID_KEY", -1l);

        // When
        isInvolvedInHumanTask.execute(parameters, serviceAccessor);
    }

}
