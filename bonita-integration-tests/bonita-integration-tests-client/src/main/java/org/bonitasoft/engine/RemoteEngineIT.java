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
package org.bonitasoft.engine;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.bonitasoft.engine.bpm.flownode.FlowNodeInstanceNotFoundException;
import org.junit.Test;

/**
 * Test all things related to remote connection
 *
 * @author Baptiste Mesta
 */
public class RemoteEngineIT extends TestWithTechnicalUser {

    /*
     * check that we return the stack server exception but not the server exception itself that is not known to the client
     */
    @Test
    public void check_remote_exception_is_given_to_client() {
        try {
            getProcessAPI().getFlowNodeInstance(123456789L);
            fail("should fail");
        } catch (final FlowNodeInstanceNotFoundException e) {
            e.printStackTrace();
            assertTrue(containsStack(e.getStackTrace(), "SFlowNodeNotFoundException"));
            assertNull(e.getCause());
        }
    }

    private boolean containsStack(final StackTraceElement[] stackTrace, final String string) {
        for (final StackTraceElement stackTraceElement : stackTrace) {
            if (stackTraceElement.getClassName().contains(string)) {
                return true;
            }
        }
        return false;
    }
}
