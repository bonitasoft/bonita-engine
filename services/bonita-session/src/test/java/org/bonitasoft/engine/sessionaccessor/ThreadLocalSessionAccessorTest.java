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