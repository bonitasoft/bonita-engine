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
package org.bonitasoft.engine.sessionaccessor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ThreadLocalSessionAccessorTest {

    private final ThreadLocalSessionAccessor threadLocalSessionAccessor = new ThreadLocalSessionAccessor();

    @Test
    public void should_set_session_id() throws Exception {
        threadLocalSessionAccessor.setSessionId(12);

        assertThat(threadLocalSessionAccessor.getSessionId()).isEqualTo(12);
    }

    @Test(expected = SessionIdNotSetException.class)
    public void should_throw_SessionIfNotSet_when_session_is_not_set() throws Exception {
        threadLocalSessionAccessor.getSessionId();
    }

    @Test(expected = SessionIdNotSetException.class)
    public void should_throw_SessionIfNotSet_when_only_tenant_is_set() throws Exception {
        threadLocalSessionAccessor.getSessionId();
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_IllegalArgumentException_when_session_id_negative() {
        threadLocalSessionAccessor.setSessionId(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_IllegalArgumentException_when_session_id_0() {
        threadLocalSessionAccessor.setSessionId(0);
    }

}
