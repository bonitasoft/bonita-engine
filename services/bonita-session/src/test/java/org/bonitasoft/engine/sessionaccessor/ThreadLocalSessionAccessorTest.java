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

    private ThreadLocalSessionAccessor threadLocalSessionAccessor = new ThreadLocalSessionAccessor();

    @Test
    public void should_set_session_info() throws Exception {
        threadLocalSessionAccessor.setSessionInfo(12, 13);

        assertThat(threadLocalSessionAccessor.getSessionId()).isEqualTo(12);
        assertThat(threadLocalSessionAccessor.getTenantId()).isEqualTo(13);
    }

    @Test(expected = SessionIdNotSetException.class)
    public void should_throw_SessionIfNotSet_when_session_is_not_set() throws Exception {
        threadLocalSessionAccessor.getSessionId();
    }

    @Test(expected = SessionIdNotSetException.class)
    public void should_throw_SessionIfNotSet_when_only_tenant_is_set() throws Exception {
        threadLocalSessionAccessor.setTenantId(1);

        threadLocalSessionAccessor.getSessionId();
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_IllegalArgumentException_when_session_id_negative() {
        threadLocalSessionAccessor.setSessionInfo(-1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_IllegalArgumentException_when_session_id_0() {
        threadLocalSessionAccessor.setSessionInfo(0, 1);
    }

    @Test
    public void should_set_tenant_id_only() throws Exception {
        threadLocalSessionAccessor.setTenantId(1);

        assertThat(threadLocalSessionAccessor.getTenantId()).isEqualTo(1);
    }

    @Test(expected = STenantIdNotSetException.class)
    public void should_throw_tenant_id_not_set_when_it_is_not_set() throws Exception {
        threadLocalSessionAccessor.getTenantId();
    }

}
