/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.service.platform;

import static com.bonitasoft.engine.execution.LockInfoAssert.assertThat;

import org.junit.Test;

public class PlatformInfoLockTest {

    public static final int NONE_TENANT_ID = -1;
    public static final String PLATFORM_INFO_LOCK_KEY = "platformInfo";
    public static final int PLATFORM_INFO_LOCK_ID = 1;

    @Test
    public void should_build_lock_for_platform_info() throws Exception {
        assertThat(PlatformInfoLock.build()).hasId(PLATFORM_INFO_LOCK_ID).hasType(PLATFORM_INFO_LOCK_KEY).hasTenantId(NONE_TENANT_ID);
    }

}
