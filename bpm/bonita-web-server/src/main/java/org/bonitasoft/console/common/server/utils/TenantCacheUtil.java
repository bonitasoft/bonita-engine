/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.utils;

import org.bonitasoft.console.common.server.preferences.constants.WebBonitaConstantsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TenantCacheUtil {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TenantCacheUtil.class.getName());

    protected static String CACHE_DISK_STORE_PATH = null;

    protected static final String PROCESS_ACTOR_INITIATOR_CACHE = "processActorInitiatorCache";

    protected TenantCacheUtil() {
        try {
            CACHE_DISK_STORE_PATH = WebBonitaConstantsUtils.getTenantInstance().getFormsWorkFolder().getAbsolutePath();
        } catch (final Exception e) {
            LOGGER.warn("Unable to retrieve the path of the cache disk store directory path.", e);
        }
    }

    public Long getProcessActorInitiatorId(final Long processId) {
        return (Long) CacheUtil.get(CACHE_DISK_STORE_PATH, PROCESS_ACTOR_INITIATOR_CACHE, processId);
    }

    public Long storeProcessActorInitiatorId(final Long processId, final Long actorInitiatorId) {
        CacheUtil.store(CACHE_DISK_STORE_PATH, PROCESS_ACTOR_INITIATOR_CACHE, processId, actorInitiatorId);
        return actorInitiatorId;
    }
}
