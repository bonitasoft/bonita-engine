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
package org.bonitasoft.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstanceNotFoundException;
import org.bonitasoft.engine.util.APITypeManager;
import org.junit.Test;

/**
 * Test all things related to remote connection
 *
 * @author Baptiste Mesta
 */
public class RemoteEngineIT extends TestWithTechnicalUser {

    /*
     * check that we return the stack server exception but not the server exception itself that is not known to the
     * client
     */
    @Test
    public void check_remote_exception_is_given_to_client() throws Exception {
        try {
            getProcessAPI().getFlowNodeInstance(123456789L);
            fail("should fail");
        } catch (final FlowNodeInstanceNotFoundException e) {
            //in local, check root cause is here
            if (APITypeManager.getAPIType() == ApiAccessType.LOCAL) {
                Throwable rootCause = e;
                while (rootCause.getCause() != null) {
                    rootCause = rootCause.getCause();
                }
                assertThat(rootCause.getClass().getSimpleName()).isEqualTo("SFlowNodeNotFoundException");
            } else {
                //in remote, check the stack trace is preserved
                assertThat(e.getStackTrace()).anyMatch(s -> s.getClassName().contains("SFlowNodeNotFoundException"));
                assertThat(e.getCause()).isNull();
            }
        }

    }
}
