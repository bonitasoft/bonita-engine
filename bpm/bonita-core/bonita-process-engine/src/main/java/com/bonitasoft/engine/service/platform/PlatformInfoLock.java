/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.service.platform;

import com.bonitasoft.engine.execution.LockInfo;

/**
 * @author Elias Ricken de Medeiros
 */
public class PlatformInfoLock {

    public static final int NONE_TENANT_ID = -1;
    public static final String PLATFORM_INFO_LOCK_KEY = "platformInfo";
    public static final int PLATFORM_INFO_LOCK_ID = 1;

    public static LockInfo build() {
        return new LockInfo(PLATFORM_INFO_LOCK_ID, PLATFORM_INFO_LOCK_KEY, NONE_TENANT_ID);
    }

}
